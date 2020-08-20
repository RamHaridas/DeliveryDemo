package com.white.delivery.ui.gallery;

public class ListDataCO {
    private String date;
    private String time;
    private String status;
    public ListDataCO(String date,String time,String status) {
        this.date = date;
        this.time=time;
        this.status=status;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
