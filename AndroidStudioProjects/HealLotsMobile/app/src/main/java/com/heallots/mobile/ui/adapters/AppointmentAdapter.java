package com.heallots.mobile.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.heallots.mobile.R;
import com.heallots.mobile.models.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private List<Appointment> appointments;
    private final Context context;
    private final OnAppointmentActionListener listener;
    private String currentTab;

    public interface OnAppointmentActionListener {
        void onViewDetails(Appointment appointment);
        void onReschedule(Appointment appointment);
        void onCancel(Appointment appointment);
        void onReview(Appointment appointment);
        void onRebook(Appointment appointment);
        void onFollowUp(Appointment appointment);
    }

    public AppointmentAdapter(Context context, List<Appointment> appointments, String currentTab, OnAppointmentActionListener listener) {
        this.context = context;
        this.appointments = appointments;
        this.currentTab = currentTab;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.bind(appointments.get(position), currentTab);
    }

    @Override
    public int getItemCount() {
        return appointments != null ? appointments.size() : 0;
    }

    public void updateAppointments(List<Appointment> newAppointments, String currentTab) {
        this.appointments = newAppointments;
        this.currentTab = currentTab;
        notifyDataSetChanged();
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final View accentBar;
        private final LinearLayout iconTile;
        private final TextView serviceEmoji;
        private final TextView serviceName;
        private final TextView specialistName;
        private final TextView dateTime;
        private final TextView statusText;
        private final TextView reasonText;
        private final Button actionBtn1;
        private final Button actionBtn2;
        private final Button actionBtn3;
        private final Space actionSpacer2;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            accentBar = itemView.findViewById(R.id.appointmentAccentBar);
            iconTile = itemView.findViewById(R.id.appointmentIconTile);
            serviceEmoji = itemView.findViewById(R.id.appointmentServiceEmoji);
            serviceName = itemView.findViewById(R.id.appointmentServiceName);
            specialistName = itemView.findViewById(R.id.appointmentSpecialistName);
            dateTime = itemView.findViewById(R.id.appointmentDateTime);
            statusText = itemView.findViewById(R.id.appointmentStatus);
            reasonText = itemView.findViewById(R.id.appointmentReasonText);
            actionBtn1 = itemView.findViewById(R.id.appointmentActionBtn1);
            actionBtn2 = itemView.findViewById(R.id.appointmentActionBtn2);
            actionBtn3 = itemView.findViewById(R.id.appointmentActionBtn3);
            actionSpacer2 = itemView.findViewById(R.id.appointmentActionSpacer2);
        }

        public void bind(Appointment appointment, String currentTab) {
            if (serviceName != null) {
                serviceName.setText(appointment.getServiceName());
            }
            if (specialistName != null) {
                specialistName.setText(appointment.getSpecialistName());
            }
            if (dateTime != null) {
                dateTime.setText("\uD83D\uDCC5 " + appointment.getAppointmentDate() + "   \u23F0 " + appointment.getTimeSlot());
            }
            if (reasonText != null) {
                String reason = appointment.getReason();
                reasonText.setText(reason == null || reason.trim().isEmpty() ? "No reason provided." : reason);
            }

            int[] statusStyle = getStatusStyle(appointment.getStatus());
            if (accentBar != null) {
                accentBar.setBackgroundColor(statusStyle[2]);
            }
            if (statusText != null) {
                statusText.setText(toDisplayStatus(appointment.getStatus()));
                statusText.setTextColor(statusStyle[1]);
                statusText.setBackground(makeRoundedDrawable(statusStyle[0], 999, 0, statusStyle[0]));
            }
            if (serviceEmoji != null) {
                serviceEmoji.setText(getServiceEmoji(appointment.getServiceName()));
            }
            if (iconTile != null) {
                iconTile.setBackground(makeRoundedDrawable(getServiceTileColor(appointment.getServiceName()), 14, 0, Color.TRANSPARENT));
            }

            configureButtons(appointment, currentTab);
        }

        private void configureButtons(Appointment appointment, String currentTab) {
            actionBtn1.setVisibility(View.VISIBLE);
            actionBtn2.setVisibility(View.VISIBLE);
            actionBtn3.setVisibility(View.VISIBLE);
            actionSpacer2.setVisibility(View.VISIBLE);

            if ("upcoming".equals(currentTab)) {
                styleDarkButton(actionBtn1, "View Details \u2192", true);
                styleOutlineButton(actionBtn2, "\uD83D\uDD04 Reschedule", false);
                styleOutlineButton(actionBtn3, "\u2715 Cancel Appointment", true);

                actionBtn1.setOnClickListener(v -> listener.onViewDetails(appointment));
                actionBtn2.setOnClickListener(v -> listener.onReschedule(appointment));
                actionBtn3.setOnClickListener(v -> listener.onCancel(appointment));
            } else if ("past".equals(currentTab)) {
                styleOutlineButton(actionBtn1, "\uD83D\uDCC4 View Summary", false);
                actionBtn1.setOnClickListener(v -> listener.onViewDetails(appointment));

                if (appointment.isReviewed()) {
                    styleDarkButton(actionBtn2, "\u2713 Review Submitted", false);
                    actionBtn2.setEnabled(false);
                } else {
                    styleDarkButton(actionBtn2, "\u2B50 Leave Review", true);
                    actionBtn2.setOnClickListener(v -> listener.onReview(appointment));
                }

                styleDarkButton(actionBtn3, "Book Follow-up \u2192", true);
                actionBtn3.setOnClickListener(v -> listener.onFollowUp(appointment));
            } else {
                styleOutlineButton(actionBtn1, "\uD83D\uDCC4 View Details", false);
                styleDarkButton(actionBtn2, "Rebook \u2192", true);
                actionBtn3.setVisibility(View.GONE);
                actionSpacer2.setVisibility(View.GONE);

                actionBtn1.setOnClickListener(v -> listener.onViewDetails(appointment));
                actionBtn2.setOnClickListener(v -> listener.onRebook(appointment));
            }
        }

        private void styleDarkButton(Button button, String text, boolean enabled) {
            button.setText(text);
            button.setEnabled(enabled);
            GradientDrawable drawable = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    enabled
                            ? new int[]{Color.parseColor("#0F172A"), Color.parseColor("#1C1408")}
                            : new int[]{Color.parseColor("#8A847B"), Color.parseColor("#8A847B")}
            );
            drawable.setCornerRadius(12f);
            button.setBackground(drawable);
            button.setTextColor(enabled ? Color.parseColor("#FBBF24") : Color.parseColor("#F5E6B1"));
        }

        private void styleOutlineButton(Button button, String text, boolean red) {
            button.setText(text);
            button.setEnabled(true);
            int stroke = red ? Color.parseColor("#FECACA") : Color.parseColor("#E8DDD0");
            int textColor = red ? Color.parseColor("#DC2626") : Color.parseColor("#B45309");
            button.setBackground(makeRoundedDrawable(Color.WHITE, 12, 2, stroke));
            button.setTextColor(textColor);
        }

        private GradientDrawable makeRoundedDrawable(int fillColor, int radiusDp, int strokeDp, int strokeColor) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(fillColor);
            drawable.setCornerRadius(radiusDp * context.getResources().getDisplayMetrics().density);
            if (strokeDp > 0) {
                drawable.setStroke((int) (strokeDp * context.getResources().getDisplayMetrics().density), strokeColor);
            }
            return drawable;
        }

        private int[] getStatusStyle(String status) {
            if (status == null) {
                return new int[]{Color.parseColor("#E5E7EB"), Color.GRAY, Color.GRAY};
            }
            String normalized = status.toLowerCase();
            if (normalized.contains("cancel")) {
                return new int[]{Color.parseColor("#FEE2E2"), Color.parseColor("#DC2626"), Color.parseColor("#EF4444")};
            }
            if (normalized.contains("done") || normalized.contains("complete") || normalized.contains("approved")) {
                return new int[]{Color.parseColor("#DCFCE7"), Color.parseColor("#15803D"), Color.parseColor("#22C55E")};
            }
            if (normalized.contains("resched")) {
                return new int[]{Color.parseColor("#E0E7FF"), Color.parseColor("#4F46E5"), Color.parseColor("#6366F1")};
            }
            return new int[]{Color.parseColor("#FEF3C7"), Color.parseColor("#B45309"), Color.parseColor("#D97706")};
        }

        private String toDisplayStatus(String status) {
            if (status == null || status.trim().isEmpty()) {
                return "Pending";
            }
            return status;
        }

        private String getServiceEmoji(String serviceName) {
            if (serviceName == null) {
                return "\uD83C\uDF3F";
            }
            switch (serviceName) {
                case "Traditional Hilot":
                    return "\uD83E\uDD32\uD83C\uDFFB";
                case "Herbal Compress":
                    return "\uD83C\uDF3F";
                case "Head & Neck Relief":
                    return "\uD83D\uDC86";
                case "Foot Reflexology":
                    return "\uD83E\uDDB6";
                case "Hot Oil Massage":
                    return "\uD83E\uDED9";
                case "Whole-Body Hilot":
                    return "\uD83E\uDDD8\uD83C\uDFFB";
                default:
                    return "\uD83C\uDF3F";
            }
        }

        private int getServiceTileColor(String serviceName) {
            if (serviceName == null) {
                return Color.parseColor("#FEF3C7");
            }
            switch (serviceName) {
                case "Traditional Hilot":
                    return Color.parseColor("#FEF3C7");
                case "Herbal Compress":
                    return Color.parseColor("#DCFCE7");
                case "Head & Neck Relief":
                    return Color.parseColor("#EDE9FE");
                case "Foot Reflexology":
                    return Color.parseColor("#FCE7F3");
                case "Hot Oil Massage":
                    return Color.parseColor("#FFEDD5");
                case "Whole-Body Hilot":
                    return Color.parseColor("#E0F2FE");
                default:
                    return Color.parseColor("#FEF3C7");
            }
        }
    }
}
