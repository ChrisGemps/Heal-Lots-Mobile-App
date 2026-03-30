package com.heallots.mobile.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.api.ApiClient
import com.heallots.mobile.api.ApiService
import com.heallots.mobile.models.Appointment
import com.heallots.mobile.models.BookAppointmentRequest
import com.heallots.mobile.storage.TokenManager
import com.heallots.mobile.ui.adapters.SpecialistAdapter
import com.heallots.mobile.utils.MockData
import java.util.Calendar
import java.util.Locale
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookAppointmentActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService

    private var backBtn: Button? = null
    private var stepIndicator: TextView? = null
    private var step1Layout: LinearLayout? = null
    private var step2Layout: LinearLayout? = null
    private var step3Layout: LinearLayout? = null

    private var step1NextBtn: Button? = null
    private var serviceGrid: RecyclerView? = null
    private lateinit var serviceAdapter: SpecialistAdapter
    private var selectedService: MockData.Service? = null

    private var step2BackBtn: Button? = null
    private var step2NextBtn: Button? = null
    private var datePickerStep2: EditText? = null
    private var morningSlotsList: RecyclerView? = null
    private var afternoonSlotsList: RecyclerView? = null
    private var selectedDate: String? = null
    private var selectedTimeSlot: String? = null
    private var morningAdapter: TimeSlotAdapter? = null
    private var afternoonAdapter: TimeSlotAdapter? = null

    private var step3BackBtn: Button? = null
    private var step3ConfirmBtn: Button? = null
    private var reasonSpinner: Spinner? = null
    private var notesInput: EditText? = null
    private var summaryServiceText: TextView? = null
    private var summarySpecialistText: TextView? = null
    private var summaryDateText: TextView? = null
    private var summaryTimeText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_book_appointment)
            Log.d(TAG, "BookAppointmentActivity onCreate")
            tokenManager = TokenManager(this)
            apiService = ApiClient.getApiService()

            initializeViews()
            setupListeners()
            setupReasonDropdown()
            loadStep1()
            showStep(1)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            finish()
        }
    }

    private fun initializeViews() {
        backBtn = findViewById(R.id.bookBackBtn)
        stepIndicator = findViewById(R.id.bookStepIndicator)
        step1Layout = findViewById(R.id.bookStep1Layout)
        step2Layout = findViewById(R.id.bookStep2Layout)
        step3Layout = findViewById(R.id.bookStep3Layout)

        step1NextBtn = findViewById(R.id.bookStep1NextBtn)
        serviceGrid = findViewById(R.id.bookStep1ServiceGrid)

        step2BackBtn = findViewById(R.id.bookStep2BackBtn)
        step2NextBtn = findViewById(R.id.bookStep2NextBtn)
        datePickerStep2 = findViewById(R.id.bookStep2DatePicker)
        morningSlotsList = findViewById(R.id.bookStep2MorningSlots)
        afternoonSlotsList = findViewById(R.id.bookStep2AfternoonSlots)

        step3BackBtn = findViewById(R.id.bookStep3BackBtn)
        step3ConfirmBtn = findViewById(R.id.bookStep3ConfirmBtn)
        reasonSpinner = findViewById(R.id.bookStep3Reason)
        notesInput = findViewById(R.id.bookStep3Notes)
        summaryServiceText = findViewById(R.id.bookSummaryService)
        summarySpecialistText = findViewById(R.id.bookSummarySpecialist)
        summaryDateText = findViewById(R.id.bookSummaryDate)
        summaryTimeText = findViewById(R.id.bookSummaryTime)
    }

    private fun setupListeners() {
        backBtn?.setOnClickListener { finish() }

        datePickerStep2?.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker() }
        }

        step1NextBtn?.setOnClickListener {
            if (selectedService == null) {
                showToast("Please select a service")
            } else {
                showStep(2)
            }
        }

        step2BackBtn?.setOnClickListener { showStep(1) }

        step2NextBtn?.setOnClickListener {
            selectedDate = datePickerStep2?.text?.toString()?.trim().orEmpty()
            when {
                selectedDate.isNullOrEmpty() -> showToast("Please select a date")
                selectedTimeSlot.isNullOrEmpty() -> showToast("Please select a time slot")
                else -> showStep(3)
            }
        }

        step3BackBtn?.setOnClickListener { showStep(2) }
        step3ConfirmBtn?.setOnClickListener { confirmBooking() }
    }

    private fun setupReasonDropdown() {
        val spinner = reasonSpinner ?: return
        val adapter = object : ArrayAdapter<CharSequence>(
            this,
            android.R.layout.simple_spinner_item,
            resources.getTextArray(R.array.book_reason_options)
        ) {
            override fun isEnabled(position: Int): Boolean = position != 0
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                datePickerStep2?.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
    }

    private fun loadStep1() {
        val services = MockData.getServices()
        serviceAdapter = SpecialistAdapter(
            this,
            services,
            object : SpecialistAdapter.OnServiceSelectedListener {
                override fun onServiceSelected(service: MockData.Service) {
                    selectedService = service
                }
            }
        )

        serviceGrid?.apply {
            layoutManager = GridLayoutManager(this@BookAppointmentActivity, 2)
            adapter = serviceAdapter
        }
    }

    private fun loadStep2() {
        morningAdapter = TimeSlotAdapter(MockData.getMorningTimeSlots(), object : TimeSlotAdapter.OnTimeSlotSelectedListener {
            override fun onSlotSelected(slot: String) {
                selectedTimeSlot = slot
                syncTimeSlotSelection()
            }
        })

        afternoonAdapter = TimeSlotAdapter(MockData.getAfternoonTimeSlots(), object : TimeSlotAdapter.OnTimeSlotSelectedListener {
            override fun onSlotSelected(slot: String) {
                selectedTimeSlot = slot
                syncTimeSlotSelection()
            }
        })

        morningSlotsList?.apply {
            layoutManager = LinearLayoutManager(this@BookAppointmentActivity)
            adapter = morningAdapter
        }

        afternoonSlotsList?.apply {
            layoutManager = LinearLayoutManager(this@BookAppointmentActivity)
            adapter = afternoonAdapter
        }

        syncTimeSlotSelection()
    }

    private fun syncTimeSlotSelection() {
        morningAdapter?.setSelectedSlot(selectedTimeSlot)
        afternoonAdapter?.setSelectedSlot(selectedTimeSlot)
    }

    private fun showStep(step: Int) {
        step1Layout?.visibility = if (step == 1) LinearLayout.VISIBLE else LinearLayout.GONE
        step2Layout?.visibility = if (step == 2) LinearLayout.VISIBLE else LinearLayout.GONE
        step3Layout?.visibility = if (step == 3) LinearLayout.VISIBLE else LinearLayout.GONE

        stepIndicator?.text = "Step $step/3"

        when (step) {
            2 -> loadStep2()
            3 -> updateSummary()
        }
    }

    private fun updateSummary() {
        summaryServiceText?.text = selectedService?.let { "${it.icon} ${it.name}" } ?: "Not selected"
        summarySpecialistText?.text = selectedService?.specialist ?: "Not selected"
        summaryDateText?.text = selectedDate?.takeUnless { it.isEmpty() } ?: "Not selected"
        summaryTimeText?.text = selectedTimeSlot ?: "Not selected"
    }

    private fun confirmBooking() {
        val reason = if ((reasonSpinner?.selectedItemPosition ?: 0) > 0) {
            reasonSpinner?.selectedItem?.toString().orEmpty()
        } else {
            ""
        }
        val notes = notesInput?.text?.toString()?.trim().orEmpty()

        if (reason.isEmpty()) {
            showToast("Please select a reason for visit")
            return
        }

        val service = selectedService
        val date = selectedDate
        val timeSlot = selectedTimeSlot
        if (service == null || date.isNullOrEmpty() || timeSlot.isNullOrEmpty()) {
            showToast("Please complete your appointment details")
            return
        }

        val authHeader = tokenManager.getAuthorizationHeader()
        if (authHeader == null) {
            showToast("Please sign in again before booking")
            return
        }

        val request = BookAppointmentRequest(
            service.name,
            service.specialist,
            date,
            timeSlot,
            reason,
            notes
        )

        step3ConfirmBtn?.isEnabled = false
        step3ConfirmBtn?.text = "Booking..."
        apiService.bookAppointment(authHeader, request).enqueue(object : Callback<Appointment> {
            override fun onResponse(call: Call<Appointment>, response: Response<Appointment>) {
                step3ConfirmBtn?.isEnabled = true
                step3ConfirmBtn?.text = "Confirm Booking"
                if (!response.isSuccessful || response.body() == null) {
                    showToast("Unable to book appointment right now")
                    return
                }

                showToast("Appointment booked successfully!")
                startActivity(Intent(this@BookAppointmentActivity, MyAppointmentsActivity::class.java))
                finish()
            }

            override fun onFailure(call: Call<Appointment>, t: Throwable) {
                Log.e(TAG, "Failed to book appointment", t)
                step3ConfirmBtn?.isEnabled = true
                step3ConfirmBtn?.text = "Confirm Booking"
                showToast("Unable to connect to the booking service")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 220)
            show()
        }
    }

    class TimeSlotAdapter(
        private val timeSlots: List<String>,
        private val listener: OnTimeSlotSelectedListener?
    ) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {
        private var selectedSlot: String? = null

        interface OnTimeSlotSelectedListener {
            fun onSlotSelected(slot: String)
        }

        fun setSelectedSlot(selectedSlot: String?) {
            this.selectedSlot = selectedSlot
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
            return TimeSlotViewHolder(view)
        }

        override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
            val slot = timeSlots[position]
            val isSelected = slot == selectedSlot
            val isDisabled = slot == LUNCH_BREAK_SLOT
            holder.bind(slot, isSelected, isDisabled)
            holder.itemView.setOnClickListener {
                if (!isDisabled) {
                    listener?.onSlotSelected(slot)
                }
            }
        }

        override fun getItemCount(): Int = timeSlots.size

        class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val timeSlotText: TextView = itemView.findViewById(R.id.timeSlotText)

            fun bind(slot: String, isSelected: Boolean, isDisabled: Boolean) {
                timeSlotText.text = slot

                val background = GradientDrawable().apply {
                    cornerRadius = 24f
                    if (isDisabled) {
                        setStroke(3, Color.parseColor("#D6CCBE"))
                        setColor(Color.parseColor("#F5F2EC"))
                    } else {
                        setStroke(3, if (isSelected) Color.parseColor("#D97706") else Color.parseColor("#E8DDD0"))
                        setColor(if (isSelected) Color.parseColor("#D97706") else Color.WHITE)
                    }
                }

                if (isDisabled) {
                    timeSlotText.alpha = 0.75f
                    timeSlotText.setTextColor(Color.parseColor("#9A8F81"))
                    timeSlotText.text = "$slot (Lunch)"
                } else {
                    timeSlotText.alpha = 1f
                    timeSlotText.setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#1C1408"))
                }

                timeSlotText.background = background
            }
        }
    }

    companion object {
        private const val TAG = "BookAppointmentActivity"
        private const val LUNCH_BREAK_SLOT = "12:00 PM"
    }
}
