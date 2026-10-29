package com.fitmate.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.app.model.ActivityLevel
import com.fitmate.app.model.Difficulty
import com.fitmate.app.model.FitnessGoal
import com.fitmate.app.model.Gender
import com.fitmate.app.model.UserProfile
import com.fitmate.app.ui.components.ChoiceChips
import com.fitmate.app.ui.components.EmojiBadge
import com.fitmate.app.ui.components.ScreenHeader
import com.fitmate.app.ui.components.formatNumber
import com.fitmate.app.viewmodel.FitnessViewModel
import java.util.Locale

/** First-launch screen: the same form as Profile, shown full screen until a profile exists. */
@Composable
fun OnboardingScreen(onCreate: (UserProfile) -> Unit) {
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("💪", fontSize = 48.sp)
            Text("Welcome to FitMate", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Tell us about yourself and we'll build your workout plan, health numbers and daily tips.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ProfileForm(initial = null, buttonLabel = "Create my plan", onSave = onCreate)
        }
    }
}

@Composable
fun ProfileScreen(viewModel: FitnessViewModel, profile: UserProfile) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("Profile", "Changing your goal or experience rebuilds your plan")
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                EmojiBadge(profile.goal.emoji, size = 56.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(profile.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${profile.age} yrs · ${formatNumber(profile.heightCm)} cm · ${formatNumber(profile.weightKg)} kg")
                    Text(
                        "BMI ${String.format(Locale.US, "%.1f", profile.bmi)} · ${viewModel.stats.totalWorkouts} workouts logged",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        ProfileForm(
            initial = profile,
            buttonLabel = "Save changes",
            onSave = {
                viewModel.saveProfile(it)
                Toast.makeText(context, "Profile saved — plan updated", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ProfileForm(initial: UserProfile?, buttonLabel: String, onSave: (UserProfile) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var age by rememberSaveable { mutableStateOf(initial?.age?.toString().orEmpty()) }
    var height by rememberSaveable { mutableStateOf(initial?.heightCm?.let(::formatNumber).orEmpty()) }
    var weight by rememberSaveable { mutableStateOf(initial?.weightKg?.let(::formatNumber).orEmpty()) }
    var gender by rememberSaveable { mutableStateOf(initial?.gender ?: Gender.MALE) }
    var activity by rememberSaveable { mutableStateOf(initial?.activityLevel ?: ActivityLevel.MODERATE) }
    var goal by rememberSaveable { mutableStateOf(initial?.goal ?: FitnessGoal.STAY_FIT) }
    var experience by rememberSaveable { mutableStateOf(initial?.experience ?: Difficulty.BEGINNER) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
            NumberField(age, { age = it }, "Age", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(height, { height = it }, "Height (cm)", Modifier.weight(1f))
            NumberField(weight, { weight = it }, "Weight (kg)", Modifier.weight(1f))
        }

        FormLabel("Gender")
        ChoiceChips(Gender.entries, gender, { it.label }) { gender = it }

        FormLabel("Activity level")
        ChoiceChips(ActivityLevel.entries, activity, { it.label }) { activity = it }
        Text(activity.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        FormLabel("Goal")
        ChoiceChips(FitnessGoal.entries, goal, { "${it.emoji} ${it.label}" }) { goal = it }

        FormLabel("Experience")
        ChoiceChips(Difficulty.entries, experience, { it.label }) { experience = it }

        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                // UserProfile validates itself; we only turn its exception into a message.
                errorMessage = try {
                    onSave(
                        UserProfile(
                            name = name,
                            age = age.toIntOrNull() ?: throw IllegalArgumentException("Enter your age as a whole number"),
                            heightCm = height.toDoubleOrNull() ?: throw IllegalArgumentException("Enter your height in cm"),
                            weightKg = weight.toDoubleOrNull() ?: throw IllegalArgumentException("Enter your weight in kg"),
                            gender = gender,
                            activityLevel = activity,
                            goal = goal,
                            experience = experience
                        )
                    )
                    null
                } catch (e: IllegalArgumentException) {
                    e.message
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) { Text(buttonLabel) }
    }
}

@Composable
private fun NumberField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
private fun FormLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}
