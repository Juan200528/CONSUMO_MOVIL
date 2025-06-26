// NotificationResponse.java
package com.juan.consumo_movil.models;

import java.util.Date;

public class NotificationResponse {
    private String _id;
    private String title;
    private int daysBefore;
    private String date;
    private String place;
    private boolean read;
    private Date createdAt;

    // Getters
    public String getId() { return _id; }
    public String getTitle() { return title; }
    public int getDaysBefore() { return daysBefore; }
    public String getDate() { return date; }
    public String getPlace() { return place; }
    public boolean isRead() { return read; }
    public Date getCreatedAt() { return createdAt; }
}