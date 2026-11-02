package com.taptap.data.repository

import com.taptap.data.db.CounterDao
import com.taptap.data.db.CounterEntity
import com.taptap.data.db.CounterEventEntity
import com.taptap.model.Counter
import com.taptap.model.CounterType
import com.taptap.model.EventKind
import com.taptap.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Every counting rule in the app.
 *
 * The tech spec (4.3) is explicit that these live here and not in a ViewModel, because
 * Home's `+` button and the counter screen's dial must produce the same result for the
 * same counter. One implementation is the only way to guarantee that.
 *
 * [clock] is injectable so the rules can be tested without waiting for real time.
 */
class CounterRepository(
    private val dao: CounterDao,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {

    /**
     * Mutations are read-modify-write, so they are serialised.
     *
     * NFR-2 says fast repeated taps are never lost: without this, two taps that read the
     * same count would both write count+step and one would vanish.
     */
    private val writeLock = Mutex()

    // --- reads ------------------------------------------------------------------------

    fun observeAll(): Flow<List<Counter>> =
        dao.observeAll().map { rows -> rows.map { it.toModel() } }

    fun observeCounter(id: Long): Flow<Counter?> =
        dao.observeById(id).map { it?.toModel() }

    suspend fun getCounter(id: Long): Counter? = dao.getById(id)?.toModel()

    /** Today's change: tap deltas since [since], which callers set to local midnight. */
    fun observeChangeSince(id: Long, since: Long): Flow<Long> = dao.observeChangeSince(id, since)

    /** Urges beaten since [since] (FR-1.2, FR-4.4). */
    fun observeUrgesSince(id: Long, since: Long): Flow<Int> = dao.observeUrgesSince(id, since)

    /** Urges beaten all time (FR-4.3). */
    fun observeUrgesTotal(id: Long): Flow<Int> = dao.observeUrgesTotal(id)

    /** Urges beaten since [since], keyed by counter — one query for the whole of Home. */
    fun observeUrgesTodayAll(since: Long): Flow<Map<Long, Int>> =
        dao.observeUrgesTodayAll(since).map { rows -> rows.associate { it.counterId to it.urges } }

    suspend fun quitCountersWithDailyMessage(): List<Counter> =
        dao.getQuitCountersWithDailyMessage().map { it.toModel() }

    // --- counting ---------------------------------------------------------------------

    /**
     * One tap (FR-3.3).
     *
     * Locked counters ignore taps (FR-3.4). Reversed counters subtract and never go below
     * zero (FR-3.5). With a target and "keep counting" off, the count stops at the target
     * (FR-3.7). A tap that would change nothing writes nothing — so the event log stays an
     * honest record of what actually moved.
     *
     * @return the delta actually applied, 0 when the tap did nothing.
     */
    suspend fun tap(id: Long): Long = writeLock.withLock {
        val c = dao.getById(id) ?: return@withLock 0L
        if (c.isLocked) return@withLock 0L

        val step = c.stepValue.toLong()
        var next = if (c.isReversed) c.count - step else c.count + step
        next = next.coerceAtLeast(0L)
        if (!c.isReversed && c.target != null && !c.keepCountingAfterTarget) {
            next = next.coerceAtMost(c.target)
        }

        val delta = next - c.count
        if (delta == 0L) return@withLock 0L

        val at = clock()
        dao.update(c.copy(count = next))
        dao.insertEvent(
            CounterEventEntity(counterId = id, kind = EventKind.TAP, delta = delta, at = at),
        )
        delta
    }

    /** Reset goes back to the counter's start value, not to zero (FR-3.6). */
    suspend fun reset(id: Long): Unit = writeLock.withLock {
        val c = dao.getById(id) ?: return@withLock
        dao.update(c.copy(count = c.startValue))
        dao.insertEvent(
            CounterEventEntity(counterId = id, kind = EventKind.RESET, at = clock()),
        )
    }

    /** Logs one urge beaten. It does not touch days free (FR-4.4). */
    suspend fun beatUrge(id: Long): Unit = writeLock.withLock {
        dao.getById(id) ?: return@withLock
        dao.insertEvent(
            CounterEventEntity(counterId = id, kind = EventKind.URGE_BEATEN, at = clock()),
        )
    }

    /**
     * "I slipped" (FR-4.8): days free start again from now, and the run just ended is
     * kept as the best streak if it beats the old one. Badges and urges are untouched.
     */
    suspend fun slip(id: Long): Unit = writeLock.withLock {
        val c = dao.getById(id) ?: return@withLock
        val now = clock()
        val streak = TimeUtils.daysFree(c.quitAt ?: now, now)
        dao.update(c.copy(quitAt = now, bestStreakDays = maxOf(c.bestStreakDays, streak)))
        dao.insertEvent(CounterEventEntity(counterId = id, kind = EventKind.SLIP, at = now))
    }

    // --- flags ------------------------------------------------------------------------

    /** Exactly one quit counter is the focus (FR-2.6, FR-4.7). */
    suspend fun setFocus(id: Long): Unit = writeLock.withLock {
        val c = dao.getById(id) ?: return@withLock
        if (c.type != CounterType.QUIT) return@withLock
        dao.setFocus(id)
    }

    /** Turns the focus off without choosing another one. */
    suspend fun clearFocus(): Unit = writeLock.withLock { dao.clearFocus() }

    suspend fun togglePin(id: Long): Unit = writeLock.withLock {
        val c = dao.getById(id) ?: return@withLock
        dao.update(c.copy(isPinned = !c.isPinned))
    }

    suspend fun toggleLock(id: Long): Unit = writeLock.withLock {
        val c = dao.getById(id) ?: return@withLock
        dao.update(c.copy(isLocked = !c.isLocked))
    }

    suspend fun toggleReverse(id: Long): Unit = writeLock.withLock {
        val c = dao.getById(id) ?: return@withLock
        dao.update(c.copy(isReversed = !c.isReversed))
    }

    // --- lifecycle --------------------------------------------------------------------

    /**
     * Creates a counter and returns its id (FR-2.7).
     *
     * A new counter that asks to be the focus takes it from whichever counter had it
     * (FR-2.6).
     */
    suspend fun create(counter: Counter): Long = writeLock.withLock {
        val entity = counter.toEntity().copy(
            id = 0,
            createdAt = if (counter.createdAt > 0) counter.createdAt else clock(),
            sortOrder = dao.nextSortOrder(counter.type.name),
            isFocus = false,
        )
        val id = dao.insert(entity)
        if (counter.type == CounterType.QUIT && counter.isFocus) dao.setFocus(id)
        id
    }

    /**
     * Saves edits (FR-5.7).
     *
     * The current count is deliberately not taken from [counter]: FR-5.4 says a new step
     * value works from the next tap and a new start value from the next reset, and the
     * count never jumps. Edit has no business moving it.
     */
    suspend fun update(counter: Counter): Unit = writeLock.withLock {
        val existing = dao.getById(counter.id) ?: return@withLock
        val wantsFocus = counter.type == CounterType.QUIT && counter.isFocus
        dao.update(
            counter.toEntity().copy(
                count = existing.count,
                createdAt = existing.createdAt,
                sortOrder = existing.sortOrder,
                isFocus = existing.isFocus,
                bestStreakDays = existing.bestStreakDays,
            ),
        )
        if (wantsFocus && !existing.isFocus) dao.setFocus(counter.id)
        if (!counter.isFocus && existing.isFocus) dao.clearFocus()
    }

    /** Deletes the counter; its events go with it through the foreign key (FR-5.7). */
    suspend fun delete(id: Long): Unit = writeLock.withLock { dao.delete(id) }
}

// ---------------------------------------------------------------------------------------
// Mapping. A private extension at the bottom of this file: the tech spec (7.3) rules out a
// mappers package.
// ---------------------------------------------------------------------------------------

private fun CounterEntity.toModel() = Counter(
    id = id,
    type = type,
    name = name,
    notes = notes,
    color = color,
    background = background,
    isPinned = isPinned,
    isFocus = isFocus,
    createdAt = createdAt,
    sortOrder = sortOrder,
    count = count,
    startValue = startValue,
    step = stepValue,
    target = target,
    keepCountingAfterTarget = keepCountingAfterTarget,
    isLocked = isLocked,
    isReversed = isReversed,
    quitAt = quitAt,
    moneyPerDay = moneyPerDay,
    goalDays = goalDays,
    reason = reason,
    bestStreakDays = bestStreakDays,
    badgesOn = badgesOn,
    dailyMessageOn = dailyMessageOn,
    dailyMessageMinute = dailyMessageMinute,
)

private fun Counter.toEntity() = CounterEntity(
    id = id,
    type = type,
    name = name,
    notes = notes,
    color = color,
    background = background,
    isPinned = isPinned,
    isFocus = isFocus,
    sortOrder = sortOrder,
    createdAt = createdAt,
    count = count,
    startValue = startValue,
    stepValue = step,
    target = target,
    keepCountingAfterTarget = keepCountingAfterTarget,
    isLocked = isLocked,
    isReversed = isReversed,
    quitAt = quitAt,
    moneyPerDay = moneyPerDay,
    goalDays = goalDays,
    reason = reason,
    bestStreakDays = bestStreakDays,
    badgesOn = badgesOn,
    dailyMessageOn = dailyMessageOn,
    dailyMessageMinute = dailyMessageMinute,
)
