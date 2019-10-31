package com.example.dairyfarmer;

public class User {
    public String username, firstName, lastName, phone, country, password;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String firstName, String lastName, String phone, String country, String password) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.country = country;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public String getFirstName(){
        return firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public String getPhone(){
        return phone;
    }
    public String getCountry(){
        return country;
    }
    public String getEmail(){
        return email;
    }
}
