package com.mealmatch.data.local

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "MealMatchPrefs"
    private const val KEY_AUTH_TOKEN = "authToken"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_AUTH_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_AUTH_TOKEN)
        editor.apply()
    }
}