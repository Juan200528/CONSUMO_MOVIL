package com.juan.consumo_movil.models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.juan.consumo_movil.R;
import java.util.List;

public class AsistenteAdapter extends RecyclerView.Adapter<AsistenteAdapter.AsistenteViewHolder> {

    private final List<Asistente> asistentes;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Asistente asistente);
        void onEditClick(Asistente asistente);
        void onDeleteClick(Asistente asistente);
    }

    public AsistenteAdapter(List<Asistente> asistentes) {
        this.asistentes = asistentes;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AsistenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asistente, parent, false);
        return new AsistenteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsistenteViewHolder holder, int position) {
        Asistente asistente = asistentes.get(position);
        holder.bind(asistente, listener);
    }

    @Override
    public int getItemCount() {
        return asistentes.size();
    }

    static class AsistenteViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombre, tvCorreo;
        ImageButton btnEditar, btnEliminar;

        public AsistenteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreAsistente);
            tvCorreo = itemView.findViewById(R.id.tvEmailAsistente);
            btnEditar = itemView.findViewById(R.id.btnEditarAsistente);
            btnEliminar = itemView.findViewById(R.id.btnEliminarAsistente);
        }

        public void bind(Asistente asistente, OnItemClickListener listener) {
            // Use getNombre() instead of getNombreCompleto()
            String nombre = asistente.getNombre();
            Log.d("AsistenteAdapter", "Nombre: " + nombre + ", Correo: " + asistente.getCorreo());
            tvNombre.setText(nombre != null && !nombre.isEmpty() ? nombre : "Sin nombre");
            tvCorreo.setText(asistente.getCorreo() != null ? asistente.getCorreo() : "Sin correo");

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(asistente);
            });

            btnEditar.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(asistente);
            });

            btnEliminar.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(asistente);
            });
        }
    }
}