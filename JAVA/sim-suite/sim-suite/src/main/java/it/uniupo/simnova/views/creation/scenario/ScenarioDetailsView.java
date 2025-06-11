package it.uniupo.simnova.views.creation.scenario;

import com.flowingcode.vaadin.addons.enhancedtabs.EnhancedTabs;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.NotifierService;
import it.uniupo.simnova.service.ai_api.ExternalApiService;
import it.uniupo.simnova.service.ai_api.LabExamService;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static it.uniupo.simnova.views.ui.helper.TabsSupport.createTabWithIcon;

/**
 * Vista per la visualizzazione dettagliata e la modifica di uno scenario di simulazione.
 * <p>
 * Questa classe gestisce il caricamento, la presentazione e l'aggiornamento dei dettagli completi
 * di uno scenario, inclusi i suoi metadati principali (titolo, autori), i parametri vitali,
 * gli esami e referti, e altre informazioni pertinenti suddivise in schede (tabs).
 * La vista supporta scenari di tipo Quick, Advanced e Patient Simulated, adattando i contenuti visualizzati.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 3.0
 */
@SuppressWarnings("ThisExpressionReferencesGlobalObjectJS")
// Sopprime l'avviso per l'uso di "this" in JavaScript inline.
@PageTitle("Dettagli Scenario")
@Route(value = "scenari", layout = MainLayout.class)
public class ScenarioDetailsView extends Composite<VerticalLayout> implements HasUrlParameter<String>, BeforeEnterObserver {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioDetailsView.class);

    /**
     * Servizio per la gestione delle operazioni di base sugli scenari.
     */
    private final ScenarioService scenarioService;

    /**
     * Servizio per la gestione dello storage dei file, utilizzato per caricare immagini e altri media.
     */
    private final FileStorageService fileStorageService;

    /**
     * Servizio per la gestione dei materiali necessari per lo scenario.
     */
    private final MaterialeService materialeNecessario;

    /**
     * Servizio per la gestione delle logiche specifiche degli scenari avanzati.
     */
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Servizio per la gestione delle logiche specifiche degli scenari simulati con paziente.
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;

    /**
     * Servizio per la gestione delle azioni chiave definite nello scenario.
     */
    private final AzioneChiaveService azioneChiaveService;

    /**
     * Servizio per la gestione degli esami e referti associati agli scenari.
     */
    private final EsameRefertoService esameRefertoService;

    /**
     * Servizio per la gestione dei dati relativi all'esame fisico.
     */
    private final EsameFisicoService esameFisicoService;

    /**
     * Servizio per la gestione dei dati del paziente al tempo zero (T0).
     */
    private final PazienteT0Service pazienteT0Service;

    /**
     * Servizio per la gestione dei presidi associati agli scenari.
     */
    private final PresidiService presidiService;
    private final ExternalApiService externalApiService;
    private final LabExamService labExamService;
    private final ExecutorService executorService;
    private final NotifierService notifierService;

    /**
     * L'ID dello scenario attualmente visualizzato in questa vista.
     */
    private Integer scenarioId;

    /**
     * L'oggetto {@link Scenario} completo attualmente visualizzato.
     */
    private Scenario scenario;

    /**
     * Componente {@link H2} che visualizza il titolo dello scenario.
     */
    private H2 titleDisplay;

    /**
     * Componente {@link Paragraph} che visualizza gli autori dello scenario.
     */
    private Paragraph authorsDisplay;

    /**
     * Campo di testo {@link TextField} per l'editing del titolo dello scenario.
     */
    private TextField titleEdit;

    /**
     * Campo di testo {@link TextField} per l'editing degli autori dello scenario.
     */
    private TextField authorsEdit;

    /**
     * Layout orizzontale che contiene i bottoni per salvare e annullare le modifiche al titolo e agli autori.
     */
    private HorizontalLayout editButtonsLayout;

    /**
     * Bottone per salvare le modifiche apportate al titolo e agli autori dello scenario.
     */
    private Button saveTitleAuthorsButton;

    /**
     * Bottone per annullare le modifiche apportate al titolo e agli autori dello scenario, ripristinando i valori originali.
     */
    private Button cancelTitleAuthorsButton;

    /**
     * Bottone per attivare la modalità di modifica del titolo e degli autori dello scenario.
     */
    private Button editTitleAuthorsButton;

    /**
     * Costruisce una nuova istanza di <code>ScenarioDetailsView</code>.
     * Inietta tutti i servizi necessari per recuperare e visualizzare i dati dello scenario.
     *
     * @param scenarioService                 Il servizio per la gestione degli scenari.
     * @param fileStorageService              Il servizio per la gestione dei file caricati.
     * @param materialeNecessario             Il servizio per la gestione del materiale necessario.
     * @param advancedScenarioService         Il servizio per la gestione degli scenari avanzati.
     * @param patientSimulatedScenarioService Il servizio per la gestione degli scenari simulati con paziente.
     * @param azionechiaveService             Il servizio per la gestione delle azioni chiave.
     * @param esameRefertoService             Il servizio per la gestione degli esami e referti.
     * @param esameFisicoService              Il servizio per la gestione degli esami fisici.
     * @param pazienteT0Service               Il servizio per la gestione dei pazienti T0.
     * @param presidiService                  Il servizio per la gestione dei presidi.
     */
    @Autowired
    public ScenarioDetailsView(ScenarioService scenarioService, FileStorageService fileStorageService,
                               MaterialeService materialeNecessario, AdvancedScenarioService advancedScenarioService,
                               PatientSimulatedScenarioService patientSimulatedScenarioService,
                               AzioneChiaveService azionechiaveService, EsameRefertoService esameRefertoService,
                               EsameFisicoService esameFisicoService, PazienteT0Service pazienteT0Service, PresidiService presidiService, ExternalApiService externalApiService, LabExamService labExamService, ExecutorService executorService, NotifierService notifierService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.materialeNecessario = materialeNecessario;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.azioneChiaveService = azionechiaveService;
        this.esameRefertoService = esameRefertoService;
        this.esameFisicoService = esameFisicoService;
        this.pazienteT0Service = pazienteT0Service;
        this.presidiService = presidiService;

        // Aggiunge una classe CSS specifica al layout principale del componente.
        getContent().addClassName("scenario-details-view");
        // Rimuove il padding predefinito dal layout principale.
        getContent().setPadding(false);
        this.externalApiService = externalApiService;
        this.labExamService = labExamService;
        this.executorService = executorService;
        this.notifierService = notifierService;
    }

    /**
     * Implementazione del metodo {@link HasUrlParameter#setParameter(BeforeEvent, Object)}.
     * Questo metodo è chiamato da Vaadin quando la vista viene navigata con un parametro URL.
     * È responsabile della validazione e dell'impostazione dell'ID dello scenario.
     *
     * @param event     L'evento di navigazione.
     * @param parameter Il parametro ID dello scenario passato nell'URL come {@link String}.
     * @throws NotFoundException Se il parametro è nullo, vuoto, non un numero valido, non positivo, o se lo scenario non esiste.
     */
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Il parametro ID scenario è nullo o vuoto.");
                throw new NumberFormatException("Il parametro ID scenario è nullo o vuoto.");
            }

            this.scenarioId = Integer.parseInt(parameter); // Converte il parametro in un Integer.
            if (scenarioId <= 0) {
                logger.warn("ID scenario non valido: {}. Deve essere un numero intero positivo.", scenarioId);
                throw new NumberFormatException("ID scenario deve essere un numero positivo.");
            }
            // Verifica l'esistenza dello scenario nel database.
            if (!scenarioService.existScenario(scenarioId)) {
                logger.warn("Tentativo di accesso a scenario non esistente con ID: {}.", scenarioId);
                throw new NotFoundException("Scenario con ID " + scenarioId + " non trovato.");
            }
            logger.info("Parametro ID scenario {} impostato con successo.", scenarioId);
        } catch (NumberFormatException e) {
            logger.error("Errore di formato per l'ID scenario ricevuto: '{}'. Dettagli: {}", parameter, e.getMessage());
            event.rerouteToError(NotFoundException.class, "ID scenario '" + parameter + "' non valido. " + e.getMessage());
        } catch (NotFoundException e) {
            logger.warn("Scenario non trovato durante l'impostazione del parametro: {}", e.getMessage());
            event.rerouteToError(NotFoundException.class, e.getMessage());
        }
    }

    /**
     * Implementazione del metodo {@link BeforeEnterObserver#beforeEnter(BeforeEnterEvent)}.
     * Questo metodo viene chiamato da Vaadin appena prima che la vista diventi attiva.
     * È il luogo ideale per caricare i dati dello scenario e inizializzare l'interfaccia utente basata su tali dati.
     *
     * @param event L'evento {@link BeforeEnterEvent} che precede l'ingresso nella vista.
     * @throws NotFoundException Se l'ID dello scenario non è stato impostato correttamente o se lo scenario non viene trovato nel database.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (scenarioId == null) {
            // Se scenarioId non è stato impostato (es. a causa di un errore precedente o navigazione errata).
            Notification.show("ID scenario non specificato. Impossibile caricare i dettagli.", 3000, Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("scenari"); // Reindirizza alla lista degli scenari.
            return;
        }
        // Carica l'oggetto Scenario completo dal servizio.
        this.scenario = scenarioService.getScenarioById(scenarioId);
        if (this.scenario == null) {
            logger.error("Scenario non trovato con ID: {} durante beforeEnter. Re indirizzamento a pagina di errore.", scenarioId);
            event.rerouteToError(NotFoundException.class, "Scenario con ID " + scenarioId + " non trovato. Impossibile visualizzare i dettagli.");
            return;
        }
        logger.info("Scenario con ID {} caricato con successo per la visualizzazione dettagliata.", scenarioId);
        initView(); // Inizializza i componenti dell'UI con i dati dello scenario.
    }

    /**
     * Inizializza i componenti dell'interfaccia utente della vista, popolandoli con i dati dello scenario.
     * Questo metodo viene chiamato dopo che lo scenario è stato caricato con successo.
     */
    private void initView() {
        // Pulisce il contenuto esistente per evitare duplicazioni in caso di re-inizializzazione.
        getContent().removeAll();

        // Configura il layout principale della vista.
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());
        AppHeader header = new AppHeader(fileStorageService);

        // Bottone "Indietro" per tornare alla lista degli scenari.
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna alla lista degli scenari");

        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari"));

        // Sezione dell'intestazione visuale per la vista.
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "DETTAGLI SCENARIO",
                "Visualizza e gestisci le informazioni dettagliate dello scenario selezionato.",
                VaadinIcon.INFO_CIRCLE.create(),
                "var(--lumo-primary-color)");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // Layout per il contenuto principale della vista.
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Contenitore per il titolo e gli autori dello scenario, con effetti visivi.
        Div titleContainer = new Div();
        titleContainer.setWidthFull();
        titleContainer.getStyle()
                .set("max-width", "800px")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Aggiunge effetti JavaScript per il mouseover/mouseout sul contenitore del titolo.
        titleContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        // Componenti per visualizzare il titolo e gli autori.
        titleDisplay = new H2(this.scenario.getTitolo());
        titleDisplay.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.FontSize.XXLARGE
        );
        titleDisplay.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px");

        Span boldAutori = new Span("Autori: ");
        boldAutori.getStyle().set("font-weight", "bold");
        Span authorsValue = new Span(this.scenario.getAutori());

        authorsDisplay = new Paragraph();
        authorsDisplay.add(boldAutori, authorsValue);
        authorsDisplay.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.FontSize.XLARGE
        );

        // Campi di testo per la modifica del titolo e degli autori (inizialmente nascosti).
        titleEdit = FieldGenerator.createTextField("Titolo", "Titolo dello scenario", true);
        titleEdit.setVisible(false);

        authorsEdit = FieldGenerator.createTextField("Autori", "Autori dello scenario", true);
        authorsEdit.setVisible(false);

        // Bottone per attivare la modalità di modifica.
        editTitleAuthorsButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editTitleAuthorsButton.setTooltipText("Modifica titolo e autori dello scenario.");
        editTitleAuthorsButton.getStyle().set("margin-left", "auto"); // Allinea a destra.

        HorizontalLayout editButtonContainer = new HorizontalLayout(editTitleAuthorsButton);
        editButtonContainer.setWidthFull();
        editButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Bottoni per salvare e annullare le modifiche (inizialmente nascosti).
        saveTitleAuthorsButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveTitleAuthorsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        saveTitleAuthorsButton.setVisible(false);

        cancelTitleAuthorsButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelTitleAuthorsButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        cancelTitleAuthorsButton.setVisible(false);

        editButtonsLayout = new HorizontalLayout(saveTitleAuthorsButton, cancelTitleAuthorsButton);
        editButtonsLayout.setSpacing(true);
        editButtonsLayout.setVisible(false);
        editButtonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Layout per la visualizzazione del titolo e degli autori.
        VerticalLayout displayLayout = new VerticalLayout(titleDisplay, authorsDisplay);
        displayLayout.setPadding(false);
        displayLayout.setSpacing(false);
        displayLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        displayLayout.setWidthFull();

        // Layout per la modifica del titolo e degli autori.
        VerticalLayout editLayout = new VerticalLayout(titleEdit, authorsEdit, editButtonsLayout);
        editLayout.setPadding(false);
        editLayout.setSpacing(true);
        editLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        editLayout.setWidthFull();
        editLayout.setVisible(false);

        titleContainer.add(displayLayout, editLayout, editButtonContainer);

        // Componente per le informazioni generali dello scenario (sottotitolo).
        Component subtitle = InfoSupport.getInfo(this.scenario, scenarioService);

        // Definizione delle schede (tabs) e dei loro contenuti.
        Tab tabInfoGenerali = createTabWithIcon("Informazioni Generali", VaadinIcon.INFO_CIRCLE);
        Tab tabStatoPaziente = createTabWithIcon("Stato Paziente", VaadinIcon.USER);
        Tab tabEsamiReferti = createTabWithIcon("Esami e Referti", VaadinIcon.CLIPBOARD_TEXT);

        // Creazione dei contenuti per ogni scheda.
        Component infoGeneraliContent = GeneralSupport.createOverviewContentWithData(
                this.scenario,
                scenarioService.isPediatric(scenarioId),
                this.scenario.getInfoGenitore(),
                scenarioService,
                materialeNecessario,
                azioneChiaveService
        );

        Component statoPazienteContent = PatientT0Support.createPatientContent(
                pazienteT0Service.getPazienteT0ById(scenarioId),
                esameFisicoService.getEsameFisicoById(scenarioId),
                scenarioId,
                esameFisicoService,
                pazienteT0Service,
                presidiService,
                advancedScenarioService
        );

        Component esamiRefertiContent = ExamSupport.createExamsContent(
                esameRefertoService,
                fileStorageService,
                scenarioId,
                scenario,
                externalApiService,
                labExamService,
                executorService,
                notifierService
        );

        // Componente EnhancedTabs per la navigazione tra le schede.
        EnhancedTabs enhancedTabs = new EnhancedTabs();
        enhancedTabs.setWidthFull();
        enhancedTabs.getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto");

        // Contenitore per i contenuti delle schede.
        Div tabsContainer = new Div();
        tabsContainer.setWidthFull();
        tabsContainer.getStyle()
                .set("max-width", "1000px")
                .set("margin", "0 auto")
                .set("overflow", "hidden")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("border-radius", "var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0 0");

        Div contentContainer = new Div();
        contentContainer.addClassName("tab-content");
        contentContainer.getStyle()
                .set("width", "100%")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "0 0 var(--lumo-border-radius-m) var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("transition", "opacity 0.3s ease-in-out"); // Aggiunge transizione per un effetto fade.

        // Mappa delle schede ai loro rispettivi contenuti.
        Map<Tab, Component> tabsToContent = new HashMap<>();
        tabsToContent.put(tabInfoGenerali, infoGeneraliContent);
        tabsToContent.put(tabStatoPaziente, statoPazienteContent);
        tabsToContent.put(tabEsamiReferti, esamiRefertiContent);

        // Aggiunge le schede iniziali.
        enhancedTabs.add(tabInfoGenerali, tabStatoPaziente, tabEsamiReferti);

        // Aggiunge la scheda "Timeline" se lo scenario è avanzato e ha tempi.
        List<Tempo> tempi = advancedScenarioService.getTempiByScenarioId(scenarioId);
        if (!tempi.isEmpty()) {
            Tab tabTimeline = createTabWithIcon("Timeline", VaadinIcon.CLOCK);
            Component timelineContent = TimesSupport.createTimelineContent(tempi, scenarioId, advancedScenarioService, scenarioService.isPediatric(scenarioId));
            tabsToContent.put(tabTimeline, timelineContent);
            enhancedTabs.add(tabTimeline);
            logger.debug("Scheda 'Timeline' aggiunta per lo scenario ID {}.", scenarioId);
        } else {
            logger.debug("Nessun tempo trovato per lo scenario ID {}. Scheda 'Timeline' non aggiunta.", scenarioId);
        }

        // Aggiunge la scheda "Sceneggiatura" se lo scenario è di tipo Patient Simulated.
        String scenarioType = scenarioService.getScenarioType(scenarioId);
        if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
            Tab tabSceneggiatura = createTabWithIcon("Sceneggiatura", VaadinIcon.FILE_TEXT);
            Component sceneggiaturaContent = SceneggiaturaSupport.createSceneggiaturaContent(
                    scenarioId,
                    patientSimulatedScenarioService.getSceneggiatura(scenarioId),
                    patientSimulatedScenarioService
            );
            tabsToContent.put(tabSceneggiatura, sceneggiaturaContent);
            enhancedTabs.add(tabSceneggiatura);
            logger.debug("Scheda 'Sceneggiatura' aggiunta per lo scenario ID {}.", scenarioId);
        } else {
            logger.debug("Lo scenario ID {} non è di tipo 'Patient Simulated Scenario'. Scheda 'Sceneggiatura' non aggiunta.", scenarioId);
        }

        // Imposta il contenuto iniziale e configura il listener per il cambio di scheda.
        contentContainer.add(infoGeneraliContent); // Mostra inizialmente la scheda "Informazioni Generali".
        enhancedTabs.addSelectedChangeListener(event -> {
            contentContainer.removeAll(); // Rimuove il contenuto precedente.
            Component selectedContent = tabsToContent.get(event.getSelectedTab());
            if (selectedContent != null) {
                contentContainer.add(selectedContent);
            }
            // Aggiunge un piccolo ritardo per l'effetto di transizione.
            contentContainer.getElement().executeJs(
                    "this.style.opacity = '0'; setTimeout(() => this.style.opacity = '1', 50);"
            );
            logger.debug("Scheda selezionata: '{}'. Contenuto aggiornato.", event.getSelectedTab().getLabel());
        });

        // Applica stili alle schede.
        enhancedTabs.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0 0")
                .set("margin", "0");

        tabsContainer.add(enhancedTabs, contentContainer); // Aggiunge le schede e il loro contenitore.
        contentLayout.add(headerSection, titleContainer, subtitle, tabsContainer); // Aggiunge le sezioni al layout del contenuto.

        // Footer layout (potrebbe essere vuoto o contenere bottoni di navigazione generici).
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Bottoni per lo scorrimento della pagina (Torna su, Scorri giù).
        Button scrollToTopButton = StyleApp.getScrollButton();
        Button scrollDownButton = StyleApp.getScrollDownButton();
        VerticalLayout scrollButtonContainer = new VerticalLayout(scrollToTopButton, scrollDownButton);
        mainLayout.add(scrollButtonContainer);


        // --- Listener per la modifica del titolo e degli autori ---

        // Quando il bottone "Modifica" viene cliccato.
        editTitleAuthorsButton.addClickListener(e -> {
            displayLayout.setVisible(false); // Nasconde il layout di visualizzazione.
            editTitleAuthorsButton.setVisible(false); // Nasconde il bottone "Modifica".
            editLayout.setVisible(true); // Mostra il layout di modifica.
            saveTitleAuthorsButton.setVisible(true); // Mostra il bottone "Salva".
            cancelTitleAuthorsButton.setVisible(true); // Mostra il bottone "Annulla".

            // Pre-popola i campi di modifica con i valori attuali.
            titleEdit.setValue(this.scenario.getTitolo());
            authorsEdit.setValue(this.scenario.getAutori());

            titleEdit.setVisible(true); // Rende visibili i campi di testo.
            authorsEdit.setVisible(true);
            editButtonsLayout.setVisible(true); // Rende visibile il layout dei bottoni di salvataggio/annullamento.
            logger.debug("Modalità di modifica titolo/autori attivata per lo scenario ID {}.", scenarioId);
        });

        // Quando il bottone "Annulla" viene cliccato.
        cancelTitleAuthorsButton.addClickListener(e -> {
            editLayout.setVisible(false); // Nasconde il layout di modifica.
            displayLayout.setVisible(true); // Mostra il layout di visualizzazione.
            editTitleAuthorsButton.setVisible(true); // Mostra il bottone "Modifica".
            logger.debug("Modifica titolo/autori annullata per lo scenario ID {}.", scenarioId);
        });

        // Quando il bottone "Salva" viene cliccato.
        saveTitleAuthorsButton.addClickListener(e -> {
            String newTitle = titleEdit.getValue();
            String newAuthors = authorsEdit.getValue();

            // Validazione dei campi.
            if (newTitle == null || newTitle.trim().isEmpty()) {
                Notification.show("Il titolo dello scenario non può essere vuoto.", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                titleEdit.focus(); // Ritorna il focus al campo titolo.
                logger.warn("Tentativo di salvare titolo vuoto per lo scenario ID {}.", scenarioId);
                return;
            }
            if (newAuthors == null || newAuthors.trim().isEmpty()) {
                Notification.show("Il campo autori non può essere vuoto.", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                authorsEdit.focus(); // Ritorna il focus al campo autori.
                logger.warn("Tentativo di salvare autori vuoti per lo scenario ID {}.", scenarioId);
                return;
            }

            try {
                // Aggiorna titolo e autori nel database tramite il servizio.
                scenarioService.updateScenarioTitleAndAuthors(scenarioId, newTitle, newAuthors);
                // Aggiorna l'oggetto Scenario locale con i nuovi valori.
                this.scenario.setTitolo(newTitle);
                this.scenario.setAutori(newAuthors);

                // Aggiorna i componenti di visualizzazione.
                titleDisplay.setText(newTitle);
                Span updatedBoldAutori = new Span("Autori: ");
                updatedBoldAutori.getStyle().set("font-weight", "bold");
                Span updatedAuthorsValue = new Span(newAuthors);
                authorsDisplay.removeAll(); // Rimuove il contenuto precedente.
                authorsDisplay.add(updatedBoldAutori, updatedAuthorsValue); // Aggiunge il nuovo contenuto.

                // Torna alla modalità di visualizzazione.
                editLayout.setVisible(false);
                displayLayout.setVisible(true);
                editTitleAuthorsButton.setVisible(true);

                Notification.show("Titolo e autori aggiornati con successo!", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                logger.info("Titolo e autori aggiornati con successo per lo scenario ID {}.", scenarioId);

            } catch (Exception ex) {
                logger.error("Errore critico durante l'aggiornamento di titolo e autori dello scenario ID {}: {}", scenarioId, ex.getMessage(), ex);
                Notification.show("Errore durante l'aggiornamento. Riprovare più tardi.", 3000, Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }
}