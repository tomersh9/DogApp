package com.example.dogapp.Enteties;

public class User {

    private String fullName;
    private int age;
    private String username;
    private String email;
    //List<Dog> dogs


    public User(String fullName, String username, String email) {
        this.fullName = fullName;
        //this.age = age;
        this.username = username;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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
