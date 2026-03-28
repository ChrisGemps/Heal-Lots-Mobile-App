package com.heallots.mobile.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.heallots.mobile.R;
import com.heallots.mobile.api.ApiClient;
import com.heallots.mobile.api.ApiService;
import com.heallots.mobile.models.Appointment;
import com.heallots.mobile.models.User;
import com.heallots.mobile.storage.TokenManager;
import com.heallots.mobile.ui.adapters.AdminAppointmentAdapter;
import com.heallots.mobile.ui.adapters.AdminUserAdapter;
import com.heallots.mobile.utils.Constants;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity implements AdminAppointmentAdapter.OnAdminAppointmentActionListener {
    private static final String TAG = "AdminDashboardActivity";
    private static final List<String> MORNING_SLOTS = Arrays.asList("08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM");
    private static final List<String> AFTERNOON_SLOTS = Arrays.asList("01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM");
    private static final String LUNCH_BREAK_SLOT = "12:00 PM";
    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private TokenManager tokenManager;
    private ApiService apiService;
    private Button overviewTab;
    private Button appointmentsTab;
    private Button usersTab;
    private LinearLayout userBadge;
    private ImageView userAvatar;
    private TextView userNameText;
    private TextView userAvatarText;
    private TextView statTotalApts;
    private TextView statPendingApts;
    private TextView statApprovedApts;
    private TextView statTotalPatients;
    private ScrollView overviewScroll;
    private LinearLayout overviewContent;
    private RecyclerView adminListRecyclerView;

    private AdminAppointmentAdapter appointmentAdapter;
    private AdminUserAdapter userAdapter;
    private final List<Appointment> allAppointments = new ArrayList<>();
    private final List<User> allUsers = new ArrayList<>();
    private String currentTab = "overview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_admin_dashboard);
            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService();
            initializeViews();
            setupRecyclerView();
            displayUserInfo();
            setupListeners();
            loadAdminData();
            selectTab("overview");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUserInfo();
        loadAdminData();
    }

    private void initializeViews() {
        overviewTab = findViewById(R.id.adminOverviewTab);
        appointmentsTab = findViewById(R.id.adminAppointmentsTab);
        usersTab = findViewById(R.id.adminUsersTab);
        userBadge = findViewById(R.id.adminUserBadge);
        userAvatar = findViewById(R.id.adminUserAvatar);
        userNameText = findViewById(R.id.adminUserName);
        userAvatarText = findViewById(R.id.adminUserAvatarText);
        statTotalApts = findViewById(R.id.adminStatTotalAppointments);
        statPendingApts = findViewById(R.id.adminStatCompletedAppointments);
        statApprovedApts = findViewById(R.id.adminStatAverageRating);
        statTotalPatients = findViewById(R.id.adminStatTotalPatients);
        overviewScroll = findViewById(R.id.adminOverviewScroll);
        overviewContent = findViewById(R.id.adminOverviewContent);
        adminListRecyclerView = findViewById(R.id.adminListRecyclerView);
    }

    private void setupRecyclerView() {
        adminListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentAdapter = new AdminAppointmentAdapter(this, allAppointments, this);
        userAdapter = new AdminUserAdapter(this, allUsers);
        adminListRecyclerView.setAdapter(appointmentAdapter);
    }

    private void displayUserInfo() {
        User currentUser = tokenManager.getUser();
        String fullName = currentUser != null ? safeText(currentUser.getFullName(), "Admin") : "Admin";
        userNameText.setText(getFirstName(fullName));
        userAvatarText.setText(fullName.substring(0, 1).toUpperCase(Locale.US));

        String profilePicture = currentUser != null ? currentUser.getProfilePictureUrl() : null;
        if (profilePicture != null && !profilePicture.trim().isEmpty()) {
            String imageUrl = profilePicture.startsWith("http")
                    ? profilePicture
                    : Constants.BASE_URL + "/api/user/profile-picture/" + profilePicture;
            userAvatarText.setVisibility(View.VISIBLE);
            Picasso.get().load(imageUrl).fit().centerCrop().into(userAvatar, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    userAvatarText.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    userAvatar.setImageDrawable(null);
                    userAvatarText.setVisibility(View.VISIBLE);
                }
            });
        } else {
            userAvatar.setImageDrawable(null);
            userAvatarText.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        overviewTab.setOnClickListener(v -> selectTab("overview"));
        appointmentsTab.setOnClickListener(v -> selectTab("appointments"));
        usersTab.setOnClickListener(v -> selectTab("users"));
        userBadge.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadAdminData() {
        loadAppointments();
        loadUsers();
    }

    private void loadAppointments() {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            return;
        }

        apiService.getAllAppointments(authHeader).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Appointment>> call, @NonNull Response<List<Appointment>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                allAppointments.clear();
                allAppointments.addAll(response.body());
                Collections.sort(allAppointments, Comparator.comparingLong(AdminDashboardActivity.this::toTimestamp));
                appointmentAdapter.updateAppointments(allAppointments);
                updateStats();
            }

            @Override
            public void onFailure(@NonNull Call<List<Appointment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load appointments", t);
            }
        });
    }

    private void loadUsers() {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            return;
        }

        apiService.getAllUsers(authHeader).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                allUsers.clear();
                allUsers.addAll(response.body());
                userAdapter.updateUsers(allUsers);
                updateStats();
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load users", t);
            }
        });
    }

    private void updateStats() {
        int totalAppointments = allAppointments.size();
        int pending = 0;
        int approved = 0;
        for (Appointment appointment : allAppointments) {
            String status = safeText(appointment.getStatus(), "").toLowerCase(Locale.US);
            if (status.contains("pending") || status.contains("resched")) {
                pending++;
            }
            if (status.contains("approved")) {
                approved++;
            }
        }

        statTotalApts.setText(String.valueOf(totalAppointments));
        statPendingApts.setText(String.valueOf(pending));
        statApprovedApts.setText(String.valueOf(approved));
        statTotalPatients.setText(String.valueOf(allUsers.size()));
    }

    private void selectTab(String tab) {
        currentTab = tab;
        styleTabButton(overviewTab, tab.equals("overview"));
        styleTabButton(appointmentsTab, tab.equals("appointments"));
        styleTabButton(usersTab, tab.equals("users"));

        if ("overview".equals(tab)) {
            overviewScroll.setVisibility(View.VISIBLE);
            adminListRecyclerView.setVisibility(View.GONE);
        } else if ("appointments".equals(tab)) {
            overviewScroll.setVisibility(View.GONE);
            adminListRecyclerView.setVisibility(View.VISIBLE);
            adminListRecyclerView.setAdapter(appointmentAdapter);
            appointmentAdapter.updateAppointments(allAppointments);
        } else {
            overviewScroll.setVisibility(View.GONE);
            adminListRecyclerView.setVisibility(View.VISIBLE);
            adminListRecyclerView.setAdapter(userAdapter);
            userAdapter.updateUsers(allUsers);
        }
    }

    @Override
    public void onViewDetails(Appointment appointment) {
        showAppointmentDetailsDialog(appointment);
    }

    @Override
    public void onApprove(Appointment appointment) {
        updateAppointmentStatus(appointment, "Approved");
    }

    @Override
    public void onCancel(Appointment appointment) {
        updateAppointmentStatus(appointment, "Cancelled");
    }

    @Override
    public void onMarkDone(Appointment appointment) {
        updateAppointmentStatus(appointment, "Done");
    }

    private void updateAppointmentStatus(Appointment appointment, String status) {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null || appointment.getId() == null) {
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("status", status);

        apiService.updateAppointmentStatus(authHeader, appointment.getId(), request).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(@NonNull Call<Appointment> call, @NonNull Response<Appointment> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                Appointment updated = response.body();
                for (int i = 0; i < allAppointments.size(); i++) {
                    Appointment existing = allAppointments.get(i);
                    if (safeText(existing.getId(), "").equals(safeText(updated.getId(), ""))) {
                        allAppointments.set(i, updated);
                        break;
                    }
                }
                appointmentAdapter.updateAppointments(allAppointments);
                updateStats();
                if (!"appointments".equals(currentTab)) {
                    selectTab(currentTab);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Appointment> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to update appointment status", t);
            }
        });
    }

    private long toTimestamp(Appointment appointment) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                    .parse(safeText(appointment.getAppointmentDate(), "") + " " + safeText(appointment.getTimeSlot(), ""))
                    .getTime();
        } catch (ParseException | NullPointerException e) {
            return Long.MAX_VALUE;
        }
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String getFirstName(String fullName) {
        String value = safeText(fullName, "Admin").trim();
        int firstSpace = value.indexOf(' ');
        return firstSpace > 0 ? value.substring(0, firstSpace) : value;
    }

    private void styleTabButton(Button button, boolean active) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(14));
        if (active) {
            drawable.setColor(Color.parseColor("#FEF3C7"));
            drawable.setStroke(dp(1), Color.parseColor("#F59E0B"));
            button.setTextColor(getColor(R.color.primary_orange));
        } else {
            drawable.setColor(Color.TRANSPARENT);
            drawable.setStroke(dp(1), Color.TRANSPARENT);
            button.setTextColor(getColor(R.color.text_tertiary));
        }
        button.setBackground(drawable);
    }

    private void showAppointmentDetailsDialog(Appointment appointment) {
        View view = getLayoutInflater().inflate(R.layout.dialog_admin_appointment_details, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        User linkedUser = findLinkedUser(appointment);
        String normalizedStatus = safeText(appointment.getStatus(), "").toLowerCase(Locale.US);

        ((TextView) view.findViewById(R.id.adminDetailPatientNameValue)).setText(safeText(appointment.getPatientName(), "Unknown"));
        ((TextView) view.findViewById(R.id.adminDetailEmailValue)).setText(safeText(appointment.getPatientEmail(), linkedUser != null ? linkedUser.getEmail() : "N/A"));
        ((TextView) view.findViewById(R.id.adminDetailPhoneValue)).setText(safeText(appointment.getPatientPhone(), linkedUser != null ? linkedUser.getPhone() : "N/A"));
        ((TextView) view.findViewById(R.id.adminDetailServiceValue)).setText(getServiceWithEmoji(appointment.getServiceName()));
        ((TextView) view.findViewById(R.id.adminDetailSpecialistValue)).setText(safeText(appointment.getSpecialistName(), "Specialist"));
        ((TextView) view.findViewById(R.id.adminDetailDateValue)).setText(safeText(appointment.getAppointmentDate(), "TBD"));
        ((TextView) view.findViewById(R.id.adminDetailTimeValue)).setText(safeText(appointment.getTimeSlot(), "TBD"));
        ((TextView) view.findViewById(R.id.adminDetailStatusValue)).setText(safeText(appointment.getStatus(), "Pending"));
        ((TextView) view.findViewById(R.id.adminDetailReasonValue)).setText(safeText(appointment.getReason(), "No reason provided."));

        LinearLayout notesBox = view.findViewById(R.id.adminDetailNotesBox);
        TextView notesValue = view.findViewById(R.id.adminDetailNotesValue);
        if (appointment.getNotes() != null && !appointment.getNotes().trim().isEmpty()) {
            notesBox.setVisibility(View.VISIBLE);
            notesValue.setText(appointment.getNotes());
        }

        LinearLayout rescheduleBox = view.findViewById(R.id.adminDetailRescheduleBox);
        TextView rescheduleReason = view.findViewById(R.id.adminDetailRescheduleReason);
        if (normalizedStatus.contains("resched") || (appointment.getRescheduleReason() != null && !appointment.getRescheduleReason().trim().isEmpty())) {
            rescheduleBox.setVisibility(View.VISIBLE);
            rescheduleReason.setText(safeText(appointment.getRescheduleReason(), "No reason provided."));
        }

        LinearLayout cancellationBox = view.findViewById(R.id.adminDetailCancellationBox);
        TextView cancellationReason = view.findViewById(R.id.adminDetailCancellationReason);
        if (normalizedStatus.contains("cancel") || (appointment.getCancellationReason() != null && !appointment.getCancellationReason().trim().isEmpty())) {
            cancellationBox.setVisibility(View.VISIBLE);
            cancellationReason.setText(safeText(appointment.getCancellationReason(), "No reason provided."));
        }

        Button headerCloseBtn = view.findViewById(R.id.adminDetailHeaderCloseBtn);
        Button actionBtn1 = view.findViewById(R.id.adminDetailActionBtn1);
        Button actionBtn2 = view.findViewById(R.id.adminDetailActionBtn2);
        Button actionBtn3 = view.findViewById(R.id.adminDetailActionBtn3);
        LinearLayout extraActionsRow = view.findViewById(R.id.adminDetailExtraActionsRow);
        Button actionBtn4 = view.findViewById(R.id.adminDetailActionBtn4);
        Space spacer2 = view.findViewById(R.id.adminDetailActionSpacer2);

        headerCloseBtn.setOnClickListener(v -> dialog.dismiss());
        styleOutlineButton(actionBtn1, "Close", false);
        actionBtn1.setOnClickListener(v -> dialog.dismiss());
        if (extraActionsRow != null) {
            extraActionsRow.setVisibility(View.GONE);
        }
        if (actionBtn4 != null) {
            actionBtn4.setVisibility(View.GONE);
        }

        if (normalizedStatus.contains("pending") || normalizedStatus.contains("resched")) {
            styleOutlineButton(actionBtn2, "Reject", true);
            styleGreenButton(actionBtn3, "Approve");
            actionBtn2.setOnClickListener(v -> {
                showStatusConfirmationDialog(
                        "Reject?",
                        "Reject this appointment for " + safeText(appointment.getPatientName(), "this patient") + "?",
                        "Yes, Reject",
                        () -> {
                            dialog.dismiss();
                            onCancel(appointment);
                        }
                );
            });
            actionBtn3.setOnClickListener(v -> {
                dialog.dismiss();
                onApprove(appointment);
            });
            actionBtn2.setVisibility(View.VISIBLE);
            actionBtn3.setVisibility(View.VISIBLE);
            spacer2.setVisibility(View.VISIBLE);
        } else if (normalizedStatus.contains("approved")) {
            styleOutlineButton(actionBtn2, "Call Off", true);
            styleAmberButton(actionBtn3, "Reschedule");
            if (actionBtn4 != null) {
                styleBlueButton(actionBtn4, "Mark as Done");
            }
            actionBtn2.setOnClickListener(v -> {
                showStatusConfirmationDialog(
                        "Call Off?",
                        "Call off this appointment for " + safeText(appointment.getPatientName(), "this patient") + "?",
                        "Yes, Call Off",
                        () -> {
                            dialog.dismiss();
                            onCancel(appointment);
                        }
                );
            });
            actionBtn3.setOnClickListener(v -> {
                dialog.dismiss();
                showAdminRescheduleDialog(appointment);
            });
            if (actionBtn4 != null) {
                actionBtn4.setOnClickListener(v -> {
                    dialog.dismiss();
                    onMarkDone(appointment);
                });
            }
            actionBtn2.setVisibility(View.VISIBLE);
            actionBtn3.setVisibility(View.VISIBLE);
            if (extraActionsRow != null) {
                extraActionsRow.setVisibility(View.VISIBLE);
            }
            if (actionBtn4 != null) {
                actionBtn4.setVisibility(View.VISIBLE);
            }
            spacer2.setVisibility(View.VISIBLE);
        } else if (normalizedStatus.contains("done") || normalizedStatus.contains("complete")) {
            styleGreenButton(actionBtn2, "Return to Approved");
            actionBtn2.setOnClickListener(v -> {
                dialog.dismiss();
                onApprove(appointment);
            });
            actionBtn2.setVisibility(View.VISIBLE);
            actionBtn3.setVisibility(View.GONE);
            spacer2.setVisibility(View.GONE);
        } else {
            actionBtn2.setVisibility(View.GONE);
            actionBtn3.setVisibility(View.GONE);
            spacer2.setVisibility(View.GONE);
        }

        dialog.show();
        styleDialogWindow(dialog);
    }

    private User findLinkedUser(Appointment appointment) {
        for (User user : allUsers) {
            if (user == null) {
                continue;
            }
            if (safeText(user.getEmail(), "").equalsIgnoreCase(safeText(appointment.getPatientEmail(), ""))) {
                return user;
            }
            if (safeText(user.getFullName(), "").equalsIgnoreCase(safeText(appointment.getPatientName(), ""))) {
                return user;
            }
        }
        return null;
    }

    private String getServiceWithEmoji(String serviceName) {
        String name = safeText(serviceName, "Hilot Session");
        switch (name) {
            case "Traditional Hilot":
                return "🤲🏻 " + name;
            case "Herbal Compress":
                return "🌿 " + name;
            case "Head & Neck Relief":
                return "💆 " + name;
            case "Foot Reflexology":
                return "🦶 " + name;
            case "Hot Oil Massage":
                return "\uD83E\uDED9 " + name;
            case "Whole-Body Hilot":
                return "🧘🏻 " + name;
            default:
                return "🌿 " + name;
        }
    }

    private void styleOutlineButton(Button button, String text, boolean red) {
        button.setText(text);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(1), red ? Color.parseColor("#FECACA") : Color.parseColor("#E8DDD0"));
        button.setBackground(drawable);
        button.setTextColor(red ? Color.parseColor("#DC2626") : Color.parseColor("#44291A"));
    }

    private void styleGreenButton(Button button, String text) {
        button.setText(text);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#22C55E"), Color.parseColor("#15803D")}
        );
        drawable.setCornerRadius(dp(12));
        button.setBackground(drawable);
        button.setTextColor(Color.WHITE);
    }

    private void styleBlueButton(Button button, String text) {
        button.setText(text);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#819DCD"), Color.parseColor("#0034A3")}
        );
        drawable.setCornerRadius(dp(12));
        button.setBackground(drawable);
        button.setTextColor(Color.WHITE);
    }

    private void styleAmberButton(Button button, String text) {
        button.setText(text);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#FEF3C7"), Color.parseColor("#F59E0B")}
        );
        drawable.setCornerRadius(dp(12));
        drawable.setStroke(dp(1), Color.parseColor("#D97706"));
        button.setBackground(drawable);
        button.setTextColor(Color.parseColor("#92400E"));
    }

    private void showAdminRescheduleDialog(Appointment appointment) {
        View view = getLayoutInflater().inflate(R.layout.dialog_reschedule_appointment, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        TextView titleView = view.findViewById(R.id.rescheduleDialogTitle);
        EditText reasonInput = view.findViewById(R.id.rescheduleReasonInput);
        TextView counter = view.findViewById(R.id.rescheduleReasonCounter);
        TextView monthTitle = view.findViewById(R.id.rescheduleMonthTitle);
        Button prevMonthBtn = view.findViewById(R.id.reschedulePrevMonthBtn);
        Button nextMonthBtn = view.findViewById(R.id.rescheduleNextMonthBtn);
        GridLayout calendarHeader = view.findViewById(R.id.rescheduleCalendarHeader);
        GridLayout calendarGrid = view.findViewById(R.id.rescheduleCalendarGrid);
        RecyclerView morningRecycler = view.findViewById(R.id.rescheduleMorningSlots);
        RecyclerView afternoonRecycler = view.findViewById(R.id.rescheduleAfternoonSlots);
        Button confirmBtn = view.findViewById(R.id.rescheduleConfirmBtn);
        Button cancelBtn = view.findViewById(R.id.rescheduleCancelBtn);

        if (titleView != null) {
            titleView.setText("Select New Date & Time");
        }
        if (reasonInput != null) {
            reasonInput.setVisibility(View.GONE);
        }
        if (counter != null) {
            counter.setVisibility(View.GONE);
        }

        final Calendar displayMonth = Calendar.getInstance();
        final int[] selectedDay = {-1};
        final String[] selectedSlot = {""};

        styleAmberButton(confirmBtn, "\u2713 Confirm Reschedule");
        confirmBtn.setEnabled(false);
        confirmBtn.setAlpha(0.5f);
        styleOutlineButton(cancelBtn, "Cancel", false);
        styleOutlineButton(prevMonthBtn, "\u2190", false);
        styleOutlineButton(nextMonthBtn, "\u2192", false);

        buildCalendarHeader(calendarHeader);

        AdminRescheduleSlotAdapter morningAdapter = new AdminRescheduleSlotAdapter(MORNING_SLOTS);
        AdminRescheduleSlotAdapter afternoonAdapter = new AdminRescheduleSlotAdapter(AFTERNOON_SLOTS);
        morningRecycler.setLayoutManager(new LinearLayoutManager(this));
        afternoonRecycler.setLayoutManager(new LinearLayoutManager(this));
        morningRecycler.setAdapter(morningAdapter);
        afternoonRecycler.setAdapter(afternoonAdapter);

        Runnable refreshCalendar = () -> {
            monthTitle.setText(new SimpleDateFormat("MMMM yyyy", Locale.US).format(displayMonth.getTime()));
            buildCalendarDays(calendarGrid, displayMonth, selectedDay, day -> {
                selectedDay[0] = day;
                buildCalendarDays(calendarGrid, displayMonth, selectedDay, clickedDay -> { });
                updateAdminRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0]);
            });
        };
        refreshCalendar.run();

        prevMonthBtn.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, -1);
            selectedDay[0] = -1;
            selectedSlot[0] = "";
            morningAdapter.setSelectedSlot("");
            afternoonAdapter.setSelectedSlot("");
            refreshCalendar.run();
            updateAdminRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0]);
        });
        nextMonthBtn.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, 1);
            selectedDay[0] = -1;
            selectedSlot[0] = "";
            morningAdapter.setSelectedSlot("");
            afternoonAdapter.setSelectedSlot("");
            refreshCalendar.run();
            updateAdminRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0]);
        });

        morningAdapter.setSelectionListener(slot -> {
            selectedSlot[0] = slot;
            afternoonAdapter.setSelectedSlot("");
            updateAdminRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0]);
        });
        afternoonAdapter.setSelectionListener(slot -> {
            selectedSlot[0] = slot;
            morningAdapter.setSelectedSlot("");
            updateAdminRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0]);
        });

        confirmBtn.setOnClickListener(v -> {
            String newDate = String.format(Locale.US, "%04d-%02d-%02d",
                    displayMonth.get(Calendar.YEAR),
                    displayMonth.get(Calendar.MONTH) + 1,
                    selectedDay[0]
            );
            submitAdminReschedule(appointment, newDate, selectedSlot[0], dialog);
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        styleDialogWindow(dialog);
    }

    private void updateAdminRescheduleConfirmState(Button confirmBtn, int selectedDay, String selectedSlot) {
        boolean enabled = selectedDay > 0 && selectedSlot != null && !selectedSlot.isEmpty();
        styleAmberButton(confirmBtn, "\u2713 Confirm Reschedule");
        confirmBtn.setEnabled(enabled);
        confirmBtn.setAlpha(enabled ? 1f : 0.5f);
    }

    private void submitAdminReschedule(Appointment appointment, String newDate, String newTimeSlot, AlertDialog dialog) {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null || appointment.getId() == null) {
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("appointmentDate", newDate);
        request.put("timeSlot", newTimeSlot);
        request.put("status", "Approved");

        apiService.updateAppointment(authHeader, appointment.getId(), request).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(@NonNull Call<Appointment> call, @NonNull Response<Appointment> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                Appointment updated = response.body();
                for (int i = 0; i < allAppointments.size(); i++) {
                    Appointment existing = allAppointments.get(i);
                    if (safeText(existing.getId(), "").equals(safeText(updated.getId(), ""))) {
                        allAppointments.set(i, updated);
                        break;
                    }
                }
                appointmentAdapter.updateAppointments(allAppointments);
                updateStats();
                if (!"appointments".equals(currentTab)) {
                    selectTab(currentTab);
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<Appointment> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to reschedule appointment", t);
            }
        });
    }

    private void showStatusConfirmationDialog(String title, String message, String confirmText, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton(confirmText, (dialog, which) -> {
                    dialog.dismiss();
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                })
                .show();
    }

    private void buildCalendarHeader(GridLayout header) {
        header.removeAllViews();
        for (String dayName : DAY_NAMES) {
            TextView label = new TextView(this);
            label.setText(dayName);
            label.setTextColor(getColor(R.color.text_secondary));
            label.setTextSize(11f);
            label.setGravity(Gravity.CENTER);
            label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(34);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.setMargins(2, 2, 2, 6);
            label.setLayoutParams(params);
            header.addView(label);
        }
    }

    private void buildCalendarDays(GridLayout grid, Calendar displayMonth, int[] selectedDay, DayClickListener listener) {
        grid.removeAllViews();
        Calendar monthCalendar = (Calendar) displayMonth.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < 42; i++) {
            TextView dayView = new TextView(this);
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(13f);
            int dayNumber = i - firstDayOfWeek + 1;
            if (dayNumber > 0 && dayNumber <= daysInMonth) {
                dayView.setText(String.valueOf(dayNumber));
                Calendar candidate = (Calendar) displayMonth.clone();
                candidate.set(Calendar.DAY_OF_MONTH, dayNumber);
                candidate.set(Calendar.HOUR_OF_DAY, 0);
                candidate.set(Calendar.MINUTE, 0);
                candidate.set(Calendar.SECOND, 0);
                candidate.set(Calendar.MILLISECOND, 0);

                boolean isPast = candidate.before(today);
                boolean isSunday = candidate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
                boolean isSelected = selectedDay[0] == dayNumber;

                if (isSelected) {
                    dayView.setBackground(makeGradientDrawable("#D97706", "#B45309", 10));
                    dayView.setTextColor(Color.WHITE);
                    dayView.setTypeface(dayView.getTypeface(), android.graphics.Typeface.BOLD);
                } else if (isPast || isSunday) {
                    dayView.setTextColor(Color.parseColor("#D4C5B0"));
                    dayView.setBackground(makeRoundedRect(Color.TRANSPARENT, 10, 0, Color.TRANSPARENT));
                } else {
                    dayView.setTextColor(Color.parseColor("#44291A"));
                    dayView.setBackground(makeRoundedRect(Color.WHITE, 10, 1, Color.parseColor("#E8DDD0")));
                    dayView.setOnClickListener(v -> {
                        selectedDay[0] = dayNumber;
                        listener.onDayClicked(dayNumber);
                    });
                }
            } else {
                dayView.setText("");
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(34);
            params.height = dp(34);
            params.setMargins(2, 2, 2, 2);
            dayView.setLayoutParams(params);
            grid.addView(dayView);
        }
    }

    private GradientDrawable makeRoundedRect(int fill, int radiusDp, int strokeDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable makeGradientDrawable(String startColor, String endColor, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(startColor), Color.parseColor(endColor)}
        );
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private void styleDialogWindow(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.94f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private interface DayClickListener {
        void onDayClicked(int day);
    }

    private static class AdminRescheduleSlotAdapter extends RecyclerView.Adapter<AdminRescheduleSlotAdapter.SlotViewHolder> {
        private final List<String> slots;
        private String selectedSlot = "";
        private OnSlotSelectedListener selectionListener;

        interface OnSlotSelectedListener {
            void onSelected(String slot);
        }

        AdminRescheduleSlotAdapter(List<String> slots) {
            this.slots = slots;
        }

        void setSelectionListener(OnSlotSelectedListener listener) {
            this.selectionListener = listener;
        }

        void setSelectedSlot(String selectedSlot) {
            this.selectedSlot = selectedSlot;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
            return new SlotViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
            String slot = slots.get(position);
            boolean selected = slot.equals(selectedSlot);
            boolean disabled = LUNCH_BREAK_SLOT.equals(slot);
            holder.bind(slot, selected, disabled);
            holder.itemView.setOnClickListener(v -> {
                if (!disabled && selectionListener != null) {
                    selectedSlot = slot;
                    notifyDataSetChanged();
                    selectionListener.onSelected(slot);
                }
            });
        }

        @Override
        public int getItemCount() {
            return slots.size();
        }

        static class SlotViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            SlotViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.timeSlotText);
            }

            void bind(String slot, boolean selected, boolean disabled) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setCornerRadius(24f);
                if (disabled) {
                    drawable.setColor(Color.parseColor("#F5F5F5"));
                    drawable.setStroke(2, Color.parseColor("#D4C5B0"));
                    textView.setText(slot + " (Lunch)");
                    textView.setTextColor(Color.parseColor("#A8956B"));
                    textView.setAlpha(0.7f);
                } else if (selected) {
                    drawable.setColors(new int[]{Color.parseColor("#FEF3C7"), Color.parseColor("#F59E0B")});
                    drawable.setStroke(2, Color.parseColor("#D97706"));
                    textView.setText(slot);
                    textView.setTextColor(Color.parseColor("#92400E"));
                    textView.setAlpha(1f);
                } else {
                    drawable.setColor(Color.WHITE);
                    drawable.setStroke(2, Color.parseColor("#E8DDD0"));
                    textView.setText(slot);
                    textView.setTextColor(Color.parseColor("#44291A"));
                    textView.setAlpha(1f);
                }
                textView.setBackground(drawable);
            }
        }
    }
}

