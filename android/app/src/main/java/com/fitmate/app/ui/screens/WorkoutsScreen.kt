package com.fitmate.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitmate.app.model.Exercise
import com.fitmate.app.model.ExerciseCategory
import com.fitmate.app.model.ExerciseLibrary
import com.fitmate.app.model.UserProfile
import com.fitmate.app.model.WorkoutPlan
import com.fitmate.app.model.WorkoutSession
import com.fitmate.app.ui.components.ChoiceChips
import com.fitmate.app.ui.components.DescribableRow
import com.fitmate.app.ui.components.LogWorkoutDialog
import com.fitmate.app.ui.components.ScreenHeader
import com.fitmate.app.viewmodel.FitnessViewModel
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun WorkoutsScreen(viewModel: FitnessViewModel, profile: UserProfile) {
    val context = LocalContext.current
    var showLibrary by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf<ExerciseCategory?>(null) }
    var exerciseToLog by remember { mutableStateOf<Exercise?>(null) }
    val plan = viewModel.plan

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader("Workouts", "Your weekly plan and the full exercise library") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !showLibrary, onClick = { showLibrary = false }, label = { Text("My plan") })
                FilterChip(selected = showLibrary, onClick = { showLibrary = true }, label = { Text("Exercise library") })
            }
        }

        if (!showLibrary && plan != null) {
            item { PlanSummaryCard(plan, profile.weightKg) }
            items(plan.sessions, key = { it.day.name }) { session ->
                SessionCard(
                    session = session,
                    isToday = session.day == LocalDate.now().dayOfWeek,
                    onLog = { exerciseToLog = it }
                )
            }
        } else {
            item {
                ChoiceChips(
                    options = listOf<ExerciseCategory?>(null) + ExerciseCategory.entries,
                    selected = category,
                    label = { it?.let { c -> "${c.emoji} ${c.label}" } ?: "All" },
                    onSelect = { category = it }
                )
            }
            items(ExerciseLibrary.byCategory(category), key = { it.id }) { exercise ->
                ExerciseCard(exercise, profile.weightKg, onLog = { exerciseToLog = exercise })
            }
        }
    }

    exerciseToLog?.let { exercise ->
        LogWorkoutDialog(
            exercise = exercise,
            weightKg = profile.weightKg,
            onDismiss = { exerciseToLog = null },
            onConfirm = { minutes ->
                viewModel.logWorkout(exercise, minutes)
                exerciseToLog = null
                Toast.makeText(context, "Logged ${exercise.name} 💪", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun PlanSummaryCard(plan: WorkoutPlan, weightKg: Double) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${plan.goal.emoji} ${plan.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Built for: ${plan.goal.label} · ${plan.level.label}")
            Row(Modifier.fillMaxWidth()) {
                PlanFigure("${plan.activeDays}", "active days", Modifier.weight(1f))
                PlanFigure("${plan.weeklyMinutes}", "min / week", Modifier.weight(1f))
                PlanFigure("${plan.weeklyCalories(weightKg).roundToInt()}", "kcal / week", Modifier.weight(1f))
            }
            Text(
                "Change your goal or experience in Profile to get a new plan.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PlanFigure(value: String, label: String, modifier: Modifier) {
    Column(modifier) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SessionCard(session: WorkoutSession, isToday: Boolean, onLog: (Exercise) -> Unit) {
    var expanded by rememberSaveable(session.day) { mutableStateOf(isToday) }
    val colors = if (isToday) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = !session.isRestDay) { expanded = !expanded },
        colors = colors
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            DescribableRow(session) {
                if (isToday) {
                    Text(
                        "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (expanded && !session.isRestDay) {
                HorizontalDivider()
                session.exercises.forEach { exercise ->
                    DescribableRow(exercise) {
                        TextButton(onClick = { onLog(exercise) }) { Text("Log") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise, weightKg: Double, onLog: () -> Unit) {
    val minutes = exercise.estimatedMinutes()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            DescribableRow(exercise) {
                FilledTonalButton(onClick = onLog) { Text("Log") }
            }
            Text(exercise.description, style = MaterialTheme.typography.bodySmall)
            Text(
                "${exercise.category.label} · ${exercise.level.label} · " +
                    "≈${exercise.caloriesBurned(weightKg, minutes).roundToInt()} kcal in $minutes min",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
