package com.juan.consumo_movil.model;

import java.util.HashMap;
import java.util.Map;

public class ComunidadModel {
    private String id;
    private String nombre;
    private String creadorId;
    private Map<String, Boolean> miembros;

    public ComunidadModel() {
        miembros = new HashMap<>();
    }

    public ComunidadModel(String id, String nombre, String creadorId) {
        this.id = id;
        this.nombre = nombre;
        this.creadorId = creadorId;
        this.miembros = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCreadorId() {
        return creadorId;
    }

    public Map<String, Boolean> getMiembros() {
        return miembros;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCreadorId(String creadorId) {
        this.creadorId = creadorId;
    }

    public void setMiembros(Map<String, Boolean> miembros) {
        this.miembros = miembros;
    }

    public void addMiembro(String uid) {
        if (this.miembros == null) {
            this.miembros = new HashMap<>();
        }
        this.miembros.put(uid, true);
    }
}
