package com.heallots.mobile.models;

public class User {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String birthday;
    private String gender;
    private String address;
    private String role;
    private String phoneNumber;
    private String profilePictureUrl;
    private String createdAt;

    public User() {}

    public User(String email, String fullName, String role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone != null ? phone : phoneNumber; }
    public void setPhone(String phone) {
        this.phone = phone;
        this.phoneNumber = phone;
    }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhoneNumber() { return phoneNumber != null ? phoneNumber : phone; }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.phone = phoneNumber;
    }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
