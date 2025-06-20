package com.juan.consumo_movil.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private DatabaseReference chatRef;
    private String currentUserId;
    private String comunidadId;
    private String comunidadNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        comunidadId = getIntent().getStringExtra("comunidadId");
        comunidadNombre = getIntent().getStringExtra("comunidadNombre");
        setTitle("Chat: " + comunidadNombre);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("comunidades").child(comunidadId).child("mensajes");

        chatAdapter = new ChatAdapter(this, messageList, currentUserId, comunidadId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> sendMessage());

        listenForMessages();
    }

    private void sendMessage() {
        String msg = messageEditText.getText().toString().trim();
        if (!msg.isEmpty()) {
            DatabaseReference newRef = chatRef.push();
            ChatMessage message = new ChatMessage(currentUserId, msg, System.currentTimeMillis());
            newRef.setValue(message);
        }
        messageEditText.setText("");
    }

    private void listenForMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ChatMessage message = child.getValue(ChatMessage.class);
                    if (message != null) message.setKey(child.getKey());
                    messageList.add(message);
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Chat", "Error al leer mensajes", error.toException());
            }
        });
    }
}