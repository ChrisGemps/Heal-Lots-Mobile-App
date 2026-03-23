package com.heallots.mobile.utils;

public class Constants {
    // Change this to your backend URL (use 10.0.2.2 for Android emulator)
    public static final String BASE_URL = "http://10.0.2.2:8080"; // Emulator uses 10.0.2.2 instead of localhost
    // For physical device, change to your machine's IP: http://192.168.x.x:8080

    // API Endpoints
    public static final String AUTH_REGISTER = "/api/auth/register";
    public static final String AUTH_LOGIN = "/api/auth/login";
    public static final String APPOINTMENTS_BOOK = "/api/appointments/book";
    public static final String APPOINTMENTS_ALL = "/api/appointments/all";
    public static final String APPOINTMENTS_USER = "/api/appointments/user";
    public static final String APPOINTMENTS_UPDATE_STATUS = "/api/appointments/{id}/status";
    public static final String APPOINTMENTS_UPDATE = "/api/appointments/{id}";
    public static final String REVIEWS_SUBMIT = "/api/reviews";
    public static final String REVIEWS_SPECIALIST_RATINGS = "/api/reviews/specialist-ratings";
    public static final String REVIEWS_CHECK_APPOINTMENT = "/api/reviews/appointment/{appointmentId}/reviewed";
    public static final String USER_PROFILE = "/api/user/profile";
    public static final String USER_CHANGE_PASSWORD = "/api/user/change-password";
    public static final String USER_UPLOAD_PROFILE_PIC = "/api/user/upload-profile-picture";
    public static final String USER_PROFILE_PICTURE = "/api/user/profile-picture/{filename}";
}
