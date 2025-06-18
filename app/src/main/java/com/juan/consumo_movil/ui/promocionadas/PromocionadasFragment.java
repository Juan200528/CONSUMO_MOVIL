package com.juan.consumo_movil.ui.promocionadas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.ActividadAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromocionadasFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActividadAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promocionadas, container, false);

        recyclerView = view.findViewById(R.id.recyclerPromocionadas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar adapter con lista vacía por ahora
        adapter = new ActividadAdapter(
                getContext(),
                new ArrayList<>(),
                actividad -> {}, // onActividadClickListener
                actividad -> {}, // onEliminarClickListener
                actividad -> {}, // onEditarClickListener
                (actividadModel, v) -> {} // onDetallesClickListener
        );
        recyclerView.setAdapter(adapter);

        cargarActividadesPromocionadas();

        return view;
    }

    private void cargarActividadesPromocionadas() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<ActividadModel>> call = apiService.getPromotedTasks();

        call.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActividadModel> actividades = response.body();

                    List<ActividadAdapter.Item> items = actividades.stream()
                            .map(actividad -> new ActividadAdapter.Item(
                                    ActividadAdapter.Item.TYPE_ACTIVIDAD,
                                    actividad,
                                    null,
                                    null
                            ))
                            .collect(Collectors.toList());

                    adapter.updateItems(items);
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
