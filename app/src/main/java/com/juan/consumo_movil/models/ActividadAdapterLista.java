package com.juan.consumo_movil.models;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;

import java.io.File;
import java.util.List;

public class ActividadAdapterLista extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ACTIVIDAD = 1;
    private static final int VIEW_TYPE_ASISTIR = 2;

    private List<Actividad> actividadList;
    private OnActividadClickListener clickListener;
    private OnDetallesClickListener detallesListener;
    private OnAsistirClickListener asistirListener;

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }

    public interface OnDetallesClickListener {
        void onDetallesClick(Actividad actividad);
    }

    public interface OnAsistirClickListener {
        void onAsistirClick(Actividad actividad, int position);
    }

    public ActividadAdapterLista(List<Actividad> actividadList,
                                 OnActividadClickListener clickListener,
                                 OnDetallesClickListener detallesListener,
                                 OnAsistirClickListener asistirListener) {
        this.actividadList = actividadList;
        this.clickListener = clickListener;
        this.detallesListener = detallesListener;
        this.asistirListener = asistirListener;
    }

    @Override
    public int getItemViewType(int position) {
        Actividad actividad = actividadList.get(position);
        return actividad.isAsistido() ? VIEW_TYPE_ASISTIR : VIEW_TYPE_ACTIVIDAD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ACTIVIDAD) {
            View view = inflater.inflate(R.layout.item_actividad_lista, parent, false);
            return new ActividadViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_asistir, parent, false);
            return new AsistirViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Actividad actividad = actividadList.get(position);

        if (holder instanceof ActividadViewHolder) {
            ActividadViewHolder actividadHolder = (ActividadViewHolder) holder;
            actividadHolder.tvTituloActividad.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");
            cargarImagen(actividad, actividadHolder.ivActividadImagen);

            actividadHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));

            actividadHolder.btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v);
                detallesListener.onDetallesClick(actividad);
            });
            actividadHolder.btnAsistirActividad.setOnClickListener(v -> asistirListener.onAsistirClick(actividad, position));

        } else if (holder instanceof AsistirViewHolder) {
            AsistirViewHolder asistirHolder = (AsistirViewHolder) holder;
            asistirHolder.tvTituloActividad.setText(actividad.getTitulo() != null ? actividad.getTitulo() : "");
            cargarImagen(actividad, asistirHolder.ivActividadImagen);

            asistirHolder.btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v);
                detallesListener.onDetallesClick(actividad);
            });

            asistirHolder.btnCancelarAsistencia.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "¿Quieres dejar de asistir a " + actividad.getTitulo() + "?", Toast.LENGTH_SHORT).show();
                // Aquí puedes llamar a la API para desasistir
            });


            asistirHolder.itemView.setOnClickListener(v -> clickListener.onActividadClick(actividad));
        }
    }

    private void cargarImagen(Actividad actividad, ImageView imageView) {
        if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
            File imgFile = new File(actividad.getImagenRuta());
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile));
            } else {
                imageView.setImageResource(R.drawable.default_image);
            }
        } else {
            imageView.setImageResource(R.drawable.default_image);
        }
    }

    private void compartirActividad(Actividad actividad, Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, actividad.getTitulo());
        shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta actividad: " + actividad.getTitulo() + "\n" + actividad.getDescripcion());
        context.startActivity(Intent.createChooser(shareIntent, "Compartir actividad"));
    }

    private void mostrarDialogoDetalles(Actividad actividad, View itemView) {
        Dialog dialog = new Dialog(itemView.getContext());
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (itemView.getResources().getDisplayMetrics().widthPixels * 0.8f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        TextView tvDetalleTitulo = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDetalleDescripcion = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvDetalleFecha = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvDetalleLugar = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvDetalleResponsables = dialog.findViewById(R.id.tvResponsablesDetalle);
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvDetalleTitulo.setText(actividad.getTitulo());
        tvDetalleDescripcion.setText(actividad.getDescripcion());
        tvDetalleFecha.setText(actividad.getFecha());
        tvDetalleLugar.setText(actividad.getLugar());
        tvDetalleResponsables.setText(actividad.getResponsables());

        if (actividad.getImagenRuta() != null && !actividad.getImagenRuta().isEmpty()) {
            File imgFile = new File(actividad.getImagenRuta());
            if (imgFile.exists()) {
                ivImagenDetalle.setImageURI(Uri.fromFile(imgFile));
            } else {
                ivImagenDetalle.setImageResource(R.drawable.default_image);
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        btnVolver.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return actividadList != null ? actividadList.size() : 0;
    }

    public void updateItems(List<Actividad> nuevasActividades) {
        actividadList.clear();
        actividadList.addAll(nuevasActividades);
        notifyDataSetChanged();
    }

    // --- ViewHolder para tipo ACTIVIDAD ---
    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;

        Button btnVerDetalles;
        Button btnAsistirActividad;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividadLista);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagenLista);

            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAsistirActividad = itemView.findViewById(R.id.btnAsistirActividad);
        }
    }

    // --- ViewHolder para tipo ASISTIDO ---
    static class AsistirViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;
        Button btnVerDetalles;
        Button btnCancelarAsistencia;


        public AsistirViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividadAsistir);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagenAsistir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnCancelarAsistencia = itemView.findViewById(R.id.btnCancelarAsistencia);

        }
    }
}