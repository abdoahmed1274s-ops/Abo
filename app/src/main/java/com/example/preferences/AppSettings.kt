package com.example.preferences

import android.content.Context
import android.content.SharedPreferences

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("action_downloader_prefs", Context.MODE_PRIVATE)

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var isRtl: Boolean
        get() = prefs.getBoolean(KEY_RTL, true)
        set(value) = prefs.edit().putBoolean(KEY_RTL, value).apply()

    var isWifiOnly: Boolean
        get() = prefs.getBoolean(KEY_WIFI_ONLY, false)
        set(value) = prefs.edit().putBoolean(KEY_WIFI_ONLY, value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var defaultQuality: String
        get() = prefs.getString(KEY_DEFAULT_QUALITY, "1080p") ?: "1080p"
        set(value) = prefs.edit().putString(KEY_DEFAULT_QUALITY, value).apply()

    companion object {
        private const val KEY_DARK_THEME = "key_dark_theme"
        private const val KEY_RTL = "key_rtl"
        private const val KEY_WIFI_ONLY = "key_wifi_only"
        private const val KEY_NOTIFICATIONS = "key_notifications"
        private const val KEY_DEFAULT_QUALITY = "key_default_quality"
    }
}
