package com.juan.consumo_movil.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ActividadModel {

    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("place")
    private String place;

    @SerializedName("date")
    private String date;

    @SerializedName("responsible")
    private List<String> responsible;

    @SerializedName("isPromoted")
    private boolean isPromoted;

    @SerializedName("past")
    private boolean pasada;

    @SerializedName("user")
    private User user;

    public static class User {
        @SerializedName("_id")
        private String id;

        public String getId() {
            return id;
        }
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPlace() {
        return place;
    }

    public String getDate() {
        return date;
    }

    public List<String> getResponsible() {
        return responsible;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public boolean isPasada() {
        return pasada;
    }

    public User getUser() {
        return user;
    }

    // --- Setters ---

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setResponsible(List<String> responsible) {
        this.responsible = responsible;
    }

    public void setPromoted(boolean promoted) {
        isPromoted = promoted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPasada(boolean pasada) {
        this.pasada = pasada;
    }

    public void setUser(User user) {
        this.user = user;
    }
}