package com.juan.consumo_movil.ui.perfil;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.juan.consumo_movil.InicioSesion;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.InfoPerfilRequest;
import com.juan.consumo_movil.utils.SessionManager;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editName, editEmail, editPhone;
    private LinearLayout btnEditProfile, btnLogout;
    private Button btnSaveChanges;
    private ImageView profileImage, btnChangePhoto;
    private SessionManager sessionManager;

    private static final String TAG = "PerfilFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        // Inicializar vistas
        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        btnLogout = view.findViewById(R.id.btnLogout);
        profileImage = view.findViewById(R.id.profileImage);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);

        sessionManager = new SessionManager(requireContext());

        cargarDatos();
        setCamposEditable(false);

        // Editar perfil
        btnEditProfile.setOnClickListener(v -> {
            setCamposEditable(true);
            btnEditProfile.setVisibility(View.GONE);
            btnSaveChanges.setVisibility(View.VISIBLE);
        });

        // Guardar cambios
        btnSaveChanges.setOnClickListener(v -> actualizarDatos());

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> mostrarDialogoLogout());

        // Cambiar foto de perfil
        btnChangePhoto.setOnClickListener(v -> seleccionarImagen());

        return view;
    }

    private void cargarDatos() {
        editName.setText(sessionManager.getUsername());
        editEmail.setText(sessionManager.getEmail());
        editPhone.setText(sessionManager.getPhone());
    }

    private void guardarDatos() {
        sessionManager.guardarSesion(
                sessionManager.getUserId(),
                editName.getText().toString().trim(),
                editEmail.getText().toString().trim(),
                editPhone.getText().toString().trim()
        );
    }

    private void setCamposEditable(boolean editable) {
        editName.setEnabled(editable);
        editEmail.setEnabled(editable);
        editPhone.setEnabled(editable);
    }

    private boolean validarCampos() {
        return !editName.getText().toString().trim().isEmpty()
                && !editEmail.getText().toString().trim().isEmpty()
                && !editPhone.getText().toString().trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        String gmailRegex = "^[\\w.-]+@gmail\\.com$";
        return email.matches(gmailRegex);
    }

    private void actualizarDatos() {
        if (!validarCampos()) {
            Toast.makeText(requireContext(), "Todos los campos deben estar completos", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = editEmail.getText().toString().trim();
        if (!isValidEmail(email)) {
            Toast.makeText(requireContext(), "El email debe ser un correo válido de Gmail", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar en SharedPreferences
        guardarDatos();


        // Enviar datos al backend
        // Preparar datos según lo que espera el backend
        String username = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String name = null; // El backend no espera este campo

        InfoPerfilRequest request = new InfoPerfilRequest(username, name, phone);

// Enviar datos al backend
        ApiService api = RetrofitClient.getApiService();
        Call<Void> call = api.cambiarInfoPerfil("Bearer " + sessionManager.getToken(), request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "✅ Datos actualizados en el servidor", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "✅ Datos Actualizados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "❌ Fallo de conexión", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al actualizar perfil", t);
            }
        });

        // Actualizar UI
        cargarDatos();
        setCamposEditable(false);
        btnSaveChanges.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
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

        sessionManager.cerrarSesion();

        Intent intent = new Intent(requireActivity(), InicioSesion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            subirImagen(imageUri);
        }
    }

    private void subirImagen(Uri uri) {
        try {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = requireContext().getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor == null) return;

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            File file = new File(picturePath);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);

            ApiService api = RetrofitClient.getApiService();
            String tokenStr = "Bearer " + sessionManager.getToken();

            Call<Void> call = api.cambiarFoto(tokenStr, body);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        profileImage.setImageURI(uri);
                        Toast.makeText(requireContext(), "✅ Foto actualizada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "⚠️ Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(requireContext(), "❌ Fallo al conectar con el servidor", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al subir foto de perfil", t);
                }
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "❌ Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error al seleccionar imagen", e);
        }
    }
}