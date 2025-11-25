package co.edu.javeriana.prestamos.notification;

import java.time.Instant;
import java.util.Map;

public class NotificationEvent {
    private String type;
    private Map<String, Object> payload;
    private Instant timestamp;

    public NotificationEvent() {
    }

    public NotificationEvent(String type, Map<String, Object> payload, Instant timestamp) {
        this.type = type;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public static NotificationEvent of(String type, Map<String, Object> payload) {
        return new NotificationEvent(type, payload, Instant.now());
    }
}
