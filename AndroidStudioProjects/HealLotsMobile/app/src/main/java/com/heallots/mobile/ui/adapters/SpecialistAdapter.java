package com.heallots.mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.heallots.mobile.R;
import com.heallots.mobile.utils.MockData;
import java.util.List;

public class SpecialistAdapter extends RecyclerView.Adapter<SpecialistAdapter.ServiceViewHolder> {
    private List<MockData.Service> services;
    private Context context;
    private OnServiceSelectedListener listener;
    private int selectedPosition = -1;
    
    public interface OnServiceSelectedListener {
        void onServiceSelected(MockData.Service service);
    }
    
    public SpecialistAdapter(Context context, List<MockData.Service> services, OnServiceSelectedListener listener) {
        this.context = context;
        this.services = services;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        MockData.Service service = services.get(position);
        holder.bind(service, position == selectedPosition);
        holder.itemView.setOnClickListener(v -> {
            int prevSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(prevSelected);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onServiceSelected(service);
        });
    }
    
    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }
    
    public class ServiceViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout serviceCard;
        private TextView serviceIcon, serviceName, serviceSpecialist, serviceDescription;
        
        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceCard = itemView.findViewById(R.id.serviceCard);
            serviceIcon = itemView.findViewById(R.id.serviceIcon);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceSpecialist = itemView.findViewById(R.id.serviceSpecialist);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
        }
        
        public void bind(MockData.Service service, boolean isSelected) {
            if (serviceIcon != null) serviceIcon.setText(service.icon);
            if (serviceName != null) serviceName.setText(service.name);
            if (serviceSpecialist != null) serviceSpecialist.setText("with " + service.specialist);
            if (serviceDescription != null) serviceDescription.setText(service.description);
            
            if (serviceCard != null) {
                int bgColor = isSelected ? android.graphics.Color.parseColor("#FFF3E0") : android.graphics.Color.WHITE;
                serviceCard.setBackgroundColor(bgColor);
            }
        }
    }
}

