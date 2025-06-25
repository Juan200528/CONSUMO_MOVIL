package com.juan.consumo_movil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;

public class VerificacionPendiente extends AppCompatActivity {

    private Button btnReenviarCorreo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacion_pendiente);

        // Inicializar vistas
        btnReenviarCorreo = findViewById(R.id.btnReenviarCorreo);

        // Configurar el botón "Reenviar correo de verificación"
        btnReenviarCorreo.setOnClickListener(v -> reenviarCorreo());
    }

    /**
     * Método para reenviar el correo de verificación
     */
    private void reenviarCorreo() {
        String email = getIntent().getStringExtra("email"); // Obtener el email del intent
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "No se encontró el correo electrónico.", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.setEmail(email);

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.resendVerificationEmail(user);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(VerificacionPendiente.this, "Correo de verificación reenviado.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(VerificacionPendiente.this, "No se pudo reenviar el correo.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(VerificacionPendiente.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}