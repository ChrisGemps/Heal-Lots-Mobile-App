package com.heallots.mobile.features.appointments.list

import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.Review
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class MyAppointmentsPresenter(
    private var view: MyAppointmentsContract.View?,
    private val repository: MyAppointmentsRepository
) : MyAppointmentsContract.Presenter {
    private val allAppointments = ArrayList<Appointment>()

    override fun loadAppointments() {
        repository.loadAppointments(
            onSuccess = { appointments ->
                allAppointments.clear()
                allAppointments.addAll(appointments)
                for (appointment in allAppointments) {
                    val status = normalizeStatus(appointment.status)
                    if (isPastStatus(status) && !appointment.id.isNullOrBlank()) {
                        repository.checkAppointmentReviewed(appointment.id!!) { reviewed ->
                            appointment.setReviewed(reviewed)
                            renderBuckets()
                        }
                    }
                }
                renderBuckets()
            },
            onError = { message ->
                allAppointments.clear()
                renderBuckets()
                view?.showNotification(message, false)
            }
        )
    }

    override fun cancelAppointment(appointment: Appointment, reason: String) {
        val id = appointment.id
        if (id.isNullOrBlank()) {
            view?.showNotification("Please sign in again to manage this appointment.", false)
            return
        }
        repository.cancelAppointment(
            appointmentId = id,
            reason = reason,
            onSuccess = { updated ->
                applyAppointmentUpdate(updated)
                view?.showNotification("Appointment cancelled successfully.", true)
            },
            onError = { message -> view?.showNotification(message, false) }
        )
    }

    override fun submitReview(appointment: Appointment, rating: Int, feedback: String) {
        val currentUser = repository.getCurrentUser()
        val appointmentId = appointment.id
        if (appointmentId.isNullOrBlank()) {
            view?.showNotification("Please sign in again to submit your review.", false)
            return
        }
        val review = Review().apply {
            this.appointmentId = appointmentId
            specialistName = appointment.specialistName
            serviceName = appointment.serviceName
            this.rating = rating
            reviewText = feedback
            patientName = currentUser?.fullName ?: "Patient"
            patientEmail = currentUser?.email ?: ""
        }
        repository.submitReview(
            review = review,
            onSuccess = {
                appointment.setReviewed(true)
                renderBuckets()
                view?.showNotification("Thank you for your review.", true)
            },
            onError = { message -> view?.showNotification(message, false) }
        )
    }

    override fun rescheduleAppointment(appointment: Appointment, newDate: String, newTimeSlot: String, reason: String) {
        val id = appointment.id
        if (id.isNullOrBlank()) {
            view?.showNotification("Please sign in again to reschedule this appointment.", false)
            return
        }
        repository.rescheduleAppointment(
            appointmentId = id,
            newDate = newDate,
            newTimeSlot = newTimeSlot,
            reason = reason,
            onSuccess = { updated ->
                applyAppointmentUpdate(updated)
                view?.showNotification("Appointment rescheduled successfully.", true)
            },
            onError = { message -> view?.showNotification(message, false) }
        )
    }

    override fun onDestroy() {
        view = null
    }

    private fun renderBuckets() {
        val upcoming = ArrayList<Appointment>()
        val past = ArrayList<Appointment>()
        val cancelled = ArrayList<Appointment>()
        for (appointment in allAppointments) {
            when {
                isCancelledStatus(normalizeStatus(appointment.status)) -> cancelled.add(appointment)
                isPastStatus(normalizeStatus(appointment.status)) -> past.add(appointment)
                else -> upcoming.add(appointment)
            }
        }
        upcoming.sortBy { getAppointmentTimestamp(it) }
        past.sortByDescending { getAppointmentTimestamp(it) }
        cancelled.sortByDescending { getAppointmentTimestamp(it) }
        view?.renderAppointments(upcoming, past, cancelled)
    }

    private fun applyAppointmentUpdate(updated: Appointment) {
        for (i in allAppointments.indices) {
            if (safe(allAppointments[i].id) == safe(updated.id)) {
                allAppointments[i] = updated
                renderBuckets()
                return
            }
        }
        allAppointments.add(updated)
        renderBuckets()
    }

    private fun getAppointmentTimestamp(appointment: Appointment?): Long {
        if (appointment == null) return Long.MAX_VALUE
        return try {
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                .parse("${safe(appointment.appointmentDate)} ${safe(appointment.timeSlot)}")?.time ?: Long.MAX_VALUE
        } catch (_: ParseException) {
            Long.MAX_VALUE
        }
    }

    private fun normalizeStatus(status: String?): String = status?.trim()?.lowercase(Locale.US) ?: ""
    private fun isCancelledStatus(status: String) = status.contains("cancel") || status.contains("reject") || status.contains("declin")
    private fun isPastStatus(status: String) = status.contains("done") || status.contains("complete")
    private fun safe(value: String?, fallback: String = ""): String = if (value.isNullOrBlank()) fallback else value
}
