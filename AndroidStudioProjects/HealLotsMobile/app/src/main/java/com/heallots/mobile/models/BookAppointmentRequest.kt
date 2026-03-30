package com.heallots.mobile.models

class BookAppointmentRequest(
    var serviceName: String? = null,
    var specialistName: String? = null,
    var appointmentDate: String? = null,
    var timeSlot: String? = null,
    var reason: String? = null,
    var notes: String? = null
)
