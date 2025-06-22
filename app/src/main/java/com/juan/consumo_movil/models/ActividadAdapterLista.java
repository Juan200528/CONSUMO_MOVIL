package com.juan.consumo_movil.models;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.utils.SessionManager;

import java.io.File;
import java.util.List;

public class ActividadAdapterLista extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MI_ACTIVIDAD = 3;
    private static final int VIEW_TYPE_OTRA_ACTIVIDAD = 1;
    private static final int VIEW_TYPE_ASISTIR = 2;

    private List<Actividad> actividadList;
    private String miUsuarioId;

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
                                 OnAsistirClickListener asistirListener,
                                 SessionManager sessionManager) {
        this.actividadList = actividadList;
        this.clickListener = clickListener;
        this.detallesListener = detallesListener;
        this.asistirListener = asistirListener;

        if (sessionManager != null && sessionManager.getUserId() != null) {
            this.miUsuarioId = sessionManager.getUserId();
        } else {
            this.miUsuarioId = "";
        }
    }

    @Override
    public int getItemViewType(int position) {
        Actividad act = actividadList.get(position);
        if (act.getIdCreador() != null && act.getIdCreador().equals(miUsuarioId)) {
            return VIEW_TYPE_MI_ACTIVIDAD;
        } else if (act.isAsistido()) {
            return VIEW_TYPE_ASISTIR;
        } else {
            return VIEW_TYPE_OTRA_ACTIVIDAD;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_MI_ACTIVIDAD) {
            View view = inflater.inflate(R.layout.item_actividad, parent, false);
            return new MiActividadViewHolder(view);
        } else if (viewType == VIEW_TYPE_ASISTIR) {
            View view = inflater.inflate(R.layout.item_asistir, parent, false);
            return new AsistirViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_actividad_lista, parent, false);
            return new OtraActividadViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Actividad actividad = actividadList.get(position);

        if (holder instanceof MiActividadViewHolder) {
            ((MiActividadViewHolder) holder).bind(actividad);
        } else if (holder instanceof OtraActividadViewHolder) {
            ((OtraActividadViewHolder) holder).bind(actividad);
        } else if (holder instanceof AsistirViewHolder) {
            ((AsistirViewHolder) holder).bind(actividad);
        }
    }

    @Override
    public int getItemCount() {
        return actividadList.size();
    }

    public void updateItems(List<Actividad> nuevasActividades) {
        actividadList.clear();
        actividadList.addAll(nuevasActividades);
        notifyDataSetChanged();
    }

    class MiActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivImagen;
        TextView btnVerDetalles;
        Switch switchPromocion;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        MiActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividad);
            ivImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            switchPromocion = itemView.findViewById(R.id.switchPromocion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        void bind(Actividad actividad) {
            tvTitulo.setText(actividad.getTitulo());
            switchPromocion.setChecked(actividad.isPromocionada());

            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            btnEditar.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Editando: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
            });

            btnEliminar.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Eliminar: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
            });

            switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(buttonView.getContext(), "Cambiar promoción: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    class OtraActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivImagen;
        Button btnVerDetalles;
        Button btnAsistir;

        OtraActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividadLista);
            ivImagen = itemView.findViewById(R.id.ivActividadImagenLista);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAsistir = itemView.findViewById(R.id.btnAsistirActividad);
        }

        void bind(Actividad actividad) {
            tvTitulo.setText(actividad.getTitulo());

            btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v.getContext());
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            btnAsistir.setOnClickListener(v -> {
                if (asistirListener != null) asistirListener.onAsistirClick(actividad, getAdapterPosition());
            });
        }
    }

    class AsistirViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivImagen;
        Button btnVerDetalles;
        Button btnCancelar;

        AsistirViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividadAsistir);
            ivImagen = itemView.findViewById(R.id.ivActividadImagenAsistir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnCancelar = itemView.findViewById(R.id.btnCancelarAsistencia);
        }

        void bind(Actividad actividad) {
            tvTitulo.setText(actividad.getTitulo());

            btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v.getContext());
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            btnCancelar.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "¿Dejar de asistir a " + actividad.getTitulo() + "?", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Mostrar diálogo de detalles
    private static void mostrarDialogoDetalles(Actividad actividad, Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        TextView tvDetalleTitulo = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDetalleDescripcion = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFecha = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugar = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsables = dialog.findViewById(R.id.tvResponsablesDetalle);
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvDetalleTitulo.setText(actividad.getTitulo());
        tvDetalleDescripcion.setText(actividad.getDescripcion());
        tvFecha.setText(actividad.getFecha());
        tvLugar.setText(actividad.getLugar());
        tvResponsables.setText(actividad.getResponsables());

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
}
