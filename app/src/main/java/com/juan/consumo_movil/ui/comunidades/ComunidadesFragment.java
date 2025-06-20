package com.juan.consumo_movil.ui.comunidades;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ChatMessage;
import com.juan.consumo_movil.model.ComunidadModel;
import com.juan.consumo_movil.ui.chat.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class ComunidadesFragment extends Fragment {
    private RecyclerView recyclerComunidades;
    private FloatingActionButton fabCrearComunidad;
    private ComunidadAdapter adapter;
    private List<ComunidadModel> comunidadList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comunidades, container, false);

        recyclerComunidades = view.findViewById(R.id.recyclerComunidades);
        fabCrearComunidad = view.findViewById(R.id.fabCrearComunidad);

        recyclerComunidades.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ComunidadAdapter(getContext(), comunidadList);
        recyclerComunidades.setAdapter(adapter);

        fabCrearComunidad.setOnClickListener(v -> mostrarDialogoCrearComunidad());

        cargarComunidades();
        Log.d("FAB", "fabCrearComunidad es null? " + (fabCrearComunidad == null));


        return view;
    }

    private void mostrarDialogoCrearComunidad() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nueva comunidad");

        final EditText input = new EditText(getContext());
        input.setHint("Nombre de la comunidad");
        builder.setView(input);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String nombre = input.getText().toString().trim();
            if (!nombre.isEmpty()) crearComunidad(nombre);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void crearComunidad(String nombreComunidad) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comunidades");
        String comunidadId = ref.push().getKey();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ComunidadModel comunidad = new ComunidadModel(comunidadId, nombreComunidad, currentUserId);
        comunidad.addMiembro(currentUserId);

        ref.child(comunidadId).setValue(comunidad);
    }

    private void cargarComunidades() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comunidades");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comunidadList.clear();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ComunidadModel comunidad = ds.getValue(ComunidadModel.class);
                    if (comunidad != null) {
                        comunidadList.add(comunidad);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Comunidades", "Error al cargar", error.toException());
            }
        });
    }
}
