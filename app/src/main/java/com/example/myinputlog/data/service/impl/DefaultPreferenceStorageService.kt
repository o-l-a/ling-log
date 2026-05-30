package com.example.myinputlog.data.service.impl

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myinputlog.data.service.PreferenceStorageService
import com.example.myinputlog.ui.screens.utils.ConfettiOptions
import com.example.myinputlog.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DefaultPreferenceStorageService @Inject constructor(
    private val datastore: DataStore<Preferences>
) : PreferenceStorageService {

    override val currentCourseId: Flow<String> = datastore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[CURRENT_COURSE_ID] ?: ""
    }

    override val themeMode: Flow<AppTheme> = datastore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }.map { preferences ->
        val name = preferences[THEME_MODE] ?: AppTheme.SYSTEM.name
        AppTheme.valueOf(name)
    }

    override val confettiColors: Flow<ConfettiOptions> = datastore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }.map { preferences ->
        val name = preferences[CONFETTI_COLORS] ?: ConfettiOptions.OPTION1.name
        ConfettiOptions.valueOf(name)
    }

    override suspend fun saveCurrentCourseId(courseId: String) {
        datastore.edit { preferences ->
            preferences[CURRENT_COURSE_ID] = courseId
        }
    }

    override suspend fun saveThemeMode(theme: AppTheme) {
        datastore.edit { preferences ->
            preferences[THEME_MODE] = theme.name
        }
    }

    override suspend fun saveConfettiColors(colors: ConfettiOptions) {
        datastore.edit { preferences ->
            preferences[CONFETTI_COLORS] = colors.name
        }
    }

    private companion object {
        const val TAG = "PreferencesStorage"
        val CURRENT_COURSE_ID = stringPreferencesKey("current_course_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CONFETTI_COLORS = stringPreferencesKey("confetti_colors")
    }
}