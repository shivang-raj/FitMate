import SwiftUI
import UserNotifications

@main
struct FitMateApp: App {
    @StateObject private var store = FitnessStore()

    init() {
        UNUserNotificationCenter.current().delegate = NotificationDelegate.shared
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
                .tint(.brand)
        }
    }
}
