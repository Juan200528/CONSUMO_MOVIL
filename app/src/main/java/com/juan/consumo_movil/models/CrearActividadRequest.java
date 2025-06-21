package com.juan.consumo_movil.models;

public class CrearActividadRequest {
    private String title;
    private String description;
    private String place;
    private String date;
    private String responsible; // Ahora es String
    private boolean promoted;

    public CrearActividadRequest(String title, String description, String place, String date, String responsible, boolean promoted) {
        this.title = title;
        this.description = description;
        this.place = place;
        this.date = date;
        this.responsible = responsible;
        this.promoted = promoted;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPlace() { return place; }
    public String getDate() { return date; }
    public String getResponsible() { return responsible; }
    public boolean isPromoted() { return promoted; }
}