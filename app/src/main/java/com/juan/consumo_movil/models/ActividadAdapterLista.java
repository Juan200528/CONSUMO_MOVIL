package com.juan.consumo_movil.models;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private static final int VIEW_TYPE_PASADA = 4;

    private List<Actividad> actividadList;
    private String miUsuarioId;

    private final OnActividadClickListener clickListener;
    private final OnDetallesClickListener detallesListener;
    private final OnAsistirClickListener asistirListener;
    private final OnEditarClickListener editarListener;
    private final OnEliminarClickListener eliminarListener;
    private final OnPromocionarClickListener promocionarListener;
    private final OnGestionarAsistentesClickListener gestionarAsistentesClickListener;

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
        this.miUsuarioId = sessionManager != null ? sessionManager.getUserId() : "";
    }

    @Override
    public int getItemViewType(int position) {
        Actividad act = actividadList.get(position);
        if (act.isPasada()) {
            return VIEW_TYPE_PASADA;
        } else if (act.getIdCreador() != null && act.getIdCreador().equals(miUsuarioId)) {
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
        switch (viewType) {
            case VIEW_TYPE_MI_ACTIVIDAD:
                return new MiActividadViewHolder(inflater.inflate(R.layout.item_actividad, parent, false),
                        clickListener, detallesListener, editarListener, eliminarListener,
                        promocionarListener, gestionarAsistentesClickListener);
            case VIEW_TYPE_ASISTIR:
                return new AsistirViewHolder(inflater.inflate(R.layout.item_asistir, parent, false),
                        detallesListener, asistirListener);
            case VIEW_TYPE_PASADA:
                return new PasadaViewHolder(inflater.inflate(R.layout.item_actividad_pasadas, parent, false),
                        detallesListener);
            default:
                return new OtraActividadViewHolder(inflater.inflate(R.layout.item_actividad_lista, parent, false),
                        detallesListener, asistirListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Actividad actividad = actividadList.get(position);
        if (holder instanceof MiActividadViewHolder) {
            ((MiActividadViewHolder) holder).bind(actividad, clickListener, editarListener, eliminarListener, promocionarListener, gestionarAsistentesClickListener);
        } else if (holder instanceof OtraActividadViewHolder) {
            ((OtraActividadViewHolder) holder).bind(actividad, detallesListener, asistirListener);
        } else if (holder instanceof AsistirViewHolder) {
            ((AsistirViewHolder) holder).bind(actividad, detallesListener, asistirListener);
        } else if (holder instanceof PasadaViewHolder) {
            ((PasadaViewHolder) holder).bind(actividad, detallesListener);
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

    // ViewHolder para actividades propias
    static class MiActividadViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final ImageView ivImagen;
        private final TextView btnVerDetalles;
        private final Switch switchPromocion;
        private final ImageButton btnEditar;
        private final ImageButton btnEliminar;
        private final TextView tvAgregarAsistentes;
        private final ImageButton btnPlus;

        MiActividadViewHolder(@NonNull View itemView,
                              OnActividadClickListener clickListener,
                              OnDetallesClickListener detallesListener,
                              OnEditarClickListener editarListener,
                              OnEliminarClickListener eliminarListener,
                              OnPromocionarClickListener promocionarListener,
                              OnGestionarAsistentesClickListener gestionarAsistentesClickListener) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividad);
            ivImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            switchPromocion = itemView.findViewById(R.id.switchPromocion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            tvAgregarAsistentes = itemView.findViewById(R.id.tvAgregarAsistentes);
            btnPlus = itemView.findViewById(R.id.btnPlus);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onActividadClick(null);
                }
            });

            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(null);
                }
            });

            btnEditar.setOnClickListener(v -> {
                if (editarListener != null) {
                    editarListener.onEditarClick(null);
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (eliminarListener != null) {
                    eliminarListener.onEliminarClick(null);
                }
            });

            tvAgregarAsistentes.setOnClickListener(v -> {
                if (gestionarAsistentesClickListener != null) {
                    gestionarAsistentesClickListener.onGestionarAsistentesClick(null);
                }
            });

            btnPlus.setOnClickListener(v -> {
                if (gestionarAsistentesClickListener != null) {
                    gestionarAsistentesClickListener.onGestionarAsistentesClick(null);
                }
            });

            switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (promocionarListener != null) {
                    promocionarListener.onPromocionarClick(null, isChecked);
                }
            });
        }

        void bind(Actividad actividad,
                  OnActividadClickListener clickListener,
                  OnEditarClickListener editarListener,
                  OnEliminarClickListener eliminarListener,
                  OnPromocionarClickListener promocionarListener,
                  OnGestionarAsistentesClickListener gestionarAsistentesClickListener) {
            tvTitulo.setText(actividad.getTitulo());
            cargarImagen(ivImagen, actividad.getImagenRuta());
            switchPromocion.setChecked(actividad.isPromocionada());

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onActividadClick(actividad);
                }
            });



            btnEditar.setOnClickListener(v -> {
                if (editarListener != null) {
                    editarListener.onEditarClick(actividad);
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (eliminarListener != null) {
                    eliminarListener.onEliminarClick(actividad);
                }
            });

            View.OnClickListener listener = v -> {
                if (gestionarAsistentesClickListener != null) {
                    gestionarAsistentesClickListener.onGestionarAsistentesClick(actividad);
                }
            };

            tvAgregarAsistentes.setOnClickListener(listener);
            btnPlus.setOnClickListener(listener);

            switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (promocionarListener != null) {
                    promocionarListener.onPromocionarClick(actividad, isChecked);
                }
            });
        }
    }

    // ViewHolder para otras actividades
    static class OtraActividadViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final ImageView ivImagen;
        private final Button btnVerDetalles;
        private final Button btnAsistir;

        OtraActividadViewHolder(@NonNull View itemView,
                                OnDetallesClickListener detallesListener,
                                OnAsistirClickListener asistirListener) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividadLista);
            ivImagen = itemView.findViewById(R.id.ivActividadImagenLista);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAsistir = itemView.findViewById(R.id.btnAsistirActividad);

            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(null);
                }
            });

            btnAsistir.setOnClickListener(v -> {
                if (asistirListener != null) {
                    asistirListener.onAsistirClick(null, -1);
                }
            });
        }

        void bind(Actividad actividad,
                  OnDetallesClickListener detallesListener,
                  OnAsistirClickListener asistirListener) {
            tvTitulo.setText(actividad.getTitulo());
            cargarImagen(ivImagen, actividad.getImagenRuta());
            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(actividad);
                }
            });
            btnAsistir.setOnClickListener(v -> {
                if (asistirListener != null) {
                    asistirListener.onAsistirClick(actividad, getAdapterPosition());
                }
            });
        }
    }

    // ViewHolder para actividades a las que ya asisto
    static class AsistirViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final ImageView ivImagen;
        private final Button btnVerDetalles;
        private final Button btnCancelar;

        AsistirViewHolder(@NonNull View itemView,
                          OnDetallesClickListener detallesListener,
                          OnAsistirClickListener asistirListener) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloActividadAsistir);
            ivImagen = itemView.findViewById(R.id.ivActividadImagenAsistir);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnCancelar = itemView.findViewById(R.id.btnCancelarAsistencia);

            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(null);
                }
            });

            btnCancelar.setOnClickListener(v -> {
                if (asistirListener != null) {
                    asistirListener.onAsistirClick(null, -1);
                }
            });
        }

        void bind(Actividad actividad,
                  OnDetallesClickListener detallesListener,
                  OnAsistirClickListener asistirListener) {
            tvTitulo.setText(actividad.getTitulo());
            cargarImagen(ivImagen, actividad.getImagenRuta());
            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(actividad);
                }
            });
            btnCancelar.setOnClickListener(v -> {
                if (asistirListener != null) {
                    asistirListener.onAsistirClick(actividad, getAdapterPosition());
                }
            });
        }
    }

    // ViewHolder para actividades pasadas
    static class PasadaViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitulo;
        private final TextView tvLugar;
        private final TextView tvResponsable;
        private final TextView tvFecha;
        private final Button btnVerDetalles;

        PasadaViewHolder(@NonNull View itemView,
                         OnDetallesClickListener detallesListener) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvLugar = itemView.findViewById(R.id.tvLugar);
            tvResponsable = itemView.findViewById(R.id.tvResponsable);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);

            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(null);
                }
            });
        }

        void bind(Actividad actividad, OnDetallesClickListener detallesListener) {
            tvTitulo.setText(actividad.getTitulo());
            tvLugar.setText("Lugar: " + actividad.getLugar());
            tvResponsable.setText("Responsables: " + actividad.getResponsables());
            tvFecha.setText("Fecha: " + actividad.getFecha());
            btnVerDetalles.setOnClickListener(v -> {
                if (detallesListener != null) {
                    detallesListener.onDetallesClick(actividad);
                }
            });
        }
    }

    private static void cargarImagen(ImageView imageView, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.default_image);
            return;
        }
        try {
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
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.default_image);
        }
    }

    public static void mostrarDialogoDetalles(Actividad actividad, Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogo_detalle_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvTitulo = dialog.findViewById(R.id.tvTituloDetalle);
        TextView tvDescripcion = dialog.findViewById(R.id.tvDescripcionDetalle);
        TextView tvFecha = dialog.findViewById(R.id.tvFechaDetalle);
        TextView tvLugar = dialog.findViewById(R.id.tvLugarDetalle);
        TextView tvResponsables = dialog.findViewById(R.id.tvResponsablesDetalle);
        ImageView ivImagen = dialog.findViewById(R.id.ivImagenDetalle);
        Button btnVolver = dialog.findViewById(R.id.btnVolver);

        tvTitulo.setText(actividad.getTitulo());
        tvDescripcion.setText(actividad.getDescripcion());
        tvFecha.setText(actividad.getFecha());
        tvLugar.setText(actividad.getLugar());
        tvResponsables.setText(actividad.getResponsables());
        cargarImagenDialogo(ivImagen, actividad.getImagenRuta(), context);

        btnVolver.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static void cargarImagenDialogo(ImageView imageView, String imagePath, Context context) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.default_image);
            return;
        }
        try {
            if (imagePath.startsWith("http")) {
                Glide.with(context)
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
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.default_image);
        }
    }

    public static void mostrarDialogoEditar(Actividad actividad, Context context, OnGuardarCambiosListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogo_editar_actividad);

        EditText etTitulo = dialog.findViewById(R.id.etEditarTitulo);
        EditText etDescripcion = dialog.findViewById(R.id.etEditarDescripcion);
        EditText etFecha = dialog.findViewById(R.id.etEditarFecha);
        EditText etLugar = dialog.findViewById(R.id.etEditarLugar);
        EditText etResponsables = dialog.findViewById(R.id.etEditarResponsables);
        Button btnGuardar = dialog.findViewById(R.id.btnGuardarCambios);
        ImageView ivCerrar = dialog.findViewById(R.id.ivCerrar);

        etTitulo.setText(actividad.getTitulo());
        etDescripcion.setText(actividad.getDescripcion());
        etFecha.setText(actividad.getFecha());
        etLugar.setText(actividad.getLugar());
        etResponsables.setText(actividad.getResponsables());

        btnGuardar.setOnClickListener(v -> {
            if (etTitulo.getText().toString().trim().isEmpty() ||
                    etDescripcion.getText().toString().trim().isEmpty() ||
                    etFecha.getText().toString().trim().isEmpty() ||
                    etLugar.getText().toString().trim().isEmpty()) {
                Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            actividad.setTitulo(etTitulo.getText().toString());
            actividad.setDescripcion(etDescripcion.getText().toString());
            actividad.setFecha(etFecha.getText().toString());
            actividad.setLugar(etLugar.getText().toString());
            actividad.setResponsables(etResponsables.getText().toString());

            listener.onGuardar(actividad);
            dialog.dismiss();
        });

        ivCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void mostrarDialogoEliminar(Actividad actividad, Context context, OnEliminarConfirmadoListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialogo_eliminar_actividad);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.findViewById(R.id.ivCerrar).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnCancelar).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            listener.onEliminar(actividad);
            dialog.dismiss();
        });

        dialog.show();
    }

    public interface OnGuardarCambiosListener {
        void onGuardar(Actividad actividad);
    }

    public interface OnEliminarConfirmadoListener {
        void onEliminar(Actividad actividad);
    }
}