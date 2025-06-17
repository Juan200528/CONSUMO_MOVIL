package com.juan.consumo_movil.ui.lista_actividades;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.Actividad;
import com.juan.consumo_movil.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaViewModel extends ViewModel {

    private static final String TAG = "ListaViewModel";

    private final MutableLiveData<List<Actividad>> actividades = new MutableLiveData<>();

    public LiveData<List<Actividad>> getActividades() {
        return actividades;
    }

    public void cargarActividadesDesdeApi(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token no disponible");
            actividades.setValue(new ArrayList<>());
            return;
        }

        ApiService api = RetrofitClient.getApiService();
        Call<List<ActividadModel>> call = api.obtenerActividades("Bearer " + token);

        call.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Actividad> listaConvertida = convertirAActividadLocal(response.body());
                    actividades.setValue(listaConvertida);
                } else {
                    Log.e(TAG, "Error al obtener actividades. Código: " + response.code());
                    actividades.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Log.e(TAG, "Fallo al llamar a la API", t);
                actividades.setValue(new ArrayList<>());
            }
        });
    }

    private List<Actividad> convertirAActividadLocal(List<ActividadModel> modelos) {
        List<Actividad> lista = new ArrayList<>();

        for (ActividadModel model : modelos) {
            Actividad actividad = new Actividad();

            try {
                actividad.setId(Integer.parseInt(model.getId()));
            } catch (NumberFormatException ignored) {
                actividad.setId(0); // valor por defecto si falla el parse
            }

            actividad.setTitulo(model.getTitle());
            actividad.setDescripcion(model.getDescription());
            actividad.setLugar(model.getPlace());
            actividad.setFecha(model.getDate());

            if (model.getResponsible() != null && !model.getResponsible().isEmpty()) {
                actividad.setResponsables(String.join(", ", model.getResponsible()));
            } else {
                actividad.setResponsables("Sin responsables");
            }

            actividad.setPromocionada(model.isPromoted());
            actividad.setPasada(false); // Puedes calcular esto según la fecha si lo deseas
            actividad.setAsistido(false); // Esto puede venir desde la API o manejarse localmente
            actividad.setImagenRuta(null); // O usa URL si recibes imágenes desde la API

            lista.add(actividad);
        }

        return lista;
    }
}