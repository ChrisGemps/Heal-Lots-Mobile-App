package com.heallots.mobile.features.appointments.dashboard

import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.Review
import com.heallots.mobile.models.User
import com.heallots.mobile.storage.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    fun getCurrentUser(): User? = tokenManager.getUser()

    fun loadRecentAppointments(onResult: (List<Appointment>) -> Unit) {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            onResult(emptyList())
            return
        }

        apiService.getUserAppointments(authHeader).enqueue(object : Callback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                onResult(if (response.isSuccessful) response.body().orEmpty() else emptyList())
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                onResult(emptyList())
            }
        })
    }

    fun loadReviews(onResult: (List<Review>) -> Unit) {
        apiService.getAllReviews().enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                onResult(if (response.isSuccessful) response.body().orEmpty() else emptyList())
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                onResult(emptyList())
            }
        })
    }
}
