package com.heallots.mobile.features.admin.dashboard

import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.User

interface AdminDashboardContract {
    interface View {
        fun renderAdminUser(user: User?)
        fun renderAdminData(appointments: List<Appointment>, users: List<User>)
    }

    interface Presenter {
        fun loadAdminData()
        fun updateAppointmentStatus(appointment: Appointment, status: String)
        fun rescheduleAppointment(appointment: Appointment, newDate: String, newTimeSlot: String)
        fun onDestroy()
    }
}
