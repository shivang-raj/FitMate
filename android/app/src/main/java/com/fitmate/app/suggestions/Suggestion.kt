package com.fitmate.app.suggestions

import com.fitmate.app.model.Describable
import com.fitmate.app.model.UserProfile
import com.fitmate.app.tracking.WorkoutStats

/** Declared most-important first, so the natural enum order doubles as sort order. */
enum class Priority(val label: String) {
    HIGH("Important"),
    MEDIUM("Suggested"),
    LOW("Tip")
}

/** Implements Comparable, so a List<Suggestion> can simply be `.sorted()` by priority. */
data class Suggestion(
    override val title: String,
    val message: String,
    val priority: Priority,
    override val emoji: String
) : Describable, Comparable<Suggestion> {

    override val subtitle: String get() = message

    override fun compareTo(other: Suggestion): Int = priority.compareTo(other.priority)
}

/** Everything a rule may look at when deciding whether to speak up. */
class SuggestionContext(
    val profile: UserProfile,
    val stats: WorkoutStats
)
