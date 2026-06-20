package com.example.ui.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.ClaudeApiClient
import com.example.data.remote.ClaudeModel
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import com.example.repository.ChatRepository
import com.example.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.BuildConfig

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val apiClient = ClaudeApiClient()

    val conversations: StateFlow<List<Conversation>> = chatRepository.conversations
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _selectedModel = MutableStateFlow(ClaudeModel.OPUS_4_8)
    val selectedModel: StateFlow<ClaudeModel> = _selectedModel
    
    fun selectModel(model: ClaudeModel) {
         _selectedModel.value = model
    }

    fun selectConversation(id: Long?) {
        _currentConversationId.value = id
        if (id != null) {
            viewModelScope.launch {
                chatRepository.getMessages(id).collect { msgs ->
                    _messages.value = msgs
                }
            }
        } else {
            _messages.value = emptyList()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.clearHistory()
            selectConversation(null)
        }
    }

    fun sendMessage(content: String, imageBase64: String? = null) {
        if ((content.isBlank() && imageBase64 == null) || _isGenerating.value) return

        viewModelScope.launch(Dispatchers.IO) {
            val userApiKey = settingsRepository.apiKeyFlow.first()
            val apiKey = if (!userApiKey.isNullOrBlank()) userApiKey else BuildConfig.CLAUDE_API_KEY
            
            var convId = _currentConversationId.value
            if (convId == null) {
                // Auto-generate title based on first message
                val title = if (content.length > 20) content.take(20) + "..." else content.ifBlank { "Image Upload" }
                convId = chatRepository.createConversation(title)
                _currentConversationId.value = convId
                
                launch(Dispatchers.Main) {
                    selectConversation(convId)
                }
            }

            // Add user message
            chatRepository.addMessage(convId, "user", content, imageBase64)
            
            // Re-fetch context for API
            val contextMessages = chatRepository.getMessages(convId).first()

            _isGenerating.value = true
            
            // Add placeholder assistant message
            val placeholderId = chatRepository.addMessage(convId, "assistant", "")
            var assistantContent = ""

            try {
                apiClient.streamChat(apiKey, _selectedModel.value.id, contextMessages)
                    .catch { e ->
                        assistantContent += "\n\nError: ${e.message}"
                        chatRepository.updateMessage(Message(placeholderId, convId, "assistant", assistantContent))
                    }
                    .collect { chunk ->
                        assistantContent += chunk
                        // Debounce update or update immediately
                        chatRepository.updateMessage(Message(placeholderId, convId, "assistant", assistantContent))
                    }
            } catch (e: Exception) {
                assistantContent += "\n\nError: ${e.message}"
                chatRepository.updateMessage(Message(placeholderId, convId, "assistant", assistantContent))
            } finally {
                _isGenerating.value = false
            }
        }
    }
}
