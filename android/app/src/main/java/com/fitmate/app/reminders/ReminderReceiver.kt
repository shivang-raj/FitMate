package com.fitmate.app.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fitmate.app.FitMateApplication

/** INHERITANCE from an Android framework class: the OS calls our overridden onReceive when an alarm fires. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val message = intent.getStringExtra(EXTRA_MESSAGE).orEmpty()
        NotificationHelper.show(context, intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0), title, message)
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
    }
}

/** Android clears alarms on reboot, so re-schedule every enabled reminder. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val container = (context.applicationContext as FitMateApplication).container
        container.reminderRepository.getAll()
            .filter { it.enabled }
            .forEach { container.reminderScheduler.schedule(it) }
    }
}
