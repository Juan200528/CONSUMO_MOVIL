package com.juan.consumo_movil.ui.gestionar;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.models.Asistente;
import com.juan.consumo_movil.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionarViewModel extends AndroidViewModel {

    private static final String TAG = "GestionarViewModel";
    private MutableLiveData<List<Asistente>> asistentes;
    private ApiService apiService;
    private String authToken;

    public GestionarViewModel(Application application) {
        super(application);
        asistentes = new MutableLiveData<>(new ArrayList<>());
        apiService = RetrofitClient.getApiService();

        // Cargar token desde SharedPreferences usando el mismo nombre que SessionManager
        authToken = application.getSharedPreferences("user_session", Application.MODE_PRIVATE)
                .getString("auth_token", "");
        Log.d(TAG, "Token obtenido en constructor: " + (authToken != null ? "✓" : "✗"));
    }

    public LiveData<List<Asistente>> getAsistentes() {
        return asistentes;
    }

    public void cargarAsistentes(String taskId) {
        // Recargar token por si ha cambiado
        authToken = getApplication()
                .getSharedPreferences("user_session", Application.MODE_PRIVATE)
                .getString("auth_token", "");

        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Token de autenticación inválido");
            Toast.makeText(getApplication(), "Token inválido. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "El ID de la actividad es inválido");
            Toast.makeText(getApplication(), "ID de actividad inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Cargando asistentes para taskId: " + taskId);
        apiService.getAttendees(taskId, "Bearer " + authToken)
                .enqueue(new Callback<List<Asistente>>() {
                    @Override
                    public void onResponse(Call<List<Asistente>> call, Response<List<Asistente>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Asistentes cargados: " + response.body().size());
                            asistentes.postValue(response.body());
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorBody = "Error al leer cuerpo de error";
                            }
                            Log.e(TAG, "Error al cargar asistentes. Código: " + response.code() + ", Mensaje: " + response.message() + ", Cuerpo: " + errorBody);
                            Toast.makeText(getApplication(), "Error al cargar los asistentes", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Asistente>> call, Throwable t) {
                        Log.e(TAG, "Fallo al cargar asistentes", t);
                        Toast.makeText(getApplication(), "Fallo de conexión al cargar asistentes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void insertarAsistente(Asistente asistente, String taskId) {
        // Validar token antes de hacer la llamada
        authToken = getApplication()
                .getSharedPreferences("user_session", Application.MODE_PRIVATE)
                .getString("auth_token", "");

        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Token inválido al insertar asistente");
            Toast.makeText(getApplication(), "Tu sesión ha expirado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "ID de actividad inválido al insertar asistente");
            Toast.makeText(getApplication(), "ID de actividad inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        Log.d(TAG, "Enviando asistente: " + gson.toJson(asistente));
        apiService.confirmAttendance("Bearer " + authToken, asistente)
                .enqueue(new Callback<Asistente>() {
                    @Override
                    public void onResponse(Call<Asistente> call, Response<Asistente> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Asistente insertado exitosamente: " + gson.toJson(response.body()));
                            cargarAsistentes(taskId); // Recargar lista
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorBody = "Error al leer el cuerpo de error";
                            }
                            Log.e(TAG, "Error al agregar asistente. Código: " + response.code() + ", Mensaje: " + response.message() + ", Cuerpo: " + errorBody);
                            if (response.code() == 401) {
                                Toast.makeText(getApplication(), "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), "Error al agregar asistente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Asistente> call, Throwable t) {
                        Log.e(TAG, "Fallo de conexión al insertar asistente", t);
                        Toast.makeText(getApplication(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void actualizarAsistente(String id, Asistente asistente, String taskId) {
        // Validar token y ID antes de continuar
        authToken = getApplication()
                .getSharedPreferences("user_session", Application.MODE_PRIVATE)
                .getString("auth_token", "");

        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Token inválido al actualizar");
            Toast.makeText(getApplication(), "Tu sesión ha expirado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "ID de actividad inválido al actualizar");
            Toast.makeText(getApplication(), "ID de actividad inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        Log.d(TAG, "Actualizando asistente con ID: " + id + ", Datos: " + gson.toJson(asistente));
        apiService.updateAttendance(id, "Bearer " + authToken, asistente)
                .enqueue(new Callback<Asistente>() {
                    @Override
                    public void onResponse(Call<Asistente> call, Response<Asistente> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Asistente actualizado exitosamente: " + gson.toJson(response.body()));
                            cargarAsistentes(taskId); // Recargar lista
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorBody = "Error al leer cuerpo de error";
                            }
                            Log.e(TAG, "Error al actualizar asistente. Código: " + response.code() + ", Mensaje: " + response.message() + ", Cuerpo: " + errorBody);
                            if (response.code() == 401) {
                                Toast.makeText(getApplication(), "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), "Error al actualizar asistente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Asistente> call, Throwable t) {
                        Log.e(TAG, "Fallo de conexión al actualizar asistente", t);
                        Toast.makeText(getApplication(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void eliminarAsistentePorId(String id, String taskId) {
        // Validar token y ID antes de continuar
        authToken = getApplication()
                .getSharedPreferences("user_session", Application.MODE_PRIVATE)
                .getString("auth_token", "");

        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Token inválido al eliminar");
            Toast.makeText(getApplication(), "Tu sesión ha expirado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskId == null || taskId.isEmpty()) {
            Log.e(TAG, "ID de actividad inválido al eliminar");
            Toast.makeText(getApplication(), "ID de actividad inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Eliminando asistente con ID: " + id);
        apiService.deleteAttendance(id, "Bearer " + authToken)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Asistente eliminado exitosamente");
                            cargarAsistentes(taskId); // Recargar lista
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                errorBody = "Error al leer cuerpo de error";
                            }
                            Log.e(TAG, "Error al eliminar asistente. Código: " + response.code() + ", Mensaje: " + response.message() + ", Cuerpo: " + errorBody);
                            if (response.code() == 401) {
                                Toast.makeText(getApplication(), "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), "Error al eliminar asistente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Fallo de conexión al eliminar asistente", t);
                        Toast.makeText(getApplication(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}