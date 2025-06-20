package com.juan.consumo_movil.ui.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ChatMessage;

import java.util.List;
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messageList;
    private String currentUserId;
    private String comunidadId;
    private Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages, String userId, String comunidadId) {
        this.context = context;
        this.messageList = messages;
        this.currentUserId = userId;
        this.comunidadId = comunidadId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessageSent);

            itemView.setOnLongClickListener(v -> {
                showDeleteDialog(getAdapterPosition());
                return true;
            });
        }
        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
        }
    }

    class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderName;
        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessageReceived);
            senderName = itemView.findViewById(R.id.textSender);
        }
        void bind(ChatMessage message) {
            messageText.setText(message.getMessage());
            FirebaseDatabase.getInstance().getReference("usuarios")
                    .child(message.getSenderId()).child("username")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            senderName.setText(name != null ? name : "Usuario");
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            senderName.setText("Usuario");
                        }
                    });
        }
    }

    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar mensaje")
                .setMessage("¿Deseas eliminar este mensaje?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ChatMessage message = messageList.get(position);
                    if (message.getKey() != null) {
                        FirebaseDatabase.getInstance().getReference("comunidades")
                                .child(comunidadId).child("mensajes")
                                .child(message.getKey()).removeValue();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
