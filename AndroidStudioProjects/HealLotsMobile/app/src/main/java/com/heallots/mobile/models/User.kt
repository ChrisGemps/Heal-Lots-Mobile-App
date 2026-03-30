package com.heallots.mobile.models

class User() {
    var id: String? = null
    var email: String? = null
    var fullName: String? = null
    var birthday: String? = null
    var gender: String? = null
    var address: String? = null
    var role: String? = null
    var profilePictureUrl: String? = null
    var createdAt: String? = null

    private var phone: String? = null
    private var phoneNumber: String? = null

    constructor(email: String?, fullName: String?, role: String?) : this() {
        this.email = email
        this.fullName = fullName
        this.role = role
    }

    fun getPhone(): String? = phone ?: phoneNumber

    fun setPhone(phone: String?) {
        this.phone = phone
        this.phoneNumber = phone
    }

    fun getPhoneNumber(): String? = phoneNumber ?: phone

    fun setPhoneNumber(phoneNumber: String?) {
        this.phoneNumber = phoneNumber
        this.phone = phoneNumber
    }
}
