package com.fitmate.app

import android.app.Application
import android.content.Context
import com.fitmate.app.data.PrefsRepository
import com.fitmate.app.data.ProfileRepository
import com.fitmate.app.data.ReminderMapper
import com.fitmate.app.data.Repository
import com.fitmate.app.data.WorkoutLogMapper
import com.fitmate.app.model.Reminder
import com.fitmate.app.model.WorkoutLog
import com.fitmate.app.reminders.AlarmReminderScheduler
import com.fitmate.app.reminders.NotificationHelper
import com.fitmate.app.reminders.ReminderScheduler
import com.fitmate.app.suggestions.SuggestionEngine

/**
 * Manual DEPENDENCY INJECTION container: builds every shared dependency once.
 * Fields are typed as interfaces (Repository, ReminderScheduler), so implementations can be swapped.
 */
class AppContainer(context: Context) {
    private val prefs = context.getSharedPreferences("fitmate", Context.MODE_PRIVATE)

    val profileRepository = ProfileRepository(prefs)
    val workoutRepository: Repository<WorkoutLog> = PrefsRepository(prefs, "workout_logs", WorkoutLogMapper)
    val reminderRepository: Repository<Reminder> = PrefsRepository(prefs, "reminders", ReminderMapper)
    val reminderScheduler: ReminderScheduler = AlarmReminderScheduler(context)
    val suggestionEngine = SuggestionEngine()
}

class FitMateApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)
    }
}
