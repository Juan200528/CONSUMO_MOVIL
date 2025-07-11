package com.juan.consumo_movil.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("_id")
    private String id;

    private String username;
    private String email;
    private String password; // Solo se usará al enviar (registro/login)

    // Getters y setters

    public String getId() {
        return id;
    }

    public void setId(String id) {  // Puedes omitirlo si solo se recibe desde el backend
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
