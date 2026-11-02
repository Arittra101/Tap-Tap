package com.taptap.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.taptap.MainActivity
import com.taptap.R
import com.taptap.data.repository.CounterRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * One local notification per quit counter per day (FR-8.6).
 *
 * The only background work in v1, and the only reason WorkManager is in the stack.
 * Reminders for normal counters are explicitly out of scope (requirements 8).
 */
class DailyMessageWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: CounterRepository by inject()

    @SuppressLint("MissingPermission") // Guarded by hasNotificationPermission() below.
    override suspend fun doWork(): Result {
        val context = applicationContext

        // Asked for when the option is turned on; if it was refused, there is nothing to
        // do and failing would just retry forever.
        if (!hasNotificationPermission(context)) return Result.success()

        ensureChannel(context)
        val manager = NotificationManagerCompat.from(context)
        val now = System.currentTimeMillis()

        repository.quitCountersWithDailyMessage().forEach { counter ->
            val quitAt = counter.quitAt ?: return@forEach
            val days = TimeUtils.daysFree(quitAt, now)

            // TT-031 decision 4: still send after a slip, but word it for the restart
            // rather than claiming a streak.
            val restarted = days == 0 && TimeUtils.totalHoursFree(quitAt, now) < 24

            val title: String
            val body: String
            if (restarted) {
                title = context.getString(R.string.notif_restart_title)
                body = context.getString(R.string.notif_restart_body)
            } else {
                title = counter.name
                val daysText = context.resources.getQuantityString(
                    R.plurals.notif_days_free, days, days,
                )
                val next = TimeUtils.daysToNextMilestone(days)
                val nextText = if (next == null) {
                    context.getString(R.string.badge_all_earned)
                } else {
                    context.resources.getQuantityString(
                        R.plurals.badge_days_to_next, next, next, TimeUtils.nextMilestone(days),
                    )
                }
                body = context.getString(R.string.notif_daily_body, daysText, nextText)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setContentIntent(openCounterIntent(context, counter.id))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

            // The permission is checked above, but it can be revoked between that check
            // and this call, so the throw is handled rather than crashing the worker.
            runCatching { manager.notify(counter.id.toInt(), notification) }
        }
        return Result.success()
    }

    /** Tapping the notification opens that quit counter. */
    private fun openCounterIntent(context: Context, counterId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_QUIT_COUNTER_ID, counterId)
        }
        return PendingIntent.getActivity(
            context,
            counterId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val CHANNEL_ID = "daily_motivation"
        const val WORK_NAME = "daily_motivation_message"
        const val EXTRA_QUIT_COUNTER_ID = "quit_counter_id"

        /** FR-8.6 default: 09:00. */
        const val DEFAULT_HOUR = 9

        fun hasNotificationPermission(context: Context): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.notif_channel_description)
                },
            )
        }

        /**
         * Schedules the daily run, first firing at the next [DEFAULT_HOUR].
         *
         * `UPDATE` rather than `REPLACE`: rescheduling on every launch must not stack a
         * second worker, or the user would get two notifications per counter per day.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyMessageWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(millisUntilNextRun(), TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        private fun millisUntilNextRun(now: Long = System.currentTimeMillis()): Long {
            val next = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, DEFAULT_HOUR)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            return next.timeInMillis - now
        }
    }
}
