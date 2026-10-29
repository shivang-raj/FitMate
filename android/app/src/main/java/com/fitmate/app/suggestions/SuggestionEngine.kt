package com.fitmate.app.suggestions

/**
 * Runs every rule and returns what they have to say, most important first.
 * OPEN/CLOSED PRINCIPLE: adding a new kind of suggestion means writing a new rule,
 * not editing this class.
 */
class SuggestionEngine(private val rules: List<SuggestionRule> = defaultRules()) {

    fun suggestionsFor(context: SuggestionContext): List<Suggestion> =
        rules.mapNotNull { it.evaluate(context) }.sorted()

    companion object {
        fun defaultRules(): List<SuggestionRule> = listOf(
            InactivityRule(),
            BmiRule(),
            GoalRule(),
            WeeklyTargetRule(),
            RecoveryRule(),
            HydrationRule(),
            // Because SuggestionRule is a `fun interface`, a rule can also be a lambda.
            SuggestionRule { context ->
                if (context.profile.age >= 40) {
                    Suggestion(
                        "Warm up a little longer",
                        "Spend 8–10 minutes warming up to protect your joints and get more from each session.",
                        Priority.LOW, "🦵"
                    )
                } else null
            }
        )
    }
}
