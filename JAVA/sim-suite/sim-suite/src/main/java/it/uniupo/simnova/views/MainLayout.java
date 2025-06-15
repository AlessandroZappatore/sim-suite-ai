package it.uniupo.simnova.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    private final ActiveNotifierManager activeNotifierManager;

    /**
     * Costruttore aggiornato per ricevere entrambi i servizi tramite dependency injection.
     *
     * @param notifierService       Servizio per la comunicazione asincrona.
     * @param activeNotifierManager Servizio per la gestione delle notifiche "fisse".
     */
    public MainLayout(NotifierService notifierService, ActiveNotifierManager activeNotifierManager) {
        this.notifierService = notifierService;
        this.activeNotifierManager = activeNotifierManager;
    }

    /**
     * Chiamato quando il layout viene "attaccato" alla UI.
     * Registra un ascoltatore che gestisce le notifiche di completamento dei task in background.
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        final UI ui = attachEvent.getUI();

        notifierService.register(ui, payload -> {
            activeNotifierManager.close(payload.notificationToCloseId());

            String message = payload.message();
            LoggerFactory.getLogger(getClass()).info("Notifica di risultato ricevuta per UI {}: {}", ui.getUIId(), message);

            boolean isError = message.toLowerCase().contains("errore");

            if (isError) {
                Notification errorNotification = new Notification(message, 8000, Notification.Position.TOP_CENTER);
                errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                errorNotification.open();
            } else {
                Span messageLabel = new Span(message);
                Button viewButton;
                Image gif;

                if (message.contains("Scenario")) {
                    gif = new Image(UIConstants.AMBULANCE_GIF_PATH, "Animazione ambulanza");

                    String currentPath = ui.getInternals().getActiveViewLocation().getPath();
                    if ("scenari".equals(currentPath)) {
                        viewButton = new Button("Ricarica Pagina");
                        viewButton.addClickListener(event -> ui.getPage().reload());
                    } else {
                        viewButton = new Button("Vedi Scenari");
                        viewButton.addClickListener(event -> ui.navigate("scenari"));
                    }
                } else if (message.contains("Esami di laboratorio")) {
                    viewButton = new Button("Ricarica Pagina");
                    gif = new Image(UIConstants.LAB_GIF_PATH, "Animazione esami");
                    viewButton.addClickListener(event -> ui.getPage().reload());
                } else if (message.contains("Nuovo referto")) {
                    viewButton = new Button("Ricarica Pagina");
                    gif = new Image(UIConstants.REF_GIF_PATH, "Animazione referti");
                    viewButton.addClickListener(event -> ui.getPage().reload());
                } else if (message.contains("Materiali necessari")) {
                    viewButton = new Button("Ricarica Pagina");
                    gif = new Image(UIConstants.MAT_GIF_PATH, "Animazione materiali");
                    viewButton.addClickListener(event -> ui.getPage().reload());
                } else {
                    viewButton = new Button("OK");
                    gif = new Image();
                }

                gif.setWidth("400px");
                gif.getStyle().set("margin-top", "var(--lumo-space-s)");

                viewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                Notification notification = new Notification();

                Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), event -> notification.close());
                closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                closeButton.getElement().setAttribute("aria-label", "Chiudi notifica");

                HorizontalLayout topRowLayout = new HorizontalLayout(messageLabel, viewButton, closeButton);
                topRowLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                topRowLayout.expand(messageLabel);

                VerticalLayout notificationLayout = new VerticalLayout(topRowLayout, gif);
                notificationLayout.setPadding(false);
                notificationLayout.setAlignItems(FlexComponent.Alignment.CENTER);

                viewButton.addClickListener(event -> notification.close());

                notification.add(notificationLayout);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setPosition(Notification.Position.TOP_CENTER);
                notification.setDuration(8000);
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
        notifierService.unregister(detachEvent.getUI());
        super.onDetach(detachEvent);
    }
}