package com.heallots.mobile.features.appointments.list

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.api.ApiClient
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.features.appointments.book.BookAppointmentActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

class MyAppointmentsActivity : AppCompatActivity(), AppointmentAdapter.OnAppointmentActionListener, MyAppointmentsContract.View {
    private lateinit var backBtn: Button
    private lateinit var emptyActionBtn: Button
    private lateinit var upcomingTab: LinearLayout
    private lateinit var pastTab: LinearLayout
    private lateinit var cancelledTab: LinearLayout
    private lateinit var upcomingTabLabel: TextView
    private lateinit var pastTabLabel: TextView
    private lateinit var cancelledTabLabel: TextView
    private lateinit var upcomingTabBadge: TextView
    private lateinit var pastTabBadge: TextView
    private lateinit var cancelledTabBadge: TextView
    private lateinit var upcomingCountText: TextView
    private lateinit var pastCountText: TextView
    private lateinit var cancelledCountText: TextView
    private lateinit var emptyState: LinearLayout
    private lateinit var emptyTitle: TextView
    private lateinit var emptySubtitle: TextView
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var notificationBanner: LinearLayout
    private lateinit var notificationIcon: TextView
    private lateinit var notificationMessage: TextView
    private lateinit var notificationClose: TextView
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var presenter: MyAppointmentsContract.Presenter
    private var currentTab = "upcoming"
    private var allAppointments = ArrayList<Appointment>()
    private var upcomingAppointments = ArrayList<Appointment>()
    private var pastAppointments = ArrayList<Appointment>()
    private var cancelledAppointments = ArrayList<Appointment>()
    private var notificationDismissRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_my_appointments)
            presenter = MyAppointmentsPresenter(
                view = this,
                repository = MyAppointmentsRepository(
                    apiService = ApiClient.getApiService(),
                    tokenManager = com.heallots.mobile.storage.TokenManager(this)
                )
            )
            initializeViews()
            setupRecyclerView()
            presenter.loadAppointments()
            setupListeners()
            selectTab("upcoming")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::presenter.isInitialized) presenter.loadAppointments()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun initializeViews() {
        backBtn = findViewById(R.id.myApptsBackBtn)
        emptyActionBtn = findViewById(R.id.myApptsEmptyActionBtn)
        upcomingTab = findViewById(R.id.myApptsUpcomingTab)
        pastTab = findViewById(R.id.myAptsPastTab)
        cancelledTab = findViewById(R.id.myAptsCancelledTab)
        upcomingTabLabel = findViewById(R.id.myApptsUpcomingTabLabel)
        pastTabLabel = findViewById(R.id.myApptsPastTabLabel)
        cancelledTabLabel = findViewById(R.id.myApptsCancelledTabLabel)
        upcomingTabBadge = findViewById(R.id.myApptsUpcomingTabBadge)
        pastTabBadge = findViewById(R.id.myApptsPastTabBadge)
        cancelledTabBadge = findViewById(R.id.myApptsCancelledTabBadge)
        upcomingCountText = findViewById(R.id.myApptsUpcomingCount)
        pastCountText = findViewById(R.id.myApptsPastCount)
        cancelledCountText = findViewById(R.id.myApptsCancelledCount)
        emptyState = findViewById(R.id.myApptsEmptyState)
        emptyTitle = findViewById(R.id.myApptsEmptyTitle)
        emptySubtitle = findViewById(R.id.myApptsEmptySubtitle)
        appointmentsRecyclerView = findViewById(R.id.myApptsRecyclerView)
        notificationBanner = findViewById(R.id.myApptsNotification)
        notificationIcon = findViewById(R.id.myApptsNotificationIcon)
        notificationMessage = findViewById(R.id.myApptsNotificationMessage)
        notificationClose = findViewById(R.id.myApptsNotificationClose)
    }

    private fun setupRecyclerView() {
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentAdapter = AppointmentAdapter(this, ArrayList(), currentTab, this)
        appointmentsRecyclerView.adapter = appointmentAdapter
    }

    private fun setupListeners() {
        backBtn.setOnClickListener { finish() }
        emptyActionBtn.setOnClickListener { startActivity(Intent(this, BookAppointmentActivity::class.java)) }
        upcomingTab.setOnClickListener { selectTab("upcoming") }
        pastTab.setOnClickListener { selectTab("past") }
        cancelledTab.setOnClickListener { selectTab("cancelled") }
        notificationClose.setOnClickListener { hideNotification() }
        notificationBanner.setOnTouchListener { _, _ ->
            notificationDismissRunnable?.let { HANDLER.removeCallbacks(it); scheduleNotificationDismiss() }
            false
        }
    }

    private fun refreshCounts() {
        upcomingCountText.text = upcomingAppointments.size.toString()
        pastCountText.text = pastAppointments.size.toString()
        cancelledCountText.text = cancelledAppointments.size.toString()
        upcomingTabBadge.text = upcomingAppointments.size.toString()
        pastTabBadge.text = pastAppointments.size.toString()
        cancelledTabBadge.text = cancelledAppointments.size.toString()
    }

    private fun selectTab(tab: String) {
        currentTab = tab
        styleTab(upcomingTab, upcomingTabLabel, upcomingTabBadge, tab == "upcoming")
        styleTab(pastTab, pastTabLabel, pastTabBadge, tab == "past")
        styleTab(cancelledTab, cancelledTabLabel, cancelledTabBadge, tab == "cancelled")
        val currentAppointments = getCurrentTabAppointments()
        appointmentAdapter.updateAppointments(currentAppointments, currentTab)
        updateEmptyState(currentAppointments.isEmpty())
    }

    private fun getCurrentTabAppointments(): List<Appointment> = when (currentTab) {
        "past" -> pastAppointments
        "cancelled" -> cancelledAppointments
        else -> upcomingAppointments
    }

    override fun renderAppointments(upcoming: List<Appointment>, past: List<Appointment>, cancelled: List<Appointment>) {
        allAppointments = ArrayList(upcoming + past + cancelled)
        upcomingAppointments = ArrayList(upcoming)
        pastAppointments = ArrayList(past)
        cancelledAppointments = ArrayList(cancelled)
        refreshCounts()
        selectTab(currentTab)
    }

    private fun sortAppointments(appointments: MutableList<Appointment>, ascending: Boolean) {
        appointments.sortBy { getAppointmentTimestamp(it) }
        if (!ascending) appointments.reverse()
    }

    private fun getAppointmentTimestamp(appointment: Appointment?): Long {
        if (appointment == null) return Long.MAX_VALUE
        return try {
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                .parse("${safe(appointment.appointmentDate)} ${safe(appointment.timeSlot)}")?.time ?: Long.MAX_VALUE
        } catch (e: ParseException) {
            Log.w(TAG, "Failed to sort appointment date for ${appointment.id}", e)
            Long.MAX_VALUE
        }
    }

    private fun submitCancellation(appointment: Appointment, reason: String, dialog: AlertDialog) {
        presenter.cancelAppointment(appointment, reason)
        dialog.dismiss()
        selectTab("cancelled")
    }

    private fun submitReview(appointment: Appointment, rating: Int, feedback: String, dialog: AlertDialog) {
        presenter.submitReview(appointment, rating, feedback)
        dialog.dismiss()
    }

    private fun submitReschedule(appointment: Appointment, newDate: String, newTimeSlot: String, reason: String, dialog: AlertDialog) {
        presenter.rescheduleAppointment(appointment, newDate, newTimeSlot, reason)
        dialog.dismiss()
        selectTab("upcoming")
    }

    private fun updateEmptyState(empty: Boolean) {
        emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        appointmentsRecyclerView.visibility = if (empty) View.GONE else View.VISIBLE
        when (currentTab) {
            "upcoming" -> {
                emptyTitle.text = "No upcoming sessions"
                emptySubtitle.text = "You are all clear for now. Book a fresh hilot session anytime."
                emptyActionBtn.visibility = View.VISIBLE
            }
            "past" -> {
                emptyTitle.text = "No past appointments"
                emptySubtitle.text = "Completed sessions will show up here once you have one."
                emptyActionBtn.visibility = View.GONE
            }
            else -> {
                emptyTitle.text = "No cancelled appointments"
                emptySubtitle.text = "Cancelled or declined sessions will appear here."
                emptyActionBtn.visibility = View.GONE
            }
        }
    }

    private fun styleTab(tab: LinearLayout, label: TextView, badge: TextView, active: Boolean) {
        if (active) {
            tab.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1C1408"))).apply { cornerRadius = dp(14).toFloat() }
            label.setTextColor(Color.parseColor("#FBBF24"))
            badge.setTextColor(Color.parseColor("#FBBF24"))
            badge.background = makePill(Color.parseColor("#2A2116"))
        } else {
            tab.setBackgroundColor(Color.TRANSPARENT)
            label.setTextColor(getColor(R.color.text_tertiary))
            badge.setTextColor(getColor(R.color.primary_orange))
            badge.background = makePill(Color.parseColor("#FEF3C7"))
        }
    }

    private fun makePill(fill: Int) = GradientDrawable().apply { setColor(fill); cornerRadius = dp(999).toFloat() }

    override fun onViewDetails(appointment: Appointment) = showDetailsDialog(appointment)
    override fun onReschedule(appointment: Appointment) { if (!canModifyAppointment(appointment)) showClinicContactDialog("Reschedule not available", "This appointment is within 24 hours. Please contact the clinic directly if you need to reschedule.") else showRescheduleDialog(appointment) }
    override fun onCancel(appointment: Appointment) { if (!canModifyAppointment(appointment)) showClinicContactDialog("Cancel not available", "This appointment is within 24 hours. Please contact the clinic directly if you need to cancel.") else showCancelDialog(appointment) }
    override fun onReview(appointment: Appointment) = showReviewDialog(appointment)
    override fun onRebook(appointment: Appointment) { startActivity(Intent(this, BookAppointmentActivity::class.java)) }
    override fun onFollowUp(appointment: Appointment) { startActivity(Intent(this, BookAppointmentActivity::class.java)) }

    private fun showDetailsDialog(appointment: Appointment) {
        val view = layoutInflater.inflate(R.layout.dialog_appointment_details, null)
        val dialog = createDialog(view)
        view.findViewById<TextView>(R.id.detailServiceValue).text = appointment.serviceName
        view.findViewById<TextView>(R.id.detailSpecialistValue).text = appointment.specialistName
        view.findViewById<TextView>(R.id.detailDateValue).text = appointment.appointmentDate
        view.findViewById<TextView>(R.id.detailTimeValue).text = appointment.timeSlot
        view.findViewById<TextView>(R.id.detailStatusValue).text = appointment.status
        view.findViewById<TextView>(R.id.detailPatientValue).text = safe(appointment.patientName, "Patient")
        view.findViewById<TextView>(R.id.detailReasonValue).text = safe(appointment.reason, "No reason provided.")
        val rescheduleBox = view.findViewById<LinearLayout>(R.id.detailRescheduleBox)
        val cancellationBox = view.findViewById<LinearLayout>(R.id.detailCancellationBox)
        if (!appointment.rescheduleReason.isNullOrBlank()) { rescheduleBox.visibility = View.VISIBLE; view.findViewById<TextView>(R.id.detailRescheduleReason).text = appointment.rescheduleReason }
        if (!appointment.cancellationReason.isNullOrBlank()) { cancellationBox.visibility = View.VISIBLE; view.findViewById<TextView>(R.id.detailCancellationReason).text = appointment.cancellationReason }
        val btn1 = view.findViewById<Button>(R.id.detailActionBtn1)
        val btn2 = view.findViewById<Button>(R.id.detailActionBtn2)
        val btn3 = view.findViewById<Button>(R.id.detailActionBtn3)
        val spacer2 = view.findViewById<Space>(R.id.detailActionSpacer2)
        val closeBtn = view.findViewById<Button>(R.id.detailCloseBtn)
        closeBtn.background = makeOutlineDrawable(Color.WHITE, Color.parseColor("#E8DDD0"))
        closeBtn.setTextColor(Color.parseColor("#44291A"))
        closeBtn.setOnClickListener { dialog.dismiss() }
        when (currentTab) {
            "upcoming" -> {
                styleDarkActionButton(btn1, "View Details ->", true)
                styleOutlineActionButton(btn2, "Reschedule", false)
                styleOutlineActionButton(btn3, "Cancel Appointment", true)
                btn2.setOnClickListener { dialog.dismiss(); onReschedule(appointment) }
                btn3.setOnClickListener { dialog.dismiss(); onCancel(appointment) }
            }
            "past" -> {
                styleOutlineActionButton(btn1, "View Summary", false)
                styleDarkActionButton(btn2, if (appointment.isReviewed()) "Review Submitted" else "Leave Review", !appointment.isReviewed())
                styleDarkActionButton(btn3, "Book Follow-up ->", true)
                btn2.setOnClickListener { if (!appointment.isReviewed()) { dialog.dismiss(); onReview(appointment) } }
                btn3.setOnClickListener { dialog.dismiss(); onFollowUp(appointment) }
            }
            else -> {
                styleOutlineActionButton(btn1, "View Details", false)
                styleDarkActionButton(btn2, "Rebook ->", true)
                btn3.visibility = View.GONE
                spacer2.visibility = View.GONE
                btn2.setOnClickListener { dialog.dismiss(); onRebook(appointment) }
            }
        }
        dialog.show(); styleDialogWindow(dialog)
    }

    private fun showCancelDialog(appointment: Appointment) {
        val view = layoutInflater.inflate(R.layout.dialog_cancel_appointment, null)
        val dialog = createDialog(view)
        val reasonInput = view.findViewById<EditText>(R.id.cancelReasonInput)
        val counter = view.findViewById<TextView>(R.id.cancelReasonCounter)
        val confirmBtn = view.findViewById<Button>(R.id.cancelConfirmBtn)
        val dismissBtn = view.findViewById<Button>(R.id.cancelDismissBtn)
        styleOutlineActionButton(confirmBtn, "Yes, Cancel Appointment", true)
        styleOutlineActionButton(dismissBtn, "Keep Appointment", false)
        confirmBtn.isEnabled = false; confirmBtn.alpha = 0.5f
        reasonInput.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                val length = s.length
                counter.text = "$length/200"
                val enabled = length > 0
                confirmBtn.isEnabled = enabled; confirmBtn.alpha = if (enabled) 1f else 0.5f
            }
        })
        confirmBtn.setOnClickListener { submitCancellation(appointment, reasonInput.text.toString().trim(), dialog) }
        dismissBtn.setOnClickListener { dialog.dismiss() }
        dialog.show(); styleDialogWindow(dialog)
    }

    private fun showReviewDialog(appointment: Appointment) {
        val view = layoutInflater.inflate(R.layout.dialog_review_appointment, null)
        val dialog = createDialog(view)
        view.findViewById<TextView>(R.id.reviewServiceText).text = appointment.serviceName
        view.findViewById<TextView>(R.id.reviewSpecialistText).text = appointment.specialistName
        val banner = view.findViewById<LinearLayout>(R.id.reviewBanner)
        val ratingLabel = view.findViewById<TextView>(R.id.reviewRatingLabel)
        val feedbackInput = view.findViewById<EditText>(R.id.reviewFeedbackInput)
        val counter = view.findViewById<TextView>(R.id.reviewCounter)
        val submitBtn = view.findViewById<Button>(R.id.reviewSubmitBtn)
        val cancelBtn = view.findViewById<Button>(R.id.reviewCancelBtn)
        styleDarkActionButton(submitBtn, "Submit Review", false)
        styleOutlineActionButton(cancelBtn, "Cancel", false)
        submitBtn.isEnabled = false; submitBtn.alpha = 0.5f
        val stars = arrayOf(
            view.findViewById<TextView>(R.id.reviewStar1),
            view.findViewById(R.id.reviewStar2),
            view.findViewById(R.id.reviewStar3),
            view.findViewById(R.id.reviewStar4),
            view.findViewById(R.id.reviewStar5)
        )
        val rating = intArrayOf(0)
        if (appointment.isReviewed()) {
            banner.visibility = View.VISIBLE
            feedbackInput.isEnabled = false
            submitBtn.isEnabled = false; submitBtn.alpha = 0.5f
            ratingLabel.text = "Review already submitted"
            updateStars(stars, 5)
        } else {
            for (i in stars.indices) {
                val value = i + 1
                stars[i].setOnClickListener {
                    rating[0] = value
                    updateStars(stars, rating[0])
                    ratingLabel.text = getRatingLabel(rating[0])
                    styleDarkActionButton(submitBtn, "Submit Review", true)
                    submitBtn.isEnabled = true; submitBtn.alpha = 1f
                }
            }
        }
        feedbackInput.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable) { counter.text = "${s.length}/500" }
        })
        submitBtn.setOnClickListener { submitReview(appointment, rating[0], feedbackInput.text.toString().trim(), dialog) }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show(); styleDialogWindow(dialog)
    }

    private fun showRescheduleDialog(appointment: Appointment) {
        val view = layoutInflater.inflate(R.layout.dialog_reschedule_appointment, null)
        val dialog = createDialog(view)
        val reasonInput = view.findViewById<EditText>(R.id.rescheduleReasonInput)
        val counter = view.findViewById<TextView>(R.id.rescheduleReasonCounter)
        val monthTitle = view.findViewById<TextView>(R.id.rescheduleMonthTitle)
        val prevMonthBtn = view.findViewById<Button>(R.id.reschedulePrevMonthBtn)
        val nextMonthBtn = view.findViewById<Button>(R.id.rescheduleNextMonthBtn)
        val calendarHeader = view.findViewById<GridLayout>(R.id.rescheduleCalendarHeader)
        val calendarGrid = view.findViewById<GridLayout>(R.id.rescheduleCalendarGrid)
        val morningRecycler = view.findViewById<RecyclerView>(R.id.rescheduleMorningSlots)
        val afternoonRecycler = view.findViewById<RecyclerView>(R.id.rescheduleAfternoonSlots)
        val confirmBtn = view.findViewById<Button>(R.id.rescheduleConfirmBtn)
        val cancelBtn = view.findViewById<Button>(R.id.rescheduleCancelBtn)

        val displayMonth = Calendar.getInstance()
        val selectedDay = intArrayOf(-1)
        val selectedSlot = arrayOf("")

        styleDarkActionButton(confirmBtn, "✓ Confirm Reschedule", false)
        confirmBtn.isEnabled = false
        confirmBtn.alpha = 0.5f
        styleOutlineActionButton(cancelBtn, "Cancel", false)
        styleNavButton(prevMonthBtn)
        styleNavButton(nextMonthBtn)

        reasonInput.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                counter.text = "${s.length}/200"
                updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
            }
        })

        buildCalendarHeader(calendarHeader)

        val morningAdapter = RescheduleSlotAdapter(MORNING_SLOTS) { slot ->
            selectedSlot[0] = slot
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
        }
        val afternoonAdapter = RescheduleSlotAdapter(AFTERNOON_SLOTS) { slot ->
            selectedSlot[0] = slot
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
        }
        morningRecycler.layoutManager = LinearLayoutManager(this)
        afternoonRecycler.layoutManager = LinearLayoutManager(this)
        morningRecycler.adapter = morningAdapter
        afternoonRecycler.adapter = afternoonAdapter

        val refreshCalendar = {
            monthTitle.text = SimpleDateFormat("MMMM yyyy", Locale.US).format(displayMonth.time)
            buildCalendarDays(calendarGrid, displayMonth, selectedDay) { day ->
                selectedDay[0] = day
                buildCalendarDays(calendarGrid, displayMonth, selectedDay) { }
                updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
            }
        }
        refreshCalendar()

        prevMonthBtn.setOnClickListener {
            displayMonth.add(Calendar.MONTH, -1)
            selectedDay[0] = -1
            selectedSlot[0] = ""
            morningAdapter.setSelectedSlot("")
            afternoonAdapter.setSelectedSlot("")
            refreshCalendar()
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
        }
        nextMonthBtn.setOnClickListener {
            displayMonth.add(Calendar.MONTH, 1)
            selectedDay[0] = -1
            selectedSlot[0] = ""
            morningAdapter.setSelectedSlot("")
            afternoonAdapter.setSelectedSlot("")
            refreshCalendar()
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
        }

        morningAdapter.setSelectionListener { slot ->
            selectedSlot[0] = slot
            afternoonAdapter.setSelectedSlot("")
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
        }
        afternoonAdapter.setSelectionListener { slot ->
            selectedSlot[0] = slot
            morningAdapter.setSelectedSlot("")
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.text.toString().trim())
        }

        confirmBtn.setOnClickListener {
            val newDate = String.format(
                Locale.US,
                "%04d-%02d-%02d",
                displayMonth.get(Calendar.YEAR),
                displayMonth.get(Calendar.MONTH) + 1,
                selectedDay[0]
            )
            submitReschedule(appointment, newDate, selectedSlot[0], reasonInput.text.toString().trim(), dialog)
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }

        dialog.show(); styleDialogWindow(dialog)
    }

    private fun updateRescheduleConfirmState(confirmBtn: Button, selectedDay: Int, selectedSlot: String?, reason: String?) {
        val enabled = selectedDay > 0 && !selectedSlot.isNullOrEmpty() && !reason.isNullOrEmpty()
        styleDarkActionButton(confirmBtn, "✓ Confirm Reschedule", enabled)
        confirmBtn.isEnabled = enabled
        confirmBtn.alpha = if (enabled) 1f else 0.5f
    }

    private fun buildCalendarHeader(header: GridLayout) {
        header.removeAllViews()
        for (dayName in DAY_NAMES) {
            val label = TextView(this)
            label.text = dayName
            label.setTextColor(getColor(R.color.text_secondary))
            label.textSize = 11f
            label.gravity = Gravity.CENTER
            label.setTypeface(label.typeface, Typeface.BOLD)
            val params = GridLayout.LayoutParams()
            params.width = dp(34)
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.setMargins(2, 2, 2, 6)
            label.layoutParams = params
            header.addView(label)
        }
    }

    private fun buildCalendarDays(grid: GridLayout, displayMonth: Calendar, selectedDay: IntArray, listener: DayClickListener) {
        grid.removeAllViews()
        val monthCalendar = displayMonth.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        for (i in 0 until 42) {
            val dayView = TextView(this)
            dayView.gravity = Gravity.CENTER
            dayView.textSize = 13f
            val dayNumber = i - firstDayOfWeek + 1
            if (dayNumber > 0 && dayNumber <= daysInMonth) {
                dayView.text = dayNumber.toString()
                val candidate = displayMonth.clone() as Calendar
                candidate.set(Calendar.DAY_OF_MONTH, dayNumber)
                candidate.set(Calendar.HOUR_OF_DAY, 0)
                candidate.set(Calendar.MINUTE, 0)
                candidate.set(Calendar.SECOND, 0)
                candidate.set(Calendar.MILLISECOND, 0)

                val isPast = candidate.before(today)
                val isSunday = candidate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                val isSelected = selectedDay[0] == dayNumber

                when {
                    isSelected -> {
                        dayView.background = makeGradientDrawable("#D97706", "#B45309", 10)
                        dayView.setTextColor(Color.WHITE)
                        dayView.setTypeface(dayView.typeface, Typeface.BOLD)
                    }
                    isPast || isSunday -> {
                        dayView.setTextColor(Color.parseColor("#D4C5B0"))
                        dayView.background = makeRoundedRect(Color.TRANSPARENT, 10, 0, Color.TRANSPARENT)
                    }
                    else -> {
                        dayView.setTextColor(Color.parseColor("#44291A"))
                        dayView.background = makeRoundedRect(Color.WHITE, 10, 1, Color.parseColor("#E8DDD0"))
                        dayView.setOnClickListener {
                            selectedDay[0] = dayNumber
                            listener.onDayClicked(dayNumber)
                        }
                    }
                }
            } else {
                dayView.text = ""
            }

            val params = GridLayout.LayoutParams()
            params.width = dp(34)
            params.height = dp(34)
            params.setMargins(2, 2, 2, 2)
            dayView.layoutParams = params
            grid.addView(dayView)
        }
    }

    private fun canModifyAppointment(appointment: Appointment): Boolean {
        return try {
            val appointmentDate = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                .parse("${appointment.appointmentDate} ${appointment.timeSlot}")
            if (appointmentDate == null) {
                true
            } else {
                val diffMillis = appointmentDate.time - System.currentTimeMillis()
                val hours = diffMillis / (1000 * 60 * 60)
                hours >= 24
            }
        } catch (e: ParseException) {
            Log.w(TAG, "Failed to parse appointment date", e)
            true
        }
    }

    override fun showNotification(message: String, success: Boolean) {
        notificationBanner.setBackgroundResource(
            if (success) R.drawable.my_appts_notification_success else R.drawable.my_appts_notification_error
        )
        notificationIcon.text = if (success) "✓" else "⚠"
        notificationMessage.text = message
        notificationBanner.translationY = -30f
        notificationBanner.alpha = 0f
        notificationBanner.visibility = View.VISIBLE
        notificationBanner.animate().translationY(0f).alpha(1f).setDuration(220).start()
        scheduleNotificationDismiss()
    }

    private fun scheduleNotificationDismiss() {
        notificationDismissRunnable?.let { HANDLER.removeCallbacks(it) }
        notificationDismissRunnable = Runnable { hideNotification() }
        HANDLER.postDelayed(notificationDismissRunnable!!, 5000)
    }

    private fun hideNotification() {
        notificationBanner.animate().translationY(-20f).alpha(0f).setDuration(180)
            .withEndAction { notificationBanner.visibility = View.GONE }
            .start()
    }

    private fun createDialog(view: View): AlertDialog = AlertDialog.Builder(this).setView(view).create()

    private fun showClinicContactDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun styleDialogWindow(dialog: AlertDialog) {
        val window = dialog.window
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout((resources.displayMetrics.widthPixels * 0.94f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun styleDarkActionButton(button: Button, text: String, enabled: Boolean) {
        button.text = text
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            if (enabled) intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1C1408"))
            else intArrayOf(Color.parseColor("#8A847B"), Color.parseColor("#8A847B"))
        )
        drawable.cornerRadius = dp(12).toFloat()
        button.background = drawable
        button.setTextColor(if (enabled) Color.parseColor("#FBBF24") else Color.parseColor("#F5E6B1"))
    }

    private fun styleOutlineActionButton(button: Button, text: String, red: Boolean) {
        button.text = text
        button.background = makeOutlineDrawable(Color.WHITE, if (red) Color.parseColor("#FECACA") else Color.parseColor("#E8DDD0"))
        button.setTextColor(if (red) Color.parseColor("#DC2626") else Color.parseColor("#44291A"))
    }

    private fun styleNavButton(button: Button) {
        button.background = makeOutlineDrawable(Color.WHITE, Color.parseColor("#E8DDD0"))
        button.setTextColor(Color.parseColor("#78716C"))
    }

    private fun makeOutlineDrawable(fill: Int, strokeColor: Int): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.setColor(fill)
        drawable.cornerRadius = dp(10).toFloat()
        drawable.setStroke(dp(1), strokeColor)
        return drawable
    }

    private fun makeRoundedRect(fill: Int, radiusDp: Int, strokeDp: Int, strokeColor: Int): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.setColor(fill)
        drawable.cornerRadius = dp(radiusDp).toFloat()
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor)
        }
        return drawable
    }

    private fun makeGradientDrawable(startColor: String, endColor: String, radiusDp: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        ).apply {
            cornerRadius = dp(radiusDp).toFloat()
        }
    }

    private fun updateStars(stars: Array<TextView>, rating: Int) {
        for (i in stars.indices) {
            stars[i].setTextColor(if (i < rating) Color.parseColor("#D97706") else Color.parseColor("#D1C6B2"))
            stars[i].scaleX = if (i < rating) 1.1f else 1f
            stars[i].scaleY = if (i < rating) 1.1f else 1f
        }
    }

    private fun getRatingLabel(rating: Int): String = when (rating) {
        1 -> "Poor"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Very Good"
        5 -> "Excellent"
        else -> "Tap a star to rate"
    }

    private fun dp(value: Int): Int = Math.round(value * resources.displayMetrics.density)

    private fun safe(value: String?, fallback: String = ""): String = if (value.isNullOrBlank()) fallback else value

    private fun interface DayClickListener {
        fun onDayClicked(day: Int)
    }

    private abstract class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    }

    private class RescheduleSlotAdapter(
        private val slots: List<String>,
        private var selectionListener: OnSlotSelectedListener?
    ) : RecyclerView.Adapter<RescheduleSlotAdapter.SlotViewHolder>() {
        private var selectedSlot = ""

        fun interface OnSlotSelectedListener {
            fun onSelected(slot: String)
        }

        fun setSelectionListener(listener: OnSlotSelectedListener?) {
            selectionListener = listener
        }

        fun setSelectedSlot(selectedSlot: String) {
            this.selectedSlot = selectedSlot
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
            return SlotViewHolder(view)
        }

        override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
            val slot = slots[position]
            val selected = slot == selectedSlot
            val disabled = slot == LUNCH_BREAK_SLOT
            holder.bind(slot, selected, disabled)
            holder.itemView.setOnClickListener {
                if (!disabled) {
                    selectedSlot = slot
                    notifyDataSetChanged()
                    selectionListener?.onSelected(slot)
                }
            }
        }

        override fun getItemCount(): Int = slots.size

        class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textView: TextView = itemView.findViewById(R.id.timeSlotText)

            fun bind(slot: String, selected: Boolean, disabled: Boolean) {
                val drawable = GradientDrawable().apply { cornerRadius = 24f }
                when {
                    disabled -> {
                        drawable.setColor(Color.parseColor("#F5F5F5"))
                        drawable.setStroke(2, Color.parseColor("#D4C5B0"))
                        textView.text = "$slot (Lunch)"
                        textView.setTextColor(Color.parseColor("#A8956B"))
                        textView.alpha = 0.7f
                    }
                    selected -> {
                        drawable.colors = intArrayOf(Color.parseColor("#D97706"), Color.parseColor("#B45309"))
                        textView.text = slot
                        textView.setTextColor(Color.WHITE)
                        textView.alpha = 1f
                    }
                    else -> {
                        drawable.setColor(Color.WHITE)
                        drawable.setStroke(2, Color.parseColor("#E8DDD0"))
                        textView.text = slot
                        textView.setTextColor(Color.parseColor("#44291A"))
                        textView.alpha = 1f
                    }
                }
                textView.background = drawable
            }
        }
    }

    companion object {
        private const val TAG = "MyAppointmentsActivity"
        private val MORNING_SLOTS = listOf("08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM")
        private val AFTERNOON_SLOTS = listOf("01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM")
        private const val LUNCH_BREAK_SLOT = "12:00 PM"
        private val DAY_NAMES = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        private val HANDLER = Handler(Looper.getMainLooper())
    }
}
