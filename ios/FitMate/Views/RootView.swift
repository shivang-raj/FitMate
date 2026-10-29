import SwiftUI

/// Each tab is one case of an enum — the compiler knows every possible tab.
enum Tab: String, CaseIterable, Identifiable {
    case home, workouts, health, reminders, profile

    var id: String { rawValue }
    var title: String { rawValue.capitalized }

    var icon: String {
        switch self {
        case .home: return "house.fill"
        case .workouts: return "figure.run"
        case .health: return "heart.fill"
        case .reminders: return "bell.fill"
        case .profile: return "person.fill"
        }
    }
}

struct RootView: View {
    @EnvironmentObject var store: FitnessStore

    var body: some View {
        if let profile = store.profile {
            MainTabView(profile: profile)
        } else {
            OnboardingView()
        }
    }
}

struct MainTabView: View {
    let profile: UserProfile
    @State private var selection: Tab = .home

    var body: some View {
        TabView(selection: $selection) {
            HomeView(profile: profile) { selection = .workouts }
                .tabItem { Label(Tab.home.title, systemImage: Tab.home.icon) }
                .tag(Tab.home)
            WorkoutsView(profile: profile)
                .tabItem { Label(Tab.workouts.title, systemImage: Tab.workouts.icon) }
                .tag(Tab.workouts)
            HealthView(profile: profile)
                .tabItem { Label(Tab.health.title, systemImage: Tab.health.icon) }
                .tag(Tab.health)
            RemindersView()
                .tabItem { Label(Tab.reminders.title, systemImage: Tab.reminders.icon) }
                .tag(Tab.reminders)
            ProfileView(profile: profile)
                .tabItem { Label(Tab.profile.title, systemImage: Tab.profile.icon) }
                .tag(Tab.profile)
        }
    }
}
