import Foundation

/// A finished workout. A `struct` is a value type: it's copied, never shared,
/// which suits a record that never changes once written.
struct WorkoutLog: Identifiable, Codable, Describable {
    var id = UUID()
    let exerciseName: String
    let category: ExerciseCategory
    let minutes: Int
    let calories: Double
    var date = Date()

    var title: String { exerciseName }

    var subtitle: String {
        let day = date.formatted(.dateTime.day().month(.abbreviated))
        return "\(minutes) min · \(Int(calories.rounded())) kcal · \(day)"
    }

    var emoji: String { category.emoji }

    /// FACTORY METHOD: builds a log from *any* Exercise subclass, using its own calorie formula.
    static func from(_ exercise: Exercise, minutes: Int, weightKg: Double) -> WorkoutLog {
        WorkoutLog(
            exerciseName: exercise.name,
            category: exercise.category,
            minutes: minutes,
            calories: exercise.caloriesBurned(weightKg: weightKg, minutes: minutes)
        )
    }
}
