package com.juan.consumo_movil.model;
public class ChatMessage {
    private String senderId;
    private String message;
    private long timestamp;
    private String key;

    public ChatMessage() {}
    public ChatMessage(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }
    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
