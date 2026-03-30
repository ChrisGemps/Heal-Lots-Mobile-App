package com.heallots.mobile.ui.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.api.ApiClient
import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.User
import com.heallots.mobile.storage.TokenManager
import com.heallots.mobile.ui.adapters.AdminAppointmentAdapter
import com.heallots.mobile.ui.adapters.AdminUserAdapter
import com.heallots.mobile.utils.Constants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback as RetrofitCallback
import retrofit2.Response

class AdminDashboardActivity : AppCompatActivity(), AdminAppointmentAdapter.OnAdminAppointmentActionListener {
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService
    private lateinit var overviewTab: Button
    private lateinit var appointmentsTab: Button
    private lateinit var usersTab: Button
    private lateinit var userBadge: LinearLayout
    private lateinit var userAvatar: ImageView
    private lateinit var userNameText: TextView
    private lateinit var userAvatarText: TextView
    private lateinit var statTotalApts: TextView
    private lateinit var statPendingApts: TextView
    private lateinit var statApprovedApts: TextView
    private lateinit var statTotalPatients: TextView
    private lateinit var overviewScroll: ScrollView
    private lateinit var adminListRecyclerView: RecyclerView
    private lateinit var appointmentAdapter: AdminAppointmentAdapter
    private lateinit var userAdapter: AdminUserAdapter
    private val allAppointments = ArrayList<Appointment>()
    private val allUsers = ArrayList<User>()
    private var currentTab = "overview"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_admin_dashboard)
            tokenManager = TokenManager(this)
            apiService = ApiClient.getApiService()
            overviewTab = findViewById(R.id.adminOverviewTab)
            appointmentsTab = findViewById(R.id.adminAppointmentsTab)
            usersTab = findViewById(R.id.adminUsersTab)
            userBadge = findViewById(R.id.adminUserBadge)
            userAvatar = findViewById(R.id.adminUserAvatar)
            userNameText = findViewById(R.id.adminUserName)
            userAvatarText = findViewById(R.id.adminUserAvatarText)
            statTotalApts = findViewById(R.id.adminStatTotalAppointments)
            statPendingApts = findViewById(R.id.adminStatCompletedAppointments)
            statApprovedApts = findViewById(R.id.adminStatAverageRating)
            statTotalPatients = findViewById(R.id.adminStatTotalPatients)
            overviewScroll = findViewById(R.id.adminOverviewScroll)
            adminListRecyclerView = findViewById(R.id.adminListRecyclerView)
            adminListRecyclerView.layoutManager = LinearLayoutManager(this)
            appointmentAdapter = AdminAppointmentAdapter(this, allAppointments, this)
            userAdapter = AdminUserAdapter(this, allUsers)
            adminListRecyclerView.adapter = appointmentAdapter
            displayUserInfo()
            setupListeners()
            loadAdminData()
            selectTab("overview")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e); finish()
        }
    }

    override fun onResume() { super.onResume(); displayUserInfo(); loadAdminData() }

    private fun setupListeners() {
        overviewTab.setOnClickListener { selectTab("overview") }
        appointmentsTab.setOnClickListener { selectTab("appointments") }
        usersTab.setOnClickListener { selectTab("users") }
        userBadge.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
    }

    private fun displayUserInfo() {
        val currentUser = tokenManager.getUser()
        val fullName = currentUser?.fullName?.takeUnless { it.isBlank() } ?: "Admin"
        userNameText.text = fullName.substringBefore(" ")
        userAvatarText.text = fullName.substring(0, 1).uppercase(Locale.US)
        val picture = currentUser?.profilePictureUrl
        if (!picture.isNullOrBlank()) {
            val imageUrl = if (picture.startsWith("http")) picture else "${Constants.BASE_URL}/api/user/profile-picture/$picture"
            userAvatarText.visibility = View.VISIBLE
            Picasso.get().load(imageUrl).fit().centerCrop().into(userAvatar, object : Callback {
                override fun onSuccess() { userAvatarText.visibility = View.GONE }
                override fun onError(e: Exception?) { userAvatar.setImageDrawable(null); userAvatarText.visibility = View.VISIBLE }
            })
        } else {
            userAvatar.setImageDrawable(null); userAvatarText.visibility = View.VISIBLE
        }
    }

    private fun loadAdminData() { loadAppointments(); loadUsers() }

    private fun loadAppointments() {
        val authHeader = tokenManager.getAuthorizationHeader() ?: return
        apiService.getAllAppointments(authHeader).enqueue(object : RetrofitCallback<List<Appointment>> {
            override fun onResponse(call: Call<List<Appointment>>, response: Response<List<Appointment>>) {
                val body = response.body() ?: return
                if (!response.isSuccessful) return
                allAppointments.clear(); allAppointments.addAll(body); allAppointments.sortBy { toTimestamp(it) }
                appointmentAdapter.updateAppointments(allAppointments); updateStats()
            }
            override fun onFailure(call: Call<List<Appointment>>, t: Throwable) { Log.e(TAG, "Failed to load appointments", t) }
        })
    }

    private fun loadUsers() {
        val authHeader = tokenManager.getAuthorizationHeader() ?: return
        apiService.getAllUsers(authHeader).enqueue(object : RetrofitCallback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                val body = response.body() ?: return
                if (!response.isSuccessful) return
                allUsers.clear(); allUsers.addAll(body); userAdapter.updateUsers(allUsers); updateStats()
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) { Log.e(TAG, "Failed to load users", t) }
        })
    }

    private fun updateStats() {
        var pending = 0; var approved = 0
        for (appointment in allAppointments) {
            val status = safe(appointment.status).lowercase(Locale.US)
            if (status.contains("pending") || status.contains("resched")) pending++
            if (status.contains("approved")) approved++
        }
        statTotalApts.text = allAppointments.size.toString()
        statPendingApts.text = pending.toString()
        statApprovedApts.text = approved.toString()
        statTotalPatients.text = allUsers.size.toString()
    }

    private fun selectTab(tab: String) {
        currentTab = tab
        styleTabButton(overviewTab, tab == "overview")
        styleTabButton(appointmentsTab, tab == "appointments")
        styleTabButton(usersTab, tab == "users")
        when (tab) {
            "overview" -> { overviewScroll.visibility = View.VISIBLE; adminListRecyclerView.visibility = View.GONE }
            "appointments" -> { overviewScroll.visibility = View.GONE; adminListRecyclerView.visibility = View.VISIBLE; adminListRecyclerView.adapter = appointmentAdapter; appointmentAdapter.updateAppointments(allAppointments) }
            else -> { overviewScroll.visibility = View.GONE; adminListRecyclerView.visibility = View.VISIBLE; adminListRecyclerView.adapter = userAdapter; userAdapter.updateUsers(allUsers) }
        }
    }

    override fun onViewDetails(appointment: Appointment) = showAppointmentDetailsDialog(appointment)
    override fun onApprove(appointment: Appointment) = updateAppointmentStatus(appointment, "Approved")
    override fun onCancel(appointment: Appointment) = updateAppointmentStatus(appointment, "Cancelled")
    override fun onMarkDone(appointment: Appointment) = updateAppointmentStatus(appointment, "Done")

    private fun updateAppointmentStatus(appointment: Appointment, status: String) {
        val authHeader = tokenManager.getAuthorizationHeader() ?: return
        val id = appointment.id ?: return
        apiService.updateAppointmentStatus(authHeader, id, hashMapOf("status" to status)).enqueue(object : RetrofitCallback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                val updated = response.body() ?: return
                if (!response.isSuccessful) return
                for (i in allAppointments.indices) if (safe(allAppointments[i].id) == safe(updated.id)) { allAppointments[i] = updated; break }
                appointmentAdapter.updateAppointments(allAppointments); updateStats(); if (currentTab != "appointments") selectTab(currentTab)
            }
            override fun onFailure(call: Call<Appointment>, t: Throwable) { Log.e(TAG, "Failed to update appointment status", t) }
        })
    }

    private fun showAppointmentDetailsDialog(appointment: Appointment) {
        val view = layoutInflater.inflate(R.layout.dialog_admin_appointment_details, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()
        val linkedUser = findLinkedUser(appointment)
        val status = safe(appointment.status).lowercase(Locale.US)
        view.findViewById<TextView>(R.id.adminDetailPatientNameValue).text = safe(appointment.patientName, "Unknown")
        view.findViewById<TextView>(R.id.adminDetailEmailValue).text = safe(appointment.patientEmail, linkedUser?.email ?: "N/A")
        view.findViewById<TextView>(R.id.adminDetailPhoneValue).text = safe(appointment.patientPhone, linkedUser?.getPhone() ?: "N/A")
        view.findViewById<TextView>(R.id.adminDetailServiceValue).text = getServiceWithEmoji(appointment.serviceName)
        view.findViewById<TextView>(R.id.adminDetailSpecialistValue).text = safe(appointment.specialistName, "Specialist")
        view.findViewById<TextView>(R.id.adminDetailDateValue).text = safe(appointment.appointmentDate, "TBD")
        view.findViewById<TextView>(R.id.adminDetailTimeValue).text = safe(appointment.timeSlot, "TBD")
        view.findViewById<TextView>(R.id.adminDetailStatusValue).text = safe(appointment.status, "Pending")
        view.findViewById<TextView>(R.id.adminDetailReasonValue).text = safe(appointment.reason, "No reason provided.")
        view.findViewById<LinearLayout>(R.id.adminDetailNotesBox).visibility = if (appointment.notes.isNullOrBlank()) View.GONE else View.VISIBLE
        if (!appointment.notes.isNullOrBlank()) view.findViewById<TextView>(R.id.adminDetailNotesValue).text = appointment.notes
        view.findViewById<LinearLayout>(R.id.adminDetailRescheduleBox).visibility = if (status.contains("resched") || !appointment.rescheduleReason.isNullOrBlank()) View.VISIBLE else View.GONE
        if (status.contains("resched") || !appointment.rescheduleReason.isNullOrBlank()) view.findViewById<TextView>(R.id.adminDetailRescheduleReason).text = safe(appointment.rescheduleReason, "No reason provided.")
        view.findViewById<LinearLayout>(R.id.adminDetailCancellationBox).visibility = if (status.contains("cancel") || !appointment.cancellationReason.isNullOrBlank()) View.VISIBLE else View.GONE
        if (status.contains("cancel") || !appointment.cancellationReason.isNullOrBlank()) view.findViewById<TextView>(R.id.adminDetailCancellationReason).text = safe(appointment.cancellationReason, "No reason provided.")
        val b1 = view.findViewById<Button>(R.id.adminDetailActionBtn1)
        val b2 = view.findViewById<Button>(R.id.adminDetailActionBtn2)
        val b3 = view.findViewById<Button>(R.id.adminDetailActionBtn3)
        val row = view.findViewById<LinearLayout>(R.id.adminDetailExtraActionsRow)
        val b4 = view.findViewById<Button>(R.id.adminDetailActionBtn4)
        val spacer = view.findViewById<Space>(R.id.adminDetailActionSpacer2)
        view.findViewById<Button>(R.id.adminDetailHeaderCloseBtn).setOnClickListener { dialog.dismiss() }
        styleOutlineButton(b1, "Close", false); b1.setOnClickListener { dialog.dismiss() }; row?.visibility = View.GONE; b4?.visibility = View.GONE
        when {
            status.contains("pending") || status.contains("resched") -> {
                styleOutlineButton(b2, "Reject", true); styleGreenButton(b3, "Approve")
                b2.setOnClickListener { showStatusConfirmationDialog("Reject?", "Reject this appointment for ${safe(appointment.patientName, "this patient")}?", "Yes, Reject") { dialog.dismiss(); onCancel(appointment) } }
                b3.setOnClickListener { dialog.dismiss(); onApprove(appointment) }
                b2.visibility = View.VISIBLE; b3.visibility = View.VISIBLE; spacer.visibility = View.VISIBLE
            }
            status.contains("approved") -> {
                styleOutlineButton(b2, "Call Off", true); styleAmberButton(b3, "Reschedule"); styleBlueButton(b4, "Mark as Done")
                b2.setOnClickListener { showStatusConfirmationDialog("Call Off?", "Call off this appointment for ${safe(appointment.patientName, "this patient")}?", "Yes, Call Off") { dialog.dismiss(); onCancel(appointment) } }
                b3.setOnClickListener { dialog.dismiss(); showAdminRescheduleDialog(appointment) }
                b4.setOnClickListener { dialog.dismiss(); onMarkDone(appointment) }
                b2.visibility = View.VISIBLE; b3.visibility = View.VISIBLE; row.visibility = View.VISIBLE; b4.visibility = View.VISIBLE; spacer.visibility = View.VISIBLE
            }
            status.contains("done") || status.contains("complete") -> {
                styleGreenButton(b2, "Return to Approved"); b2.setOnClickListener { dialog.dismiss(); onApprove(appointment) }
                b2.visibility = View.VISIBLE; b3.visibility = View.GONE; spacer.visibility = View.GONE
            }
            else -> { b2.visibility = View.GONE; b3.visibility = View.GONE; spacer.visibility = View.GONE }
        }
        dialog.show(); styleDialogWindow(dialog)
    }

    private fun showAdminRescheduleDialog(appointment: Appointment) {
        val view = layoutInflater.inflate(R.layout.dialog_reschedule_appointment, null)
        val dialog = AlertDialog.Builder(this).setView(view).create()
        val title = view.findViewById<TextView>(R.id.rescheduleDialogTitle)
        val reason = view.findViewById<EditText>(R.id.rescheduleReasonInput)
        val counter = view.findViewById<TextView>(R.id.rescheduleReasonCounter)
        val month = view.findViewById<TextView>(R.id.rescheduleMonthTitle)
        val prev = view.findViewById<Button>(R.id.reschedulePrevMonthBtn)
        val next = view.findViewById<Button>(R.id.rescheduleNextMonthBtn)
        val header = view.findViewById<GridLayout>(R.id.rescheduleCalendarHeader)
        val grid = view.findViewById<GridLayout>(R.id.rescheduleCalendarGrid)
        val morning = view.findViewById<RecyclerView>(R.id.rescheduleMorningSlots)
        val afternoon = view.findViewById<RecyclerView>(R.id.rescheduleAfternoonSlots)
        val confirm = view.findViewById<Button>(R.id.rescheduleConfirmBtn)
        val cancel = view.findViewById<Button>(R.id.rescheduleCancelBtn)
        title?.text = "Select New Date & Time"; reason?.visibility = View.GONE; counter?.visibility = View.GONE
        val displayMonth = Calendar.getInstance(); val selectedDay = intArrayOf(-1); val selectedSlot = arrayOf("")
        styleAmberButton(confirm, "\u2713 Confirm Reschedule"); confirm.isEnabled = false; confirm.alpha = 0.5f
        styleOutlineButton(cancel, "Cancel", false); styleOutlineButton(prev, "\u2190", false); styleOutlineButton(next, "\u2192", false)
        buildCalendarHeader(header)
        val morningAdapter = AdminRescheduleSlotAdapter(MORNING_SLOTS); val afternoonAdapter = AdminRescheduleSlotAdapter(AFTERNOON_SLOTS)
        morning.layoutManager = LinearLayoutManager(this); afternoon.layoutManager = LinearLayoutManager(this)
        morning.adapter = morningAdapter; afternoon.adapter = afternoonAdapter
        val refresh = {
            month.text = SimpleDateFormat("MMMM yyyy", Locale.US).format(displayMonth.time)
            buildCalendarDays(grid, displayMonth, selectedDay) { day -> selectedDay[0] = day; buildCalendarDays(grid, displayMonth, selectedDay) {}; updateAdminRescheduleConfirmState(confirm, selectedDay[0], selectedSlot[0]) }
        }
        refresh()
        prev.setOnClickListener { displayMonth.add(Calendar.MONTH, -1); selectedDay[0] = -1; selectedSlot[0] = ""; morningAdapter.setSelectedSlot(""); afternoonAdapter.setSelectedSlot(""); refresh(); updateAdminRescheduleConfirmState(confirm, selectedDay[0], selectedSlot[0]) }
        next.setOnClickListener { displayMonth.add(Calendar.MONTH, 1); selectedDay[0] = -1; selectedSlot[0] = ""; morningAdapter.setSelectedSlot(""); afternoonAdapter.setSelectedSlot(""); refresh(); updateAdminRescheduleConfirmState(confirm, selectedDay[0], selectedSlot[0]) }
        morningAdapter.setSelectionListener { selectedSlot[0] = it; afternoonAdapter.setSelectedSlot(""); updateAdminRescheduleConfirmState(confirm, selectedDay[0], selectedSlot[0]) }
        afternoonAdapter.setSelectionListener { selectedSlot[0] = it; morningAdapter.setSelectedSlot(""); updateAdminRescheduleConfirmState(confirm, selectedDay[0], selectedSlot[0]) }
        confirm.setOnClickListener { submitAdminReschedule(appointment, String.format(Locale.US, "%04d-%02d-%02d", displayMonth.get(Calendar.YEAR), displayMonth.get(Calendar.MONTH) + 1, selectedDay[0]), selectedSlot[0], dialog) }
        cancel.setOnClickListener { dialog.dismiss() }
        dialog.show(); styleDialogWindow(dialog)
    }

    private fun updateAdminRescheduleConfirmState(confirm: Button, day: Int, slot: String?) { val enabled = day > 0 && !slot.isNullOrEmpty(); styleAmberButton(confirm, "\u2713 Confirm Reschedule"); confirm.isEnabled = enabled; confirm.alpha = if (enabled) 1f else 0.5f }
    private fun submitAdminReschedule(appointment: Appointment, newDate: String, newTimeSlot: String, dialog: AlertDialog) {
        val authHeader = tokenManager.getAuthorizationHeader() ?: return
        val id = appointment.id ?: return
        apiService.updateAppointment(authHeader, id, hashMapOf("appointmentDate" to newDate, "timeSlot" to newTimeSlot, "status" to "Approved")).enqueue(object : RetrofitCallback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) { val updated = response.body() ?: return; if (!response.isSuccessful) return; for (i in allAppointments.indices) if (safe(allAppointments[i].id) == safe(updated.id)) { allAppointments[i] = updated; break }; appointmentAdapter.updateAppointments(allAppointments); updateStats(); if (currentTab != "appointments") selectTab(currentTab); dialog.dismiss() }
            override fun onFailure(call: Call<Appointment>, t: Throwable) { Log.e(TAG, "Failed to reschedule appointment", t) }
        })
    }
    private fun showStatusConfirmationDialog(title: String, message: String, confirmText: String, onConfirm: (() -> Unit)?) { AlertDialog.Builder(this).setTitle(title).setMessage(message).setNegativeButton("Cancel") { d, _ -> d.dismiss() }.setPositiveButton(confirmText) { d, _ -> d.dismiss(); onConfirm?.invoke() }.show() }
    private fun findLinkedUser(appointment: Appointment): User? = allUsers.firstOrNull { safe(it.email) == safe(appointment.patientEmail) || safe(it.fullName) == safe(appointment.patientName) }
    private fun getServiceWithEmoji(name: String?): String { val n = safe(name, "Hilot Session"); return when (n) { "Traditional Hilot" -> "\uD83E\uDD32\uD83C\uDFFB $n"; "Herbal Compress" -> "\uD83C\uDF3F $n"; "Head & Neck Relief" -> "\uD83D\uDC86 $n"; "Foot Reflexology" -> "\uD83E\uDDB6 $n"; "Hot Oil Massage" -> "\uD83E\uDED9 $n"; "Whole-Body Hilot" -> "\uD83E\uDDD8\uD83C\uDFFB $n"; else -> "\uD83C\uDF3F $n" } }
    private fun styleTabButton(b: Button, active: Boolean) { b.background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); if (active) { setColor(Color.parseColor("#FEF3C7")); setStroke(dp(1), Color.parseColor("#F59E0B")) } else { setColor(Color.TRANSPARENT); setStroke(dp(1), Color.TRANSPARENT) } }; b.setTextColor(if (active) getColor(R.color.primary_orange) else getColor(R.color.text_tertiary)) }
    private fun styleOutlineButton(b: Button, text: String, red: Boolean) { b.text = text; b.background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(12).toFloat(); setStroke(dp(1), if (red) Color.parseColor("#FECACA") else Color.parseColor("#E8DDD0")) }; b.setTextColor(if (red) Color.parseColor("#DC2626") else Color.parseColor("#44291A")) }
    private fun styleGreenButton(b: Button, text: String) { b.text = text; b.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#22C55E"), Color.parseColor("#15803D"))).apply { cornerRadius = dp(12).toFloat() }; b.setTextColor(Color.WHITE) }
    private fun styleBlueButton(b: Button, text: String) { b.text = text; b.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#819DCD"), Color.parseColor("#0034A3"))).apply { cornerRadius = dp(12).toFloat() }; b.setTextColor(Color.WHITE) }
    private fun styleAmberButton(b: Button, text: String) { b.text = text; b.background = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#FEF3C7"), Color.parseColor("#F59E0B"))).apply { cornerRadius = dp(12).toFloat(); setStroke(dp(1), Color.parseColor("#D97706")) }; b.setTextColor(Color.parseColor("#92400E")) }
    private fun buildCalendarHeader(header: GridLayout) { header.removeAllViews(); DAY_NAMES.forEach { d -> header.addView(TextView(this).apply { text = d; setTextColor(getColor(R.color.text_secondary)); textSize = 11f; gravity = Gravity.CENTER; setTypeface(typeface, Typeface.BOLD); layoutParams = GridLayout.LayoutParams().apply { width = dp(34); height = ViewGroup.LayoutParams.WRAP_CONTENT; setMargins(2, 2, 2, 6) } }) } }
    private fun buildCalendarDays(grid: GridLayout, displayMonth: Calendar, selectedDay: IntArray, listener: (Int) -> Unit) { grid.removeAllViews(); val monthCalendar = displayMonth.clone() as Calendar; monthCalendar.set(Calendar.DAY_OF_MONTH, 1); val first = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1; val max = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }; for (i in 0 until 42) { val dayView = TextView(this).apply { gravity = Gravity.CENTER; textSize = 13f }; val day = i - first + 1; if (day in 1..max) { dayView.text = day.toString(); val candidate = displayMonth.clone() as Calendar; candidate.set(Calendar.DAY_OF_MONTH, day); candidate.set(Calendar.HOUR_OF_DAY, 0); candidate.set(Calendar.MINUTE, 0); candidate.set(Calendar.SECOND, 0); candidate.set(Calendar.MILLISECOND, 0); val past = candidate.before(today); val sunday = candidate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY; val selected = selectedDay[0] == day; when { selected -> { dayView.background = makeGradientDrawable("#D97706", "#B45309", 10); dayView.setTextColor(Color.WHITE); dayView.setTypeface(dayView.typeface, Typeface.BOLD) }; past || sunday -> { dayView.setTextColor(Color.parseColor("#D4C5B0")); dayView.background = makeRoundedRect(Color.TRANSPARENT, 10, 0, Color.TRANSPARENT) }; else -> { dayView.setTextColor(Color.parseColor("#44291A")); dayView.background = makeRoundedRect(Color.WHITE, 10, 1, Color.parseColor("#E8DDD0")); dayView.setOnClickListener { selectedDay[0] = day; listener(day) } } } }; dayView.layoutParams = GridLayout.LayoutParams().apply { width = dp(34); height = dp(34); setMargins(2, 2, 2, 2) }; grid.addView(dayView) } }
    private fun makeRoundedRect(fill: Int, radiusDp: Int, strokeDp: Int, strokeColor: Int) = GradientDrawable().apply { setColor(fill); cornerRadius = dp(radiusDp).toFloat(); if (strokeDp > 0) setStroke(dp(strokeDp), strokeColor) }
    private fun makeGradientDrawable(start: String, end: String, radiusDp: Int) = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor(start), Color.parseColor(end))).apply { cornerRadius = dp(radiusDp).toFloat() }
    private fun styleDialogWindow(dialog: AlertDialog) { dialog.window?.let { it.setBackgroundDrawableResource(android.R.color.transparent); it.setLayout((resources.displayMetrics.widthPixels * 0.94f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT) } }
    private fun toTimestamp(appointment: Appointment): Long = try { SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).parse("${safe(appointment.appointmentDate)} ${safe(appointment.timeSlot)}")?.time ?: Long.MAX_VALUE } catch (_: Exception) { Long.MAX_VALUE }
    private fun safe(value: String?, fallback: String = ""): String = if (value.isNullOrBlank()) fallback else value
    private fun dp(value: Int): Int = Math.round(value * resources.displayMetrics.density)

    private class AdminRescheduleSlotAdapter(private val slots: List<String>) : RecyclerView.Adapter<AdminRescheduleSlotAdapter.SlotViewHolder>() {
        private var selectedSlot = ""; private var selectionListener: ((String) -> Unit)? = null
        fun setSelectionListener(listener: (String) -> Unit) { selectionListener = listener }
        fun setSelectedSlot(selected: String) { selectedSlot = selected; notifyDataSetChanged() }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SlotViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false))
        override fun getItemCount(): Int = slots.size
        override fun onBindViewHolder(holder: SlotViewHolder, position: Int) { val slot = slots[position]; val selected = slot == selectedSlot; val disabled = slot == LUNCH_BREAK_SLOT; holder.bind(slot, selected, disabled); holder.itemView.setOnClickListener { if (!disabled) { selectedSlot = slot; notifyDataSetChanged(); selectionListener?.invoke(slot) } } }
        class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textView: TextView = itemView.findViewById(R.id.timeSlotText)
            fun bind(slot: String, selected: Boolean, disabled: Boolean) { val d = GradientDrawable().apply { cornerRadius = 24f }; when { disabled -> { d.setColor(Color.parseColor("#F5F5F5")); d.setStroke(2, Color.parseColor("#D4C5B0")); textView.text = "$slot (Lunch)"; textView.setTextColor(Color.parseColor("#A8956B")); textView.alpha = 0.7f }; selected -> { d.colors = intArrayOf(Color.parseColor("#FEF3C7"), Color.parseColor("#F59E0B")); d.setStroke(2, Color.parseColor("#D97706")); textView.text = slot; textView.setTextColor(Color.parseColor("#92400E")); textView.alpha = 1f }; else -> { d.setColor(Color.WHITE); d.setStroke(2, Color.parseColor("#E8DDD0")); textView.text = slot; textView.setTextColor(Color.parseColor("#44291A")); textView.alpha = 1f } }; textView.background = d }
        }
    }

    companion object {
        private const val TAG = "AdminDashboardActivity"
        private val MORNING_SLOTS = listOf("08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM")
        private val AFTERNOON_SLOTS = listOf("01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM")
        private const val LUNCH_BREAK_SLOT = "12:00 PM"
        private val DAY_NAMES = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }
}
