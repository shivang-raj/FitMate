package com.fitmate.app.model

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/** COMPOSITION: a session *has* a list of exercises. */
class WorkoutSession(
    val day: DayOfWeek,
    val focus: String,
    val exercises: List<Exercise>
) : Describable {

    val isRestDay: Boolean get() = exercises.isEmpty()

    fun totalMinutes(): Int = exercises.sumOf { it.estimatedMinutes() }

    /** Polymorphism: each exercise is asked for calories using its *own* formula. */
    fun estimatedCalories(weightKg: Double): Double =
        exercises.sumOf { it.caloriesBurned(weightKg, it.estimatedMinutes()) }

    val dayName: String get() = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    override val title: String get() = "$dayName · $focus"
    override val subtitle: String
        get() = if (isRestDay) "Recovery day" else "${exercises.size} exercises · ~${totalMinutes()} min"
    override val emoji: String get() = if (isRestDay) "😴" else exercises.first().emoji
}

/** COMPOSITION again: a plan *has* seven sessions. */
class WorkoutPlan(
    val name: String,
    val goal: FitnessGoal,
    val level: Difficulty,
    val sessions: List<WorkoutSession>
) {
    val activeDays: Int get() = sessions.count { !it.isRestDay }

    val weeklyMinutes: Int get() = sessions.sumOf { it.totalMinutes() }

    fun weeklyCalories(weightKg: Double): Double = sessions.sumOf { it.estimatedCalories(weightKg) }

    fun sessionFor(day: DayOfWeek): WorkoutSession? = sessions.firstOrNull { it.day == day }
}
