package it.uniupo.simnova.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
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
 * Layout principale dell'applicazione che gestisce le notifiche e l'interfaccia utente.
 * Estende AppLayout per fornire una struttura di layout comune.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class MainLayout extends AppLayout {
    /**
     * Servizio per la gestione delle notifiche.
     */
    private final NotifierService notifierService;
    /**
     * Gestore per le notifiche attive, utilizzato per chiudere notifiche specifiche.
     */
    private final ActiveNotifierManager activeNotifierManager;


    /**
     * Costruttore che inizializza il layout principale con i servizi necessari.
     *
     * @param notifierService       il servizio per la gestione delle notifiche
     * @param activeNotifierManager il gestore delle notifiche attive
     */
    public MainLayout(NotifierService notifierService, ActiveNotifierManager activeNotifierManager) {
        this.notifierService = notifierService;
        this.activeNotifierManager = activeNotifierManager;

    }

    /**
     * Metodo chiamato quando il layout viene allegato all'interfaccia utente.
     * Registra il servizio di notifica per ricevere aggiornamenti e gestire le notifiche.
     *
     * @param attachEvent l'evento di allegamento dell'interfaccia utente
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        final UI ui = attachEvent.getUI();


        notifierService.register(ui, payload -> {

            activeNotifierManager.close(payload.notificationToCloseId());
            LoggerFactory.getLogger(getClass()).info("Notifica di risultato ricevuta per UI {}: {}", ui.getUIId(), payload);

            if (payload.status() == NotifierService.Status.ERROR) {
                H5 errorTitle = new H5(payload.title());
                errorTitle.getStyle().set("color", "var(--lumo-base-color)").set("margin", "0");

                Span errorDetails = new Span(payload.details());
                errorDetails.getStyle().set("font-size", "var(--lumo-font-size-s)");

                VerticalLayout notificationLayout = new VerticalLayout(errorTitle, errorDetails);
                notificationLayout.setPadding(true);
                notificationLayout.setSpacing(true);
                notificationLayout.getStyle().set("padding-right", "var(--lumo-space-l)");

                Notification errorNotification = new Notification(notificationLayout);
                errorNotification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                errorNotification.setPosition(Notification.Position.TOP_CENTER);
                errorNotification.setDuration(10000);
                errorNotification.open();

            } else {

                Span messageLabel = new Span(payload.details());
                Button viewButton;
                Image gif;

                if (payload.details().contains("Scenario")) {
                    gif = new Image(UIConstants.AMBULANCE_GIF_PATH, "Animazione ambulanza");

                    String currentPath = ui.getInternals().getActiveViewLocation().getPath();
                    if ("scenari".equals(currentPath)) {
                        viewButton = new Button("Ricarica Pagina");
                        viewButton.addClickListener(event -> ui.getPage().reload());
                    } else {
                        viewButton = new Button("Vedi Scenari");
                        viewButton.addClickListener(event -> ui.navigate("scenari"));
                    }
                } else if (payload.details().contains("Esami di laboratorio")) {
                    viewButton = new Button("Ricarica Pagina");
                    gif = new Image(UIConstants.LAB_GIF_PATH, "Animazione esami");
                    viewButton.addClickListener(event -> ui.getPage().reload());
                } else if (payload.details().contains("Nuovo referto")) {
                    viewButton = new Button("Ricarica Pagina");
                    gif = new Image(UIConstants.REF_GIF_PATH, "Animazione referti");
                    viewButton.addClickListener(event -> ui.getPage().reload());
                } else if (payload.details().contains("Materiali necessari")) {
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
     * Metodo chiamato quando il layout viene staccato dall'interfaccia utente.
     *
     * @param detachEvent l'evento di distacco dell'interfaccia utente
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        notifierService.unregister(detachEvent.getUI());
        super.onDetach(detachEvent);
    }
}