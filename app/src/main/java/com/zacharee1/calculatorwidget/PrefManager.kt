package com.zacharee1.calculatorwidget

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.preference.PreferenceManager

val Context.prefManager: PrefManager
    get() = PrefManager.getInstance(this)

class PrefManager private constructor(context: Context) {
    companion object {
        const val BACKGROUND_COLOR_BASE = "background_color_"
        const val BORDER_COLOR_BASE = "border_color_"
        const val TEXT_COLOR_BASE = "text_color_"

        private var instance: PrefManager? = null

        fun getInstance(context: Context): PrefManager {
            if (instance == null) instance = PrefManager(context.applicationContext)

            return instance!!
        }
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getBackgroundColorForId(id: Int) =
            getInt(BACKGROUND_COLOR_BASE + id, Color.TRANSPARENT)

    fun setBackgroundColorForId(id: Int, color: Int) =
            putInt(BACKGROUND_COLOR_BASE + id, color)

    fun getBorderColorForId(id: Int) =
            getInt(BORDER_COLOR_BASE + id, Color.WHITE)

    fun setBorderColorForId(id: Int, color: Int) =
            putInt(BORDER_COLOR_BASE + id, color)

    fun getTextColorForId(id: Int) =
            getInt(TEXT_COLOR_BASE + id, Color.WHITE)

    fun setTextColorForId(id: Int, color: Int) =
            putInt(TEXT_COLOR_BASE + id, color)

    fun getBoolean(key: String, def: Boolean) = prefs.getBoolean(key, def)
    fun getFloat(key: String, def: Float) = prefs.getFloat(key, def)
    fun getInt(key: String, def: Int) = prefs.getInt(key, def)
    fun getString(key: String, def: String? = null) = prefs.getString(key, def)
    fun getStringSet(key: String, def: Set<String>) = prefs.getStringSet(key, def)

    fun remove(key: String) = prefs.edit().remove(key).apply()

    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun putFloat(key: String, value: Float) = prefs.edit().putFloat(key, value).apply()
    fun putInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun putString(key: String, value: String?) = prefs.edit().putString(key, value).apply()
    fun putStringSet(key: String, set: Set<String>) = prefs.edit().putStringSet(key, set).apply()

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
            prefs.registerOnSharedPreferenceChangeListener(listener)

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
}