package it.uniupo.simnova.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.uniupo.simnova.service.ActiveNotifierManager;
import it.uniupo.simnova.service.NotifierService;
import it.uniupo.simnova.views.constant.UIConstants;
import org.slf4j.LoggerFactory;

/**
 * Il layout principale dell'applicazione.
 * Estende AppLayout e funge da contenitore per tutte le altre viste.
 * Gestisce la registrazione e la visualizzazione delle notifiche provenienti da task in background.
 */
public class MainLayout extends AppLayout {

    private final NotifierService notifierService;
    private final ActiveNotifierManager activeNotifierManager; // <-- NUOVO: Inietta il manager per le notifiche attive

    /**
     * Costruttore aggiornato per ricevere entrambi i servizi tramite dependency injection.
     * @param notifierService Servizio per la comunicazione asincrona.
     * @param activeNotifierManager Servizio per la gestione delle notifiche "fisse".
     */
    public MainLayout(NotifierService notifierService, ActiveNotifierManager activeNotifierManager) {
        this.notifierService = notifierService;
        this.activeNotifierManager = activeNotifierManager; // <-- NUOVO: Inizializza il nuovo manager
    }

    /**
     * Chiamato quando il layout viene "attaccato" alla UI.
     * Registra un ascoltatore che gestisce le notifiche di completamento dei task in background.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        final UI ui = attachEvent.getUI();

        // Registra un ascoltatore che ora accetta il 'NotificationPayload'
        notifierService.register(ui, payload -> {

            // 1. PRIMA AZIONE: Chiudi la notifica "fissa" di "lavori in corso"
            activeNotifierManager.close(payload.notificationToCloseId());

            // 2. SECONDA AZIONE: Estrai il messaggio e mostra la notifica di risultato finale
            String message = payload.message();
            LoggerFactory.getLogger(getClass()).info("Notifica di risultato ricevuta per UI {}: {}", ui.getUIId(), message);

            boolean isError = message.toLowerCase().contains("errore");

            if (isError) {
                // Notifica di errore (logica originale mantenuta)
                Notification errorNotification = new Notification(message, 8000, Notification.Position.TOP_CENTER);
                errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                errorNotification.open();
            } else {
                // Notifica di successo personalizzata (logica originale mantenuta)
                Span messageLabel = new Span(message);
                Button viewButton;
                Image gif;

                if (message.contains("Scenario")) {
                    viewButton = new Button("Vedi Scenari");
                    gif = new Image(UIConstants.AMBULANCE_GIF_PATH, "Animazione ambulanza");
                    viewButton.addClickListener(event -> ui.navigate("scenari"));
                } else if (message.contains("Esami di laboratorio") || message.contains("Materiali necessari")) { // Aggiunto "Materiali" per coerenza
                    viewButton = new Button("Ricarica Pagina");
                    gif = new Image(UIConstants.LAB_GIF_PATH, "Animazione esami");
                    viewButton.addClickListener(event -> ui.getPage().reload());
                } else if (message.contains("Nuovo referto")) {
                    viewButton = new Button("Ricarica Pagina");
                    gif = new Image(UIConstants.REF_GIF_PATH, "Animazione referti");
                    viewButton.addClickListener(event -> ui.getPage().reload());
                } else {
                    // Fallback generico
                    viewButton = new Button("OK");
                    gif = new Image(); // Immagine vuota
                }

                gif.setWidth("400px");
                gif.getStyle().set("margin-top", "var(--lumo-space-s)");

                viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                viewButton.getStyle().set("margin-left", "var(--lumo-space-l)");

                HorizontalLayout topRowLayout = new HorizontalLayout(messageLabel, viewButton);
                topRowLayout.setAlignItems(FlexComponent.Alignment.CENTER);

                VerticalLayout notificationLayout = new VerticalLayout(topRowLayout, gif);
                notificationLayout.setPadding(false);
                notificationLayout.setAlignItems(FlexComponent.Alignment.CENTER);

                Notification notification = new Notification(notificationLayout);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.setDuration(8000);

                // Chiude la notifica quando il bottone viene cliccato
                viewButton.addClickListener(event -> notification.close());

                notification.open();
            }
        });
    }

    /**
     * Chiamato quando il layout viene "staccato" (es. l'utente chiude la scheda del browser).
     * Rimuove l'ascoltatore per evitare memory leak.
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Pulisce l'ascoltatore per l'istanza UI specifica
        notifierService.unregister(detachEvent.getUI());
        super.onDetach(detachEvent);
    }
}