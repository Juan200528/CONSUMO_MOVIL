package com.juan.consumo_movil.model;

public class PerfilRequest {
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;

    public PerfilRequest(String nombre, String email, String telefono, String direccion) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }


}