import SwiftUI

extension Color {
    static let brand = Color(red: 0.91, green: 0.27, blue: 0.17)
}

/// EXTENSION: adds a UI colour to the model enum without touching the model file.
extension BmiCategory {
    var color: Color {
        switch self {
        case .underweight: return .blue
        case .normal: return .green
        case .overweight: return .orange
        case .obese: return .red
        }
    }
}

/// Shows 170.0 as "170" but keeps 72.5 as "72.5".
func formatNumber(_ value: Double) -> String {
    value.truncatingRemainder(dividingBy: 1) == 0 ? String(Int(value)) : String(value)
}

/// GENERIC view: a rounded card around any content.
struct CardView<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        content
            .padding()
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(.secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

struct SectionTitle: View {
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text)
            .font(.title3.bold())
            .padding(.top, 8)
    }
}

struct EmojiBadge: View {
    let emoji: String
    var size: CGFloat = 44

    var body: some View {
        Text(emoji)
            .font(.system(size: size * 0.45))
            .frame(width: size, height: size)
            .background(Color.brand.opacity(0.15), in: Circle())
    }
}

struct StatTile: View {
    let emoji: String
    let value: String
    let label: String

    var body: some View {
        CardView {
            VStack(alignment: .leading, spacing: 2) {
                Text(emoji).font(.title2)
                Text(value).font(.title2.bold())
                Text(label).font(.caption).foregroundStyle(.secondary)
            }
        }
    }
}

/// POLYMORPHISM in the UI: one view draws *any* Describable —
/// an Exercise, WorkoutSession, WorkoutLog, Reminder or Suggestion.
struct DescribableRow<Trailing: View>: View {
    let item: any Describable
    @ViewBuilder var trailing: Trailing

    var body: some View {
        HStack(spacing: 12) {
            EmojiBadge(emoji: item.emoji)
            VStack(alignment: .leading, spacing: 2) {
                Text(item.title).font(.subheadline.weight(.semibold))
                Text(item.subtitle).font(.caption).foregroundStyle(.secondary)
            }
            Spacer(minLength: 8)
            trailing
        }
    }
}

extension DescribableRow where Trailing == EmptyView {
    init(item: any Describable) {
        self.init(item: item) { EmptyView() }
    }
}

struct ChoiceChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline.weight(.medium))
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(isSelected ? Color.brand : Color(.tertiarySystemFill), in: Capsule())
                .foregroundStyle(isSelected ? Color.white : Color.primary)
        }
        .buttonStyle(.plain)
    }
}

/// GENERIC view: a scrollable row of chips for any list of options.
struct ChoiceChips<Option: Hashable>: View {
    let options: [Option]
    @Binding var selection: Option
    let label: (Option) -> String

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.self) { option in
                    ChoiceChip(title: label(option), isSelected: option == selection) {
                        selection = option
                    }
                }
            }
        }
    }
}

struct SuggestionCard: View {
    let suggestion: Suggestion

    private var tint: Color {
        switch suggestion.priority {
        case .high: return .brand
        case .medium: return .teal
        case .low: return .gray
        }
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Text(suggestion.emoji).font(.title)
            VStack(alignment: .leading, spacing: 4) {
                Text(suggestion.priority.label.uppercased())
                    .font(.caption2.bold())
                    .foregroundStyle(tint)
                Text(suggestion.title).font(.subheadline.bold())
                Text(suggestion.message).font(.subheadline).foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .padding()
        .background(tint.opacity(0.12), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

/// Coloured BMI scale from 10 to 40 with a marker at the user's value.
struct BmiGauge: View {
    let bmi: Double

    private let segments: [(category: BmiCategory, span: Double, label: String)] = [
        (.underweight, 8.5, "Under"),
        (.normal, 6.5, "Healthy"),
        (.overweight, 5, "Over"),
        (.obese, 10, "Obese")
    ]

    var body: some View {
        GeometryReader { geometry in
            let width = geometry.size.width
            let markerX = width * min(max((bmi - 10) / 30, 0), 1)
            VStack(alignment: .leading, spacing: 4) {
                ZStack(alignment: .leading) {
                    HStack(spacing: 0) {
                        ForEach(segments.indices, id: \.self) { index in
                            Rectangle()
                                .fill(segments[index].category.color)
                                .frame(width: width * segments[index].span / 30, height: 12)
                        }
                    }
                    .clipShape(Capsule())
                    RoundedRectangle(cornerRadius: 2)
                        .fill(Color.primary)
                        .frame(width: 4, height: 28)
                        .offset(x: markerX - 2)
                }
                .frame(height: 28)
                HStack(spacing: 0) {
                    ForEach(segments.indices, id: \.self) { index in
                        Text(segments[index].label)
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                            .frame(width: width * segments[index].span / 30)
                    }
                }
            }
        }
        .frame(height: 50)
    }
}

/// Asks how long the workout took and previews calories using the exercise's own formula.
struct LogWorkoutSheet: View {
    let exercise: Exercise
    let weightKg: Double
    let onLog: (Int) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var minutes: Int

    init(exercise: Exercise, weightKg: Double, onLog: @escaping (Int) -> Void) {
        self.exercise = exercise
        self.weightKg = weightKg
        self.onLog = onLog
        _minutes = State(initialValue: exercise.estimatedMinutes())
    }

    private var calories: Int {
        Int(exercise.caloriesBurned(weightKg: weightKg, minutes: minutes).rounded())
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    DescribableRow(item: exercise)
                    Text(exercise.details).font(.footnote).foregroundStyle(.secondary)
                }
                Section("Duration") {
                    Stepper("\(minutes) min", value: $minutes, in: 1...300)
                }
                Section {
                    LabeledContent("Calories burned", value: "≈ \(calories) kcal")
                } footer: {
                    Text("Calculated with the \(exercise.category.label) formula for your weight.")
                }
            }
            .navigationTitle("Log workout")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Log") {
                        onLog(minutes)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}
