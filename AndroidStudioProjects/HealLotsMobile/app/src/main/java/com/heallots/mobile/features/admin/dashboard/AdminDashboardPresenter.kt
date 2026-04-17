package com.heallots.mobile.features.admin.dashboard

import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.User
import java.text.SimpleDateFormat
import java.util.Locale

class AdminDashboardPresenter(
    private var view: AdminDashboardContract.View?,
    private val repository: AdminDashboardRepository
) : AdminDashboardContract.Presenter {
    private val appointments = ArrayList<Appointment>()
    private val users = ArrayList<User>()

    override fun loadAdminData() {
        view?.renderAdminUser(repository.getCurrentUser())
        repository.loadAppointments {
            appointments.clear()
            appointments.addAll(it.sortedBy { appointment -> toTimestamp(appointment) })
            view?.renderAdminData(appointments, users)
        }
        repository.loadUsers {
            users.clear()
            users.addAll(it)
            view?.renderAdminData(appointments, users)
        }
    }

    override fun updateAppointmentStatus(appointment: Appointment, status: String) {
        val id = appointment.id ?: return
        repository.updateAppointmentStatus(id, status) {
            applyUpdate(it)
        }
    }

    override fun rescheduleAppointment(appointment: Appointment, newDate: String, newTimeSlot: String) {
        val id = appointment.id ?: return
        repository.rescheduleAppointment(id, newDate, newTimeSlot) {
            applyUpdate(it)
        }
    }

    override fun onDestroy() {
        view = null
    }

    private fun applyUpdate(updated: Appointment) {
        for (i in appointments.indices) {
            if (appointments[i].id == updated.id) {
                appointments[i] = updated
                view?.renderAdminData(appointments, users)
                return
            }
        }
        appointments.add(updated)
        view?.renderAdminData(appointments, users)
    }

    private fun toTimestamp(appointment: Appointment): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                .parse("${appointment.appointmentDate ?: ""} ${appointment.timeSlot ?: ""}")
                ?.time ?: Long.MAX_VALUE
        } catch (_: Exception) {
            Long.MAX_VALUE
        }
    }
}
