import Foundation

enum ReminderType: String, Codable, CaseIterable, Identifiable {
    case workout, water, meal, stretch, sleep

    var id: String { rawValue }

    var label: String {
        switch self {
        case .workout: return "Workout"
        case .water: return "Drink water"
        case .meal: return "Healthy meal"
        case .stretch: return "Stretch break"
        case .sleep: return "Wind down"
        }
    }

    var emoji: String {
        switch self {
        case .workout: return "💪"
        case .water: return "💧"
        case .meal: return "🥗"
        case .stretch: return "🧘"
        case .sleep: return "🌙"
        }
    }

    var message: String {
        switch self {
        case .workout: return "Time to move! Your workout is waiting."
        case .water: return "Have a glass of water to stay hydrated."
        case .meal: return "Fuel up with a balanced, protein-rich meal."
        case .stretch: return "Stand up and stretch for two minutes."
        case .sleep: return "Put the screens away — sleep is when muscles recover."
        }
    }

    var defaultTime: (hour: Int, minute: Int) {
        switch self {
        case .workout: return (7, 0)
        case .water: return (11, 0)
        case .meal: return (13, 0)
        case .stretch: return (16, 0)
        case .sleep: return (22, 30)
        }
    }
}

/// A daily reminder.
struct Reminder: Identifiable, Codable, Describable {
    var id = UUID()
    var type: ReminderType
    var hour: Int
    var minute: Int
    var enabled = true

    var timeLabel: String {
        let date = Calendar.current.date(bySettingHour: hour, minute: minute, second: 0, of: Date()) ?? Date()
        return date.formatted(date: .omitted, time: .shortened)
    }

    var title: String { type.label }
    var subtitle: String { "Every day at \(timeLabel)" }
    var emoji: String { type.emoji }
}
