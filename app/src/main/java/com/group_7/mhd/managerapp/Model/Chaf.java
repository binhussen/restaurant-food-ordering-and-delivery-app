package com.group_7.mhd.managerapp.Model;

public class Chaf {
    private String name,phone,password,secureCode;

    public Chaf() {
    }

    /*public Chaf(String name, String phone, String password) {
        this.name = name;
        this.phone = phone;
        this.password = password;
    }*/

    public Chaf(String name, String phone, String password, String secureCode) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.secureCode = secureCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }
}

