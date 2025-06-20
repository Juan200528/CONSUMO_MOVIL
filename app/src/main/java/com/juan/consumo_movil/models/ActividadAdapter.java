package com.juan.consumo_movil.models;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ActividadModel;
import com.juan.consumo_movil.ui.gestionar.GestionarFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    public static class Item {
        public static final int TYPE_ACTIVIDAD = 0;
        public static final int TYPE_TITULO = 1;
        public static final int TYPE_PASADAS = 2;

        public int type;
        public ActividadModel actividad;
        public String titulo;
        public List<ActividadModel> actividadesPasadas;

        public Item(int type, ActividadModel actividad, String titulo, List<ActividadModel> actividadesPasadas) {
            this.type = type;
            this.actividad = actividad;
            this.titulo = titulo;
            this.actividadesPasadas = actividadesPasadas;
        }
    }

    private Context context;
    private List<Item> actividadList;
    private OnActividadClickListener listener;
    private Consumer<ActividadModel> onDeleteClick;
    private Consumer<ActividadModel> onEditClick;
    private Consumer<ActividadModel> onDetailsClick;

    public interface OnActividadClickListener {
        void onActividadClick(ActividadModel actividad);
    }

    public ActividadAdapter(Context context, List<Item> actividadList, OnActividadClickListener listener,
                            Consumer<ActividadModel> onDeleteClick,
                            Consumer<ActividadModel> onEditClick,
                            Consumer<ActividadModel> onDetailsClick) {
        this.context = context;
        this.actividadList = actividadList != null ? actividadList : new ArrayList<>();
        this.listener = listener;
        this.onDeleteClick = onDeleteClick;
        this.onEditClick = onEditClick;
        this.onDetailsClick = onDetailsClick;
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
        return new ActividadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        Item item = actividadList.get(position);
        ActividadModel actividad = item.actividad;

        if (actividad == null) return;

        holder.tvTitulo.setText(actividad.getTitle());
        holder.btnEliminar.setOnClickListener(v -> onDeleteClick.accept(actividad));
        holder.btnEditar.setOnClickListener(v -> onEditClick.accept(actividad));
        holder.btnDetalles.setOnClickListener(v -> onDetailsClick.accept(actividad));

        // Navegación a GestionarFragment
        holder.layoutAsistentes.setOnClickListener(v -> abrirGestionarFragment(actividad));
        holder.tvAgregarAsistentes.setOnClickListener(v -> abrirGestionarFragment(actividad));
        holder.btnPlus.setOnClickListener(v -> abrirGestionarFragment(actividad));
    }

    private void abrirGestionarFragment(ActividadModel actividad) {
        GestionarFragment gestionarFragment = new GestionarFragment();
        Bundle args = new Bundle();
        args.putString("activity_id", actividad.getId());
        args.putString("activity_title", actividad.getTitle());
        gestionarFragment.setArguments(args);

        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, gestionarFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public int getItemCount() {
        return actividadList.size();
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageButton btnEliminar, btnEditar, btnDetalles;
        LinearLayout layoutAsistentes;
        TextView tvAgregarAsistentes;
        ImageButton btnPlus;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividad);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnDetalles = itemView.findViewById(R.id.btnVerDetalles);
            layoutAsistentes = itemView.findViewById(R.id.layoutAsistentes);
            tvAgregarAsistentes = itemView.findViewById(R.id.tvAgregarAsistentes);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
