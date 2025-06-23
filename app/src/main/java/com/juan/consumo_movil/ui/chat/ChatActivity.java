package com.juan.consumo_movil.ui.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.juan.consumo_movil.R;
import com.juan.consumo_movil.model.ChatMessage;
import com.juan.consumo_movil.ui.chat.ChatAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int MIN_RECORD_TIME = 600;

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton, btnRecord;
    private TextView tituloComunidad;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();

    private DatabaseReference chatRef;
    private String currentUserId;
    private String comunidadId;

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        requestAudioPermission();
        initViews();
        setupRecyclerView();
        loadCommunityName();
        setupSendButton();
        setupRecordButton();
        listenForMessages();
        setupKeyboardBehavior();
    }

    // === Inicializaciones ===

    private void requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            permissionToRecordAccepted = true;
        }
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        btnRecord = findViewById(R.id.btnRecord);
        tituloComunidad = findViewById(R.id.nombreComunidadTextView);

        comunidadId = getIntent().getStringExtra("comunidadId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

    // === Carga del nombre de la comunidad ===

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

    // === Botón de enviar mensaje ===

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
            newRef.setValue(message);
        }
        messageEditText.setText("");
        scrollToBottom();
    }

    // === Botón de grabar audio ===

    private void setupRecordButton() {
        btnRecord.setOnTouchListener((v, event) -> {
            if (!permissionToRecordAccepted) {
                Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show();
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    pressStartTime = System.currentTimeMillis();
                    return true;
                case MotionEvent.ACTION_UP:
                    long duration = System.currentTimeMillis() - pressStartTime;
                    if (duration > MIN_RECORD_TIME) {
                        stopRecordingAndUpload();
                    } else {
                        cancelRecording();
                        Toast.makeText(this, "Grabación muy corta", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        });
    }

    private long pressStartTime;

    private void startRecording() {
        audioFilePath = getExternalCacheDir().getAbsolutePath() +
                "/audio_" + System.currentTimeMillis() + ".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("ChatActivity", "startRecording error", e);
            Toast.makeText(this, "Error al preparar la grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndUpload() {
        if (!isRecording) return;

        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        } catch (Exception e) {
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show();
            Log.e("ChatActivity", "stopRecording error", e);
            return;
        }

        Uri uri = Uri.fromFile(new File(audioFilePath));
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("audios/" + uri.getLastPathSegment());

        storageRef.putFile(uri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    ChatMessage message = new ChatMessage();
                    message.setSenderId(currentUserId);
                    message.setTimestamp(System.currentTimeMillis());
                    message.setAudio(true);
                    message.setAudioUrl(downloadUri.toString());
                    String key = chatRef.push().getKey();
                    message.setKey(key);
                    chatRef.child(key).setValue(message);
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Error al obtener URL de audio", Toast.LENGTH_SHORT).show())
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Error al subir audio", Toast.LENGTH_SHORT).show());
    }

    private void cancelRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception ignored) {}
            mediaRecorder = null;
        }
        if (new File(audioFilePath).exists()) {
            new File(audioFilePath).delete();
        }
        isRecording = false;
    }

    // === Escucha de mensajes en tiempo real ===

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

    // === Scroll automático ===

    private void scrollToBottom() {
        chatRecyclerView.post(() ->
                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1));
    }

    // === Comportamiento del teclado ===

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

    // === Manejo de permisos ===

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (!permissionToRecordAccepted) {
                Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}