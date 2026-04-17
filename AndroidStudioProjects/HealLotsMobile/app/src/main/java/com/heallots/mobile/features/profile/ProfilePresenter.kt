package com.heallots.mobile.features.profile

import android.net.Uri
import com.heallots.mobile.models.User

class ProfilePresenter(
    private var view: ProfileContract.View?,
    private val repository: ProfileRepository
) : ProfileContract.Presenter {
    override fun loadUser() {
        view?.renderUser(repository.getCurrentUser())
    }

    override fun saveProfile(user: User) {
        if (user.fullName.isNullOrBlank() || user.email.isNullOrBlank()) {
            view?.showError("Full Name and Email are required")
            return
        }
        repository.updateProfile(
            user,
            onSuccess = {
                view?.renderUser(it)
                view?.showSuccess("Profile updated successfully!")
            },
            onError = { message -> view?.showError(message) }
        )
    }

    override fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        when {
            currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() ->
                view?.showError("All password fields are required")
            newPassword.length < 8 -> view?.showError("New password must be at least 8 characters")
            newPassword != confirmPassword -> view?.showError("Passwords do not match")
            else -> repository.changePassword(
                currentPassword = currentPassword,
                newPassword = newPassword,
                onSuccess = { view?.showSuccess("Password updated successfully!") },
                onError = { message -> view?.showError(message) }
            )
        }
    }

    override fun uploadPhoto(uri: Uri) {
        repository.uploadProfilePicture(
            uri,
            onSuccess = {
                view?.renderUser(it)
                view?.showSuccess("Profile picture updated!")
            },
            onError = { message -> view?.showError(message) }
        )
    }

    override fun logout() {
        repository.logout()
        view?.navigateToLogin()
    }

    override fun onDestroy() {
        view = null
    }
}
