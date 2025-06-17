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

    @SerializedName("promotion")
    private Promotion promotion;

    // Opcional: si usas datos del usuario
    @SerializedName("user")
    private User user;

    public static class User {
        @SerializedName("_id")
        private String id;

        @SerializedName("email")
        private String email;

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class Promotion {
        @SerializedName("startDate")
        private String startDate;

        @SerializedName("endDate")
        private String endDate;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    // --- Getters y setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getResponsible() {
        return responsible;
    }

    public void setResponsible(List<String> responsible) {
        this.responsible = responsible;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public void setPromoted(boolean promoted) {
        isPromoted = promoted;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}