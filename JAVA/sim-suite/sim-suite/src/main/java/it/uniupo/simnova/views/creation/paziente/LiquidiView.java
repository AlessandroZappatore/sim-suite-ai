package it.uniupo.simnova.views.creation.paziente;

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
 * Vista per la gestione dei liquidi e delle dosi dei farmaci nello scenario di simulazione.
 * Permette di inserire e modificare la lista di liquidi e le dosi dei farmaci disponibili
 * all'inizio della simulazione (T0). Questa vista è parte del flusso di creazione dello scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Liquidi e dosi farmaci")
@Route(value = "liquidi", layout = MainLayout.class)
public class LiquidiView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori nella vista LiquidiView.
     */
    private static final Logger logger = LoggerFactory.getLogger(LiquidiView.class);

    /**
     * Servizio per la gestione degli scenari, utilizzato per recuperare e aggiornare i dati dello scenario corrente.
     */
    private final ScenarioService scenarioService;
    /**
     * Editor WYSIWYG per la gestione del testo dei liquidi e delle dosi dei farmaci.
     * Utilizza TinyMCE per fornire un'interfaccia di editing ricca e intuitiva.
     */
    private final TinyMce liquidiEditor;
    /**
     * ID dello scenario corrente, utilizzato per identificare quale scenario si sta modificando.
     * Questo ID viene passato come parametro nell'URL della vista.
     */
    private Integer scenarioId;

    /**
     * Costruttore della vista {@code LiquidiView}.
     * Inizializza i servizi e configura la struttura base dell'interfaccia utente.
     *
     * @param scenarioService    Il servizio per la gestione degli scenari.
     * @param fileStorageService Il servizio per la gestione dei file, utilizzato per l'AppHeader.
     */
    public LiquidiView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna al moulage");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Liquidi e dosi farmaci",
                "Inserisci i liquidi e dosi farmaci disponibili all'inizio della simulazione (T0)",
                VaadinIcon.DROP.create(), // Icona a forma di goccia
                "var(--lumo-primary-color)"
        );

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        liquidiEditor = TinyEditor.getEditor(); // Crea l'editor TinyMCE

        contentLayout.add(headerSection, liquidiEditor);

        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione indietro (alla vista del moulage)
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("moulage/" + scenarioId)));

        // Listener per il pulsante "Avanti"
        nextButton.addClickListener(e -> {
            String content = liquidiEditor.getValue();
            // Verifica se il contenuto dell'editor è vuoto o contiene solo tag HTML vuoti
            boolean isEmpty = content == null || content.trim().isEmpty() ||
                    content.trim().equals("<p><br></p>") || content.trim().equals("<p></p>");

            if (isEmpty) {
                // Se il contenuto è vuoto, mostra un dialogo di conferma prima di procedere
                StyleApp.createConfirmDialog(
                        "Contenuto vuoto",
                        "Sei sicuro di voler continuare senza definire liquidi o dosi di farmaci?",
                        "Prosegui",
                        "Annulla",
                        () -> saveLiquidiAndNavigate(nextButton.getUI()) // Azione da eseguire se si conferma
                );
            } else {
                // Se il contenuto non è vuoto, procede direttamente al salvataggio
                saveLiquidiAndNavigate(nextButton.getUI());
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

            loadExistingLiquidi(); // Carica i dati esistenti se presenti
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            // Reindirizza a una pagina di errore 404
            event.rerouteToError(NotFoundException.class, "ID scenario " + parameter + " non valido.");
        }
    }

    /**
     * Carica i liquidi e le dosi di farmaci esistenti per lo scenario corrente.
     * Popola l'editor TinyMCE con il testo recuperato dal database.
     */
    private void loadExistingLiquidi() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        if (scenario != null && scenario.getLiquidi() != null && !scenario.getLiquidi().isEmpty()) {
            liquidiEditor.setValue(scenario.getLiquidi());
        }
    }

    /**
     * Salva il contenuto dei liquidi e delle dosi di farmaci nell'editor
     * e naviga alla vista successiva (`pazienteT0`). Mostra notifiche di stato e di errore.
     *
     * @param uiOptional L'istanza opzionale dell'UI corrente per la navigazione.
     */
    private void saveLiquidiAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar); // Mostra una progress bar durante il salvataggio

            try {
                boolean success = scenarioService.updateScenarioLiquidi(
                        scenarioId, liquidiEditor.getValue() // Salva il contenuto dell'editor
                );

                // Aggiorna l'UI dopo il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar); // Rimuove la progress bar
                    if (success) {
                        ui.navigate("pazienteT0/" + scenarioId); // Naviga alla vista del Paziente T0
                    } else {
                        Notification.show("Errore durante il salvataggio di liquidi e dosi farmaci", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio di liquidi e dosi farmaci per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                // Gestisce errori durante il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio di liquidi e dosi farmaci per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}