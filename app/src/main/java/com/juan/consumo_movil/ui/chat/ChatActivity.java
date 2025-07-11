package com.juan.consumo_movil.ui.chat;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ChatMessage;
import com.juan.consumo_movil.ui.chat.ChatAdapter;
import com.juan.consumo_movil.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private TextView tituloComunidad;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();

    private DatabaseReference chatRef;
    private String currentUserId;
    private String comunidadId;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupRecyclerView();
        loadCommunityName();
        setupSendButton();
        listenForMessages();
        setupKeyboardBehavior();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        tituloComunidad = findViewById(R.id.nombreComunidadTextView);

        comunidadId = getIntent().getStringExtra("comunidadId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUsername = SessionManager.getInstance().getUsername();

        chatRef = FirebaseDatabase.getInstance()
                .getReference("comunidades")
                .child(comunidadId)
                .child("mensajes");
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(this, messageList, currentUserId, comunidadId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void loadCommunityName() {
        FirebaseDatabase.getInstance().getReference("comunidades")
                .child(comunidadId)
                .child("nombre")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nombre = snapshot.getValue(String.class);
                        tituloComunidad.setText(nombre != null ? nombre : "Comunidad");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tituloComunidad.setText("Comunidad");
                    }
                });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            sendMessage();
            messageEditText.requestFocus();
        });
    }

    private void sendMessage() {
        String msg = messageEditText.getText().toString().trim();
        if (!msg.isEmpty()) {
            DatabaseReference newRef = chatRef.push();
            ChatMessage message = new ChatMessage(currentUserId, msg, System.currentTimeMillis());

            // Obtener el nombre del usuario desde SessionManager 👈
            String username = SessionManager.getInstance().getUsername();
            message.setAutor(username); // Asegúrate de que ChatMessage tenga este campo

            newRef.setValue(message);
        }
        messageEditText.setText("");
        scrollToBottom();
    }

    private void listenForMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ChatMessage message = child.getValue(ChatMessage.class);
                    if (message != null) {
                        message.setKey(child.getKey());
                        messageList.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                scrollToBottom();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Chat", "Error al leer mensajes", error.toException());
            }
        });
    }

    private void scrollToBottom() {
        chatRecyclerView.post(() -> {
            if (chatAdapter.getItemCount() > 0) {
                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }

    private void setupKeyboardBehavior() {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            rootView.getWindowVisibleDisplayFrame(rect);
            int screenHeight = ((ViewGroup) rootView).getHeight();
            int keypadHeight = screenHeight - rect.bottom;
            if (keypadHeight > screenHeight * 0.15) {
                scrollToBottom();
            }
        });
        messageEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollToBottom();
            }
        });
    }
}