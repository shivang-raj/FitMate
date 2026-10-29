import Foundation

/// STRATEGY pattern: each fitness goal has its own algorithm for building a week.
protocol PlanStrategy {
    var goal: FitnessGoal { get }
    func buildPlan(level: Difficulty) -> WorkoutPlan
}

/// TEMPLATE METHOD pattern: `buildPlan` fixes the steps (get schedule → scale to level → wrap in a plan)
/// and is `final`, so subclasses can only fill in `goal`, `planName` and `weeklySchedule()`.
/// Swift has no `protected`, so the helpers are internal and documented as "for subclasses".
class BasePlanStrategy: PlanStrategy {

    var goal: FitnessGoal { fatalError("Subclasses must override goal") }

    var planName: String { fatalError("Subclasses must override planName") }

    func weeklySchedule() -> [WorkoutSession] { fatalError("Subclasses must override weeklySchedule()") }

    final func buildPlan(level: Difficulty) -> WorkoutPlan {
        let sessions = weeklySchedule().map { session in
            WorkoutSession(day: session.day, focus: session.focus,
                           exercises: session.exercises.map { $0.scaled(for: level) })
        }
        return WorkoutPlan(name: planName, goal: goal, level: level, sessions: sessions)
    }

    // Helpers for subclasses.
    func day(_ day: Weekday, _ focus: String, _ exerciseIds: String...) -> WorkoutSession {
        WorkoutSession(day: day, focus: focus, exercises: exerciseIds.map { ExerciseLibrary.shared[$0] })
    }

    func rest(_ day: Weekday) -> WorkoutSession {
        WorkoutSession(day: day, focus: "Rest & recover", exercises: [])
    }
}

final class FatLossStrategy: BasePlanStrategy {
    override var goal: FitnessGoal { .loseWeight }
    override var planName: String { "Fat Burn Program" }

    override func weeklySchedule() -> [WorkoutSession] {
        [
            day(.monday, "HIIT + Full body", "burpees", "squats", "pushups", "crunches"),
            day(.tuesday, "Steady cardio", "running", "hamstring_stretch"),
            day(.wednesday, "Active recovery", "brisk_walk", "sun_salutation", "cat_cow"),
            day(.thursday, "HIIT + Legs", "mountain_climbers", "lunges", "glute_bridge"),
            day(.friday, "Cardio + Upper body", "cycling", "rows", "shoulder_press"),
            day(.saturday, "Tabata finisher", "tabata", "jump_rope", "childs_pose"),
            rest(.sunday)
        ]
    }
}

final class MuscleGainStrategy: BasePlanStrategy {
    override var goal: FitnessGoal { .buildMuscle }
    override var planName: String { "Push / Pull / Legs" }

    override func weeklySchedule() -> [WorkoutSession] {
        [
            day(.monday, "Push", "pushups", "shoulder_press", "dips", "crunches"),
            day(.tuesday, "Pull", "rows", "curls", "deadlifts"),
            day(.wednesday, "Legs", "squats", "lunges", "glute_bridge", "crunches"),
            day(.thursday, "Mobility", "cat_cow", "hip_opener", "hamstring_stretch"),
            day(.friday, "Upper body", "pushups", "rows", "shoulder_press", "curls"),
            day(.saturday, "Lower body + Core", "squats", "deadlifts", "crunches"),
            rest(.sunday)
        ]
    }
}

final class GeneralFitnessStrategy: BasePlanStrategy {
    override var goal: FitnessGoal { .stayFit }
    override var planName: String { "Balanced Fitness" }

    override func weeklySchedule() -> [WorkoutSession] {
        [
            day(.monday, "Full-body strength", "squats", "pushups", "rows", "crunches"),
            day(.tuesday, "Cardio", "running", "hamstring_stretch"),
            day(.wednesday, "Yoga flow", "sun_salutation", "cobra", "childs_pose"),
            day(.thursday, "HIIT", "mountain_climbers", "lunges", "glute_bridge"),
            day(.friday, "Cardio + Core", "cycling", "crunches"),
            day(.saturday, "Outdoor walk", "brisk_walk", "hip_opener"),
            rest(.sunday)
        ]
    }
}

final class FlexibilityStrategy: BasePlanStrategy {
    override var goal: FitnessGoal { .improveFlexibility }
    override var planName: String { "Mobility & Yoga" }

    override func weeklySchedule() -> [WorkoutSession] {
        // Generics in action: grab every stretch in the library without listing ids.
        let allStretches: [Exercise] = ExerciseLibrary.shared.ofType(FlexibilityExercise.self)
        return [
            day(.monday, "Morning yoga", "sun_salutation", "cat_cow", "cobra"),
            day(.tuesday, "Walk + Hips", "brisk_walk", "hip_opener", "hamstring_stretch"),
            WorkoutSession(day: .wednesday, focus: "Full stretch routine", exercises: allStretches),
            day(.thursday, "Core & posture", "glute_bridge", "crunches", "cobra"),
            day(.friday, "Yoga flow", "sun_salutation", "childs_pose", "hip_opener"),
            day(.saturday, "Easy cycling", "cycling", "hamstring_stretch"),
            rest(.sunday)
        ]
    }
}

/// FACTORY pattern: callers ask for a plan and never need to know which strategy class built it.
enum WorkoutPlanFactory {
    static func strategy(for goal: FitnessGoal) -> any PlanStrategy {
        switch goal {
        case .loseWeight: return FatLossStrategy()
        case .buildMuscle: return MuscleGainStrategy()
        case .stayFit: return GeneralFitnessStrategy()
        case .improveFlexibility: return FlexibilityStrategy()
        }
    }

    static func createPlan(for profile: UserProfile) -> WorkoutPlan {
        strategy(for: profile.goal).buildPlan(level: profile.experience)
    }
}
