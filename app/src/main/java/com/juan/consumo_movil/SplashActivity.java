package com.juan.consumo_movil;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.juan.consumo_movil.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar SessionManager
        SessionManager.init(this);
        SessionManager sessionManager = SessionManager.getInstance();

        // Verificar si el usuario ya está logueado
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(SplashActivity.this, MenuActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, InicioSesion.class));
        }

        finish(); // Evita regresar a Splash
    }
}