package com.juan.consumo_movil.ui.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        return messageList.get(position).getSenderId().equals(currentUserId)
                ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        boolean showProfile = true;
        boolean showDate = true;

        if (position > 0) {
            ChatMessage prev = messageList.get(position - 1);
            if (prev.getSenderId().equals(message.getSenderId())) {
                showProfile = false;
            }

            Calendar currCal = Calendar.getInstance();
            currCal.setTimeInMillis(message.getTimestamp());
            Calendar prevCal = Calendar.getInstance();
            prevCal.setTimeInMillis(prev.getTimestamp());

            showDate = !(currCal.get(Calendar.YEAR) == prevCal.get(Calendar.YEAR) &&
                    currCal.get(Calendar.DAY_OF_YEAR) == prevCal.get(Calendar.DAY_OF_YEAR));
        }

        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message, showProfile, showDate);
        } else {
            ((ReceivedMessageHolder) holder).bind(message, showProfile, showDate);
        }

        // Pasamos referencia del adaptador para evitar NullPointerException
        holder.itemView.setTag(this);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // === Sent Message ViewHolder ===
    class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, dateText, reactionText;
        ImageView profileImage;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessageSent);
            dateText = itemView.findViewById(R.id.textDateSent);
            reactionText = itemView.findViewById(R.id.textReactionSent);
            profileImage = itemView.findViewById(R.id.imageProfileSent);

            itemView.setOnLongClickListener(v -> {
                ChatAdapter adapter = (ChatAdapter) v.getTag();
                if (adapter != null) {
                    adapter.showOptionsDialog(getAdapterPosition());
                }
                return true;
            });
        }

        void bind(ChatMessage message, boolean showProfile, boolean showDate) {
            messageText.setText(message.getMessage());
            reactionText.setVisibility(message.getReaction() != null ? View.VISIBLE : View.GONE);
            reactionText.setText(message.getReaction());

            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            if (showDate) {
                dateText.setText(formatDate(message.getTimestamp()));
            }

            profileImage.setVisibility(showProfile ? View.VISIBLE : View.INVISIBLE);
        }
    }

    // === Received Message ViewHolder ===
    class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderName, dateText, reactionText;
        ImageView profileImage;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessageReceived);
            senderName = itemView.findViewById(R.id.textSender);
            dateText = itemView.findViewById(R.id.textDateReceived);
            reactionText = itemView.findViewById(R.id.textReactionReceived);
            profileImage = itemView.findViewById(R.id.imageProfileReceived);

            itemView.setOnLongClickListener(v -> {
                ChatAdapter adapter = (ChatAdapter) v.getTag();
                if (adapter != null) {
                    adapter.showOptionsDialog(getAdapterPosition());
                }
                return true;
            });
        }

        void bind(ChatMessage message, boolean showProfile, boolean showDate) {
            messageText.setText(message.getMessage());
            reactionText.setVisibility(message.getReaction() != null ? View.VISIBLE : View.GONE);
            reactionText.setText(message.getReaction());

            senderName.setVisibility(showProfile ? View.VISIBLE : View.GONE);
            profileImage.setVisibility(showProfile ? View.VISIBLE : View.INVISIBLE);

            dateText.setVisibility(showDate ? View.VISIBLE : View.GONE);
            if (showDate) {
                dateText.setText(formatDate(message.getTimestamp()));
            }

            if (showProfile && message.getAutor() != null && !message.getAutor().isEmpty()) {
                senderName.setText(message.getAutor());
            } else {
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
    }

    // === Opciones del mensaje ===
    public void showOptionsDialog(int position) {
        ChatMessage msg = messageList.get(position);

        boolean isMyMessage = msg.getSenderId().equals(currentUserId);

        List<CharSequence> optionsList = new ArrayList<>();
        if (isMyMessage) {
            optionsList.add("Editar");
            optionsList.add("Eliminar");
        }
        optionsList.add("Agregar reacción");

        CharSequence[] opciones = optionsList.toArray(new CharSequence[0]);

        new AlertDialog.Builder(context)
                .setItems(opciones, (dialog, which) -> {
                    String selected = opciones[which].toString();

                    if (selected.equals("Editar")) {
                        showEditDialog(position);
                    } else if (selected.equals("Eliminar")) {
                        showDeleteDialog(position);
                    } else if (selected.equals("Agregar reacción")) {
                        showReactionDialog(position);
                    }
                }).show();
    }

    private void showEditDialog(int pos) {
        ChatMessage msg = messageList.get(pos);
        if (!msg.getSenderId().equals(currentUserId)) return;

        EditText input = new EditText(context);
        input.setText(msg.getMessage());
        new AlertDialog.Builder(context)
                .setTitle("Editar mensaje")
                .setView(input)
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    String nuevo = input.getText().toString().trim();
                    if (!nuevo.isEmpty() && msg.getKey() != null) {
                        FirebaseDatabase.getInstance().getReference("comunidades")
                                .child(comunidadId).child("mensajes")
                                .child(msg.getKey()).child("message").setValue(nuevo);
                    }
                }).setNegativeButton("Cancelar", null).show();
    }

    private void showDeleteDialog(int pos) {
        ChatMessage msg = messageList.get(pos);
        if (!msg.getSenderId().equals(currentUserId)) return;

        if (msg.getKey() == null) return;
        new AlertDialog.Builder(context)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar este mensaje?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    FirebaseDatabase.getInstance().getReference("comunidades")
                            .child(comunidadId).child("mensajes")
                            .child(msg.getKey()).removeValue();
                }).setNegativeButton("Cancelar", null).show();
    }

    private void showReactionDialog(int pos) {
        String[] reactions = {"👍", "❤️", "😂", "😮", "😢", "🔥"};
        ChatMessage msg = messageList.get(pos);
        new AlertDialog.Builder(context)
                .setTitle("Reaccionar")
                .setItems(reactions, (dialog, which) -> {
                    msg.setReaction(reactions[which]);
                    FirebaseDatabase.getInstance().getReference("comunidades")
                            .child(comunidadId).child("mensajes")
                            .child(msg.getKey()).child("reaction").setValue(reactions[which]);
                }).show();
    }

    // === Utils ===
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}