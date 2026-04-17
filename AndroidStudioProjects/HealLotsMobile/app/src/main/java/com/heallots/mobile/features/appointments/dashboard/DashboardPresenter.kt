package com.heallots.mobile.features.appointments.dashboard

import com.heallots.mobile.models.Appointment
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardPresenter(
    private var view: DashboardContract.View?,
    private val repository: DashboardRepository
) : DashboardContract.Presenter {
    override fun loadDashboard() {
        view?.renderUser(repository.getCurrentUser())
        repository.loadRecentAppointments { appointments ->
            val upcoming = appointments
                .sortedBy { toTimestamp(it) }
                .filterNot { appointment ->
                    val status = safeText(appointment.status).lowercase(Locale.US)
                    status.contains("cancel") || status.contains("done") || status.contains("complete")
                }
                .take(3)
            view?.renderAppointments(upcoming)
        }
        repository.loadReviews { reviews ->
            view?.renderReviews(reviews)
        }
    }

    override fun onDestroy() {
        view = null
    }

    private fun toTimestamp(appointment: Appointment): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                .parse("${safeText(appointment.appointmentDate)} ${safeText(appointment.timeSlot)}")
                ?.time ?: Long.MAX_VALUE
        } catch (_: ParseException) {
            Long.MAX_VALUE
        } catch (_: NullPointerException) {
            Long.MAX_VALUE
        }
    }

    private fun safeText(value: String?): String = value?.takeUnless { it.isBlank() } ?: ""
}
