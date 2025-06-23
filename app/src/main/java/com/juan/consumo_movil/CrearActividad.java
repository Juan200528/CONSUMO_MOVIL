package com.juan.consumo_movil;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearActividad extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int PERM_REQ = 100;

    private EditText etTitulo, etDesc, etFecha, etLugar, etResp;
    private ImageButton btnSubir, btnDate;
    private ImageView ivImg;
    private File imgFile;
    private ApiService api;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        // Inicialización de vistas
        etTitulo = findViewById(R.id.etTitulo);
        etDesc = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        etLugar = findViewById(R.id.etLugar);
        etResp = findViewById(R.id.etResponsables);
        btnSubir = findViewById(R.id.btnSubir);
        btnDate = findViewById(R.id.btnCalendario);
        ivImg = findViewById(R.id.ivActividadImagen);

        api = RetrofitClient.getApiService();
        sessionManager = new SessionManager(this);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnSubir.setOnClickListener(v -> pickImage());
        findViewById(R.id.btnCrear).setOnClickListener(v -> upload());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
                etFecha.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean storagePerm() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    private void pickImage() {
        if (!storagePerm()) {
            String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            ActivityCompat.requestPermissions(this, new String[]{perm}, PERM_REQ);
        } else {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ivImg.setImageURI(uri);
            imgFile = uriToFile(uri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQ && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        }
    }

    private File uriToFile(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            File tmp = new File(getCacheDir(), "img_" + UUID.randomUUID() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }

    private void upload() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDesc.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String lugar = etLugar.getText().toString().trim();
        String responsablesStr = etResp.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(fecha) || TextUtils.isEmpty(lugar)) {
            Toast.makeText(this, "Título, fecha y lugar son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date inputDate = sdf.parse(fecha);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if (inputDate.before(today.getTime())) {
                Toast.makeText(this, "No puedes seleccionar una fecha anterior a hoy", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Inicie sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody titleBody = createRequestBody(titulo);
        RequestBody descBody = createRequestBody(descripcion);
        RequestBody dateBody = createRequestBody(formatDateForBackend(fecha));
        RequestBody placeBody = createRequestBody(lugar);
        RequestBody responsableBody = !TextUtils.isEmpty(responsablesStr) ? createRequestBody(responsablesStr) : null;

        MultipartBody.Part imagePart = null;
        if (imgFile != null && imgFile.exists()) {
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imgFile);
            imagePart = MultipartBody.Part.createFormData("image", imgFile.getName(), requestFile);
        }

        Call<ActividadModel> call = api.crearActividadConImagen(
                "Bearer " + token,
                titleBody,
                descBody,
                dateBody,
                placeBody,
                responsableBody,
                imagePart
        );

        call.enqueue(new Callback<ActividadModel>() {
            @Override
            public void onResponse(Call<ActividadModel> call, Response<ActividadModel> response) {
                cleanupTempFile();
                if (response.isSuccessful()) {
                    finish();
                    Toast.makeText(CrearActividad.this, "Actividad creada exitosamente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ActividadModel> call, Throwable t) {
                cleanupTempFile();
            }
        });
    }

    private RequestBody createRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private String formatDateForBackend(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            outputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr + "T00:00:00.000Z";
        }
    }

    private void cleanupTempFile() {
        if (imgFile != null && imgFile.exists()) {
            boolean deleted = imgFile.delete();
            if (!deleted) {
                Log.w("CrearActividad", "No se pudo eliminar archivo temporal");
            }
        }
    }
}