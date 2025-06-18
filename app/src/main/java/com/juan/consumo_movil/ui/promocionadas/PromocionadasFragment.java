package com.juan.consumo_movil.ui.promocionadas;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.ui.promocionadas.PromocionadasAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PromocionadasFragment extends Fragment {

    private RecyclerView recyclerView;
    private PromocionadasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promocionadas, container, false);

        recyclerView = view.findViewById(R.id.recyclerPromocionadas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new PromocionadasAdapter(new ArrayList<>(), this::mostrarDialogoDetalles);
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
                    adapter.updateList(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void mostrarDialogoDetalles(ActividadModel actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Hacer que el diálogo ocupe el 80% del ancho de la pantalla
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.8f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        TextView tvTituloDetalle = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcionDetalle = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFechaDetalle = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugarDetalle = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsablesDetalle = dialog.findViewById(R.id.tvResponsablesDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvTituloDetalle.setText(actividad.getTitle());
        tvDescripcionDetalle.setText(actividad.getDescription());
        tvFechaDetalle.setText(actividad.getDate());
        tvLugarDetalle.setText(actividad.getPlace());
        tvResponsablesDetalle.setText(String.join(", ", actividad.getResponsible()));

        btnVolver.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}