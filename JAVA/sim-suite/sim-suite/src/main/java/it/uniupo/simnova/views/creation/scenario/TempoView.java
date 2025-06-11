package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.AdditionalParamDialog;
import it.uniupo.simnova.views.ui.helper.TimeSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;

import static it.uniupo.simnova.views.constant.AdditionParametersConst.ADDITIONAL_PARAMETERS;
import static it.uniupo.simnova.views.constant.AdditionParametersConst.CUSTOM_PARAMETER_KEY;

/**
 * Vista per la creazione e gestione dei "tempi" in uno scenario avanzato di simulazione.
 * <p>
 * Questa vista consente di definire una sequenza di stati (tempi) del paziente,
 * specificando per ognuno i parametri vitali, le azioni che i discenti devono intraprendere
 * per progredire e le possibili transizioni. Permette anche di aggiungere parametri vitali
 * e metriche personalizzate.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
@PageTitle("Tempi Scenario")
@Route(value = "tempi", layout = MainLayout.class)
public class TempoView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(TempoView.class);

    /**
     * Il contenitore principale che ospita tutte le sezioni di tempo (T0, T1, T2...).
     */
    private final VerticalLayout timeSectionsContainer;

    /**
     * Una lista che tiene traccia di tutti gli oggetti {@link TimeSection} attualmente visualizzati nella UI.
     */
    private final List<TimeSection> timeSections = new ArrayList<>();

    /**
     * Il bottone per navigare alla schermata successiva del flusso di creazione/modifica dello scenario.
     */
    private final Button nextButton;

    /**
     * Il servizio per la gestione delle operazioni di base sugli scenari.
     */
    private final ScenarioService scenarioService;

    /**
     * Il servizio specifico per la gestione degli scenari avanzati, che include le operazioni sui tempi.
     */
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Il servizio per la gestione dei dati del paziente al tempo zero (T0), ovvero lo stato iniziale.
     */
    private final PazienteT0Service pazienteT0Service;

    /**
     * Un contatore utilizzato per assegnare un numero progressivo (ID) a ogni nuova sezione di tempo (T1, T2...).
     * Inizia da 1, poiché T0 ha un trattamento speciale.
     */
    private int timeCount = 1;

    /**
     * L'ID dello scenario corrente, passato come parametro URL.
     */
    private int scenarioId;

    /**
     * La modalità di apertura della vista ("create" per la creazione di un nuovo scenario, "edit" per la modifica di uno esistente).
     */
    private String mode;

    /**
     * Costruisce una nuova istanza di <code>TempoView</code>.
     * Inizializza l'interfaccia utente, inclusi l'header, il corpo centrale con il contenitore dei tempi
     * e il footer con i bottoni di navigazione.
     *
     * @param scenarioService         Il servizio per la gestione degli scenari.
     * @param fileStorageService      Il servizio per la gestione dei file, utilizzato per l'intestazione dell'applicazione.
     * @param advancedScenarioService Il servizio specifico per gli scenari avanzati.
     * @param pazienteT0Service       Il servizio per la gestione dei dati del paziente T0.
     */
    public TempoView(ScenarioService scenarioService, FileStorageService fileStorageService,
                     AdvancedScenarioService advancedScenarioService, PazienteT0Service pazienteT0Service) {
        this.scenarioService = scenarioService;
        this.advancedScenarioService = advancedScenarioService;
        this.pazienteT0Service = pazienteT0Service;

        // Configura il layout principale della vista.
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // Configura l'header dell'applicazione e il bottone "Indietro".
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna all'esame fisico");

        // Listener per il bottone "Indietro": naviga alla vista "esameFisico".
        backButton.addClickListener(e -> {
            if (scenarioId > 0) {
                // Se scenarioId è valido, naviga alla vista precedente nel flusso di creazione.
                e.getSource().getUI().ifPresent(ui -> ui.navigate("esameFisico/" + scenarioId));
            } else {
                // Altrimenti, torna indietro nella cronologia del browser.
                e.getSource().getUI().ifPresent(ui -> ui.getPage().getHistory().back());
            }
        });

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // Configura il layout per il contenuto centrale.
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Sezione dell'intestazione visuale per la vista, con titolo, sottotitolo e icona.
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "DEFINIZIONE TEMPI SCENARIO",
                "Definisci i tempi dello scenario (T0, T1, T2...). Per ogni tempo, specifica i parametri vitali, " +
                        "eventuali parametri aggiuntivi, l'azione richiesta per procedere e le transizioni possibili (Tempo SI / Tempo NO). " +
                        "T0 rappresenta lo stato iniziale del paziente.",
                VaadinIcon.CLOCK.create(), // Icona dell'orologio.
                "var(--lumo-primary-color)"
        );

        // Contenitore per le sezioni dei tempi.
        timeSectionsContainer = new VerticalLayout();
        timeSectionsContainer.setWidthFull();
        timeSectionsContainer.setSpacing(true);

        // Bottone per aggiungere nuove sezioni di tempo (Tn).
        Button addTimeButton = new Button("Aggiungi Tempo (Tn)", new Icon(VaadinIcon.PLUS_CIRCLE));
        addTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTimeButton.addClassName(LumoUtility.Margin.Top.XLARGE);
        addTimeButton.addClickListener(event -> addTimeSection(timeCount++)); // Aggiunge una nuova sezione e incrementa il contatore.

        // Aggiunge le sezioni dell'header e il contenitore dei tempi al layout del contenuto.
        contentLayout.add(headerSection, timeSectionsContainer, addTimeButton);

        // Configura il bottone "Avanti" e il suo listener.
        nextButton = StyleApp.getNextButton();
        nextButton.addClickListener(e -> saveAllTimeSections()); // Al click, tenta di salvare tutti i tempi.

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunge tutte le sezioni al layout principale.
        mainLayout.add(customHeader, contentLayout, footerLayout);
    }

    /**
     * Implementazione del metodo {@link HasUrlParameter#setParameter(BeforeEvent, Object)}.
     * Questo metodo viene chiamato da Vaadin quando la vista viene navigata con un parametro URL.
     * Gestisce l'estrazione dell'ID dello scenario e della modalità ("create" o "edit") dall'URL.
     *
     * @param event     L'evento di navigazione.
     * @param parameter Il parametro URL, che può contenere l'ID dello scenario e opzionalmente la modalità "edit" (es. "123" o "123/edit").
     * @throws NotFoundException Se il parametro è nullo, vuoto, non un numero valido, non positivo, o se lo scenario non esiste.
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro URL mancante per la vista Tempi. ID scenario richiesto.");
                throw new NumberFormatException("ID Scenario è richiesto.");
            }

            // Divide il parametro per ottenere l'ID e la modalità.
            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];

            this.scenarioId = Integer.parseInt(scenarioIdStr.trim());
            // Validazione dell'ID dello scenario.
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("ID Scenario non valido o non esistente: {}. Re indirizzamento a pagina di errore.", scenarioId);
                throw new NumberFormatException("ID Scenario non valido o non esistente.");
            }

            // Verifica che lo scenario non sia di tipo "Quick Scenario".
            if (scenarioService.getScenarioType(scenarioId).equals("Quick Scenario")) {
                logger.warn("Tentativo di accedere alla gestione dei Tempi per un Quick Scenario (ID {}). Questa funzionalità è solo per scenari avanzati/simulati.", scenarioId);
                throw new NumberFormatException("I Quick Scenario non supportano la gestione dei tempi.");
            }

            // Determina la modalità: "edit" se il secondo segmento è "edit", altrimenti "create".
            mode = parts.length > 1 && "edit".equalsIgnoreCase(parts[1]) ? "edit" : "create";

            logger.info("Vista Tempi caricata per lo scenario ID: {}, in modalità: {}.", this.scenarioId, mode);

            VerticalLayout mainLayout = getContent();

            // Nasconde l'header (AppHeader) in modalità "edit" per un layout più compatto.
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst() // Trova il primo HorizontalLayout (presumibilmente l'header).
                    .ifPresent(headerLayout -> headerLayout.setVisible(!"edit".equals(mode)));

            // Nasconde la CreditsComponent nel footer in modalità "edit".
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second) // Trova l'ultimo HorizontalLayout (presumibilmente il footer).
                    .ifPresent(footerLayout -> footerLayout.getChildren()
                            .filter(component -> component instanceof CreditsComponent)
                            .forEach(credits -> credits.setVisible(!"edit".equals(mode))));

            // Carica i dati iniziali (T0) e, in modalità "edit", anche i tempi esistenti.
            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT attiva: caricamento dati Tempi esistenti per lo scenario {}.", this.scenarioId);
                nextButton.setText("Salva Modifiche"); // Cambia testo del bottone.
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS); // Aggiunge stile di successo.
                nextButton.setIcon(new Icon(VaadinIcon.CHECK)); // Cambia icona.
            } else {
                logger.info("Modalità CREATE attiva: caricamento dati iniziali T0 e preparazione per nuovi tempi per lo scenario {}.", this.scenarioId);
            }
            loadInitialData();
            loadExistingTimes();
        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dell'ID Scenario: '{}'. Dettagli: {}", parameter, e.getMessage(), e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante. Assicurati che l'URL sia corretto.");
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'impostazione dei parametri per la vista Tempi: {}", e.getMessage(), e);
            event.rerouteToError(NotFoundException.class, "Si è verificato un errore durante il caricamento della pagina. Riprova.");
        }
    }

    /**
     * Aggiunge una nuova sezione temporale {@link TimeSection} alla vista.
     * Ogni sezione rappresenta un momento specifico nello scenario (es. T1, T2...).
     * La sezione include campi per i parametri vitali, azioni, transizioni e parametri aggiuntivi.
     *
     * @param timeNumber Il numero progressivo del tempo da aggiungere (es. 1 per T1, 2 per T2).
     */
    private void addTimeSection(int timeNumber) {
        // Verifica se una sezione per questo numero di tempo esiste già per evitare duplicati.
        boolean alreadyExists = timeSections.stream().anyMatch(ts -> ts.getTimeNumber() == timeNumber);
        if (alreadyExists) {
            logger.debug("Sezione per T{} esiste già, non viene aggiunta di nuovo.", timeNumber);
            return;
        }

        // Crea una nuova istanza di TimeSection.
        TimeSection timeSection = new TimeSection(timeNumber, scenarioService, timeSections, timeSectionsContainer, scenarioId);
        timeSections.add(timeSection); // Aggiunge la nuova sezione alla lista di gestione.

        // Ordina le sezioni per numero di tempo e le riaggiunge al contenitore per mantenere l'ordine.
        timeSections.sort(Comparator.comparingInt(TimeSection::getTimeNumber));
        timeSectionsContainer.removeAll(); // Rimuove tutte le sezioni esistenti.
        timeSections.forEach(ts -> timeSectionsContainer.add(ts.getLayout())); // Aggiunge le sezioni ordinate.

        // Se è la sezione T0, nasconde il bottone di rimozione (T0 non può essere rimosso).
        if (timeNumber == 0) {
            timeSection.hideRemoveButton();
        }

        // Aggiunge un bottone per aprire il dialog di aggiunta parametri aggiuntivi.
        Button addParamsButton = new Button("Aggiungi Parametri Aggiuntivi", new Icon(VaadinIcon.PLUS));
        addParamsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addParamsButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addParamsButton.addClickListener(e -> AdditionalParamDialog.showAdditionalParamsDialog(timeSection));

        // Aggiunge il bottone al layout dei parametri medici della sezione.
        timeSection.getMedicalParamsForm().add(addParamsButton);

        logger.info("Aggiunta nuova sezione Tempo T{} per lo scenario ID {}.", timeNumber, scenarioId);
    }


    /**
     * Salva tutti i tempi definiti dall'utente nel database.
     * Raccoglie i dati da ogni {@link TimeSection}, li converte in oggetti {@link Tempo},
     * e li passa al {@link AdvancedScenarioService} per il salvataggio transazionale.
     * In caso di successo, naviga alla schermata successiva (Dettagli Scenario o Sceneggiatura, a seconda del tipo di scenario).
     * Gestisce gli errori di validazione o di database tramite notifiche all'utente.
     */
    private void saveAllTimeSections() {
        try {
            List<Tempo> allTempi = new ArrayList<>();


            for (TimeSection section : timeSections) {
                Tempo tempo = section.prepareDataForSave();
                allTempi.add(tempo);
                logger.info("Dati preparati per salvare tempo T{}: {}", tempo.getIdTempo(), tempo);
            }


            boolean success = advancedScenarioService.saveTempi(scenarioId, allTempi);

            if (success) {
                if (!mode.equals("edit")) {
                    Notification.show("Tempi dello scenario salvati con successo!", 3000,
                            Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                logger.info("Tempi salvati con successo per scenario {}", scenarioId);


                if ("create".equals(mode)) {
                    String scenarioType = scenarioService.getScenarioType(scenarioId);
                    switch (scenarioType) {
                        case "Advanced Scenario":

                            nextButton.getUI().ifPresent(ui -> ui.navigate("scenari/" + scenarioId));
                            break;
                        case "Patient Simulated Scenario":

                            nextButton.getUI().ifPresent(ui -> ui.navigate("sceneggiatura/" + scenarioId));
                            break;
                        default:
                            Notification.show("Tipo di scenario non riconosciuto per navigazione", 3000,
                                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                            logger.error("Tipo di scenario '{}' non gestito per navigazione post-salvataggio tempi (ID {})",
                                    scenarioType, scenarioId);
                            break;
                    }
                } else if ("edit".equals(mode)) {
                    nextButton.getUI().ifPresent(ui -> ui.navigate("scenari/" + scenarioId));
                    Notification.show("Modifiche ai tempi salvate con successo!", 3000,
                            Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            } else {
                Notification.show("Errore durante il salvataggio dei tempi nel database.", 5000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                logger.error("Errore durante il salvataggio dei tempi (scenarioService.saveTempi ha restituito false) per scenario {}",
                        scenarioId);
            }
        } catch (Exception e) {

            Notification.show("Errore imprevisto durante il salvataggio: " + e.getMessage(), 5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            logger.error("Eccezione durante il salvataggio dei tempi per scenario {}", scenarioId, e);
        }
    }

    /**
     * Carica i dati iniziali per la sezione T0 (stato iniziale del paziente).
     * Recupera i parametri dal {@link PazienteT0} associato allo scenario e li precompila
     * nei campi della sezione T0, rendendoli non modificabili se i dati provengono da PazienteT0.
     * Aggiunge una sezione T0 vuota e modificabile solo se in modalità "create" e PazienteT0 non esiste.
     */
    private void loadInitialData() {
        try {
            PazienteT0 pazienteT0 = pazienteT0Service.getPazienteT0ById(scenarioId);

            // Verifica se una sezione T0 è già presente nell'UI.
            Optional<TimeSection> existingT0 = timeSections.stream()
                    .filter(ts -> ts.getTimeNumber() == 0)
                    .findFirst();

            if (pazienteT0 != null) {
                // Se PazienteT0 esiste nel DB, popola o aggiorna la sezione T0 nell'UI.
                TimeSection t0Section;
                if (existingT0.isEmpty()) {
                    // Se T0 non è ancora nell'UI, lo aggiunge.
                    addTimeSection(0);
                    t0Section = timeSections.stream().filter(ts -> ts.getTimeNumber() == 0).findFirst().orElse(null);
                    if (t0Section == null) {
                        logger.error("Impossibile trovare la sezione T0 appena aggiunta per lo scenario {}.", scenarioId);
                        return;
                    }
                } else {
                    t0Section = existingT0.get();
                }

                // Popola i campi della sezione T0 con i dati di PazienteT0.
                t0Section.setPaValue(pazienteT0.getPA());
                t0Section.setFcValue(pazienteT0.getFC());
                t0Section.setRrValue(pazienteT0.getRR());
                t0Section.setTValue(pazienteT0.getT());
                t0Section.setSpo2Value(pazienteT0.getSpO2());
                t0Section.setFio2Value(pazienteT0.getFiO2());
                t0Section.setLitriO2Value(pazienteT0.getLitriO2());
                t0Section.setEtco2Value(pazienteT0.getEtCO2());

                // Notifica all'utente che i parametri di T0 non sono modificabili direttamente qui.
                Notification.show("I parametri base di T0 derivano dallo stato iniziale del paziente e non sono modificabili direttamente qui.",
                        4000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);

                // Carica i parametri aggiuntivi specifici per T0.
                loadAdditionalParameters(t0Section, 0);

            } else if (existingT0.isEmpty() && "create".equals(mode)) {
                // Se PazienteT0 non esiste nel DB e siamo in modalità "create", aggiunge una sezione T0 vuota e modificabile.
                logger.info("PazienteT0 non trovato per lo scenario {}, aggiunta sezione T0 vuota in modalità create.", scenarioId);
                addTimeSection(0);

            } else if (existingT0.isPresent()) {
                // Questo caso potrebbe indicare un'inconsistenza: T0 è nell'UI ma non nel DB.
                logger.warn("Sezione T0 presente nell'UI ma PazienteT0 non trovato nel DB per lo scenario {}. Controllare consistenza dati.", scenarioId);
                // Potrebbe essere necessario un reset o un messaggio specifico.
            }

        } catch (Exception e) {
            Notification.show("Errore critico nel caricamento dei dati iniziali di T0: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            logger.error("Errore durante il caricamento dei dati iniziali (PazienteT0) per lo scenario {}: {}", scenarioId, e.getMessage(), e);
        }
    }


    /**
     * Carica i dati dei tempi esistenti (T1, T2, ...) dallo scenario salvato nel database.
     * Questo metodo viene chiamato specificamente in modalità "edit" dopo che {@link #loadInitialData()} ha gestito T0.
     * Popola le sezioni temporali della UI con i dati recuperati, gestendo anche i parametri aggiuntivi.
     */
    private void loadExistingTimes() {
        List<Tempo> existingTempi = advancedScenarioService.getTempiByScenarioId(scenarioId);

        if (!existingTempi.isEmpty()) {
            logger.info("Trovati {} tempi esistenti (oltre a T0) per lo scenario ID {}.", existingTempi.size(), scenarioId);

            // Preserva la sezione T0 se già caricata, poi pulisce e ricostruisce la lista e il contenitore.
            TimeSection t0Section = timeSections.stream().filter(ts -> ts.getTimeNumber() == 0).findFirst().orElse(null);
            timeSections.clear();
            timeSectionsContainer.removeAll();
            if (t0Section != null) {
                timeSections.add(t0Section);
                timeSectionsContainer.add(t0Section.getLayout());
            }

            // Itera sui tempi recuperati dal DB e aggiunge/popola le sezioni corrispondenti.
            for (Tempo tempo : existingTempi) {
                int tempoId = tempo.getIdTempo();
                if (tempoId >= 0) { // Assicura che l'ID del tempo sia valido (non negativo).
                    addTimeSection(tempoId); // Aggiunge la sezione UI per questo tempo.
                    TimeSection section = timeSections.stream()
                            .filter(ts -> ts.getTimeNumber() == tempoId)
                            .findFirst()
                            .orElse(null);

                    if (section != null) {
                        // Popola i campi vitali per T1, T2, ...
                        // T0 è già stato gestito da loadInitialData.
                        if (tempoId > 0) {
                            section.paField.setValue(tempo.getPA() != null ? tempo.getPA() : "");
                            section.fcField.setValue(Optional.ofNullable(tempo.getFC()).map(Double::valueOf).orElse(null));
                            section.rrField.setValue(Optional.ofNullable(tempo.getRR()).map(Double::valueOf).orElse(null));
                            section.tField.setValue(Optional.of(tempo.getT()).orElse(null));
                            section.spo2Field.setValue(Optional.ofNullable(tempo.getSpO2()).map(Double::valueOf).orElse(null));
                            section.fio2Field.setValue(Optional.ofNullable(tempo.getFiO2()).map(Double::valueOf).orElse(null));
                            section.litriO2Field.setValue(tempo.getLitriO2());
                            section.etco2Field.setValue(Optional.ofNullable(tempo.getEtCO2()).map(Double::valueOf).orElse(null));
                        }

                        // Popola gli altri campi del tempo.
                        section.actionDetailsArea.setValue(tempo.getAzione() != null ? tempo.getAzione() : "");
                        section.timeIfYesField.setValue(tempo.getTSi());
                        section.timeIfNoField.setValue(tempo.getTNo());
                        section.additionalDetailsArea.setValue(tempo.getAltriDettagli() != null ? tempo.getAltriDettagli() : "");
                        section.ruoloGenitoreArea.setValue(tempo.getRuoloGenitore() != null ? tempo.getRuoloGenitore() : "");

                        // Popola il TimePicker per il timerTempo.
                        if (tempo.getTimerTempo() > 0) {
                            try {
                                section.timerPicker.setValue(LocalTime.ofSecondOfDay(tempo.getTimerTempo()));
                            } catch (Exception e) {
                                logger.warn("Errore nel parsing del timer ({} secondi) per T{} dello scenario ID {}. Il campo sarà vuoto.", tempo.getTimerTempo(), tempoId, scenarioId, e);
                                section.timerPicker.setValue(null);
                            }
                        } else {
                            section.timerPicker.setValue(null); // Se il timer è 0 o negativo, lo imposta a null.
                        }

                        // Carica i parametri aggiuntivi per questo tempo.
                        loadAdditionalParameters(section, tempoId);
                    } else {
                        logger.error("Impossibile trovare/creare la sezione UI per il tempo T{} durante il caricamento dello scenario ID {}. Dati non visualizzati correttamente.", tempoId, scenarioId);
                    }
                }
            }

            // Aggiorna il timeCount per i nuovi tempi da aggiungere, basandosi sul massimo ID esistente.
            timeCount = existingTempi.stream()
                    .mapToInt(Tempo::getIdTempo)
                    .max()
                    .orElse(0) + 1;
            if (timeCount == 0) timeCount = 1; // Assicura che timeCount sia almeno 1 per T1 se non ci sono altri tempi.

            // Ordina e riaggiunge tutte le sezioni al contenitore per garantire l'ordine corretto nella UI.
            timeSections.sort(Comparator.comparingInt(TimeSection::getTimeNumber));
            timeSectionsContainer.removeAll();
            timeSections.forEach(ts -> timeSectionsContainer.add(ts.getLayout()));

        } else {
            logger.info("Nessun tempo (T1, T2...) trovato nel database per lo scenario ID {}.", scenarioId);
        }
    }

    /**
     * Carica i {@link ParametroAggiuntivo parametri aggiuntivi} associati a un tempo specifico
     * (identificato da <code>tempoId</code>) e a un dato scenario (<code>scenarioId</code>) dal database.
     * Aggiunge i campi di input corrispondenti per questi parametri alla {@link TimeSection} fornita,
     * popolandoli con i valori recuperati.
     *
     * @param section La {@link TimeSection} (componente UI) a cui aggiungere e visualizzare i parametri aggiuntivi.
     * @param tempoId L'ID del tempo (0 per T0, 1 per T1, ecc.) di cui caricare i parametri aggiuntivi.
     */
    private void loadAdditionalParameters(TimeSection section, int tempoId) {
        List<ParametroAggiuntivo> params = advancedScenarioService.getParametriAggiuntiviByTempoId(tempoId, scenarioId);

        if (!params.isEmpty()) {
            logger.debug("Caricamento di {} parametri aggiuntivi per il tempo T{} dello scenario ID {}.", params.size(), tempoId, scenarioId);
            for (ParametroAggiuntivo param : params) {
                String paramName = param.getNome();
                String unit = param.getUnitaMisura() != null ? param.getUnitaMisura() : "";
                String valueStr = param.getValore();

                // Costruisce una chiave unica per il parametro, distinguendo tra standard e custom.
                String paramKey = ADDITIONAL_PARAMETERS.keySet().stream()
                        .filter(s -> s.equalsIgnoreCase(paramName))
                        .findFirst()
                        .orElse(CUSTOM_PARAMETER_KEY + "_" + paramName.replaceAll("\\s+", "_")); // Per parametri custom.

                // Costruisce il label visualizzato (nome + unità di misura).
                String label = paramName + (unit.isEmpty() ? "" : " (" + unit + ")");

                // Aggiunge il campo input per il parametro aggiuntivo alla sezione UI.
                section.addCustomParameter(paramKey, label, unit);

                // Popola il campo con il valore recuperato.
                if (section.getCustomParameters().containsKey(paramKey)) {
                    try {
                        if (valueStr != null && !valueStr.trim().isEmpty()) {
                            double value = Double.parseDouble(valueStr.trim().replace(',', '.')); // Gestisce virgola/punto.
                            section.getCustomParameters().get(paramKey).setValue(value);
                        } else {
                            section.getCustomParameters().get(paramKey).setValue(0.0); // Valore nullo o vuoto -> 0.0.
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Errore di parsing del valore '{}' per il parametro '{}' (T{}, Scenario ID {}). Impostato a 0. Dettagli: {}",
                                valueStr, paramName, tempoId, scenarioId, e.getMessage(), e);
                        section.getCustomParameters().get(paramKey).setValue(0.0);
                    } catch (NullPointerException e) {
                        logger.error("Errore: valore nullo inaspettato per il parametro '{}' (T{}, Scenario ID {}). Impostato a 0. Dettagli: {}",
                                paramName, tempoId, scenarioId, e.getMessage(), e);
                        section.getCustomParameters().get(paramKey).setValue(0.0);
                    }
                } else {
                    logger.warn("Campo per il parametro con chiave '{}' non trovato nell'UI dopo l'aggiunta durante il caricamento (Tempo T{}, Scenario ID {}). Assicurati che 'addCustomParameter' crei il campo correttamente.",
                            paramKey, tempoId, scenarioId);
                }
            }
        } else {
            logger.debug("Nessun parametro aggiuntivo trovato per il tempo T{} dello scenario ID {}.", tempoId, scenarioId);
        }
    }
}