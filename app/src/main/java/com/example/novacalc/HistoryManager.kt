package com.example.novacalc

import android.content.Context
import android.content.SharedPreferences

data class HistoryItem(val expression: String, val result: String)

class HistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calc_history_prefs", Context.MODE_PRIVATE)

    fun addHistory(expression: String, result: String) {
        if (expression.isBlank() || result == "Error") return
        val currentList = getHistory().toMutableList()
        // Avoid duplicate consecutive entries
        if (currentList.isNotEmpty() && currentList[0].expression == expression && currentList[0].result == result) {
            return
        }
        currentList.add(0, HistoryItem(expression, result))
        // Limit to top 50
        val trimmed = if (currentList.size > 50) currentList.subList(0, 50) else currentList
        saveList(trimmed)
    }

    fun getHistory(): List<HistoryItem> {
        val raw = prefs.getString("history_data", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";;;").mapNotNull { entry ->
            val parts = entry.split("|||")
            if (parts.size == 2) HistoryItem(parts[0], parts[1]) else null
        }
    }

    fun clearHistory() {
        prefs.edit().remove("history_data").apply()
    }

    private fun saveList(list: List<HistoryItem>) {
        val serialized = list.joinToString(";;;") { "${it.expression}|||${it.result}" }
        prefs.edit().putString("history_data", serialized).apply()
    }
}
