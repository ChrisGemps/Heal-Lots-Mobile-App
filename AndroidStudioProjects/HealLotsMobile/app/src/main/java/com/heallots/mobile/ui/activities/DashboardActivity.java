package com.heallots.mobile.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.heallots.mobile.ui.adapters.DashboardAppointmentAdapter;
import com.heallots.mobile.ui.adapters.ReviewAdapter;
import com.heallots.mobile.utils.Constants;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";

    private TokenManager tokenManager;
    private ApiService apiService;
    private LinearLayout bookCard;
    private LinearLayout appointmentsCard;
    private LinearLayout userBadge;
    private TextView userNameText;
    private TextView userRoleText;
    private TextView greetingText;
    private ImageView userAvatar;
    private TextView avatarText;
    private Button adminBtn;
    private Button viewAllAppointmentsBtn;
    private RecyclerView appointmentsRecycler;
    private LinearLayout appointmentsEmpty;
    private TextView avgRatingText;
    private TextView totalReviewsText;
    private LinearLayout reviewsTabs;
    private RecyclerView reviewsRecycler;
    private LinearLayout reviewsEmpty;

    private DashboardAppointmentAdapter appointmentAdapter;
    private ReviewAdapter reviewAdapter;
    private final List<Appointment> recentAppointments = new ArrayList<>();
    private final List<Review> allReviews = new ArrayList<>();
    private final List<Review> filteredReviews = new ArrayList<>();
    private String selectedReviewTab = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_dashboard);
            tokenManager = new TokenManager(this);
            apiService = ApiClient.getApiService();
            initializeViews();
            setupRecyclerViews();
            setUserInfo();
            setupListeners();
            loadDashboardData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (apiService != null) {
            setUserInfo();
            loadDashboardData();
        }
    }

    private void initializeViews() {
        bookCard = findViewById(R.id.dashboardBookCard);
        appointmentsCard = findViewById(R.id.dashboardAppointmentsCard);
        userBadge = findViewById(R.id.dashboardUserBadge);
        userNameText = findViewById(R.id.dashboardUserName);
        userRoleText = findViewById(R.id.dashboardUserRole);
        greetingText = findViewById(R.id.dashboardGreeting);
        userAvatar = findViewById(R.id.dashboardUserAvatar);
        avatarText = findViewById(R.id.dashboardUserAvatarText);
        adminBtn = findViewById(R.id.dashboardAdminBtn);
        viewAllAppointmentsBtn = findViewById(R.id.dashboardViewAllAppointments);
        appointmentsRecycler = findViewById(R.id.dashboardAppointmentsRecycler);
        appointmentsEmpty = findViewById(R.id.dashboardAppointmentsEmpty);
        avgRatingText = findViewById(R.id.dashboardAvgRating);
        totalReviewsText = findViewById(R.id.dashboardTotalReviews);
        reviewsTabs = findViewById(R.id.dashboardReviewsTabs);
        reviewsRecycler = findViewById(R.id.dashboardReviewsRecycler);
        reviewsEmpty = findViewById(R.id.dashboardReviewsEmpty);
    }

    private void setupRecyclerViews() {
        appointmentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        appointmentAdapter = new DashboardAppointmentAdapter(this, recentAppointments, appointment ->
                startActivity(new Intent(DashboardActivity.this, MyAppointmentsActivity.class)));
        appointmentsRecycler.setAdapter(appointmentAdapter);

        reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(this, filteredReviews);
        reviewsRecycler.setAdapter(reviewAdapter);
    }

    private void setUserInfo() {
        User currentUser = tokenManager.getUser();
        String fullName = currentUser != null ? safeText(currentUser.getFullName(), "User") : "User";
        String role = currentUser != null ? safeText(currentUser.getRole(), "USER") : "USER";
        String firstName = fullName.contains(" ") ? fullName.substring(0, fullName.indexOf(' ')) : fullName;

        greetingText.setText("Welcome back, " + firstName);
        userNameText.setText(fullName);
        userRoleText.setText("ADMIN".equalsIgnoreCase(role) ? "Admin" : "Patient");
        avatarText.setText(fullName.substring(0, 1).toUpperCase(Locale.US));
        adminBtn.setVisibility("ADMIN".equalsIgnoreCase(role) ? View.VISIBLE : View.GONE);
        adminBtn.setBackground(makeTopBarOutline());

        String profilePictureUrl = currentUser != null ? currentUser.getProfilePictureUrl() : null;
        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()) {
            String imageUrl = profilePictureUrl.startsWith("http")
                    ? profilePictureUrl
                    : Constants.BASE_URL + "/api/user/profile-picture/" + profilePictureUrl;
            avatarText.setVisibility(View.VISIBLE);
            Picasso.get().load(imageUrl).fit().centerCrop().into(userAvatar, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    avatarText.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    userAvatar.setImageDrawable(null);
                    avatarText.setVisibility(View.VISIBLE);
                }
            });
        } else {
            userAvatar.setImageDrawable(null);
            avatarText.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        bookCard.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, BookAppointmentActivity.class)));
        appointmentsCard.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MyAppointmentsActivity.class)));
        viewAllAppointmentsBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, MyAppointmentsActivity.class)));
        userBadge.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
        adminBtn.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, AdminDashboardActivity.class)));
    }

    private void loadDashboardData() {
        loadRecentAppointments();
        loadReviews();
    }

    private void loadRecentAppointments() {
        String authHeader = tokenManager.getAuthorizationHeader();
        if (authHeader == null) {
            renderAppointments(new ArrayList<>());
            return;
        }

        apiService.getUserAppointments(authHeader).enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Appointment>> call, @NonNull Response<List<Appointment>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    renderAppointments(new ArrayList<>());
                    return;
                }

                List<Appointment> appointments = new ArrayList<>(response.body());
                Collections.sort(appointments, Comparator.comparingLong(DashboardActivity.this::toTimestamp));
                List<Appointment> upcoming = new ArrayList<>();
                for (Appointment appointment : appointments) {
                    String status = safeText(appointment.getStatus(), "").toLowerCase(Locale.US);
                    if (!status.contains("cancel") && !status.contains("done") && !status.contains("complete")) {
                        upcoming.add(appointment);
                    }
                    if (upcoming.size() == 3) {
                        break;
                    }
                }
                renderAppointments(upcoming);
            }

            @Override
            public void onFailure(@NonNull Call<List<Appointment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load dashboard appointments", t);
                renderAppointments(new ArrayList<>());
            }
        });
    }

    private void renderAppointments(List<Appointment> appointments) {
        recentAppointments.clear();
        recentAppointments.addAll(appointments);
        appointmentAdapter.updateAppointments(recentAppointments);
        boolean empty = recentAppointments.isEmpty();
        appointmentsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        appointmentsRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void loadReviews() {
        apiService.getAllReviews().enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(@NonNull Call<List<Review>> call, @NonNull Response<List<Review>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    renderReviews(new ArrayList<>());
                    return;
                }
                renderReviews(new ArrayList<>(response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load reviews", t);
                renderReviews(new ArrayList<>());
            }
        });
    }

    private void renderReviews(List<Review> reviews) {
        Collections.sort(reviews, (left, right) -> Long.compare(getReviewTimestamp(right), getReviewTimestamp(left)));
        allReviews.clear();
        allReviews.addAll(reviews);

        totalReviewsText.setText(String.valueOf(allReviews.size()));
        avgRatingText.setText(String.format(Locale.US, "%.1f", calculateAverageRating(allReviews)));

        buildReviewTabs();
        filterReviews(selectedReviewTab);
    }

    private void buildReviewTabs() {
        reviewsTabs.removeAllViews();
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("All", allReviews.size());
        for (Review review : allReviews) {
            String key = safeText(review.getServiceName(), "Other");
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Button tab = new Button(this);
            tab.setAllCaps(false);
            tab.setMinHeight(0);
            tab.setMinimumHeight(0);
            tab.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            tab.setPadding(dp(14), dp(8), dp(14), dp(8));
            tab.setText(entry.getKey() + " (" + entry.getValue() + ")");
            tab.setTextSize(12f);
            tab.setTextColor(entry.getKey().equals(selectedReviewTab) ? getColor(R.color.primary_orange) : getColor(R.color.text_tertiary));
            tab.setOnClickListener(v -> {
                selectedReviewTab = entry.getKey();
                buildReviewTabs();
                filterReviews(selectedReviewTab);
            });
            reviewsTabs.addView(tab);
        }
    }

    private void filterReviews(String tab) {
        filteredReviews.clear();
        if ("All".equals(tab)) {
            filteredReviews.addAll(allReviews);
        } else {
            for (Review review : allReviews) {
                if (tab.equals(safeText(review.getServiceName(), "Other"))) {
                    filteredReviews.add(review);
                }
            }
        }

        reviewAdapter.updateReviews(filteredReviews);
        boolean empty = filteredReviews.isEmpty();
        reviewsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        reviewsRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private double calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0d;
        }

        int total = 0;
        for (Review review : reviews) {
            total += Math.max(0, review.getRating());
        }
        return (double) total / reviews.size();
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private long getReviewTimestamp(Review review) {
        String createdAt = review != null ? safeText(review.getCreatedAt(), "") : "";
        if (createdAt.isEmpty()) {
            return Long.MIN_VALUE;
        }

        List<SimpleDateFormat> formats = new ArrayList<>();
        formats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US));
        formats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US));
        formats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US));

        for (SimpleDateFormat format : formats) {
            try {
                Date parsed = format.parse(createdAt);
                if (parsed != null) {
                    return parsed.getTime();
                }
            } catch (ParseException ignored) {
            }
        }

        return Long.MIN_VALUE;
    }

    private GradientDrawable makeTopBarOutline() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(dp(999));
        drawable.setStroke(dp(1), Color.parseColor("#FBBF24"));
        return drawable;
    }
}
