package com.juan.consumo_movil.ui.principal;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.ActividadAdapter;
import com.juan.consumo_movil.models.PromotionRequest;
import com.juan.consumo_movil.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrincipalFragment extends Fragment implements ActividadAdapter.OnActividadClickListener {

    private static final String TAG = "PrincipalFragment";
    private RecyclerView recyclerActividades;
    private TextView tvEmptyActividades;
    private ActividadAdapter actividadAdapter;
    private List<ActividadAdapter.Item> itemList;
    private ExecutorService executorService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_principal, container, false);

        // Inicializar vistas
        recyclerActividades = root.findViewById(R.id.recyclerActividades);
        tvEmptyActividades = root.findViewById(R.id.tvEmptyActividades);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerActividades.setLayoutManager(layoutManager);
        recyclerActividades.setHasFixedSize(true);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerActividades);

        // Preparar lista y adaptador
        itemList = new ArrayList<>();
        actividadAdapter = new ActividadAdapter(
                requireContext(),
                itemList,
                this,
                this::mostrarDialogoEliminar,
                this::mostrarDialogoEditar,
                this::mostrarDialogoDetalles
        );
        recyclerActividades.setAdapter(actividadAdapter);

        cargarActividades();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarActividades(); // Recargar actividades al regresar al fragmento
    }

    public void cargarActividades() {
        SessionManager sessionManager = new SessionManager(requireContext());
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Error: Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        tvEmptyActividades.setText("Cargando actividades...");
        tvEmptyActividades.setVisibility(View.VISIBLE);
        recyclerActividades.setVisibility(View.GONE);

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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date fechaHoy;
            try {
                fechaHoy = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                fechaHoy = new Date();
            }

            List<ActividadModel> actividadesActuales = new ArrayList<>();
            List<ActividadModel> actividadesPasadas = new ArrayList<>();

            for (ActividadModel act : actividadesAPI) {
                try {
                    Date fechaActividad = sdf.parse(act.getDate());
                    if (fechaActividad.before(fechaHoy)) {
                        actividadesPasadas.add(act);
                    } else {
                        actividadesActuales.add(act);
                    }
                } catch (ParseException e) {
                    actividadesActuales.add(act);
                }
            }

            Collections.sort(actividadesActuales, (a1, a2) -> {
                try {
                    return sdf.parse(a1.getDate()).compareTo(sdf.parse(a2.getDate()));
                } catch (ParseException e) {
                    return 0;
                }
            });

            for (ActividadModel act : actividadesActuales) {
                tempItemList.add(new ActividadAdapter.Item(ActividadAdapter.Item.TYPE_ACTIVIDAD, act, null, null));
            }

            if (!actividadesPasadas.isEmpty()) {
                Collections.sort(actividadesPasadas, (a1, a2) -> {
                    try {
                        return sdf.parse(a2.getDate()).compareTo(sdf.parse(a1.getDate()));
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
                if (!itemList.isEmpty()) {
                    recyclerActividades.smoothScrollToPosition(0);
                }
            });
        });
    }

    private void mostrarDialogoEliminar(ActividadModel actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_eliminar_actividad);
        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(requireContext());
            String token = sessionManager.getToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(getContext(), "Error: Token no disponible", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            ApiService api = RetrofitClient.getApiService();
            Call<Void> call = api.eliminarActividad("Bearer " + token, actividad.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        itemList.removeIf(item -> item.getActividadModel() != null &&
                                item.getActividadModel().equals(actividad));
                        actividadAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Actividad eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error al eliminar del servidor", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                    actualizarVisibilidad();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void mostrarDialogoEditar(ActividadModel actividad) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_editar_actividad);
        EditText etEditarTitulo = dialog.findViewById(R.id.etEditarTitulo);
        EditText etEditarDescripcion = dialog.findViewById(R.id.etEditarDescripcion);
        EditText etEditarFecha = dialog.findViewById(R.id.etEditarFecha);
        EditText etEditarLugar = dialog.findViewById(R.id.etEditarLugar);
        EditText etEditarResponsables = dialog.findViewById(R.id.etEditarResponsables);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarCambios);
        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar); // Botón X

        etEditarTitulo.setText(actividad.getTitle());
        etEditarDescripcion.setText(actividad.getDescription());
        etEditarFecha.setText(actividad.getDate());
        etEditarLugar.setText(actividad.getPlace());
        etEditarResponsables.setText(String.join(", ", actividad.getResponsible()));

        // Evitar teclado y abrir calendario
        etEditarFecha.setKeyListener(null);
        etEditarFecha.setFocusable(false);
        etEditarFecha.setOnClickListener(v -> mostrarCalendario(etEditarFecha));

        btnGuardar.setOnClickListener(v -> {
            actividad.setTitle(etEditarTitulo.getText().toString());
            actividad.setDescription(etEditarDescripcion.getText().toString());
            actividad.setDate(etEditarFecha.getText().toString());
            actividad.setPlace(etEditarLugar.getText().toString());
            actividad.setResponsible(List.of(etEditarResponsables.getText().toString().split(", ")));
            actividadAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Cambios guardados", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void mostrarCalendario(EditText editTextFecha) {
        String fechaActual = editTextFecha.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        if (!fechaActual.isEmpty()) {
            try {
                cal.setTime(sdf.parse(fechaActual));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(requireContext(), R.style.DatePickerTheme_Custom);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                contextThemeWrapper,
                (view, year1, month1, day1) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, day1);
                    String formattedDate = sdf.format(selectedDate.getTime());
                    editTextFecha.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.setOnShowListener(dialogInterface -> {
            try {
                DatePickerDialog d = (DatePickerDialog) dialogInterface;
                d.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF4CAF50"));
                d.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FF4CAF50"));
            } catch (Exception e) {
                Log.e("DatePicker", "Error al cambiar color de botones", e);
            }
        });

        datePickerDialog.show();
    }

    private void mostrarDialogoDetalles(ActividadModel actividad, View itemView) {
        Dialog dialog = new Dialog(itemView.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Sin título
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Fondo transparente

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (itemView.getResources().getDisplayMetrics().widthPixels * 0.8f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        TextView tvTituloDetalle = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcionDetalle = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFechaDetalle = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugarDetalle = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsablesDetalle = dialog.findViewById(R.id.tvResponsablesDetalle);
        Switch switchPromocion = dialog.findViewById(R.id.switchPromocion);
        Button btnEditar = dialog.findViewById(R.id.btnEditar);
        Button btnEliminar = dialog.findViewById(R.id.btnEliminar);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);

        tvTituloDetalle.setText(actividad.getTitle());
        tvDescripcionDetalle.setText(actividad.getDescription());
        tvFechaDetalle.setText(actividad.getDate());
        tvLugarDetalle.setText(actividad.getPlace());
        tvResponsablesDetalle.setText(String.join(", ", actividad.getResponsible()));

        if (ivImagenDetalle != null && actividad.getImage() != null && !actividad.getImage().isEmpty()) {
            Glide.with(this)
                    .load(actividad.getImage())
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(ivImagenDetalle);
        } else if (ivImagenDetalle != null) {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        if (switchPromocion != null) {
            switchPromocion.setChecked(actividad.isPromoted());
            switchPromocion.setEnabled(!actividad.isPasada());

            switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SessionManager sessionManager = new SessionManager(requireContext());
                String token = sessionManager.getToken();
                if (token == null || token.isEmpty()) {
                    Toast.makeText(itemView.getContext(), "Error: Token no disponible", Toast.LENGTH_SHORT).show();
                    switchPromocion.setChecked(!isChecked);
                    return;
                }

                String startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
                String endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                        .format(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)); // 30 días

                PromotionRequest request = new PromotionRequest(
                        actividad.getId(),
                        isChecked,
                        startDate,
                        endDate
                );

                ApiService api = RetrofitClient.getApiService();
                api.promoteTask(actividad.getId(), request).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            actividad.setPromoted(isChecked);
                            Toast.makeText(buttonView.getContext(),
                                    isChecked ? "Actividad promocionada" : "Promoción desactivada",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(buttonView.getContext(), "Error al actualizar promoción", Toast.LENGTH_SHORT).show();
                            switchPromocion.setChecked(!isChecked);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(buttonView.getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                        switchPromocion.setChecked(!isChecked);
                    }
                });
            });
        }

        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoEditar(actividad);
            });
        }

        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoEliminar(actividad);
            });
        }

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void actualizarVisibilidad() {
        recyclerActividades.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyActividades.setVisibility(itemList.isEmpty() ? View.VISIBLE : View.GONE);
        if (itemList.isEmpty()) {
            tvEmptyActividades.setText("No hay actividades disponibles");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public void onActividadClick(ActividadModel actividad) {
        Toast.makeText(getContext(), "Clic en: " + actividad.getTitle(), Toast.LENGTH_SHORT).show();
    }
}