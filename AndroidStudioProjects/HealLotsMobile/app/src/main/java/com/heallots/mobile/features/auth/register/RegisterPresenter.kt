package com.heallots.mobile.features.auth.register

import com.heallots.mobile.models.RegisterRequest

class RegisterPresenter(
    private var view: RegisterContract.View?,
    private val repository: RegisterRepository
) : RegisterContract.Presenter {
    override fun onRegisterClicked(form: RegisterForm) {
        val normalizedPhone = form.phone.trim().replace(" ", "")
        val fullName = form.name.trim()
        val email = form.email.trim()
        val birthday = form.birthday
        val gender = form.gender
        val address = form.address.trim()
        val password = form.password.trim()
        val confirmPassword = form.confirmPassword.trim()
        val request = RegisterRequest(
            fullName,
            email,
            normalizedPhone,
            birthday,
            gender,
            address,
            password
        )

        when {
            fullName.isEmpty() -> view?.showError("Full name is required")
            fullName.length < 2 -> view?.showError("Name must be at least 2 characters")
            email.isEmpty() -> view?.showError("Email address is required")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                view?.showError("Invalid email format (e.g., maria@example.com)")
            normalizedPhone.isEmpty() || normalizedPhone.length < 10 ->
                view?.showError("Phone number must be at least 10 digits")
            birthday.isEmpty() || birthday.length != 8 ->
                view?.showError("Birthday must be complete (MM/DD/YYYY)")
            gender == "Select Gender" -> view?.showError("Please select a gender")
            address.isEmpty() -> view?.showError("Address is required")
            address.length < 5 -> view?.showError("Address must be at least 5 characters")
            password.isEmpty() -> view?.showError("Password is required")
            password.length < 6 -> view?.showError("Password must be at least 6 characters")
            confirmPassword.isEmpty() -> view?.showError("Please confirm your password")
            password != confirmPassword -> view?.showError("Passwords do not match")
            else -> {
                view?.hideError()
                view?.showLoading()
                repository.register(
                    request,
                    onSuccess = {
                        view?.hideLoading()
                        view?.navigateToDashboard()
                    },
                    onError = { message ->
                        view?.hideLoading()
                        view?.showError(message)
                    }
                )
            }
        }
    }

    override fun onLoginClicked() {
        view?.navigateToLogin()
    }

    override fun onDestroy() {
        view = null
    }
}
