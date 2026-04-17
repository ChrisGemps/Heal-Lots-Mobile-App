package com.heallots.mobile.features.appointments.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.models.Appointment
import java.util.Locale

class DashboardAppointmentAdapter(
    private val context: Context,
    appointments: List<Appointment>?,
    private val listener: OnAppointmentClickListener?
) : RecyclerView.Adapter<DashboardAppointmentAdapter.AppointmentViewHolder>() {
    private var appointments: List<Appointment> = appointments ?: ArrayList()

    interface OnAppointmentClickListener {
        fun onAppointmentClick(appointment: Appointment)
    }

    fun updateAppointments(updatedAppointments: List<Appointment>?) {
        appointments = updatedAppointments ?: ArrayList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emoji: TextView = itemView.findViewById(R.id.dashboardAppointmentEmoji)
        private val service: TextView = itemView.findViewById(R.id.dashboardAppointmentService)
        private val specialist: TextView = itemView.findViewById(R.id.dashboardAppointmentSpecialist)
        private val dateTime: TextView = itemView.findViewById(R.id.dashboardAppointmentDateTime)
        private val status: TextView = itemView.findViewById(R.id.dashboardAppointmentStatus)

        fun bind(appointment: Appointment) {
            service.text = safeText(appointment.serviceName, "Hilot Session")
            specialist.text = "with ${safeText(appointment.specialistName, "Specialist")}"
            dateTime.text = "\uD83D\uDCC5 ${safeText(appointment.appointmentDate, "TBD")}   \u23F0 ${safeText(appointment.timeSlot, "TBD")}"
            emoji.text = getEmoji(appointment.serviceName)
            status.text = safeText(appointment.status, "Pending")
            status.background = makeStatusBackground(appointment.status)
            itemView.setOnClickListener { listener?.onAppointmentClick(appointment) }
        }

        private fun makeStatusBackground(statusValue: String?): GradientDrawable {
            return GradientDrawable().apply {
                cornerRadius = 999f
                val status = statusValue?.lowercase(Locale.getDefault()).orEmpty()
                setColor(
                    when {
                        "cancel" in status -> Color.parseColor("#FEE2E2")
                        "done" in status || "complete" in status || "approved" in status -> Color.parseColor("#DCFCE7")
                        "resched" in status -> Color.parseColor("#E0E7FF")
                        else -> Color.parseColor("#FEF3C7")
                    }
                )
            }
        }

        private fun getEmoji(serviceName: String?): String {
            return when (serviceName) {
                "Traditional Hilot" -> "\uD83E\uDD32\uD83C\uDFFB"
                "Herbal Compress" -> "\uD83C\uDF3F"
                "Head & Neck Relief" -> "\uD83D\uDC86"
                "Foot Reflexology" -> "\uD83E\uDDB6"
                "Hot Oil Massage" -> "\uD83E\uDED9"
                "Whole-Body Hilot" -> "\uD83E\uDDD8\uD83C\uDFFB"
                else -> "\uD83C\uDF3F"
            }
        }

        private fun safeText(value: String?, fallback: String): String {
            return if (value.isNullOrBlank()) fallback else value
        }
    }
}
