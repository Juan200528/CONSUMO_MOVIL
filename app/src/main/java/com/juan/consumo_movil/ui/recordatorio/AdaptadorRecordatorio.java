package com.juan.consumo_movil.ui.recordatorio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.models.Recordatorio;

import java.util.List;

public class AdaptadorRecordatorio extends RecyclerView.Adapter<AdaptadorRecordatorio.ViewHolder> {

    private List<Recordatorio> lista;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Recordatorio recordatorio, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public AdaptadorRecordatorio(List<Recordatorio> lista,
                                 OnEditClickListener onEditListener,
                                 OnDeleteClickListener onDeleteListener) {
        this.lista = lista;
        this.onEditClickListener = onEditListener;
        this.onDeleteClickListener = onDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recordatorio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recordatorio recordatorio = lista.get(position);
        holder.tvTitulo.setText(recordatorio.getTitulo());
        holder.tvFecha.setText("Fecha: " + recordatorio.getFecha());
        holder.tvLugar.setText("Lugar: " + recordatorio.getLugar());

        holder.btnEditar.setOnClickListener(v ->
                onEditClickListener.onEditClick(recordatorio, position));

        holder.btnEliminar.setOnClickListener(v ->
                onDeleteClickListener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvLugar;
        ImageButton btnEditar, btnEliminar;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividad);
            tvFecha = itemView.findViewById(R.id.tvFechaActividad);
            tvLugar = itemView.findViewById(R.id.tvLugarActividad);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}