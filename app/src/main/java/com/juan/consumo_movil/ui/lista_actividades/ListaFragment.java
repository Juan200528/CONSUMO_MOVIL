package com.juan.consumo_movil.ui.lista_actividades;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.Actividad;
import com.juan.consumo_movil.models.ActividadAdapterLista;
import com.juan.consumo_movil.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaFragment extends Fragment implements
        ActividadAdapterLista.OnActividadClickListener,
        ActividadAdapterLista.OnDetallesClickListener,
        ActividadAdapterLista.OnAsistirClickListener {

    private RecyclerView recyclerView;
    private ActividadAdapterLista adapter;
    private List<Actividad> actividadList = new ArrayList<>();
    private List<Actividad> listaOriginal = new ArrayList<>();
    private TextView tvEmpty;
    private ImageButton btnBuscar;
    private SessionManager sessionManager;
    private String miUsuarioId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista, container, false);

        recyclerView = view.findViewById(R.id.recyclerLista);
        btnBuscar = view.findViewById(R.id.btnBuscarLupa);
        tvEmpty = view.findViewById(R.id.tvEmptyLista);
        sessionManager = new SessionManager(requireContext());
        miUsuarioId = sessionManager.getUserId();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActividadAdapterLista(actividadList, this, this, this, sessionManager);
        recyclerView.setAdapter(adapter);

        cargarActividadesIniciales();

        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        return view;
    }

    private void cargarActividadesIniciales() {
        String token = sessionManager.fetchAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().obtenerActividadesOtrosUsuarios(token)
                .enqueue(new Callback<List<ActividadModel>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ActividadModel>> call, @NonNull Response<List<ActividadModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            listaOriginal.clear();
                            for (ActividadModel model : response.body()) {
                                Actividad act = convertirAPIaActividad(model);
                                listaOriginal.add(act);
                            }
                            actividadList = new ArrayList<>(listaOriginal);
                            adapter.updateItems(actividadList);
                            actualizarVisibilidad();
                        } else {
                            Toast.makeText(requireContext(), "Error cargando actividades ajenas", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ActividadModel>> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Actividad convertirAPIaActividad(ActividadModel model) {
        Actividad actividad = new Actividad();
        actividad.setId(model.getId());
        actividad.setTitulo(model.getTitle());
        actividad.setDescripcion(model.getDescription());
        actividad.setLugar(model.getPlace());
        actividad.setFecha(model.getDate());
        actividad.setPromocionada(model.isPromoted());
        actividad.setPasada(model.isPasada());
        actividad.setAsistido(false);
        actividad.setImagenRuta(model.getImage());
        actividad.setIdCreador(model.getUser() != null ? model.getUser().getId() : "desconocido");
        if (model.getResponsible() != null && !model.getResponsible().isEmpty()) {
            actividad.setResponsables(String.join(", ", model.getResponsible()));
        } else {
            actividad.setResponsables("Sin responsables");
        }
        return actividad;
    }

    private void mostrarDialogoBusqueda() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_buscar_filtros);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etBuscar = dialog.findViewById(R.id.etBuscar);
        RadioGroup rgFecha = dialog.findViewById(R.id.rgFechaSeleccionada);
        RadioGroup rgEstado = dialog.findViewById(R.id.rgEstadoSeleccionado);
        Button btnBuscar = dialog.findViewById(R.id.btnBuscar);

        btnBuscar.setOnClickListener(v -> {
            String filtroTexto = etBuscar.getText().toString().trim();
            boolean filtrarProximas = rgFecha.getCheckedRadioButtonId() == R.id.rbFechaProximas;
            boolean filtrarPasadas = rgFecha.getCheckedRadioButtonId() == R.id.rbFechaPasadas;
            boolean filtrarPromocionadas = rgEstado.getCheckedRadioButtonId() == R.id.rbEstadoPromocionadas;

            aplicarFiltros(filtroTexto, filtrarProximas, filtrarPasadas, filtrarPromocionadas);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void aplicarFiltros(String texto, boolean proximas, boolean pasadas, boolean promocionadas) {
        String token = sessionManager.fetchAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().searchTasks(token, texto)
                .enqueue(new Callback<List<ActividadModel>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ActividadModel>> call, @NonNull Response<List<ActividadModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Actividad> filtradas = new ArrayList<>();
                            Date hoy = new Date();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                            for (ActividadModel model : response.body()) {
                                Actividad act = convertirAPIaActividad(model);

                                // Filtro por texto (si hay texto ingresado)
                                if (!texto.isEmpty() && !act.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                                    continue;
                                }

                                // Filtro por fecha
                                boolean coincideFecha = true;
                                try {
                                    Date fechaAct = sdf.parse(act.getFecha());
                                    if (proximas && (fechaAct == null || !fechaAct.after(hoy))) coincideFecha = false;
                                    if (pasadas && (fechaAct == null || !fechaAct.before(hoy))) coincideFecha = false;
                                } catch (ParseException e) {
                                    coincideFecha = false;
                                }

                                // Filtro por promoción
                                boolean coincidePromocion = !promocionadas || act.isPromocionada();

                                // Agrega si cumple todos
                                if (coincideFecha && coincidePromocion) {
                                    filtradas.add(act);
                                }
                            }

                            actividadList = filtradas;
                            adapter.updateItems(filtradas);
                            actualizarVisibilidad();
                        } else {
                            Toast.makeText(requireContext(), "No se encontraron actividades", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ActividadModel>> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Error búsqueda", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void actualizarVisibilidad() {
        if (actividadList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        // No implementado
    }

    @Override
    public void onDetallesClick(Actividad actividad) {
        // Ahora manejado dentro de ActividadAdapterLista
    }

    @Override
    public void onAsistirClick(Actividad actividad, int position) {
        Toast.makeText(requireContext(), "Asistir a: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}
