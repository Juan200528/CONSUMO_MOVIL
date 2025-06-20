package com.juan.consumo_movil.ui.lista_actividades;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
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
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaFragment extends Fragment implements ActividadAdapterLista.OnActividadClickListener,
        ActividadAdapterLista.OnDetallesClickListener, ActividadAdapterLista.OnAsistirClickListener {

    private static final String TAG = "ListaFragment";
    private RecyclerView recyclerView;
    private ActividadAdapterLista adapter;
    private List<Actividad> actividadList;
    private List<Actividad> listaOriginal;
    private TextView tvEmpty;
    private ImageButton btnBuscar;
    private SessionManager sessionManager;

    public static ListaFragment newInstance() {
        return new ListaFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista, container, false);

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recyclerLista);
        btnBuscar = view.findViewById(R.id.btnBuscarLupa);
        tvEmpty = view.findViewById(R.id.tvEmptyLista);
        sessionManager = SessionManager.getInstance(); // ✅ Uso del Singleton

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        actividadList = new ArrayList<>();
        listaOriginal = new ArrayList<>();

        adapter = new ActividadAdapterLista(actividadList, this, this, this);
        recyclerView.setAdapter(adapter);

        cargarActividadesDesdeAPI();

        // Listener del botón de búsqueda
        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        return view;
    }

    private void cargarActividadesDesdeAPI() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getApiService();

        Call<List<ActividadModel>> call = api.obtenerActividadesOtrosUsuarios("Bearer " + token);

        call.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActividadModel> modelos = response.body();
                    List<Actividad> listaConvertida = new ArrayList<>();
                    for (ActividadModel model : modelos) {
                        Actividad actividad = convertirAPIaActividad(model);
                        listaConvertida.add(actividad);
                    }
                    listaOriginal.clear();
                    listaOriginal.addAll(listaConvertida);

                    // 🔥 Carga solo actividades ajenas al inicio
                    adapter.cargarSoloAjenas(listaOriginal);
                    actualizarVisibilidad();
                } else {
                    Log.e(TAG, "Error al obtener actividades: " + response.code());
                    Toast.makeText(requireContext(), "Error al cargar actividades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Log.e(TAG, "Fallo al llamar a la API", t);
                Toast.makeText(requireContext(), "No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
            }
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
        actividad.setFecha(model.getDate());

        if (model.getResponsible() != null && !model.getResponsible().isEmpty()) {
            actividad.setResponsables(String.join(", ", model.getResponsible()));
        } else {
            actividad.setResponsables("Sin responsables");
        }

        actividad.setPromocionada(model.isPromoted());
        actividad.setPasada(model.isPasada());
        actividad.setAsistido(false); // Esto puede venir desde otro endpoint o manejarse localmente

        // 🔥 Copiar id del creador desde el modelo de la API
        if (model.getUser() != null && model.getUser().getId() != null) {
            actividad.setIdCreador(model.getUser().getId());
            Log.d("ACTIVIDAD", "ID Creador: " + model.getUser().getId());
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
            int idFechaSeleccionada = rgFecha.getCheckedRadioButtonId();
            boolean filtrarProximas = idFechaSeleccionada == R.id.rbFechaProximas;
            boolean filtrarPasadas = idFechaSeleccionada == R.id.rbFechaPasadas;

            int idEstadoSeleccionado = rgEstado.getCheckedRadioButtonId();
            boolean filtrarPromocionadas = idEstadoSeleccionado == R.id.rbEstadoPromocionadas;

            aplicarFiltros(filtroTexto, filtrarProximas, filtrarPasadas, filtrarPromocionadas);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void aplicarFiltros(String texto, boolean proximas, boolean pasadas, boolean promocionadas) {
        if (texto.isEmpty()) {
            // ✅ Si no hay texto, filtramos localmente
            List<Actividad> resultados = new ArrayList<>();

            for (Actividad act : listaOriginal) {
                boolean coincideFecha = true;
                if (proximas) {
                    coincideFecha = esFechaFutura(act.getFecha());
                } else if (pasadas) {
                    coincideFecha = !esFechaFutura(act.getFecha());
                }

                boolean coincideEstado = true;
                if (promocionadas) {
                    coincideEstado = act.isPromocionada();
                }

                if (coincideFecha && coincideEstado) {
                    resultados.add(act);
                }
            }

            adapter.updateItems(resultados);
            Toast.makeText(requireContext(), "Mostrando " + adapter.getItemCount() + " resultado(s)", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔍 Si hay texto, hacemos búsqueda remota
        String token = SessionManager.getInstance().fetchAuthToken(); // Debe devolver "Bearer <token>"
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Token no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getApiService();

        // 🔥 Llamada al servidor
        Call<List<ActividadModel>> call = api.searchTasks(token, texto);
        call.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActividadModel> modelos = response.body();
                    List<Actividad> resultados = new ArrayList<>();

                    for (ActividadModel model : modelos) {
                        Actividad act = convertirAPIaActividad(model);

                        // ✅ Comparación exacta del título
                        boolean coincideTexto = act.getTitulo().equalsIgnoreCase(texto.trim());

                        boolean coincideFecha = true;
                        if (proximas) {
                            coincideFecha = esFechaFutura(act.getFecha());
                        } else if (pasadas) {
                            coincideFecha = !esFechaFutura(act.getFecha());
                        }

                        boolean coincideEstado = true;
                        if (promocionadas) {
                            coincideEstado = act.isPromocionada();
                        }

                        if (coincideTexto && coincideFecha && coincideEstado) {
                            resultados.add(act);
                        }
                    }

                    adapter.updateItems(resultados);
                    Toast.makeText(requireContext(), "Mostrando " + adapter.getItemCount() + " resultado(s)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "No se encontraron coincidencias", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Log.e("BuscarError", "Error en búsqueda remota", t);
                Toast.makeText(requireContext(), "Fallo al buscar actividades", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean esFechaFutura(String fechaStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        try {
            Date fecha = sdf.parse(fechaStr);
            return fecha != null && fecha.after(today);
        } catch (ParseException e) {
            return false;
        }
    }

    private void actualizarVisibilidad() {
        if (adapter.getItemCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        Toast.makeText(requireContext(), "Clic en: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetallesClick(Actividad actividad) {
        Toast.makeText(requireContext(), "Ver detalles: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAsistirClick(Actividad actividad, int position) {
        Toast.makeText(requireContext(), "Asistiendo a: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}