import SwiftUI

struct RemindersView: View {
    @EnvironmentObject var store: FitnessStore
    @State private var showingAdd = false

    var body: some View {
        NavigationStack {
            List {
                Section {
                    if store.reminders.isEmpty {
                        Text("No reminders yet. Tap + to create one.")
                            .foregroundStyle(.secondary)
                    }
                    ForEach(store.reminders) { reminder in
                        ReminderRow(reminder: reminder)
                            .swipeActions {
                                Button("Delete", role: .destructive) { store.deleteReminder(reminder) }
                            }
                    }
                } footer: {
                    Text("Swipe left on a reminder to delete it.")
                }
            }
            .navigationTitle("Reminders")
            .toolbar {
                Button {
                    showingAdd = true
                } label: {
                    Label("Add reminder", systemImage: "plus")
                }
            }
            .sheet(isPresented: $showingAdd) {
                AddReminderSheet()
                    .environmentObject(store)
            }
        }
    }
}

private struct ReminderRow: View {
    @EnvironmentObject var store: FitnessStore
    let reminder: Reminder

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            DescribableRow(item: reminder) {
                Toggle("Enabled", isOn: Binding(
                    get: { reminder.enabled },
                    set: { store.setReminder(reminder, enabled: $0) }
                ))
                .labelsHidden()
            }
            Text(reminder.type.message)
                .font(.caption)
                .foregroundStyle(.secondary)
            Button("Send a test notification") { store.testReminder(reminder) }
                .font(.caption.weight(.semibold))
                .buttonStyle(.borderless)
        }
        .padding(.vertical, 4)
    }
}

private struct AddReminderSheet: View {
    @EnvironmentObject var store: FitnessStore
    @Environment(\.dismiss) private var dismiss

    @State private var type: ReminderType = .workout
    @State private var time = AddReminderSheet.defaultTime(for: .workout)

    private static func defaultTime(for type: ReminderType) -> Date {
        let preset = type.defaultTime
        return Calendar.current.date(bySettingHour: preset.hour, minute: preset.minute, second: 0, of: Date()) ?? Date()
    }

    var body: some View {
        NavigationStack {
            Form {
                Picker("Type", selection: $type) {
                    ForEach(ReminderType.allCases) { option in
                        Text("\(option.emoji)  \(option.label)").tag(option)
                    }
                }
                .pickerStyle(.inline)
                .onChange(of: type) { newType in
                    time = Self.defaultTime(for: newType)
                }

                DatePicker("Time", selection: $time, displayedComponents: .hourAndMinute)
            }
            .navigationTitle("New reminder")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        let parts = Calendar.current.dateComponents([.hour, .minute], from: time)
                        store.addReminder(type: type, hour: parts.hour ?? 7, minute: parts.minute ?? 0)
                        dismiss()
                    }
                }
            }
        }
    }
}
