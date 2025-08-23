package com.ritsu.ai.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.ritsu.ai.data.RitsuDatabase
import com.ritsu.ai.util.PreferenceManager

class RitsuDataProvider : ContentProvider() {
    
    companion object {
        private const val TAG = "RitsuDataProvider"
        private const val AUTHORITY = "com.ritsu.ai.provider"
        
        // URIs
        private val BASE_URI = Uri.parse("content://$AUTHORITY")
        val AVATAR_CONFIG_URI = Uri.withAppendedPath(BASE_URI, "avatar_config")
        val CLOTHING_URI = Uri.withAppendedPath(BASE_URI, "clothing")
        val LEARNING_URI = Uri.withAppendedPath(BASE_URI, "learning")
        val PREFERENCES_URI = Uri.withAppendedPath(BASE_URI, "preferences")
        
        // URI matcher codes
        private const val AVATAR_CONFIG = 1
        private const val CLOTHING = 2
        private const val LEARNING = 3
        private const val PREFERENCES = 4
        
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "avatar_config", AVATAR_CONFIG)
            addURI(AUTHORITY, "clothing", CLOTHING)
            addURI(AUTHORITY, "learning", LEARNING)
            addURI(AUTHORITY, "preferences", PREFERENCES)
        }
    }
    
    private lateinit var database: RitsuDatabase
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(): Boolean {
        try {
            database = RitsuDatabase.getInstance(context!!)
            preferenceManager = PreferenceManager(context!!)
            Log.d(TAG, "ContentProvider creado")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear ContentProvider", e)
            return false
        }
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return try {
            when (uriMatcher.match(uri)) {
                AVATAR_CONFIG -> {
                    database.avatarConfigDao().getAllAvatarConfigs()
                }
                CLOTHING -> {
                    database.clothingItemDao().getAllClothingItems()
                }
                LEARNING -> {
                    database.learningPatternDao().getAllLearningPatterns()
                }
                PREFERENCES -> {
                    // Para preferencias usamos SharedPreferences, no Room
                    null
                }
                else -> {
                    Log.w(TAG, "URI no reconocida: $uri")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en query", e)
            null
        }
    }
    
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            AVATAR_CONFIG -> "vnd.android.cursor.dir/vnd.$AUTHORITY.avatar_config"
            CLOTHING -> "vnd.android.cursor.dir/vnd.$AUTHORITY.clothing"
            LEARNING -> "vnd.android.cursor.dir/vnd.$AUTHORITY.learning"
            PREFERENCES -> "vnd.android.cursor.dir/vnd.$AUTHORITY.preferences"
            else -> null
        }
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return try {
            when (uriMatcher.match(uri)) {
                AVATAR_CONFIG -> {
                    val avatarConfig = values?.let { 
                        com.ritsu.ai.data.model.AvatarConfig.fromContentValues(it) 
                    }
                    avatarConfig?.let {
                        database.avatarConfigDao().insertAvatarConfig(it)
                        Uri.withAppendedPath(AVATAR_CONFIG_URI, it.id.toString())
                    }
                }
                CLOTHING -> {
                    val clothingItem = values?.let { 
                        com.ritsu.ai.data.model.ClothingItem.fromContentValues(it) 
                    }
                    clothingItem?.let {
                        database.clothingItemDao().insertClothingItem(it)
                        Uri.withAppendedPath(CLOTHING_URI, it.id.toString())
                    }
                }
                LEARNING -> {
                    val learningPattern = values?.let { 
                        com.ritsu.ai.data.model.LearningPattern.fromContentValues(it) 
                    }
                    learningPattern?.let {
                        database.learningPatternDao().insertLearningPattern(it)
                        Uri.withAppendedPath(LEARNING_URI, it.id.toString())
                    }
                }
                else -> {
                    Log.w(TAG, "URI no reconocida para insert: $uri")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en insert", e)
            null
        }
    }
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return try {
            when (uriMatcher.match(uri)) {
                AVATAR_CONFIG -> {
                    database.avatarConfigDao().deleteAllAvatarConfigs()
                }
                CLOTHING -> {
                    database.clothingItemDao().deleteAllClothingItems()
                }
                LEARNING -> {
                    database.learningPatternDao().deleteAllLearningPatterns()
                }
                else -> {
                    Log.w(TAG, "URI no reconocida para delete: $uri")
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en delete", e)
            0
        }
    }
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return try {
            when (uriMatcher.match(uri)) {
                AVATAR_CONFIG -> {
                    val avatarConfig = values?.let { 
                        com.ritsu.ai.data.model.AvatarConfig.fromContentValues(it) 
                    }
                    avatarConfig?.let {
                        database.avatarConfigDao().updateAvatarConfig(it)
                        1
                    } ?: 0
                }
                CLOTHING -> {
                    val clothingItem = values?.let { 
                        com.ritsu.ai.data.model.ClothingItem.fromContentValues(it) 
                    }
                    clothingItem?.let {
                        database.clothingItemDao().updateClothingItem(it)
                        1
                    } ?: 0
                }
                LEARNING -> {
                    val learningPattern = values?.let { 
                        com.ritsu.ai.data.model.LearningPattern.fromContentValues(it) 
                    }
                    learningPattern?.let {
                        database.learningPatternDao().updateLearningPattern(it)
                        1
                    } ?: 0
                }
                else -> {
                    Log.w(TAG, "URI no reconocida para update: $uri")
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en update", e)
            0
        }
    }
}