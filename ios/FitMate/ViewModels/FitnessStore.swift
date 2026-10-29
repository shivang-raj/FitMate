import Foundation
import Combine

/// The single source of truth for every screen.
/// ENCAPSULATION: views can *read* the `private(set)` state, but every change goes through
/// a method below, which also keeps storage and notifications in sync.
final class FitnessStore: ObservableObject {
    @Published private(set) var profile: UserProfile? = nil
    @Published private(set) var logs: [WorkoutLog] = []
    @Published private(set) var reminders: [Reminder] = []

    let healthMetrics: [any HealthMetric] = HealthMetrics.all

    private let profileStore: ProfileStore
    private let workoutRepository: any Repository<WorkoutLog>
    private let reminderRepository: any Repository<Reminder>
    private let scheduler: any NotificationScheduler
    private let suggestionEngine: SuggestionEngine

    /// DEPENDENCY INJECTION: every collaborator can be swapped (e.g. for tests); defaults are the real ones.
    init(
        profileStore: ProfileStore = ProfileStore(),
        workoutRepository: any Repository<WorkoutLog> = UserDefaultsRepository<WorkoutLog>(key: "workout_logs"),
        reminderRepository: any Repository<Reminder> = UserDefaultsRepository<Reminder>(key: "reminders"),
        scheduler: any NotificationScheduler = LocalNotificationScheduler(),
        suggestionEngine: SuggestionEngine = SuggestionEngine()
    ) {
        self.profileStore = profileStore
        self.workoutRepository = workoutRepository
        self.reminderRepository = reminderRepository
        self.scheduler = scheduler
        self.suggestionEngine = suggestionEngine
        profile = profileStore.load()
        logs = sortedLogs()
        reminders = reminderRepository.getAll()
    }

    // MARK: Derived state (recomputed from profile/logs whenever they change)

    var stats: WorkoutStats { WorkoutStats(logs: logs) }

    var plan: WorkoutPlan? {
        guard let profile else { return nil }
        return WorkoutPlanFactory.createPlan(for: profile)
    }

    var suggestions: [Suggestion] {
        guard let profile else { return [] }
        return suggestionEngine.suggestions(for: SuggestionContext(profile: profile, stats: stats))
    }

    // MARK: Actions

    func saveProfile(_ newProfile: UserProfile) {
        profileStore.save(newProfile)
        profile = newProfile
    }

    func logWorkout(_ exercise: Exercise, minutes: Int) {
        guard let profile else { return }
        workoutRepository.save(WorkoutLog.from(exercise, minutes: minutes, weightKg: profile.weightKg))
        logs = sortedLogs()
    }

    func deleteLog(_ log: WorkoutLog) {
        workoutRepository.delete(id: log.id)
        logs = sortedLogs()
    }

    func addReminder(type: ReminderType, hour: Int, minute: Int) {
        let reminder = Reminder(type: type, hour: hour, minute: minute)
        scheduler.requestPermission()
        reminderRepository.save(reminder)
        scheduler.schedule(reminder)
        reminders = reminderRepository.getAll()
    }

    func setReminder(_ reminder: Reminder, enabled: Bool) {
        var updated = reminder
        updated.enabled = enabled
        if enabled { scheduler.requestPermission() }
        reminderRepository.save(updated)
        scheduler.schedule(updated) // schedule() cancels when disabled
        reminders = reminderRepository.getAll()
    }

    func deleteReminder(_ reminder: Reminder) {
        scheduler.cancel(reminder)
        reminderRepository.delete(id: reminder.id)
        reminders = reminderRepository.getAll()
    }

    func testReminder(_ reminder: Reminder) {
        scheduler.requestPermission()
        scheduler.showNow(reminder)
    }

    private func sortedLogs() -> [WorkoutLog] {
        workoutRepository.getAll().sorted { $0.date > $1.date }
    }
}
