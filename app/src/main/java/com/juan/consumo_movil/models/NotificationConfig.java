package com.juan.consumo_movil.models;

public class NotificationConfig {
    private String title;
    private int daysBefore;
    private String date;
    private String place;

    public NotificationConfig(String title, int daysBefore, String date, String place) {
        this.title = title;
        this.daysBefore = daysBefore;
        this.date = date;
        this.place = place;
    }

    // Getters y setters
}