package com.heallots.mobile.features.profile

import android.net.Uri
import com.heallots.mobile.models.User

interface ProfileContract {
    interface View {
        fun renderUser(user: User?)
        fun showSuccess(message: String)
        fun showError(message: String)
        fun navigateToLogin()
    }

    interface Presenter {
        fun loadUser()
        fun saveProfile(user: User)
        fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String)
        fun uploadPhoto(uri: Uri)
        fun logout()
        fun onDestroy()
    }
}
