import Foundation

/// Anything the UI can show as a row with an emoji, a title and a subtitle.
/// Exercises, plan sessions, workout logs, reminders and suggestions all conform,
/// so one view (`DescribableRow`) can draw all five — that's polymorphism.
protocol Describable {
    var title: String { get }
    var subtitle: String { get }
    var emoji: String { get }
}
