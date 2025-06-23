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
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionarViewModel extends AndroidViewModel {

    private MutableLiveData<List<Asistente>> asistentes;
    private ApiService apiService;
    private String authToken;

    public GestionarViewModel(Application application) {
        super(application);
        asistentes = new MutableLiveData<>(new ArrayList<>());
        apiService = RetrofitClient.getApiService();
        authToken = application.getSharedPreferences("user_prefs", Application.MODE_PRIVATE)
                .getString("auth_token", "");
        Log.d("GestionarViewModel", "Token de autenticación: " + authToken);
    }

    public LiveData<List<Asistente>> getAsistentes() {
        return asistentes;
    }

    public void cargarAsistentes(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            Log.e("GestionarViewModel", "taskId es null o vacío");
            Toast.makeText(getApplication(), "ID de actividad inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("GestionarViewModel", "Cargando asistentes para taskId: " + taskId);
        apiService.getAttendees(taskId, "Bearer " + authToken).enqueue(new Callback<List<Asistente>>() {
            @Override
            public void onResponse(Call<List<Asistente>> call, Response<List<Asistente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("GestionarViewModel", "Asistentes cargados: " + response.body().size());
                    asistentes.postValue(response.body());
                } else {
                    Log.e("GestionarViewModel", "Error al cargar asistentes. Código: " + response.code() + ", Mensaje: " + response.message());
                    if (response.code() == 401) {
                        Toast.makeText(getApplication(), "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplication(), "Error al cargar asistentes", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Asistente>> call, Throwable t) {
                Log.e("GestionarViewModel", "Fallo al cargar asistentes: " + t.getMessage());
                Toast.makeText(getApplication(), "Fallo de conexión al cargar asistentes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void insertarAsistente(Asistente asistente, String taskId) {
        Gson gson = new Gson();
        Log.d("GestionarViewModel", "Enviando asistente: " + gson.toJson(asistente));
        apiService.confirmAttendance("Bearer " + authToken, asistente)
                .enqueue(new Callback<Asistente>() {
                    @Override
                    public void onResponse(Call<Asistente> call, Response<Asistente> response) {
                        if (response.isSuccessful()) {
                            Log.d("GestionarViewModel", "Asistente insertado exitosamente. Respuesta: " + gson.toJson(response.body()));
                            cargarAsistentes(taskId);
                        } else {
                            String errorBody = null;
                            try {
                                errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin cuerpo de error";
                            } catch (Exception e) {
                                errorBody = "Error al leer el cuerpo de error: " + e.getMessage();
                            }
                            Log.e("GestionarViewModel", "Error al agregar asistente. Código: " + response.code() + ", Mensaje: " + response.message() + ", Cuerpo de error: " + errorBody);
                            if (response.code() == 401) {
                                Toast.makeText(getApplication(), "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), "Error al agregar asistente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Asistente> call, Throwable t) {
                        Log.e("GestionarViewModel", "Fallo de conexión al insertar asistente: " + t.getMessage());
                        Toast.makeText(getApplication(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void actualizarAsistente(String id, Asistente asistente, String taskId) {
        Gson gson = new Gson();
        Log.d("GestionarViewModel", "Actualizando asistente con ID: " + id + ", Datos: " + gson.toJson(asistente));
        apiService.updateAttendance(id, "Bearer " + authToken, asistente)
                .enqueue(new Callback<Asistente>() {
                    @Override
                    public void onResponse(Call<Asistente> call, Response<Asistente> response) {
                        if (response.isSuccessful()) {
                            Log.d("GestionarViewModel", "Asistente actualizado exitosamente. Respuesta: " + gson.toJson(response.body()));
                            cargarAsistentes(taskId);
                        } else {
                            String errorBody = null;
                            try {
                                errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin cuerpo de error";
                            } catch (Exception e) {
                                errorBody = "Error al leer el cuerpo de error: " + e.getMessage();
                            }
                            Log.e("GestionarViewModel", "Error al actualizar asistente. Código: " + response.code() + ", Mensaje: " + response.message() + ", Cuerpo de error: " + errorBody);
                            if (response.code() == 401) {
                                Toast.makeText(getApplication(), "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), "Error al actualizar asistente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Asistente> call, Throwable t) {
                        Log.e("GestionarViewModel", "Fallo de conexión al actualizar asistente: " + t.getMessage());
                        Toast.makeText(getApplication(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void eliminarAsistentePorId(String id, String taskId) {
        Log.d("GestionarViewModel", "Eliminando asistente con ID: " + id);
        apiService.deleteAttendance(id, "Bearer " + authToken)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("GestionarViewModel", "Asistente eliminado exitosamente");
                            cargarAsistentes(taskId);
                        } else {
                            String errorBody = null;
                            try {
                                errorBody = response.errorBody() != null ? response.errorBody().string() : "Sin cuerpo de error";
                            } catch (Exception e) {
                                errorBody = "Error al leer el cuerpo de error: " + e.getMessage();
                            }
                            Log.e("GestionarViewModel", "Error al eliminar asistente. Código: " + response.code() + ", Mensaje: " + response.message() + ", Cuerpo de error: " + errorBody);
                            if (response.code() == 401) {
                                Toast.makeText(getApplication(), "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplication(), "Error al eliminar asistente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("GestionarViewModel", "Fallo de conexión al eliminar asistente: " + t.getMessage());
                        Toast.makeText(getApplication(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}