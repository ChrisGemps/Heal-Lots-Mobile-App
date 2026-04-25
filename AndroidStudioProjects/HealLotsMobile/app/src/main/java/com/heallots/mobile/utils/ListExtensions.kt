package com.heallots.mobile.utils

import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.Review
import kotlin.jvm.JvmName

/**
 * Filter appointments by status
 */
fun List<Appointment>.filterByStatus(status: String): List<Appointment> =
    filter { it.status?.contains(status, ignoreCase = true) == true }

/**
 * Filter appointments by specialist
 */
@JvmName("filterAppointmentsBySpecialist")
fun List<Appointment>.filterBySpecialist(specialistName: String): List<Appointment> =
    filter { it.specialistName?.contains(specialistName, ignoreCase = true) == true }

/**
 * Filter appointments by date
 */
fun List<Appointment>.filterByDate(date: String): List<Appointment> =
    filter { it.appointmentDate == date }

/**
 * Calculate average rating from reviews
 */
fun List<Review>.calculateAverageRating(): Double {
    if (isEmpty()) return 0.0
    val sum = sumOf { it.rating.toDouble() }
    return sum / size
}

/**
 * Filter reviews by specialist
 */
@JvmName("filterReviewsBySpecialist")
fun List<Review>.filterBySpecialist(specialistName: String): List<Review> =
    filter { it.specialistName?.contains(specialistName, ignoreCase = true) == true }

/**
 * Filter reviews by rating
 */
fun List<Review>.filterByRating(minRating: Int): List<Review> =
    filter { it.rating >= minRating }

/**
 * Sort appointments by date (newest first)
 */
fun List<Appointment>.sortByDateNewest(): List<Appointment> =
    sortedByDescending { it.appointmentDate }

/**
 * Sort appointments by date (oldest first)
 */
fun List<Appointment>.sortByDateOldest(): List<Appointment> =
    sortedBy { it.appointmentDate }

/**
 * Sort reviews by rating (highest first)
 */
fun List<Review>.sortByRatingHighest(): List<Review> =
    sortedByDescending { it.rating }

/**
 * Sort reviews by rating (lowest first)
 */
fun List<Review>.sortByRatingLowest(): List<Review> =
    sortedBy { it.rating }

/**
 * Get recent appointments (limit)
 */
fun List<Appointment>.getRecent(limit: Int): List<Appointment> =
    sortByDateNewest().take(limit)

/**
 * Check if appointment exists by id
 */
fun List<Appointment>.existsById(id: String?): Boolean =
    any { it.id == id }

/**
 * Find appointment by id
 */
fun List<Appointment>.findById(id: String?): Appointment? =
    find { it.id == id }
