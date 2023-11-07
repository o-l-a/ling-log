package com.example.myinputlog.data.service.impl

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.myinputlog.data.service.PreferenceStorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DefaultPreferenceStorageService @Inject constructor(
    private val datastore: DataStore<Preferences>
) : PreferenceStorageService {

    val currentCourseId: Flow<String> = datastore.data.catch {
        if (it is IOException) {
            Log.e(TAG, "Error reading preferences.", it)
            emit(emptyPreferences())
        } else {
            throw it
        }
    }.map { preferences ->
        preferences[CURRENT_COURSE_ID] ?: ""
    }

    override suspend fun saveCurrentCourseId(courseId: String) {
        datastore.edit { preferences ->
            preferences[CURRENT_COURSE_ID] = courseId
        }
    }

    private companion object {
        const val TAG = "PreferencesStorage"
        val CURRENT_COURSE_ID = stringPreferencesKey("current_course_id")
    }
}