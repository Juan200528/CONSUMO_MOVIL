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

import com.bumptech.glide.Glide;
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
        if (token == null || token.isEmpty()) return;

        tvEmpty.setText("Cargando actividades...");
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        RetrofitClient.getApiService().obtenerActividadesOtrosUsuarios(token)
                .enqueue(new Callback<List<ActividadModel>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ActividadModel>> call, @NonNull Response<List<ActividadModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            listaOriginal.clear();
                            for (ActividadModel model : response.body()) {
                                listaOriginal.add(convertirAPIaActividad(model));
                            }
                            aplicarFiltros("", false, false, false);
                        } else {
                            actualizarVisibilidad();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ActividadModel>> call, @NonNull Throwable t) {
                        actualizarVisibilidad();
                    }
                });
    }

    private Actividad convertirAPIaActividad(ActividadModel model) {
        Actividad act = new Actividad();
        act.setId(model.getId());
        act.setTitulo(model.getTitle());
        act.setDescripcion(model.getDescription());
        act.setLugar(model.getPlace());
        act.setFecha(model.getDate());
        act.setPromocionada(model.isPromoted());
        act.setPasada(model.isPasada());
        act.setAsistido(false);
        act.setImagenRuta(model.getImage());
        act.setIdCreador(model.getUser() != null ? model.getUser().getId() : "desconocido");

        if (model.getResponsible() != null && !model.getResponsible().isEmpty()) {
            act.setResponsables(String.join(", ", model.getResponsible()));
        } else {
            act.setResponsables("Sin responsables");
        }

        return act;
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
            boolean proximas = rgFecha.getCheckedRadioButtonId() == R.id.rbFechaProximas;
            boolean pasadas = rgFecha.getCheckedRadioButtonId() == R.id.rbFechaPasadas;
            boolean promocionadas = rgEstado.getCheckedRadioButtonId() == R.id.rbEstadoPromocionadas;

            aplicarFiltros(filtroTexto, proximas, pasadas, promocionadas);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void aplicarFiltros(String texto, boolean proximas, boolean pasadas, boolean promocionadas) {
        String token = sessionManager.fetchAuthToken();
        if (token == null || token.isEmpty()) return;

        tvEmpty.setText("Buscando...");
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

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

                                if (!texto.isEmpty() && !act.getTitulo().toLowerCase().contains(texto.toLowerCase())) continue;

                                try {
                                    Date fechaAct = sdf.parse(act.getFecha());
                                    if (proximas && fechaAct != null && !fechaAct.after(hoy)) continue;
                                    if (pasadas && fechaAct != null && !fechaAct.before(hoy)) continue;
                                } catch (ParseException e) {}

                                boolean coincidePromocion = !promocionadas || act.isPromocionada();

                                if (coincidePromocion) {
                                    filtradas.add(act);
                                }
                            }

                            actividadList = filtradas;
                            adapter.updateItems(filtradas);
                            actualizarVisibilidad();
                        } else {
                            actividadList.clear();
                            adapter.updateItems(new ArrayList<>());
                            actualizarVisibilidad();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ActividadModel>> call, @NonNull Throwable t) {
                        actividadList.clear();
                        adapter.updateItems(new ArrayList<>());
                        actualizarVisibilidad();
                    }
                });
    }

    private void actualizarVisibilidad() {
        recyclerView.setVisibility(actividadList.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(actividadList.isEmpty() ? View.VISIBLE : View.GONE);
        if (actividadList.isEmpty()) {
            tvEmpty.setText("No hay actividades disponibles");
        }
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Toast.makeText(requireContext(), "Clic en: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetallesClick(Actividad actividad) {
        ActividadAdapterLista.mostrarDialogoDetalles(actividad, requireContext());
    }

    @Override
    public void onAsistirClick(Actividad actividad, int position) {
        Toast.makeText(requireContext(), "Asistiendo a: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}