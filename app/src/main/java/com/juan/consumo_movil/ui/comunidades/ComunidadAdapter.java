package com.juan.consumo_movil.ui.comunidades;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ComunidadModel;
import com.juan.consumo_movil.ui.chat.ChatActivity;

import java.util.List;

public class ComunidadAdapter extends RecyclerView.Adapter<ComunidadAdapter.ViewHolder> {
    private Context context;
    private List<ComunidadModel> comunidades;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public ComunidadAdapter(Context context, List<ComunidadModel> comunidades) {
        this.context = context;
        this.comunidades = comunidades;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comunidad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComunidadModel comunidad = comunidades.get(position);
        holder.nombre.setText(comunidad.getNombre());

        boolean esMiembro = comunidad.getMiembros() != null && comunidad.getMiembros().containsKey(currentUserId);
        holder.boton.setText(esMiembro ? "Entrar" : "Unirse");

        holder.boton.setOnClickListener(v -> {
            if (!esMiembro) {
                FirebaseDatabase.getInstance().getReference("comunidades")
                        .child(comunidad.getId())
                        .child("miembros")
                        .child(currentUserId)
                        .setValue(true);
            } else {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("comunidadId", comunidad.getId());
                intent.putExtra("comunidadNombre", comunidad.getNombre());
                context.startActivity(intent);
            }
        });

        // 🔴 NUEVO: Long click para eliminar comunidad (solo el creador puede)
        holder.itemView.setOnLongClickListener(v -> {
            if (comunidad.getCreadorId().equals(currentUserId)) {
                new AlertDialog.Builder(context)
                        .setTitle("Eliminar comunidad")
                        .setMessage("¿Deseas eliminar esta comunidad? Se eliminará también su chat.")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            FirebaseDatabase.getInstance().getReference("comunidades")
                                    .child(comunidad.getId()).removeValue()
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(context, "Comunidad eliminada", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                Toast.makeText(context, "Solo el creador puede eliminar esta comunidad", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return comunidades.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        Button boton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.textNombreComunidad);
            boton = itemView.findViewById(R.id.btnUnirseOEntrar);
        }
    }
}
