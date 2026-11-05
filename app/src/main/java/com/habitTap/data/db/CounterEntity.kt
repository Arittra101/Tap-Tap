package com.habitTap.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.habitTap.model.Background
import com.habitTap.model.CounterType
import com.habitTap.model.ThemeColor

/**
 * Both counter types live in one table, with the quit-only columns nullable.
 *
 * The tech spec (7.1) chose this over two tables deliberately: Home reads every counter
 * in one query, and there is no join to get a counter's own colour or pin state.
 */
@Entity(tableName = "counters")
data class CounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: CounterType,
    val name: String,
    val notes: String? = null,
    /** null = same as the app's colour (FR-5.3). */
    val color: ThemeColor? = null,
    val background: Background = Background.NONE,
    val isPinned: Boolean = false,
    /** QUIT only; at most one row has this true (FR-2.6). Enforced by [CounterDao.setFocus]. */
    val isFocus: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long,

    // ----- NORMAL -----
    val count: Long = 0,
    val startValue: Long = 0,
    val stepValue: Int = 1,
    val target: Long? = null,
    val keepCountingAfterTarget: Boolean = false,
    val isLocked: Boolean = false,
    val isReversed: Boolean = false,

    // ----- QUIT -----
    val quitAt: Long? = null,
    val moneyPerDay: Double? = null,
    val goalDays: Int? = null,
    val reason: String? = null,
    val bestStreakDays: Int = 0,
    val badgesOn: Boolean = true,
    val dailyMessageOn: Boolean = true,
    /** Minutes from midnight; 540 = 09:00, the FR-8.6 default. */
    val dailyMessageMinute: Int = 9 * 60,
)
