package com.fitmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitmate.app.model.BmiCategory
import com.fitmate.app.model.Describable
import com.fitmate.app.model.Exercise
import com.fitmate.app.suggestions.Priority
import com.fitmate.app.suggestions.Suggestion
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ScreenHeader(title: String, subtitle: String? = null) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun EmojiBadge(emoji: String, size: Dp = 44.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.45f).sp)
    }
}

@Composable
fun StatCard(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(emoji, fontSize = 22.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * POLYMORPHISM in the UI: one composable draws *any* Describable —
 * an Exercise, WorkoutSession, WorkoutLog, Reminder or Suggestion.
 */
@Composable
fun DescribableRow(
    item: Describable,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmojiBadge(item.emoji)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

/** GENERIC composable: a row of chips for any list of options (enums, nullable filters, …). */
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) }
            )
        }
    }
}

@Composable
fun SuggestionCard(suggestion: Suggestion) {
    val colors = when (suggestion.priority) {
        Priority.HIGH -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Priority.MEDIUM -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Priority.LOW -> CardDefaults.cardColors()
    }
    Card(colors = colors, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp)) {
            Text(suggestion.emoji, fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    suggestion.priority.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
                Text(suggestion.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(suggestion.message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Simple bar chart of calories burned on each of the last 7 days. */
@Composable
fun WeeklyBarChart(data: List<Pair<LocalDate, Double>>, modifier: Modifier = Modifier) {
    val max = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0
    Row(
        modifier
            .fillMaxWidth()
            .height(150.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (date, kcal) ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (kcal > 0) kcal.roundToInt().toString() else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    Modifier
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height((100 * kcal / max).dp.coerceAtLeast(4.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (kcal > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/** EXTENSION PROPERTY: adds a UI colour to the model enum without touching the model file. */
val BmiCategory.color: Color
    get() = when (this) {
        BmiCategory.UNDERWEIGHT -> Color(0xFF42A5F5)
        BmiCategory.NORMAL -> Color(0xFF43A047)
        BmiCategory.OVERWEIGHT -> Color(0xFFFB8C00)
        BmiCategory.OBESE -> Color(0xFFE53935)
    }

/** Coloured BMI scale from 10 to 40 with a marker at the user's value. */
@Composable
fun BmiGauge(bmi: Double, modifier: Modifier = Modifier) {
    val segments = listOf(
        Triple(BmiCategory.UNDERWEIGHT, 8.5f, "Under"),
        Triple(BmiCategory.NORMAL, 6.5f, "Healthy"),
        Triple(BmiCategory.OVERWEIGHT, 5f, "Over"),
        Triple(BmiCategory.OBESE, 10f, "Obese")
    )
    val fraction = ((bmi - 10.0) / 30.0).coerceIn(0.0, 1.0).toFloat()
    Column(modifier.fillMaxWidth()) {
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                segments.forEach { (category, weight, _) ->
                    Box(
                        Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(category.color)
                    )
                }
            }
            Box(
                Modifier
                    .offset(x = maxWidth * fraction - 2.dp)
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
        Row(Modifier.fillMaxWidth()) {
            segments.forEach { (_, weight, label) ->
                Text(
                    label,
                    modifier = Modifier.weight(weight),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/** Asks how long the workout took and previews calories using the exercise's own formula. */
@Composable
fun LogWorkoutDialog(
    exercise: Exercise,
    weightKg: Double,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int) -> Unit
) {
    var minutesText by remember { mutableStateOf(exercise.estimatedMinutes().toString()) }
    val minutes = minutesText.toIntOrNull()?.takeIf { it in 1..300 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${exercise.emoji} ${exercise.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(exercise.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = minutesText,
                    onValueChange = { minutesText = it.filter(Char::isDigit).take(3) },
                    label = { Text("Minutes") },
                    singleLine = true,
                    isError = minutes == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text(
                    if (minutes != null) "≈ ${exercise.caloriesBurned(weightKg, minutes).roundToInt()} kcal burned"
                    else "Enter between 1 and 300 minutes",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (minutes != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(onClick = { minutes?.let(onConfirm) }, enabled = minutes != null) { Text("Log workout") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/** Shows 170.0 as "170" but keeps 72.5 as "72.5". */
fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
