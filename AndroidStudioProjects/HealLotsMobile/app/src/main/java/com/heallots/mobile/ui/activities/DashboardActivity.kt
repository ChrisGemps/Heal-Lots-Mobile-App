package com.heallots.mobile.ui.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.api.ApiClient
import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.Review
import com.heallots.mobile.models.User
import com.heallots.mobile.storage.TokenManager
import com.heallots.mobile.ui.adapters.DashboardAppointmentAdapter
import com.heallots.mobile.ui.adapters.ReviewAdapter
import com.heallots.mobile.utils.Constants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback as RetrofitCallback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService
    private lateinit var bookCard: LinearLayout
    private lateinit var appointmentsCard: LinearLayout
    private lateinit var userBadge: LinearLayout
    private lateinit var userNameText: TextView
    private lateinit var userRoleText: TextView
    private lateinit var greetingText: TextView
    private lateinit var userAvatar: ImageView
    private lateinit var avatarText: TextView
    private lateinit var adminBtn: Button
    private lateinit var viewAllAppointmentsBtn: Button
    private lateinit var appointmentsRecycler: RecyclerView
    private lateinit var appointmentsEmpty: LinearLayout
    private lateinit var avgRatingText: TextView
    private lateinit var totalReviewsText: TextView
    private lateinit var reviewsTabs: LinearLayout
    private lateinit var reviewsRecycler: RecyclerView
    private lateinit var reviewsEmpty: LinearLayout

    private lateinit var appointmentAdapter: DashboardAppointmentAdapter
    private lateinit var reviewAdapter: ReviewAdapter
    private val recentAppointments = ArrayList<Appointment>()
    private val allReviews = ArrayList<Review>()
    private val filteredReviews = ArrayList<Review>()
    private var selectedReviewTab = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_dashboard)
            tokenManager = TokenManager(this)
            apiService = ApiClient.getApiService()
            initializeViews()
            setupRecyclerViews()
            setUserInfo()
            setupListeners()
            loadDashboardData()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::apiService.isInitialized) {
            setUserInfo()
            loadDashboardData()
        }
    }

    private fun initializeViews() {
        bookCard = findViewById(R.id.dashboardBookCard)
        appointmentsCard = findViewById(R.id.dashboardAppointmentsCard)
        userBadge = findViewById(R.id.dashboardUserBadge)
        userNameText = findViewById(R.id.dashboardUserName)
        userRoleText = findViewById(R.id.dashboardUserRole)
        greetingText = findViewById(R.id.dashboardGreeting)
        userAvatar = findViewById(R.id.dashboardUserAvatar)
        avatarText = findViewById(R.id.dashboardUserAvatarText)
        adminBtn = findViewById(R.id.dashboardAdminBtn)
        viewAllAppointmentsBtn = findViewById(R.id.dashboardViewAllAppointments)
        appointmentsRecycler = findViewById(R.id.dashboardAppointmentsRecycler)
        appointmentsEmpty = findViewById(R.id.dashboardAppointmentsEmpty)
        avgRatingText = findViewById(R.id.dashboardAvgRating)
        totalReviewsText = findViewById(R.id.dashboardTotalReviews)
        reviewsTabs = findViewById(R.id.dashboardReviewsTabs)
        reviewsRecycler = findViewById(R.id.dashboardReviewsRecycler)
        reviewsEmpty = findViewById(R.id.dashboardReviewsEmpty)
    }

    private fun setupRecyclerViews() {
        appointmentsRecycler.layoutManager = LinearLayoutManager(this)
        appointmentAdapter = DashboardAppointmentAdapter(
            this,
            recentAppointments,
            object : DashboardAppointmentAdapter.OnAppointmentClickListener {
                override fun onAppointmentClick(appointment: Appointment) {
                    startActivity(Intent(this@DashboardActivity, MyAppointmentsActivity::class.java))
                }
            }
        )
        appointmentsRecycler.adapter = appointmentAdapter

        reviewsRecycler.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(this, filteredReviews)
        reviewsRecycler.adapter = reviewAdapter
    }

    private fun setUserInfo() {
        val currentUser: User? = tokenManager.getUser()
        val fullName = currentUser?.fullName?.takeUnless { it.isBlank() } ?: "User"
        val role = currentUser?.role?.takeUnless { it.isBlank() } ?: "USER"
        val firstName = getFirstName(fullName)

        greetingText.text = "Welcome back, $firstName"
        userNameText.text = firstName
        userRoleText.text = if (role.equals("ADMIN", ignoreCase = true)) "Admin" else "Patient"
        avatarText.text = fullName.substring(0, 1).uppercase(Locale.US)
        adminBtn.visibility = if (role.equals("ADMIN", ignoreCase = true)) View.VISIBLE else View.GONE
        adminBtn.background = makeTopBarOutline()

        val profilePictureUrl = currentUser?.profilePictureUrl
        if (!profilePictureUrl.isNullOrBlank()) {
            val imageUrl = if (profilePictureUrl.startsWith("http")) {
                profilePictureUrl
            } else {
                "${Constants.BASE_URL}/api/user/profile-picture/$profilePictureUrl"
            }
            avatarText.visibility = View.VISIBLE
            Picasso.get().load(imageUrl).fit().centerCrop().into(userAvatar, object : Callback {
                override fun onSuccess() {
                    avatarText.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                    userAvatar.setImageDrawable(null)
                    avatarText.visibility = View.VISIBLE
                }
            })
        } else {
            userAvatar.setImageDrawable(null)
            avatarText.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        bookCard.setOnClickListener { startActivity(Intent(this, BookAppointmentActivity::class.java)) }
        appointmentsCard.setOnClickListener { startActivity(Intent(this, MyAppointmentsActivity::class.java)) }
        viewAllAppointmentsBtn.setOnClickListener { startActivity(Intent(this, MyAppointmentsActivity::class.java)) }
        userBadge.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        adminBtn.setOnClickListener { startActivity(Intent(this, AdminDashboardActivity::class.java)) }
    }

    private fun loadDashboardData() {
        loadRecentAppointments()
        loadReviews()
    }

    private fun loadRecentAppointments() {
        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            renderAppointments(ArrayList())
            return
        }

        apiService.getUserAppointments(authHeader).enqueue(object : RetrofitCallback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    renderAppointments(ArrayList())
                    return
                }

                val appointments = ArrayList(body)
                appointments.sortBy { toTimestamp(it) }
                val upcoming = ArrayList<Appointment>()
                for (appointment in appointments) {
                    val status = safeText(appointment.status, "").lowercase(Locale.US)
                    if (!status.contains("cancel") && !status.contains("done") && !status.contains("complete")) {
                        upcoming.add(appointment)
                    }
                    if (upcoming.size == 3) {
                        break
                    }
                }
                renderAppointments(upcoming)
            }

            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) {
                Log.e(TAG, "Failed to load dashboard appointments", t)
                renderAppointments(ArrayList())
            }
        })
    }

    private fun renderAppointments(appointments: List<Appointment>) {
        recentAppointments.clear()
        recentAppointments.addAll(appointments)
        appointmentAdapter.updateAppointments(recentAppointments)
        val empty = recentAppointments.isEmpty()
        appointmentsEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        appointmentsRecycler.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun loadReviews() {
        apiService.getAllReviews().enqueue(object : RetrofitCallback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    renderReviews(ArrayList())
                    return
                }
                renderReviews(ArrayList(body))
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                Log.e(TAG, "Failed to load reviews", t)
                renderReviews(ArrayList())
            }
        })
    }

    private fun renderReviews(reviews: List<Review>) {
        allReviews.clear()
        allReviews.addAll(reviews.sortedByDescending { getReviewTimestamp(it) })

        totalReviewsText.text = allReviews.size.toString()
        avgRatingText.text = String.format(Locale.US, "%.1f", calculateAverageRating(allReviews))

        buildReviewTabs()
        filterReviews(selectedReviewTab)
    }

    private fun buildReviewTabs() {
        reviewsTabs.removeAllViews()
        val counts = LinkedHashMap<String, Int>()
        counts["All"] = allReviews.size
        for (review in allReviews) {
            val key = safeText(review.serviceName, "Other")
            counts[key] = counts.getOrDefault(key, 0) + 1
        }

        for ((key, value) in counts) {
            val tab = Button(this).apply {
                isAllCaps = false
                minHeight = 0
                minimumHeight = 0
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(dp(14), dp(8), dp(14), dp(8))
                text = "$key ($value)"
                textSize = 12f
                setTextColor(
                    if (key == selectedReviewTab) getColor(R.color.primary_orange)
                    else getColor(R.color.text_tertiary)
                )
                setOnClickListener {
                    selectedReviewTab = key
                    buildReviewTabs()
                    filterReviews(selectedReviewTab)
                }
            }
            reviewsTabs.addView(tab)
        }
    }

    private fun filterReviews(tab: String) {
        filteredReviews.clear()
        if (tab == "All") {
            filteredReviews.addAll(allReviews)
        } else {
            for (review in allReviews) {
                if (tab == safeText(review.serviceName, "Other")) {
                    filteredReviews.add(review)
                }
            }
        }

        reviewAdapter.updateReviews(filteredReviews)
        val empty = filteredReviews.isEmpty()
        reviewsEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        reviewsRecycler.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun calculateAverageRating(reviews: List<Review>): Double {
        if (reviews.isEmpty()) {
            return 0.0
        }

        var total = 0
        for (review in reviews) {
            total += review.rating.coerceAtLeast(0)
        }
        return total.toDouble() / reviews.size
    }

    private fun toTimestamp(appointment: Appointment): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                .parse("${safeText(appointment.appointmentDate, "")} ${safeText(appointment.timeSlot, "")}")
                ?.time ?: Long.MAX_VALUE
        } catch (_: ParseException) {
            Long.MAX_VALUE
        } catch (_: NullPointerException) {
            Long.MAX_VALUE
        }
    }

    private fun dp(value: Int): Int {
        return Math.round(value * resources.displayMetrics.density)
    }

    private fun safeText(value: String?, fallback: String): String {
        return if (value.isNullOrBlank()) fallback else value
    }

    private fun getFirstName(fullName: String): String {
        val value = safeText(fullName, "User").trim()
        val firstSpace = value.indexOf(' ')
        return if (firstSpace > 0) value.substring(0, firstSpace) else value
    }

    private fun getReviewTimestamp(review: Review?): Long {
        val createdAt = review?.createdAt?.let { safeText(it, "") } ?: ""
        if (createdAt.isEmpty()) {
            return Long.MIN_VALUE
        }

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        )

        for (format in formats) {
            try {
                val parsed: Date? = format.parse(createdAt)
                if (parsed != null) {
                    return parsed.time
                }
            } catch (_: ParseException) {
            }
        }

        return Long.MIN_VALUE
    }

    private fun makeTopBarOutline(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            cornerRadius = dp(999).toFloat()
            setStroke(dp(1), Color.parseColor("#FBBF24"))
        }
    }

    companion object {
        private const val TAG = "DashboardActivity"
    }
}
