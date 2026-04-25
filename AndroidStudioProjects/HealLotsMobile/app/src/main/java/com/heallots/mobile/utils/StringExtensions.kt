package com.heallots.mobile.utils

/**
 * Safe null/blank coalescing for Strings
 */
fun String?.orBlank(): String = if (this.isNullOrBlank()) "" else this

/**
 * Return provided default if string is null or blank
 */
fun String?.ifBlank(default: String): String = if (this.isNullOrBlank()) default else this

/**
 * Safe trim with fallback for null/blank strings
 */
fun String?.trimOrDefault(fallback: String = ""): String = if (this.isNullOrBlank()) fallback else this.trim()

/**
 * Get first character of string as uppercase, or empty string if null
 */
fun String?.firstChar(): String = this?.firstOrNull()?.uppercase() ?: ""

/**
 * Extract first name from full name (up to first space)
 */
fun String?.getFirstName(): String = this?.takeWhile { it != ' ' } ?: ""

/**
 * Case-insensitive equality check
 */
fun String.equalsIgnoreCase(other: String?): Boolean = this.equals(other, ignoreCase = true)

/**
 * Check if string is a valid email format
 */
fun String?.isValidEmail(): Boolean = this?.contains("@") == true && this.contains(".")

/**
 * Check if string is not null and not blank (opposite of isNullOrBlank)
 */
fun String?.isValid(): Boolean = !this.isNullOrBlank()

/**
 * Return this string if not blank, otherwise return fallback
 */
infix fun String?.or(fallback: String): String = if (this.isNullOrBlank()) fallback else this
