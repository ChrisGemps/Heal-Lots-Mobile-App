package com.heallots.mobile.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.heallots.mobile.R;
import com.heallots.mobile.api.ApiClient;
import com.heallots.mobile.api.ApiService;
import com.heallots.mobile.models.Appointment;
import com.heallots.mobile.models.Review;
import com.heallots.mobile.models.User;
import com.heallots.mobile.storage.TokenManager;
import com.heallots.mobile.ui.adapters.AppointmentAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAppointmentsActivity extends AppCompatActivity implements AppointmentAdapter.OnAppointmentActionListener {
    private static final String TAG = "MyAppointmentsActivity";
    private static final List<String> MORNING_SLOTS = Arrays.asList("08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM");
    private static final List<String> AFTERNOON_SLOTS = Arrays.asList("01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM");
    private static final String LUNCH_BREAK_SLOT = "12:00 PM";
    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private Button backBtn;
    private Button emptyActionBtn;
    private LinearLayout upcomingTab;
    private LinearLayout pastTab;
    private LinearLayout cancelledTab;
    private TextView upcomingTabLabel;
    private TextView pastTabLabel;
    private TextView cancelledTabLabel;
    private TextView upcomingTabBadge;
    private TextView pastTabBadge;
    private TextView cancelledTabBadge;
    private TextView upcomingCountText;
    private TextView pastCountText;
    private TextView cancelledCountText;
    private LinearLayout emptyState;
    private TextView emptyTitle;
    private TextView emptySubtitle;
    private RecyclerView appointmentsRecyclerView;
    private LinearLayout notificationBanner;
    private TextView notificationIcon;
    private TextView notificationMessage;
    private TextView notificationClose;

    private AppointmentAdapter appointmentAdapter;
    private TokenManager tokenManager;
    private ApiService apiService;
    private String currentTab = "upcoming";
    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Appointment> upcomingAppointments = new ArrayList<>();
    private List<Appointment> pastAppointments = new ArrayList<>();
    private List<Appointment> cancelledAppointments = new ArrayList<>();
    private String patientName = "Patient";
    private String patientEmail = "";
    private Runnable notificationDismissRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_my_appointments);
            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService();
            initializeViews();
            setupRecyclerView();
            loadPatientName();
            loadAppointments();
            setupListeners();
            selectTab("upcoming");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager != null && apiService != null) {
            loadAppointments();
        }
    }

    private void initializeViews() {
        backBtn = findViewById(R.id.myApptsBackBtn);
        emptyActionBtn = findViewById(R.id.myApptsEmptyActionBtn);
        upcomingTab = findViewById(R.id.myApptsUpcomingTab);
        pastTab = findViewById(R.id.myAptsPastTab);
        cancelledTab = findViewById(R.id.myAptsCancelledTab);
        upcomingTabLabel = findViewById(R.id.myApptsUpcomingTabLabel);
        pastTabLabel = findViewById(R.id.myApptsPastTabLabel);
        cancelledTabLabel = findViewById(R.id.myApptsCancelledTabLabel);
        upcomingTabBadge = findViewById(R.id.myApptsUpcomingTabBadge);
        pastTabBadge = findViewById(R.id.myApptsPastTabBadge);
        cancelledTabBadge = findViewById(R.id.myApptsCancelledTabBadge);
        upcomingCountText = findViewById(R.id.myApptsUpcomingCount);
        pastCountText = findViewById(R.id.myApptsPastCount);
        cancelledCountText = findViewById(R.id.myApptsCancelledCount);
        emptyState = findViewById(R.id.myApptsEmptyState);
        emptyTitle = findViewById(R.id.myApptsEmptyTitle);
        emptySubtitle = findViewById(R.id.myApptsEmptySubtitle);
        appointmentsRecyclerView = findViewById(R.id.myApptsRecyclerView);
        notificationBanner = findViewById(R.id.myApptsNotification);
        notificationIcon = findViewById(R.id.myApptsNotificationIcon);
        notificationMessage = findViewById(R.id.myApptsNotificationMessage);
        notificationClose = findViewById(R.id.myApptsNotificationClose);
    }

    private void setupRecyclerView() {
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentAdapter = new AppointmentAdapter(this, new ArrayList<>(), currentTab, this);
        appointmentsRecyclerView.setAdapter(appointmentAdapter);
    }

    private void loadPatientName() {
        User currentUser = tokenManager.getUser();
        if (currentUser != null) {
            patientName = safeText(currentUser.getFullName(), patientName);
            patientEmail = safeText(currentUser.getEmail(), "");
            return;
        }
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        patientName = userPrefs.getString("fullName", "Patient");
        patientEmail = userPrefs.getString("email", "");
    }

    private void loadAppointments() {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            showNotification("Please sign in again to load your appointments.", false);
            updateAppointmentBuckets(new ArrayList<>());
            return;
        }

        apiService.getUserAppointments(authHeader).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Appointment>> call, @NonNull Response<List<Appointment>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Failed to load appointments. Code: " + response.code());
                    showNotification("We couldn't load your appointments right now.", false);
                    updateAppointmentBuckets(new ArrayList<>());
                    return;
                }

                List<Appointment> appointments = new ArrayList<>(response.body());
                updateAppointmentBuckets(appointments);
                syncReviewStates();
            }

            @Override
            public void onFailure(@NonNull Call<List<Appointment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load appointments", t);
                showNotification("We couldn't reach the server for your appointments.", false);
                updateAppointmentBuckets(new ArrayList<>());
            }
        });
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> finish());
        emptyActionBtn.setOnClickListener(v -> startActivity(new Intent(this, BookAppointmentActivity.class)));
        upcomingTab.setOnClickListener(v -> selectTab("upcoming"));
        pastTab.setOnClickListener(v -> selectTab("past"));
        cancelledTab.setOnClickListener(v -> selectTab("cancelled"));
        notificationClose.setOnClickListener(v -> hideNotification());
        notificationBanner.setOnTouchListener((v, event) -> {
            if (notificationDismissRunnable != null) {
                HANDLER.removeCallbacks(notificationDismissRunnable);
                scheduleNotificationDismiss();
            }
            return false;
        });
    }

    private void refreshCounts() {
        upcomingCountText.setText(String.valueOf(upcomingAppointments.size()));
        pastCountText.setText(String.valueOf(pastAppointments.size()));
        cancelledCountText.setText(String.valueOf(cancelledAppointments.size()));
        upcomingTabBadge.setText(String.valueOf(upcomingAppointments.size()));
        pastTabBadge.setText(String.valueOf(pastAppointments.size()));
        cancelledTabBadge.setText(String.valueOf(cancelledAppointments.size()));
    }

    private void selectTab(String tab) {
        currentTab = tab;
        styleTab(upcomingTab, upcomingTabLabel, upcomingTabBadge, "upcoming".equals(tab));
        styleTab(pastTab, pastTabLabel, pastTabBadge, "past".equals(tab));
        styleTab(cancelledTab, cancelledTabLabel, cancelledTabBadge, "cancelled".equals(tab));
        List<Appointment> currentAppointments = getCurrentTabAppointments();
        appointmentAdapter.updateAppointments(currentAppointments, currentTab);
        updateEmptyState(currentAppointments.isEmpty());
    }

    private List<Appointment> getCurrentTabAppointments() {
        switch (currentTab) {
            case "past":
                return pastAppointments;
            case "cancelled":
                return cancelledAppointments;
            default:
                return upcomingAppointments;
        }
    }

    private void updateAppointmentBuckets(List<Appointment> appointments) {
        allAppointments = new ArrayList<>(appointments);
        upcomingAppointments = new ArrayList<>();
        pastAppointments = new ArrayList<>();
        cancelledAppointments = new ArrayList<>();

        for (Appointment appointment : allAppointments) {
            if (appointment == null) {
                continue;
            }

            String normalizedStatus = normalizeStatus(appointment.getStatus());
            if (isCancelledStatus(normalizedStatus)) {
                cancelledAppointments.add(appointment);
            } else if (isPastStatus(normalizedStatus)) {
                pastAppointments.add(appointment);
            } else {
                upcomingAppointments.add(appointment);
            }
        }

        sortAppointments(upcomingAppointments, true);
        sortAppointments(pastAppointments, false);
        sortAppointments(cancelledAppointments, false);
        refreshCounts();
        selectTab(currentTab);
    }

    private void sortAppointments(List<Appointment> appointments, boolean ascending) {
        Comparator<Appointment> comparator = Comparator.comparingLong(this::getAppointmentTimestamp);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        Collections.sort(appointments, comparator);
    }

    private long getAppointmentTimestamp(Appointment appointment) {
        if (appointment == null) {
            return Long.MAX_VALUE;
        }

        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
                    .parse(safeText(appointment.getAppointmentDate(), "") + " " + safeText(appointment.getTimeSlot(), ""));
            if (parsed != null) {
                return parsed.getTime();
            }
        } catch (ParseException e) {
            Log.w(TAG, "Failed to sort appointment date for " + appointment.getId(), e);
        }
        return Long.MAX_VALUE;
    }

    private void syncReviewStates() {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            return;
        }

        for (Appointment appointment : pastAppointments) {
            if (appointment == null || appointment.getId() == null || appointment.getId().trim().isEmpty()) {
                continue;
            }

            apiService.checkAppointmentReviewed(authHeader, appointment.getId()).enqueue(new Callback<Map<String, Boolean>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, Boolean>> call, @NonNull Response<Map<String, Boolean>> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        return;
                    }
                    Boolean reviewed = response.body().get("reviewed");
                    appointment.setReviewed(Boolean.TRUE.equals(reviewed));
                    if ("past".equals(currentTab)) {
                        appointmentAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, Boolean>> call, @NonNull Throwable t) {
                    Log.w(TAG, "Failed to check review status for appointment " + appointment.getId(), t);
                }
            });
        }
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toLowerCase(Locale.US);
    }

    private boolean isCancelledStatus(String normalizedStatus) {
        return normalizedStatus.contains("cancel") || normalizedStatus.contains("reject") || normalizedStatus.contains("declin");
    }

    private boolean isPastStatus(String normalizedStatus) {
        return normalizedStatus.contains("done") || normalizedStatus.contains("complete");
    }

    private void applyAppointmentUpdate(Appointment updatedAppointment) {
        if (updatedAppointment == null) {
            return;
        }

        boolean replaced = false;
        for (int i = 0; i < allAppointments.size(); i++) {
            Appointment existing = allAppointments.get(i);
            if (existing != null && safeText(existing.getId(), "").equals(safeText(updatedAppointment.getId(), ""))) {
                allAppointments.set(i, updatedAppointment);
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            allAppointments.add(updatedAppointment);
        }

        updateAppointmentBuckets(allAppointments);
    }

    private void submitCancellation(Appointment appointment, String reason, AlertDialog dialog) {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null || appointment.getId() == null) {
            showNotification("Please sign in again to manage this appointment.", false);
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("status", "Cancelled");
        request.put("cancellationReason", reason);

        apiService.updateAppointmentStatus(authHeader, appointment.getId(), request).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(@NonNull Call<Appointment> call, @NonNull Response<Appointment> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showNotification("We couldn't cancel this appointment right now.", false);
                    return;
                }

                applyAppointmentUpdate(response.body());
                dialog.dismiss();
                selectTab("cancelled");
                showNotification("Appointment cancelled successfully.", true);
            }

            @Override
            public void onFailure(@NonNull Call<Appointment> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to cancel appointment", t);
                showNotification("We couldn't cancel this appointment right now.", false);
            }
        });
    }

    private void submitReview(Appointment appointment, int rating, String feedback, AlertDialog dialog) {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null || appointment.getId() == null) {
            showNotification("Please sign in again to submit your review.", false);
            return;
        }

        Review review = new Review();
        review.setAppointmentId(appointment.getId());
        review.setSpecialistName(appointment.getSpecialistName());
        review.setServiceName(appointment.getServiceName());
        review.setRating(rating);
        review.setReviewText(feedback);
        review.setPatientName(patientName);
        review.setPatientEmail(patientEmail);

        apiService.submitReview(authHeader, review).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (!response.isSuccessful()) {
                    showNotification("We couldn't submit your review right now.", false);
                    return;
                }

                appointment.setReviewed(true);
                appointmentAdapter.notifyDataSetChanged();
                dialog.dismiss();
                showNotification("Thank you for your review.", true);
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to submit review", t);
                showNotification("We couldn't submit your review right now.", false);
            }
        });
    }

    private void submitReschedule(Appointment appointment, String newDate, String newTimeSlot, String reason, AlertDialog dialog) {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null || appointment.getId() == null) {
            showNotification("Please sign in again to reschedule this appointment.", false);
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("appointmentDate", newDate);
        request.put("timeSlot", newTimeSlot);
        request.put("rescheduleReason", reason);
        request.put("status", "Rescheduled");

        apiService.updateAppointment(authHeader, appointment.getId(), request).enqueue(new Callback<Appointment>() {
            @Override
            public void onResponse(@NonNull Call<Appointment> call, @NonNull Response<Appointment> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showNotification("We couldn't reschedule this appointment right now.", false);
                    return;
                }

                applyAppointmentUpdate(response.body());
                dialog.dismiss();
                selectTab("upcoming");
                showNotification("Appointment rescheduled successfully.", true);
            }

            @Override
            public void onFailure(@NonNull Call<Appointment> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to reschedule appointment", t);
                showNotification("We couldn't reschedule this appointment right now.", false);
            }
        });
    }

    private void updateEmptyState(boolean empty) {
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        appointmentsRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if ("upcoming".equals(currentTab)) {
            emptyTitle.setText("No upcoming sessions");
            emptySubtitle.setText("You are all clear for now. Book a fresh hilot session anytime.");
            emptyActionBtn.setVisibility(View.VISIBLE);
        } else if ("past".equals(currentTab)) {
            emptyTitle.setText("No past appointments");
            emptySubtitle.setText("Completed sessions will show up here once you have one.");
            emptyActionBtn.setVisibility(View.GONE);
        } else {
            emptyTitle.setText("No cancelled appointments");
            emptySubtitle.setText("Cancelled or declined sessions will appear here.");
            emptyActionBtn.setVisibility(View.GONE);
        }
    }

    private void styleTab(LinearLayout tab, TextView label, TextView badge, boolean active) {
        if (active) {
            GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.parseColor("#0F172A"), Color.parseColor("#1C1408")});
            bg.setCornerRadius(dp(14));
            tab.setBackground(bg);
            label.setTextColor(Color.parseColor("#FBBF24"));
            badge.setTextColor(Color.parseColor("#FBBF24"));
            badge.setBackground(makePill(Color.parseColor("#2A2116")));
        } else {
            tab.setBackgroundColor(Color.TRANSPARENT);
            label.setTextColor(getColor(R.color.text_tertiary));
            badge.setTextColor(getColor(R.color.primary_orange));
            badge.setBackground(makePill(Color.parseColor("#FEF3C7")));
        }
    }

    private GradientDrawable makePill(int fill) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(999));
        return drawable;
    }

    @Override
    public void onViewDetails(Appointment appointment) {
        showDetailsDialog(appointment);
    }

    @Override
    public void onReschedule(Appointment appointment) {
        if (!canModifyAppointment(appointment)) {
            showClinicContactDialog("Reschedule not available", "This appointment is within 24 hours. Please contact the clinic directly if you need to reschedule.");
            return;
        }
        showRescheduleDialog(appointment);
    }

    @Override
    public void onCancel(Appointment appointment) {
        if (!canModifyAppointment(appointment)) {
            showClinicContactDialog("Cancel not available", "This appointment is within 24 hours. Please contact the clinic directly if you need to cancel.");
            return;
        }
        showCancelDialog(appointment);
    }

    @Override
    public void onReview(Appointment appointment) {
        showReviewDialog(appointment);
    }

    @Override
    public void onRebook(Appointment appointment) {
        startActivity(new Intent(this, BookAppointmentActivity.class));
    }

    @Override
    public void onFollowUp(Appointment appointment) {
        startActivity(new Intent(this, BookAppointmentActivity.class));
    }

    private void showDetailsDialog(Appointment appointment) {
        View view = getLayoutInflater().inflate(R.layout.dialog_appointment_details, null);
        AlertDialog dialog = createDialog(view);

        ((TextView) view.findViewById(R.id.detailServiceValue)).setText(appointment.getServiceName());
        ((TextView) view.findViewById(R.id.detailSpecialistValue)).setText(appointment.getSpecialistName());
        ((TextView) view.findViewById(R.id.detailDateValue)).setText(appointment.getAppointmentDate());
        ((TextView) view.findViewById(R.id.detailTimeValue)).setText(appointment.getTimeSlot());
        ((TextView) view.findViewById(R.id.detailStatusValue)).setText(appointment.getStatus());
        ((TextView) view.findViewById(R.id.detailPatientValue)).setText(safeText(appointment.getPatientName(), patientName));
        ((TextView) view.findViewById(R.id.detailReasonValue)).setText(safeText(appointment.getReason(), "No reason provided."));

        LinearLayout rescheduleBox = view.findViewById(R.id.detailRescheduleBox);
        LinearLayout cancellationBox = view.findViewById(R.id.detailCancellationBox);
        TextView rescheduleReason = view.findViewById(R.id.detailRescheduleReason);
        TextView cancellationReason = view.findViewById(R.id.detailCancellationReason);

        if (appointment.getRescheduleReason() != null && !appointment.getRescheduleReason().trim().isEmpty()) {
            rescheduleBox.setVisibility(View.VISIBLE);
            rescheduleReason.setText(appointment.getRescheduleReason());
        }
        if (appointment.getCancellationReason() != null && !appointment.getCancellationReason().trim().isEmpty()) {
            cancellationBox.setVisibility(View.VISIBLE);
            cancellationReason.setText(appointment.getCancellationReason());
        }

        Button btn1 = view.findViewById(R.id.detailActionBtn1);
        Button btn2 = view.findViewById(R.id.detailActionBtn2);
        Button btn3 = view.findViewById(R.id.detailActionBtn3);
        Space spacer2 = view.findViewById(R.id.detailActionSpacer2);
        Button closeBtn = view.findViewById(R.id.detailCloseBtn);

        closeBtn.setBackground(makeOutlineDrawable(Color.WHITE, Color.parseColor("#E8DDD0")));
        closeBtn.setTextColor(Color.parseColor("#44291A"));
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        if ("upcoming".equals(currentTab)) {
            styleDarkActionButton(btn1, "View Details →", true);
            styleOutlineActionButton(btn2, "🔄 Reschedule", false);
            styleOutlineActionButton(btn3, "✕ Cancel Appointment", true);
            btn2.setOnClickListener(v -> {
                dialog.dismiss();
                onReschedule(appointment);
            });
            btn3.setOnClickListener(v -> {
                dialog.dismiss();
                onCancel(appointment);
            });
        } else if ("past".equals(currentTab)) {
            styleOutlineActionButton(btn1, "📄 View Summary", false);
            styleDarkActionButton(btn2, appointment.isReviewed() ? "✓ Review Submitted" : "⭐ Leave Review", !appointment.isReviewed());
            styleDarkActionButton(btn3, "Book Follow-up →", true);
            btn2.setOnClickListener(v -> {
                if (!appointment.isReviewed()) {
                    dialog.dismiss();
                    onReview(appointment);
                }
            });
            btn3.setOnClickListener(v -> {
                dialog.dismiss();
                onFollowUp(appointment);
            });
        } else {
            styleOutlineActionButton(btn1, "📄 View Details", false);
            styleDarkActionButton(btn2, "Rebook →", true);
            btn3.setVisibility(View.GONE);
            spacer2.setVisibility(View.GONE);
            btn2.setOnClickListener(v -> {
                dialog.dismiss();
                onRebook(appointment);
            });
        }

        dialog.show();
        styleDialogWindow(dialog);
    }

    private void showCancelDialog(Appointment appointment) {
        View view = getLayoutInflater().inflate(R.layout.dialog_cancel_appointment, null);
        AlertDialog dialog = createDialog(view);
        EditText reasonInput = view.findViewById(R.id.cancelReasonInput);
        TextView counter = view.findViewById(R.id.cancelReasonCounter);
        Button confirmBtn = view.findViewById(R.id.cancelConfirmBtn);
        Button dismissBtn = view.findViewById(R.id.cancelDismissBtn);

        styleOutlineActionButton(confirmBtn, "✕ Yes, Cancel Appointment", true);
        styleOutlineActionButton(dismissBtn, "Keep Appointment", false);
        confirmBtn.setEnabled(false);
        confirmBtn.setAlpha(0.5f);

        reasonInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s != null ? s.length() : 0;
                counter.setText(length + "/200");
                boolean enabled = length > 0;
                confirmBtn.setEnabled(enabled);
                confirmBtn.setAlpha(enabled ? 1f : 0.5f);
            }
        });

        confirmBtn.setOnClickListener(v -> {
            submitCancellation(appointment, reasonInput.getText().toString().trim(), dialog);
        });
        dismissBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        styleDialogWindow(dialog);
    }

    private void showReviewDialog(Appointment appointment) {
        View view = getLayoutInflater().inflate(R.layout.dialog_review_appointment, null);
        AlertDialog dialog = createDialog(view);

        TextView serviceText = view.findViewById(R.id.reviewServiceText);
        TextView specialistText = view.findViewById(R.id.reviewSpecialistText);
        LinearLayout banner = view.findViewById(R.id.reviewBanner);
        TextView ratingLabel = view.findViewById(R.id.reviewRatingLabel);
        EditText feedbackInput = view.findViewById(R.id.reviewFeedbackInput);
        TextView counter = view.findViewById(R.id.reviewCounter);
        Button submitBtn = view.findViewById(R.id.reviewSubmitBtn);
        Button cancelBtn = view.findViewById(R.id.reviewCancelBtn);

        serviceText.setText(appointment.getServiceName());
        specialistText.setText(appointment.getSpecialistName());
        styleDarkActionButton(submitBtn, "✓ Submit Review", false);
        styleOutlineActionButton(cancelBtn, "Cancel", false);
        submitBtn.setEnabled(false);
        submitBtn.setAlpha(0.5f);

        TextView[] stars = {
                view.findViewById(R.id.reviewStar1),
                view.findViewById(R.id.reviewStar2),
                view.findViewById(R.id.reviewStar3),
                view.findViewById(R.id.reviewStar4),
                view.findViewById(R.id.reviewStar5)
        };
        final int[] rating = {0};

        if (appointment.isReviewed()) {
            banner.setVisibility(View.VISIBLE);
            feedbackInput.setEnabled(false);
            submitBtn.setEnabled(false);
            submitBtn.setAlpha(0.5f);
            ratingLabel.setText("Review already submitted");
            updateStars(stars, 5);
        } else {
            for (int i = 0; i < stars.length; i++) {
                final int value = i + 1;
                stars[i].setOnClickListener(v -> {
                    rating[0] = value;
                    updateStars(stars, rating[0]);
                    ratingLabel.setText(getRatingLabel(rating[0]));
                    styleDarkActionButton(submitBtn, "✓ Submit Review", true);
                    submitBtn.setEnabled(true);
                    submitBtn.setAlpha(1f);
                });
            }
        }

        feedbackInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int length = s != null ? s.length() : 0;
                counter.setText(length + "/500");
            }
        });

        submitBtn.setOnClickListener(v -> {
            submitReview(appointment, rating[0], feedbackInput.getText().toString().trim(), dialog);
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        styleDialogWindow(dialog);
    }

    private void showRescheduleDialog(Appointment appointment) {
        View view = getLayoutInflater().inflate(R.layout.dialog_reschedule_appointment, null);
        AlertDialog dialog = createDialog(view);

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

        final Calendar displayMonth = Calendar.getInstance();
        final int[] selectedDay = {-1};
        final String[] selectedSlot = {""};

        styleDarkActionButton(confirmBtn, "✓ Confirm Reschedule", false);
        confirmBtn.setEnabled(false);
        confirmBtn.setAlpha(0.5f);
        styleOutlineActionButton(cancelBtn, "Cancel", false);
        styleNavButton(prevMonthBtn);
        styleNavButton(nextMonthBtn);

        reasonInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                counter.setText((s == null ? 0 : s.length()) + "/200");
                updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
            }
        });

        buildCalendarHeader(calendarHeader);

        RescheduleSlotAdapter morningAdapter = new RescheduleSlotAdapter(MORNING_SLOTS, slot -> {
            selectedSlot[0] = slot;
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
        });
        RescheduleSlotAdapter afternoonAdapter = new RescheduleSlotAdapter(AFTERNOON_SLOTS, slot -> {
            selectedSlot[0] = slot;
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
        });
        morningRecycler.setLayoutManager(new LinearLayoutManager(this));
        afternoonRecycler.setLayoutManager(new LinearLayoutManager(this));
        morningRecycler.setAdapter(morningAdapter);
        afternoonRecycler.setAdapter(afternoonAdapter);

        Runnable[] refreshCalendar = new Runnable[1];
        refreshCalendar[0] = () -> {
            monthTitle.setText(new SimpleDateFormat("MMMM yyyy", Locale.US).format(displayMonth.getTime()));
            buildCalendarDays(calendarGrid, displayMonth, selectedDay, day -> {
                selectedDay[0] = day;
                buildCalendarDays(calendarGrid, displayMonth, selectedDay, clickedDay -> {});
                updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
            });
        };
        refreshCalendar[0].run();

        prevMonthBtn.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, -1);
            selectedDay[0] = -1;
            selectedSlot[0] = "";
            morningAdapter.setSelectedSlot("");
            afternoonAdapter.setSelectedSlot("");
            refreshCalendar[0].run();
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
        });
        nextMonthBtn.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, 1);
            selectedDay[0] = -1;
            selectedSlot[0] = "";
            morningAdapter.setSelectedSlot("");
            afternoonAdapter.setSelectedSlot("");
            refreshCalendar[0].run();
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
        });

        morningAdapter.setSelectionListener(slot -> {
            selectedSlot[0] = slot;
            afternoonAdapter.setSelectedSlot("");
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
        });
        afternoonAdapter.setSelectionListener(slot -> {
            selectedSlot[0] = slot;
            morningAdapter.setSelectedSlot("");
            updateRescheduleConfirmState(confirmBtn, selectedDay[0], selectedSlot[0], reasonInput.getText().toString().trim());
        });

        confirmBtn.setOnClickListener(v -> {
            String newDate = String.format(Locale.US, "%04d-%02d-%02d",
                    displayMonth.get(Calendar.YEAR),
                    displayMonth.get(Calendar.MONTH) + 1,
                    selectedDay[0]
            );
            submitReschedule(appointment, newDate, selectedSlot[0], reasonInput.getText().toString().trim(), dialog);
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        styleDialogWindow(dialog);
    }

    private void updateRescheduleConfirmState(Button confirmBtn, int selectedDay, String selectedSlot, String reason) {
        boolean enabled = selectedDay > 0 && selectedSlot != null && !selectedSlot.isEmpty() && reason != null && !reason.isEmpty();
        styleDarkActionButton(confirmBtn, "✓ Confirm Reschedule", enabled);
        confirmBtn.setEnabled(enabled);
        confirmBtn.setAlpha(enabled ? 1f : 0.5f);
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

    private boolean canModifyAppointment(Appointment appointment) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);
            Date appointmentDate = format.parse(appointment.getAppointmentDate() + " " + appointment.getTimeSlot());
            if (appointmentDate == null) {
                return true;
            }
            long diffMillis = appointmentDate.getTime() - System.currentTimeMillis();
            long hours = diffMillis / (1000 * 60 * 60);
            return hours >= 24;
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse appointment date", e);
            return true;
        }
    }

    private void showNotification(String message, boolean success) {
        notificationBanner.setBackgroundResource(success ? R.drawable.my_appts_notification_success : R.drawable.my_appts_notification_error);
        notificationIcon.setText(success ? "✓" : "⚠");
        notificationMessage.setText(message);
        notificationBanner.setTranslationY(-30f);
        notificationBanner.setAlpha(0f);
        notificationBanner.setVisibility(View.VISIBLE);
        notificationBanner.animate().translationY(0f).alpha(1f).setDuration(220).start();
        scheduleNotificationDismiss();
    }

    private void scheduleNotificationDismiss() {
        if (notificationDismissRunnable != null) {
            HANDLER.removeCallbacks(notificationDismissRunnable);
        }
        notificationDismissRunnable = this::hideNotification;
        HANDLER.postDelayed(notificationDismissRunnable, 5000);
    }

    private void hideNotification() {
        notificationBanner.animate().translationY(-20f).alpha(0f).setDuration(180).withEndAction(() -> notificationBanner.setVisibility(View.GONE)).start();
    }

    private AlertDialog createDialog(View view) {
        return new AlertDialog.Builder(this).setView(view).create();
    }

    private void showClinicContactDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void styleDialogWindow(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.94f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void styleDarkActionButton(Button button, String text, boolean enabled) {
        button.setText(text);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                enabled ? new int[]{Color.parseColor("#0F172A"), Color.parseColor("#1C1408")} : new int[]{Color.parseColor("#8A847B"), Color.parseColor("#8A847B")}
        );
        drawable.setCornerRadius(dp(12));
        button.setBackground(drawable);
        button.setTextColor(enabled ? Color.parseColor("#FBBF24") : Color.parseColor("#F5E6B1"));
    }

    private void styleOutlineActionButton(Button button, String text, boolean red) {
        button.setText(text);
        button.setBackground(makeOutlineDrawable(Color.WHITE, red ? Color.parseColor("#FECACA") : Color.parseColor("#E8DDD0")));
        button.setTextColor(red ? Color.parseColor("#DC2626") : Color.parseColor("#44291A"));
    }

    private void styleNavButton(Button button) {
        button.setBackground(makeOutlineDrawable(Color.WHITE, Color.parseColor("#E8DDD0")));
        button.setTextColor(Color.parseColor("#78716C"));
    }

    private GradientDrawable makeOutlineDrawable(int fill, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(10));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
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
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.parseColor(startColor), Color.parseColor(endColor)});
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private void updateStars(TextView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setTextColor(i < rating ? Color.parseColor("#D97706") : Color.parseColor("#D1C6B2"));
            stars[i].setScaleX(i < rating ? 1.1f : 1f);
            stars[i].setScaleY(i < rating ? 1.1f : 1f);
        }
    }

    private String getRatingLabel(int rating) {
        switch (rating) {
            case 1:
                return "Poor";
            case 2:
                return "Fair";
            case 3:
                return "Good";
            case 4:
                return "Very Good";
            case 5:
                return "Excellent";
            default:
                return "Tap a star to rate";
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private interface DayClickListener {
        void onDayClicked(int day);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private static class RescheduleSlotAdapter extends RecyclerView.Adapter<RescheduleSlotAdapter.SlotViewHolder> {
        private final List<String> slots;
        private String selectedSlot = "";
        private OnSlotSelectedListener selectionListener;

        interface OnSlotSelectedListener {
            void onSelected(String slot);
        }

        RescheduleSlotAdapter(List<String> slots, OnSlotSelectedListener listener) {
            this.slots = slots;
            this.selectionListener = listener;
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
                    drawable.setColors(new int[]{Color.parseColor("#D97706"), Color.parseColor("#B45309")});
                    textView.setText(slot);
                    textView.setTextColor(Color.WHITE);
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
