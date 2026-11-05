package com.habitTap.util

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Asking for notifications at the moment the daily message is switched on (FR-8.6).
 *
 * Not at launch: a permission prompt before the user has asked for anything is the
 * fastest way to get it denied. And a denial is reported back rather than swallowed, so
 * the switch can stay off instead of silently promising a message that never arrives.
 */
@Composable
fun rememberDailyMessagePermission(): DailyMessagePermission {
    val context = LocalContext.current
    var pendingGrant by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        pendingGrant?.invoke(granted)
        pendingGrant = null
    }

    return remember(context, launcher) {
        DailyMessagePermission { wantOn, onResult ->
            // Turning it off never needs permission.
            if (!wantOn) {
                onResult(false)
                return@DailyMessagePermission
            }
            when {
                DailyMessageWorker.hasNotificationPermission(context) -> onResult(true)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    pendingGrant = onResult
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                // Below 13 there is no runtime permission; the user may still have turned
                // notifications off in system settings, which `hasNotificationPermission`
                // already reported.
                else -> onResult(false)
            }
        }
    }
}

/**
 * Call [request] with the state the user asked for. The callback receives whether the
 * daily message may actually be switched on.
 */
fun interface DailyMessagePermission {
    fun request(wantOn: Boolean, onResult: (Boolean) -> Unit)
}
