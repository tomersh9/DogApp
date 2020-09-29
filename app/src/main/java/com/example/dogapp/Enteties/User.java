package com.example.dogapp.Enteties;

import java.util.Comparator;
import java.util.List;

public class User implements Comparable<User> {

    private String fullName;
    private String dateOfBirth;
    private Integer age;
    private String email;
    private Integer gender;
    private Boolean type; //true for walker, false for user
    private String location;
    private String photoUrl;
    private String coverUrl;
    private String id;
    private Boolean status;
    private String timeStamp;
    private String aboutMe;
    private String experience;
    private Integer kmRange;
    private List<Integer> dogSizesList;
    private Boolean lastCall;
    private Integer paymentPerWalk;

    public User() {
    }

    public User(String fullName, String dateOfBirth, Integer age, String email, Integer gender, Boolean type, String location, String photoUrl, String coverUrl, String id, Boolean status, String timeStamp, String aboutMe, String experience, Integer kmRange, List<Integer> dogSizesList, Boolean lastCall, Integer paymentPerWalk) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.email = email;
        this.gender = gender;
        this.type = type;
        this.location = location;
        this.photoUrl = photoUrl;
        this.coverUrl = coverUrl;
        this.id = id;
        this.status = status;
        this.timeStamp = timeStamp;
        this.aboutMe = aboutMe;
        this.experience = experience;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
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

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public Integer getKmRange() {
        return kmRange;
    }

    public void setKmRange(Integer kmRange) {
        this.kmRange = kmRange;
    }

    public List<Integer> getDogSizesList() {
        return dogSizesList;
    }

    public void setDogSizesList(List<Integer> dogSizesList) {
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
        int compare = Long.compare(time2, time1);

        return compare;
    }
}
