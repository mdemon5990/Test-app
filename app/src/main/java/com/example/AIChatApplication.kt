package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.room.AppDatabase
import com.example.repository.ChatRepository
import com.example.repository.SettingsRepository

class AIChatApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var chatRepository: ChatRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "ai_chat_db"
        ).fallbackToDestructiveMigration().build()
        chatRepository = ChatRepository(database.chatDao())
        settingsRepository = SettingsRepository(this)
    }
}
