package com.juan.consumo_movil.models;

public class NotificationConfig {
    private String title;
    private int daysBefore;
    private String date;
    private String place;
    private String task; // Nuevo campo

    public NotificationConfig(String title, int daysBefore, String date, String place, String task) {
        this.title = title;
        this.daysBefore = daysBefore;
        this.date = date;
        this.place = place;
        this.task = task;
    }

    // Getters
    public String getTitle() { return title; }
    public int getDaysBefore() { return daysBefore; }
    public String getDate() { return date; }
    public String getPlace() { return place; }
    public String getTask() { return task; }
}