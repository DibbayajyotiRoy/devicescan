package com.devicelens.app.helpers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * In-app debug logger. All scan-related events are pushed here so they
 * can be viewed on the Debug Log screen without needing ADB.
 */
object DebugLog {

    data class Entry(
        val timestamp: Long = System.currentTimeMillis(),
        val tag: String,
        val message: String,
        val level: Level = Level.INFO
    ) {
        enum class Level { INFO, WARN, ERROR }

        fun formatted(): String {
            val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
            val prefix = when (level) {
                Level.INFO -> "ℹ️"
                Level.WARN -> "⚠️"
                Level.ERROR -> "❌"
            }
            return "$time $prefix [$tag] $message"
        }
    }

    private const val MAX_ENTRIES = 500

    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries

    fun i(tag: String, message: String) = add(Entry(tag = tag, message = message, level = Entry.Level.INFO))
    fun w(tag: String, message: String) = add(Entry(tag = tag, message = message, level = Entry.Level.WARN))
    fun e(tag: String, message: String) = add(Entry(tag = tag, message = message, level = Entry.Level.ERROR))

    fun clear() {
        _entries.value = emptyList()
    }

    private fun add(entry: Entry) {
        _entries.value = (_entries.value + entry).takeLast(MAX_ENTRIES)
    }
}
