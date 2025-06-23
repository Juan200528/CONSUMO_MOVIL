package com.juan.consumo_movil.models;

public class Actividad {

    private String id; // Cambiado de int a String
    private String titulo;
    private String descripcion;
    private String lugar;
    private String fecha;
    private String responsables;
    private boolean promocionada;
    private boolean pasada;
    private boolean asistido;
    private String idCreador;
    private String imagenRuta;

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getResponsables() {
        return responsables;
    }

    public void setResponsables(String responsables) {
        this.responsables = responsables;
    }

    public boolean isPromocionada() {
        return promocionada;
    }

    public void setPromocionada(boolean promocionada) {
        this.promocionada = promocionada;
    }

    public boolean isPasada() {
        return pasada;
    }

    public void setPasada(boolean pasada) {
        this.pasada = pasada;
    }

    public boolean isAsistido() {
        return asistido;
    }

    public void setAsistido(boolean asistido) {
        this.asistido = asistido;
    }

    public String getIdCreador() {
        return idCreador;
    }

    public void setIdCreador(String idCreador) {
        this.idCreador = idCreador;
    }

    public String getImagenRuta() {
        return imagenRuta;
    }

    public void setImagenRuta(String imagenRuta) {
        this.imagenRuta = imagenRuta;
    }
}