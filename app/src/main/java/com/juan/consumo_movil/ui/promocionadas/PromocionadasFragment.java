package com.juan.consumo_movil.ui.promocionadas;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_promocionadas, container, false);

        // Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerPromocionadas);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        // Inicializar adaptador con lista vacía y listener de detalles
        adapter = new PromocionadasAdapter(new ArrayList<>(), this::mostrarDialogoDetalles);
        recyclerView.setAdapter(adapter);

        // Cargar datos desde la API
        cargarActividadesPromocionadas();

        return view;
    }

    /**
     * Método que llama a la API para obtener las actividades promocionadas
     */
    private void cargarActividadesPromocionadas() {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<ActividadModel>> call = apiService.getPromotedTasks(); // Asegúrate que este método exista

        call.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Actualizar el adaptador con la lista obtenida
                    adapter.updateList(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                t.printStackTrace();
                // Aquí puedes mostrar un mensaje de error si lo deseas
            }
        });
    }

    /**
     * Muestra un diálogo con los detalles de la actividad seleccionada
     *
     * @param actividad El modelo de la actividad seleccionada
     */
    private void mostrarDialogoDetalles(ActividadModel actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Sin título
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Fondo transparente

        // Ajustar tamaño del diálogo (80% del ancho de pantalla)
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.8f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        // Vincular vistas del diálogo
        TextView tvTituloDetalle = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcionDetalle = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFechaDetalle = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugarDetalle = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsablesDetalle = dialog.findViewById(R.id.tvResponsablesDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        // Asignar valores desde el modelo
        tvTituloDetalle.setText(actividad.getTitle());
        tvDescripcionDetalle.setText(actividad.getDescription());
        tvFechaDetalle.setText(actividad.getDate());
        tvLugarDetalle.setText(actividad.getPlace());
        tvResponsablesDetalle.setText(String.join(", ", actividad.getResponsible()));

        // Botón para cerrar el diálogo
        btnVolver.setOnClickListener(v -> dialog.dismiss());

        // Mostrar el diálogo
        dialog.show();
    }
}