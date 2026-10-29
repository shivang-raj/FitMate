import SwiftUI

/// First-launch screen: the same form as Profile, shown full screen until a profile exists.
struct OnboardingView: View {
    @EnvironmentObject var store: FitnessStore

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("💪").font(.system(size: 48))
                        Text("Welcome to FitMate").font(.title.bold())
                        Text("Tell us about yourself and we'll build your workout plan, health numbers and daily tips.")
                            .foregroundStyle(.secondary)
                    }
                    .listRowBackground(Color.clear)
                }
                ProfileForm(initial: nil, buttonTitle: "Create my plan") { profile in
                    store.saveProfile(profile)
                }
            }
        }
    }
}

struct ProfileView: View {
    @EnvironmentObject var store: FitnessStore
    let profile: UserProfile
    @State private var showSaved = false

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    HStack(spacing: 12) {
                        EmojiBadge(emoji: profile.goal.emoji, size: 56)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(profile.name).font(.title3.bold())
                            Text("\(profile.age) yrs · \(formatNumber(profile.heightCm)) cm · \(formatNumber(profile.weightKg)) kg")
                                .font(.subheadline)
                            Text("BMI \(String(format: "%.1f", profile.bmi)) · \(store.stats.totalWorkouts) workouts logged")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                ProfileForm(initial: profile, buttonTitle: "Save changes") { updated in
                    store.saveProfile(updated)
                    showSaved = true
                }
                // A new profile object (e.g. saved from the Health tab) resets the form.
                .id(ObjectIdentifier(profile))
            }
            .navigationTitle("Profile")
            .alert("Profile saved", isPresented: $showSaved) {
                Button("OK", role: .cancel) {}
            } message: {
                Text("Your plan and suggestions have been updated.")
            }
        }
    }
}

struct ProfileForm: View {
    let initial: UserProfile?
    let buttonTitle: String
    let onSave: (UserProfile) -> Void

    @State private var name: String
    @State private var age: String
    @State private var height: String
    @State private var weight: String
    @State private var gender: Gender
    @State private var activity: ActivityLevel
    @State private var goal: FitnessGoal
    @State private var experience: Difficulty
    @State private var errorMessage: String? = nil

    init(initial: UserProfile?, buttonTitle: String, onSave: @escaping (UserProfile) -> Void) {
        self.initial = initial
        self.buttonTitle = buttonTitle
        self.onSave = onSave
        _name = State(initialValue: initial?.name ?? "")
        _age = State(initialValue: initial.map { String($0.age) } ?? "")
        _height = State(initialValue: initial.map { formatNumber($0.heightCm) } ?? "")
        _weight = State(initialValue: initial.map { formatNumber($0.weightKg) } ?? "")
        _gender = State(initialValue: initial?.gender ?? .male)
        _activity = State(initialValue: initial?.activityLevel ?? .moderate)
        _goal = State(initialValue: initial?.goal ?? .stayFit)
        _experience = State(initialValue: initial?.experience ?? .beginner)
    }

    var body: some View {
        Section("About you") {
            TextField("Name", text: $name)
                .textContentType(.name)
            LabeledContent("Age") {
                TextField("years", text: $age)
                    .keyboardType(.numberPad)
                    .multilineTextAlignment(.trailing)
            }
            LabeledContent("Height (cm)") {
                TextField("cm", text: $height)
                    .keyboardType(.decimalPad)
                    .multilineTextAlignment(.trailing)
            }
            LabeledContent("Weight (kg)") {
                TextField("kg", text: $weight)
                    .keyboardType(.decimalPad)
                    .multilineTextAlignment(.trailing)
            }
            Picker("Gender", selection: $gender) {
                ForEach(Gender.allCases) { option in
                    Text(option.label).tag(option)
                }
            }
            .pickerStyle(.segmented)
        }

        Section {
            Picker("Activity level", selection: $activity) {
                ForEach(ActivityLevel.allCases) { option in
                    Text(option.label).tag(option)
                }
            }
        } footer: {
            Text(activity.details)
        }

        Section("Goal & experience") {
            ChoiceChips(options: FitnessGoal.allCases, selection: $goal) { "\($0.emoji) \($0.label)" }
            Picker("Experience", selection: $experience) {
                ForEach(Difficulty.allCases) { option in
                    Text(option.label).tag(option)
                }
            }
            .pickerStyle(.segmented)
        }

        Section {
            if let errorMessage {
                Text(errorMessage).foregroundStyle(.red)
            }
            Button(action: save) {
                Text(buttonTitle)
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
            }
        }
    }

    /// UserProfile validates itself; the form only turns its error into a message.
    private func save() {
        do {
            guard let ageValue = Int(age) else { throw ValidationError.notANumber("age") }
            guard let heightValue = Double(height) else { throw ValidationError.notANumber("height") }
            guard let weightValue = Double(weight) else { throw ValidationError.notANumber("weight") }
            let profile = try UserProfile(
                name: name, age: ageValue, heightCm: heightValue, weightKg: weightValue,
                gender: gender, activityLevel: activity, goal: goal, experience: experience
            )
            errorMessage = nil
            onSave(profile)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
