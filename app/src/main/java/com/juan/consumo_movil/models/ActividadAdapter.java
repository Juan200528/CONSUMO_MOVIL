package com.juan.consumo_movil.models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.juan.consumo_movil.R;
import com.juan.consumo_movil.api.ApiService;
import com.juan.consumo_movil.api.RetrofitClient;
import com.juan.consumo_movil.model.ActividadModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class Item {
        public static final int TYPE_ACTIVIDAD = 1;
        public static final int TYPE_TITULO = 2;
        public static final int TYPE_PASADAS = 3;

        private final int type;
        private final ActividadModel actividadModel;
        private final String titulo;
        private final List<ActividadModel> actividadesPasadas;

        public Item(int type, ActividadModel actividadModel, String titulo, List<ActividadModel> actividadesPasadas) {
            this.type = type;
            this.actividadModel = actividadModel;
            this.titulo = titulo;
            this.actividadesPasadas = actividadesPasadas;
        }

        public int getType() {
            return type;
        }

        public ActividadModel getActividadModel() {
            return actividadModel;
        }

        public String getTitulo() {
            return titulo;
        }

        public List<ActividadModel> getActividadesPasadas() {
            return actividadesPasadas;
        }
    }

    private List<Item> itemList;
    private OnActividadClickListener onActividadClickListener;
    private OnEliminarClickListener onEliminarClickListener;
    private OnEditarClickListener onEditarClickListener;
    private OnDetallesClickListener onDetallesClickListener;

    public interface OnActividadClickListener {
        void onActividadClick(ActividadModel actividadModel);
    }

    public interface OnEliminarClickListener {
        void onEliminarClick(ActividadModel actividadModel);
    }

    public interface OnEditarClickListener {
        void onEditarClick(ActividadModel actividadModel);
    }

    // ✅ Interfaz actualizada para recibir ambos parámetros
    public interface OnDetallesClickListener {
        void onDetallesClick(ActividadModel actividadModel, View view);
    }

    public ActividadAdapter(Context context, List<Item> itemList,
                            OnActividadClickListener onActividadClickListener,
                            OnEliminarClickListener onEliminarClickListener,
                            OnEditarClickListener onEditarClickListener,
                            OnDetallesClickListener onDetallesClickListener) {
        this.itemList = itemList;
        this.onActividadClickListener = onActividadClickListener;
        this.onEliminarClickListener = onEliminarClickListener;
        this.onEditarClickListener = onEditarClickListener;
        this.onDetallesClickListener = onDetallesClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Item.TYPE_ACTIVIDAD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad, parent, false);
            return new ActividadViewHolder(view);
        } else if (viewType == Item.TYPE_TITULO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_titulo, parent, false);
            return new TituloViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actividad_pasadas, parent, false);
            return new PasadasViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = itemList.get(position);
        if (holder instanceof ActividadViewHolder) {
            ((ActividadViewHolder) holder).bind(item.getActividadModel(), onActividadClickListener, onDetallesClickListener, onEditarClickListener, onEliminarClickListener);
        } else if (holder instanceof TituloViewHolder) {
            ((TituloViewHolder) holder).bind(item.getTitulo());
        } else if (holder instanceof PasadasViewHolder) {
            ((PasadasViewHolder) holder).bind(item.getActividadesPasadas(), onActividadClickListener, onDetallesClickListener, onEditarClickListener, onEliminarClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateItems(List<Item> newItemList) {
        itemList.clear();
        itemList.addAll(newItemList);
        notifyDataSetChanged();
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloActividad;
        ImageView ivActividadImagen;
        TextView btnVerDetalles;
        Switch switchPromocion;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloActividad = itemView.findViewById(R.id.tvTituloActividad);
            ivActividadImagen = itemView.findViewById(R.id.ivActividadImagen);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            switchPromocion = itemView.findViewById(R.id.switchPromocion);
        }

        public void bind(ActividadModel actividadModel,
                         OnActividadClickListener onActividadClickListener,
                         OnDetallesClickListener onDetallesClickListener,
                         OnEditarClickListener onEditarClickListener,
                         OnEliminarClickListener onEliminarClickListener) {
            tvTituloActividad.setText(actividadModel.getTitle());
            switchPromocion.setChecked(actividadModel.isPromoted());
            switchPromocion.setEnabled(!actividadModel.isPasada());

            // Clic en tarjeta completa
            itemView.setOnClickListener(v -> {
                if (onActividadClickListener != null) {
                    onActividadClickListener.onActividadClick(actividadModel);
                }
            });

            // Botón Ver Detalles
            btnVerDetalles.setOnClickListener(v -> {
                if (onDetallesClickListener != null) {
                    onDetallesClickListener.onDetallesClick(actividadModel, v);
                }
            });

            // Botón Editar
            itemView.findViewById(R.id.btnEditar).setOnClickListener(v -> {
                if (onEditarClickListener != null) {
                    onEditarClickListener.onEditarClick(actividadModel);
                }
            });

            // Botón Eliminar
            itemView.findViewById(R.id.btnEliminar).setOnClickListener(v -> {
                if (onEliminarClickListener != null) {
                    onEliminarClickListener.onEliminarClick(actividadModel);
                }
            });

            // Acción del Switch Promocionar
            switchPromocion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!actividadModel.isPasada()) {
                    String id = actividadModel.getId();
                    if (id == null || id.isEmpty() || id.equals("0")) {
                        Toast.makeText(buttonView.getContext(), "ID no válido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
                    String endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000));
                    PromotionRequest request = new PromotionRequest(id, isChecked, startDate, endDate);

                    ApiService apiService = RetrofitClient.getApiService();
                    apiService.promoteTask(id, request).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(buttonView.getContext(), "Promoción actualizada", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(buttonView.getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(buttonView.getContext(), "Fallo de red", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    static class TituloViewHolder extends RecyclerView.ViewHolder {
        TextView tvTituloSeccion;

        public TituloViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTituloSeccion = itemView.findViewById(R.id.tvTituloSeccion);
        }

        public void bind(String titulo) {
            tvTituloSeccion.setText(titulo);
        }
    }

    static class PasadasViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerPasadas;

        public PasadasViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerPasadas = itemView.findViewById(R.id.recyclerActividadesPasadas);
        }

        public void bind(List<ActividadModel> pasadas,
                         OnActividadClickListener onActividadClickListener,
                         OnDetallesClickListener onDetallesClickListener,
                         OnEditarClickListener onEditarClickListener,
                         OnEliminarClickListener onEliminarClickListener) {
            List<Item> items = pasadas.stream()
                    .map(a -> new Item(Item.TYPE_ACTIVIDAD, a, null, null))
                    .collect(Collectors.toList());

            ActividadAdapter adapter = new ActividadAdapter(
                    recyclerPasadas.getContext(),
                    items,
                    onActividadClickListener,
                    onEliminarClickListener,
                    onEditarClickListener,
                    onDetallesClickListener
            );

            recyclerPasadas.setLayoutManager(new LinearLayoutManager(recyclerPasadas.getContext(), LinearLayoutManager.HORIZONTAL, false));
            recyclerPasadas.setAdapter(adapter);
        }
    }
}