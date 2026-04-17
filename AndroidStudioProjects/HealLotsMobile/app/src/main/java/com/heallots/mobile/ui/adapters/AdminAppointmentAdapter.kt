package com.heallots.mobile.features.admin.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.models.Appointment
import java.util.Locale

class AdminAppointmentAdapter(
    private val context: Context,
    appointments: List<Appointment>?,
    private val listener: OnAdminAppointmentActionListener
) : RecyclerView.Adapter<AdminAppointmentAdapter.AppointmentViewHolder>() {

    interface OnAdminAppointmentActionListener {
        fun onViewDetails(appointment: Appointment)
        fun onApprove(appointment: Appointment)
        fun onCancel(appointment: Appointment)
        fun onMarkDone(appointment: Appointment)
    }

    private var appointments: List<Appointment> = appointments ?: ArrayList()

    fun updateAppointments(updatedAppointments: List<Appointment>?) {
        appointments = updatedAppointments ?: ArrayList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val accentBar: View = itemView.findViewById(R.id.appointmentAccentBar)
        private val iconTile: LinearLayout = itemView.findViewById(R.id.appointmentIconTile)
        private val serviceEmoji: TextView = itemView.findViewById(R.id.appointmentServiceEmoji)
        private val serviceName: TextView = itemView.findViewById(R.id.appointmentServiceName)
        private val specialistName: TextView = itemView.findViewById(R.id.appointmentSpecialistName)
        private val dateTime: TextView = itemView.findViewById(R.id.appointmentDateTime)
        private val statusText: TextView = itemView.findViewById(R.id.appointmentStatus)
        private val reasonText: TextView = itemView.findViewById(R.id.appointmentReasonText)
        private val actionBtn1: Button = itemView.findViewById(R.id.appointmentActionBtn1)
        private val actionBtn2: Button = itemView.findViewById(R.id.appointmentActionBtn2)
        private val actionBtn3: Button = itemView.findViewById(R.id.appointmentActionBtn3)
        private val actionSpacer2: Space = itemView.findViewById(R.id.appointmentActionSpacer2)

        fun bind(appointment: Appointment) {
            serviceName.text = safeText(appointment.serviceName, "Hilot Session")
            specialistName.text =
                "Patient: ${safeText(appointment.patientName, "Unknown")} • ${safeText(appointment.specialistName, "Specialist")}"
            dateTime.text = "\uD83D\uDCC5 ${safeText(appointment.appointmentDate, "TBD")}   \u23F0 ${safeText(appointment.timeSlot, "TBD")}"
            reasonText.text = safeText(appointment.reason, "No reason provided.")
            serviceEmoji.text = getServiceEmoji(appointment.serviceName)

            val statusStyle = getStatusStyle(appointment.status)
            accentBar.setBackgroundColor(statusStyle[2])
            statusText.text = safeText(appointment.status, "Pending")
            statusText.setTextColor(statusStyle[1])
            statusText.background = makeRoundedDrawable(statusStyle[0], 999, 0, statusStyle[0])
            iconTile.background = makeRoundedDrawable(getServiceTileColor(appointment.serviceName), 14, 0, Color.TRANSPARENT)

            configureButtons(appointment)
        }

        private fun configureButtons(appointment: Appointment) {
            val status = safeText(appointment.status, "").lowercase(Locale.US)
            styleOutlineButton(actionBtn1, "View Details", false)
            actionBtn1.setOnClickListener { listener.onViewDetails(appointment) }

            when {
                status.contains("done") || status.contains("complete") -> {
                    styleDarkButton(actionBtn2, "Completed", false)
                    actionBtn2.isEnabled = false
                    actionBtn3.visibility = View.GONE
                    actionSpacer2.visibility = View.GONE
                }
                status.contains("cancel") -> {
                    styleOutlineButton(actionBtn2, "Cancelled", true)
                    actionBtn2.isEnabled = false
                    actionBtn3.visibility = View.GONE
                    actionSpacer2.visibility = View.GONE
                }
                status.contains("approved") -> {
                    styleDarkButton(actionBtn2, "Mark Done", true)
                    styleOutlineButton(actionBtn3, "Cancel", true)
                    actionBtn2.setOnClickListener { listener.onMarkDone(appointment) }
                    actionBtn3.setOnClickListener { listener.onCancel(appointment) }
                    actionBtn3.visibility = View.VISIBLE
                    actionSpacer2.visibility = View.VISIBLE
                }
                else -> {
                    styleDarkButton(actionBtn2, "Approve", true)
                    styleOutlineButton(actionBtn3, "Cancel", true)
                    actionBtn2.setOnClickListener { listener.onApprove(appointment) }
                    actionBtn3.setOnClickListener { listener.onCancel(appointment) }
                    actionBtn3.visibility = View.VISIBLE
                    actionSpacer2.visibility = View.VISIBLE
                }
            }
        }

        private fun styleDarkButton(button: Button, text: String, enabled: Boolean) {
            button.text = text
            button.isEnabled = enabled
            val drawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                if (enabled) {
                    intArrayOf(Color.parseColor("#0F172A"), Color.parseColor("#1C1408"))
                } else {
                    intArrayOf(Color.parseColor("#8A847B"), Color.parseColor("#8A847B"))
                }
            )
            drawable.cornerRadius = 12f
            button.background = drawable
            button.setTextColor(if (enabled) Color.parseColor("#FBBF24") else Color.parseColor("#F5E6B1"))
        }

        private fun styleOutlineButton(button: Button, text: String, red: Boolean) {
            button.text = text
            button.isEnabled = true
            val stroke = if (red) Color.parseColor("#FECACA") else Color.parseColor("#E8DDD0")
            val textColor = if (red) Color.parseColor("#DC2626") else Color.parseColor("#B45309")
            button.background = makeRoundedDrawable(Color.WHITE, 12, 2, stroke)
            button.setTextColor(textColor)
        }

        private fun makeRoundedDrawable(fillColor: Int, radiusDp: Int, strokeDp: Int, strokeColor: Int): GradientDrawable {
            return GradientDrawable().apply {
                setColor(fillColor)
                cornerRadius = radiusDp * context.resources.displayMetrics.density
                if (strokeDp > 0) {
                    setStroke((strokeDp * context.resources.displayMetrics.density).toInt(), strokeColor)
                }
            }
        }

        private fun getStatusStyle(status: String?): IntArray {
            if (status == null) {
                return intArrayOf(Color.parseColor("#E5E7EB"), Color.GRAY, Color.GRAY)
            }
            val normalized = status.lowercase(Locale.US)
            return when {
                normalized.contains("cancel") -> intArrayOf(Color.parseColor("#FEE2E2"), Color.parseColor("#DC2626"), Color.parseColor("#EF4444"))
                normalized.contains("done") || normalized.contains("complete") || normalized.contains("approved") ->
                    intArrayOf(Color.parseColor("#DCFCE7"), Color.parseColor("#15803D"), Color.parseColor("#22C55E"))
                normalized.contains("resched") -> intArrayOf(Color.parseColor("#E0E7FF"), Color.parseColor("#4F46E5"), Color.parseColor("#6366F1"))
                else -> intArrayOf(Color.parseColor("#FEF3C7"), Color.parseColor("#B45309"), Color.parseColor("#D97706"))
            }
        }

        private fun getServiceEmoji(serviceName: String?): String {
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

        private fun getServiceTileColor(serviceName: String?): Int {
            return when (serviceName) {
                "Traditional Hilot" -> Color.parseColor("#FEF3C7")
                "Herbal Compress" -> Color.parseColor("#DCFCE7")
                "Head & Neck Relief" -> Color.parseColor("#EDE9FE")
                "Foot Reflexology" -> Color.parseColor("#FCE7F3")
                "Hot Oil Massage" -> Color.parseColor("#FFEDD5")
                "Whole-Body Hilot" -> Color.parseColor("#E0F2FE")
                else -> Color.parseColor("#FEF3C7")
            }
        }

        private fun safeText(value: String?, fallback: String): String {
            return if (value.isNullOrBlank()) fallback else value
        }
    }
}
