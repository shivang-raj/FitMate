package com.fitmate.app.tracking

import com.fitmate.app.model.WorkoutLog
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A read-only view over the workout history. Every number is derived from the logs,
 * never stored separately, so it can't get out of sync.
 * "This week" means the last 7 days, including today.
 */
class WorkoutStats(
    private val logs: List<WorkoutLog>,
    private val today: LocalDate = LocalDate.now()
) {
    private val weekStart: LocalDate = today.minusDays(6)

    private val thisWeek: List<WorkoutLog> =
        logs.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(today) }

    val totalWorkouts: Int get() = logs.size
    val workoutsThisWeek: Int get() = thisWeek.size
    val minutesThisWeek: Int get() = thisWeek.sumOf { it.minutes }
    val caloriesThisWeek: Double get() = thisWeek.sumOf { it.calories }
    val activeDaysThisWeek: Int get() = thisWeek.map { it.date }.distinct().size

    /** Fraction of the WHO's 150 active minutes a week, capped at 1. */
    val weeklyTargetProgress: Float
        get() = (minutesThisWeek.toFloat() / WEEKLY_MINUTES_TARGET).coerceAtMost(1f)

    /** Days since the last workout, or null if nothing has been logged yet. */
    val daysSinceLastWorkout: Int?
        get() = logs.maxOfOrNull { it.date }?.let { ChronoUnit.DAYS.between(it, today).toInt() }

    /** Consecutive days with at least one workout, ending today (or yesterday if today is still open). */
    val currentStreak: Int
        get() {
            val activeDays = logs.map { it.date }.toSet()
            var cursor = if (today in activeDays) today else today.minusDays(1)
            var streak = 0
            while (cursor in activeDays) {
                streak++
                cursor = cursor.minusDays(1)
            }
            return streak
        }

    /** Calories burned on each of the last 7 days, oldest first. Feeds the bar chart. */
    fun dailyCalories(): List<Pair<LocalDate, Double>> = (6 downTo 0).map { daysAgo ->
        val day = today.minusDays(daysAgo.toLong())
        day to thisWeek.filter { it.date == day }.sumOf { it.calories }
    }

    companion object {
        const val WEEKLY_MINUTES_TARGET = 150
    }
}
