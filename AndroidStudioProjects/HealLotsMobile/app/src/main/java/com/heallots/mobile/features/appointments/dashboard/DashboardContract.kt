package com.heallots.mobile.features.appointments.dashboard

import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.Review
import com.heallots.mobile.models.User

interface DashboardContract {
    interface View {
        fun renderUser(user: User?)
        fun renderAppointments(appointments: List<Appointment>)
        fun renderReviews(reviews: List<Review>)
    }

    interface Presenter {
        fun loadDashboard()
        fun onDestroy()
    }
}
