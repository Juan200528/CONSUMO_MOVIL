package com.juan.consumo_movil.ui.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatMessage> messageList;
    private final String currentUserId;
    private final String comunidadId;
    private final Context context;

    public ChatAdapter(Context context, List<ChatMessage> messages, String userId, String comunidadId) {
        this.context = context;
        this.messageList = messages;
        this.currentUserId = userId;
        this.comunidadId = comunidadId;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
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
        boolean showProfile = true;
        boolean showDate = true;

        if (position > 0) {
            ChatMessage prevMessage = messageList.get(position - 1);

            if (prevMessage.getSenderId().equals(message.getSenderId())) {
                showProfile = false;
            }

            Calendar currentCal = Calendar.getInstance();
            currentCal.setTimeInMillis(message.getTimestamp());
            Calendar prevCal = Calendar.getInstance();
            prevCal.setTimeInMillis(prevMessage.getTimestamp());

            showDate = !(currentCal.get(Calendar.YEAR) == prevCal.get(Calendar.YEAR)
                    && currentCal.get(Calendar.DAY_OF_YEAR) == prevCal.get(Calendar.DAY_OF_YEAR));
        }

        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message, showProfile, showDate);
        } else {
            ((ReceivedMessageHolder) holder).bind(message, showProfile, showDate);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder para mensajes enviados
    class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateText, reactionText;
        ImageView profileImage;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessageSent);
            profileImage = itemView.findViewById(R.id.imageProfileSent);
            dateText = itemView.findViewById(R.id.textDateSent);
            reactionText = itemView.findViewById(R.id.textReactionSent);

            itemView.setOnLongClickListener(v -> {
                CharSequence[] opciones = {"Editar", "Eliminar", "Responder", "Agregar reacción"};
                new AlertDialog.Builder(context)
                        .setItems(opciones, (dialog, which) -> {
                            switch (which) {
                                case 0: showEditDialog(getAdapterPosition()); break;
                                case 1: showDeleteDialog(getAdapterPosition()); break;
                                case 2: showReplyDialog(getAdapterPosition()); break;
                                case 3: showReactionDialog(getAdapterPosition()); break;
                            }
                        }).show();
                return true;
            });
        }

        void bind(ChatMessage message, boolean showProfile, boolean showDate) {
            messageText.setText(message.getMessage());
            profileImage.setVisibility(showProfile ? View.VISIBLE : View.INVISIBLE);

            if (message.getReaction() != null) {
                reactionText.setText(message.getReaction());
                reactionText.setVisibility(View.VISIBLE);
            } else {
                reactionText.setVisibility(View.GONE);
            }

            if (showDate) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(formatDate(message.getTimestamp()));
            } else {
                dateText.setVisibility(View.GONE);
            }
        }
    }

    // ViewHolder para mensajes recibidos
    class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderName, dateText, reactionText;
        ImageView profileImage;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessageReceived);
            senderName = itemView.findViewById(R.id.textSender);
            profileImage = itemView.findViewById(R.id.imageProfileReceived);
            dateText = itemView.findViewById(R.id.textDateReceived);
            reactionText = itemView.findViewById(R.id.textReactionReceived);
        }

        void bind(ChatMessage message, boolean showProfile, boolean showDate) {
            messageText.setText(message.getMessage());

            if (showProfile) {
                senderName.setVisibility(View.VISIBLE);
                profileImage.setVisibility(View.VISIBLE);
            } else {
                senderName.setVisibility(View.GONE);
                profileImage.setVisibility(View.INVISIBLE);
            }

            if (message.getReaction() != null) {
                reactionText.setText(message.getReaction());
                reactionText.setVisibility(View.VISIBLE);
            } else {
                reactionText.setVisibility(View.GONE);
            }

            if (showDate) {
                dateText.setVisibility(View.VISIBLE);
                dateText.setText(formatDate(message.getTimestamp()));
            } else {
                dateText.setVisibility(View.GONE);
            }

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

    private void showEditDialog(int position) {
        ChatMessage message = messageList.get(position);
        EditText input = new EditText(context);
        input.setText(message.getMessage());

        new AlertDialog.Builder(context)
                .setTitle("Editar mensaje")
                .setView(input)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String nuevoTexto = input.getText().toString().trim();
                    if (!nuevoTexto.isEmpty() && message.getKey() != null) {
                        FirebaseDatabase.getInstance().getReference("comunidades")
                                .child(comunidadId).child("mensajes")
                                .child(message.getKey()).child("message").setValue(nuevoTexto);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDeleteDialog(int position) {
        ChatMessage message = messageList.get(position);
        if (message.getKey() == null) return;

        new AlertDialog.Builder(context)
                .setTitle("Eliminar mensaje")
                .setMessage("¿Deseas eliminar este mensaje?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference("comunidades")
                            .child(comunidadId).child("mensajes")
                            .child(message.getKey()).removeValue();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showReactionDialog(int position) {
        String[] reactions = {"👍", "❤️", "😂", "😮", "😢", "🔥"};
        ChatMessage msg = messageList.get(position);
        new AlertDialog.Builder(context)
                .setTitle("Reacción")
                .setItems(reactions, (dialog, which) -> {
                    msg.setReaction(reactions[which]);
                    FirebaseDatabase.getInstance().getReference("comunidades")
                            .child(comunidadId).child("mensajes")
                            .child(msg.getKey()).child("reaction").setValue(reactions[which]);
                }).show();
    }

    private void showReplyDialog(int position) {
        ChatMessage original = messageList.get(position);
        EditText input = new EditText(context);
        input.setHint("Responder a: " + original.getMessage());

        new AlertDialog.Builder(context)
                .setTitle("Respuesta")
                .setView(input)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String reply = input.getText().toString().trim();
                    if (!reply.isEmpty()) {
                        enviarMensaje(reply + "\n↪ " + original.getMessage());
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Implementar según cómo envíes tus mensajes
    private void enviarMensaje(String mensaje) {
        // Este método debe ser implementado en tu actividad o fragmento
        // Puedes hacerlo mediante un callback o interfaz desde el adaptador
    }
}
