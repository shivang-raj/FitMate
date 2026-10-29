package com.fitmate.app.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitmate.app.model.Reminder
import com.fitmate.app.model.ReminderType
import com.fitmate.app.reminders.NotificationHelper
import com.fitmate.app.ui.components.DescribableRow
import com.fitmate.app.ui.components.ScreenHeader
import com.fitmate.app.viewmodel.FitnessViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun RemindersScreen(viewModel: FitnessViewModel) {
    val context = LocalContext.current
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var canNotify by remember { mutableStateOf(NotificationHelper.canNotify(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        canNotify = granted
    }

    fun ensurePermission() {
        if (!canNotify && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenHeader("Reminders", "Daily nudges for workouts, water, meals and sleep") }

            if (!canNotify) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Notifications are off", fontWeight = FontWeight.Bold)
                            Text("Allow notifications so FitMate can remind you.")
                            Button(onClick = { ensurePermission() }) { Text("Allow notifications") }
                        }
                    }
                }
            }

            if (viewModel.reminders.isEmpty()) {
                item {
                    Text(
                        "No reminders yet. Tap \"Add reminder\" to create one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(viewModel.reminders, key = { it.id }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onToggle = { enabled ->
                        if (enabled) ensurePermission()
                        viewModel.setReminderEnabled(reminder, enabled)
                    },
                    onTest = {
                        ensurePermission()
                        viewModel.testReminder(reminder)
                    },
                    onDelete = { viewModel.deleteReminder(reminder) }
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Add reminder") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAddDialog) {
        AddReminderDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { type, hour, minute ->
                ensurePermission()
                viewModel.addReminder(type, hour, minute)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onToggle: (Boolean) -> Unit,
    onTest: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            DescribableRow(reminder) {
                Switch(checked = reminder.enabled, onCheckedChange = onToggle)
            }
            Text(
                reminder.type.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onTest) { Text("Test now") }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun AddReminderDialog(onDismiss: () -> Unit, onAdd: (ReminderType, Int, Int) -> Unit) {
    val context = LocalContext.current
    var type by rememberSaveable { mutableStateOf(ReminderType.WORKOUT) }
    var hour by rememberSaveable { mutableIntStateOf(type.defaultHour) }
    var minute by rememberSaveable { mutableIntStateOf(type.defaultMinute) }

    fun select(option: ReminderType) {
        type = option
        hour = option.defaultHour
        minute = option.defaultMinute
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New reminder") },
        text = {
            Column {
                ReminderType.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { select(option) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == type, onClick = { select(option) })
                        Text("${option.emoji}  ${option.label}")
                    }
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, pickedHour, pickedMinute ->
                                hour = pickedHour
                                minute = pickedMinute
                            },
                            hour,
                            minute,
                            DateFormat.is24HourFormat(context)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Time: " + LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("h:mm a")))
                }
            }
        },
        confirmButton = { Button(onClick = { onAdd(type, hour, minute) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
