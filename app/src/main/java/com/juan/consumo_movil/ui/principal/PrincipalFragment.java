package com.juan.consumo_movil.ui.principal;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.Actividad;
import com.juan.consumo_movil.models.ActividadAdapter;
import com.juan.consumo_movil.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrincipalFragment extends Fragment implements ActividadAdapter.OnActividadClickListener {

    private static final String TAG = "PrincipalFragment";

    private RecyclerView recyclerActividades;
    private TextView tvEmptyActividades;
    private ActividadAdapter actividadAdapter;
    private List<ActividadAdapter.Item> itemList;
    private String userId;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_principal, container, false);

        recyclerActividades = root.findViewById(R.id.recyclerActividades);
        tvEmptyActividades = root.findViewById(R.id.tvEmptyActividades);

        SessionManager sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getUserId();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerActividades.setLayoutManager(layoutManager);
        recyclerActividades.setHasFixedSize(true);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerActividades);

        itemList = new ArrayList<>();
        actividadAdapter = new ActividadAdapter(itemList, this, this::mostrarDialogoEliminar, this::mostrarDialogoEditar, this::mostrarDialogoDetalles);
        recyclerActividades.setAdapter(actividadAdapter);

        cargarActividades();

        return root;
    }

    public void cargarActividades() {
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Error: Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getApiService();
        Call<List<ActividadModel>> call = api.obtenerActividades("Bearer " + token);

        call.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    procesarActividadesDeAPI(response.body());
                } else {
                    Log.e(TAG, "Error al obtener actividades. Código: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar actividades", Toast.LENGTH_SHORT).show();
                    actualizarVisibilidad();
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Log.e(TAG, "Fallo de conexión", t);
                Toast.makeText(getContext(), "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
                actualizarVisibilidad();
            }
        });
    }

    private void procesarActividadesDeAPI(List<ActividadModel> actividadesAPI) {
        executorService.execute(() -> {
            List<ActividadAdapter.Item> tempItemList = new ArrayList<>();

            List<Actividad> actividadesConvertidas = new ArrayList<>();
            for (ActividadModel model : actividadesAPI) {
                actividadesConvertidas.add(convertirAPIaActividad(model));
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaHoy;
            try {
                fechaHoy = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                fechaHoy = new Date();
            }

            List<Actividad> actividadesActuales = new ArrayList<>();
            List<Actividad> actividadesPasadas = new ArrayList<>();

            for (Actividad act : actividadesConvertidas) {
                try {
                    Date fechaActividad = sdf.parse(act.getFecha());
                    if (fechaActividad.before(fechaHoy)) {
                        act.setPasada(true);
                        actividadesPasadas.add(act);
                    } else {
                        act.setPasada(false);
                        actividadesActuales.add(act);
                    }
                } catch (ParseException e) {
                    act.setPasada(false);
                    actividadesActuales.add(act);
                }
            }

            Collections.sort(actividadesActuales, (a1, a2) -> {
                try {
                    return sdf.parse(a1.getFecha()).compareTo(sdf.parse(a2.getFecha()));
                } catch (ParseException e) {
                    return 0;
                }
            });

            for (Actividad act : actividadesActuales) {
                tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_ACTIVIDAD, act, null, null));
            }

            if (!actividadesPasadas.isEmpty()) {
                Collections.sort(actividadesPasadas, (a1, a2) -> {
                    try {
                        return sdf.parse(a2.getFecha()).compareTo(sdf.parse(a1.getFecha()));
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_TITULO, null,
                        getString(R.string.actividades_pasadas).toUpperCase(Locale.getDefault()), null));
                tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_PASADAS, null, null, actividadesPasadas));
            }

            requireActivity().runOnUiThread(() -> {
                itemList.clear();
                itemList.addAll(tempItemList);
                actividadAdapter.notifyDataSetChanged();
                actualizarVisibilidad();
            });
        });
    }

    private Actividad convertirAPIaActividad(ActividadModel model) {
        Actividad actividad = new Actividad();
        try {
            actividad.setId(Integer.parseInt(model.getId()));
        } catch (NumberFormatException ignored) {
            actividad.setId(0);
        }
        actividad.setTitulo(model.getTitle());
        actividad.setDescripcion(model.getDescription());
        actividad.setLugar(model.getPlace());
        actividad.setIdCreador(userId);
        if (model.getDate() != null) {
            actividad.setFecha(model.getDate());
        }
        if (model.getResponsible() != null && !model.getResponsible().isEmpty()) {
            actividad.setResponsables(String.join(", ", model.getResponsible()));
        }
        actividad.setPromocionada(model.isPromoted());
        return actividad;
    }

    private void mostrarDialogoEliminar(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_eliminar_actividad);
        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            // Aquí puedes llamar a una API para eliminar si lo deseas
            Toast.makeText(getContext(), "Funcionalidad de eliminación no disponible", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoEditar(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_editar_actividad);
        EditText etEditarTitulo = dialog.findViewById(R.id.etEditarTitulo);
        EditText etEditarDescripcion = dialog.findViewById(R.id.etEditarDescripcion);
        EditText etEditarFecha = dialog.findViewById(R.id.etEditarFecha);
        EditText etEditarLugar = dialog.findViewById(R.id.etEditarLugar);
        EditText etEditarResponsables = dialog.findViewById(R.id.etEditarResponsables);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarCambios);

        etEditarTitulo.setText(actividad.getTitulo());
        etEditarDescripcion.setText(actividad.getDescripcion());
        etEditarFecha.setText(actividad.getFecha());
        etEditarLugar.setText(actividad.getLugar());
        etEditarResponsables.setText(actividad.getResponsables());

        btnGuardar.setOnClickListener(v -> {
            actividad.setTitulo(etEditarTitulo.getText().toString());
            actividad.setDescripcion(etEditarDescripcion.getText().toString());
            String nuevaFecha = etEditarFecha.getText().toString();
            actividad.setFecha(nuevaFecha);
            actividad.setLugar(etEditarLugar.getText().toString());
            actividad.setResponsables(etEditarResponsables.getText().toString());

            try {
                Date currentDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date actividadDate = sdf.parse(nuevaFecha);
                actividad.setPasada(actividadDate.before(currentDate));
            } catch (ParseException e) {
                actividad.setPasada(false);
            }

            Toast.makeText(getContext(), "Funcionalidad de edición no disponible", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoDetalles(Actividad actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        TextView tvTituloDetalle = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcionDetalle = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFechaDetalle = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugarDetalle = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsablesDetalle = dialog.findViewById(R.id.tvResponsablesDetalle);
        Switch switchPromocion = dialog.findViewById(R.id.switchPromocion);

        tvTituloDetalle.setText(actividad.getTitulo());
        tvDescripcionDetalle.setText(actividad.getDescripcion());
        tvFechaDetalle.setText(actividad.getFecha());
        tvLugarDetalle.setText(actividad.getLugar());
        tvResponsablesDetalle.setText(actividad.getResponsables());
        switchPromocion.setChecked(actividad.isPromocionada());

        dialog.findViewById(R.id.btnVolver).setOnClickListener(v -> dialog.dismiss());

        switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(getContext(), "Promocionando: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Desactivado: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void actualizarVisibilidad() {
        recyclerActividades.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyActividades.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Toast.makeText(getContext(), "Clic en: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}