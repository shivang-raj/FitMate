package com.fitmate.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitmate.app.health.HealthMetric
import com.fitmate.app.model.UserProfile
import com.fitmate.app.ui.components.BmiGauge
import com.fitmate.app.ui.components.EmojiBadge
import com.fitmate.app.ui.components.ScreenHeader
import com.fitmate.app.ui.components.SectionTitle
import com.fitmate.app.ui.components.color
import com.fitmate.app.ui.components.formatNumber
import com.fitmate.app.viewmodel.FitnessViewModel
import java.util.Locale

@Composable
fun HealthScreen(viewModel: FitnessViewModel, profile: UserProfile) {
    val context = LocalContext.current
    var heightText by rememberSaveable(profile.heightCm) { mutableStateOf(formatNumber(profile.heightCm)) }
    var weightText by rememberSaveable(profile.weightKg) { mutableStateOf(formatNumber(profile.weightKg)) }

    // Build a temporary profile. UserProfile's validating setters reject impossible values,
    // so the calculator gets input checking for free.
    val result = remember(heightText, weightText, profile) {
        runCatching {
            val height = heightText.toDoubleOrNull() ?: throw IllegalArgumentException("Enter your height in cm")
            val weight = weightText.toDoubleOrNull() ?: throw IllegalArgumentException("Enter your weight in kg")
            profile.withBody(height, weight)
        }
    }
    val preview = result.getOrNull()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader("Health", "BMI calculator and your daily numbers") }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("BMI calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("Height (cm)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (preview != null) {
                        val category = preview.bmiCategory
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                String.format(Locale.US, "%.1f", preview.bmi),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = category.color
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                category.label,
                                style = MaterialTheme.typography.titleLarge,
                                color = category.color,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        BmiGauge(preview.bmi)
                        Text(category.advice, style = MaterialTheme.typography.bodyMedium)
                        val changed = preview.heightCm != profile.heightCm || preview.weightKg != profile.weightKg
                        if (changed) {
                            Button(onClick = {
                                viewModel.saveProfile(preview)
                                Toast.makeText(context, "Saved to your profile", Toast.LENGTH_SHORT).show()
                            }) { Text("Save to my profile") }
                        }
                    } else {
                        Text(
                            result.exceptionOrNull()?.message ?: "Invalid input",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        if (preview != null) {
            item { SectionTitle("Your daily numbers") }
            // POLYMORPHISM: every card is drawn the same way, whichever HealthMetric it holds.
            items(viewModel.healthMetrics) { metric -> MetricCard(metric, preview) }
        }
    }
}

@Composable
private fun MetricCard(metric: HealthMetric, profile: UserProfile) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge(metric.emoji)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(metric.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(metric.formatted(profile), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(metric.interpret(profile), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
