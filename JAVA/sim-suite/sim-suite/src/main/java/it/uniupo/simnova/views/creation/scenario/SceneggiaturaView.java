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
import it.uniupo.simnova.domain.scenario.PatientSimulatedScenario;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
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
 * Vista per la gestione della sceneggiatura di uno scenario di simulazione.
 *
 * <p>Questa vista consente all'utente di inserire o modificare la sceneggiatura
 * dettagliata dello scenario corrente, inclusi azioni, dialoghi ed eventi chiave.
 * È specificamente progettata per gli scenari di tipo "Patient Simulated Scenario".</p>
 *
 * <p>Implementa {@link HasUrlParameter} per ricevere l'ID dello scenario come parametro nell'URL.</p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Sceneggiatura")
@Route(value = "sceneggiatura", layout = MainLayout.class)
public class SceneggiaturaView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(SceneggiaturaView.class);

    /**
     * Il servizio per la gestione delle operazioni sugli scenari.
     */
    private final ScenarioService scenarioService;

    /**
     * Il servizio specifico per la gestione degli scenari di tipo "Patient Simulated Scenario".
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;

    /**
     * L'editor di testo avanzato (TinyMCE) utilizzato per la stesura della sceneggiatura.
     */
    private final TinyMce sceneggiaturaEditor;

    /**
     * L'ID dello scenario corrente, passato come parametro URL.
     */
    private Integer scenarioId;

    /**
     * Costruisce una nuova istanza di <code>SceneggiaturaView</code>.
     * Inizializza l'interfaccia utente, inclusi l'header, il corpo centrale con l'editor di testo
     * e il footer con i bottoni di navigazione.
     *
     * @param scenarioService                 Il servizio per la gestione degli scenari.
     * @param fileStorageService              Il servizio per la gestione dei file, utilizzato per l'intestazione dell'applicazione.
     * @param patientSimulatedScenarioService Il servizio per la gestione degli scenari di tipo "Patient Simulated Scenario".
     */
    public SceneggiaturaView(ScenarioService scenarioService, FileStorageService fileStorageService, PatientSimulatedScenarioService patientSimulatedScenarioService) {
        this.scenarioService = scenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;

        // Configura il layout principale della vista.
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // Configura l'header personalizzato con un bottone "Indietro" e l'header dell'app.
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna ai tempi");
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // Sezione dell'intestazione visuale per la vista, con titolo, sottotitolo e icona.
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "SCENEGGIATURA",
                "Inserisci la sceneggiatura dettagliata dello scenario corrente, includendo azioni, dialoghi ed eventi chiave.",
                VaadinIcon.FILE_TEXT.create(),
                "var(--lumo-primary-color)"
        );

        // Configura il layout per il contenuto centrale.
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Inizializza l'editor TinyMCE per la sceneggiatura.
        sceneggiaturaEditor = TinyEditor.getEditor();

        // Aggiunge le sezioni dell'header e l'editor al layout del contenuto.
        contentLayout.add(headerSection, sceneggiaturaEditor);

        // Configura il footer con il bottone "Avanti".
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunge tutte le sezioni al layout principale.
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Listener per il bottone "Indietro".
        // Naviga alla vista "tempi" passando l'ID dello scenario corrente.
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("tempi/" + scenarioId)));

        // Listener per il bottone "Avanti".
        nextButton.addClickListener(e -> {
            String content = sceneggiaturaEditor.getValue();
            // Controlla se il contenuto dell'editor è vuoto o contiene solo tag HTML vuoti.
            boolean isEmpty = content == null || content.trim().isEmpty() ||
                    content.trim().equals("<p><br></p>") || content.trim().equals("<p></p>");

            if (isEmpty) {
                // Se il patto d'aula è vuoto, chiede conferma all'utente prima di proseguire.
                StyleApp.createConfirmDialog(
                        "Sceneggiatura Vuota",
                        "La sceneggiatura è vuota. Sei sicuro di voler procedere senza salvare?",
                        "Prosegui", // Testo per il bottone di conferma.
                        "Annulla",  // Testo per il bottone di annullamento.
                        () -> saveSceneggiaturaAndNavigate(nextButton.getUI()) // Callback se l'utente conferma.
                );
            } else {
                // Se il patto d'aula non è vuoto, procede direttamente al salvataggio e alla navigazione.
                saveSceneggiaturaAndNavigate(nextButton.getUI());
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
     *                  la navigazione verrà reindirizzata a una pagina di errore. Inoltre, verifica che lo scenario
     *                  sia effettivamente di tipo "Patient Simulated Scenario".
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            // Verifica che il parametro ID non sia nullo o vuoto.
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("ID scenario non fornito nell'URL per la vista Sceneggiatura. Re indirizzamento a pagina di errore.");
                throw new NumberFormatException("ID scenario non fornito."); // Lancia per cattura nel catch.
            }

            this.scenarioId = Integer.parseInt(parameter); // Converte l'ID da String a Integer.
            // Verifica che l'ID sia valido e che lo scenario esista nel database.
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Tentativo di accesso a scenario non esistente o con ID non valido: {}. Re indirizzamento a pagina di errore.", scenarioId);
                throw new NumberFormatException("Scenario con ID " + scenarioId + " non trovato o non valido."); // Lancia per cattura nel catch.
            }

            // Verifica che lo scenario sia di tipo "Patient Simulated Scenario".
            String scenarioType = scenarioService.getScenarioType(scenarioId);
            if (!"Patient Simulated Scenario".equals(scenarioType)) {
                logger.warn("L'utente ha tentato di accedere alla vista Sceneggiatura per uno scenario di tipo '{}' (ID {}). Questa funzionalità è solo per 'Patient Simulated Scenario'.", scenarioType, scenarioId);
                event.rerouteToError(NotFoundException.class, "Questa funzionalità è disponibile solo per scenari di tipo 'Patient Simulated Scenario'. Lo scenario con ID " + scenarioId + " è di tipo " + scenarioType + ".");
                return;
            }

            logger.info("Caricamento sceneggiatura per lo scenario ID: {}.", scenarioId);
            loadExistingSceneggiatura(); // Carica la sceneggiatura esistente associata allo scenario.

        } catch (NumberFormatException e) {
            logger.error("Errore: ID scenario non valido ricevuto come parametro: '{}'. Dettagli: {}. Re indirizzamento a NotFoundException.", parameter, e.getMessage());
            event.rerouteToError(NotFoundException.class, "Formato ID scenario non valido o scenario non trovato: " + parameter + ". Assicurati che l'ID sia un numero intero valido e che lo scenario esista.");
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'impostazione dei parametri per la vista Sceneggiatura per scenario ID: {}. Dettagli: {}", parameter, e.getMessage(), e);
            event.rerouteToError(NotFoundException.class, "Errore durante il caricamento della pagina della sceneggiatura. Riprova più tardi.");
        }
    }

    /**
     * Carica il testo della sceneggiatura esistente per lo scenario corrente dal database
     * e lo imposta nell'editor TinyMCE.
     */
    private void loadExistingSceneggiatura() {
        // Recupera l'oggetto PatientSimulatedScenario dal servizio.
        PatientSimulatedScenario scenario = patientSimulatedScenarioService.getPatientSimulatedScenarioById(scenarioId);
        // Se lo scenario esiste e ha una sceneggiatura non nulla e non vuota, la imposta nell'editor.
        if (scenario != null && scenario.getSceneggiatura() != null && !scenario.getSceneggiatura().isEmpty()) {
            sceneggiaturaEditor.setValue(scenario.getSceneggiatura());
            logger.debug("Sceneggiatura esistente caricata per lo scenario ID {}.", scenarioId);
        } else {
            logger.debug("Nessuna sceneggiatura esistente trovata per lo scenario ID {}. L'editor sarà vuoto.", scenarioId);
        }
    }

    /**
     * Salva il contenuto dell'editor della sceneggiatura nel database e naviga alla vista successiva
     * (Dettagli Scenario). Una {@link ProgressBar} viene mostrata durante l'operazione di salvataggio.
     *
     * @param uiOptional L'{@link Optional} che incapsula l'istanza di {@link UI} corrente.
     */
    private void saveSceneggiaturaAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {

            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {

                boolean success = patientSimulatedScenarioService.updateScenarioSceneggiatura(
                        scenarioId, sceneggiaturaEditor.getValue()
                );

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        logger.info("Sceneggiatura salvata con successo per lo scenario con ID: {}", scenarioId);
                        ui.navigate("scenari/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio della sceneggiatura", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio della sceneggiatura per lo scenario con ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio della sceneggiatura per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}