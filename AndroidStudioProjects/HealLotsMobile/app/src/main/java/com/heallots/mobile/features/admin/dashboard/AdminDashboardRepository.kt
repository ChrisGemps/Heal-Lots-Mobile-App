package com.heallots.mobile.features.admin.dashboard

import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.User
import com.heallots.mobile.storage.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminDashboardRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun getCurrentUser(): User? = tokenManager.getUser()

    fun loadAppointments(onResult: (List<Appointment>) -> Unit) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onResult(emptyList())
            return
        }
        apiService.getAllAppointments(authHeader).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                onResult(if (response.isSuccessful) response.body().orEmpty() else emptyList())
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                onResult(emptyList())
            }
        })
    }

    fun loadUsers(onResult: (List<User>) -> Unit) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onResult(emptyList())
            return
        }
        apiService.getAllUsers(authHeader).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                onResult(if (response.isSuccessful) response.body().orEmpty() else emptyList())
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                onResult(emptyList())
            }
        })
    }

    fun updateAppointmentStatus(
        appointmentId: String,
        status: String,
        onSuccess: (Appointment) -> Unit
    ) {
        val authHeader = tokenManager.getAuthorizationHeader() ?: return
        apiService.updateAppointmentStatus(authHeader, appointmentId, hashMapOf("status" to status))
            .enqueue(object : Callback<Appointment> {
                override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                    response.body()?.let(onSuccess)
                }

                override fun onFailure(call: Call<Appointment>, t: Throwable) = Unit
            })
    }

    fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTimeSlot: String,
        onSuccess: (Appointment) -> Unit
    ) {
        val authHeader = tokenManager.getAuthorizationHeader() ?: return
        apiService.updateAppointment(
            authHeader,
            appointmentId,
            hashMapOf("appointmentDate" to newDate, "timeSlot" to newTimeSlot, "status" to "Approved")
        ).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                response.body()?.let(onSuccess)
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) = Unit
        })
    }
}
