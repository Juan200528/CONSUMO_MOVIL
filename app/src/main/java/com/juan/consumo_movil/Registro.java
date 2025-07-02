package com.juan.consumo_movil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.LoginResponse;
import com.juan.consumo_movil.model.User;
import com.juan.consumo_movil.utils.SessionManager;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registro extends AppCompatActivity {
    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button btnRegistrar;
    private SessionManager sessionManager;
    private boolean isRegistering = false;
    private boolean isPollingActive = false; // Nueva variable para controlar el polling

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        RetrofitClient.init(getApplicationContext());
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        sessionManager = new SessionManager(this);

        setupLoginLink();
        setupPasswordField(passwordEditText);
        setupPasswordField(confirmPasswordEditText);
        setupRegisterButtonWithStateEffect();

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Solo verificar correo si no hay polling activo, el usuario está logueado, y no venimos de un registro reciente
        if (!isPollingActive && sessionManager.isLoggedIn()) {
            String token = sessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                // Verificar una sola vez al resumir la actividad
                verificarCorreoSilencioso(token);
            }
        }
    }

    private void setupLoginLink() {
        TextView loginLinkTextView = findViewById(R.id.loginLinkTextView);
        String originalText = "¿Ya tienes cuenta? Entrar";
        SpannableString spannableString = new SpannableString(originalText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Registro.this, InicioSesion.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        int startIndex = originalText.indexOf("Entrar");
        int endIndex = startIndex + "Entrar".length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginLinkTextView.setText(spannableString);
        loginLinkTextView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        loginLinkTextView.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupPasswordField(EditText editText) {
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            EditText edit = (EditText) v;
            int drawableRightStart = edit.getRight()
                    - edit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                    - edit.getCompoundDrawablePadding();
            if (event.getAction() == MotionEvent.ACTION_DOWN && event.getRawX() >= drawableRightStart) {
                edit.setTransformationMethod(null);
                edit.setSelection(edit.getText().length());
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                edit.setSelection(edit.getText().length());
                return true;
            }
            return false;
        });
    }

    private void setupRegisterButtonWithStateEffect() {
        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xFF03683E, 0xFF064349});
        gradientDrawableNormal.setCornerRadius(80f);
        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(0xFF063449);
        gradientDrawablePressed.setCornerRadius(80f);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, gradientDrawablePressed);
        stateListDrawable.addState(new int[]{}, gradientDrawableNormal);
        btnRegistrar.setBackground(stateListDrawable);
    }

    private void registrarUsuario() {
        if (isRegistering) return;
        isRegistering = true;
        String nombreCompleto = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nombreCompleto)) {
            fullNameEditText.setError("Ingrese su nombre completo");
            fullNameEditText.requestFocus();
            isRegistering = false;
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Ingrese su correo electrónico");
            emailEditText.requestFocus();
            isRegistering = false;
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Ingrese un correo válido");
            emailEditText.requestFocus();
            isRegistering = false;
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Ingrese una contraseña");
            passwordEditText.requestFocus();
            isRegistering = false;
            return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            passwordEditText.requestFocus();
            isRegistering = false;
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Las contraseñas no coinciden");
            confirmPasswordEditText.requestFocus();
            isRegistering = false;
            return;
        }

        User user = new User();
        user.setUsername(nombreCompleto);
        user.setEmail(email);
        user.setPassword(password);

        ApiService apiService = RetrofitClient.getApiService();
        Call<LoginResponse> call = apiService.register(user);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                isRegistering = false;
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = extractTokenFromHeaders(response);
                    if (token != null && !token.isEmpty()) {
                        sessionManager.guardarToken(token);
                        sessionManager.guardarSesion(
                                Objects.requireNonNull(loginResponse.getId()).toString(),
                                loginResponse.getUsername(),
                                loginResponse.getEmail(),
                                "N/A"
                        );
                    }
                    Toast.makeText(Registro.this, "Usuario registrado. Revisa tu correo para verificar la cuenta", Toast.LENGTH_SHORT).show();

                    // Intentar abrir la aplicación de Gmail si está instalada
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            // Fallback a Play Store
                            Uri marketUri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm");
                            Intent storeIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                            storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(storeIntent);
                        }
                    } catch (Exception e) {
                        Uri marketUri = Uri.parse(" https://play.google.com/store/apps/details?id=com.google.android.gm");
                        Intent storeIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                        storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(storeIntent);
                    }

                    // Solo iniciar polling si tenemos token
                    if (token != null && !token.isEmpty()) {
                        startEmailVerificationPolling(token);
                    }
                } else {
                    Toast.makeText(Registro.this, "El registro no fue posible. Inténtalo nuevamente.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                isRegistering = false;
                Toast.makeText(Registro.this, "No se pudo conectar con el servidor. Verifica tu conexión e inténtalo de nuevo.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startEmailVerificationPolling(String token) {
        isPollingActive = true;
        Toast.makeText(this, "Esperando verificación de correo...", Toast.LENGTH_SHORT).show();
        checkEmailVerification(token, 0);
    }

    private void checkEmailVerification(String token, int attempts) {
        final int maxAttempts = 60; // 3 minutos (60 intentos x 3 segundos)
        if (attempts >= maxAttempts) {
            isPollingActive = false;
            Toast.makeText(Registro.this, "Tiempo de espera agotado. Por favor, verifica tu correo y vuelve a abrir la app.", Toast.LENGTH_LONG).show();
            return;
        }
        ApiService apiService = RetrofitClient.getApiService();
        Call<ResponseBody> call = apiService.verifyEmail("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // ¡Email verificado exitosamente!
                    isPollingActive = false;
                    Toast.makeText(Registro.this, "¡Correo verificado! Redirigiendo al menú...", Toast.LENGTH_SHORT).show();
                    // Esperar un momento para que el usuario vea el mensaje y luego redirigir
                    new android.os.Handler(getMainLooper()).postDelayed(() -> {
                        redirectToMenu();
                    }, 1500);
                } else if (response.code() == 401 || response.code() == 403) {
                    // Email aún no verificado, seguir intentando
                    new android.os.Handler(getMainLooper()).postDelayed(() -> {
                        checkEmailVerification(token, attempts + 1);
                    }, 3000);
                } else {
                    // Otro error, seguir intentando por si es temporal
                    new android.os.Handler(getMainLooper()).postDelayed(() -> {
                        checkEmailVerification(token, attempts + 1);
                    }, 3000);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Error de conexión, seguir intentando
                new android.os.Handler(getMainLooper()).postDelayed(() -> {
                    checkEmailVerification(token, attempts + 1);
                }, 3000);
            }
        });
    }

    // Verificación silenciosa para onResume (sin mensajes de error molestos)
    private void verificarCorreoSilencioso(String token) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<ResponseBody> call = apiService.verifyEmail("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Email ya verificado, ir al menú
                    Toast.makeText(Registro.this, "Bienvenido de vuelta", Toast.LENGTH_SHORT).show();
                    redirectToMenu();
                }
                // Si no está verificado, no hacer nada (usuario puede estar esperando el polling)
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Error silencioso, no molestar al usuario
            }
        });
    }

    // Método centralizado para redirigir al menú
    private void redirectToMenu() {
        Intent intent = new Intent(Registro.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String extractTokenFromHeaders(Response<LoginResponse> response) {
        for (int i = 0; i < response.headers().size(); i++) {
            String name = response.headers().name(i);
            String value = response.headers().value(i);
            if (name.equalsIgnoreCase("set-cookie") && value.contains("token=")) {
                int start = value.indexOf("token=") + 6;
                int end = value.indexOf(';', start);
                return (end > start) ? value.substring(start, end) : value.substring(start);
            }
        }
        return null;
    }
}