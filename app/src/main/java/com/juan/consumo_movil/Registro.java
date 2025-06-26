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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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
    private Button btnRegistrar, btnGoogle;
    private SessionManager sessionManager;
    private boolean isRegistering = false;
    private GoogleSignInClient googleSignInClient;

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
        btnGoogle = findViewById(R.id.btnGoogle);
        sessionManager = new SessionManager(this);
        setupLoginLink();
        setupPasswordField(passwordEditText);
        setupPasswordField(confirmPasswordEditText);
        setupRegisterButtonWithStateEffect();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
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
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
        editText.setCompoundDrawablePadding(10);
        editText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            EditText edit = (EditText) v;
            int drawableRightStart = edit.getRight()
                    - edit.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()
                    - edit.getCompoundDrawablePadding();
            if (event.getRawX() >= drawableRightStart) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        edit.setTransformationMethod(null);
                        edit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0);
                        edit.setSelection(edit.getText().length());
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        edit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
                        edit.setSelection(edit.getText().length());
                        return true;
                }
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

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                loginWithGoogle(account.getEmail(), account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Fallo al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginWithGoogle(String email, String idToken) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(idToken);
        ApiService apiService = RetrofitClient.getApiService();
        Call<LoginResponse> call = apiService.loginWithGoogle(user);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = extractTokenFromHeaders(response);
                    if (token == null || token.isEmpty()) {
                        Toast.makeText(Registro.this, "Error al iniciar sesión automáticamente", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sessionManager.guardarToken(token);
                    sessionManager.guardarSesion(
                            Objects.requireNonNull(loginResponse.getId()).toString(),
                            loginResponse.getUsername(),
                            loginResponse.getEmail(),
                            "N/A"
                    );
                    verificarCorreo(token);
                } else {
                    Toast.makeText(Registro.this, "No se pudo iniciar sesión con Google", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(Registro.this, "No se pudo conectar con el servidor.", Toast.LENGTH_LONG).show();
            }
        });
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
                    if (token == null || token.isEmpty()) {
                        Toast.makeText(Registro.this, "Usuario registrado. Revisa tu correo para verificar la cuenta", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sessionManager.guardarToken(token);
                    sessionManager.guardarSesion(
                            Objects.requireNonNull(loginResponse.getId()).toString(),
                            loginResponse.getUsername(),
                            loginResponse.getEmail(),
                            "N/A"
                    );
                    verificarCorreo(token);
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

    private void verificarCorreo(String token) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<ResponseBody> call = apiService.verifyEmail("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(Registro.this, MenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    Toast.makeText(Registro.this, "Verifica tu correo antes de continuar", Toast.LENGTH_LONG).show();
                    sessionManager.cerrarSesion();
                    Intent intent = new Intent(Registro.this, InicioSesion.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(Registro.this, "Fallo al verificar correo", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Registro.this, MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });
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