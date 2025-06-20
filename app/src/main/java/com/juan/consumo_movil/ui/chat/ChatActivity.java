package com.juan.consumo_movil.ui.chat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton; // Cambiado de Button a ImageButton
    private ImageButton btnRecord;
    private TextView tituloComunidad;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private DatabaseReference chatRef;
    private String currentUserId;
    private String comunidadId;

    private MediaRecorder recorder;
    private String audioPath;
    private boolean isRecording = false;
    private long pressStartTime;
    private static final int MIN_RECORD_TIME = 600;

    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            permissionToRecordAccepted = true;
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton); // ImageButton
        btnRecord = findViewById(R.id.btnRecord);
        tituloComunidad = findViewById(R.id.nombreComunidadTextView);

        comunidadId = getIntent().getStringExtra("comunidadId");

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

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("comunidades")
                .child(comunidadId)
                .child("mensajes");

        chatAdapter = new ChatAdapter(this, messageList, currentUserId, comunidadId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> sendMessage());

        listenForMessages();

        btnRecord.setOnTouchListener((v, event) -> {
            if (!permissionToRecordAccepted) {
                Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show();
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressStartTime = System.currentTimeMillis();
                    startRecording();
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

    private void sendMessage() {
        String msg = messageEditText.getText().toString().trim();
        if (!msg.isEmpty()) {
            DatabaseReference newRef = chatRef.push();
            ChatMessage message = new ChatMessage(currentUserId, msg, System.currentTimeMillis());
            newRef.setValue(message);
        }
        messageEditText.setText("");
    }

    private void startRecording() {
        audioPath = getExternalCacheDir().getAbsolutePath() + "/audio_" + System.currentTimeMillis() + ".3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audioPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al grabar", Toast.LENGTH_SHORT).show();
            Log.e("ChatActivity", "startRecording error", e);
        }
    }

    private void cancelRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception ignored) {}
            recorder = null;
            new File(audioPath).delete();
        }
        isRecording = false;
    }

    private void stopRecordingAndUpload() {
        if (!isRecording) return;
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecording = false;
        } catch (Exception e) {
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show();
            Log.e("ChatActivity", "stopRecording error", e);
            return;
        }

        Uri uri = Uri.fromFile(new File(audioPath));
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("audios/" + uri.getLastPathSegment());
        storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                ChatMessage message = new ChatMessage();
                message.setSenderId(currentUserId);
                message.setTimestamp(System.currentTimeMillis());
                message.setAudio(true);
                message.setAudioUrl(downloadUri.toString());

                String key = chatRef.push().getKey();
                message.setKey(key);

                chatRef.child(key).setValue(message);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener URL de audio", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al subir audio", Toast.LENGTH_SHORT).show();
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!permissionToRecordAccepted) {
                Toast.makeText(this, "Permiso de grabación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
