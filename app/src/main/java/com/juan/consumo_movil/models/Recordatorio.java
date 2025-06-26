package com.juan.consumo_movil.models;

public class Recordatorio {
    private String titulo;
    private int diasAntes;
    private String fecha;
    private String lugar;

    public Recordatorio(String titulo, int diasAntes, String fecha, String lugar) {
        this.titulo = titulo;
        this.diasAntes = diasAntes;
        this.fecha = fecha;
        this.lugar = lugar;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getDiasAntes() { return diasAntes; }
    public void setDiasAntes(int diasAntes) { this.diasAntes = diasAntes; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
}