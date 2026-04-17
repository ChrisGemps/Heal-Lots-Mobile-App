package com.heallots.mobile.features.appointments.list

import com.heallots.mobile.models.Appointment

interface MyAppointmentsContract {
    interface View {
        fun renderAppointments(
            upcoming: List<Appointment>,
            past: List<Appointment>,
            cancelled: List<Appointment>
        )
        fun showNotification(message: String, success: Boolean)
    }

    interface Presenter {
        fun loadAppointments()
        fun cancelAppointment(appointment: Appointment, reason: String)
        fun submitReview(appointment: Appointment, rating: Int, feedback: String)
        fun rescheduleAppointment(appointment: Appointment, newDate: String, newTimeSlot: String, reason: String)
        fun onDestroy()
    }
}
