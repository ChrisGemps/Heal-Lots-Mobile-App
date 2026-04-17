package com.heallots.mobile.features.appointments.book

import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.BookAppointmentRequest

interface BookAppointmentContract {
    interface View {
        fun showBookingLoading()
        fun hideBookingLoading()
        fun showMessage(message: String)
        fun navigateToAppointments()
    }

    interface Presenter {
        fun bookAppointment(request: BookAppointmentRequest)
        fun onDestroy()
    }
}
