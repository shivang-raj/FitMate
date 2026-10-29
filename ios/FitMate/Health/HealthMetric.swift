import Foundation

/// ABSTRACTION: every metric knows how to calculate and explain itself.
/// The Health screen just loops over `[any HealthMetric]` — it never needs to know
/// which concrete metric it is drawing (POLYMORPHISM).
protocol HealthMetric {
    var name: String { get }
    var emoji: String { get }
    func calculate(for profile: UserProfile) -> Double
    func interpret(for profile: UserProfile) -> String
    func formatted(for profile: UserProfile) -> String
}

/// Protocol extension = a default implementation every metric gets for free
/// (the same idea as a Kotlin interface method with a body).
extension HealthMetric {
    func formatted(for profile: UserProfile) -> String {
        String(format: "%.1f", calculate(for: profile))
    }
}

struct BmiMetric: HealthMetric {
    let name = "Body Mass Index"
    let emoji = "⚖️"

    func calculate(for profile: UserProfile) -> Double { profile.bmi }

    func interpret(for profile: UserProfile) -> String {
        "\(profile.bmiCategory.label). \(profile.bmiCategory.advice)"
    }
}

/// Mifflin–St Jeor equation: calories your body burns at complete rest.
struct BmrMetric: HealthMetric {
    let name = "Basal Metabolic Rate"
    let emoji = "🔋"

    func calculate(for profile: UserProfile) -> Double {
        let weightPart = 10 * profile.weightKg
        let heightPart = 6.25 * profile.heightCm
        let agePart = 5 * Double(profile.age)
        return weightPart + heightPart - agePart + profile.gender.bmrOffset
    }

    func formatted(for profile: UserProfile) -> String { "\(Int(calculate(for: profile).rounded())) kcal" }

    func interpret(for profile: UserProfile) -> String { "Energy your body uses at complete rest." }
}

/// COMPOSITION: reuses a BmrMetric instead of copying its formula.
struct DailyCaloriesMetric: HealthMetric {
    let name = "Daily calorie target"
    let emoji = "🍽️"
    var bmr = BmrMetric()

    func calculate(for profile: UserProfile) -> Double {
        bmr.calculate(for: profile) * profile.activityLevel.multiplier + profile.goal.calorieAdjustment
    }

    func formatted(for profile: UserProfile) -> String { "\(Int(calculate(for: profile).rounded())) kcal" }

    func interpret(for profile: UserProfile) -> String {
        switch profile.goal {
        case .loseWeight: return "Includes a 500 kcal deficit for about 0.5 kg of fat loss a week."
        case .buildMuscle: return "Includes a 300 kcal surplus to fuel muscle growth."
        case .stayFit, .improveFlexibility: return "Eat around this much to maintain your current weight."
        }
    }
}

struct WaterIntakeMetric: HealthMetric {
    let name = "Daily water"
    let emoji = "💧"

    func calculate(for profile: UserProfile) -> Double {
        let base = profile.weightKg * 0.033
        let activeBonus = profile.activityLevel >= .active ? 0.5 : 0
        return base + activeBonus
    }

    func formatted(for profile: UserProfile) -> String { String(format: "%.1f L", calculate(for: profile)) }

    func interpret(for profile: UserProfile) -> String {
        "About \(Int((calculate(for: profile) / 0.25).rounded())) glasses of 250 ml."
    }
}

struct ProteinMetric: HealthMetric {
    let name = "Daily protein"
    let emoji = "🥚"

    func calculate(for profile: UserProfile) -> Double {
        let gramsPerKg: Double
        switch profile.goal {
        case .buildMuscle: gramsPerKg = 1.8
        case .loseWeight: gramsPerKg = 1.6
        case .stayFit, .improveFlexibility: gramsPerKg = 1.2
        }
        return profile.weightKg * gramsPerKg
    }

    func formatted(for profile: UserProfile) -> String { "\(Int(calculate(for: profile).rounded())) g" }

    func interpret(for profile: UserProfile) -> String { "Spread it across 3–4 meals for best absorption." }
}

/// Healthy weight range for the user's height, from BMI 18.5 to 24.9.
struct HealthyWeightMetric: HealthMetric {
    let name = "Healthy weight range"
    let emoji = "🎯"

    private func weight(atBmi bmi: Double, for profile: UserProfile) -> Double {
        let heightM = profile.heightCm / 100
        return bmi * heightM * heightM
    }

    func calculate(for profile: UserProfile) -> Double { weight(atBmi: 21.7, for: profile) }

    func formatted(for profile: UserProfile) -> String {
        let low = Int(weight(atBmi: 18.5, for: profile).rounded())
        let high = Int(weight(atBmi: 24.9, for: profile).rounded())
        return "\(low)–\(high) kg"
    }

    func interpret(for profile: UserProfile) -> String {
        let low = weight(atBmi: 18.5, for: profile)
        let high = weight(atBmi: 24.9, for: profile)
        if profile.weightKg < low {
            return "You're \(Int((low - profile.weightKg).rounded())) kg below the range."
        } else if profile.weightKg > high {
            return "You're \(Int((profile.weightKg - high).rounded())) kg above the range."
        }
        return "You're inside the healthy range."
    }
}

enum HealthMetrics {
    static let all: [any HealthMetric] = [
        BmiMetric(),
        HealthyWeightMetric(),
        BmrMetric(),
        DailyCaloriesMetric(),
        ProteinMetric(),
        WaterIntakeMetric()
    ]
}
