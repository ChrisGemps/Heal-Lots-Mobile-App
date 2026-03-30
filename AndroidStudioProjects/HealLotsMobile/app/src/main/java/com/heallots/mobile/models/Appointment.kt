package com.heallots.mobile.models

class Appointment {
    var id: String? = null
    var serviceName: String? = null
    var specialistName: String? = null
    var patientName: String? = null
    var patientEmail: String? = null
    var patientPhone: String? = null
    var appointmentDate: String? = null
    var timeSlot: String? = null
    var reason: String? = null
    var notes: String? = null
    var status: String? = null
    var rescheduleReason: String? = null
    var cancellationReason: String? = null
    var createdAt: String? = null
    var updatedAt: String? = null

    private var reviewed: Boolean = false

    fun isReviewed(): Boolean = reviewed

    fun setReviewed(reviewed: Boolean) {
        this.reviewed = reviewed
    }
}
