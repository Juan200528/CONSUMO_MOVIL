package com.juan.consumo_movil.ui.gestionar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.models.Asistente;
import com.juan.consumo_movil.models.AsistenteAdapter;
import com.juan.consumo_movil.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class GestionarFragment extends Fragment implements AsistenteAdapter.OnItemClickListener {

    private EditText etNombreAsistente, etEmailAsistente;
    private Button btnAgregarAsistente;
    private TextView tvTituloActividad, tvEmptyGestion;
    private RecyclerView recyclerView;

    private AsistenteAdapter adapter;
    private List<Asistente> asistenteList;
    private GestionarViewModel viewModel;
    private SessionManager sessionManager;

    private String activityId;
    private String activityTitle;

    public GestionarFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestionar, container, false);

        sessionManager = new SessionManager(requireContext());
        initUI(view);
        setupRecyclerView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            activityId = getArguments().getString("activity_id");
            activityTitle = getArguments().getString("activity_title");

            tvTituloActividad.setText(activityTitle);
            tvEmptyGestion.setText("No hay asistentes registrados.");
            tvEmptyGestion.setVisibility(View.GONE);

            viewModel.cargarAsistentes(activityId);
        } else {
            tvEmptyGestion.setText("Selecciona una actividad desde la lista.");
            tvEmptyGestion.setVisibility(View.VISIBLE);
            tvTituloActividad.setText("");
        }

        viewModel.getAsistentes().observe(getViewLifecycleOwner(), asistentes -> {
            if (asistentes != null) {
                Log.d("GestionarFragment", "Asistentes recibidos: " + asistentes.size());
                asistenteList.clear();
                asistenteList.addAll(asistentes);
                adapter.notifyDataSetChanged();
                tvEmptyGestion.setVisibility(asistentes.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        btnAgregarAsistente.setOnClickListener(v -> agregarAsistente());
    }

    private void initUI(View view) {
        etNombreAsistente = view.findViewById(R.id.etNombreAsistente);
        etEmailAsistente = view.findViewById(R.id.etEmailAsistente);
        btnAgregarAsistente = view.findViewById(R.id.btnAgregarAsistente);
        tvTituloActividad = view.findViewById(R.id.tvTituloActividad);
        tvEmptyGestion = view.findViewById(R.id.tvEmptyGestion);
        recyclerView = view.findViewById(R.id.containerAsistentes);
    }

    private void setupRecyclerView() {
        asistenteList = new ArrayList<>();
        adapter = new AsistenteAdapter(asistenteList);
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(GestionarViewModel.class);
    }

    private void agregarAsistente() {
        String nombre = etNombreAsistente.getText().toString().trim();
        String email = etEmailAsistente.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombreAsistente.setError("Campo requerido");
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailAsistente.setError("Correo inválido");
            return;
        }

        Asistente asistente = new Asistente();
        asistente.setIdAsistente(sessionManager.getUserId());
        asistente.setIdActividad(activityId);
        asistente.setNombreCompleto(nombre);
        asistente.setCorreo(email);
        asistente.setActividadNombre(activityTitle);

        viewModel.insertarAsistente(asistente, activityId);
        etNombreAsistente.setText("");
        etEmailAsistente.setText("");
    }

    @Override
    public void onItemClick(Asistente asistente) {
        // Detalles (no implementado)
    }

    @Override
    public void onEditClick(Asistente asistente) {
        DialogFragment dialog = new EditAsistenteDialogFragment(asistente, updatedAsistente -> {
            viewModel.actualizarAsistente(asistente.getId(), updatedAsistente, activityId);
        });
        dialog.show(getChildFragmentManager(), "EditAsistenteDialog");
    }

    @Override
    public void onDeleteClick(Asistente asistente) {
        DialogFragment dialog = new DeleteAsistenteDialogFragment(asistente, () -> {
            viewModel.eliminarAsistentePorId(asistente.getId(), activityId);
        });
        dialog.show(getChildFragmentManager(), "DeleteAsistenteDialog");
    }

    public static class EditAsistenteDialogFragment extends DialogFragment {
        private Asistente asistente;
        private OnSaveListener listener;

        public interface OnSaveListener {
            void onSave(Asistente updatedAsistente);
        }

        public EditAsistenteDialogFragment(Asistente asistente, OnSaveListener listener) {
            this.asistente = asistente;
            this.listener = listener;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialogo_editar_asistente, container, false);

            EditText etNombre = view.findViewById(R.id.etEditarNombre);
            EditText etEmail = view.findViewById(R.id.etEditarCorreo);
            Button btnGuardarCambios = view.findViewById(R.id.btnGuardarCambios);
            ImageView ivCerrar = view.findViewById(R.id.ivCerrar);

            etNombre.setText(asistente.getNombreCompleto());
            etEmail.setText(asistente.getCorreo());

            btnGuardarCambios.setOnClickListener(v -> {
                String nombre = etNombre.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if (nombre.isEmpty()) {
                    etNombre.setError("Campo requerido");
                    return;
                }

                if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Correo inválido");
                    return;
                }

                asistente.setNombreCompleto(nombre);
                asistente.setCorreo(email);

                listener.onSave(asistente);
                dismiss();
            });

            ivCerrar.setOnClickListener(v -> dismiss());

            return view;
        }
    }

    public static class DeleteAsistenteDialogFragment extends DialogFragment {
        private Asistente asistente;
        private OnDeleteListener listener;

        public interface OnDeleteListener {
            void onDelete();
        }

        public DeleteAsistenteDialogFragment(Asistente asistente, OnDeleteListener listener) {
            this.asistente = asistente;
            this.listener = listener;
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialogo_eliminar_asistente, container, false);

            Button btnConfirmar = view.findViewById(R.id.btnConfirmar);
            Button btnCancelar = view.findViewById(R.id.btnCancelar);
            ImageView ivCerrar = view.findViewById(R.id.ivCerrar);

            btnConfirmar.setOnClickListener(v -> {
                listener.onDelete();
                dismiss();
            });

            btnCancelar.setOnClickListener(v -> dismiss());
            ivCerrar.setOnClickListener(v -> dismiss());

            return view;
        }
    }
}