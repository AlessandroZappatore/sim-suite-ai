package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

import java.util.Optional;

/**
 * Vista per la gestione del briefing dello scenario di simulazione.
 * <p>
 * Questa vista consente agli utenti di definire il testo introduttivo che verrà
 * presentato ai discenti prima dell'inizio della simulazione. Fa parte del flusso
 * di creazione e modifica guidata dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Briefing")
@Route(value = "briefing", layout = MainLayout.class)
public class BriefingView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(BriefingView.class);

    /**
     * Il servizio per la gestione delle operazioni sugli scenari.
     */
    private final ScenarioService scenarioService;

    /**
     * L'editor di testo avanzato (TinyMCE) utilizzato per la stesura del briefing.
     */
    private final TinyMce briefingEditor;

    /**
     * L'ID dello scenario corrente, passato come parametro URL.
     */
    private Integer scenarioId;

    /**
     * Costruisce una nuova istanza di <code>BriefingView</code>.
     * Inizializza l'interfaccia utente, inclusi l'header, il corpo centrale con l'editor di testo
     * e il footer con i bottoni di navigazione.
     *
     * @param scenarioService    Il servizio per la gestione degli scenari.
     * @param fileStorageService Il servizio per la gestione dei file, utilizzato per l'intestazione dell'applicazione.
     */
    public BriefingView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configura il layout principale della vista.
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // Configura l'header personalizzato con un bottone "Indietro" e l'header dell'app.
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna alla descrizione");
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // Configura il layout per il contenuto centrale.
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Sezione dell'intestazione visuale per la vista, con titolo, sottotitolo e icona.
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "BRIEFING",
                "Definisci il briefing che verrà mostrato ai discenti prima della simulazione.",
                VaadinIcon.INFO_CIRCLE.create(),
                "var(--lumo-primary-color)"
        );

        // Inizializza l'editor TinyMCE per il briefing.
        briefingEditor = TinyEditor.getEditor();
        contentLayout.add(headerSection, briefingEditor);

        // Configura il footer con il bottone "Avanti".
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunge tutte le sezioni al layout principale.
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per il bottone "Indietro".
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("descrizione/" + scenarioId)));

        // Listener per il bottone "Avanti".
        nextButton.addClickListener(e -> {
            String content = briefingEditor.getValue();
            // Controlla se il contenuto è vuoto o contiene solo tag HTML vuoti.
            boolean isEmpty = content == null || content.trim().isEmpty() ||
                    content.trim().equals("<p><br></p>") || content.trim().equals("<p></p>");

            if (isEmpty) {
                // Se il briefing è vuoto, chiede conferma all'utente prima di proseguire.
                StyleApp.createConfirmDialog(
                        "Briefing Vuoto",
                        "Il briefing è attualmente vuoto. Sei sicuro di voler continuare senza definirlo? Un briefing chiaro è fondamentale per la simulazione.",
                        "Prosegui", // Testo per il bottone di conferma
                        "Annulla",  // Testo per il bottone di annullamento
                        () -> saveBriefingAndNavigate(nextButton.getUI()) // Callback se l'utente conferma.
                );
            } else {
                // Se il briefing non è vuoto, procede direttamente al salvataggio e alla navigazione.
                saveBriefingAndNavigate(nextButton.getUI());
            }
        });
    }

    /**
     * Implementazione del metodo {@link HasUrlParameter#setParameter(BeforeEvent, Object)}
     * per gestire l'ID dello scenario passato tramite l'URL.
     * Questo metodo è invocato automaticamente da Vaadin all'apertura della vista.
     *
     * @param event     L'evento di navigazione.
     * @param parameter L'ID dello scenario come {@link String}. Se <code>null</code> o non valido,
     *                  la navigazione verrà reindirizzata a una pagina di errore.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            // Verifica che il parametro ID non sia nullo o vuoto.
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("ID scenario non fornito nell'URL per la vista Briefing. Re indirizzamento a pagina di errore.");
                throw new NumberFormatException("ID scenario non fornito."); // Lancia per cattura nel catch.
            }

            this.scenarioId = Integer.parseInt(parameter); // Converte l'ID da String a Integer.
            // Verifica che l'ID sia valido e che lo scenario esista nel database.
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Tentativo di accesso a scenario non esistente o con ID non valido: {}. Re indirizzamento a pagina di errore.", scenarioId);
                throw new NumberFormatException("Scenario con ID " + scenarioId + " non trovato o non valido."); // Lancia per cattura nel catch.
            }

            logger.info("Caricamento briefing per lo scenario ID: {}.", scenarioId);
            loadExistingBriefing(); // Carica il briefing esistente associato allo scenario.

        } catch (NumberFormatException e) {
            logger.error("Errore: ID scenario non valido ricevuto come parametro: '{}'. Dettagli: {}. Re indirizzamento a NotFoundException.", parameter, e.getMessage());
            event.rerouteToError(NotFoundException.class, "Formato ID scenario non valido o scenario non trovato: " + parameter + ". Assicurati che l'ID sia un numero intero valido e che lo scenario esista.");
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'impostazione dei parametri per la vista Briefing per scenario ID: {}. Dettagli: {}", parameter, e.getMessage(), e);
            event.rerouteToError(NotFoundException.class, "Errore durante il caricamento della pagina del briefing. Riprova più tardi.");
        }
    }

    /**
     * Carica il testo del briefing esistente per lo scenario corrente dal database
     * e lo imposta nell'editor TinyMCE.
     */
    private void loadExistingBriefing() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        // Se lo scenario esiste e ha un briefing non nullo e non vuoto, lo imposta nell'editor.
        if (scenario != null && scenario.getBriefing() != null && !scenario.getBriefing().isEmpty()) {
            briefingEditor.setValue(scenario.getBriefing());
            logger.debug("Briefing esistente caricato per lo scenario ID {}.", scenarioId);
        } else {
            logger.debug("Nessun briefing esistente trovato per lo scenario ID {}. L'editor sarà vuoto.", scenarioId);
        }
    }

    /**
     * Salva il contenuto dell'editor del briefing nel database e naviga alla vista successiva.
     * La navigazione successiva dipende dal tipo di paziente dello scenario (pediatrico o meno).
     * Una {@link ProgressBar} viene mostrata durante l'operazione di salvataggio.
     *
     * @param uiOptional L'{@link Optional} che incapsula l'istanza di {@link UI} corrente.
     */
    private void saveBriefingAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = scenarioService.updateScenarioBriefing(
                        scenarioId, briefingEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        if (scenarioService.isPediatric(scenarioId))
                            ui.navigate("infoGenitori/" + scenarioId);
                        else
                            ui.navigate("pattoaula/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio del briefing",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio del briefing", e);
                });
            }
        });
    }
}