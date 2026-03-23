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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminAppointmentAdapter extends RecyclerView.Adapter<AdminAppointmentAdapter.AppointmentViewHolder> {
    public interface OnAdminAppointmentActionListener {
        void onViewDetails(Appointment appointment);
        void onApprove(Appointment appointment);
        void onCancel(Appointment appointment);
        void onMarkDone(Appointment appointment);
    }

    private final Context context;
    private final OnAdminAppointmentActionListener listener;
    private List<Appointment> appointments = new ArrayList<>();

    public AdminAppointmentAdapter(Context context, List<Appointment> appointments, OnAdminAppointmentActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.appointments = appointments != null ? appointments : new ArrayList<>();
    }

    public void updateAppointments(List<Appointment> updatedAppointments) {
        this.appointments = updatedAppointments != null ? updatedAppointments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        holder.bind(appointments.get(position));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder {
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

        AppointmentViewHolder(@NonNull View itemView) {
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

        void bind(Appointment appointment) {
            serviceName.setText(safeText(appointment.getServiceName(), "Hilot Session"));
            specialistName.setText("Patient: " + safeText(appointment.getPatientName(), "Unknown") + " • " + safeText(appointment.getSpecialistName(), "Specialist"));
            dateTime.setText("\uD83D\uDCC5 " + safeText(appointment.getAppointmentDate(), "TBD") + "   \u23F0 " + safeText(appointment.getTimeSlot(), "TBD"));
            reasonText.setText(safeText(appointment.getReason(), "No reason provided."));
            serviceEmoji.setText(getServiceEmoji(appointment.getServiceName()));

            int[] statusStyle = getStatusStyle(appointment.getStatus());
            accentBar.setBackgroundColor(statusStyle[2]);
            statusText.setText(safeText(appointment.getStatus(), "Pending"));
            statusText.setTextColor(statusStyle[1]);
            statusText.setBackground(makeRoundedDrawable(statusStyle[0], 999, 0, statusStyle[0]));
            iconTile.setBackground(makeRoundedDrawable(getServiceTileColor(appointment.getServiceName()), 14, 0, Color.TRANSPARENT));

            configureButtons(appointment);
        }

        private void configureButtons(Appointment appointment) {
            String status = safeText(appointment.getStatus(), "").toLowerCase(Locale.US);
            styleOutlineButton(actionBtn1, "View Details", false);
            actionBtn1.setOnClickListener(v -> listener.onViewDetails(appointment));

            if (status.contains("done") || status.contains("complete")) {
                styleDarkButton(actionBtn2, "Completed", false);
                actionBtn2.setEnabled(false);
                actionBtn3.setVisibility(View.GONE);
                actionSpacer2.setVisibility(View.GONE);
            } else if (status.contains("cancel")) {
                styleOutlineButton(actionBtn2, "Cancelled", true);
                actionBtn2.setEnabled(false);
                actionBtn3.setVisibility(View.GONE);
                actionSpacer2.setVisibility(View.GONE);
            } else if (status.contains("approved")) {
                styleDarkButton(actionBtn2, "Mark Done", true);
                styleOutlineButton(actionBtn3, "Cancel", true);
                actionBtn2.setOnClickListener(v -> listener.onMarkDone(appointment));
                actionBtn3.setOnClickListener(v -> listener.onCancel(appointment));
                actionBtn3.setVisibility(View.VISIBLE);
                actionSpacer2.setVisibility(View.VISIBLE);
            } else {
                styleDarkButton(actionBtn2, "Approve", true);
                styleOutlineButton(actionBtn3, "Cancel", true);
                actionBtn2.setOnClickListener(v -> listener.onApprove(appointment));
                actionBtn3.setOnClickListener(v -> listener.onCancel(appointment));
                actionBtn3.setVisibility(View.VISIBLE);
                actionSpacer2.setVisibility(View.VISIBLE);
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
            String normalized = status.toLowerCase(Locale.US);
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

        private String getServiceEmoji(String serviceName) {
            if (serviceName == null) return "\uD83C\uDF3F";
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
            if (serviceName == null) return Color.parseColor("#FEF3C7");
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

        private String safeText(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
