package com.fitmate.app.reminders

import android.Manifest
import android.app.AlarmManager
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
import com.fitmate.app.MainActivity
import com.fitmate.app.R
import com.fitmate.app.model.Reminder
import java.time.ZonedDateTime

/**
 * DEPENDENCY INVERSION: the ViewModel only talks to this interface,
 * not to AlarmManager directly. (The iOS app has the same protocol backed by UNUserNotificationCenter.)
 */
interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminder: Reminder)
    fun showNow(reminder: Reminder)
}

class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminder: Reminder) {
        if (!reminder.enabled) {
            cancel(reminder)
            return
        }
        // Inexact daily alarms need no special permission; Android may shift them by a few minutes to save battery.
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(reminder),
            AlarmManager.INTERVAL_DAY,
            pendingIntentFor(reminder)
        )
    }

    override fun cancel(reminder: Reminder) {
        alarmManager.cancel(pendingIntentFor(reminder))
    }

    override fun showNow(reminder: Reminder) {
        NotificationHelper.show(context, reminder.id.hashCode(), notificationTitle(reminder), reminder.type.message)
    }

    private fun pendingIntentFor(reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, reminder.id.hashCode())
            .putExtra(ReminderReceiver.EXTRA_TITLE, notificationTitle(reminder))
            .putExtra(ReminderReceiver.EXTRA_MESSAGE, reminder.type.message)
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notificationTitle(reminder: Reminder) = "${reminder.type.emoji} ${reminder.type.label}"

    private fun nextTriggerMillis(reminder: Reminder): Long {
        val now = ZonedDateTime.now()
        var next = now.withHour(reminder.hour).withMinute(reminder.minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.toInstant().toEpochMilli()
    }
}

object NotificationHelper {
    private const val CHANNEL_ID = "fitmate_reminders"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Fitness reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Workout, water and meal reminders"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun show(context: Context, id: Int, title: String, message: String) {
        if (!canNotify(context)) return
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_fitmate)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Permission was revoked between the check and the call; nothing to show.
        }
    }
}
