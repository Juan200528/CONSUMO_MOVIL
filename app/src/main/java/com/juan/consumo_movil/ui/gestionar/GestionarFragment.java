package com.juan.consumo_movil.ui.gestionar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.models.Actividad;
import com.juan.consumo_movil.models.Asistente;
import com.juan.consumo_movil.models.AsistenteAdapter;
import java.util.ArrayList;
import java.util.List;

public class GestionarFragment extends Fragment {

    private GestionarViewModel viewModel;
    private EditText etNombreAsistente, etEmailAsistente;
    private Button btnAgregarAsistente, btnExportarLista;
    private LinearLayout containerAsistentes;
    private AsistenteAdapter asistenteAdapter;
    private List<Asistente> asistenteList;
    private int activityId;
    private TextView tvTituloActividad, tvEmptyGestion;
    private int userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gestionar, container, false);

        // Obtener userId
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show();
            return root;
        }

        // Obtener activity_id de los argumentos
        if (getArguments() != null) {
            activityId = getArguments().getInt("activity_id", -1);
        } else {
            activityId = -1;
        }

        // Inicializar vistas
        etNombreAsistente = root.findViewById(R.id.etNombreAsistente);
        etEmailAsistente = root.findViewById(R.id.etEmailAsistente);
        btnAgregarAsistente = root.findViewById(R.id.btnAgregarAsistente);
        containerAsistentes = root.findViewById(R.id.containerAsistentes);
        tvTituloActividad = root.findViewById(R.id.tvTituloActividad);
        tvEmptyGestion = root.findViewById(R.id.tvEmptyGestion);

        // Inicializar lista y adaptador
        asistenteList = new ArrayList<>();
        asistenteAdapter = new AsistenteAdapter(asistenteList,
                this::mostrarDialogoEditar,
                this::mostrarDialogoEliminar);

        // Configurar RecyclerView dentro del containerAsistentes
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(asistenteAdapter);
        containerAsistentes.addView(recyclerView);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(GestionarViewModel.class);

        // Cargar datos según activityId
        if (activityId != -1) {
            ManagerDb db = new ManagerDb(getContext());
            db.open();
            Actividad actividad = db.obtenerActividades().stream()
                    .filter(a -> a.getId() == activityId)
                    .findFirst().orElse(null);
            if (actividad != null) {
                tvTituloActividad.setText(actividad.getTitulo());
                tvEmptyGestion.setVisibility(View.GONE);
                // Cargar asistentes iniciales
                viewModel.cargarAsistentes(activityId);
            } else {
                tvEmptyGestion.setText("Selecciona una actividad desde la lista para gestionarla.");
                tvEmptyGestion.setVisibility(View.VISIBLE);
                tvTituloActividad.setText("");
                asistenteList.clear();
                asistenteAdapter.notifyDataSetChanged();
            }
            db.close();
        } else {
            tvEmptyGestion.setText("Selecciona una actividad desde la lista para gestionarla.");
            tvEmptyGestion.setVisibility(View.VISIBLE);
            tvTituloActividad.setText("");
            asistenteList.clear();
            asistenteAdapter.notifyDataSetChanged();
        }

        // Observar cambios en la lista de asistentes
        viewModel.getAsistentes().observe(getViewLifecycleOwner(), asistentes -> {
            asistenteList.clear();
            if (activityId != -1) {
                asistenteList.addAll(asistentes.stream()
                        .filter(a -> a.getIdActividad() == activityId)
                        .toList());
            }
            asistenteAdapter.notifyDataSetChanged();
            if (asistenteList.isEmpty() && activityId != -1) {
                tvEmptyGestion.setText("No hay asistentes registrados para esta actividad.");
                tvEmptyGestion.setVisibility(View.VISIBLE);
            } else {
                tvEmptyGestion.setVisibility(View.GONE);
            }
        });

        // Configurar botón de agregar
        btnAgregarAsistente.setOnClickListener(v -> agregarAsistente());

        // Configurar botón de exportar (sin implementación por ahora)
        btnExportarLista.setOnClickListener(v -> Toast.makeText(getContext(), "Exportar lista no implementado", Toast.LENGTH_SHORT).show());

        return root;
    }

    private void agregarAsistente() {
        String nombreCompleto = etNombreAsistente.getText().toString().trim();
        String correo = etEmailAsistente.getText().toString().trim();

        if (activityId == -1) {
            Toast.makeText(getContext(), "Debes seleccionar una actividad primero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nombreCompleto.isEmpty()) {
            etNombreAsistente.setError("El nombre es obligatorio");
            return;
        }
        if (correo.isEmpty()) {
            etEmailAsistente.setError("El correo es obligatorio");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmailAsistente.setError("Correo inválido");
            return;
        }

        String primerNombre = nombreCompleto.split(" ")[0];

        Asistente asistente = new Asistente();
        asistente.setIdAsistente(userId);
        asistente.setIdActividad(activityId);
        asistente.setNombreCompleto(nombreCompleto);
        asistente.setNombre(primerNombre);
        asistente.setCorreo(correo);
        asistente.setActividadNombre(tvTituloActividad.getText().toString());

        viewModel.insertarAsistente(asistente);
        etNombreAsistente.setText("");
        etEmailAsistente.setText("");
        Toast.makeText(getContext(), "Asistente agregado", Toast.LENGTH_SHORT).show();
    }

    private void mostrarDialogoEditar(Asistente asistente) {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_editar_asistente);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        EditText etEditarNombre = dialog.findViewById(R.id.etEditarNombre);
        EditText etEditarCorreo = dialog.findViewById(R.id.etEditarCorreo);
        Button btnGuardarCambios = dialog.findViewById(R.id.btnGuardarCambios);

        etEditarNombre.setText(asistente.getNombreCompleto() != null ? asistente.getNombreCompleto() : "");
        etEditarCorreo.setText(asistente.getCorreo() != null ? asistente.getCorreo() : "");

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnGuardarCambios.setOnClickListener(v -> {
            String nuevoNombreCompleto = etEditarNombre.getText().toString().trim();
            String nuevoCorreo = etEditarCorreo.getText().toString().trim();

            if (nuevoNombreCompleto.isEmpty()) {
                etEditarNombre.setError("El nombre es obligatorio");
                return;
            }
            if (nuevoCorreo.isEmpty()) {
                etEditarCorreo.setError("El correo es obligatorio");
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
                etEditarCorreo.setError("Correo inválido");
                return;
            }

            String primerNombre = nuevoNombreCompleto.contains(" ") ? nuevoNombreCompleto.split(" ")[0] : nuevoNombreCompleto;

            asistente.setNombreCompleto(nuevoNombreCompleto);
            asistente.setNombre(primerNombre);
            asistente.setCorreo(nuevoCorreo);
            asistente.setActividadNombre(tvTituloActividad.getText().toString());
            viewModel.actualizarAsistente(asistente);

            int position = asistenteList.indexOf(asistente);
            if (position != -1) {
                asistenteList.set(position, asistente);
                asistenteAdapter.notifyItemChanged(position);
            }

            Toast.makeText(getContext(), "Asistente actualizado", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoEliminar(Asistente asistente) {
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(R.layout.dialogo_eliminar_asistente);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            viewModel.eliminarAsistente(asistente);
            asistenteList.remove(asistente);
            asistenteAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Asistente eliminado", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}