package com.heallots.mobile.features.appointments.list

import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.Review
import com.heallots.mobile.models.User
import com.heallots.mobile.storage.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAppointmentsRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun getCurrentUser(): User? = tokenManager.getUser()

    fun loadAppointments(onSuccess: (List<Appointment>) -> Unit, onError: (String) -> Unit) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again to load your appointments.")
            return
        }

        apiService.getUserAppointments(authHeader).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    onSuccess(body)
                } else {
                    onError("We couldn't load your appointments right now.")
                }
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                onError("We couldn't reach the server for your appointments.")
            }
        })
    }

    fun checkAppointmentReviewed(appointmentId: String, onResult: (Boolean) -> Unit) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onResult(false)
            return
        }
        apiService.checkAppointmentReviewed(authHeader, appointmentId)
            .enqueue(object : Callback<Map<String, Boolean>> {
                override fun onResponse(
                    call: Call<Map<String, Boolean>>,
                    response: Response<Map<String, Boolean>>
                ) {
                    onResult(response.isSuccessful && response.body()?.get("reviewed") == true)
                }

                override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                    onResult(false)
                }
            })
    }

    fun cancelAppointment(
        appointmentId: String,
        reason: String,
        onSuccess: (Appointment) -> Unit,
        onError: (String) -> Unit
    ) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again to manage this appointment.")
            return
        }
        apiService.updateAppointmentStatus(
            authHeader,
            appointmentId,
            hashMapOf("status" to "Cancelled", "cancellationReason" to reason)
        ).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body)
                else onError("We couldn't cancel this appointment right now.")
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                onError("We couldn't cancel this appointment right now.")
            }
        })
    }

    fun submitReview(
        review: Review,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again to submit your review.")
            return
        }
        apiService.submitReview(authHeader, review).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) onSuccess() else onError("We couldn't submit your review right now.")
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                onError("We couldn't submit your review right now.")
            }
        })
    }

    fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTimeSlot: String,
        reason: String,
        onSuccess: (Appointment) -> Unit,
        onError: (String) -> Unit
    ) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again to reschedule this appointment.")
            return
        }
        apiService.updateAppointment(
            authHeader,
            appointmentId,
            hashMapOf(
                "appointmentDate" to newDate,
                "timeSlot" to newTimeSlot,
                "rescheduleReason" to reason,
                "status" to "Rescheduled"
            )
        ).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                val body = response.body()
                if (response.isSuccessful && body != null) onSuccess(body)
                else onError("We couldn't reschedule this appointment right now.")
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                onError("We couldn't reschedule this appointment right now.")
            }
        })
    }
}
