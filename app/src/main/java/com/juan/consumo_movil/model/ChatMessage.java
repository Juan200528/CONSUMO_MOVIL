package com.juan.consumo_movil.model;

public class ChatMessage {
    private String senderId;
    private String message;
    private long timestamp;
    private String key;
    private String reaction;
    private boolean isAudio;
    private String audioUrl;

    public ChatMessage() {}

    public ChatMessage(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }

    public boolean isAudio() { return isAudio; }
    public void setAudio(boolean audio) { isAudio = audio; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
}
