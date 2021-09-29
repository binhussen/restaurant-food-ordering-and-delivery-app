package com.group_7.mhd.managerapp.Model;

public class User {

    private String Name;
    private String Password;
    private String Phone;
    private String IsStaff;
    private String Email;

    public User() {

    }

    public User(String name, String password, String phone, String isStaff, String email) {
        Name = name;
        Password = password;
        Phone = phone;
        IsStaff = isStaff;
        Email = email;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }
}
