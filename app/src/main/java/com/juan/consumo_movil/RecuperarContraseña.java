package com.juan.consumo_movil;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.models.ResetPasswordRequest; // 👈 Importa el modelo correcto

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarContraseña extends AppCompatActivity {

    private EditText etEmail;
    private Button btnEnviarCorreo, btnCancelar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_forgetpassword);

        etEmail = findViewById(R.id.emailEditText);
        btnEnviarCorreo = findViewById(R.id.btnsendEmail);
        btnCancelar = findViewById(R.id.btnCancel);

        RetrofitClient.init(getApplicationContext());

        btnEnviarCorreo.setOnClickListener(v -> enviarCorreoRecuperacion());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void enviarCorreoRecuperacion() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Por favor ingresa tu correo");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo inválido");
            etEmail.requestFocus();
            return;
        }

        // Usa el modelo correcto: com.juan.consumo_movil.models.ResetPasswordRequest
        ResetPasswordRequest request = new ResetPasswordRequest(); // 👈 Sin prefijo de paquete
        request.setEmail(email);

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.sendPasswordResetEmail(request); // 👈 Llama al método correctamente

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RecuperarContraseña.this,
                            "Se ha enviado un enlace a tu correo.", Toast.LENGTH_LONG).show();
                    finish(); // Volver a la pantalla de inicio de sesión
                } else {
                    Toast.makeText(RecuperarContraseña.this,
                            "Error: No se pudo enviar el correo.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RecuperarContraseña.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}