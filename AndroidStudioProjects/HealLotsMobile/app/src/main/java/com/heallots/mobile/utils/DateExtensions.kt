package com.heallots.mobile.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Convert Long timestamp to formatted date string
 */
fun Long.toFormattedDate(pattern: String = "yyyy-MM-dd hh:mm a", locale: Locale = Locale.US): String {
    return try {
        SimpleDateFormat(pattern, locale).format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

/**
 * Convert Long timestamp to date with default pattern
 */
fun Long.formatDate(): String = toFormattedDate("yyyy-MM-dd", Locale.US)

/**
 * Convert Long timestamp to time with default pattern
 */
fun Long.formatTime(): String = toFormattedDate("hh:mm a", Locale.US)

/**
 * Convert Long timestamp to datetime with default pattern
 */
fun Long.formatDateTime(): String = toFormattedDate("yyyy-MM-dd hh:mm a", Locale.US)

/**
 * Convert Calendar to formatted date string
 */
fun Calendar.toFormattedString(pattern: String = "MMMM yyyy", locale: Locale = Locale.US): String {
    return try {
        SimpleDateFormat(pattern, locale).format(this.time)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Convert Calendar to date string with default pattern
 */
fun Calendar.formatDate(): String = toFormattedString("yyyy-MM-dd", Locale.US)

/**
 * Convert Calendar to month-year string
 */
fun Calendar.formatMonthYear(): String = toFormattedString("MMMM yyyy", Locale.US)

/**
 * Convert date string to timestamp
 */
fun String.toTimestamp(pattern: String = "yyyy-MM-dd hh:mm a", locale: Locale = Locale.US): Long {
    return try {
        SimpleDateFormat(pattern, locale).parse(this)?.time ?: Long.MAX_VALUE
    } catch (e: Exception) {
        Long.MAX_VALUE
    }
}

/**
 * Parse date string with custom pattern
 */
fun String.parseDate(pattern: String = "yyyy-MM-dd"): Date? {
    return try {
        SimpleDateFormat(pattern, Locale.US).parse(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * Check if date is today
 */
fun Calendar.isToday(): Boolean {
    val today = Calendar.getInstance()
    return get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

/**
 * Check if date is in the past
 */
fun Calendar.isPast(): Boolean = before(Calendar.getInstance())

/**
 * Check if date is in the future
 */
fun Calendar.isFuture(): Boolean = after(Calendar.getInstance())

/**
 * Add days to calendar
 */
fun Calendar.addDays(days: Int): Calendar {
    add(Calendar.DAY_OF_MONTH, days)
    return this
}

/**
 * Add months to calendar
 */
fun Calendar.addMonths(months: Int): Calendar {
    add(Calendar.MONTH, months)
    return this
}

/**
 * Set to start of day (00:00:00)
 */
fun Calendar.startOfDay(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

/**
 * Set to end of day (23:59:59)
 */
fun Calendar.endOfDay(): Calendar {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
    return this
}
