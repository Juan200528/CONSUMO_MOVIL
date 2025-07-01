package com.juan.consumo_movil.ui.lista_actividades;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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
import com.juan.consumo_movil.LocalAttendanceManager;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.Actividad;
import com.juan.consumo_movil.models.ActividadAdapterLista;
import com.juan.consumo_movil.models.Asistente;
import com.juan.consumo_movil.ui.gestionar.GestionarFragment;
import com.juan.consumo_movil.utils.SessionManager;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaFragment extends Fragment implements
        ActividadAdapterLista.OnActividadClickListener,
        ActividadAdapterLista.OnDetallesClickListener,
        ActividadAdapterLista.OnAsistirClickListener,
        ActividadAdapterLista.OnEditarClickListener,
        ActividadAdapterLista.OnEliminarClickListener,
        ActividadAdapterLista.OnPromocionarClickListener,
        ActividadAdapterLista.OnGestionarAsistentesClickListener {

    private RecyclerView recyclerView;
    private ActividadAdapterLista adapter;
    private List<Actividad> actividadList = new ArrayList<>();
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
        adapter = new ActividadAdapterLista(
                actividadList,
                this,
                this,
                this,
                this,
                this,
                this,
                this,
                sessionManager
        );
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
                            List<Actividad> nuevasActividades = new ArrayList<>();

                            for (ActividadModel model : response.body()) {
                                Actividad act = convertirAPIaActividad(model);

                                // 👇 Busca si ya estaba marcado como asistido antes del refresco
                                boolean yaAsiste = false;
                                String oldAttendanceId = null;

                                for (Actividad oldAct : actividadList) {
                                    if (oldAct.getId().equals(act.getId())) {
                                        yaAsiste = oldAct.isAsistido();
                                        oldAttendanceId = oldAct.getAttendanceId(); // Necesitas tener este campo en tu clase Actividad
                                        break;
                                    }
                                }

                                // 👇 Actualizamos los valores de asistencia
                                act.setAsistido(yaAsiste);
                                act.setAttendanceId(oldAttendanceId); // Si existe, se mantiene

                                nuevasActividades.add(act);
                            }

                            actividadList = nuevasActividades;
                            adapter.updateItems(actividadList);
                            actualizarVisibilidad();
                        } else {
                            actualizarVisibilidad();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<ActividadModel>> call, @NonNull Throwable t) {
                        Log.e("ListaFragment", "Fallo al obtener actividades", t);
                        Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
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
        act.setImagenRuta(model.getImage());

        // Manejar el ID del creador de forma segura
        String creadorId = "desconocido";
        if (model.getUser() != null && model.getUser().getId() != null) {
            creadorId = model.getUser().getId();
        }
        act.setIdCreador(creadorId);

        // Mantener estado local si ya estaba marcado antes
        boolean yaAsiste = false;
        String oldAttendanceId = null;

        // Buscar en la lista anterior
        for (Actividad oldAct : actividadList) {
            if (oldAct != null && oldAct.getId() != null && oldAct.getId().equals(act.getId())) {
                yaAsiste = oldAct.isAsistido();
                oldAttendanceId = oldAct.getAttendanceId();
                break;
            }
        }

        // Si no está guardado localmente, consulta en SharedPreferences
        if (!yaAsiste) {
            Set<String> attendances = LocalAttendanceManager.getAllAttendances(requireContext());
            for (String item : attendances) {
                if (item != null && item.contains("|") && item.startsWith(act.getId())) {
                    String[] parts = item.split("\\|");
                    if (parts.length > 1) {
                        yaAsiste = true;
                        oldAttendanceId = parts[1];
                        break;
                    }
                }
            }
        }

        // Asignar valores finales a la actividad
        act.setAsistido(yaAsiste);
        act.setAttendanceId(oldAttendanceId);

        // Responsables
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
        if (getActivity() == null || getActivity().isFinishing()) return;
        new android.os.Handler(Looper.getMainLooper()).post(() -> {
            try {
                ActividadAdapterLista.mostrarDialogoDetalles(actividad, requireContext());
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void onAsistirClick(Actividad actividad, int position) {
        if (actividad.isAsistido()) {
            mostrarDialogoCancelarAsistencia(actividad, position);
        } else {
            mostrarDialogoConfirmarAsistir(actividad, position);
        }
    }

    private void mostrarDialogoConfirmarAsistir(Actividad actividad, int position) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_asistir);

        EditText etNombre = dialog.findViewById(R.id.etNombreAsistir);
        EditText etEmail = dialog.findViewById(R.id.etEmailAsistir);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            confirmarAsistencia(actividad, position, nombre, email);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void confirmarAsistencia(Actividad actividad, int position, String nombre, String email) {
        String token = sessionManager.fetchAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontró sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = miUsuarioId;
        String taskId = actividad.getId();

        // Validación adicional: asegurarse que el ID no sea nulo o vacío
        if (taskId == null || taskId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "ID de actividad inválido", Toast.LENGTH_SHORT).show();
            Log.e("confirmarAsistencia", "El ID de la actividad es nulo o vacío");
            return;
        }

        Asistente asistente = new Asistente(
                null, // id (null porque es nuevo)
                userId,
                taskId,
                nombre,
                nombre.split(" ").length > 0 ? nombre.split(" ")[0] : nombre, // Evita fallos si no hay espacio
                email,
                actividad.getTitulo()
        );

        RetrofitClient.getApiService().confirmAttendance("Bearer " + token, asistente)
                .enqueue(new Callback<Asistente>() {
                    @Override
                    public void onResponse(Call<Asistente> call, Response<Asistente> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Asistente res = response.body();
                            actividad.setAsistido(true);

                            // Solo guardamos el attendanceId si viene del servidor
                            String serverAttendanceId = res.getId();
                            if (serverAttendanceId != null && !serverAttendanceId.isEmpty()) {
                                actividad.setAttendanceId(serverAttendanceId);
                                LocalAttendanceManager.saveAttendance(requireContext(), actividad.getId(), serverAttendanceId);
                            }

                            adapter.notifyItemChanged(position);
                            Toast.makeText(requireContext(), "Ahora asistes a " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                                Log.e("confirmarAsistencia", "Error en API: " + errorBody);
                                Toast.makeText(requireContext(), "Fallo al asistir", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(requireContext(), "No se pudo procesar la respuesta", Toast.LENGTH_SHORT).show();
                                Log.e("confirmarAsistencia", "Fallo al leer error body", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Asistente> call, Throwable t) {
                        Log.e("confirmarAsistencia", "Fallo de red: " + t.getMessage());
                        Toast.makeText(requireContext(), "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void mostrarDialogoCancelarAsistencia(Actividad actividad, int position) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_cancelar_asistencia);

        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            cancelarAsistencia(actividad, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void cancelarAsistencia(Actividad actividad, int position) {
        String rawToken = sessionManager.fetchAuthToken();
        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontró sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + rawToken;
        String attendanceId = actividad.getAttendanceId(); // ✅ Usamos el ID correcto

        if (attendanceId == null || attendanceId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Asistencia no encontrada", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService().deleteAttendance(token, attendanceId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Asistencia eliminada", Toast.LENGTH_SHORT).show();
                            actividad.setAsistido(false);
                            actividad.setAttendanceId(null);
                            LocalAttendanceManager.removeAttendance(requireContext(), actividad.getId());
                            adapter.notifyItemChanged(position);
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                                Log.e("CancelarAsistencia", "Error en API: " + errorBody);
                                Toast.makeText(requireContext(), "Fallo al cancelar", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(requireContext(), "Error al leer respuesta", Toast.LENGTH_SHORT).show();
                                Log.e("CancelarAsistencia", "Fallo al leer error body", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("CancelarAsistencia", "Fallo de red: " + t.getMessage());
                        Toast.makeText(requireContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    public void onEditarClick(Actividad actividad) {
        ActividadAdapterLista.mostrarDialogoEditar(actividad, requireContext(), this::guardarCambios);
    }

    @Override
    public void onEliminarClick(Actividad actividad) {
        ActividadAdapterLista.mostrarDialogoEliminar(actividad, requireContext(), this::eliminarActividad);
    }

    private void guardarCambios(Actividad actividad) {
        Toast.makeText(requireContext(), "Cambios guardados: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
        adapter.updateItems(actividadList);
    }

    private void eliminarActividad(Actividad actividad) {
        actividadList.remove(actividad);
        adapter.updateItems(actividadList);
        Toast.makeText(requireContext(), "Actividad eliminada: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPromocionarClick(Actividad actividad, boolean isChecked) {
        Toast.makeText(requireContext(),
                (isChecked ? "Promocionar: " : "Despromocionar: ") + actividad.getTitulo(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGestionarAsistentesClick(Actividad actividad) {
        String activityId = actividad.getId();
        String activityTitle = actividad.getTitulo();

        if (activityId == null || activityId.isEmpty()) {
            Toast.makeText(requireContext(), "ID de actividad inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sessionManager.fetchAuthToken() == null) {
            Toast.makeText(requireContext(), "Token no disponible. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        GestionarFragment gestionarFragment = new GestionarFragment();
        Bundle args = new Bundle();
        args.putString("activity_id", activityId);
        args.putString("activity_title", activityTitle);
        gestionarFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, gestionarFragment)
                .addToBackStack(null)
                .commit();
    }
}