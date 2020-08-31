package com.km0_compro.Model;

import java.security.PrivilegedAction;

public class User {

    private String id;
    private String username;
    private String imageURL;
    private String type;
    private String lat;
    private String lng;
    private String phone;
    private String realname;
    private String address;
    private String totalincome;
    private String totalorders;

    //constructor with all the instances selected
    public User(String id, String username, String imageURL) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
    }

    //constructor with no instances selected
    public User() {
    }

    public String getTotalincome() {
        return totalincome;
    }

    public void setTotalincome(String totalincome) {
        this.totalincome = totalincome;
    }

    public String getTotalorders() {
        return totalorders;
    }

    public void setTotalorders(String totalorders) {
        this.totalorders = totalorders;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}
