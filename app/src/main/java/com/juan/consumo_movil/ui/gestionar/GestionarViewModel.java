package com.juan.consumo_movil.ui.gestionar;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private MutableLiveData<String> errorMessage;
    private ApiService apiService;
    private String authToken;

    public GestionarViewModel(Application application) {
        super(application);
        asistentes = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        apiService = RetrofitClient.getApiService();
        authToken = application.getSharedPreferences("user_prefs", Application.MODE_PRIVATE)
                .getString("auth_token", "");
    }

    public LiveData<List<Asistente>> getAsistentes() {
        return asistentes;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void cargarAsistentes(String taskId) {
        apiService.getAttendees(taskId, "Bearer " + authToken).enqueue(new Callback<List<Asistente>>() {
            @Override
            public void onResponse(Call<List<Asistente>> call, Response<List<Asistente>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    asistentes.setValue(response.body());
                } else {
                    asistentes.setValue(new ArrayList<>());
                    errorMessage.setValue("Error al cargar asistentes: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Asistente>> call, Throwable t) {
                asistentes.setValue(new ArrayList<>());
                errorMessage.setValue("Fallo de conexión: " + t.getMessage());
            }
        });
    }

    public void insertarAsistente(Asistente asistente) {
        apiService.confirmAttendance("Bearer " + authToken, asistente).enqueue(new Callback<Asistente>() {
            @Override
            public void onResponse(Call<Asistente> call, Response<Asistente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cargarAsistentes(String.valueOf(asistente.getIdActividad()));
                } else {
                    errorMessage.setValue("Error al agregar asistente: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Asistente> call, Throwable t) {
                errorMessage.setValue("Fallo al agregar asistente: " + t.getMessage());
            }
        });
    }

    public void actualizarAsistente(Asistente asistente) {
        apiService.confirmAttendance("Bearer " + authToken, asistente).enqueue(new Callback<Asistente>() {
            @Override
            public void onResponse(Call<Asistente> call, Response<Asistente> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cargarAsistentes(String.valueOf(asistente.getIdActividad()));
                } else {
                    errorMessage.setValue("Error al actualizar asistente: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Asistente> call, Throwable t) {
                errorMessage.setValue("Fallo al actualizar asistente: " + t.getMessage());
            }
        });
    }

    public void eliminarAsistente(Asistente asistente) {
        apiService.cancelAttendance(String.valueOf(asistente.getIdActividad()), "Bearer " + authToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cargarAsistentes(String.valueOf(asistente.getIdActividad()));
                } else {
                    errorMessage.setValue("Error al eliminar asistente: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue("Fallo al eliminar asistente: " + t.getMessage());
            }
        });
    }
}