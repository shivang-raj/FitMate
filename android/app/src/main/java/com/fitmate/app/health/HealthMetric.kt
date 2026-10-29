package com.fitmate.app.health

import com.fitmate.app.model.ActivityLevel
import com.fitmate.app.model.FitnessGoal
import com.fitmate.app.model.UserProfile
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * ABSTRACTION: every metric knows how to calculate and explain itself.
 * The Health screen just loops over a List<HealthMetric> — it never needs
 * to know which concrete metric it is drawing (POLYMORPHISM).
 */
interface HealthMetric {
    val name: String
    val emoji: String

    fun calculate(profile: UserProfile): Double

    fun interpret(profile: UserProfile): String

    /** Interface method with a default body; implementations may override it. */
    fun formatted(profile: UserProfile): String = String.format(Locale.US, "%.1f", calculate(profile))
}

class BmiMetric : HealthMetric {
    override val name = "Body Mass Index"
    override val emoji = "⚖️"

    override fun calculate(profile: UserProfile) = profile.bmi

    override fun interpret(profile: UserProfile) =
        "${profile.bmiCategory.label}. ${profile.bmiCategory.advice}"
}

/** Mifflin–St Jeor equation: calories your body burns at complete rest. */
class BmrMetric : HealthMetric {
    override val name = "Basal Metabolic Rate"
    override val emoji = "🔋"

    override fun calculate(profile: UserProfile): Double =
        10 * profile.weightKg + 6.25 * profile.heightCm - 5 * profile.age + profile.gender.bmrOffset

    override fun formatted(profile: UserProfile) = "${calculate(profile).roundToInt()} kcal"

    override fun interpret(profile: UserProfile) = "Energy your body uses at complete rest."
}

/** COMPOSITION: reuses a BmrMetric instead of copying its formula. */
class DailyCaloriesMetric(private val bmr: BmrMetric = BmrMetric()) : HealthMetric {
    override val name = "Daily calorie target"
    override val emoji = "🍽️"

    override fun calculate(profile: UserProfile): Double =
        bmr.calculate(profile) * profile.activityLevel.multiplier + profile.goal.calorieAdjustment

    override fun formatted(profile: UserProfile) = "${calculate(profile).roundToInt()} kcal"

    override fun interpret(profile: UserProfile) = when (profile.goal) {
        FitnessGoal.LOSE_WEIGHT -> "Includes a 500 kcal deficit for about 0.5 kg of fat loss a week."
        FitnessGoal.BUILD_MUSCLE -> "Includes a 300 kcal surplus to fuel muscle growth."
        else -> "Eat around this much to maintain your current weight."
    }
}

class WaterIntakeMetric : HealthMetric {
    override val name = "Daily water"
    override val emoji = "💧"

    override fun calculate(profile: UserProfile): Double {
        val base = profile.weightKg * 0.033
        val activeBonus = if (profile.activityLevel >= ActivityLevel.ACTIVE) 0.5 else 0.0
        return base + activeBonus
    }

    override fun formatted(profile: UserProfile) = String.format(Locale.US, "%.1f L", calculate(profile))

    override fun interpret(profile: UserProfile) =
        "About ${(calculate(profile) / 0.25).roundToInt()} glasses of 250 ml."
}

class ProteinMetric : HealthMetric {
    override val name = "Daily protein"
    override val emoji = "🥚"

    override fun calculate(profile: UserProfile): Double {
        val gramsPerKg = when (profile.goal) {
            FitnessGoal.BUILD_MUSCLE -> 1.8
            FitnessGoal.LOSE_WEIGHT -> 1.6
            else -> 1.2
        }
        return profile.weightKg * gramsPerKg
    }

    override fun formatted(profile: UserProfile) = "${calculate(profile).roundToInt()} g"

    override fun interpret(profile: UserProfile) = "Spread it across 3–4 meals for best absorption."
}

/** Healthy weight range for the user's height, from BMI 18.5 to 24.9. */
class HealthyWeightMetric : HealthMetric {
    override val name = "Healthy weight range"
    override val emoji = "🎯"

    private fun weightAt(bmi: Double, profile: UserProfile): Double {
        val heightM = profile.heightCm / 100.0
        return bmi * heightM * heightM
    }

    override fun calculate(profile: UserProfile) = weightAt(21.7, profile)

    override fun formatted(profile: UserProfile) =
        "${weightAt(18.5, profile).roundToInt()}–${weightAt(24.9, profile).roundToInt()} kg"

    override fun interpret(profile: UserProfile): String {
        val low = weightAt(18.5, profile)
        val high = weightAt(24.9, profile)
        return when {
            profile.weightKg < low -> "You're ${abs(low - profile.weightKg).roundToInt()} kg below the range."
            profile.weightKg > high -> "You're ${abs(profile.weightKg - high).roundToInt()} kg above the range."
            else -> "You're inside the healthy range."
        }
    }
}

object HealthMetrics {
    val all: List<HealthMetric> = listOf(
        BmiMetric(),
        HealthyWeightMetric(),
        BmrMetric(),
        DailyCaloriesMetric(),
        ProteinMetric(),
        WaterIntakeMetric()
    )
}
