package com.juan.consumo_movil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.LoginResponse;
import com.juan.consumo_movil.model.User;
import com.juan.consumo_movil.utils.SessionManager;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Registro extends AppCompatActivity {
    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button btnRegistrar;
    private SessionManager sessionManager;
    private boolean isRegistering = false;

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

        setupPasswordField(passwordEditText);
        setupPasswordField(confirmPasswordEditText);

        setupRegisterButtonWithStateEffect();
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void setupPasswordField(EditText editText) {
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
        editText.setCompoundDrawablePadding(10);
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            int drawableRightStart = editText.getRight()
                    - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                    - editText.getCompoundDrawablePadding();
            if (event.getAction() == MotionEvent.ACTION_DOWN ||
                    event.getAction() == MotionEvent.ACTION_MOVE) {
                if (event.getRawX() >= drawableRightStart) {
                    editText.setTransformationMethod(null);
                    editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0);
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
                editText.setSelection(editText.getText().length());
                return true;
            }
            return false;
        });
    }

    private void setupRegisterButtonWithStateEffect() {
        GradientDrawable gradientDrawableNormal = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#03683E"), Color.parseColor("#064349")});
        gradientDrawableNormal.setCornerRadius(80f);

        GradientDrawable gradientDrawablePressed = new GradientDrawable();
        gradientDrawablePressed.setColor(Color.parseColor("#063449"));
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
                    String tokenCookie = null;

                    for (int i = 0; i < response.headers().size(); i++) {
                        String name = response.headers().name(i);
                        String value = response.headers().value(i);
                        if (name.equalsIgnoreCase("set-cookie") && value.contains("token=")) {
                            int start = value.indexOf("token=") + 6;
                            int end = value.indexOf(';', start);
                            tokenCookie = (end > start) ? value.substring(start, end) : value.substring(start);
                            break;
                        }
                    }

                    if (tokenCookie == null || tokenCookie.isEmpty()) {
                        return;
                    }

                    sessionManager.guardarToken(tokenCookie);
                    sessionManager.guardarSesion(
                            Objects.requireNonNull(loginResponse.getId()).toString(),
                            loginResponse.getUsername(),
                            loginResponse.getEmail(),
                            "N/A"
                    );

                    Intent intent = new Intent(Registro.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                isRegistering = false;
            }
        });
    }
}