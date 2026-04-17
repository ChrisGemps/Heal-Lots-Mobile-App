package com.heallots.mobile.features.auth.login

class LoginPresenter(
    private var view: LoginContract.View?,
    private val repository: LoginRepository
) : LoginContract.Presenter {
    override fun onLoginClicked(email: String, password: String) {
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()

        when {
            trimmedEmail.isEmpty() -> view?.showError("Please enter your email address.")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                view?.showError("Please enter a valid email address.")
            trimmedPassword.isEmpty() -> view?.showError("Please enter your password.")
            trimmedPassword.length < 6 -> view?.showError("Password must be at least 6 characters.")
            else -> {
                view?.hideError()
                view?.showLoading()
                repository.login(
                    email = trimmedEmail,
                    password = trimmedPassword,
                    onSuccess = { response ->
                        view?.hideLoading()
                        view?.navigateToDashboard(response.user?.role == "ADMIN")
                    },
                    onError = { message ->
                        view?.hideLoading()
                        view?.showError(message)
                    }
                )
            }
        }
    }

    override fun onRegisterClicked() {
        view?.navigateToRegister()
    }

    override fun onDestroy() {
        view = null
    }
}
