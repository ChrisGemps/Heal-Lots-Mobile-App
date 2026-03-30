package com.heallots.mobile.utils

object Constants {
    const val BASE_URL: String = "http://10.0.2.2:8080"

    const val AUTH_REGISTER: String = "/api/auth/register"
    const val AUTH_LOGIN: String = "/api/auth/login"
    const val APPOINTMENTS_BOOK: String = "/api/appointments/book"
    const val APPOINTMENTS_ALL: String = "/api/appointments/all"
    const val APPOINTMENTS_USER: String = "/api/appointments/user"
    const val APPOINTMENTS_UPDATE_STATUS: String = "/api/appointments/{id}/status"
    const val APPOINTMENTS_UPDATE: String = "/api/appointments/{id}"
    const val REVIEWS_SUBMIT: String = "/api/reviews"
    const val REVIEWS_SPECIALIST_RATINGS: String = "/api/reviews/specialist-ratings"
    const val REVIEWS_CHECK_APPOINTMENT: String = "/api/reviews/appointment/{appointmentId}/reviewed"
    const val USER_PROFILE: String = "/api/user/profile"
    const val USER_CHANGE_PASSWORD: String = "/api/user/change-password"
    const val USER_UPLOAD_PROFILE_PIC: String = "/api/user/upload-profile-picture"
    const val USER_PROFILE_PICTURE: String = "/api/user/profile-picture/{filename}"
}
