package com.heallots.mobile.models;

public class BookAppointmentRequest {
    private String serviceName;
    private String specialistName;
    private String appointmentDate;
    private String timeSlot;
    private String reason;
    private String notes;

    public BookAppointmentRequest() {}

    public BookAppointmentRequest(String serviceName, String specialistName, String appointmentDate, String timeSlot, String reason, String notes) {
        this.serviceName = serviceName;
        this.specialistName = specialistName;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.reason = reason;
        this.notes = notes;
    }

    // Getters and Setters
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getSpecialistName() { return specialistName; }
    public void setSpecialistName(String specialistName) { this.specialistName = specialistName; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
