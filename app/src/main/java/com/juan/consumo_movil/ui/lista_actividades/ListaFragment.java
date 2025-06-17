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
import androidx.lifecycle.ViewModelProvider;
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

    private ListaViewModel listaViewModel;

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
        sessionManager = new SessionManager(requireContext());

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        actividadList = new ArrayList<>();
        listaOriginal = new ArrayList<>();
        adapter = new ActividadAdapterLista(actividadList, this, this, this);
        recyclerView.setAdapter(adapter);

        // Inicializar ViewModel
        listaViewModel = new ViewModelProvider(this).get(ListaViewModel.class);

        // Observar cambios en las actividades
        listaViewModel.getActividades().observe(getViewLifecycleOwner(), actividades -> {
            if (actividades != null) {
                listaOriginal.clear();
                listaOriginal.addAll(actividades);
                actividadList.clear();
                actividadList.addAll(listaOriginal);
                adapter.notifyDataSetChanged();
                actualizarVisibilidad();
            }
        });

        // Cargar actividades desde el ViewModel
        listaViewModel.cargarActividadesDesdeApi(requireContext());

        // Listener del botón de búsqueda
        btnBuscar.setOnClickListener(v -> mostrarDialogoBusqueda());

        return view;
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
        actividadList.clear();
        for (Actividad act : listaOriginal) {
            boolean coincideTexto = texto.isEmpty() ||
                    act.getTitulo().toLowerCase(Locale.getDefault()).contains(texto.toLowerCase(Locale.getDefault())) ||
                    (act.getDescripcion() != null && act.getDescripcion().toLowerCase(Locale.getDefault()).contains(texto.toLowerCase(Locale.getDefault()))) ||
                    (act.getResponsables() != null && act.getResponsables().toLowerCase(Locale.getDefault()).contains(texto.toLowerCase(Locale.getDefault())));

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
                actividadList.add(act);
            }
        }

        adapter.notifyDataSetChanged();
        actualizarVisibilidad();

        Toast.makeText(requireContext(), "Mostrando " + actividadList.size() + " resultados", Toast.LENGTH_SHORT).show();
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