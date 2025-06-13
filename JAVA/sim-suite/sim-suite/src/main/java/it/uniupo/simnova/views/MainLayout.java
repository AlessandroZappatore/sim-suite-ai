package it.uniupo.simnova.views; // O un package dedicato ai layout/viste

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
import it.uniupo.simnova.service.NotifierService;
import it.uniupo.simnova.views.constant.UIConstants;
import org.slf4j.LoggerFactory;

/**
 * Il layout principale dell'applicazione.
 * Estende AppLayout e funge da contenitore per tutte le altre viste.
 * È il posto ideale per la logica che deve vivere per tutta la sessione UI,
 * come l'ascoltatore di notifiche.
 */
public class MainLayout extends AppLayout {

    private final NotifierService notifierService;

    // Usa l'iniezione tramite costruttore, che è la pratica migliore in Spring
    public MainLayout(NotifierService notifierService) {
        this.notifierService = notifierService;
    }

    // Questo metodo viene chiamato quando il layout viene "attaccato" alla UI
    // Dentro la classe MainLayout.java

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        final UI ui = attachEvent.getUI();

        notifierService.register(ui, message -> {
            LoggerFactory.getLogger(getClass()).info("Notifica ricevuta per UI {}: {}", ui.getUIId(), message);

            boolean isError = message.toLowerCase().contains("errore");

            if (isError) {
                // Notifica di errore
                Notification errorNotification = new Notification(message, 8000, Notification.Position.TOP_CENTER);
                errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                errorNotification.open();
            } else {
                // Notifica di successo personalizzata
                Span messageLabel = new Span(message);
                Button viewButton;
                Image gif;

                // *** LOGICA PER SCEGLIERE GIF E BOTTONE ***
                if (message.contains("Scenario")) {
                    viewButton = new Button("Vedi Scenari");
                    gif = new Image(UIConstants.AMBULANCE_GIF_PATH, "Animazione ambulanza");
                    viewButton.addClickListener(event -> ui.navigate("scenari"));
                } else if (message.contains("Esami di laboratorio")) {
                    viewButton = new Button("Ricarica Pagina");
                    // Sostituisci con il percorso della tua GIF per gli esami
                    gif = new Image(UIConstants.LAB_GIF_PATH, "Animazione esami");
                    // Il bottone ricarica la pagina corrente per mostrare la nuova card
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

    // Questo metodo viene chiamato quando il layout viene "staccato" (l'utente chiude la scheda)
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Pulisce l'ascoltatore per evitare memory leak
        notifierService.unregister(detachEvent.getUI());
        super.onDetach(detachEvent);
    }
}