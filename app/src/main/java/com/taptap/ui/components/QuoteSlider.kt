package com.taptap.ui.components

import android.provider.Settings
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taptap.R
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * A slowly rotating line of encouragement at the top of Home.
 *
 * It is always there — on a first run with nothing created, and afterwards. Tapping it
 * opens New counter with the quit type already chosen, so the encouragement leads
 * somewhere instead of just sitting on the screen.
 *
 * It can also be swiped, because an auto-advancing panel that cannot be controlled is
 * annoying when the line you wanted scrolls away. A swipe also restarts the dwell, so the
 * line you just chose does not slide off half a second later.
 *
 * The text area is a **fixed three lines tall**, measured from the live line height so it
 * follows the font scale. A panel that resized itself as the quotes rotated would shunt
 * the whole of Home up and down every six seconds.
 */
@Composable
fun QuoteSlider(
    quotes: List<String>,
    ctaText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    intervalMillis: Long = 6_000,
) {
    if (quotes.isEmpty()) return
    val c = TapTapTheme.colors
    val context = LocalContext.current

    val pagerState = rememberPagerState(pageCount = { quotes.size })

    // Someone who has turned animations off should not have the text moving by itself.
    val animationsOn = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }

    // Keyed on the page, so a manual swipe gives the new line a full interval of its own.
    LaunchedEffect(pagerState.currentPage, animationsOn, quotes.size) {
        if (!animationsOn || quotes.size < 2) return@LaunchedEffect
        delay(intervalMillis)
        pagerState.animateScrollToPage(
            page = (pagerState.currentPage + 1) % quotes.size,
            animationSpec = tween(500),
        )
    }

    // Read through stringResource, not context.getString: the latter is not
    // configuration-aware and would keep the previous language after an in-app
    // language change (FR-8.2).
    val panelLabel = stringResource(R.string.a11y_quote_slide, quotes[pagerState.currentPage])

    // Three lines of body text. In sp, so 200% font scale grows the panel with the text
    // instead of clipping it (NFR-7).
    val textHeight = with(LocalDensity.current) { TapTapType.body.lineHeight.toPx().times(3).toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(TapTapShapes.lg)
            .clickable(role = Role.Button, onClick = onClick)
            .background(c.accentSoft)
            // One announcement for the whole panel. Announcing each page as it rotated
            // would interrupt whatever the user was reading.
            .semantics(mergeDescendants = true) { contentDescription = panelLabel },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.space4, vertical = Spacing.space4),
            horizontalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            // A pull-quote mark in a gutter of its own, so it marks the text as a quote
            // without ever sitting on top of a line.
            Text(
                text = "“",
                style = TapTapType.title.copy(fontSize = 40.sp, lineHeight = 40.sp),
                color = c.onAccentSoft,
                modifier = Modifier
                    .alpha(0.40f)
                    .clearAndSetSemantics { },
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.space3),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(textHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) { page ->
                    Text(
                        text = quotes[page],
                        style = TapTapType.body.copy(
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = c.onAccentSoft,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            // Fade the outgoing line out and the incoming one in, so the
                            // change is a dissolve rather than a hard slide. The offset is
                            // read inside graphicsLayer, which runs at draw time — reading it
                            // in composition would recompose every frame of a swipe.
                            .graphicsLayer {
                                val offset = (
                                    (pagerState.currentPage - page) +
                                        pagerState.currentPageOffsetFraction
                                    ).absoluteValue
                                alpha = 1f - (offset * 1.4f).coerceIn(0f, 1f)
                            }
                            .clearAndSetSemantics { },
                    )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Dots(count = quotes.size, selected = pagerState.currentPage)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = ctaText,
                        style = TapTapType.label,
                        color = c.onAccentSoft,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = TapTapIcons.ChevronRight,
                        contentDescription = null,
                        tint = c.onAccentSoft,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            }
        }
    }
}

/**
 * Position dots. The current one is a short bar rather than a bigger circle: the shape
 * difference survives both low vision and a greyscale screen, which a size difference
 * alone does not (NFR-6).
 */
@Composable
private fun Dots(count: Int, selected: Int) {
    val c = TapTapTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clearAndSetSemantics { },
    ) {
        repeat(count) { i ->
            val on = i == selected
            Box(
                Modifier
                    .width(if (on) 16.dp else 6.dp)
                    .height(6.dp)
                    // alpha must come first: it only fades what is drawn after it.
                    .alpha(if (on) 1f else 0.30f)
                    .background(c.onAccentSoft, TapTapShapes.pill),
            )
        }
    }
}
