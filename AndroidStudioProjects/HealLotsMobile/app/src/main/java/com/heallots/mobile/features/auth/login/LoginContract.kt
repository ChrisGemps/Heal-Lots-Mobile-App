package com.heallots.mobile.features.auth.login

interface LoginContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun hideError()
        fun navigateToRegister()
        fun navigateToDashboard(isAdmin: Boolean)
    }

    interface Presenter {
        fun onLoginClicked(email: String, password: String)
        fun onRegisterClicked()
        fun onDestroy()
    }
}
