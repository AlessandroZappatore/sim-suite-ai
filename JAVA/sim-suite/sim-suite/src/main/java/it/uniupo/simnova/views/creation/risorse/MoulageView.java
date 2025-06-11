package it.uniupo.simnova.views.creation.risorse;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

/**
 * Vista per la gestione del ***moulage** (trucco ed effetti speciali) nello scenario di simulazione.
 * Questa vista permette all'utente di inserire o modificare la descrizione del trucco
 * da applicare al manichino/paziente simulato per lo scenario corrente.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Moulage")
@Route(value = "moulage", layout = MainLayout.class)
public class MoulageView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori nella vista Moulage.
     */
    private static final Logger logger = LoggerFactory.getLogger(MoulageView.class);

    /**
     * Servizio per la gestione degli scenari, utilizzato per recuperare e aggiornare i dati del moulage.
     */
    private final ScenarioService scenarioService;
    /**
     * Editor WYSIWYG TinyMCE per la descrizione del moulage.
     * Permette di inserire testo formattato e immagini per rappresentare il trucco.
     */
    private final TinyMce moulageEditor;
    /**
     * ID dello scenario corrente, utilizzato per identificare quale moulage modificare o visualizzare.
     */
    private Integer scenarioId;

    /**
     * Costruttore della vista {@code MoulageView}.
     * Inizializza i servizi e configura la struttura base dell'interfaccia utente.
     *
     * @param scenarioService    Il servizio per la gestione degli scenari.
     * @param fileStorageService Il servizio per la gestione dei file, utilizzato per l'AppHeader.
     */
    public MoulageView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna agli esami e referti");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Moulage",
                "Inserisci la descrizione del trucco da applicare al manichino/paziente simulato",
                VaadinIcon.EYE.create(), // Icona a forma di occhio
                "var(--lumo-primary-color)"
        );

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        moulageEditor = TinyEditor.getEditor(); // Crea l'editor TinyMCE

        contentLayout.add(headerSection, moulageEditor);

        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione indietro (alla vista esami e referti)
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("esamiReferti/" + scenarioId)));

        // Listener per il pulsante "Avanti"
        nextButton.addClickListener(e -> {
            String content = moulageEditor.getValue();
            // Verifica se il contenuto dell'editor è vuoto o contiene solo tag HTML vuoti
            boolean isEmpty = content == null || content.trim().isEmpty() ||
                    content.trim().equals("<p><br></p>") || content.trim().equals("<p></p>");

            if (isEmpty) {
                // Se il contenuto è vuoto, mostra un dialogo di conferma prima di procedere
                StyleApp.createConfirmDialog(
                        "Descrizione vuota",
                        "Sei sicuro di voler continuare senza una descrizione del moulage?",
                        "Prosegui",
                        "Annulla",
                        () -> saveMoulageAndNavigate(nextButton.getUI()) // Azione da eseguire se si conferma
                );
            } else {
                // Se il contenuto non è vuoto, procede direttamente al salvataggio
                saveMoulageAndNavigate(nextButton.getUI());
            }
        });
    }

    /**
     * Gestisce il parametro dell'URL (ID dello scenario).
     * Verifica la validità dell'ID e se lo scenario esiste.
     *
     * @param event     L'evento di navigazione.
     * @param parameter L'ID dello scenario come stringa.
     * @throws NotFoundException Se l'ID dello scenario non è valido o lo scenario non esiste.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            // Verifica che lo scenario esista nel servizio
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }

            loadExistingMoulage(); // Carica il moulage esistente se presente
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            // Reindirizza a una pagina di errore 404
            event.rerouteToError(NotFoundException.class, "ID scenario " + parameter + " non valido.");
        }
    }

    /**
     * Carica la descrizione del moulage esistente per lo scenario corrente.
     * Popola l'editor TinyMCE con il testo recuperato dal database.
     */
    private void loadExistingMoulage() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getMoulage() != null && !scenario.getMoulage().isEmpty()) {
            moulageEditor.setValue(scenario.getMoulage());
        }
    }

    /**
     * Salva il contenuto del moulage dall'editor e naviga alla vista successiva (`liquidi`).
     * Mostra notifiche di stato e di errore.
     *
     * @param uiOptional L'istanza opzionale dell'UI corrente per la navigazione.
     */
    private void saveMoulageAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar); // Mostra una progress bar durante il salvataggio

            try {
                boolean success = scenarioService.updateScenarioMoulage(
                        scenarioId, moulageEditor.getValue() // Salva il contenuto dell'editor
                );

                // Aggiorna l'UI dopo il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar); // Rimuove la progress bar
                    if (success) {
                        ui.navigate("liquidi/" + scenarioId); // Naviga alla vista successiva (Liquidi e dosi farmaci)
                    } else {
                        Notification.show("Errore durante il salvataggio del moulage", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio del moulage per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                // Gestisce errori durante il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio del moulage per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}