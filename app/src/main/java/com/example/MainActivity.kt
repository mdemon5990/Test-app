package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.DrawerContent
import com.example.ui.features.chat.ChatScreen
import com.example.ui.features.chat.ChatViewModel
import com.example.ui.features.settings.SettingsScreen
import com.example.ui.features.settings.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AIChatApplication

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel(app.chatRepository, app.settingsRepository) as T
                }
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return SettingsViewModel(app.settingsRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            val themeMode by settingsViewModel.themeMode.collectAsState()
            
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val chatViewModel: ChatViewModel = viewModel(factory = factory)
                
                NavHost(navController = navController, startDestination = "chat") {
                    composable("chat") {
                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                        val scope = rememberCoroutineScope()
                        val conversations by chatViewModel.conversations.collectAsState()
                        val currentId by chatViewModel.currentConversationId.collectAsState()
                        
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                DrawerContent(
                                    conversations = conversations,
                                    currentId = currentId,
                                    onSelect = { 
                                        chatViewModel.selectConversation(it)
                                        scope.launch { drawerState.close() }
                                    },
                                    onNavigateSettings = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("settings")
                                    },
                                    onClearHistory = {
                                        chatViewModel.clearHistory()
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                        ) {
                            ChatScreen(
                                viewModel = chatViewModel,
                                onNavigateToSettings = { navController.navigate("settings") },
                                drawerState = drawerState,
                                onNewChat = { chatViewModel.selectConversation(null) }
                            )
                        }
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

