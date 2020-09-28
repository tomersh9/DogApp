package com.example.dogapp.Enteties;

public class Review {

    private String userID;
    private String fullName;
    private String location;
    private String profileUrl;
    private String timeStamp;
    private String description;
    private Integer rateNumber;

    public Review() {
    }

    public Review(String userID, String fullName, String location, String profileUrl, String timeStamp, String description, Integer rateNumber) {
        this.userID = userID;
        this.fullName = fullName;
        this.location = location;
        this.profileUrl = profileUrl;
        this.timeStamp = timeStamp;
        this.description = description;
        this.rateNumber = rateNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRateNumber() {
        return rateNumber;
    }

    public void setRateNumber(Integer rateNumber) {
        this.rateNumber = rateNumber;
    }
}
