package com.habitTap.model

/**
 * One counter, of either type, as the UI sees it.
 *
 * Quit-only fields are null on a normal counter and the other way round — the tech spec
 * keeps both types in one table so queries and code stay simple (spec 7.1).
 *
 * The two computed properties below exist so Home and the counter screen cannot disagree
 * about whether a tap is allowed (spec 7.3).
 */
data class Counter(
    val id: Long,
    val type: CounterType,
    val name: String,
    val notes: String?,
    /** null = same as the app's colour (FR-5.3). */
    val color: ThemeColor?,
    val background: Background,
    val isPinned: Boolean,
    /** Quit counters only, and at most one is true (FR-2.6). */
    val isFocus: Boolean,
    val createdAt: Long,
    val sortOrder: Int,

    // NORMAL
    val count: Long,
    val startValue: Long,
    val step: Int,
    val target: Long?,
    val keepCountingAfterTarget: Boolean,
    val isLocked: Boolean,
    val isReversed: Boolean,

    // QUIT
    val quitAt: Long?,
    val moneyPerDay: Double?,
    val goalDays: Int?,
    val reason: String?,
    val bestStreakDays: Int,
    val badgesOn: Boolean,
    val dailyMessageOn: Boolean,
    /** Minutes from midnight; 540 = 09:00, the FR-8.6 default. */
    val dailyMessageMinute: Int,
) {
    /** FR-3.7: at or past the target, when there is one. */
    val isTargetReached: Boolean
        get() = target != null && count >= target

    /**
     * FR-3.4 / FR-3.7: a locked counter never counts, and one that has hit its target
     * stops unless the user asked it to keep counting. Counting down is always allowed —
     * that is how you get back under a target.
     */
    val canTap: Boolean
        get() = !isLocked && !(isTargetReached && !keepCountingAfterTarget && !isReversed)
}
