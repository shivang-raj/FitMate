package com.fitmate.app.planner

import com.fitmate.app.model.Difficulty
import com.fitmate.app.model.ExerciseLibrary
import com.fitmate.app.model.FitnessGoal
import com.fitmate.app.model.FlexibilityExercise
import com.fitmate.app.model.UserProfile
import com.fitmate.app.model.WorkoutPlan
import com.fitmate.app.model.WorkoutSession
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY

/** STRATEGY pattern: each fitness goal has its own algorithm for building a week. */
interface PlanStrategy {
    val goal: FitnessGoal
    fun buildPlan(level: Difficulty): WorkoutPlan
}

/**
 * TEMPLATE METHOD pattern: [buildPlan] fixes the steps (get schedule → scale to level → wrap in a plan)
 * and is `final`, so subclasses can only fill in [weeklySchedule] and [planName].
 */
abstract class BasePlanStrategy : PlanStrategy {

    protected abstract val planName: String

    protected abstract fun weeklySchedule(): List<WorkoutSession>

    final override fun buildPlan(level: Difficulty): WorkoutPlan {
        val sessions = weeklySchedule().map { session ->
            WorkoutSession(session.day, session.focus, session.exercises.map { it.scaledFor(level) })
        }
        return WorkoutPlan(planName, goal, level, sessions)
    }

    // `protected` helpers: visible to subclasses, hidden from everyone else.
    protected fun day(day: DayOfWeek, focus: String, vararg exerciseIds: String) =
        WorkoutSession(day, focus, exerciseIds.map { ExerciseLibrary[it] })

    protected fun rest(day: DayOfWeek) = WorkoutSession(day, "Rest & recover", emptyList())
}

class FatLossStrategy : BasePlanStrategy() {
    override val goal = FitnessGoal.LOSE_WEIGHT
    override val planName = "Fat Burn Program"

    override fun weeklySchedule() = listOf(
        day(MONDAY, "HIIT + Full body", "burpees", "squats", "pushups", "crunches"),
        day(TUESDAY, "Steady cardio", "running", "hamstring_stretch"),
        day(WEDNESDAY, "Active recovery", "brisk_walk", "sun_salutation", "cat_cow"),
        day(THURSDAY, "HIIT + Legs", "mountain_climbers", "lunges", "glute_bridge"),
        day(FRIDAY, "Cardio + Upper body", "cycling", "rows", "shoulder_press"),
        day(SATURDAY, "Tabata finisher", "tabata", "jump_rope", "childs_pose"),
        rest(SUNDAY)
    )
}

class MuscleGainStrategy : BasePlanStrategy() {
    override val goal = FitnessGoal.BUILD_MUSCLE
    override val planName = "Push / Pull / Legs"

    override fun weeklySchedule() = listOf(
        day(MONDAY, "Push", "pushups", "shoulder_press", "dips", "crunches"),
        day(TUESDAY, "Pull", "rows", "curls", "deadlifts"),
        day(WEDNESDAY, "Legs", "squats", "lunges", "glute_bridge", "crunches"),
        day(THURSDAY, "Mobility", "cat_cow", "hip_opener", "hamstring_stretch"),
        day(FRIDAY, "Upper body", "pushups", "rows", "shoulder_press", "curls"),
        day(SATURDAY, "Lower body + Core", "squats", "deadlifts", "crunches"),
        rest(SUNDAY)
    )
}

class GeneralFitnessStrategy : BasePlanStrategy() {
    override val goal = FitnessGoal.STAY_FIT
    override val planName = "Balanced Fitness"

    override fun weeklySchedule() = listOf(
        day(MONDAY, "Full-body strength", "squats", "pushups", "rows", "crunches"),
        day(TUESDAY, "Cardio", "running", "hamstring_stretch"),
        day(WEDNESDAY, "Yoga flow", "sun_salutation", "cobra", "childs_pose"),
        day(THURSDAY, "HIIT", "mountain_climbers", "lunges", "glute_bridge"),
        day(FRIDAY, "Cardio + Core", "cycling", "crunches"),
        day(SATURDAY, "Outdoor walk", "brisk_walk", "hip_opener"),
        rest(SUNDAY)
    )
}

class FlexibilityStrategy : BasePlanStrategy() {
    override val goal = FitnessGoal.IMPROVE_FLEXIBILITY
    override val planName = "Mobility & Yoga"

    override fun weeklySchedule(): List<WorkoutSession> {
        // Generics in action: grab every stretch in the library without listing ids.
        val allStretches = ExerciseLibrary.ofType<FlexibilityExercise>()
        return listOf(
            day(MONDAY, "Morning yoga", "sun_salutation", "cat_cow", "cobra"),
            day(TUESDAY, "Walk + Hips", "brisk_walk", "hip_opener", "hamstring_stretch"),
            WorkoutSession(WEDNESDAY, "Full stretch routine", allStretches),
            day(THURSDAY, "Core & posture", "glute_bridge", "crunches", "cobra"),
            day(FRIDAY, "Yoga flow", "sun_salutation", "childs_pose", "hip_opener"),
            day(SATURDAY, "Easy cycling", "cycling", "hamstring_stretch"),
            rest(SUNDAY)
        )
    }
}

/** FACTORY pattern: callers ask for a plan and never need to know which strategy class built it. */
object WorkoutPlanFactory {

    fun strategyFor(goal: FitnessGoal): PlanStrategy = when (goal) {
        FitnessGoal.LOSE_WEIGHT -> FatLossStrategy()
        FitnessGoal.BUILD_MUSCLE -> MuscleGainStrategy()
        FitnessGoal.STAY_FIT -> GeneralFitnessStrategy()
        FitnessGoal.IMPROVE_FLEXIBILITY -> FlexibilityStrategy()
    }

    fun createPlan(profile: UserProfile): WorkoutPlan =
        strategyFor(profile.goal).buildPlan(profile.experience)
}
