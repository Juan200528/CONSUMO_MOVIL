package com.juan.consumo_movil.ui.promocionadas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ActividadModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar actividades promocionadas en un RecyclerView.
 */
public class PromocionadasAdapter extends RecyclerView.Adapter<PromocionadasAdapter.PromocionadaViewHolder> {

    private List<ActividadModel> actividadesPromocionadas;
    private final OnDetallesClickListener onDetallesClickListener;

    /**
     * Interfaz para manejar clics en "Ver Detalles"
     */
    public interface OnDetallesClickListener {
        void onDetallesClick(ActividadModel actividadModel);
    }

    public PromocionadasAdapter(@NonNull List<ActividadModel> actividadesPromocionadas,
                                @NonNull OnDetallesClickListener onDetallesClickListener) {
        this.actividadesPromocionadas = actividadesPromocionadas != null
                ? new ArrayList<>(actividadesPromocionadas)
                : new ArrayList<>();
        this.onDetallesClickListener = onDetallesClickListener;
    }

    /**
     * Método para actualizar la lista de actividades desde fuera del adaptador
     */
    public void updateList(@NonNull List<ActividadModel> nuevaLista) {
        if (nuevaLista != null) {
            actividadesPromocionadas.clear();
            actividadesPromocionadas.addAll(nuevaLista);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public PromocionadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actividad_promocionada, parent, false);
        return new PromocionadaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromocionadaViewHolder holder, int position) {
        ActividadModel actividad = actividadesPromocionadas.get(position);
        holder.bind(actividad, onDetallesClickListener);
    }

    @Override
    public int getItemCount() {
        return actividadesPromocionadas.size();
    }

    static class PromocionadaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividadPromocionada;
        ImageView ivActividadImagenPromocionada;
        Button btnVerDetallesPromocionada;

        public PromocionadaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadPromocionada = itemView.findViewById(R.id.tvTituloActividadPromocionada);
            ivActividadImagenPromocionada = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetallesPromocionada = itemView.findViewById(R.id.btnVerDetallesPromocionada);
        }

        public void bind(ActividadModel actividadModel, OnDetallesClickListener listener) {
            if (actividadModel != null) {
                tvTituloActividadPromocionada.setText(actividadModel.getTitle());

                // Cargar la imagen con Glide
                Glide.with(itemView)
                        .load(actividadModel.getImage()) // Devuelve la URL de la imagen
                        .placeholder(R.drawable.default_image) // Imagen por defecto mientras se carga
                        .error(R.drawable.default_image) // Imagen si hay error
                        .into(ivActividadImagenPromocionada);

                btnVerDetallesPromocionada.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDetallesClick(actividadModel);
                    }
                });
            }
        }
    }
}