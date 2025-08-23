package com.ritsuai.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ritsuai.database.dao.*
import com.ritsuai.database.entities.*
import com.ritsuai.database.converters.*

@Database(
    entities = [
        ConversationEntity::class,
        UserPreferenceEntity::class,
        AvatarOutfitEntity::class,
        PhoneActionEntity::class,
        LearningDataEntity::class,
        ClothingItemEntity::class,
        SystemLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DateConverter::class,
    StringListConverter::class,
    JsonConverter::class
)
abstract class RitsuDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun conversationDao(): ConversationDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun avatarOutfitDao(): AvatarOutfitDao
    abstract fun phoneActionDao(): PhoneActionDao
    abstract fun learningDataDao(): LearningDataDao
    abstract fun clothingItemDao(): ClothingItemDao
    abstract fun systemLogDao(): SystemLogDao
    
    companion object {
        const val DATABASE_NAME = "ritsu_database"
    }
}