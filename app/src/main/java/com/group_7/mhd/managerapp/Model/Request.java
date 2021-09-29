package com.group_7.mhd.managerapp.Model;

import java.util.List;

/**
 * Created by Guest User on 4/1/2018.
 */

public class Request {
    private String phone;
    private String name;
    private String addresslat;
    private String addresslon;
    private String total;
    private String status;
    private String paymentMethod;
    private String table_No;
    private String TackAway;

    private List<Order>foods;//list of food Orders

    public Request() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddresslat() {
        return addresslat;
    }

    public void setAddresslat(String addresslat) {
        this.addresslat = addresslat;
    }

    public String getAddresslon() {
        return addresslon;
    }

    public void setAddresslon(String addresslon) {
        this.addresslon = addresslon;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTable_No() {
        return table_No;
    }

    public void setTable_No(String table_No) {
        this.table_No = table_No;
    }

    public String getTackAway() {
        return TackAway;
    }

    public void setTackAway(String tackAway) {
        TackAway = tackAway;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
