package com.juan.consumo_movil.models;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    private OnEditarClickListener editarListener;
    private OnEliminarClickListener eliminarListener;
    private OnPromocionarClickListener promocionarListener;
    private OnGestionarAsistentesClickListener gestionarAsistentesClickListener;

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

    public interface OnPromocionarClickListener {
        void onPromocionarClick(Actividad actividad, boolean isChecked);
    }

    public interface OnGestionarAsistentesClickListener {
        void onGestionarAsistentesClick(Actividad actividad);
    }

    public ActividadAdapterLista(List<Actividad> actividadList,
                                 OnActividadClickListener clickListener,
                                 OnDetallesClickListener detallesListener,
                                 OnAsistirClickListener asistirListener,
                                 OnEditarClickListener editarListener,
                                 OnEliminarClickListener eliminarListener,
                                 OnPromocionarClickListener promocionarListener,
                                 OnGestionarAsistentesClickListener gestionarAsistentesClickListener,
                                 SessionManager sessionManager) {
        this.actividadList = actividadList;
        this.clickListener = clickListener;
        this.detallesListener = detallesListener;
        this.asistirListener = asistirListener;
        this.editarListener = editarListener;
        this.eliminarListener = eliminarListener;
        this.promocionarListener = promocionarListener;
        this.gestionarAsistentesClickListener = gestionarAsistentesClickListener;

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

    // ————————————————————————————— ViewHolder Classes ——————————————————————————————

    class MiActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivImagen;
        TextView btnVerDetalles;
        Switch switchPromocion;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        // Botones nuevos para gestión de asistentes
        TextView tvAgregarAsistentes; // Debe estar en item_actividad.xml
        ImageButton btnPlus;

        MiActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividad);
            ivImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            switchPromocion = itemView.findViewById(R.id.switchPromocion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            // Estos IDs deben existir en tu layout item_actividad.xml
            tvAgregarAsistentes = itemView.findViewById(R.id.tvAgregarAsistentes);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }

        void bind(Actividad actividad) {
            tvTitulo.setText(actividad.getTitulo());
            cargarImagen(ivImagen, actividad.getImagenRuta());
            switchPromocion.setChecked(actividad.isPromocionada());

            // Navegación al hacer clic en el item
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onActividadClick(actividad);
                }
            });

            // Botón Ver Detalles
            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(actividad);
                }
            });

            // Botón Editar
            btnEditar.setOnClickListener(v -> {
                if (editarListener != null) {
                    editarListener.onEditarClick(actividad);
                }
            });

            // Botón Eliminar
            btnEliminar.setOnClickListener(v -> {
                if (eliminarListener != null) {
                    eliminarListener.onEliminarClick(actividad);
                }
            });

            // Redirección a GestionarFragment
            View.OnClickListener navigateListener = v -> {
                if (gestionarAsistentesClickListener != null) {
                    gestionarAsistentesClickListener.onGestionarAsistentesClick(actividad);
                }
            };

            tvAgregarAsistentes.setOnClickListener(navigateListener);
            btnPlus.setOnClickListener(navigateListener);
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
            cargarImagen(ivImagen, actividad.getImagenRuta());

            btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v.getContext());
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            btnAsistir.setOnClickListener(v -> {
                if (asistirListener != null) {
                    asistirListener.onAsistirClick(actividad, getAdapterPosition());
                }
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
            cargarImagen(ivImagen, actividad.getImagenRuta());

            btnVerDetalles.setOnClickListener(v -> {
                mostrarDialogoDetalles(actividad, v.getContext());
                if (detallesListener != null) detallesListener.onDetallesClick(actividad);
            });

            btnCancelar.setOnClickListener(v -> {
                Toast.makeText(v.getContext(), "Dejar de asistir a: " + actividad.getTitulo(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ————————————————————————————— Métodos auxiliares ——————————————————————————————

    private void cargarImagen(ImageView imageView, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.default_image);
            return;
        }
        if (imagePath.startsWith("http")) {
            Glide.with(imageView.getContext())
                    .load(imagePath)
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(imageView);
        } else {
            File file = new File(imagePath);
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file));
            } else {
                imageView.setImageResource(R.drawable.default_image);
            }
        }
    }

    public static void mostrarDialogoDetalles(Actividad actividad, Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.8f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        TextView tvDetalleTitulo = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcion = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFecha = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugar = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsables = dialog.findViewById(R.id.tvResponsablesDetalle);
        ImageView ivImagenDetalle = dialog.findViewById(R.id.ivImagenDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvDetalleTitulo.setText(actividad.getTitulo());
        tvDescripcion.setText(actividad.getDescripcion());
        tvFecha.setText(actividad.getFecha());
        tvLugar.setText(actividad.getLugar());
        tvResponsables.setText(actividad.getResponsables());

        String imagePath = actividad.getImagenRuta();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("http")) {
                Glide.with(context)
                        .load(imagePath)
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image)
                        .into(ivImagenDetalle);
            } else {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    ivImagenDetalle.setImageURI(Uri.fromFile(imgFile));
                } else {
                    ivImagenDetalle.setImageResource(R.drawable.default_image);
                }
            }
        } else {
            ivImagenDetalle.setImageResource(R.drawable.default_image);
        }

        btnVolver.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void mostrarDialogoEditar(Actividad actividad, Context context, OnGuardarCambiosListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogo_editar_actividad);

        EditText etEditarTitulo = dialog.findViewById(R.id.etEditarTitulo);
        EditText etEditarDescripcion = dialog.findViewById(R.id.etEditarDescripcion);
        EditText etEditarFecha = dialog.findViewById(R.id.etEditarFecha);
        EditText etEditarLugar = dialog.findViewById(R.id.etEditarLugar);
        EditText etEditarResponsables = dialog.findViewById(R.id.etEditarResponsables);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarCambios);
        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);

        etEditarTitulo.setText(actividad.getTitulo());
        etEditarDescripcion.setText(actividad.getDescripcion());
        etEditarFecha.setText(actividad.getFecha());
        etEditarLugar.setText(actividad.getLugar());
        etEditarResponsables.setText(actividad.getResponsables());

        btnGuardar.setOnClickListener(v -> {
            actividad.setTitulo(etEditarTitulo.getText().toString());
            actividad.setDescripcion(etEditarDescripcion.getText().toString());
            actividad.setFecha(etEditarFecha.getText().toString());
            actividad.setLugar(etEditarLugar.getText().toString());
            actividad.setResponsables(etEditarResponsables.getText().toString());
            listener.onGuardar(actividad);
            dialog.dismiss();
        });

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void mostrarDialogoEliminar(Actividad actividad, Context context, OnEliminarConfirmadoListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogo_eliminar_actividad);

        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);
        Button btnCancelar = dialog.findViewById(R.id.btnCancelar);
        Button btnConfirmar = dialog.findViewById(R.id.btnConfirmar);

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            listener.onEliminar(actividad);
            dialog.dismiss();
        });

        dialog.show();
    }

    // ————————————————————————————— Setters para listeners ——————————————————————————————

    public void setOnEditarClickListener(OnEditarClickListener listener) {
        this.editarListener = listener;
    }

    public void setOnEliminarClickListener(OnEliminarClickListener listener) {
        this.eliminarListener = listener;
    }

    public void setOnPromocionarClickListener(OnPromocionarClickListener listener) {
        this.promocionarListener = listener;
    }

    public void setOnGestionarAsistentesClickListener(OnGestionarAsistentesClickListener listener) {
        this.gestionarAsistentesClickListener = listener;
    }

    // ————————————————————————————— Interfaces adicionales ——————————————————————————————

    public interface OnGuardarCambiosListener {
        void onGuardar(Actividad actividad);
    }

    public interface OnEliminarConfirmadoListener {
        void onEliminar(Actividad actividad);
    }
}