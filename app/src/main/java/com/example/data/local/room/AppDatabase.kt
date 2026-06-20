package com.example.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.domain.model.Conversation
import com.example.domain.model.Message

@Database(entities = [Conversation::class, Message::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
