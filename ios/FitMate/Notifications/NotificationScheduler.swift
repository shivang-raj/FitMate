import Foundation
import UserNotifications

/// DEPENDENCY INVERSION: the store only talks to this protocol, not to UNUserNotificationCenter.
/// (The Android app has the same interface backed by AlarmManager.)
protocol NotificationScheduler {
    func requestPermission()
    func schedule(_ reminder: Reminder)
    func cancel(_ reminder: Reminder)
    func showNow(_ reminder: Reminder)
}

final class LocalNotificationScheduler: NotificationScheduler {
    private let center = UNUserNotificationCenter.current()

    func requestPermission() {
        center.requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    func schedule(_ reminder: Reminder) {
        cancel(reminder)
        guard reminder.enabled else { return }
        var time = DateComponents()
        time.hour = reminder.hour
        time.minute = reminder.minute
        let trigger = UNCalendarNotificationTrigger(dateMatching: time, repeats: true)
        let request = UNNotificationRequest(identifier: reminder.id.uuidString, content: content(for: reminder), trigger: trigger)
        center.add(request) { _ in }
    }

    func cancel(_ reminder: Reminder) {
        center.removePendingNotificationRequests(withIdentifiers: [reminder.id.uuidString])
    }

    func showNow(_ reminder: Reminder) {
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 3, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content(for: reminder), trigger: trigger)
        center.add(request) { _ in }
    }

    private func content(for reminder: Reminder) -> UNMutableNotificationContent {
        let content = UNMutableNotificationContent()
        content.title = "\(reminder.type.emoji) \(reminder.type.label)"
        content.body = reminder.type.message
        content.sound = .default
        return content
    }
}

/// INHERITANCE from NSObject + protocol conformance: lets notifications appear as banners
/// even while the app is open.
final class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationDelegate()

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .list, .sound])
    }
}
