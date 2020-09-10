package com.example.dogapp.Enteties;

public class User {

    private String fullName;
    private String dateOfBirth;
    private String username;
    private String email;
    private String gender;
    private String title;
    private String location;

    /*public User(String fullName, String username, String email) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
    }*/

    public User(String fullName, String dateOfBirth, String username, String email, String gender, String title, String location) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.username = username;
        this.email = email;
        this.gender = gender;
        this.title = title;
        this.location = location;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
