package com.fitmate.app.suggestions

import com.fitmate.app.health.DailyCaloriesMetric
import com.fitmate.app.health.ProteinMetric
import com.fitmate.app.health.WaterIntakeMetric
import com.fitmate.app.model.BmiCategory
import com.fitmate.app.model.FitnessGoal
import com.fitmate.app.tracking.WorkoutStats
import java.util.Locale
import kotlin.math.roundToInt

/**
 * One small, independent rule = one strategy. Returning null means "nothing to say".
 * `fun interface` means a rule can also be written as a lambda (see SuggestionEngine).
 */
fun interface SuggestionRule {
    fun evaluate(context: SuggestionContext): Suggestion?
}

class InactivityRule(private val maxRestDays: Int = 2) : SuggestionRule {
    override fun evaluate(context: SuggestionContext): Suggestion? {
        val days = context.stats.daysSinceLastWorkout
        return when {
            days == null -> Suggestion(
                "Log your first workout",
                "Open Workouts, pick any exercise and tap Log. Every streak starts with day one.",
                Priority.HIGH, "🚀"
            )
            days >= maxRestDays -> Suggestion(
                "Time to get moving",
                "It's been $days days since your last workout. Even a 20-minute brisk walk counts!",
                Priority.HIGH, "⏰"
            )
            else -> null
        }
    }
}

class BmiRule : SuggestionRule {
    override fun evaluate(context: SuggestionContext): Suggestion {
        val profile = context.profile
        val bmi = String.format(Locale.US, "%.1f", profile.bmi)
        return when (profile.bmiCategory) {
            BmiCategory.UNDERWEIGHT -> Suggestion(
                "Build healthy mass",
                "Your BMI is $bmi. Add calorie-dense, protein-rich meals and strength-train 3 times a week.",
                Priority.HIGH, "🍗"
            )
            BmiCategory.NORMAL -> Suggestion(
                "Healthy BMI",
                "Your BMI of $bmi is in the healthy range. Keep mixing strength and cardio to stay there.",
                Priority.LOW, "✅"
            )
            BmiCategory.OVERWEIGHT -> Suggestion(
                "Trim down gradually",
                "Your BMI is $bmi. Aim for 150+ cardio minutes a week and a modest daily calorie deficit.",
                Priority.HIGH, "⚖️"
            )
            BmiCategory.OBESE -> Suggestion(
                "Start with low-impact cardio",
                "Your BMI is $bmi. Brisk walks and cycling protect your joints. Consider checking in with a doctor.",
                Priority.HIGH, "🚶"
            )
        }
    }
}

class GoalRule : SuggestionRule {
    override fun evaluate(context: SuggestionContext): Suggestion {
        val profile = context.profile
        return when (profile.goal) {
            FitnessGoal.LOSE_WEIGHT -> Suggestion(
                "Your calorie target",
                "Eat about ${DailyCaloriesMetric().calculate(profile).roundToInt()} kcal a day. " +
                    "A 500 kcal deficit loses roughly 0.5 kg a week.",
                Priority.MEDIUM, "🔥"
            )
            FitnessGoal.BUILD_MUSCLE -> Suggestion(
                "Protein + progressive overload",
                "Eat around ${ProteinMetric().calculate(profile).roundToInt()} g of protein daily " +
                    "and add a rep or a little weight each week.",
                Priority.MEDIUM, "💪"
            )
            FitnessGoal.STAY_FIT -> Suggestion(
                "Keep it balanced",
                "Two strength days plus three cardio days a week is a great maintenance mix.",
                Priority.LOW, "⚡"
            )
            FitnessGoal.IMPROVE_FLEXIBILITY -> Suggestion(
                "Stretch little and often",
                "10 minutes every day beats one long session. Hold each stretch 20–30 s without bouncing.",
                Priority.MEDIUM, "🧘"
            )
        }
    }
}

class WeeklyTargetRule : SuggestionRule {
    override fun evaluate(context: SuggestionContext): Suggestion {
        val minutes = context.stats.minutesThisWeek
        val target = WorkoutStats.WEEKLY_MINUTES_TARGET
        return if (minutes >= target) {
            Suggestion(
                "Weekly target smashed",
                "$minutes active minutes in the last 7 days — above the WHO's $target-minute guideline.",
                Priority.LOW, "🏆"
            )
        } else {
            Suggestion(
                "${target - minutes} min to your weekly goal",
                "You've logged $minutes of the recommended $target active minutes this week.",
                Priority.MEDIUM, "🎯"
            )
        }
    }
}

class RecoveryRule : SuggestionRule {
    override fun evaluate(context: SuggestionContext): Suggestion? {
        val activeDays = context.stats.activeDaysThisWeek
        return if (activeDays >= 6) {
            Suggestion(
                "Schedule a rest day",
                "You trained on $activeDays of the last 7 days. Muscles grow while you recover, so try yoga or full rest.",
                Priority.MEDIUM, "😴"
            )
        } else null
    }
}

class HydrationRule : SuggestionRule {
    override fun evaluate(context: SuggestionContext): Suggestion {
        val metric = WaterIntakeMetric()
        return Suggestion(
            "Stay hydrated",
            "Drink about ${metric.formatted(context.profile)} of water today. ${metric.interpret(context.profile)}",
            Priority.LOW, "💧"
        )
    }
}
