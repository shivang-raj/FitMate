import Foundation

/// Runs every rule and returns what they have to say, most important first.
/// OPEN/CLOSED PRINCIPLE: adding a new kind of suggestion means writing a new rule,
/// not editing this class.
final class SuggestionEngine {
    private let rules: [any SuggestionRule]

    init(rules: [any SuggestionRule] = SuggestionEngine.defaultRules) {
        self.rules = rules
    }

    func suggestions(for context: SuggestionContext) -> [Suggestion] {
        rules.compactMap { $0.evaluate(context) }.sorted()
    }

    static var defaultRules: [any SuggestionRule] {
        [
            InactivityRule(),
            BmiRule(),
            GoalRule(),
            WeeklyTargetRule(),
            RecoveryRule(),
            HydrationRule(),
            // A rule can also be a closure.
            ClosureRule { context in
                guard context.profile.age >= 40 else { return nil }
                return Suggestion(
                    title: "Warm up a little longer",
                    message: "Spend 8–10 minutes warming up to protect your joints and get more from each session.",
                    priority: .low, emoji: "🦵"
                )
            }
        ]
    }
}
