package com.example.repository

import com.example.data.local.room.ChatDao
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val conversations: Flow<List<Conversation>> = chatDao.getAllConversations()

    fun getMessages(conversationId: Long): Flow<List<Message>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun createConversation(title: String): Long {
        return chatDao.insertConversation(Conversation(title = title))
    }

    suspend fun addMessage(conversationId: Long, role: String, content: String, imageBase64: String? = null): Long {
        chatDao.updateConversationTimestamp(conversationId)
        return chatDao.insertMessage(Message(conversationId = conversationId, role = role, content = content, imageBase64 = imageBase64))
    }
    
    suspend fun updateMessage(message: Message) {
        chatDao.updateMessage(message)
    }

    suspend fun updateConversationTitle(id: Long, title: String) {
        chatDao.updateConversationTitle(id, title)
    }

    suspend fun deleteConversation(id: Long) {
        chatDao.deleteConversation(id)
    }

    suspend fun clearHistory() {
        chatDao.clearHistory()
    }
}
