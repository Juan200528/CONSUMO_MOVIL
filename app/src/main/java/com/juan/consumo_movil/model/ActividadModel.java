package com.juan.consumo_movil.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    @SerializedName("promocionada")
    private boolean promocionada;

    @SerializedName("asistido")
    private boolean asistido;

    @SerializedName("pasada")
    private boolean pasada;

    @SerializedName("image")
    private String imageUrl;

    @SerializedName("user")
    private User user;

    // --- Constructor vacío necesario para Gson ---
    public ActividadModel() {
    }

    // --- Getters y Setters ---

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
        return promocionada;
    }

    public void setPromoted(boolean promocionada) {
        this.promocionada = promocionada;
    }

    public boolean isAsistido() {
        return asistido;
    }

    public void setAsistido(boolean asistido) {
        this.asistido = asistido;
    }

    public boolean isPasada() {
        return pasada;
    }

    public void setPasada(boolean pasada) {
        this.pasada = pasada;
    }

    public String getImage() {
        return imageUrl;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Verifica si la actividad aún es vigente comparando su fecha con la actual.
     *
     * @return true si la fecha de la actividad es posterior a la fecha actual.
     */
    public boolean estaVigente() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date fechaActividad = sdf.parse(this.date);
            return fechaActividad != null && fechaActividad.after(new Date());
        } catch (ParseException e) {
            // Si hay error al parsear la fecha, se asume que no es vigente
            return false;
        }
    }

    // --- Métodos equals y hashCode basados en el ID único de la actividad ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActividadModel)) return false;

        ActividadModel that = (ActividadModel) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    // --- Clase interna User ---
    public static class User {
        @SerializedName("_id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}