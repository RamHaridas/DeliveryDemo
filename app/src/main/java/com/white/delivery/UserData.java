package com.white.delivery;

public class UserData {

    String name;
    String email;
    String pass;
    String mobile;
    String url;

    public UserData(){}

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPass() {
        return pass;
    }

    public String getMobile() {
        return mobile;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
