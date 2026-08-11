package com.example.myinputlog.data.service.impl

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.ui.screens.common.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DefaultPreferenceStorageService @Inject constructor(
    private val datastore: DataStore<Preferences>
) : PreferenceStorageService {

    private fun buildLongKey(baseKey: String, userId: String) =
        longPreferencesKey("${userId}_$baseKey")

    private fun buildStringKey(baseKey: String, userId: String) =
        stringPreferencesKey("${userId}_$baseKey")

    override fun currentCourseId(userId: String): Flow<String> = datastore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[buildStringKey(CURRENT_COURSE_ID, userId)] ?: ""
    }

    override fun themeMode(userId: String): Flow<AppTheme> = datastore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }.map { preferences ->
        val name = preferences[buildStringKey(THEME_MODE, userId)] ?: AppTheme.SYSTEM.name
        AppTheme.valueOf(name)
    }

    override fun confettiColors(userId: String): Flow<ConfettiOptions> =
        datastore.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            val name =
                preferences[buildStringKey(CONFETTI_COLORS, userId)] ?: ConfettiOptions.OPTION1.name
            ConfettiOptions.valueOf(name)
        }

    override suspend fun saveCurrentCourseId(userId: String, courseId: String) {
        datastore.edit { preferences ->
            preferences[buildStringKey(CURRENT_COURSE_ID, userId)] = courseId
        }
    }

    override suspend fun clearCurrentCourseId(userId: String) {
        datastore.edit { preferences ->
            preferences.remove(buildStringKey(CURRENT_COURSE_ID, userId))
        }
    }

    override suspend fun saveThemeMode(userId: String, theme: AppTheme) {
        datastore.edit { preferences ->
            preferences[buildStringKey(THEME_MODE, userId)] = theme.name
        }
    }

    override suspend fun saveConfettiColors(userId: String, colors: ConfettiOptions) {
        datastore.edit { preferences ->
            preferences[buildStringKey(CONFETTI_COLORS, userId)] = colors.name
        }
    }

    override suspend fun getLastPullTimestamp(userId: String): Long {
        return try {
            datastore.data.catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                preferences[buildLongKey(LAST_PULL, userId)] ?: 0L
            }.first()
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
            0L
        }
    }

    override suspend fun saveLastPullTimestamp(userId: String, timestamp: Long) {
        datastore.edit { preferences ->
            preferences[buildLongKey(LAST_PULL, userId)] = timestamp
        }
    }

    private companion object {
        const val TAG = "PreferencesStorage"
        const val CURRENT_COURSE_ID = "current_course_id"
        const val THEME_MODE = "theme_mode"
        const val CONFETTI_COLORS = "confetti_colors"
        const val LAST_PULL = "last_pull_timestamp"
    }
}