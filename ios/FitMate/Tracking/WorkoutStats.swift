import Foundation

struct DailyTotal: Identifiable {
    let date: Date
    let calories: Double
    var id: Date { date }
}

/// A read-only view over the workout history. Every number is derived from the logs,
/// never stored separately, so it can't get out of sync.
/// "This week" means the last 7 days, including today.
struct WorkoutStats {
    static let weeklyMinutesTarget = 150

    private let logs: [WorkoutLog]
    private let thisWeek: [WorkoutLog]
    private let today: Date
    private let calendar: Calendar

    init(logs: [WorkoutLog], now: Date = Date(), calendar: Calendar = .current) {
        let startOfToday = calendar.startOfDay(for: now)
        let weekStart = calendar.date(byAdding: .day, value: -6, to: startOfToday) ?? startOfToday
        let tomorrow = calendar.date(byAdding: .day, value: 1, to: startOfToday) ?? now
        self.logs = logs
        self.thisWeek = logs.filter { $0.date >= weekStart && $0.date < tomorrow }
        self.today = startOfToday
        self.calendar = calendar
    }

    var totalWorkouts: Int { logs.count }
    var workoutsThisWeek: Int { thisWeek.count }
    var minutesThisWeek: Int { thisWeek.reduce(0) { $0 + $1.minutes } }
    var caloriesThisWeek: Double { thisWeek.reduce(0.0) { $0 + $1.calories } }
    var activeDaysThisWeek: Int { Set(thisWeek.map { calendar.startOfDay(for: $0.date) }).count }

    /// Fraction of the WHO's 150 active minutes a week, capped at 1.
    var weeklyTargetProgress: Double {
        min(1, Double(minutesThisWeek) / Double(WorkoutStats.weeklyMinutesTarget))
    }

    /// Days since the last workout, or nil if nothing has been logged yet.
    var daysSinceLastWorkout: Int? {
        guard let last = logs.map(\.date).max() else { return nil }
        return calendar.dateComponents([.day], from: calendar.startOfDay(for: last), to: today).day
    }

    /// Consecutive days with at least one workout, ending today (or yesterday if today is still open).
    var currentStreak: Int {
        let activeDays = Set(logs.map { calendar.startOfDay(for: $0.date) })
        var cursor = activeDays.contains(today) ? today : dayBefore(today)
        var streak = 0
        while activeDays.contains(cursor) {
            streak += 1
            cursor = dayBefore(cursor)
        }
        return streak
    }

    /// Calories burned on each of the last 7 days, oldest first. Feeds the bar chart.
    func dailyCalories() -> [DailyTotal] {
        (0...6).reversed().map { daysAgo -> DailyTotal in
            let day = calendar.date(byAdding: .day, value: -daysAgo, to: today) ?? today
            let total = thisWeek
                .filter { calendar.isDate($0.date, inSameDayAs: day) }
                .reduce(0.0) { $0 + $1.calories }
            return DailyTotal(date: day, calories: total)
        }
    }

    private func dayBefore(_ date: Date) -> Date {
        calendar.date(byAdding: .day, value: -1, to: date) ?? date
    }
}
