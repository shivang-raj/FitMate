import Foundation

enum ValidationError: LocalizedError {
    case emptyName
    case invalidAge
    case invalidHeight
    case invalidWeight
    case notANumber(String)

    var errorDescription: String? {
        switch self {
        case .emptyName: return "Name can't be empty"
        case .invalidAge: return "Age must be between 10 and 100"
        case .invalidHeight: return "Height must be between 80 and 250 cm"
        case .invalidWeight: return "Weight must be between 20 and 300 kg"
        case .notANumber(let field): return "Enter your \(field) as a number"
        }
    }
}

/// ENCAPSULATION: the core fields are `private(set)` — anyone can read them, but they can only
/// change through throwing setter methods that validate the new value.
/// So a UserProfile can never hold an impossible height, weight or age.
final class UserProfile: Codable {
    static let ageRange = 10...100
    static let heightRange = 80.0...250.0
    static let weightRange = 20.0...300.0

    private(set) var name: String
    private(set) var age: Int
    private(set) var heightCm: Double
    private(set) var weightKg: Double
    var gender: Gender
    var activityLevel: ActivityLevel
    var goal: FitnessGoal
    var experience: Difficulty

    init(
        name: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        gender: Gender = .male,
        activityLevel: ActivityLevel = .moderate,
        goal: FitnessGoal = .stayFit,
        experience: Difficulty = .beginner
    ) throws {
        self.name = ""
        self.age = 0
        self.heightCm = 0
        self.weightKg = 0
        self.gender = gender
        self.activityLevel = activityLevel
        self.goal = goal
        self.experience = experience
        // Route the values through the setters so they get validated too.
        try setName(name)
        try setAge(age)
        try setHeight(heightCm)
        try setWeight(weightKg)
    }

    func setName(_ value: String) throws {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { throw ValidationError.emptyName }
        name = trimmed
    }

    func setAge(_ value: Int) throws {
        guard Self.ageRange.contains(value) else { throw ValidationError.invalidAge }
        age = value
    }

    func setHeight(_ value: Double) throws {
        guard Self.heightRange.contains(value) else { throw ValidationError.invalidHeight }
        heightCm = value
    }

    func setWeight(_ value: Double) throws {
        guard Self.weightRange.contains(value) else { throw ValidationError.invalidWeight }
        weightKg = value
    }

    /// Body-mass index = weight (kg) / height (m)². A read-only computed property.
    var bmi: Double {
        let heightM = heightCm / 100
        return weightKg / (heightM * heightM)
    }

    var bmiCategory: BmiCategory { BmiCategory(bmi: bmi) }

    /// Returns a new, validated profile with a different height/weight (used by the BMI calculator).
    func withBody(heightCm: Double, weightKg: Double) throws -> UserProfile {
        try UserProfile(
            name: name, age: age, heightCm: heightCm, weightKg: weightKg,
            gender: gender, activityLevel: activityLevel, goal: goal, experience: experience
        )
    }
}
