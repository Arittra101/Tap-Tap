package com.habitTap.ui.tutorial

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.habitTap.R
import com.habitTap.model.ThemeColor
import com.habitTap.model.ThemeMode
import com.habitTap.ui.components.ButtonVariant
import com.habitTap.ui.components.TapTapButton
import com.habitTap.ui.components.TapTapIcons
import com.habitTap.ui.components.WelcomeMark
import com.habitTap.ui.theme.MinTouchTarget
import com.habitTap.ui.theme.Spacing
import com.habitTap.ui.theme.TapTapTheme
import com.habitTap.ui.theme.TapTapType
import kotlinx.coroutines.launch

/**
 * One slide of the first-run tutorial.
 *
 * A null [icon] means the slide uses the welcome ring instead — the same mark Home's
 * empty state shows, so the first thing a new user sees is the first thing they see again
 * on Home.
 */
private data class TutorialPage(
    @StringRes val title: Int,
    @StringRes val body: Int,
    val icon: ImageVector?,
)

private val Pages = listOf(
    TutorialPage(R.string.home_welcome_title, R.string.tutorial_1_body, null),
    TutorialPage(R.string.tutorial_2_title, R.string.tutorial_2_body, TapTapIcons.Plus),
    TutorialPage(R.string.tutorial_3_title, R.string.tutorial_3_body, TapTapIcons.Quit),
    TutorialPage(R.string.tutorial_4_title, R.string.tutorial_4_body, TapTapIcons.Settings),
)

/**
 * The tutorial a new user sees once, before Home.
 *
 * It holds no state worth keeping: which slide is showing lives in the pager, and the one
 * thing that outlives the screen — that it has been seen — is [onDone]'s business. So
 * there is no ViewModel here, unlike the rest of the app's screens.
 *
 * [onDone] is both "Get started" on the last slide and "Skip" on any slide: either way
 * the user is finished with it and should not be shown it again.
 */
@Composable
fun TutorialScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val c = TapTapTheme.colors
    val pagerState = rememberPagerState { Pages.size }
    val scope = rememberCoroutineScope()
    val onLastPage = pagerState.currentPage == Pages.lastIndex

    // Back walks the slides backwards. On the first slide it is left alone, so back does
    // what it does anywhere else at the start of the app: leaves.
    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding(),
    ) {
        // Skip keeps its row even on the last slide, where it would only repeat the
        // button below it — without the fixed height the slides would jump up by 48dp.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTarget)
                .padding(horizontal = Spacing.space2),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (!onLastPage) {
                Text(
                    text = stringResource(R.string.tutorial_skip),
                    style = TapTapType.label,
                    color = c.inkMuted,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onDone)
                        .padding(horizontal = Spacing.space3, vertical = Spacing.space3),
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { index ->
            Slide(page = Pages[index])
        }

        PageDots(
            count = Pages.size,
            current = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.space6),
        )

        TapTapButton(
            text = stringResource(
                if (onLastPage) R.string.tutorial_start else R.string.tutorial_next,
            ),
            onClick = {
                if (onLastPage) {
                    onDone()
                } else {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            variant = ButtonVariant.Primary,
            minHeight = 56.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.space4),
        )
        // The bottom inset as its own spacer, the way the form screens do it: hung off a
        // fixed height it would be measured away and the navigation bar would sit on the
        // button.
        Spacer(Modifier.height(Spacing.space4))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun Slide(page: TutorialPage) {
    val c = TapTapTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.space6),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (page.icon == null) WelcomeMark() else SlideMark(page.icon)

        Spacer(Modifier.height(Spacing.space8))

        Text(
            text = stringResource(page.title),
            style = TapTapType.titleLg,
            color = c.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(Spacing.space3))
        Text(
            text = stringResource(page.body),
            style = TapTapType.body,
            color = c.inkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(300.dp),
        )
    }
}

/** The 168dp mark for the slides that are about one feature, matching [WelcomeMark]'s size. */
@Composable
private fun SlideMark(icon: ImageVector) {
    val c = TapTapTheme.colors
    Box(
        modifier = Modifier
            .size(168.dp)
            .clip(CircleShape)
            .background(c.accentSoft)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = c.accentStrong, modifier = Modifier.size(72.dp))
    }
}

/**
 * Which slide of how many.
 *
 * One label for the whole row, not one per dot: a screen reader should hear "Step 2 of 4"
 * once, not four decorations it cannot act on.
 */
@Composable
private fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val c = TapTapTheme.colors
    val label = stringResource(R.string.tutorial_page_of, current + 1, count)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = label },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .padding(horizontal = Spacing.space1)
                    .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(if (active) c.accent else c.lineStrong),
            )
        }
    }
}

@Preview(name = "Tutorial · first slide", heightDp = 800)
@Composable
private fun TutorialFirstPreview() {
    TapTapTheme(ThemeColor.SAFFRON, ThemeMode.LIGHT) {
        TutorialScreen(onDone = {})
    }
}

@Preview(name = "Tutorial · feature slide (dark)", heightDp = 800)
@Composable
private fun TutorialSlidePreview() {
    TapTapTheme(ThemeColor.SAFFRON, ThemeMode.DARK) {
        Column(Modifier.fillMaxSize().background(TapTapTheme.colors.bg)) {
            Slide(page = remember { Pages[2] })
        }
    }
}
