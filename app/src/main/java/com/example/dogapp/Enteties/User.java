package com.example.dogapp.Enteties;

import java.util.Comparator;

public class User  implements Comparable<User> {

    private String fullName;
    private String dateOfBirth;
    private String email;
    private String gender;
    private String title;
    private String location;
    private String photoUri;
    private String id;
    private String status;
    private String timeStamp;
    private String aboutMe;
    private String kmRange;
    private String dogSizesList;
    private Boolean lastCall;
    private Integer paymentPerWalk;

    public User() {}

    public User(String fullName, String dateOfBirth, String email, String gender, String title, String location, String photoUri, String id, String status, String timeStamp, String aboutMe, String kmRange, String dogSizesList, Boolean lastCall, Integer paymentPerWalk) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.gender = gender;
        this.title = title;
        this.location = location;
        this.photoUri = photoUri;
        this.id = id;
        this.status = status;
        this.timeStamp = timeStamp;
        this.aboutMe = aboutMe;
        this.kmRange = kmRange;
        this.dogSizesList = dogSizesList;
        this.lastCall = lastCall;
        this.paymentPerWalk = paymentPerWalk;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getKmRange() {
        return kmRange;
    }

    public void setKmRange(String kmRange) {
        this.kmRange = kmRange;
    }

    public String getDogSizesList() {
        return dogSizesList;
    }

    public void setDogSizesList(String dogSizesList) {
        this.dogSizesList = dogSizesList;
    }

    public Boolean getLastCall() {
        return lastCall;
    }

    public void setLastCall(Boolean lastCall) {
        this.lastCall = lastCall;
    }

    public Integer getPaymentPerWalk() {
        return paymentPerWalk;
    }

    public void setPaymentPerWalk(Integer paymentPerWalk) {
        this.paymentPerWalk = paymentPerWalk;
    }

    @Override
    public int compareTo(User o) {
        
        long time1 = Long.parseLong(timeStamp);
        long time2 = Long.parseLong(o.timeStamp);
        int compare = Long.compare(time2,time1);
        
        return  compare;
    }
}
