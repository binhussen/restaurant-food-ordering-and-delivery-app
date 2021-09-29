package com.group_7.mhd.managerapp.Model;

import java.util.List;

public class Comment {

    private String phone;
    private String name;
    private String commentt;
    private String commentd;

    public Comment() {
    }

    public Comment(String phone, String name, String commentt, String commentd) {
        this.phone = phone;
        this.name = name;
        this.commentt = commentt;
        this.commentd = commentd;
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

    public String getCommentt() {
        return commentt;
    }

    public void setCommentt(String commentt) {
        this.commentt = commentt;
    }

    public String getCommentd() {
        return commentd;
    }

    public void setCommentd(String commentd) {
        this.commentd = commentd;
    }
}
