package com.heallots.mobile.models;

public class Review {
    private String id;
    private String appointmentId;
    private String specialistName;
    private String serviceName;
    private int rating;
    private String reviewText;
    private String patientName;
    private String patientEmail;
    private String patientProfilePictureUrl;
    private String createdAt;

    public Review() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getSpecialistName() { return specialistName; }
    public void setSpecialistName(String specialistName) { this.specialistName = specialistName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public String getPatientProfilePictureUrl() { return patientProfilePictureUrl; }
    public void setPatientProfilePictureUrl(String patientProfilePictureUrl) { this.patientProfilePictureUrl = patientProfilePictureUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
