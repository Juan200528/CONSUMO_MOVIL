package com.juan.consumo_movil.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ActividadModel;

import java.util.List;

public class ActividadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class Item {
        public static final int TYPE_ACTIVIDAD = 1;
        public static final int TYPE_TITULO = 2;
        public static final int TYPE_PASADAS = 3;

        private final int type;
        private final ActividadModel actividadModel;
        private final String titulo;
        private final List<ActividadModel> actividadesPasadas;

        public Item(int type, ActividadModel actividadModel, String titulo, List<ActividadModel> actividadesPasadas) {
            this.type = type;
            this.actividadModel = actividadModel;
            this.titulo = titulo;
            this.actividadesPasadas = actividadesPasadas;
        }

        public int getType() {
            return type;
        }

        public ActividadModel getActividadModel() {
            return actividadModel;
        }

        public String getTitulo() {
            return titulo;
        }

        public List<ActividadModel> getActividadesPasadas() {
            return actividadesPasadas;
        }
    }

    private final List<Item> items;
    private final Context context;
    private final OnActividadClickListener onActividadClickListener;
    private final OnDetallesClickListener onDetallesClickListener;
    private final OnEditarClickListener onEditarClickListener;
    private final OnEliminarClickListener onEliminarClickListener;

    public interface OnActividadClickListener {
        void onActividadClick(ActividadModel actividad);
    }

    public interface OnDetallesClickListener {
        void onDetallesClick(ActividadModel actividad);
    }

    public interface OnEditarClickListener {
        void onEditarClick(ActividadModel actividad);
    }

    public interface OnEliminarClickListener {
        void onEliminarClick(ActividadModel actividad);
    }

    public ActividadAdapter(Context context, List<Item> items,
                            OnActividadClickListener onActividadClickListener,
                            OnDetallesClickListener onDetallesClickListener,
                            OnEditarClickListener onEditarClickListener,
                            OnEliminarClickListener onEliminarClickListener) {
        this.context = context;
        this.items = items;
        this.onActividadClickListener = onActividadClickListener;
        this.onDetallesClickListener = onDetallesClickListener;
        this.onEditarClickListener = onEditarClickListener;
        this.onEliminarClickListener = onEliminarClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case Item.TYPE_TITULO:
                View tituloView = inflater.inflate(R.layout.item_actividad_titulo, parent, false);
                return new TituloViewHolder(tituloView);

            case Item.TYPE_ACTIVIDAD:
                View actividadView = inflater.inflate(R.layout.item_actividad, parent, false);
                return new ActividadViewHolder(actividadView, onActividadClickListener, onEditarClickListener, onEliminarClickListener, onDetallesClickListener);

            case Item.TYPE_PASADAS:
                View pasadasView = inflater.inflate(R.layout.item_actividad_pasadas, parent, false);
                return new PasadasViewHolder(pasadasView, onDetallesClickListener);

            default:
                throw new IllegalArgumentException("Tipo de vista desconocido: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);

        if (holder instanceof TituloViewHolder) {
            ((TituloViewHolder) holder).bind(item.getTitulo());
        } else if (holder instanceof ActividadViewHolder) {
            ((ActividadViewHolder) holder).bind(item.getActividadModel());
        } else if (holder instanceof PasadasViewHolder) {
            ((PasadasViewHolder) holder).bind(item.getActividadesPasadas());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    static class TituloViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTituloSeccion;

        TituloViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }

        void bind(String titulo) {
            tvTituloSeccion.setText(titulo);
        }
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final TextView tvLugar;
        private final TextView tvResponsables;
        private final TextView tvFecha;

        private ActividadModel model; // Guardamos localmente

        ActividadViewHolder(@NonNull View itemView,
                            OnActividadClickListener onActividadClickListener,
                            OnEditarClickListener onEditarClickListener,
                            OnEliminarClickListener onEliminarClickListener,
                            OnDetallesClickListener onDetallesClickListener) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvLugar = itemView.findViewById(R.id.tvLugar);
            tvResponsables = itemView.findViewById(R.id.tvResponsable);
            tvFecha = itemView.findViewById(R.id.tvFecha);

            itemView.setOnClickListener(v -> {
                if (model != null && onActividadClickListener != null) {
                    onActividadClickListener.onActividadClick(model);
                }
            });

            itemView.findViewById(R.id.btnVerDetalles).setOnClickListener(v -> {
                if (model != null && onDetallesClickListener != null) {
                    onDetallesClickListener.onDetallesClick(model);
                }
            });

            itemView.findViewById(R.id.btnEditar).setOnClickListener(v -> {
                if (model != null && onEditarClickListener != null) {
                    onEditarClickListener.onEditarClick(model);
                }
            });

            itemView.findViewById(R.id.btnEliminar).setOnClickListener(v -> {
                if (model != null && onEliminarClickListener != null) {
                    onEliminarClickListener.onEliminarClick(model);
                }
            });
        }

        void bind(ActividadModel model) {
            this.model = model;

            tvTitulo.setText(model.getTitle());
            tvLugar.setText(model.getPlace());
            tvResponsables.setText(String.join(", ", model.getResponsible()));
            tvFecha.setText(model.getDate());
        }
    }

    static class PasadasViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final TextView tvLugar;
        private final TextView tvResponsable;
        private final TextView tvFecha;
        private final Button btnVerDetalles;

        private List<ActividadModel> pasadasList;
        private final OnDetallesClickListener detallesListener;

        PasadasViewHolder(@NonNull View itemView, OnDetallesClickListener detallesListener) {
            super(itemView);
            this.detallesListener = detallesListener;

            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvLugar = itemView.findViewById(R.id.tvLugar);
            tvResponsable = itemView.findViewById(R.id.tvResponsable);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);

            btnVerDetalles.setOnClickListener(v -> {
                if (pasadasList != null && !pasadasList.isEmpty() && detallesListener != null) {
                    detallesListener.onDetallesClick(pasadasList.get(0));
                }
            });
        }

        void bind(List<ActividadModel> pasadas) {
            this.pasadasList = pasadas;

            if (pasadas == null || pasadas.isEmpty()) return;

            ActividadModel model = pasadas.get(0); // Mostrar la primera

            tvTitulo.setText(model.getTitle());
            tvLugar.setText(model.getPlace());
            tvResponsable.setText(String.join(", ", model.getResponsible()));
            tvFecha.setText(model.getDate());
        }
    }
}