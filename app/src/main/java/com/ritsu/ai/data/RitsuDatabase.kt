package com.ritsu.ai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ritsu.ai.data.dao.AvatarConfigDao
import com.ritsu.ai.data.dao.ClothingItemDao
import com.ritsu.ai.data.dao.LearningPatternDao
import com.ritsu.ai.data.dao.UserPreferenceDao
import com.ritsu.ai.data.model.AvatarConfig
import com.ritsu.ai.data.model.ClothingItem
import com.ritsu.ai.data.model.LearningPattern
import com.ritsu.ai.data.model.UserPreference
import com.ritsu.ai.data.util.Converters

@Database(
    entities = [
        AvatarConfig::class,
        ClothingItem::class,
        LearningPattern::class,
        UserPreference::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RitsuDatabase : RoomDatabase() {
    
    abstract fun avatarConfigDao(): AvatarConfigDao
    abstract fun clothingItemDao(): ClothingItemDao
    abstract fun learningPatternDao(): LearningPatternDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    
    companion object {
        const val DATABASE_NAME = "ritsu_database"
        
        @Volatile
        private var INSTANCE: RitsuDatabase? = null
        
        fun getInstance(context: Context): RitsuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RitsuDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}