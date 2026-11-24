package com.nrcm.student.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

// Serializable data classes for caching
@kotlinx.serialization.Serializable
data class CachedAttendance(
    val months: List<MonthRecord>,
    val semester: SemesterSummary,
    val lastUpdated: Long, // epoch millis
    val fetchSuccessful: Boolean
)

// Cache helper with EncryptedSharedPreferences for Remember Me
object CacheManager {
    private const val PREFS_NAME = "nrcm_student_cache"
    private const val KEY_CACHED_DATA = "cached_attendance"
    private const val KEY_LAST_REFRESH = "last_refresh"

    // Save cached attendance data
    fun saveCache(context: Context, data: CachedAttendance) {
        try {
            val json = Json.encodeToString(data)
            context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CACHED_DATA, json)
                .putLong(KEY_LAST_REFRESH, data.lastUpdated)
                .apply()
        } catch (e: Exception) {
            // Silently fail - cache is optional
        }
    }

    // Load cached attendance data, returns null if no cache exists
    fun loadCache(context: Context): CachedAttendance? {
        try {
            val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = sharedPrefs.getString(KEY_CACHED_DATA, null)
            return if (json != null) Json.decodeFromString<CachedAttendance>(json) else null
        } catch (e: Exception) {
            // Silently fail
            return null
        }
    }

    // Clear cached attendance data on logout
    fun clearCache(context: Context) {
        try {
            context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        } catch (e: Exception) {
            // Silently fail
        }
    }

    // Remember Me: save encrypted roll number/password
    fun saveRememberMe(context: Context, rollNo: String, pass: String) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "nrcm_student_encrypted",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            encryptedPrefs.edit()
                .putString("roll_no", rollNo)
                .putString("password", pass)
                .apply()
        } catch (e: Exception) {
            // Silently fail
        }
    }

    fun clearRememberMe(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "nrcm_student_encrypted",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            encryptedPrefs.edit().remove("roll_no").remove("password").apply()
        } catch (e: Exception) {
            // Silently fail
        }
    }

    fun loadRememberMe(context: Context): Pair<String, String>? {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "nrcm_student_encrypted",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val roll = encryptedPrefs.getString("roll_no", null)
            val pass = encryptedPrefs.getString("password", null)
            return if (roll != null && pass != null) roll to pass else null
        } catch (e: Exception) {
            return null
        }
    }
}
