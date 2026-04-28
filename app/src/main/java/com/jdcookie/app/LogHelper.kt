package com.jdcookie.app

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogHelper {
    private const val PREF_NAME = "AppLogs"
    private const val KEY_LOGS = "logs"
    private const val MAX_LOGS = 200
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val lock = Any()

    fun log(context: Context, level: String, tag: String, message: String) {
        synchronized(lock) {
            val logs = getLogs(context)
            val entry = JSONObject().apply {
                put("time", dateFormat.format(Date()))
                put("level", level)
                put("tag", tag)
                put("message", message)
            }
            logs.put(entry)
            if (logs.length() > MAX_LOGS) {
                val trimmed = JSONArray()
                for (i in logs.length() - MAX_LOGS until logs.length()) {
                    trimmed.put(logs.get(i))
                }
                saveLogs(context, trimmed.toString())
            } else {
                saveLogs(context, logs.toString())
            }
        }
    }

    fun info(context: Context, tag: String, message: String) {
        log(context, "INFO", tag, message)
    }

    fun error(context: Context, tag: String, message: String) {
        log(context, "ERROR", tag, message)
    }

    fun warn(context: Context, tag: String, message: String) {
        log(context, "WARN", tag, message)
    }

    fun getLogs(context: Context): JSONArray {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LOGS, null) ?: return JSONArray()
        return JSONArray(json)
    }

    fun clearLogs(context: Context) {
        synchronized(lock) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit { clear() }
        }
    }

    private fun saveLogs(context: Context, json: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_LOGS, json)
        }
    }
}
