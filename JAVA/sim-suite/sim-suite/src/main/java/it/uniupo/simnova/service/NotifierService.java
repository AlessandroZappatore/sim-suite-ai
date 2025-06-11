package it.uniupo.simnova.service;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class NotifierService {

    private final Map<UI, Consumer<String>> listeners = new ConcurrentHashMap<>();

    /**
     * Registra un "ascoltatore" per una specifica istanza UI.
     * L'ascoltatore è un'azione (in questo caso, mostrare una notifica) da eseguire.
     */
    public void register(UI ui, Consumer<String> listener) {
        listeners.put(ui, listener);
    }

    /**
     * Rimuove l'ascoltatore per un'istanza UI (per evitare memory leak).
     */
    public void unregister(UI ui) {
        listeners.remove(ui);
    }

    /**
     * Invia un messaggio a una specifica istanza UI.
     * Questo metodo viene chiamato dal task in background.
     */
    public void notify(UI ui, String message) {
        Consumer<String> listener = listeners.get(ui);
        if (listener != null) {
            // Usa ui.access() per eseguire l'aggiornamento grafico in modo sicuro
            ui.access(() -> listener.accept(message));
        }
    }
}