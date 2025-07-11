package com.juan.consumo_movil.model;

public class InfoPerfilRequest {
    private String username;
    private String name; // Puede ser null si no se envía
    private String phone;

    public InfoPerfilRequest(String username, String name, String phone) {
        this.username = username;
        this.name = name;
        this.phone = phone;
    }
}