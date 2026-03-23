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
import com.heallots.mobile.models.Appointment;

import java.util.ArrayList;
import java.util.List;

public class DashboardAppointmentAdapter extends RecyclerView.Adapter<DashboardAppointmentAdapter.AppointmentViewHolder> {
    private final Context context;
    private final OnAppointmentClickListener listener;
    private List<Appointment> appointments = new ArrayList<>();

    public interface OnAppointmentClickListener {
        void onAppointmentClick(Appointment appointment);
    }

    public DashboardAppointmentAdapter(Context context, List<Appointment> appointments, OnAppointmentClickListener listener) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_appointment, parent, false);
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
        private final TextView emoji;
        private final TextView service;
        private final TextView specialist;
        private final TextView dateTime;
        private final TextView status;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.dashboardAppointmentEmoji);
            service = itemView.findViewById(R.id.dashboardAppointmentService);
            specialist = itemView.findViewById(R.id.dashboardAppointmentSpecialist);
            dateTime = itemView.findViewById(R.id.dashboardAppointmentDateTime);
            status = itemView.findViewById(R.id.dashboardAppointmentStatus);
        }

        void bind(Appointment appointment) {
            service.setText(safeText(appointment.getServiceName(), "Hilot Session"));
            specialist.setText("with " + safeText(appointment.getSpecialistName(), "Specialist"));
            dateTime.setText("\uD83D\uDCC5 " + safeText(appointment.getAppointmentDate(), "TBD") + "   \u23F0 " + safeText(appointment.getTimeSlot(), "TBD"));
            emoji.setText(getEmoji(appointment.getServiceName()));
            status.setText(safeText(appointment.getStatus(), "Pending"));
            status.setBackground(makeStatusBackground(appointment.getStatus()));
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAppointmentClick(appointment);
                }
            });
        }

        private GradientDrawable makeStatusBackground(String statusValue) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(999f);
            String status = statusValue == null ? "" : statusValue.toLowerCase();
            if (status.contains("cancel")) {
                drawable.setColor(Color.parseColor("#FEE2E2"));
            } else if (status.contains("done") || status.contains("complete") || status.contains("approved")) {
                drawable.setColor(Color.parseColor("#DCFCE7"));
            } else if (status.contains("resched")) {
                drawable.setColor(Color.parseColor("#E0E7FF"));
            } else {
                drawable.setColor(Color.parseColor("#FEF3C7"));
            }
            return drawable;
        }

        private String getEmoji(String serviceName) {
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

        private String safeText(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
