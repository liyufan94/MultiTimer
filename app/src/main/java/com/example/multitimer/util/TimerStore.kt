package com.example.multitimer.util

import android.content.Context
import com.example.multitimer.TimerItem
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object TimerStore {
    private val PREF = "timer_pref"
    private val KEY = "times"

    fun save(context: Context, items: List<TimerItem>) {
        val array = JSONArray();
        items.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("totalMillis", it.totalMillis)
            obj.put("endTime", it.endTime)
            array.put(obj)
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit { putString(KEY, array.toString()).apply() }
    }

    fun load(context: Context): List<TimerItem> {
        val json = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList();

        val result = mutableListOf<TimerItem>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.optString("name", "计时器")
            val totalMillis = obj.optLong("totalMillis", 0L)
            val endTime = obj.optLong("endTime", 0L)
            result.add(TimerItem(name, 0L, totalMillis, endTime))
        }
        return result;
    }

}