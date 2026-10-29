import SwiftUI

struct HealthView: View {
    @EnvironmentObject var store: FitnessStore
    let profile: UserProfile

    @State private var heightText = ""
    @State private var weightText = ""

    /// Builds a temporary profile. UserProfile's validating setters reject impossible values,
    /// so the calculator gets input checking for free.
    private var preview: Result<UserProfile, Error> {
        Result {
            guard let height = Double(heightText) else { throw ValidationError.notANumber("height") }
            guard let weight = Double(weightText) else { throw ValidationError.notANumber("weight") }
            return try profile.withBody(heightCm: height, weightKg: weight)
        }
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("BMI calculator") {
                    LabeledContent("Height (cm)") {
                        TextField("cm", text: $heightText)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                    }
                    LabeledContent("Weight (kg)") {
                        TextField("kg", text: $weightText)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                    }
                    switch preview {
                    case .success(let result):
                        BmiResultView(profile: result)
                        if result.heightCm != profile.heightCm || result.weightKg != profile.weightKg {
                            Button("Save to my profile") { store.saveProfile(result) }
                        }
                    case .failure(let error):
                        Text(error.localizedDescription).foregroundStyle(.red)
                    }
                }

                if case .success(let result) = preview {
                    // POLYMORPHISM: every row is drawn the same way, whichever HealthMetric it holds.
                    Section("Your daily numbers") {
                        ForEach(store.healthMetrics.indices, id: \.self) { index in
                            MetricRow(metric: store.healthMetrics[index], profile: result)
                        }
                    }
                }
            }
            .navigationTitle("Health")
            .onAppear {
                heightText = formatNumber(profile.heightCm)
                weightText = formatNumber(profile.weightKg)
            }
        }
    }
}

private struct BmiResultView: View {
    let profile: UserProfile

    var body: some View {
        let category = profile.bmiCategory
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text(String(format: "%.1f", profile.bmi))
                    .font(.system(size: 44, weight: .bold, design: .rounded))
                    .foregroundStyle(category.color)
                Text(category.label)
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(category.color)
            }
            BmiGauge(bmi: profile.bmi)
            Text(category.advice).font(.subheadline)
        }
        .padding(.vertical, 4)
    }
}

private struct MetricRow: View {
    let metric: any HealthMetric
    let profile: UserProfile

    var body: some View {
        HStack(spacing: 12) {
            EmojiBadge(emoji: metric.emoji)
            VStack(alignment: .leading, spacing: 2) {
                Text(metric.name).font(.caption).foregroundStyle(.secondary)
                Text(metric.formatted(for: profile)).font(.title3.bold())
                Text(metric.interpret(for: profile)).font(.caption)
            }
        }
    }
}
