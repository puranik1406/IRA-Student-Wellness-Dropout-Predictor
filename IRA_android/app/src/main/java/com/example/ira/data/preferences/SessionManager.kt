package com.example.ira.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ira_session")

class SessionManager(private val context: Context) {

    companion object {
        private val USER_ID = longPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_TYPE = stringPreferencesKey("user_type") // "student" or "counselor"
        private val IS_LOGGED_IN = stringPreferencesKey("is_logged_in")
    }

    // Save user session
    suspend fun saveUserSession(
        userId: Long,
        userName: String,
        userEmail: String,
        userType: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_NAME] = userName
            preferences[USER_EMAIL] = userEmail
            preferences[USER_TYPE] = userType
            preferences[IS_LOGGED_IN] = "true"
        }
    }

    // Get user session as Flow
    val userSession: Flow<UserSession?> = context.dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGGED_IN] == "true"
        if (isLoggedIn) {
            UserSession(
                userId = preferences[USER_ID] ?: 0L,
                userName = preferences[USER_NAME] ?: "",
                userEmail = preferences[USER_EMAIL] ?: "",
                userType = preferences[USER_TYPE] ?: ""
            )
        } else {
            null
        }
    }

    // Check if user is logged in
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] == "true"
    }

    // Get user type
    val userType: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TYPE]
    }

    // Clear session (logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Get user ID
    suspend fun getUserId(): Long? {
        var userId: Long? = null
        context.dataStore.data.map { preferences ->
            userId = preferences[USER_ID]
        }
        return userId
    }
}

data class UserSession(
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val userType: String // "student" or "counselor"
)
