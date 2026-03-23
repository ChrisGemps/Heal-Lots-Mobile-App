package com.heallots.mobile.ui.activities;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.heallots.mobile.R;
import com.heallots.mobile.api.ApiClient;
import com.heallots.mobile.api.ApiService;
import com.heallots.mobile.models.Appointment;
import com.heallots.mobile.models.BookAppointmentRequest;
import com.heallots.mobile.storage.TokenManager;
import com.heallots.mobile.ui.adapters.SpecialistAdapter;
import com.heallots.mobile.utils.MockData;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookAppointmentActivity extends AppCompatActivity {
    private static final String TAG = "BookAppointmentActivity";
    private static final String LUNCH_BREAK_SLOT = "12:00 PM";
    private TokenManager tokenManager;
    private ApiService apiService;

    private Button backBtn;
    private TextView stepIndicator;
    private LinearLayout step1Layout;
    private LinearLayout step2Layout;
    private LinearLayout step3Layout;

    // Step 1 views
    private Button step1NextBtn;
    private RecyclerView serviceGrid;
    private SpecialistAdapter serviceAdapter;
    private MockData.Service selectedService;

    // Step 2 views
    private Button step2BackBtn;
    private Button step2NextBtn;
    private EditText datePickerStep2;
    private RecyclerView morningSlotsList;
    private RecyclerView afternoonSlotsList;
    private String selectedDate;
    private String selectedTimeSlot;
    private TimeSlotAdapter morningAdapter;
    private TimeSlotAdapter afternoonAdapter;

    // Step 3 views
    private Button step3BackBtn;
    private Button step3ConfirmBtn;
    private Spinner reasonSpinner;
    private EditText notesInput;
    private TextView summaryServiceText;
    private TextView summarySpecialistText;
    private TextView summaryDateText;
    private TextView summaryTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_book_appointment);
            Log.d(TAG, "BookAppointmentActivity onCreate");
            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService();

            initializeViews();
            setupListeners();
            setupReasonDropdown();
            loadStep1();
            showStep(1);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            finish();
        }
    }

    private void initializeViews() {
        backBtn = findViewById(R.id.bookBackBtn);
        stepIndicator = findViewById(R.id.bookStepIndicator);
        step1Layout = findViewById(R.id.bookStep1Layout);
        step2Layout = findViewById(R.id.bookStep2Layout);
        step3Layout = findViewById(R.id.bookStep3Layout);

        // Step 1
        step1NextBtn = findViewById(R.id.bookStep1NextBtn);
        serviceGrid = findViewById(R.id.bookStep1ServiceGrid);

        // Step 2
        step2BackBtn = findViewById(R.id.bookStep2BackBtn);
        step2NextBtn = findViewById(R.id.bookStep2NextBtn);
        datePickerStep2 = findViewById(R.id.bookStep2DatePicker);
        morningSlotsList = findViewById(R.id.bookStep2MorningSlots);
        afternoonSlotsList = findViewById(R.id.bookStep2AfternoonSlots);

        // Step 3
        step3BackBtn = findViewById(R.id.bookStep3BackBtn);
        step3ConfirmBtn = findViewById(R.id.bookStep3ConfirmBtn);
        reasonSpinner = findViewById(R.id.bookStep3Reason);
        notesInput = findViewById(R.id.bookStep3Notes);
        summaryServiceText = findViewById(R.id.bookSummaryService);
        summarySpecialistText = findViewById(R.id.bookSummarySpecialist);
        summaryDateText = findViewById(R.id.bookSummaryDate);
        summaryTimeText = findViewById(R.id.bookSummaryTime);
    }

    private void setupListeners() {
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        if (datePickerStep2 != null) {
            datePickerStep2.setFocusable(false);
            datePickerStep2.setClickable(true);
            datePickerStep2.setOnClickListener(v -> showDatePicker());
        }

        if (step1NextBtn != null) {
            step1NextBtn.setOnClickListener(v -> {
                if (selectedService == null) {
                    showToast("Please select a service");
                } else {
                    showStep(2);
                }
            });
        }

        if (step2BackBtn != null) {
            step2BackBtn.setOnClickListener(v -> showStep(1));
        }

        if (step2NextBtn != null) {
            step2NextBtn.setOnClickListener(v -> {
                selectedDate = datePickerStep2 != null ? datePickerStep2.getText().toString().trim() : "";
                if (selectedDate.isEmpty()) {
                    showToast("Please select a date");
                } else if (selectedTimeSlot == null || selectedTimeSlot.isEmpty()) {
                    showToast("Please select a time slot");
                } else {
                    showStep(3);
                }
            });
        }

        if (step3BackBtn != null) {
            step3BackBtn.setOnClickListener(v -> showStep(2));
        }

        if (step3ConfirmBtn != null) {
            step3ConfirmBtn.setOnClickListener(v -> confirmBooking());
        }
    }

    private void setupReasonDropdown() {
        if (reasonSpinner == null) {
            return;
        }

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getTextArray(R.array.book_reason_options)
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reasonSpinner.setAdapter(adapter);
        reasonSpinner.setSelection(0);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (datePickerStep2 != null) {
                        datePickerStep2.setText(selectedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void loadStep1() {
        List<MockData.Service> services = MockData.getServices();
        serviceAdapter = new SpecialistAdapter(this, services, service -> {
            selectedService = service;
        });

        if (serviceGrid != null) {
            serviceGrid.setLayoutManager(new GridLayoutManager(this, 2));
            serviceGrid.setAdapter(serviceAdapter);
        }
    }

    private void loadStep2() {
        morningAdapter = new TimeSlotAdapter(MockData.getMorningTimeSlots(), slot -> {
            selectedTimeSlot = slot;
            syncTimeSlotSelection();
        });

        afternoonAdapter = new TimeSlotAdapter(MockData.getAfternoonTimeSlots(), slot -> {
            selectedTimeSlot = slot;
            syncTimeSlotSelection();
        });

        if (morningSlotsList != null) {
            morningSlotsList.setLayoutManager(new LinearLayoutManager(this));
            morningSlotsList.setAdapter(morningAdapter);
        }

        if (afternoonSlotsList != null) {
            afternoonSlotsList.setLayoutManager(new LinearLayoutManager(this));
            afternoonSlotsList.setAdapter(afternoonAdapter);
        }

        syncTimeSlotSelection();
    }

    private void syncTimeSlotSelection() {
        if (morningAdapter != null) {
            morningAdapter.setSelectedSlot(selectedTimeSlot);
        }
        if (afternoonAdapter != null) {
            afternoonAdapter.setSelectedSlot(selectedTimeSlot);
        }
    }

    private void showStep(int step) {
        if (step1Layout != null) {
            step1Layout.setVisibility(step == 1 ? LinearLayout.VISIBLE : LinearLayout.GONE);
        }
        if (step2Layout != null) {
            step2Layout.setVisibility(step == 2 ? LinearLayout.VISIBLE : LinearLayout.GONE);
        }
        if (step3Layout != null) {
            step3Layout.setVisibility(step == 3 ? LinearLayout.VISIBLE : LinearLayout.GONE);
        }

        if (stepIndicator != null) {
            stepIndicator.setText("Step " + step + "/3");
        }

        if (step == 2) {
            loadStep2();
        } else if (step == 3) {
            updateSummary();
        }
    }

    private void updateSummary() {
        if (summaryServiceText != null) {
            summaryServiceText.setText(selectedService != null ? selectedService.icon + " " + selectedService.name : "Not selected");
        }
        if (summarySpecialistText != null) {
            summarySpecialistText.setText(selectedService != null ? selectedService.specialist : "Not selected");
        }
        if (summaryDateText != null) {
            summaryDateText.setText(selectedDate != null && !selectedDate.isEmpty() ? selectedDate : "Not selected");
        }
        if (summaryTimeText != null) {
            summaryTimeText.setText(selectedTimeSlot != null ? selectedTimeSlot : "Not selected");
        }
    }

    private void confirmBooking() {
        String reason = reasonSpinner != null && reasonSpinner.getSelectedItemPosition() > 0
                ? String.valueOf(reasonSpinner.getSelectedItem())
                : "";
        String notes = notesInput != null ? notesInput.getText().toString().trim() : "";

        if (reason.isEmpty()) {
            showToast("Please select a reason for visit");
            return;
        }

        if (selectedService == null || selectedDate == null || selectedDate.isEmpty() || selectedTimeSlot == null || selectedTimeSlot.isEmpty()) {
            showToast("Please complete your appointment details");
            return;
        }

        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            showToast("Please sign in again before booking");
            return;
        }

        BookAppointmentRequest request = new BookAppointmentRequest(
                selectedService.name,
                selectedService.specialist,
                selectedDate,
                selectedTimeSlot,
                reason,
                notes
        );

        step3ConfirmBtn.setEnabled(false);
        step3ConfirmBtn.setText("Booking...");
        apiService.bookAppointment(authHeader, request).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(Call<Appointment> call, Response<Appointment> response) {
                step3ConfirmBtn.setEnabled(true);
                step3ConfirmBtn.setText("Confirm Booking");
                if (!response.isSuccessful() || response.body() == null) {
                    showToast("Unable to book appointment right now");
                    return;
                }

                showToast("Appointment booked successfully!");
                startActivity(new android.content.Intent(BookAppointmentActivity.this, MyAppointmentsActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<Appointment> call, Throwable t) {
                Log.e(TAG, "Failed to book appointment", t);
                step3ConfirmBtn.setEnabled(true);
                step3ConfirmBtn.setText("Confirm Booking");
                showToast("Unable to connect to the booking service");
            }
        });
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 220);
        toast.show();
    }

    public static class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {
        private final List<String> timeSlots;
        private final OnTimeSlotSelectedListener listener;
        private String selectedSlot;

        public interface OnTimeSlotSelectedListener {
            void onSlotSelected(String slot);
        }

        public TimeSlotAdapter(List<String> timeSlots, OnTimeSlotSelectedListener listener) {
            this.timeSlots = timeSlots;
            this.listener = listener;
        }

        public void setSelectedSlot(String selectedSlot) {
            this.selectedSlot = selectedSlot;
            notifyDataSetChanged();
        }

        @Override
        public TimeSlotViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_time_slot, parent, false);
            return new TimeSlotViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TimeSlotViewHolder holder, int position) {
            String slot = timeSlots.get(position);
            boolean isSelected = slot.equals(selectedSlot);
            boolean isDisabled = LUNCH_BREAK_SLOT.equals(slot);
            holder.bind(slot, isSelected, isDisabled);
            holder.itemView.setOnClickListener(v -> {
                if (!isDisabled && listener != null) {
                    listener.onSlotSelected(slot);
                }
            });
        }

        @Override
        public int getItemCount() {
            return timeSlots != null ? timeSlots.size() : 0;
        }

        public static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
            private final TextView timeSlotText;

            public TimeSlotViewHolder(android.view.View itemView) {
                super(itemView);
                timeSlotText = itemView.findViewById(R.id.timeSlotText);
            }

            public void bind(String slot, boolean isSelected, boolean isDisabled) {
                if (timeSlotText == null) {
                    return;
                }

                timeSlotText.setText(slot);

                GradientDrawable background = new GradientDrawable();
                background.setCornerRadius(24f);
                if (isDisabled) {
                    background.setStroke(3, Color.parseColor("#D6CCBE"));
                    background.setColor(Color.parseColor("#F5F2EC"));
                    timeSlotText.setAlpha(0.75f);
                    timeSlotText.setTextColor(Color.parseColor("#9A8F81"));
                    timeSlotText.setText(slot + " (Lunch)");
                } else {
                    background.setStroke(3, isSelected ? Color.parseColor("#D97706") : Color.parseColor("#E8DDD0"));
                    background.setColor(isSelected ? Color.parseColor("#D97706") : Color.WHITE);
                    timeSlotText.setAlpha(1f);
                    timeSlotText.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#1C1408"));
                }

                timeSlotText.setBackground(background);
            }
        }
    }
}
