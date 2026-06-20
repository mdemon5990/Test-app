package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DrawerContent(
    conversations: List<Conversation>,
    currentId: Long?,
    onSelect: (Long?) -> Unit,
    onNavigateSettings: () -> Unit,
    onClearHistory: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI Chat",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Divider()
        
        ListItem(
            headlineContent = { Text("New chat") },
            leadingContent = { Icon(Icons.Default.Add, null) },
            modifier = Modifier.clickable { onSelect(null) }
        )
        
        Divider()
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(conversations, key = { it.id }) { conv ->
                val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                NavigationDrawerItem(
                    label = { Text(conv.title, maxLines = 1) },
                    badge = { Text(formatter.format(Date(conv.updatedAt)), style = MaterialTheme.typography.labelSmall) },
                    selected = conv.id == currentId,
                    onClick = { onSelect(conv.id) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
        
        Divider()
        ListItem(
            headlineContent = { Text("Clear History") },
            leadingContent = { Icon(Icons.Default.Delete, null) },
            modifier = Modifier.clickable { onClearHistory() }
        )
        ListItem(
            headlineContent = { Text("Settings") },
            leadingContent = { Icon(Icons.Default.Settings, null) },
            modifier = Modifier.clickable { onNavigateSettings() }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
