package com.juan.consumo_movil;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

public class PantallaPrincipal extends AppCompatActivity {

    TextView PanasCoop;
    private AppCompatButton btnRegister;
    private AppCompatButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);

        // Iniciar sesión anónima en Firebase (sin mostrar logs)
        FirebaseAuth.getInstance().signInAnonymously();

        // Título
        PanasCoop = findViewById(R.id.PanasCoop);

        // Inicializar los botones
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        // Configuración grosor de título
        PanasCoop.getPaint().setFakeBoldText(true);
        PanasCoop.setLetterSpacing(0.15f);

        // Limpiar cualquier fondo predeterminado
        btnRegister.setBackground(null);

        // Crear GradientDrawable para el estado normal (degradado)
        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")});
        gradientDrawableNormal.setCornerRadius(80f);

        // Crear GradientDrawable para el estado presionado (color sólido)
        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(Color.parseColor("#063449"));
        gradientDrawablePressed.setCornerRadius(80f);

        // Configurar StateListDrawable para los estados del botón
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, gradientDrawablePressed);
        stateListDrawable.addState(new int[]{}, gradientDrawableNormal);

        // Aplicar el StateListDrawable al botón de Registrar
        btnRegister.setBackground(stateListDrawable);

        // Listener para el botón de Registro
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipal.this, Registro.class);
            startActivity(intent);
        });

        // Listener para el botón de Iniciar Sesión
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(PantallaPrincipal.this, InicioSesion.class);
            startActivity(intent);
        });
    }
}