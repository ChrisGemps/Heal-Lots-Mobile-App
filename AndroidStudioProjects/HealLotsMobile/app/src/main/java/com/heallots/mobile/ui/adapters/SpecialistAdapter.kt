package com.heallots.mobile.features.appointments.book

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.utils.MockData

class SpecialistAdapter(
    private val context: Context,
    private val services: List<MockData.Service>?,
    private val listener: OnServiceSelectedListener?
) : RecyclerView.Adapter<SpecialistAdapter.ServiceViewHolder>() {
    private var selectedPosition = -1

    interface OnServiceSelectedListener {
        fun onServiceSelected(service: MockData.Service)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services?.get(position) ?: return
        holder.bind(service, position == selectedPosition)
        holder.itemView.setOnClickListener {
            val prevSelected = selectedPosition
            selectedPosition = position
            if (prevSelected >= 0) {
                notifyItemChanged(prevSelected)
            }
            notifyItemChanged(selectedPosition)
            listener?.onServiceSelected(service)
        }
    }

    override fun getItemCount(): Int = services?.size ?: 0

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceCard: LinearLayout? = itemView.findViewById(R.id.serviceCard)
        private val serviceIcon: TextView? = itemView.findViewById(R.id.serviceIcon)
        private val serviceName: TextView? = itemView.findViewById(R.id.serviceName)
        private val serviceSpecialist: TextView? = itemView.findViewById(R.id.serviceSpecialist)
        private val serviceDescription: TextView? = itemView.findViewById(R.id.serviceDescription)

        fun bind(service: MockData.Service, isSelected: Boolean) {
            serviceIcon?.text = service.icon
            serviceName?.text = service.name
            serviceSpecialist?.text = "with ${service.specialist}"
            serviceDescription?.text = service.description

            serviceCard?.setBackgroundColor(
                if (isSelected) Color.parseColor("#FFF3E0") else Color.WHITE
            )
        }
    }
}
