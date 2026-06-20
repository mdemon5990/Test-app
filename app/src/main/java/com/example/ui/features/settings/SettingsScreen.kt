package com.example.ui.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val currentApiKey by viewModel.apiKey.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()

    var apiKeyInput by remember { mutableStateOf(currentApiKey ?: "") }

    LaunchedEffect(currentApiKey) {
        currentApiKey?.let { apiKeyInput = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("API Configuration", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Custom API Key (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            
            Button(
                onClick = { viewModel.saveApiKey(apiKeyInput) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Key")
            }

            Divider()

            Text("Appearance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("light", "dark", "system").forEach { theme ->
                    FilterChip(
                        selected = currentTheme == theme,
                        onClick = { viewModel.saveThemeMode(theme) },
                        label = { Text(theme.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Developer: Emon",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
