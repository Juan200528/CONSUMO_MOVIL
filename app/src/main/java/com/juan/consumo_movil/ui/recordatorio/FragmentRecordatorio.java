package com.juan.consumo_movil.ui.recordatorio;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.models.Recordatorio;
import com.juan.consumo_movil.models.NotificationConfig;
import com.juan.consumo_movil.models.NotificationResponse;
import com.juan.consumo_movil.NotificationWorker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.concurrent.TimeUnit;

public class FragmentRecordatorio extends Fragment {

    private ArrayList<Recordatorio> listaRecordatorios;
    private AdaptadorRecordatorio adaptador;
    private RecyclerView recyclerView;

    // Lista de actividades obtenidas desde la API
    private List<ActividadModel> listaActividades = new ArrayList<>();

    // Token del usuario logueado
    private String token = "Bearer TU_TOKEN_AQUI"; // Reemplaza esto con el token real

    // Servicio de API
    private ApiService apiService;

    public FragmentRecordatorio() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordatorio, container, false);

        EditText etTitulo = view.findViewById(R.id.etTituloActividad);
        EditText etDias = view.findViewById(R.id.etDiasActividad);
        Button btnGuardar = view.findViewById(R.id.btnGuardarConfig);
        recyclerView = view.findViewById(R.id.recycler_view_notifications);

        apiService = RetrofitClient.getApiService();

        listaRecordatorios = new ArrayList<>();
        adaptador = new AdaptadorRecordatorio(
                listaRecordatorios,
                (recordatorio, position) -> showEditDialog(recordatorio, position),
                this::showDeleteDialog
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptador);

        createNotificationChannel();

        // Cargar todas las actividades desde la API
        cargarTodasLasActividades();

        // Cargar notificaciones desde el backend
        cargarNotificacionesDesdeBackend();

        btnGuardar.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString().trim();
            String diasStr = etDias.getText().toString().trim();

            if (titulo.isEmpty() || diasStr.isEmpty()) {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int dias = Integer.parseInt(diasStr);

                // Buscar actividad por título
                ActividadModel actividadEncontrada = null;
                for (ActividadModel act : listaActividades) {
                    if (act.getTitle().equalsIgnoreCase(titulo)) {
                        actividadEncontrada = act;
                        break;
                    }
                }

                if (actividadEncontrada == null) {
                    Toast.makeText(getContext(), "No se encontró una actividad con ese título", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fecha = actividadEncontrada.getDate();
                String lugar = actividadEncontrada.getPlace();
                String taskId = actividadEncontrada.getId(); // Obtenemos el ID de la actividad

                NotificationConfig config = new NotificationConfig(
                        titulo,
                        dias,
                        fecha,
                        lugar,
                        taskId // ✅ Ahora se pasan los 5 argumentos necesarios
                );

                Call<NotificationResponse> call = apiService.saveNotificationConfig(token, config);
                call.enqueue(new Callback<NotificationResponse>() {
                    @Override
                    public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Recordatorio nuevo = new Recordatorio(
                                    config.getTitle(),
                                    config.getDaysBefore(),
                                    config.getDate(),
                                    config.getPlace()
                            );
                            nuevo.setId(response.body().getId());
                            nuevo.setActivityId(config.getTask()); // Asignamos el activityId
                            listaRecordatorios.add(nuevo);
                            adaptador.notifyItemInserted(listaRecordatorios.size() - 1);
                            programarNotificacion(nuevo, dias);
                            etTitulo.setText("");
                            etDias.setText("");
                            Toast.makeText(getContext(), "Guardado en servidor", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Error al guardar en servidor", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<NotificationResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Número inválido", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void cargarTodasLasActividades() {
        ApiService apiService = RetrofitClient.getApiService();

        // Obtener mis actividades
        Call<List<ActividadModel>> callMisActividades = apiService.obtenerActividades(token);
        callMisActividades.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaActividades.addAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al cargar tus actividades", Toast.LENGTH_SHORT).show();
            }
        });

        // Obtener actividades de otros usuarios
        Call<List<ActividadModel>> callOtrasActividades = apiService.obtenerActividadesOtrosUsuarios(token);
        callOtrasActividades.enqueue(new Callback<List<ActividadModel>>() {
            @Override
            public void onResponse(Call<List<ActividadModel>> call, Response<List<ActividadModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaActividades.addAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ActividadModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al cargar actividades de otros usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarNotificacionesDesdeBackend() {
        Call<List<NotificationResponse>> call = apiService.getUserNotifications(token);
        call.enqueue(new Callback<List<NotificationResponse>>() {
            @Override
            public void onResponse(Call<List<NotificationResponse>> call, Response<List<NotificationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (NotificationResponse n : response.body()) {
                        Recordatorio r = new Recordatorio(
                                n.getTitle(),
                                n.getDaysBefore(),
                                n.getDate(),
                                n.getPlace()
                        );
                        r.setId(n.getId());
                        r.setActivityId(n.getTaskId()); // Asegúrate de tener este getter
                        listaRecordatorios.add(r);
                    }
                    adaptador.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<NotificationResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "No se pudieron cargar las notificaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarEnBackend(Recordatorio recordatorio, int position) {
        NotificationConfig config = new NotificationConfig(
                recordatorio.getTitulo(),
                recordatorio.getDiasAntes(),
                recordatorio.getFecha(),
                recordatorio.getLugar(),
                recordatorio.getActivityId() // ✅ Ahora se pasa el task ID
        );

        Call<NotificationResponse> call = apiService.updateNotification(token, recordatorio.getId(), config);
        call.enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adaptador.notifyItemChanged(position);
                } else {
                    Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarDelBackend(String id, int position) {
        Call<Void> call = apiService.deleteNotification(token, id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listaRecordatorios.remove(position);
                    adaptador.notifyItemRemoved(position);
                } else {
                    Toast.makeText(getContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "CanalRecordatorios";
            String description = "Notificaciones de recordatorios";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("recordatorio", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void programarNotificacion(Recordatorio recordatorio, int diasAntes) {
        long tiempoEnMillis = System.currentTimeMillis() + (diasAntes * 24 * 60 * 60 * 1000);

        Data data = new Data.Builder()
                .putString("titulo", recordatorio.getTitulo())
                .putString("mensaje", "Tu actividad está próxima.")
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(tiempoEnMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        WorkManager.getInstance(requireContext()).enqueue(workRequest);
    }

    private void showEditDialog(Recordatorio recordatorio, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialogo_editar_dias, null);
        builder.setView(dialogView);
        EditText etDiasEditar = dialogView.findViewById(R.id.etDiasEditar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        etDiasEditar.setText(String.valueOf(recordatorio.getDiasAntes()));

        AlertDialog dialog = builder.create();
        dialog.show();

        btnGuardar.setOnClickListener(v -> {
            String diasText = etDiasEditar.getText().toString();
            if (!diasText.isEmpty()) {
                try {
                    int nuevosDias = Integer.parseInt(diasText);
                    if (nuevosDias >= 0) {
                        recordatorio.setDiasAntes(nuevosDias);
                        adaptador.notifyItemChanged(position);
                        programarNotificacion(recordatorio, nuevosDias);
                        actualizarEnBackend(recordatorio, position);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Ingresa un número válido", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Número inválido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Campo vacío", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDeleteDialog(int position) {
        Recordatorio recordatorio = listaRecordatorios.get(position);
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Recordatorio")
                .setMessage("¿Estás seguro de que deseas eliminar este recordatorio?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarDelBackend(recordatorio.getId(), position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}