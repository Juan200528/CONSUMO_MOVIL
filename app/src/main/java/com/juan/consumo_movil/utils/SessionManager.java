package com.juan.consumo_movil.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_TOKEN = "auth_token";

    private final SharedPreferences sharedPreferences;

    // ✅ Instancia Singleton
    private static SessionManager instance;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    /**
     * Inicializa la instancia global del SessionManager
     */
    public static void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
    }

    /**
     * Devuelve la instancia global del SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager no ha sido inicializado. Llama a SessionManager.init(context) antes.");
        }
        return instance;
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

    public void guardarToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public void guardarAddress(String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ADDRESS, address);
        editor.apply();
    }

    public String getUserId() { return sharedPreferences.getString(KEY_USER_ID, null); }
    public String getUsername() { return sharedPreferences.getString(KEY_USERNAME, null); }
    public String getEmail() { return sharedPreferences.getString(KEY_EMAIL, null); }
    public String getPhone() { return sharedPreferences.getString(KEY_PHONE, null); }
    public String getAddress() { return sharedPreferences.getString(KEY_ADDRESS, null); }
    public String getToken() { return sharedPreferences.getString(KEY_TOKEN, null); }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public String fetchAuthToken() {
        String token = getToken();
        return (token != null && !token.isEmpty()) ? "Bearer " + token : null;
    }

    public void cerrarSesion() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}