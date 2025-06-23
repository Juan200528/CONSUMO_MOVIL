package com.juan.consumo_movil.models;

import com.google.gson.annotations.SerializedName;

public class Asistente {

    @SerializedName("_id")
    private String id;

    @SerializedName("userId")
    private String idAsistente;

    @SerializedName("taskId")
    private String idActividad;

    @SerializedName("fullName")
    private String nombreCompleto;

    @SerializedName("name")
    private String nombre;

    @SerializedName("email")
    private String correo;

    @SerializedName("activityName")
    private String actividadNombre;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdAsistente() { return idAsistente; }
    public void setIdAsistente(String idAsistente) { this.idAsistente = idAsistente; }

    public String getIdActividad() { return idActividad; }
    public void setIdActividad(String idActividad) { this.idActividad = idActividad; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
        this.nombre = nombreCompleto != null && nombreCompleto.contains(" ") ? nombreCompleto.split(" ")[0] : nombreCompleto != null ? nombreCompleto : "";
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getActividadNombre() { return actividadNombre; }
    public void setActividadNombre(String actividadNombre) { this.actividadNombre = actividadNombre; }

    public String getCorreoAbreviado() {
        return correo != null ? correo.split("@")[0] : "";
    }
}