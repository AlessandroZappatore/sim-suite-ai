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
 * Gestore delle notifiche attive per mostrare messaggi fissi con una ProgressBar.
 * Le notifiche possono essere aperte e chiuse tramite ID univoco.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class ActiveNotifierManager {
    /**
     * Logger per il gestore delle notifiche attive.
     */
    private static final Logger logger = LoggerFactory.getLogger(ActiveNotifierManager.class);
    /**
     * Mappa per tenere traccia delle notifiche attive, indicizzate per ID.
     */
    private final Map<String, Notification> activeNotifications = new ConcurrentHashMap<>();

    /**
     * Costruttore privato per evitare istanziazioni dirette.
     */
    private ActiveNotifierManager() {
        // Costruttore privato per evitare istanziazioni dirette
    }
    /**
     * Mostra una notifica fissa con un messaggio e una ProgressBar.
     *
     * @param message messaggio da visualizzare nella notifica
     * @return ID univoco della notifica creata, che può essere utilizzato per chiuderla in seguito
     */
    public String show(String message) {
        String notificationId = UUID.randomUUID().toString();

        Span messageLabel = new Span(message);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setWidth("100%");

        VerticalLayout layout = new VerticalLayout(messageLabel, progressBar);
        layout.setPadding(false);
        layout.setSpacing(true);

        Notification notification = new Notification(layout);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        notification.setPosition(Notification.Position.BOTTOM_START);
        notification.setDuration(0);

        activeNotifications.put(notificationId, notification);
        notification.open();
        logger.info("Mostrata notifica fissa con ProgressBar (ID: {})", notificationId);

        return notificationId;
    }

    /**
     * Chiude una notifica fissa identificata dal suo ID.
     *
     * @param notificationId ID della notifica da chiudere
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