package com.example.jdcookie

import android.content.Context
import androidx.core.content.edit

object PrefsHelper {

    fun save(prefName: String, context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    fun get(prefName: String, context: Context, key: String, defaultValue: String = ""): String {
        val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getAll(prefName: String, context: Context): Map<String, *> {
        val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        return sharedPreferences.all
    }

    fun clear(prefName: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            clear()
        }
    }
}