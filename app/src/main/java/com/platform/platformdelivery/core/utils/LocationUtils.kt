package com.platform.platformdelivery.core.utils

object LocationUtils {
    /**
     * Formats latitude or longitude to 4 decimal places
     * @param value The coordinate value as String or Double
     * @return Formatted string with 4 decimal places, or null if input is null/empty
     */
    fun formatCoordinate(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return try {
            val doubleValue = value.toDouble()
            String.format("%.4f", doubleValue)
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Formats latitude or longitude to 4 decimal places
     * @param value The coordinate value as Double
     * @return Formatted string with 4 decimal places, or null if input is null
     */
    fun formatCoordinate(value: Double?): String? {
        if (value == null) return null
        return String.format("%.4f", value)
    }
}

