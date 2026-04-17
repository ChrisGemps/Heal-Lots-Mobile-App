package com.heallots.mobile.features.auth.register

interface RegisterContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun hideError()
        fun navigateToLogin()
        fun navigateToDashboard()
    }

    interface Presenter {
        fun onRegisterClicked(form: RegisterForm)
        fun onLoginClicked()
        fun onDestroy()
    }
}
