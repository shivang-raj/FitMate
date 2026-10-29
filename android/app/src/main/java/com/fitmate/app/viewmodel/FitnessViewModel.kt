package com.fitmate.app.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.fitmate.app.FitMateApplication
import com.fitmate.app.health.HealthMetric
import com.fitmate.app.health.HealthMetrics
import com.fitmate.app.model.Exercise
import com.fitmate.app.model.Reminder
import com.fitmate.app.model.ReminderType
import com.fitmate.app.model.UserProfile
import com.fitmate.app.model.WorkoutLog
import com.fitmate.app.model.WorkoutPlan
import com.fitmate.app.planner.WorkoutPlanFactory
import com.fitmate.app.suggestions.Suggestion
import com.fitmate.app.suggestions.SuggestionContext
import com.fitmate.app.tracking.WorkoutStats

/**
 * The single source of truth for the UI.
 * ENCAPSULATION: screens can *read* state, but every change goes through a method below
 * (`private set`), which also keeps storage and alarms in sync.
 */
class FitnessViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as FitMateApplication).container

    var profile by mutableStateOf(container.profileRepository.load())
        private set

    var logs by mutableStateOf(loadLogs())
        private set

    var reminders by mutableStateOf(container.reminderRepository.getAll())
        private set

    val healthMetrics: List<HealthMetric> = HealthMetrics.all

    // Derived state: recomputed from profile/logs whenever they change.
    val stats: WorkoutStats get() = WorkoutStats(logs)

    val plan: WorkoutPlan? get() = profile?.let { WorkoutPlanFactory.createPlan(it) }

    val suggestions: List<Suggestion>
        get() = profile?.let { container.suggestionEngine.suggestionsFor(SuggestionContext(it, stats)) }.orEmpty()

    fun saveProfile(newProfile: UserProfile) {
        container.profileRepository.save(newProfile)
        profile = newProfile
    }

    fun logWorkout(exercise: Exercise, minutes: Int) {
        val weight = profile?.weightKg ?: return
        container.workoutRepository.save(WorkoutLog.from(exercise, minutes, weight))
        logs = loadLogs()
    }

    fun deleteLog(log: WorkoutLog) {
        container.workoutRepository.delete(log.id)
        logs = loadLogs()
    }

    fun addReminder(type: ReminderType, hour: Int, minute: Int) {
        val reminder = Reminder(type = type, hour = hour, minute = minute)
        container.reminderRepository.save(reminder)
        container.reminderScheduler.schedule(reminder)
        reminders = container.reminderRepository.getAll()
    }

    fun setReminderEnabled(reminder: Reminder, enabled: Boolean) {
        val updated = reminder.copy(enabled = enabled)
        container.reminderRepository.save(updated)
        container.reminderScheduler.schedule(updated) // schedule() cancels when disabled
        reminders = container.reminderRepository.getAll()
    }

    fun deleteReminder(reminder: Reminder) {
        container.reminderScheduler.cancel(reminder)
        container.reminderRepository.delete(reminder.id)
        reminders = container.reminderRepository.getAll()
    }

    fun testReminder(reminder: Reminder) = container.reminderScheduler.showNow(reminder)

    private fun loadLogs(): List<WorkoutLog> =
        container.workoutRepository.getAll().sortedByDescending { it.timestamp }
}
