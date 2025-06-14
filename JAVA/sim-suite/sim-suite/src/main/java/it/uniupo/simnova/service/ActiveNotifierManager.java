package it.uniupo.simnova.service;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestisce le notifiche "fisse" per le operazioni in background.
 * Permette di mostrare una notifica e di chiuderla in un secondo momento tramite un ID univoco.
 */
@Service
public class ActiveNotifierManager {

    private static final Logger logger = LoggerFactory.getLogger(ActiveNotifierManager.class);
    private final Map<String, Notification> activeNotifications = new ConcurrentHashMap<>();

    /**
     * Mostra una notifica fissa (senza auto-chiusura) e ne restituisce l'ID univoco.
     *
     * @param message Il messaggio da visualizzare (es. "Generazione in corso...").
     * @return L'ID univoco della notifica creata.
     */
    public String show(String message) {
        String notificationId = UUID.randomUUID().toString();

        // Messaggio da visualizzare sopra la barra
        Span messageLabel = new Span(message);

        // Crea una ProgressBar e impostala su "indeterminata"
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setWidth("100%"); // Occupa tutta la larghezza della notifica

        // Usa un VerticalLayout per mettere il testo sopra la barra
        VerticalLayout layout = new VerticalLayout(messageLabel, progressBar);
        layout.setPadding(false);
        layout.setSpacing(true);

        // Stile e configurazione della notifica
        Notification notification = new Notification(layout);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        notification.setPosition(Notification.Position.BOTTOM_START);
        notification.setDuration(0); // 0 significa che non si chiude da sola

        activeNotifications.put(notificationId, notification);
        notification.open();
        logger.info("Mostrata notifica fissa con ProgressBar (ID: {})", notificationId);

        return notificationId;
    }

    /**
     * Chiude una notifica attiva dato il suo ID.
     *
     * @param notificationId L'ID della notifica da chiudere.
     */
    public void close(String notificationId) {
        if (notificationId == null) return;

        Notification notification = activeNotifications.remove(notificationId);
        if (notification != null) {
            notification.close();
            logger.info("Chiusa notifica fissa con ID: {}", notificationId);
        } else {
            logger.warn("Nessuna notifica fissa trovata con ID: {}", notificationId);
        }
    }
}