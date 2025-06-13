package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Classe di utility per la gestione e visualizzazione degli accessi (venosi e arteriosi)
 * di un paziente all'interno di uno scenario. Fornisce metodi per creare interfacce
 * di visualizzazione e aggiunta/eliminazione di accessi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class AccessSupport {
    /**
     * Logger per la registrazione degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(AccessSupport.class);

    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    public AccessSupport() {
        // Costruttore vuoto, non necessario per questa classe di utilità
    }
    /**
     * Genera una card per la visualizzazione e gestione degli accessi venosi e arteriosi.
     * Include griglie per visualizzare gli accessi esistenti e form per aggiungerne di nuovi.
     *
     * @param pazienteT0Service Il servizio per la gestione dei dati del paziente T0.
     * @param scenarioId        L'ID dello scenario a cui è associato il paziente.
     * @return Un {@link Div} contenente la card degli accessi.
     */
    public static Div getAccessoCard(PazienteT0Service pazienteT0Service, Integer scenarioId) {
        PazienteT0 paziente = pazienteT0Service.getPazienteT0ById(scenarioId);

        Div accessesCard = new Div();
        accessesCard.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding-top", "var(--lumo-space-s)")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                .set("width", "100%");

        // Sezione Accessi Venosi
        HorizontalLayout accessVenosiTitleLayout = new HorizontalLayout();
        accessVenosiTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        accessVenosiTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        accessVenosiTitleLayout.setWidthFull();
        accessVenosiTitleLayout.setSpacing(true);

        Icon accessVenosiIcon = new Icon(VaadinIcon.LINES);
        accessVenosiIcon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-xs)")
                .set("border-radius", "50%");

        H4 accessVenosiTitle = new H4("Accessi Venosi");
        accessVenosiTitle.getStyle()
                .set("margin", "0")
                .set("font-weight", "500");

        accessVenosiTitleLayout.add(accessVenosiIcon, accessVenosiTitle);
        accessesCard.add(accessVenosiTitleLayout);

        // Griglia per gli accessi venosi
        Grid<Accesso> accessiVenosiGrid = createAccessiGrid(paziente.getAccessiVenosi(), pazienteT0Service, scenarioId, true);
        accessiVenosiGrid.getStyle()
                .set("border", "none")
                .set("box-shadow", "none")
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("margin-left", "auto")
                .set("margin-right", "auto")
                .set("max-width", "600px");
        accessesCard.add(accessiVenosiGrid);

        // Contenitore per il form di aggiunta accesso venoso
        VerticalLayout addVenosoFormContainer = new VerticalLayout();
        addVenosoFormContainer.setWidthFull();
        addVenosoFormContainer.setSpacing(true);
        addVenosoFormContainer.setPadding(false);
        addVenosoFormContainer.setVisible(false);

        Button addVenosoButton = StyleApp.getButton("Aggiungi Accesso Venoso",
                VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addVenosoButton.getStyle()
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-m)");

        // Listener per il pulsante "Aggiungi Accesso Venoso"
        addVenosoButton.addClickListener(e -> {
            addVenosoFormContainer.removeAll(); // Pulisce il form precedente
            AccessoComponent nuovoAccessoVenosoComp = new AccessoComponent("Venoso", false);
            addVenosoFormContainer.add(nuovoAccessoVenosoComp);

            Button saveVenosoButton = StyleApp.getButton("Salva", VaadinIcon.CHECK, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
            Button cancelVenosoButton = StyleApp.getButton("Annulla", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");

            saveVenosoButton.addClickListener(saveEvent -> {
                Accesso nuovoAccesso = nuovoAccessoVenosoComp.getAccesso();
                // Validazione dei campi
                if (nuovoAccesso.getTipologia() == null || nuovoAccesso.getTipologia().isEmpty() ||
                        nuovoAccesso.getPosizione() == null || nuovoAccesso.getPosizione().isEmpty() ||
                        nuovoAccesso.getLato() == null || nuovoAccesso.getLato().isEmpty() ||
                        nuovoAccesso.getMisura() == null) {
                    Notification.show("Compilare tutti i campi dell'accesso.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                try {
                    pazienteT0Service.addAccesso(scenarioId, nuovoAccesso, true); // Aggiunge l'accesso venoso
                    PazienteT0 pazienteAggiornato = pazienteT0Service.getPazienteT0ById(scenarioId); // Ricarica i dati
                    accessiVenosiGrid.setItems(pazienteAggiornato.getAccessiVenosi()); // Aggiorna la griglia
                    Notification.show("Accesso venoso aggiunto con successo", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    // Nasconde il form e riabilita il pulsante "Aggiungi"
                    addVenosoFormContainer.removeAll();
                    addVenosoFormContainer.setVisible(false);
                    addVenosoButton.setVisible(true);
                } catch (Exception ex) {
                    logger.error("Errore durante l'aggiunta dell'accesso venoso", ex);
                    Notification.show("Errore durante l'aggiunta dell'accesso venoso: " + ex.getMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            cancelVenosoButton.addClickListener(cancelEvent -> {
                addVenosoFormContainer.removeAll();
                addVenosoFormContainer.setVisible(false);
                addVenosoButton.setVisible(true);
            });

            HorizontalLayout buttonsLayout = new HorizontalLayout(saveVenosoButton, cancelVenosoButton);
            buttonsLayout.setSpacing(true);
            addVenosoFormContainer.add(buttonsLayout);
            addVenosoFormContainer.setVisible(true);
            addVenosoButton.setVisible(false); // Nasconde il pulsante "Aggiungi" quando il form è visibile
        });

        HorizontalLayout addVenosoLayout = new HorizontalLayout(addVenosoButton);
        addVenosoLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addVenosoLayout.setWidthFull();
        accessesCard.add(addVenosoLayout);
        accessesCard.add(addVenosoFormContainer);

        // Sezione Accessi Arteriosi (struttura simile agli accessi venosi)
        HorizontalLayout accessArtTitleLayout = new HorizontalLayout();
        accessArtTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        accessArtTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        accessArtTitleLayout.setWidthFull();
        accessArtTitleLayout.setSpacing(true);

        Icon accessArtIcon = new Icon(VaadinIcon.LINES);
        accessArtIcon.getStyle()
                .set("color", "var(--lumo-success-color)")
                .set("background-color", "var(--lumo-success-color-10pct)")
                .set("padding", "var(--lumo-space-xs)")
                .set("border-radius", "50%");

        H4 accessArtTitle = new H4("Accessi Arteriosi");
        accessArtTitle.getStyle()
                .set("margin", "0")
                .set("font-weight", "500");

        accessArtTitleLayout.add(accessArtIcon, accessArtTitle);
        accessesCard.add(accessArtTitleLayout);

        // Griglia per gli accessi arteriosi
        Grid<Accesso> accessiArtGrid = createAccessiGrid(paziente.getAccessiArteriosi(), pazienteT0Service, scenarioId, false);
        accessiArtGrid.getStyle()
                .set("border", "none")
                .set("box-shadow", "none")
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-left", "auto")
                .set("margin-right", "auto")
                .set("max-width", "600px");
        accessesCard.add(accessiArtGrid);

        // Contenitore per il form di aggiunta accesso arterioso
        VerticalLayout addArteriosoFormContainer = new VerticalLayout();
        addArteriosoFormContainer.setWidthFull();
        addArteriosoFormContainer.setSpacing(true);
        addArteriosoFormContainer.setPadding(false);
        addArteriosoFormContainer.setVisible(false);

        Button addArtButton = StyleApp.getButton("Aggiungi Accesso Arterioso",
                VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addArtButton.getStyle()
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-m)");

        // Listener per il pulsante "Aggiungi Accesso Arterioso"
        addArtButton.addClickListener(e -> {
            addArteriosoFormContainer.removeAll();
            AccessoComponent nuovoAccessoArteriosoComp = new AccessoComponent("Arterioso", false);
            addArteriosoFormContainer.add(nuovoAccessoArteriosoComp);

            Button saveArteriosoButton = StyleApp.getButton("Salva", VaadinIcon.CHECK, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
            Button cancelArteriosoButton = StyleApp.getButton("Cancella", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");

            saveArteriosoButton.addClickListener(saveEvent -> {
                Accesso nuovoAccesso = nuovoAccessoArteriosoComp.getAccesso();
                // Validazione dei campi
                if (nuovoAccesso.getTipologia() == null || nuovoAccesso.getTipologia().isEmpty() ||
                        nuovoAccesso.getPosizione() == null || nuovoAccesso.getPosizione().isEmpty() ||
                        nuovoAccesso.getLato() == null || nuovoAccesso.getLato().isEmpty() ||
                        nuovoAccesso.getMisura() == null) {
                    Notification.show("Compilare tutti i campi dell'accesso.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                try {
                    pazienteT0Service.addAccesso(scenarioId, nuovoAccesso, false); // Aggiunge l'accesso arterioso
                    PazienteT0 pazienteAggiornato = pazienteT0Service.getPazienteT0ById(scenarioId);
                    accessiArtGrid.setItems(pazienteAggiornato.getAccessiArteriosi()); // Aggiorna la griglia
                    Notification.show("Accesso arterioso aggiunto con successo", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                    addArteriosoFormContainer.removeAll();
                    addArteriosoFormContainer.setVisible(false);
                    addArtButton.setVisible(true);
                } catch (Exception ex) {
                    logger.error("Errore durante l'aggiunta dell'accesso arterioso", ex);
                    Notification.show("Errore durante l'aggiunta dell'accesso arterioso: " + ex.getMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            cancelArteriosoButton.addClickListener(cancelEvent -> {
                addArteriosoFormContainer.removeAll();
                addArteriosoFormContainer.setVisible(false);
                addArtButton.setVisible(true);
            });

            HorizontalLayout buttonsLayout = new HorizontalLayout(saveArteriosoButton, cancelArteriosoButton);
            buttonsLayout.setSpacing(true);
            addArteriosoFormContainer.add(buttonsLayout);
            addArteriosoFormContainer.setVisible(true);
            addArtButton.setVisible(false);
        });

        HorizontalLayout addArtLayout = new HorizontalLayout(addArtButton);
        addArtLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addArtLayout.setWidthFull();
        accessesCard.add(addArtLayout);
        accessesCard.add(addArteriosoFormContainer);

        return accessesCard;
    }

    /**
     * Crea una griglia {@link Grid} per visualizzare una lista di accessi.
     * Include colonne per tipologia, posizione, lato, misura e un pulsante di eliminazione.
     *
     * @param accessi           La lista di oggetti {@link Accesso} da visualizzare.
     * @param pazienteT0Service Il servizio per la gestione dei dati del paziente T0.
     * @param scenarioId        L'ID dello scenario a cui gli accessi appartengono.
     * @param isVenoso          Indica se la griglia è per accessi venosi (true) o arteriosi (false).
     * @return Una {@link Grid} configurata per gli accessi.
     */
    private static Grid<Accesso> createAccessiGrid(List<Accesso> accessi, PazienteT0Service pazienteT0Service, Integer scenarioId, boolean isVenoso) {
        Grid<Accesso> grid = new Grid<>();
        grid.setItems(accessi);
        grid.addColumn(Accesso::getTipologia).setHeader("Tipologia").setAutoWidth(true);
        grid.addColumn(Accesso::getPosizione).setHeader("Posizione").setAutoWidth(true);
        grid.addColumn(Accesso::getLato).setHeader("Lato").setAutoWidth(true);
        grid.addColumn(Accesso::getMisura).setHeader("Misura").setAutoWidth(true);

        // Colonna per il pulsante di eliminazione
        grid.addComponentColumn(accesso -> {
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setTooltipText("Elimina accesso " + accesso.getTipologia());
            deleteButton.getElement().setAttribute("aria-label", "Elimina accesso");
            deleteButton.getStyle()
                    .set("color", "var(--lumo-error-color)")
                    .set("cursor", "pointer");

            deleteButton.addClickListener(e -> {
                ConfirmDialog confirmDialog = new ConfirmDialog();
                confirmDialog.setHeader("Conferma eliminazione");
                confirmDialog.setText("Sei sicuro di voler eliminare questo accesso " +
                        (isVenoso ? "venoso" : "arterioso") + "?");

                confirmDialog.setCancelable(true);
                confirmDialog.setCancelText("Annulla");
                confirmDialog.setConfirmText("Elimina");
                confirmDialog.setConfirmButtonTheme("error primary");

                confirmDialog.addConfirmListener(event -> {
                    try {
                        pazienteT0Service.deleteAccesso(scenarioId, accesso.getIdAccesso(), isVenoso); // Elimina l'accesso
                        // Mostra notifica di successo
                        Notification.show("Accesso " + (isVenoso ? "venoso" : "arterioso") + " eliminato con successo", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        // Ricarica gli elementi nella griglia per riflettere la modifica
                        grid.setItems(isVenoso ? pazienteT0Service.getPazienteT0ById(scenarioId).getAccessiVenosi() : pazienteT0Service.getPazienteT0ById(scenarioId).getAccessiArteriosi());
                    } catch (Exception ex) {
                        logger.error("Errore durante l'eliminazione dell'accesso", ex);
                        Notification.show("Errore durante l'eliminazione dell'accesso", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });

                confirmDialog.open(); // Apre il dialog di conferma
            });

            return deleteButton;
        }).setHeader("Azioni").setAutoWidth(true).setFlexGrow(0); // Colonna delle azioni non espandibile

        grid.setAllRowsVisible(true); // Mostra tutte le righe della griglia senza scrollbar interne
        return grid;
    }
}