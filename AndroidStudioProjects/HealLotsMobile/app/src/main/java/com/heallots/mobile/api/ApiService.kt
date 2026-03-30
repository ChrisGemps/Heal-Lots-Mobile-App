package com.heallots.mobile.api

import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.BookAppointmentRequest
import com.heallots.mobile.models.LoginRequest
import com.heallots.mobile.models.LoginResponse
import com.heallots.mobile.models.RegisterRequest
import com.heallots.mobile.models.Review
import com.heallots.mobile.models.User
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("/api/auth/register")
    fun register(@Body request: RegisterRequest): Call<LoginResponse>

    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/appointments/book")
    fun bookAppointment(
        @Header("Authorization") token: String,
        @Body request: BookAppointmentRequest
    ): Call<Appointment>

    @GET("/api/appointments/all")
    fun getAllAppointments(@Header("Authorization") token: String): Call<List<Appointment>>

    @GET("/api/appointments/user")
    fun getUserAppointments(@Header("Authorization") token: String): Call<List<Appointment>>

    @PUT("/api/appointments/{id}/status")
    fun updateAppointmentStatus(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String,
        @Body request: Map<String, String>
    ): Call<Appointment>

    @PUT("/api/appointments/{id}")
    fun updateAppointment(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String,
        @Body request: Map<String, String>
    ): Call<Appointment>

    @POST("/api/reviews")
    fun submitReview(
        @Header("Authorization") token: String,
        @Body review: Review
    ): Call<Map<String, String>>

    @GET("/api/reviews")
    fun getAllReviews(): Call<List<Review>>

    @GET("/api/reviews/specialist-ratings")
    fun getSpecialistRatings(@Header("Authorization") token: String): Call<Map<String, SpecialistRating>>

    @GET("/api/reviews/appointment/{appointmentId}/reviewed")
    fun checkAppointmentReviewed(
        @Header("Authorization") token: String,
        @Path("appointmentId") appointmentId: String
    ): Call<Map<String, Boolean>>

    @PUT("/api/user/profile")
    fun updateProfile(@Header("Authorization") token: String, @Body user: User): Call<User>

    @PUT("/api/user/change-password")
    fun changePassword(
        @Header("Authorization") token: String,
        @Body request: Map<String, String>
    ): Call<Map<String, String>>

    @GET("/api/user/all")
    fun getAllUsers(@Header("Authorization") token: String): Call<List<User>>

    @Multipart
    @POST("/api/user/upload-profile-picture")
    fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Call<User>
}

class SpecialistRating {
    var rating: Double = 0.0
    var reviews: Int = 0
}
