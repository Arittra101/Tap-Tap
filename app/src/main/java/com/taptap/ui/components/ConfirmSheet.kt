package com.taptap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.taptap.ui.theme.Spacing
import com.taptap.ui.theme.TapTapShapes
import com.taptap.ui.theme.TapTapTheme
import com.taptap.ui.theme.TapTapType

/**
 * The sheet that asks before an action that loses data: Reset (FR-3.6), "I slipped"
 * (FR-4.8) and Delete counter (FR-5.7).
 *
 * The caller supplies the copy. The design system's rule is that the title is a question,
 * the body is one sentence saying exactly what happens, and the confirm button is a verb
 * — and for a slip, that the words stay kind and say what is kept.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmSheet(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TapTapTheme.colors
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = TapTapShapes.sheet,
        containerColor = c.surfaceRaised,
        contentColor = c.ink,
        scrimColor = c.dim,
        dragHandle = null,
        modifier = modifier.semantics { paneTitle = title },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.space4,
                    end = Spacing.space4,
                    top = Spacing.space6,
                    bottom = 28.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.space4),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = title, style = TapTapType.title, color = c.ink)
                Text(text = message, style = TapTapType.body, color = c.inkMuted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.space3)) {
                TapTapButton(
                    text = cancelLabel,
                    onClick = onDismiss,
                    variant = ButtonVariant.Secondary,
                    minHeight = 48.dp,
                    modifier = Modifier.weight(1f),
                )
                TapTapButton(
                    text = confirmLabel,
                    onClick = onConfirm,
                    variant = ButtonVariant.Danger,
                    minHeight = 48.dp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
