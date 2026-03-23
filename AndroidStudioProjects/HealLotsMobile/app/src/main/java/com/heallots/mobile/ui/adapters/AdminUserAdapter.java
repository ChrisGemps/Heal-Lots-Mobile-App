package com.heallots.mobile.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.heallots.mobile.R;
import com.heallots.mobile.models.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

    private final Context context;
    private List<User> users = new ArrayList<>();

    public AdminUserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users != null ? users : new ArrayList<>();
    }

    public void updateUsers(List<User> updatedUsers) {
        this.users = updatedUsers != null ? updatedUsers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView avatar;
        private final TextView name;
        private final TextView email;
        private final TextView phone;
        private final TextView role;
        private final TextView joined;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.adminUserItemAvatar);
            name = itemView.findViewById(R.id.adminUserItemName);
            email = itemView.findViewById(R.id.adminUserItemEmail);
            phone = itemView.findViewById(R.id.adminUserItemPhone);
            role = itemView.findViewById(R.id.adminUserItemRole);
            joined = itemView.findViewById(R.id.adminUserItemJoined);
        }

        void bind(User user) {
            String fullName = safeText(user.getFullName(), "User");
            name.setText(fullName);
            avatar.setText(fullName.substring(0, 1).toUpperCase());
            email.setText(safeText(user.getEmail(), "No email"));
            phone.setText(safeText(user.getPhone(), "No phone"));
            role.setText(safeText(user.getRole(), "USER").toUpperCase());
            role.setBackground(makeRoleBackground(user.getRole()));
            joined.setText("Joined: " + formatJoinedDate(user.getCreatedAt()));
        }

        private GradientDrawable makeRoleBackground(String roleValue) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(999f);
            if (roleValue != null && roleValue.equalsIgnoreCase("ADMIN")) {
                drawable.setColor(Color.parseColor("#FEF3C7"));
            } else {
                drawable.setColor(Color.parseColor("#E0F2FE"));
            }
            return drawable;
        }

        private String safeText(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }

        private String formatJoinedDate(String rawValue) {
            String value = safeText(rawValue, "Unknown");
            if ("Unknown".equals(value)) {
                return value;
            }

            try {
                return LocalDate.parse(value).format(DISPLAY_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }

            try {
                return OffsetDateTime.parse(value).toLocalDate().format(DISPLAY_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }

            try {
                return Instant.parse(value)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DISPLAY_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
            }

            int separatorIndex = value.indexOf('T');
            if (separatorIndex > 0) {
                try {
                    return LocalDate.parse(value.substring(0, separatorIndex)).format(DISPLAY_DATE_FORMAT);
                } catch (DateTimeParseException ignored) {
                }
            }

            if (value.length() >= 10) {
                try {
                    return LocalDate.parse(value.substring(0, 10)).format(DISPLAY_DATE_FORMAT);
                } catch (DateTimeParseException ignored) {
                }
            }

            return value;
        }
    }
}
