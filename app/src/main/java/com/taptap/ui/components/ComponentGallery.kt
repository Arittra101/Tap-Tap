package com.taptap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.taptap.model.Background
import com.taptap.model.Counter
import com.taptap.model.CounterType
import com.taptap.model.ThemeColor
import com.taptap.model.ThemeMode
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * A gallery of every component in Epic C, used as the `@Preview` the tickets ask for and
 * as a quick visual check on a device.
 */
@Composable
fun ComponentGallery(modifier: Modifier = Modifier) {
    val c = TapTapTheme.colors
    Column(
        modifier = modifier
            .background(c.bg)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.space4),
        verticalArrangement = Arrangement.spacedBy(Spacing.space6),
    ) {
        Section("Buttons") {
            TapTapButton("Create counter", {}, variant = ButtonVariant.Primary)
            TapTapButton("Cancel", {}, variant = ButtonVariant.Secondary)
            TapTapButton("Reset", {}, variant = ButtonVariant.Danger)
            TapTapButton("I slipped", {}, variant = ButtonVariant.DangerOutline, icon = TapTapIcons.Reset)
            TapTapButton("New counter", {}, variant = ButtonVariant.Fab, icon = TapTapIcons.Plus)
            TapTapButton("Create counter", {}, enabled = false)
        }

        Section("Status pills") {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                StatusPill("Step 5")
                StatusPill("Locked", tone = PillTone.Muted, icon = TapTapIcons.Lock)
                StatusPill("Target reached", tone = PillTone.Success, icon = TapTapIcons.Check)
            }
        }

        Section("Dial — counting") {
            CounterDial(32, DialState.Counting, "Step 1", "Tap to +1", "Add 1", {}, target = 50, ofText = "of 50")
        }
        Section("Dial — locked") {
            CounterDial(32, DialState.Locked, "Locked", "Locked", "Counter is locked", {}, target = 50, ofText = "of 50")
        }
        Section("Dial — target reached") {
            CounterDial(50, DialState.TargetReached, "Target reached", "Done for today", "Target reached", {}, target = 50, ofText = "of 50")
        }

        Section("Stat strips") {
            StatStrip(listOf(Stat("+12", "Today"), Stat("18", "Left to target"), Stat("64%", "Done")))
            StatStrip(listOf(Stat("৳2,100", "Money saved"), Stat("41", "Urges beaten"), Stat("21 days", "Best streak")))
        }

        Section("Control bar") {
            ControlBar(false, false, "Lock", "Reverse", "Reset", {}, {}, {})
            ControlBar(true, true, "Unlock", "Count up", "Reset", {}, {}, {})
        }

        Section("Cards") {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
            ) {
                CounterCard(sampleNormal, {}, {}, {}, "Add 1 to Push-ups", "Pin Push-ups", "Open Push-ups", "Target reached", "Step 1", "/ 50")
                CounterCard(sampleDone, {}, {}, {}, "Add", "Unpin", "Open", "Target reached", "Target reached", "/ 8")
                NewCounterCard("New counter", onClick = {})
            }
            QuitCard(
                counter = sampleQuit,
                daysFree = 14,
                daysFreeLabel = "days free",
                sideText = "৳2,100 saved",
                nextBadgeText = "7 days to your 21-day badge",
                milestoneProgress = 0.4f,
                onOpen = {},
                onMakeFocus = {},
                onTogglePin = {},
                makeFocusLabel = "Make Quit cigarettes your focus",
                makeFocusText = "Make focus",
                pinLabel = "Pin Quit cigarettes",
                openLabel = "Open Quit cigarettes",
            )
            FocusCard(
                sampleQuit, 14, 0.0f, "7 days to your 21-day badge", "৳2,100", "3",
                FocusCardLabels("days free", "money saved", "urges beaten today", "I beat an urge", "Open"),
                {}, {},
            )
            FocusPicker(
                listOf(sampleQuit, sampleQuit.copy(id = 2, name = "Quit soft drinks", color = ThemeColor.SAFFRON)),
                "Pick one habit to focus on",
                "It stays big at the top of Home, so you see your progress first every day.",
                {},
            )
        }

        Section("Milestones") {
            MilestoneRow(
                listOf(1, 3, 7, 14, 21, 30, 60).mapIndexed { i, d ->
                    MilestoneBadgeData(
                        days = d,
                        label = if (d == 1) "1 day" else "$d days",
                        state = when {
                            d <= 14 -> BadgeState.Earned
                            d == 21 -> BadgeState.Next
                            else -> BadgeState.Locked
                        },
                        stateLabel = when {
                            d <= 14 -> "earned"; d == 21 -> "next"; else -> "locked"
                        },
                        badgeWord = if (d == 1) "day badge" else "day badge",
                    )
                },
                contentPadding = 0.dp,
            )
        }

        Section("Form") {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
                TypeCard("Normal counter", "Count anything: reps, glasses of water, pages", TapTapIcons.Plus, true, {}, Modifier.weight(1f))
                TypeCard("Quit counter", "Stay away from a bad habit, day by day", TapTapIcons.Quit, false, {}, Modifier.weight(1f))
            }
            ChipRow {
                TapTapChip("Cigarettes", true, {})
                TapTapChip("Alcohol", false, {})
                TapTapChip("Soft drinks", false, {})
                TapTapChip("Phone at night", false, {})
            }
            TapTapTextField("Push-ups", {}, "Counter name", required = true, placeholder = "e.g. Push-ups")
            TapTapTextField("", {}, "Your reason to quit", optionalLabel = "Optional", singleLine = false, minHeight = 88.dp, placeholder = "e.g. To breathe better on my morning walk", help = "We show it to you when you open this counter and when an urge comes.")
            SegmentedControl(listOf(Segment("l", "Light"), Segment("d", "Dark"), Segment("s", "System")), "s", {})
            SettingGroup(heading = "When you tap") {
                SwitchRow("Vibration", true, {}, help = "Short buzz on every + and −", icon = TapTapIcons.Vibrate)
                SettingDivider()
                SwitchRow("Sound", false, {}, help = "Soft click on every + and −", icon = TapTapIcons.Sound)
                SettingDivider()
                ValueRow("Privacy policy", icon = TapTapIcons.Shield, showChevron = true, onClick = {})
            }
            Stepper("Step value", 5, {}, "Lower step value", "Raise step value", help = "Added per tap", min = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                ColorSwatch("App", null, null, false, {}, appOptionLabel = "App")
                ColorSwatch("Pink", c.swatchPink, c.onAccent, true, {})
                ColorSwatch("Teal", c.swatchTeal, c.onAccent, false, {})
                ColorSwatch("Saffron", c.swatchSaffron, c.onAccent, false, {})
                ColorSwatch("Blue", c.swatchBlue, c.onAccent, false, {})
            }
        }

        Section("Backgrounds") {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                Background.entries.forEach { BackgroundTile(it, Modifier.size(64.dp)) }
            }
            Box(Modifier.fillMaxWidth().height(180.dp)) {
                CounterBackground(Background.WAVES)
                Text("32", style = TapTapType.countXl, color = c.ink, modifier = Modifier.padding(Spacing.space4))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val c = TapTapTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space3)) {
        Text(title, style = TapTapType.title, color = c.inkMuted)
        content()
    }
}

private val sampleNormal = Counter(
    id = 1, type = CounterType.NORMAL, name = "Push-ups", notes = null, color = ThemeColor.TEAL,
    background = Background.NONE, isPinned = false, isFocus = false, createdAt = 0, sortOrder = 0,
    count = 32, startValue = 0, step = 1, target = 50, keepCountingAfterTarget = false,
    isLocked = false, isReversed = false, quitAt = null, moneyPerDay = null, goalDays = null,
    reason = null, bestStreakDays = 0, badgesOn = true, dailyMessageOn = true, dailyMessageMinute = 540,
)

private val sampleDone = sampleNormal.copy(
    id = 2, name = "Glasses of water", color = ThemeColor.SAFFRON, count = 8, target = 8, isPinned = true,
)

private val sampleQuit = Counter(
    id = 3, type = CounterType.QUIT, name = "Quit cigarettes", notes = null, color = ThemeColor.PINK,
    background = Background.NONE, isPinned = false, isFocus = true, createdAt = 0, sortOrder = 0,
    count = 0, startValue = 0, step = 1, target = null, keepCountingAfterTarget = false,
    isLocked = false, isReversed = false, quitAt = 0, moneyPerDay = 150.0, goalDays = 30,
    reason = "To breathe better on my morning walk.", bestStreakDays = 21, badgesOn = true,
    dailyMessageOn = true, dailyMessageMinute = 540,
)

@Preview(name = "Components · Pink Light", heightDp = 3200)
@Composable
private fun GalleryLight() {
    TapTapTheme(ThemeColor.PINK, ThemeMode.LIGHT) { ComponentGallery() }
}

@Preview(name = "Components · Teal Dark", heightDp = 3200)
@Composable
private fun GalleryDark() {
    TapTapTheme(ThemeColor.TEAL, ThemeMode.DARK) { ComponentGallery() }
}
