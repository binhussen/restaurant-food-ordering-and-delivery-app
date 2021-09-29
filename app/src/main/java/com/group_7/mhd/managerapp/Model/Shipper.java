package com.group_7.mhd.managerapp.Model;

public class Shipper {
    private String name,phone,password,secureCode,type;

    public Shipper() {
    }

    public Shipper(String name, String phone, String password, String secureCode, String type) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.secureCode = secureCode;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
