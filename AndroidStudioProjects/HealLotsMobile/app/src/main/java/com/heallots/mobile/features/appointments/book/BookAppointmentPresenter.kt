package com.heallots.mobile.features.appointments.book

import com.heallots.mobile.models.BookAppointmentRequest

class BookAppointmentPresenter(
    private var view: BookAppointmentContract.View?,
    private val repository: BookAppointmentRepository
) : BookAppointmentContract.Presenter {
    override fun bookAppointment(request: BookAppointmentRequest) {
        val reason = request.reason.orEmpty()
        val serviceName = request.serviceName.orEmpty()
        val specialistName = request.specialistName.orEmpty()
        val appointmentDate = request.appointmentDate.orEmpty()
        val timeSlot = request.timeSlot.orEmpty()
        when {
            reason.isBlank() -> view?.showMessage("Please select a reason for visit")
            serviceName.isBlank() || specialistName.isBlank() ||
                appointmentDate.isBlank() || timeSlot.isBlank() ->
                view?.showMessage("Please complete your appointment details")
            else -> {
                view?.showBookingLoading()
                repository.bookAppointment(
                    request,
                    onSuccess = {
                        view?.hideBookingLoading()
                        view?.showMessage("Appointment booked successfully!")
                        view?.navigateToAppointments()
                    },
                    onError = { message ->
                        view?.hideBookingLoading()
                        view?.showMessage(message)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        view = null
    }
}
