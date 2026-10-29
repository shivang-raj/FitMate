import Foundation

/// One small, independent rule = one strategy. Returning nil means "nothing to say".
protocol SuggestionRule {
    func evaluate(_ context: SuggestionContext) -> Suggestion?
}

/// Lets a closure act as a rule — Swift's version of Kotlin's `fun interface` lambdas.
struct ClosureRule: SuggestionRule {
    let body: (SuggestionContext) -> Suggestion?

    func evaluate(_ context: SuggestionContext) -> Suggestion? { body(context) }
}

struct InactivityRule: SuggestionRule {
    var maxRestDays = 2

    func evaluate(_ context: SuggestionContext) -> Suggestion? {
        guard let days = context.stats.daysSinceLastWorkout else {
            return Suggestion(
                title: "Log your first workout",
                message: "Open Workouts, pick any exercise and tap Log. Every streak starts with day one.",
                priority: .high, emoji: "🚀"
            )
        }
        guard days >= maxRestDays else { return nil }
        return Suggestion(
            title: "Time to get moving",
            message: "It's been \(days) days since your last workout. Even a 20-minute brisk walk counts!",
            priority: .high, emoji: "⏰"
        )
    }
}

struct BmiRule: SuggestionRule {
    func evaluate(_ context: SuggestionContext) -> Suggestion? {
        let profile = context.profile
        let bmi = String(format: "%.1f", profile.bmi)
        switch profile.bmiCategory {
        case .underweight:
            return Suggestion(
                title: "Build healthy mass",
                message: "Your BMI is \(bmi). Add calorie-dense, protein-rich meals and strength-train 3 times a week.",
                priority: .high, emoji: "🍗"
            )
        case .normal:
            return Suggestion(
                title: "Healthy BMI",
                message: "Your BMI of \(bmi) is in the healthy range. Keep mixing strength and cardio to stay there.",
                priority: .low, emoji: "✅"
            )
        case .overweight:
            return Suggestion(
                title: "Trim down gradually",
                message: "Your BMI is \(bmi). Aim for 150+ cardio minutes a week and a modest daily calorie deficit.",
                priority: .high, emoji: "⚖️"
            )
        case .obese:
            return Suggestion(
                title: "Start with low-impact cardio",
                message: "Your BMI is \(bmi). Brisk walks and cycling protect your joints. Consider checking in with a doctor.",
                priority: .high, emoji: "🚶"
            )
        }
    }
}

struct GoalRule: SuggestionRule {
    func evaluate(_ context: SuggestionContext) -> Suggestion? {
        let profile = context.profile
        switch profile.goal {
        case .loseWeight:
            let calories = Int(DailyCaloriesMetric().calculate(for: profile).rounded())
            return Suggestion(
                title: "Your calorie target",
                message: "Eat about \(calories) kcal a day. A 500 kcal deficit loses roughly 0.5 kg a week.",
                priority: .medium, emoji: "🔥"
            )
        case .buildMuscle:
            let protein = Int(ProteinMetric().calculate(for: profile).rounded())
            return Suggestion(
                title: "Protein + progressive overload",
                message: "Eat around \(protein) g of protein daily and add a rep or a little weight each week.",
                priority: .medium, emoji: "💪"
            )
        case .stayFit:
            return Suggestion(
                title: "Keep it balanced",
                message: "Two strength days plus three cardio days a week is a great maintenance mix.",
                priority: .low, emoji: "⚡"
            )
        case .improveFlexibility:
            return Suggestion(
                title: "Stretch little and often",
                message: "10 minutes every day beats one long session. Hold each stretch 20–30 s without bouncing.",
                priority: .medium, emoji: "🧘"
            )
        }
    }
}

struct WeeklyTargetRule: SuggestionRule {
    func evaluate(_ context: SuggestionContext) -> Suggestion? {
        let minutes = context.stats.minutesThisWeek
        let target = WorkoutStats.weeklyMinutesTarget
        if minutes >= target {
            return Suggestion(
                title: "Weekly target smashed",
                message: "\(minutes) active minutes in the last 7 days — above the WHO's \(target)-minute guideline.",
                priority: .low, emoji: "🏆"
            )
        }
        return Suggestion(
            title: "\(target - minutes) min to your weekly goal",
            message: "You've logged \(minutes) of the recommended \(target) active minutes this week.",
            priority: .medium, emoji: "🎯"
        )
    }
}

struct RecoveryRule: SuggestionRule {
    func evaluate(_ context: SuggestionContext) -> Suggestion? {
        let activeDays = context.stats.activeDaysThisWeek
        guard activeDays >= 6 else { return nil }
        return Suggestion(
            title: "Schedule a rest day",
            message: "You trained on \(activeDays) of the last 7 days. Muscles grow while you recover, so try yoga or full rest.",
            priority: .medium, emoji: "😴"
        )
    }
}

struct HydrationRule: SuggestionRule {
    func evaluate(_ context: SuggestionContext) -> Suggestion? {
        let metric = WaterIntakeMetric()
        let amount = metric.formatted(for: context.profile)
        let glasses = metric.interpret(for: context.profile)
        return Suggestion(
            title: "Stay hydrated",
            message: "Drink about \(amount) of water today. \(glasses)",
            priority: .low, emoji: "💧"
        )
    }
}
