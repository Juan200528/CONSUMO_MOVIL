package com.juan.consumo_movil.ui.promocionadas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ActividadModel;

import java.util.List;

public class PromocionadasAdapter extends RecyclerView.Adapter<PromocionadasAdapter.PromocionadaViewHolder> {

    private final List<ActividadModel> actividadesPromocionadas;
    private final OnDetallesClickListener onDetallesClickListener;

    public interface OnDetallesClickListener {
        void onDetallesClick(ActividadModel actividadModel);
    }

    public PromocionadasAdapter(List<ActividadModel> actividadesPromocionadas, OnDetallesClickListener onDetallesClickListener) {
        this.actividadesPromocionadas = actividadesPromocionadas;
        this.onDetallesClickListener = onDetallesClickListener;
    }

    @NonNull
    @Override
    public PromocionadaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_promocionada, parent, false);
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

        public void bind(ActividadModel actividadModel, OnDetallesClickListener onDetallesClickListener) {
            tvTituloActividadPromocionada.setText(actividadModel.getTitle());
            // Configurar imagen si es necesario
            btnVerDetallesPromocionada.setOnClickListener(v -> {
                if (onDetallesClickListener != null) {
                    onDetallesClickListener.onDetallesClick(actividadModel);
                }
            });
        }
    }
}