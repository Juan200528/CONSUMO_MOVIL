package com.juan.consumo_movil.ui.perfil;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.juan.consumo_movil.InicioSesion;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private EditText editName, editEmail, editPhone, editAddress;
    private LinearLayout btnEditProfile, btnLogout;
    private Button btnSaveChanges;
    private SessionManager sessionManager;

    private static final String TAG = "PerfilFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        editAddress = view.findViewById(R.id.editAddress);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnLogout = view.findViewById(R.id.btnLogout);

        sessionManager = new SessionManager(requireContext());

        cargarDatos();
        setCamposEditable(false);

        btnEditProfile.setOnClickListener(v -> {
            setCamposEditable(true);
            btnEditProfile.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
        });

        btnSaveChanges.setOnClickListener(v -> actualizarDatos());

        btnLogout.setOnClickListener(v -> mostrarDialogoLogout());

        return view;
    }

    private void cargarDatos() {
        editName.setText(sessionManager.getUsername());
        editEmail.setText(sessionManager.getEmail());
        editPhone.setText(sessionManager.getPhone());
        editAddress.setText(sessionManager.getAddress());
    }

    private void guardarDatos() {
        sessionManager.guardarSesion(
                sessionManager.getUserId(),
                editName.getText().toString().trim(),
                editEmail.getText().toString().trim(),
                editPhone.getText().toString().trim()
        );
        sessionManager.guardarAddress(editAddress.getText().toString().trim());
    }

    private void setCamposEditable(boolean editable) {
        editName.setEnabled(editable);
        editEmail.setEnabled(editable);
        editPhone.setEnabled(editable);
        editAddress.setEnabled(editable);
    }

    private boolean validarCampos() {
        return !editName.getText().toString().trim().isEmpty()
                && !editEmail.getText().toString().trim().isEmpty()
                && !editPhone.getText().toString().trim().isEmpty()
                && !editAddress.getText().toString().trim().isEmpty();
    }

    private void actualizarDatos() {
        if (!validarCampos()) {
            Toast.makeText(requireContext(), "Todos los campos deben estar completos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar en SharedPreferences (sesión local)
        guardarDatos();

        // Mostrar mensaje de éxito
        Toast.makeText(requireContext(), "Datos actualizados localmente", Toast.LENGTH_SHORT).show();

        // Actualizar UI
        cargarDatos();
        setCamposEditable(false);
        btnSaveChanges.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);

        // Si deseas enviar los cambios al servidor, descomenta esto:
        /*
        ApiService api = RetrofitClient.getApiService();
        Call<Void> call = api.actualizarPerfil("Bearer " + sessionManager.getToken(), new PerfilRequest(
                editName.getText().toString().trim(),
                editEmail.getText().toString().trim(),
                editPhone.getText().toString().trim(),
                editAddress.getText().toString().trim()
        ));

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "✅ Datos actualizados en el servidor", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "⚠️ Error al actualizar en el servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "❌ Fallo de conexión", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al actualizar perfil", t);
            }
        });
        */
    }

    private void mostrarDialogoLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> cerrarSesion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesion() {
        // Opcional: llamar a API para cerrar sesión
        ApiService apiService = RetrofitClient.getApiService();
        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Logout exitoso en el servidor");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Fallo al cerrar sesión en el servidor", t);
            }
        });

        // Limpiar sesión local
        sessionManager.cerrarSesion();

        // Navegar a inicio de sesión
        Intent intent = new Intent(requireActivity(), InicioSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}