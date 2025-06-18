package com.juan.consumo_movil.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Clave del archivo SharedPreferences
    private static final String PREF_NAME = "user_session";

    // Claves para almacenar datos del usuario
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_TOKEN = "auth_token";

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Guarda los datos principales del usuario al iniciar sesión
     */
    public void guardarSesion(String id, String username, String email, String phone) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, id);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Guarda el token de autenticación (Bearer Token)
     */
    public void guardarToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    /**
     * Guarda la dirección del usuario (opcional)
     */
    public void guardarAddress(String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ADDRESS, address);
        editor.apply();
    }

    /**
     * Obtiene los datos del usuario
     */
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    public String getAddress() {
        return sharedPreferences.getString(KEY_ADDRESS, null);
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Verifica si el usuario ha iniciado sesión
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    /**
     * Devuelve el token con formato Bearer para usar en encabezados HTTP
     */
    public String fetchAuthToken() {
        String token = getToken();
        return (token != null && !token.isEmpty()) ? "Bearer " + token : null;
    }

    /**
     * Cierra la sesión del usuario (borra todos los datos guardados)
     */
    public void cerrarSesion() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}