package com.heallots.mobile.features.auth.register

data class RegisterForm(
    val name: String,
    val email: String,
    val phone: String,
    val birthday: String,
    val gender: String,
    val address: String,
    val password: String,
    val confirmPassword: String
)
