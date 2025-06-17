package com.juan.consumo_movil.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;

import java.util.List;
import java.util.stream.Collectors;

public class ActividadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class Item {
        public static final int TYPE_ACTIVIDAD = 1;
        public static final int TYPE_TITULO = 2;
        public static final int TYPE_PASADAS = 3;

        private final int type;
        private final Actividad actividad;
        private final String titulo;
        private final List<Actividad> actividadesPasadas;

        public Item(int type, Actividad actividad, String titulo, List<Actividad> actividadesPasadas) {
            this.type = type;
            this.actividad = actividad;
            this.titulo = titulo;
            this.actividadesPasadas = actividadesPasadas;
        }

        public int getType() {
            return type;
        }

        public Actividad getActividad() {
            return actividad;
        }

        public String getTitulo() {
            return titulo;
        }

        public List<Actividad> getActividadesPasadas() {
            return actividadesPasadas;
        }
    }

    private List<Item> itemList;
    private OnActividadClickListener onActividadClickListener;
    private OnEliminarClickListener onEliminarClickListener;
    private OnEditarClickListener onEditarClickListener;
    private OnDetallesClickListener onDetallesClickListener;

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }

    public interface OnEliminarClickListener {
        void onEliminarClick(Actividad actividad);
    }

    public interface OnEditarClickListener {
        void onEditarClick(Actividad actividad);
    }

    public interface OnDetallesClickListener {
        void onDetallesClick(Actividad actividad);
    }

    public ActividadAdapter(List<Item> itemList,
                            OnActividadClickListener onActividadClickListener,
                            OnEliminarClickListener onEliminarClickListener,
                            OnEditarClickListener onEditarClickListener,
                            OnDetallesClickListener onDetallesClickListener) {
        this.itemList = itemList;
        this.onActividadClickListener = onActividadClickListener;
        this.onEliminarClickListener = onEliminarClickListener;
        this.onEditarClickListener = onEditarClickListener;
        this.onDetallesClickListener = onDetallesClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Item.TYPE_ACTIVIDAD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
            return new ActividadViewHolder(view);
        } else if (viewType == Item.TYPE_TITULO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_titulo, parent, false);
            return new TituloViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_pasadas, parent, false);
            return new PasadasViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = itemList.get(position);

        if (holder instanceof ActividadViewHolder) {
            ((ActividadViewHolder) holder).bind(item.getActividad(), onActividadClickListener, onDetallesClickListener, onEditarClickListener, onEliminarClickListener);
        } else if (holder instanceof TituloViewHolder) {
            ((TituloViewHolder) holder).bind(item.getTitulo());
        } else if (holder instanceof PasadasViewHolder) {
            ((PasadasViewHolder) holder).bind(item.getActividadesPasadas(), onActividadClickListener, onDetallesClickListener, onEditarClickListener, onEliminarClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateItems(List<Item> newItemList) {
        itemList.clear();
        itemList.addAll(newItemList);
        notifyDataSetChanged();
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;
        TextView btnVerDetalles;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividad);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
        }

        public void bind(Actividad actividad,
                         OnActividadClickListener onActividadClickListener,
                         OnDetallesClickListener onDetallesClickListener,
                         OnEditarClickListener onEditarClickListener,
                         OnEliminarClickListener onEliminarClickListener) {
            tvTituloActividad.setText(actividad.getTitulo());
            // Aquí puedes cargar imagen si tienes URL o ruta

            itemView.setOnClickListener(v -> {
                if (onActividadClickListener != null) {
                    onActividadClickListener.onActividadClick(actividad);
                }
            });

            btnVerDetalles.setOnClickListener(v -> {
                if (onDetallesClickListener != null) {
                    onDetallesClickListener.onDetallesClick(actividad);
                }
            });
        }
    }

    static class TituloViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;

        public TituloViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }

        public void bind(String titulo) {
            tvTituloSeccion.setText(titulo);
        }
    }

    static class PasadasViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerPasadas;

        public PasadasViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerPasadas = itemView.findViewById(R.id.recyclerActividadesPasadas);
        }

        public void bind(List<Actividad> pasadas,
                         OnActividadClickListener onActividadClickListener,
                         OnDetallesClickListener onDetallesClickListener,
                         OnEditarClickListener onEditarClickListener,
                         OnEliminarClickListener onEliminarClickListener) {
            if (recyclerPasadas.getAdapter() == null) {
                ActividadAdapter adapter = new ActividadAdapter(
                        pasadas.stream()
                                .map(a -> new Item(Item.TYPE_ACTIVIDAD, a, null, null))
                                .collect(Collectors.toList()),
                        onActividadClickListener, onEliminarClickListener, onEditarClickListener, onDetallesClickListener);
                LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerPasadas.getContext(), LinearLayoutManager.HORIZONTAL, false);
                recyclerPasadas.setLayoutManager(layoutManager);
                recyclerPasadas.setAdapter(adapter);
            } else {
                ((ActividadAdapter) recyclerPasadas.getAdapter()).updateItems(
                        pasadas.stream()
                                .map(a -> new Item(Item.TYPE_ACTIVIDAD, a, null, null))
                                .collect(Collectors.toList())
                );
            }
        }
    }
}