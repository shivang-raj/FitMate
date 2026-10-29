import Foundation

/// GENERIC repository contract. `Item` is a primary associated type,
/// so callers can write `any Repository<WorkoutLog>`.
protocol Repository<Item> {
    associatedtype Item: Identifiable & Codable where Item.ID == UUID

    func getAll() -> [Item]
    /// Inserts the item, or replaces the stored item with the same id.
    func save(_ item: Item)
    func delete(id: UUID)
}

/// One generic class stores workout logs *and* reminders as JSON in UserDefaults.
/// Codable does the per-type conversion, so no hand-written mappers are needed.
final class UserDefaultsRepository<T: Identifiable & Codable>: Repository where T.ID == UUID {
    private let key: String
    private let defaults: UserDefaults

    init(key: String, defaults: UserDefaults = .standard) {
        self.key = key
        self.defaults = defaults
    }

    func getAll() -> [T] {
        guard let data = defaults.data(forKey: key) else { return [] }
        return (try? JSONDecoder().decode([T].self, from: data)) ?? []
    }

    func save(_ item: T) {
        var items = getAll()
        if let index = items.firstIndex(where: { $0.id == item.id }) {
            items[index] = item
        } else {
            items.append(item)
        }
        write(items)
    }

    func delete(id: UUID) {
        write(getAll().filter { $0.id != id })
    }

    private func write(_ items: [T]) {
        guard let data = try? JSONEncoder().encode(items) else { return }
        defaults.set(data, forKey: key)
    }
}

/// The profile is a single object rather than a list, so it gets its own tiny store.
final class ProfileStore {
    private let key = "profile"
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func load() -> UserProfile? {
        guard let data = defaults.data(forKey: key) else { return nil }
        return try? JSONDecoder().decode(UserProfile.self, from: data)
    }

    func save(_ profile: UserProfile) {
        guard let data = try? JSONEncoder().encode(profile) else { return }
        defaults.set(data, forKey: key)
    }
}
