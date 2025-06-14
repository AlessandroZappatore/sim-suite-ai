package it.uniupo.simnova.service;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class NotifierService {

    // Record per un payload strutturato
    public record NotificationPayload(String message, String notificationToCloseId) {}

    private final Map<UI, Consumer<NotificationPayload>> listeners = new ConcurrentHashMap<>();

    public void register(UI ui, Consumer<NotificationPayload> listener) {
        listeners.put(ui, listener);
    }

    public void unregister(UI ui) {
        listeners.remove(ui);
    }

    public void notify(UI ui, NotificationPayload payload) {
        Consumer<NotificationPayload> listener = listeners.get(ui);
        if (listener != null) {
            ui.access(() -> listener.accept(payload));
        }
    }
}