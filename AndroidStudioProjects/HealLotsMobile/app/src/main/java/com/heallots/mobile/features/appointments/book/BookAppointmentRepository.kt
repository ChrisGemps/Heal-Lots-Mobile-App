package com.heallots.mobile.features.appointments.book

import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.BookAppointmentRequest
import com.heallots.mobile.storage.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookAppointmentRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun bookAppointment(
        request: BookAppointmentRequest,
        onSuccess: (Appointment) -> Unit,
        onError: (String) -> Unit
    ) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onError("Please sign in again before booking")
            return
        }

        apiService.bookAppointment(authHeader, request).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    onSuccess(body)
                } else {
                    onError("Unable to book appointment right now")
                }
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                onError("Unable to connect to the booking service")
            }
        })
    }
}
