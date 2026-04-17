package com.heallots.mobile.features.profile

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.heallots.mobile.R
import com.heallots.mobile.api.ApiClient
import com.heallots.mobile.features.auth.login.LoginActivity
import com.heallots.mobile.models.User
import com.heallots.mobile.utils.Constants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity(), ProfileContract.View {
    private val photoPickerLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.GetContent(),
        ::handleSelectedPhoto
    )

    private lateinit var presenter: ProfileContract.Presenter
    private var currentUser: User? = null

    private lateinit var backBtn: Button
    private lateinit var signOutBtn: Button
    private lateinit var editBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var changePasswordBtn: Button
    private lateinit var updatePasswordBtn: Button
    private lateinit var cancelPasswordBtn: Button
    private lateinit var photoImage: ImageView
    private lateinit var photoInitial: TextView
    private lateinit var changePhotoBtn: Button
    private lateinit var photoError: TextView
    private lateinit var profileNameDisplay: TextView
    private lateinit var fullNameView: TextView
    private lateinit var emailView: TextView
    private lateinit var phoneView: TextView
    private lateinit var birthdayView: TextView
    private lateinit var addressView: TextView
    private lateinit var genderView: TextView
    private lateinit var fullNameEdit: EditText
    private lateinit var emailEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var birthdayEdit: EditText
    private lateinit var addressEdit: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var passwordViewState: LinearLayout
    private lateinit var passwordEditState: LinearLayout
    private lateinit var currentPasswordEdit: EditText
    private lateinit var newPasswordEdit: EditText
    private lateinit var confirmPasswordEdit: EditText
    private lateinit var currentPasswordToggle: ImageView
    private lateinit var newPasswordToggle: ImageView
    private lateinit var confirmPasswordToggle: ImageView
    private lateinit var passwordStrengthBar: ProgressBar
    private lateinit var passwordStrengthText: TextView
    private lateinit var passwordMatchText: TextView
    private lateinit var successBanner: FrameLayout
    private lateinit var errorBanner: FrameLayout

    private var isEditingProfile = false
    private var isEditingPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_profile)
            presenter = ProfilePresenter(
                view = this,
                repository = ProfileRepository(
                    context = this,
                    apiService = ApiClient.getApiService(),
                    tokenManager = com.heallots.mobile.storage.TokenManager(this)
                )
            )

            initializeViews()
            setupGenderSpinner()
            presenter.loadUser()
            setupListeners()
            setupPasswordListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.loadUser()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun initializeViews() {
        backBtn = findViewById(R.id.profileBackBtn)
        signOutBtn = findViewById(R.id.profileSignOutBtn)
        editBtn = findViewById(R.id.profileEditBtn)
        saveBtn = findViewById(R.id.profileSaveBtn)
        changePasswordBtn = findViewById(R.id.profileChangePasswordBtn)
        updatePasswordBtn = findViewById(R.id.profileUpdatePasswordBtn)
        cancelPasswordBtn = findViewById(R.id.profileCancelPasswordBtn)
        photoImage = findViewById(R.id.profilePhotoImage)
        photoInitial = findViewById(R.id.profilePhotoInitial)
        changePhotoBtn = findViewById(R.id.profileChangePhotoBtn)
        photoError = findViewById(R.id.profilePhotoError)
        profileNameDisplay = findViewById(R.id.profileNameDisplay)
        fullNameView = findViewById(R.id.profileFullNameView)
        emailView = findViewById(R.id.profileEmailView)
        phoneView = findViewById(R.id.profilePhoneView)
        birthdayView = findViewById(R.id.profileBirthdayView)
        addressView = findViewById(R.id.profileAddressView)
        genderView = findViewById(R.id.profileGenderView)
        fullNameEdit = findViewById(R.id.profileFullNameEdit)
        emailEdit = findViewById(R.id.profileEmailEdit)
        phoneEdit = findViewById(R.id.profilePhoneEdit)
        birthdayEdit = findViewById(R.id.profileBirthdayEdit)
        addressEdit = findViewById(R.id.profileAddressEdit)
        genderSpinner = findViewById(R.id.profileGenderEdit)
        passwordViewState = findViewById(R.id.passwordViewState)
        passwordEditState = findViewById(R.id.passwordEditState)
        currentPasswordEdit = findViewById(R.id.profileCurrentPasswordEdit)
        newPasswordEdit = findViewById(R.id.profileNewPasswordEdit)
        confirmPasswordEdit = findViewById(R.id.profileConfirmPasswordEdit)
        currentPasswordToggle = findViewById(R.id.profileCurrentPasswordToggle)
        newPasswordToggle = findViewById(R.id.profileNewPasswordToggle)
        confirmPasswordToggle = findViewById(R.id.profileConfirmPasswordToggle)
        passwordStrengthBar = findViewById(R.id.profilePasswordStrengthBar)
        passwordStrengthText = findViewById(R.id.profilePasswordStrengthText)
        passwordMatchText = findViewById(R.id.profilePasswordMatchText)
        successBanner = findViewById(R.id.profileSuccessBanner)
        errorBanner = findViewById(R.id.profileErrorBanner)
    }

    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Male", "Female", "Other", "Prefer not to say")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }

    private fun bindUserInfo() {
        if (currentUser == null) {
            currentUser = User().apply {
                fullName = readPref("fullName", "User")
                email = readPref("email", "")
                setPhone(readPref("phone", ""))
                birthday = readPref("birthday", "")
                address = readPref("address", "")
                gender = readPref("gender", "")
                profilePictureUrl = readPref("profilePictureUrl", "")
                role = readPref("role", "USER")
            }
        }

        val user = currentUser ?: return
        val fullName = safeText(user.fullName, "User")
        profileNameDisplay.text = fullName
        fullNameView.text = fullName
        emailView.text = safeText(user.email, "")
        phoneView.text = formatPhoneForDisplay(user.getPhone())
        birthdayView.text = formatBirthdayForDisplay(user.birthday)
        addressView.text = safeText(user.address, "")
        genderView.text = safeText(user.gender, "")

        fullNameEdit.setText(fullName)
        emailEdit.setText(safeText(user.email, ""))
        phoneEdit.setText(formatPhoneForDisplay(user.getPhone()))
        birthdayEdit.setText(formatBirthdayForDisplay(user.birthday))
        addressEdit.setText(safeText(user.address, ""))
        setSpinnerValue(genderSpinner, safeText(user.gender, "Prefer not to say"))

        photoInitial.text = fullName.substring(0, 1).uppercase(Locale.US)
        loadProfilePhoto()
    }

    override fun renderUser(user: User?) {
        currentUser = user
        bindUserInfo()
    }

    private fun setupListeners() {
        backBtn.setOnClickListener { finish() }
        signOutBtn.setOnClickListener { presenter.logout() }
        editBtn.setOnClickListener { toggleEditMode() }
        saveBtn.setOnClickListener { saveProfileChanges() }
        changePhotoBtn.setOnClickListener { photoPickerLauncher.launch("image/*") }
        changePasswordBtn.setOnClickListener { togglePasswordEdit() }
        updatePasswordBtn.setOnClickListener { updatePassword() }
        cancelPasswordBtn.setOnClickListener { cancelPasswordEdit() }
    }

    private fun setupPasswordListeners() {
        currentPasswordToggle.setOnClickListener { togglePasswordVisibility(currentPasswordEdit, currentPasswordToggle) }
        newPasswordToggle.setOnClickListener { togglePasswordVisibility(newPasswordEdit, newPasswordToggle) }
        confirmPasswordToggle.setOnClickListener { togglePasswordVisibility(confirmPasswordEdit, confirmPasswordToggle) }

        newPasswordEdit.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updatePasswordStrength(s.toString())
                checkPasswordMatch()
            }
        })
        confirmPasswordEdit.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                checkPasswordMatch()
            }
        })
    }

    private fun toggleEditMode() {
        isEditingProfile = !isEditingProfile
        val viewVisibility = if (isEditingProfile) View.GONE else View.VISIBLE
        val editVisibility = if (isEditingProfile) View.VISIBLE else View.GONE

        fullNameView.visibility = viewVisibility
        emailView.visibility = viewVisibility
        phoneView.visibility = viewVisibility
        birthdayView.visibility = viewVisibility
        addressView.visibility = viewVisibility
        genderView.visibility = viewVisibility

        fullNameEdit.visibility = editVisibility
        emailEdit.visibility = editVisibility
        phoneEdit.visibility = editVisibility
        birthdayEdit.visibility = editVisibility
        addressEdit.visibility = editVisibility
        genderSpinner.visibility = editVisibility

        saveBtn.visibility = if (isEditingProfile) View.VISIBLE else View.GONE
        editBtn.text = if (isEditingProfile) "Cancel" else "Edit"

        if (!isEditingProfile) bindUserInfo()
    }

    private fun saveProfileChanges() {
        val fullName = fullNameEdit.text.toString().trim()
        val email = emailEdit.text.toString().trim()
        val phone = normalizePhoneForApi(phoneEdit.text.toString().trim())
        val birthday = normalizeBirthdayForApi(birthdayEdit.text.toString().trim())
        val address = addressEdit.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()

        val requestUser = User().apply {
            this.fullName = fullName
            this.email = email
            setPhone(phone)
            this.birthday = birthday
            this.address = address
            this.gender = gender
            profilePictureUrl = currentUser?.profilePictureUrl
        }
        saveBtn.isEnabled = false
        presenter.saveProfile(requestUser)
    }

    private fun togglePasswordEdit() {
        isEditingPassword = !isEditingPassword
        passwordViewState.visibility = if (isEditingPassword) View.GONE else View.VISIBLE
        passwordEditState.visibility = if (isEditingPassword) View.VISIBLE else View.GONE
        clearPasswordFields()
    }

    private fun updatePassword() {
        val currentPassword = currentPasswordEdit.text.toString()
        val newPassword = newPasswordEdit.text.toString()
        val confirmPassword = confirmPasswordEdit.text.toString()
        updatePasswordBtn.isEnabled = false
        presenter.updatePassword(currentPassword, newPassword, confirmPassword)
    }

    private fun cancelPasswordEdit() {
        clearPasswordFields()
        isEditingPassword = false
        passwordViewState.visibility = View.VISIBLE
        passwordEditState.visibility = View.GONE
    }

    private fun handleSelectedPhoto(uri: Uri?) {
        if (uri == null) return
        presenter.uploadPhoto(uri)
    }

    private fun loadProfilePhoto() {
        photoError.visibility = View.GONE
        val profilePictureUrl = currentUser?.profilePictureUrl
        if (profilePictureUrl.isNullOrBlank()) {
            photoImage.setImageDrawable(null)
            photoImage.visibility = View.VISIBLE
            photoInitial.visibility = View.VISIBLE
            return
        }

        photoImage.visibility = View.VISIBLE
        photoInitial.visibility = View.VISIBLE
        val imageUrl = if (profilePictureUrl.startsWith("http")) profilePictureUrl else "${Constants.BASE_URL}/api/user/profile-picture/$profilePictureUrl"
        Picasso.get().load(imageUrl).fit().centerCrop().into(photoImage, object : Callback {
            override fun onSuccess() {
                photoInitial.visibility = View.GONE
                photoError.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                photoImage.setImageDrawable(null)
                photoInitial.visibility = View.VISIBLE
                photoError.visibility = View.VISIBLE
                photoError.text = "Unable to load profile photo"
            }
        })
    }

    private fun clearPasswordFields() {
        currentPasswordEdit.setText("")
        newPasswordEdit.setText("")
        confirmPasswordEdit.setText("")
        passwordStrengthBar.progress = 0
        passwordStrengthText.text = ""
        passwordMatchText.text = ""
    }

    private fun updatePasswordStrength(password: String) {
        var strength = 0
        var strengthTextValue = "Weak"
        var color = Color.RED
        if (password.length >= 8) strength++
        if (password.matches(Regex(".*[A-Z].*"))) strength++
        if (password.matches(Regex(".*[a-z].*"))) strength++
        if (password.matches(Regex(".*[0-9].*"))) strength++
        if (password.matches(Regex(".*[^A-Za-z0-9].*"))) strength++
        when (strength) {
            2 -> {
                strengthTextValue = "Fair"
                color = Color.parseColor("#FF9800")
            }
            3 -> {
                strengthTextValue = "Good"
                color = Color.parseColor("#FFC107")
            }
            4 -> {
                strengthTextValue = "Strong"
                color = Color.parseColor("#4CAF50")
            }
            5 -> {
                strengthTextValue = "Very Strong"
                color = Color.parseColor("#2196F3")
            }
        }
        passwordStrengthBar.progress = strength
        passwordStrengthText.text = strengthTextValue
        passwordStrengthText.setTextColor(color)
    }

    private fun checkPasswordMatch() {
        val newPass = newPasswordEdit.text.toString()
        val confirmPass = confirmPasswordEdit.text.toString()
        if (confirmPass.isEmpty()) {
            passwordMatchText.text = ""
            return
        }
        if (newPass == confirmPass) {
            passwordMatchText.text = "Passwords match"
            passwordMatchText.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            passwordMatchText.text = "Passwords do not match"
            passwordMatchText.setTextColor(Color.RED)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView) {
        editText.transformationMethod =
            if (editText.transformationMethod is PasswordTransformationMethod) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
        toggleIcon.setImageResource(R.drawable.ic_placeholder)
        editText.setSelection(editText.text.length)
    }

    private fun setSpinnerValue(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as? ArrayAdapter<*> ?: return
        for (i in 0 until adapter.count) {
            if (value.equals(adapter.getItem(i).toString(), ignoreCase = true)) {
                spinner.setSelection(i)
                return
            }
        }
    }

    private fun readPref(key: String, fallback: String): String =
        getSharedPreferences("user_prefs", MODE_PRIVATE).getString(key, fallback) ?: fallback

    private fun safeText(value: String?, fallback: String): String = if (value.isNullOrBlank()) fallback else value

    private fun formatBirthdayForDisplay(rawBirthday: String?): String {
        val value = safeText(rawBirthday, "")
        if (value.isEmpty()) return ""
        return try {
            when {
                value.matches(Regex("\\d{8}")) -> {
                    val parsed = SimpleDateFormat("MMddyyyy", Locale.US).parse(value)
                    if (parsed != null) SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(parsed) else value
                }
                else -> {
                    val parsed = SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(value)
                    if (parsed != null) SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(parsed) else value
                }
            }
        } catch (_: ParseException) {
            value
        }
    }

    private fun normalizeBirthdayForApi(displayBirthday: String): String {
        val value = safeText(displayBirthday, "")
        if (value.isEmpty() || value.matches(Regex("\\d{8}"))) return value
        return try {
            val parsed: Date? = SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(value)
            if (parsed != null) SimpleDateFormat("MMddyyyy", Locale.US).format(parsed) else value
        } catch (_: ParseException) {
            value
        }
    }

    private fun formatPhoneForDisplay(rawPhone: String?): String {
        val value = safeText(rawPhone, "")
        if (value.isEmpty() || value.contains(" ")) return value
        val digits = value.replace(Regex("[^0-9]"), "")
        return if (digits.length == 11 && digits.startsWith("0")) {
            "${digits.substring(0, 4)} ${digits.substring(4, 7)} ${digits.substring(7)}"
        } else value
    }

    private fun normalizePhoneForApi(displayPhone: String): String = safeText(displayPhone, "").replace(Regex("\\s+"), "")

    override fun showSuccess(message: String) {
        saveBtn.isEnabled = true
        updatePasswordBtn.isEnabled = true
        if (isEditingProfile) toggleEditMode()
        if (isEditingPassword) cancelPasswordEdit()
        showPopupMessage(message)
    }
    override fun showError(message: String) {
        saveBtn.isEnabled = true
        updatePasswordBtn.isEnabled = true
        showPopupMessage(message)
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showPopupMessage(message: String) {
        successBanner.visibility = View.GONE
        errorBanner.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    private abstract class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
        override fun afterTextChanged(s: Editable) = Unit
    }

    companion object {
        private const val TAG = "ProfileActivity"
    }
}
