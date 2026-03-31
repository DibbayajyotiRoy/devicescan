package com.devicelens.app.domain.classification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OuiLookup @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val table: Map<String, String> by lazy {
        try {
            context.assets.open("oui.txt").bufferedReader().useLines { lines ->
                buildMap {
                    lines.forEach { line ->
                        val parts = line.split("\t")
                        if (parts.size >= 2) {
                            put(parts[0].uppercase().trim(), parts[1].trim())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun lookup(mac: String): String {
        if (mac.length < 8) return "Unknown"
        return table[mac.take(8).uppercase()] ?: "Unknown"
    }
}
