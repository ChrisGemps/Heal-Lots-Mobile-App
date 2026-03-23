package com.heallots.mobile.api;

import com.heallots.mobile.models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.Map;

public interface ApiService {

    // ============ Authentication ============
    @POST("/api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // ============ Appointments ============
    @POST("/api/appointments/book")
    Call<Appointment> bookAppointment(@Header("Authorization") String token, @Body BookAppointmentRequest request);

    @GET("/api/appointments/all")
    Call<java.util.List<Appointment>> getAllAppointments(@Header("Authorization") String token);

    @GET("/api/appointments/user")
    Call<java.util.List<Appointment>> getUserAppointments(@Header("Authorization") String token);

    @PUT("/api/appointments/{id}/status")
    Call<Appointment> updateAppointmentStatus(
            @Header("Authorization") String token,
            @Path("id") String appointmentId,
            @Body Map<String, String> request
    );

    @PUT("/api/appointments/{id}")
    Call<Appointment> updateAppointment(
            @Header("Authorization") String token,
            @Path("id") String appointmentId,
            @Body Map<String, String> request
    );

    // ============ Reviews ============
    @POST("/api/reviews")
    Call<Map<String, String>> submitReview(@Header("Authorization") String token, @Body Review review);

    @GET("/api/reviews")
    Call<List<Review>> getAllReviews();

    @GET("/api/reviews/specialist-ratings")
    Call<Map<String, SpecialistRating>> getSpecialistRatings(@Header("Authorization") String token);

    @GET("/api/reviews/appointment/{appointmentId}/reviewed")
    Call<Map<String, Boolean>> checkAppointmentReviewed(
            @Header("Authorization") String token,
            @Path("appointmentId") String appointmentId
    );

    // ============ User Profile ============
    @PUT("/api/user/profile")
    Call<User> updateProfile(@Header("Authorization") String token, @Body User user);

    @PUT("/api/user/change-password")
    Call<Map<String, String>> changePassword(@Header("Authorization") String token, @Body Map<String, String> request);

    @GET("/api/user/all")
    Call<List<User>> getAllUsers(@Header("Authorization") String token);

    @Multipart
    @POST("/api/user/upload-profile-picture")
    Call<User> uploadProfilePicture(
            @Header("Authorization") String token,
            @Part okhttp3.MultipartBody.Part file
    );
}

// Model class for specialist ratings
class SpecialistRating {
    public double rating;
    public int reviews;
}
