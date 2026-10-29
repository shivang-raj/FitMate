import Foundation

/// Raw values are the sort order: most important first.
enum Priority: Int, Comparable {
    case high = 0, medium, low

    var label: String {
        switch self {
        case .high: return "Important"
        case .medium: return "Suggested"
        case .low: return "Tip"
        }
    }

    static func < (lhs: Priority, rhs: Priority) -> Bool { lhs.rawValue < rhs.rawValue }
}

/// Conforms to Comparable, so `[Suggestion]` can simply be `.sorted()` by priority.
struct Suggestion: Identifiable, Comparable, Describable {
    let id = UUID()
    let title: String
    let message: String
    let priority: Priority
    let emoji: String

    var subtitle: String { message }

    static func < (lhs: Suggestion, rhs: Suggestion) -> Bool { lhs.priority < rhs.priority }
}

/// Everything a rule may look at when deciding whether to speak up.
struct SuggestionContext {
    let profile: UserProfile
    let stats: WorkoutStats
}
