import Foundation

// Swift enums are full types: they can have raw values, computed properties and methods.

enum Gender: String, Codable, CaseIterable, Identifiable {
    case male, female

    var id: String { rawValue }
    var label: String { rawValue.capitalized }

    var bmrOffset: Double {
        switch self {
        case .male: return 5
        case .female: return -161
        }
    }
}

enum ActivityLevel: String, Codable, CaseIterable, Identifiable, Comparable {
    case sedentary, light, moderate, active, athlete

    var id: String { rawValue }
    var label: String { rawValue.capitalized }

    var details: String {
        switch self {
        case .sedentary: return "Little or no exercise"
        case .light: return "Exercise 1–3 days a week"
        case .moderate: return "Exercise 3–5 days a week"
        case .active: return "Exercise 6–7 days a week"
        case .athlete: return "Hard training twice a day"
        }
    }

    var multiplier: Double {
        switch self {
        case .sedentary: return 1.2
        case .light: return 1.375
        case .moderate: return 1.55
        case .active: return 1.725
        case .athlete: return 1.9
        }
    }

    static func < (lhs: ActivityLevel, rhs: ActivityLevel) -> Bool {
        lhs.multiplier < rhs.multiplier
    }
}

enum FitnessGoal: String, Codable, CaseIterable, Identifiable {
    case loseWeight, buildMuscle, stayFit, improveFlexibility

    var id: String { rawValue }

    var label: String {
        switch self {
        case .loseWeight: return "Lose weight"
        case .buildMuscle: return "Build muscle"
        case .stayFit: return "Stay fit"
        case .improveFlexibility: return "Flexibility"
        }
    }

    var emoji: String {
        switch self {
        case .loseWeight: return "🔥"
        case .buildMuscle: return "💪"
        case .stayFit: return "⚡"
        case .improveFlexibility: return "🧘"
        }
    }

    var calorieAdjustment: Double {
        switch self {
        case .loseWeight: return -500
        case .buildMuscle: return 300
        case .stayFit, .improveFlexibility: return 0
        }
    }
}

enum Difficulty: String, Codable, CaseIterable, Identifiable {
    case beginner, intermediate, advanced

    var id: String { rawValue }
    var label: String { rawValue.capitalized }

    var extraSets: Int {
        switch self {
        case .beginner: return 0
        case .intermediate: return 1
        case .advanced: return 2
        }
    }

    var durationFactor: Double {
        switch self {
        case .beginner: return 0.8
        case .intermediate: return 1.0
        case .advanced: return 1.3
        }
    }
}

enum MuscleGroup: String, CaseIterable {
    case chest, back, legs, arms, shoulders, core, fullBody

    var label: String { self == .fullBody ? "Full body" : rawValue.capitalized }
}

enum ExerciseCategory: String, Codable, CaseIterable, Identifiable {
    case strength, cardio, hiit, flexibility

    var id: String { rawValue }

    var label: String { self == .hiit ? "HIIT" : rawValue.capitalized }

    var emoji: String {
        switch self {
        case .strength: return "🏋️"
        case .cardio: return "🏃"
        case .hiit: return "🔥"
        case .flexibility: return "🧘"
        }
    }
}

enum BmiCategory: CaseIterable {
    case underweight, normal, overweight, obese

    /// Custom initializer: picks the right case for a BMI value.
    init(bmi: Double) {
        switch bmi {
        case ..<18.5: self = .underweight
        case ..<25: self = .normal
        case ..<30: self = .overweight
        default: self = .obese
        }
    }

    var label: String {
        switch self {
        case .underweight: return "Underweight"
        case .normal: return "Healthy"
        case .overweight: return "Overweight"
        case .obese: return "Obese"
        }
    }

    var advice: String {
        switch self {
        case .underweight: return "Focus on nutrient-dense meals and strength training."
        case .normal: return "Great work — keep up a balanced routine."
        case .overweight: return "Regular cardio and a small calorie deficit will help."
        case .obese: return "Start with low-impact cardio and consider a doctor's advice."
        }
    }
}
