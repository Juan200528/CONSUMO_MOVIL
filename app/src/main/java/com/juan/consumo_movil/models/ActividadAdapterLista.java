package com.juan.consumo_movil.models;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
import java.util.stream.Collectors;

public class ActividadAdapterLista extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Tipos de vistas
    private static final int VIEW_TYPE_MI_ACTIVIDAD = 3;   // item_actividad.xml
    private static final int VIEW_TYPE_OTRA_ACTIVIDAD = 1; // item_actividad_lista.xml
    private static final int VIEW_TYPE_ASISTIR = 2;        // item_asistir.xml

    private List<Actividad> actividadList;
    private String miUsuarioId;

    private OnActividadClickListener clickListener;
    private OnDetallesClickListener detallesListener;
    private OnAsistirClickListener asistirListener;
    private OnEditarClickListener editarListener;
    private OnEliminarClickListener eliminarListener;

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }

    public interface OnDetallesClickListener {
        void onDetallesClick(Actividad actividad);
    }

    public interface OnAsistirClickListener {
        void onAsistirClick(Actividad actividad, int position);
    }

    public interface OnEditarClickListener {
        void onEditarClick(Actividad actividad);
    }

    public interface OnEliminarClickListener {
        void onEliminarClick(Actividad actividad);
    }

    public ActividadAdapterLista(List<Actividad> actividadList,
                                 OnActividadClickListener clickListener,
                                 OnDetallesClickListener detallesListener,
                                 OnAsistirClickListener asistirListener) {
        this.actividadList = actividadList;
        this.clickListener = clickListener;
        this.detallesListener = detallesListener;
        this.asistirListener = asistirListener;
        this.editarListener = editarListener;
        this.eliminarListener = eliminarListener;

        if (SessionManager.getInstance() != null) {
            this.miUsuarioId = SessionManager.getInstance().getUserId();
        } else {
            this.miUsuarioId = "";
            Log.e("ADAPTER", "SessionManager no inicializado.");
        }
    }

    @Override
    public int getItemViewType(int position) {
        Actividad act = actividadList.get(position);

        boolean esMia = act.getIdCreador() != null &&
                !act.getIdCreador().isEmpty() &&
                act.getIdCreador().equals(miUsuarioId);

        return esMia ? VIEW_TYPE_MI_ACTIVIDAD : VIEW_TYPE_OTRA_ACTIVIDAD;
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
            ((MiActividadViewHolder) holder).bind(
                    actividad,
                    clickListener,
                    detallesListener,
                    asistirListener,
                    editarListener,
                    eliminarListener,
                    position
            );
        } else if (holder instanceof OtraActividadViewHolder) {
            ((OtraActividadViewHolder) holder).bind(
                    actividad,
                    clickListener,
                    detallesListener,
                    asistirListener,
                    position
            );
        } else if (holder instanceof AsistirViewHolder) {
            ((AsistirViewHolder) holder).bind(
                    actividad,
                    clickListener,
                    detallesListener,
                    asistirListener,
                    position
            );
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

    public void cargarSoloAjenas(List<Actividad> todasLasActividades) {
        List<Actividad> listaFiltrada = todasLasActividades.stream()
                .filter(act -> !act.getIdCreador().equals(miUsuarioId))
                .collect(Collectors.toList());
        updateItems(listaFiltrada);
    }

    // --- ViewHolder para TYPE_MI_ACTIVIDAD ---
    static class MiActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivImagen;
        TextView btnVerDetalles;
        Switch switchPromocion;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        public MiActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividad);
            ivImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            switchPromocion = itemView.findViewById(R.id.switchPromocion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        public void bind(Actividad actividad,
                         OnActividadClickListener clickListener,
                         OnDetallesClickListener detallesListener,
                         OnAsistirClickListener asistirListener,
                         OnEditarClickListener editarListener,
                         OnEliminarClickListener eliminarListener,
                         int position) {
            tvTitulo.setText(actividad.getTitulo());
            switchPromocion.setChecked(actividad.isPromocionada());

            // Clic general en tarjeta
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onActividadClick(actividad);
            });

            // Botón Ver Detalles
            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            // Botón Editar
            btnEditar.setOnClickListener(v -> {
                if (editarListener != null) editarListener.onEditarClick(actividad);
            });

            // Botón Eliminar
            btnEliminar.setOnClickListener(v -> {
                if (eliminarListener != null) eliminarListener.onEliminarClick(actividad);
            });

            // Switch Promocionar
            switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(buttonView.getContext(),
                        "Cambiar promoción: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
                // Aquí puedes llamar a la API para promocionar/despromocionar
            });
        }
    }

    // --- ViewHolder para TYPE_OTRA_ACTIVIDAD ---
    static class OtraActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivImagen;
        Button btnVerDetalles;
        Button btnAsistir;

        public OtraActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividadLista);
            ivImagen = itemView.findViewById(R.id.ivActividadImagenLista);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAsistir = itemView.findViewById(R.id.btnAsistirActividad);
        }

        public void bind(Actividad actividad,
                         OnActividadClickListener clickListener,
                         OnDetallesClickListener detallesListener,
                         OnAsistirClickListener asistirListener,
                         int position) {
            tvTitulo.setText(actividad.getTitulo());
            cargarImagen(actividad, ivImagen);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onActividadClick(actividad);
            });

            btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v.getContext());
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            btnAsistir.setOnClickListener(v -> {
                if (asistirListener != null) asistirListener.onAsistirClick(actividad, position);
            });
        }
    }

    // --- ViewHolder para tipo ASISTIDO ---
    static class AsistirViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivImagen;
        Button btnVerDetalles;
        Button btnCancelar;

        public AsistirViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividadAsistir);
            ivImagen = itemView.findViewById(R.id.ivActividadImagenAsistir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnCancelar = itemView.findViewById(R.id.btnCancelarAsistencia);
        }

        public void bind(Actividad actividad,
                         OnActividadClickListener clickListener,
                         OnDetallesClickListener detallesListener,
                         OnAsistirClickListener asistirListener,
                         int position) {
            tvTitulo.setText(actividad.getTitulo());
            cargarImagen(actividad, ivImagen);

            btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v.getContext());
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            btnCancelar.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "¿Dejar de asistir a " + actividad.getTitulo() + "?", Toast.LENGTH_SHORT).show();
            });

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onActividadClick(actividad);
            });
        }
    }

    // Cargar imagen desde archivo
    private static void cargarImagen(Actividad actividad, ImageView imageView) {
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