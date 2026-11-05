package com.habitTap.ui.home

import com.habitTap.model.Background
import com.habitTap.model.Counter
import com.habitTap.model.CounterType
import com.habitTap.model.ThemeColor

/**
 * Sample data for Home's previews, matching the counters the design canvas shows so a
 * preview can be compared against the artboard side by side.
 */
internal object HomePreviewData {

    private const val DAY = 24L * 60 * 60 * 1000
    private val now = 1_762_000_000_000L

    private fun normal(
        id: Long,
        name: String,
        color: ThemeColor,
        count: Long,
        target: Long?,
        step: Int = 1,
        pinned: Boolean = false,
    ) = Counter(
        id = id, type = CounterType.NORMAL, name = name, notes = null, color = color,
        background = Background.NONE, isPinned = pinned, isFocus = false, createdAt = now,
        sortOrder = id.toInt(), count = count, startValue = 0, step = step, target = target,
        keepCountingAfterTarget = false, isLocked = false, isReversed = false,
        quitAt = null, moneyPerDay = null, goalDays = null, reason = null,
        bestStreakDays = 0, badgesOn = true, dailyMessageOn = true, dailyMessageMinute = 540,
    )

    private fun quit(
        id: Long,
        name: String,
        color: ThemeColor,
        days: Int,
        perDay: Double?,
        reason: String,
        focus: Boolean = false,
        pinned: Boolean = false,
    ) = Counter(
        id = id, type = CounterType.QUIT, name = name, notes = null, color = color,
        background = Background.NONE, isPinned = pinned, isFocus = focus, createdAt = now,
        sortOrder = id.toInt(), count = 0, startValue = 0, step = 1, target = null,
        keepCountingAfterTarget = false, isLocked = false, isReversed = false,
        quitAt = now - days * DAY, moneyPerDay = perDay, goalDays = 30, reason = reason,
        bestStreakDays = 21, badgesOn = true, dailyMessageOn = true, dailyMessageMinute = 540,
    )

    private val normals = listOf(
        normal(2, "Glasses of water", ThemeColor.SAFFRON, 5, 8, pinned = true),
        normal(1, "Push-ups", ThemeColor.TEAL, 32, 50),
        normal(3, "Pages read", ThemeColor.BLUE, 30, 30, step = 5),
        normal(4, "Cups of tea", ThemeColor.PINK, 2, null),
    )

    private val cigarettes = quit(11, "Quit cigarettes", ThemeColor.PINK, 14, 150.0, "To breathe better on my morning walk.", focus = true)
    private val soda = quit(12, "Quit soft drinks", ThemeColor.SAFFRON, 6, 60.0, "Less sugar, more energy.")
    private val phone = quit(13, "Less phone at night", ThemeColor.BLUE, 3, null, "Sleep before midnight.", pinned = true)

    val withFocus = HomeState(
        isLoading = false,
        focus = cigarettes,
        focusUrgesToday = 3,
        normalCounters = normals,
        quitCounters = listOf(phone, soda),
        quitUrgesToday = mapOf(12L to 1, 13L to 5),
        allQuitCounters = listOf(cigarettes, soda, phone),
        nowMillis = now,
    )

    val noFocus = withFocus.copy(
        focus = null,
        showFocusPicker = true,
        quitCounters = listOf(phone, cigarettes.copy(isFocus = false), soda),
    )
}
