package com.juan.consumo_movil.ui.promocionadas;

import android.annotation.SuppressLint;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PromocionadasAdapter extends RecyclerView.Adapter<PromocionadasAdapter.ViewHolder> {

    private List<ActividadModel> actividades;
    private OnItemClickListener listener;

    public PromocionadasAdapter(List<ActividadModel> actividades, OnItemClickListener listener) {
        this.actividades = actividades;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actividad_promocionada, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActividadModel actividad = actividades.get(position);

        // Mostrar título
        holder.tvTituloActividadPromocionada.setText(actividad.getTitle());

        // Cargar imagen
        String imageUrl = actividad.getImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_image)
                    .into(holder.ivActividadImagen);
        }

        // --- Lógica para mostrar u ocultar "FINALIZADA" ---
        boolean isFinalizada = false;
        try {
            String fechaCompleta = actividad.getDate();

            if (fechaCompleta == null || fechaCompleta.trim().isEmpty()) {
                isFinalizada = false; // Fecha vacía, no es finalizada
            } else {
                // Separar por 'T' o espacio, dependiendo del formato
                String[] partesFecha = fechaCompleta.split("[T\\s]+");
                String fechaStr = partesFecha[0];

                // Intentar parsear con LocalDate.parse() o con formateador
                LocalDate fechaActividad;
                try {
                    fechaActividad = LocalDate.parse(fechaStr); // Formato ISO: yyyy-MM-dd
                } catch (DateTimeParseException e1) {
                    // Si falla, intentar con un formateador personalizado (ej. dd/MM/yyyy)
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    fechaActividad = LocalDate.parse(fechaStr, formatter);
                }

                LocalDate hoy = LocalDate.now();
                isFinalizada = fechaActividad.isBefore(hoy);
            }
        } catch (Exception e) {
            isFinalizada = false; // Si hay cualquier error, no se marca como finalizada
        }

        if (isFinalizada) {
            holder.tvFinalizada.setVisibility(View.VISIBLE);
        } else {
            holder.tvFinalizada.setVisibility(View.GONE);
        }
        // --- Fin lógica FINALIZADA ---

        // Listener para ver detalles
        holder.btnVerDetallesPromocionada.setOnClickListener(v -> listener.onItemClick(actividad));
    }

    @Override
    public int getItemCount() {
        return actividades.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ActividadModel actividad);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividadPromocionada;
        ImageView ivActividadImagen;
        Button btnVerDetallesPromocionada;
        TextView tvFinalizada; // Referencia a la nueva etiqueta

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividadPromocionada = itemView.findViewById(R.id.tvTituloActividadPromocionada);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetallesPromocionada = itemView.findViewById(R.id.btnVerDetallesPromocionada);
            tvFinalizada = itemView.findViewById(R.id.tvFinalizada); // Inicializar
        }
    }

    // Método opcional para actualizar la lista desde el fragmento
    public void updateList(List<ActividadModel> nuevasActividades) {
        this.actividades.clear();
        this.actividades.addAll(nuevasActividades);
        notifyDataSetChanged();
    }
}