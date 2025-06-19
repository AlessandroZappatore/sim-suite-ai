package it.uniupo.simnova.service;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Servizio per la gestione delle notifiche tra il backend e il frontend.
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@Service
public class NotifierService {
    /**
     * Mappa per tenere traccia dei listener registrati per ciascuna UI.
     * La chiave è l'istanza di UI e il valore è il listener che accetta un NotificationPayload.
     */
    private final Map<UI, Consumer<NotificationPayload>> listeners = new ConcurrentHashMap<>();

    /**
     * Registra un listener per le notifiche su una specifica UI.
     *
     * @param ui       l'istanza di UI per cui registrare il listener
     * @param listener il listener che accetta un NotificationPayload
     */
    public void register(UI ui, Consumer<NotificationPayload> listener) {
        listeners.put(ui, listener);
    }

    /**
     * Deregistra un listener per una specifica UI.
     *
     * @param ui l'istanza di UI per cui deregistrare il listener
     */
    public void unregister(UI ui) {
        listeners.remove(ui);
    }

    /**
     * Notifica un evento a tutti i listener registrati per la UI specificata.
     *
     * @param ui      l'istanza di UI per cui inviare la notifica
     * @param payload il payload della notifica contenente lo stato, il titolo, i dettagli e l'ID della notifica da chiudere
     */
    public void notify(UI ui, NotificationPayload payload) {
        Consumer<NotificationPayload> listener = listeners.get(ui);
        if (listener != null) {
            ui.access(() -> listener.accept(payload));
        }
    }

    /**
     * Enumerazione che rappresenta lo stato della notifica.
     */
    public enum Status {
        /**
         * Stato di successo della notifica.
         */
        SUCCESS,
        /**
         * Stato di errore della notifica.
         */
        ERROR
    }

    /**
     * Payload per le notifiche, contenente lo stato, il titolo, i dettagli e l'ID della notifica da chiudere.
     *
     * @param status lo stato della notifica (SUCCESS o ERROR)
     * @param title il titolo della notifica
     * @param details i dettagli della notifica
     * @param notificationToCloseId l'ID della notifica da chiudere, se necessario
     */
    public record NotificationPayload(
            Status status,
            String title,
            String details,
            String notificationToCloseId
    ) {
    }
}