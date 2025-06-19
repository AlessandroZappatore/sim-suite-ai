package it.uniupo.simnova.views.creation;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.ZipExportService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.operations.ScenarioDeletionService;
import it.uniupo.simnova.service.scenario.operations.ScenarioImportService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.DialogSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.DynamicFileDownloader;

import static it.uniupo.simnova.views.ui.helper.support.SanitizedFileName.sanitizeFileName;

/**
 * Vista per la gestione e visualizzazione della lista degli scenari.
 * Permette di filtrare, cercare, esportare (PDF, ZIP) ed eliminare scenari.
 * Utilizza Vaadin per la creazione dell'interfaccia utente.
 *
 * @author Alessandro Zappatore
 * @version 1.2
 */
@SuppressWarnings({"ThisExpressionReferencesGlobalObjectJS", "JSCheckFunctionSignatures"})
@PageTitle("Lista Scenari")
@Route(value = "scenari", layout = MainLayout.class)
public class ScenariosListView extends Composite<VerticalLayout> {

    /**
     * Dimensione della pagina per la paginazione degli scenari.
     * Indica quanti scenari visualizzare per pagina.
     */
    private static final int PAGE_SIZE = 10;
    /**
     * Indica se la vista è stata distaccata dall'UI.
     * Utilizzato per evitare operazioni su UI distaccate.
     */
    public final AtomicBoolean detached;
    /**
     * Lunghezza massima per i campi di testo visualizzati nella griglia.
     * Utilizzati per limitare la visualizzazione di titoli, descrizioni, autori e patologie.
     */
    private final int MAX_TITLE_LENGTH = 20;
    /**
     * Lunghezza massima per la descrizione degli scenari.
     */
    private final int MAX_DESCRIPTION_LENGTH = 30;
    /**
     * Lunghezza massima per i nomi degli autori e delle patologie.
     * Utilizzati per limitare la visualizzazione di autori e patologie nella griglia.
     */
    private final int MAX_AUTHORS_NAME_LENGTH = 20;
    /**
     * Lunghezza massima per il campo patologia.
     * Utilizzato per limitare la visualizzazione della patologia nella griglia.
     */
    private final int MAX_PATHOLOGY_LENGTH = 20;

    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per importazione degli scenari da file ZIP.
     */
    private final ScenarioImportService scenarioImportService;
    /**
     * Servizi per le operazioni di esportazione e gestione degli scenari.
     */
    private final ZipExportService zipExportService;
    /**
     * Servizi per la gestione di componenti specifici degli scenari.
     */
    private final AzioneChiaveService azioneChiaveService;
    /**
     * Servizi per la gestione di pazienti ed esami fisici.
     */
    private final PazienteT0Service pazienteT0Service;
    /**
     * Servizi per la gestione di esami fisici e referti.
     */
    private final EsameFisicoService esameFisicoService;
    /**
     * Servizi per la gestione di esami e referti.
     */
    private final EsameRefertoService esameRefertoService;
    /**
     * Servizi per la gestione di scenari avanzati e simulati.
     */
    private final AdvancedScenarioService advancedScenarioService;
    /**
     * Servizio per la gestione di scenari simulati per pazienti.
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    /**
     * Servizio per la cancellazione degli scenari.
     */
    private final ScenarioDeletionService scenarioDeletionService;
    /**
     * Servizio per la gestione del materiale necessario per gli scenari.
     */
    private final MaterialeService materialeService;
    /**
     * Servizio per la gestione dei file, utilizzato per operazioni di caricamento e download.
     */
    private final FileStorageService fileStorageService;

    /**
     * Griglia per la visualizzazione degli scenari.
     */
    private final Grid<Scenario> scenariosGrid = new Grid<>();
    /**
     * ExecutorService per gestire operazioni asincrone e virtual threads.
     * Utilizzato per evitare blocchi dell'UI durante operazioni lunghe come l'esportazione.
     */
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    /**
     * Barra di progresso per indicare operazioni in corso.
     * Utilizzata per mostrare il caricamento dei dati o l'esportazione.
     */
    private ProgressBar progressBar;
    /**
     * Controlli di paginazione per la griglia degli scenari.
     * Permette di navigare tra le pagine di scenari visualizzati.
     */
    private HorizontalLayout paginationControls;
    /**
     * Pagina corrente della griglia degli scenari.
     * Utilizzata per gestire la paginazione e il caricamento dei dati.
     */
    private int currentPage = 0;
    /**
     * Informazioni sulla pagina corrente visualizzate nella paginazione.
     * Mostra il numero di pagina e il totale degli scenari.
     */
    private Span pageInfo;
    /**
     * Campo di ricerca per tipologia del paziente.
     */
    private ComboBox<String> searchPatientType;
    /**
     * Campo di ricerca per il titolo dello scenario.
     */
    private TextField searchTitolo;
    /**
     * Campo di ricerca per gli autori dello scenario.
     */
    private TextField searchAutori;
    /**
     * Campo di ricerca per il tipo di scenario.
     * Permette di filtrare gli scenari in base al loro tipo (es. Advanced, Patient Simulated, Quick).
     */
    private ComboBox<String> searchTipo;
    /**
     * Campo di ricerca per la patologia dello scenario.
     * Permette di filtrare gli scenari in base alla patologia associata.
     */
    private TextField searchPatologia;
    /**
     * Bottone per resettare i filtri di ricerca applicati.
     * Permette di tornare alla visualizzazione completa degli scenari.
     */
    private Button resetButton;
    /**
     * Lista di tutti gli scenari caricati.
     * Utilizzata per applicare i filtri e popolare la griglia.
     */
    private List<Scenario> allScenarios = new ArrayList<>();
    /**
     * Lista di scenari filtrati in base ai criteri di ricerca.
     * Utilizzata per visualizzare solo gli scenari che soddisfano i filtri applicati.
     */
    private List<Scenario> filteredScenarios = new ArrayList<>();
    /**
     * Logger per registrare eventi e errori nella vista.
     */
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ScenariosListView.class);

    /**
     * Costruttore privato per evitare la creazione di istanze senza iniezione dei servizi.
     *
     * @param scenarioService                 servizio per la gestione degli scenari
     * @param zipExportService                servizio per l'esportazione in formato ZIP
     * @param fileStorageService              servizio per la gestione dei file
     * @param scenarioImportService           servizio per l'importazione degli scenari
     * @param azioneChiaveService             servizio per la gestione delle azioni chiave
     * @param pazienteT0Service               servizio per la gestione dei pazienti T0
     * @param esameFisicoService              servizio per la gestione degli esami fisici
     * @param esameRefertoService             servizio per la gestione degli esami e referti
     * @param advancedScenarioService         servizio per la gestione degli scenari avanzati
     * @param patientSimulatedScenarioService servizio per la gestione degli scenari simulati per pazienti
     * @param scenarioDeletionService         servizio per la cancellazione degli scenari
     * @param materialeService                servizio per la gestione del materiale necessario
     */
    @Autowired
    public ScenariosListView(ScenarioService scenarioService,
                             ZipExportService zipExportService,
                             FileStorageService fileStorageService,
                             ScenarioImportService scenarioImportService,
                             AzioneChiaveService azioneChiaveService,
                             PazienteT0Service pazienteT0Service,
                             EsameFisicoService esameFisicoService,
                             EsameRefertoService esameRefertoService,
                             AdvancedScenarioService advancedScenarioService,
                             PatientSimulatedScenarioService patientSimulatedScenarioService,
                             ScenarioDeletionService scenarioDeletionService,
                             MaterialeService materialeService) {
        this.scenarioService = scenarioService;
        this.zipExportService = zipExportService;
        this.fileStorageService = fileStorageService;
        this.scenarioImportService = scenarioImportService;
        this.azioneChiaveService = azioneChiaveService;
        this.pazienteT0Service = pazienteT0Service;
        this.esameFisicoService = esameFisicoService;
        this.esameRefertoService = esameRefertoService;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.scenarioDeletionService = scenarioDeletionService;
        this.materialeService = materialeService;
        this.detached = new AtomicBoolean(false);
        initView();
    }

    /**
     * Eseguito quando la vista viene collegata all'interfaccia utente.
     * Questo è il punto corretto per avviare operazioni di caricamento dati asincrone.
     *
     * @param attachEvent l'evento di collegamento
     */
    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        loadData();
    }
    /**
     * Eseguito quando la vista viene distaccata dall'UI.
     * Imposta il flag {@code detached} su true e spegne l'ExecutorService.
     *
     * @param detachEvent L'evento di distacco.
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detached.set(true);
        executorService.shutdownNow();
    }

    /**
     * Inizializza la struttura e i componenti principali della vista.
     */
    private void initView() {
        // Layout principale gestito da StyleApp, non usato direttamente dopo l'inizializzazione
        @SuppressWarnings("unused") VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna alla pagina di creazione");

        Button newScenarioButton = StyleApp.getButton("Nuovo scenario", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "--lumo-primary-color");
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        customHeader.add(newScenarioButton);

        configureSearchFilters();
        HorizontalLayout filterLayout = new HorizontalLayout(searchPatientType, searchTitolo, searchTipo, searchAutori, searchPatologia, resetButton);
        filterLayout.setWidthFull();
        filterLayout.setPadding(true);
        filterLayout.setSpacing(true);
        filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        configureGrid();
        configurePagination();

        progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        contentLayout.add(progressBar, filterLayout, scenariosGrid, paginationControls);

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);

        getContent().add(customHeader, contentLayout, footerLayout);

        // Listener per i bottoni di navigazione e creazione
        backButton.addClickListener(e -> {
            if (!detached.get()) {
                getUI().ifPresent(ui -> ui.navigate(""));
            }
        });

        newScenarioButton.addClickListener(e -> {
            if (!detached.get()) {
                DialogSupport.showZipUploadDialog(detached, executorService, scenarioImportService, this::loadData);
            }
        });
    }

    /**
     * Configura i campi di input per la ricerca e il filtraggio degli scenari.
     */
    private void configureSearchFilters() {
        List<String> allPatientTypes = List.of("Tutti", "Adulto", "Pediatrico", "Neonatale", "Prematuro");
        searchPatientType = FieldGenerator.createComboBox(
                "Filtra per Tipo Paziente", allPatientTypes, "Tutti", false);
        searchPatientType.setClearButtonVisible(true);
        searchPatientType.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchTitolo = FieldGenerator.createTextField("Filtra per Titolo", "Cerca titolo...", false);
        searchTitolo.setClearButtonVisible(true);
        searchTitolo.setValueChangeMode(ValueChangeMode.LAZY);
        searchTitolo.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchAutori = FieldGenerator.createTextField("Filtra per Autori", "Cerca autori...", false);
        searchAutori.setClearButtonVisible(true);
        searchAutori.setValueChangeMode(ValueChangeMode.LAZY);
        searchAutori.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        List<String> allTypes = List.of("Tutti", "Patient Simulated Scenario", "Advanced Scenario", "Quick Scenario");
        searchTipo = FieldGenerator.createComboBox("Filtra per Tipo", allTypes, "Tutti", false);
        searchTipo.setClearButtonVisible(true);
        searchTipo.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        searchPatologia = FieldGenerator.createTextField("Filtra per Patologia", "Cerca patologia...", false);
        searchPatologia.setClearButtonVisible(true);
        searchPatologia.setValueChangeMode(ValueChangeMode.LAZY);
        searchPatologia.addValueChangeListener(e -> applyFiltersAndRefreshGrid());

        resetButton = new Button("Reset Filtri", new Icon(VaadinIcon.CLOSE_CIRCLE_O));
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.getStyle().set("margin-top", "auto");
        resetButton.addClickListener(e -> {
            searchPatientType.setValue("Tutti");
            searchTitolo.clear();
            searchAutori.clear();
            searchTipo.setValue("Tutti");
            searchPatologia.clear();
            applyFiltersAndRefreshGrid();
        });
    }

    /**
     * Applica i filtri correnti alla lista degli scenari e aggiorna la griglia e la paginazione.
     */
    private void applyFiltersAndRefreshGrid() {
        if (detached.get()) {
            return;
        }

        String tipologiaPatientFilter = searchPatientType.getValue() != null ? searchPatientType.getValue() : "Tutti";
        String titoloFilter = searchTitolo.getValue().trim().toLowerCase();
        String autoriFilter = searchAutori.getValue().trim().toLowerCase();
        String tipoFilter = searchTipo.getValue() != null ? searchTipo.getValue() : "Tutti";
        String patologiaFilter = searchPatologia.getValue().trim().toLowerCase();

        filteredScenarios = allScenarios.stream()
                .filter(scenario -> "Tutti".equals(tipologiaPatientFilter) ||
                        (scenario.getTipologia() != null && scenario.getTipologia().equalsIgnoreCase(tipologiaPatientFilter)))
                .filter(scenario -> titoloFilter.isEmpty() ||
                        (scenario.getTitolo() != null && scenario.getTitolo().toLowerCase().contains(titoloFilter)))
                .filter(scenario -> autoriFilter.isEmpty() ||
                        (scenario.getAutori() != null && scenario.getAutori().toLowerCase().contains(autoriFilter)))
                .filter(scenario -> "Tutti".equals(tipoFilter) ||
                        (scenarioService.getScenarioType(scenario.getId()) != null && scenarioService.getScenarioType(scenario.getId()).equalsIgnoreCase(tipoFilter)))
                .filter(scenario -> patologiaFilter.isEmpty() ||
                        (scenario.getPatologia() != null && scenario.getPatologia().toLowerCase().contains(patologiaFilter)))
                .collect(Collectors.toList());

        currentPage = 0;
        updateGridItems();
        updatePaginationInfo();
    }

    /**
     * Configura le colonne e le proprietà della tabella (Grid) degli scenari.
     */
    private void configureGrid() {
        scenariosGrid.setWidthFull();
        scenariosGrid.addClassName(LumoUtility.BorderRadius.MEDIUM);
        scenariosGrid.addClassName(LumoUtility.BoxShadow.SMALL);
        scenariosGrid.getStyle().set("min-height", "400px");

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String patientType = scenario.getTipologia() != null ? scenario.getTipologia() : "Unknown";
                    Span span;
                    Icon icon;
                    String title;
                    String colorClass;

                    switch (patientType) {
                        case "Adulto":
                            icon = FontAwesome.Solid.USER.create();
                            title = "Paziente adulto";
                            colorClass = LumoUtility.TextColor.PRIMARY;
                            break;
                        case "Pediatrico":
                            icon = FontAwesome.Solid.CHILD.create();
                            title = "Paziente pediatrico";
                            colorClass = LumoUtility.TextColor.SUCCESS;
                            break;
                        case "Neonatale":
                            icon = FontAwesome.Solid.BABY.create();
                            title = "Paziente neonatale";
                            colorClass = LumoUtility.TextColor.WARNING;
                            break;
                        case "Prematuro":
                            icon = FontAwesome.Solid.HANDS_HOLDING_CHILD.create();
                            title = "Paziente prematuro";
                            colorClass = LumoUtility.TextColor.ERROR;
                            break;
                        default:
                            icon = FontAwesome.Solid.INFO_CIRCLE.create();
                            title = "Tipo paziente non specificato";
                            colorClass = LumoUtility.TextColor.TERTIARY;
                            break;
                    }

                    span = new Span(icon);
                    span.addClassName(colorClass);
                    span.getElement().setAttribute("title", title);
                    icon.setSize("24px");
                    return span;
                })).setHeader("Tipo Paziente")
                .setFlexGrow(0)
                .setComparator(Comparator.comparing(Scenario::getTipologia, Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String titolo = scenario.getTitolo() != null ? scenario.getTitolo() : "";
                    String displayTitolo = titolo.length() > MAX_TITLE_LENGTH ? titolo.substring(0, MAX_TITLE_LENGTH) + "..." : titolo;
                    Span titoloSpan = new Span(displayTitolo);
                    if (!titolo.isEmpty()) {
                        titoloSpan.getElement().setAttribute("title", titolo);
                    }
                    return titoloSpan;
                })).setHeader("Titolo")
                .setSortable(true)
                .setFlexGrow(1)
                .setComparator(Comparator.comparing(Scenario::getTitolo, Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String tipo = scenarioService.getScenarioType(scenario.getId());
                    tipo = tipo != null ? tipo : "N/D";

                    HorizontalLayout container = new HorizontalLayout();
                    container.setSpacing(true);
                    container.setPadding(false);
                    container.setAlignItems(FlexComponent.Alignment.CENTER);

                    Icon icon;
                    Span tipoSpan = new Span(tipo);
                    String iconColorClass = LumoUtility.TextColor.SECONDARY;
                    String fontWeightClass = LumoUtility.FontWeight.NORMAL;

                    switch (tipo.toLowerCase()) {
                        case "patient simulated scenario":
                            icon = FontAwesome.Solid.USER_INJURED.create();
                            icon.setSize("24px");
                            iconColorClass = LumoUtility.TextColor.ERROR;
                            fontWeightClass = LumoUtility.FontWeight.SEMIBOLD;
                            tipoSpan.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        case "advanced scenario":
                            icon = VaadinIcon.CLOCK.create();
                            icon.setSize("20px");
                            iconColorClass = LumoUtility.TextColor.SUCCESS;
                            fontWeightClass = LumoUtility.FontWeight.SEMIBOLD;
                            tipoSpan.addClassNames(LumoUtility.TextColor.SUCCESS, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        case "quick scenario":
                            icon = VaadinIcon.BOLT.create();
                            icon.setSize("20px");
                            iconColorClass = LumoUtility.TextColor.PRIMARY;
                            fontWeightClass = LumoUtility.FontWeight.SEMIBOLD;
                            tipoSpan.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.SEMIBOLD);
                            break;
                        default:
                            icon = VaadinIcon.QUESTION.create();
                            tipoSpan.addClassName(LumoUtility.TextColor.SECONDARY);
                            break;
                    }

                    icon.addClassName(iconColorClass);

                    // Aggiunge la classe del fontWeight solo se non è già presente
                    if (!tipoSpan.getClassNames().contains(LumoUtility.FontWeight.SEMIBOLD) &&
                            !tipoSpan.getClassNames().contains(LumoUtility.FontWeight.NORMAL) &&
                            !fontWeightClass.equals(LumoUtility.FontWeight.NORMAL)) {
                        tipoSpan.addClassName(fontWeightClass);
                    }

                    container.add(icon, tipoSpan);
                    return container;
                })).setHeader("Tipo")
                .setSortable(true)
                .setFlexGrow(2)
                .setComparator(Comparator.comparing(s -> scenarioService.getScenarioType(s.getId()), Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String autori = scenario.getAutori() != null ? scenario.getAutori() : "";
                    String displayAutori = autori.length() > MAX_AUTHORS_NAME_LENGTH ? autori.substring(0, MAX_AUTHORS_NAME_LENGTH) + "..." : autori;
                    Span autoriSpan = new Span(displayAutori);
                    if (!autori.isEmpty()) {
                        autoriSpan.getElement().setAttribute("title", autori);
                    }
                    return autoriSpan;
                })).setHeader("Autori")
                .setSortable(true)
                .setFlexGrow(1)
                .setComparator(Comparator.comparing(Scenario::getAutori, Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String patologia = scenario.getPatologia() != null ? scenario.getPatologia() : "";
                    String displayPatologia = patologia.length() > MAX_PATHOLOGY_LENGTH ? patologia.substring(0, MAX_PATHOLOGY_LENGTH) + "..." : patologia;
                    Span patoSpan = new Span(displayPatologia);
                    if (!patologia.isEmpty()) {
                        patoSpan.getElement().setAttribute("title", patologia);
                    }
                    return patoSpan;
                })).setHeader("Patologia")
                .setSortable(true)
                .setFlexGrow(1)
                .setComparator(Comparator.comparing(Scenario::getPatologia, Comparator.nullsLast(String::compareToIgnoreCase)));

        scenariosGrid.addColumn(new ComponentRenderer<>(scenario -> {
                    String descrizione = scenario.getDescrizione() != null ? scenario.getDescrizione() : "";
                    Div container = new Div();
                    container.setWidthFull();
                    container.getStyle()
                            .set("max-width", "200px")
                            .set("overflow", "hidden")
                            .set("text-overflow", "ellipsis")
                            .set("white-space", "nowrap");
                    if (descrizione.isEmpty()) {
                        return container;
                    }
                    String plainText = Jsoup.parse(descrizione).text(); // Rimuove i tag HTML
                    container.setText(plainText.length() > MAX_DESCRIPTION_LENGTH ? plainText.substring(0, MAX_DESCRIPTION_LENGTH) + "..." : plainText);
                    container.getElement().setAttribute("title", plainText);
                    return container;
                })).setHeader("Descrizione")
                .setFlexGrow(1);

        // Colonna Azioni (PDF, ZIP, Elimina)
        scenariosGrid.addComponentColumn(scenario -> {
                    HorizontalLayout actions = new HorizontalLayout();
                    actions.setSpacing(false);
                    actions.setMargin(false);
                    actions.setPadding(false);
                    actions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

                    Button pdfButton = new Button(FontAwesome.Regular.FILE_PDF.create());
                    pdfButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
                    pdfButton.getElement().setAttribute("title", "Esporta in PDF");
                    pdfButton.getStyle().set("margin-right", "var(--lumo-space-xs)");
                    pdfButton.addClickListener(e -> {
                        if (!detached.get()) {
                            exportToPdf(scenario);
                        }
                    });

                    Button simButton = new Button(FontAwesome.Solid.FILE_EXPORT.create());
                    simButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
                    simButton.getElement().setAttribute("title", "Esporta in .zip (sim.execution)");
                    simButton.getStyle().set("margin-right", "var(--lumo-space-xs)");
                    simButton.addClickListener(e -> {
                        if (!detached.get()) {
                            exportToSimExecution(scenario);
                        }
                    });

                    Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
                    deleteButton.getElement().setAttribute("title", "Elimina scenario");
                    deleteButton.addClickListener(e -> {
                        if (!detached.get()) {
                            confirmAndDeleteScenario(scenario);
                        }
                    });

                    actions.add(pdfButton, simButton, deleteButton);
                    return actions;
                }).setHeader("Azioni")
                .setFlexGrow(0)
                .setWidth("120px");

        // Listener per il click su una riga della griglia per navigare ai dettagli dello scenario
        scenariosGrid.addItemClickListener(event -> {
            if (!detached.get() && event.getItem() != null) {
                getUI().ifPresent(ui -> ui.navigate("scenari/" + event.getItem().getId()));
            }
        });
    }

    /**
     * Configura i controlli di paginazione per la griglia.
     */
    private void configurePagination() {
        paginationControls = new HorizontalLayout();
        paginationControls.setWidthFull();
        paginationControls.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        paginationControls.setAlignItems(FlexComponent.Alignment.CENTER);
        paginationControls.setSpacing(true);
        paginationControls.setPadding(true);

        Button firstPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
        Button prevPageButton = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        pageInfo = new Span("Pagina -");
        Button nextPageButton = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        Button lastPageButton = new Button(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));

        firstPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        prevPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        nextPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        lastPageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);

        pageInfo.getStyle().set("margin", "0 var(--lumo-space-s)");

        // Listener per i bottoni di paginazione
        firstPageButton.addClickListener(e -> {
            if (currentPage > 0 && !detached.get()) {
                currentPage = 0;
                updateGridItems();
                updatePaginationInfo();
            }
        });
        prevPageButton.addClickListener(e -> {
            if (currentPage > 0 && !detached.get()) {
                currentPage--;
                updateGridItems();
                updatePaginationInfo();
            }
        });
        nextPageButton.addClickListener(e -> {
            if (!detached.get()) {
                int totalPages = (int) Math.ceil((double) filteredScenarios.size() / PAGE_SIZE);
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });
        lastPageButton.addClickListener(e -> {
            if (!detached.get()) {
                int totalPages = Math.max(1, (int) Math.ceil((double) filteredScenarios.size() / PAGE_SIZE));
                if (currentPage < totalPages - 1) {
                    currentPage = totalPages - 1;
                    updateGridItems();
                    updatePaginationInfo();
                }
            }
        });

        paginationControls.add(firstPageButton, prevPageButton, pageInfo, nextPageButton, lastPageButton);
        paginationControls.setVisible(false);
    }

    /**
     * Aggiorna le informazioni di paginazione visualizzate.
     */
    private void updatePaginationInfo() {
        if (detached.get()) {
            return;
        }
        int totalItems = filteredScenarios.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));

        if (totalItems == 0) {
            pageInfo.setText("Nessun risultato");
        } else {
            pageInfo.setText("Pagina " + (currentPage + 1) + " di " + totalPages);
        }

        getUI().ifPresent(ui -> ui.access(() -> {
            boolean hasPrev = currentPage > 0;
            boolean hasNext = currentPage < totalPages - 1;
            ((Button) paginationControls.getComponentAt(0)).setEnabled(hasPrev);
            ((Button) paginationControls.getComponentAt(1)).setEnabled(hasPrev);
            ((Button) paginationControls.getComponentAt(3)).setEnabled(hasNext);
            ((Button) paginationControls.getComponentAt(4)).setEnabled(hasNext);
            paginationControls.setVisible(true);
        }));
    }

    /**
     * Aggiorna gli elementi visualizzati nella griglia in base alla pagina corrente.
     */
    private void updateGridItems() {
        if (detached.get()) {
            return;
        }
        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredScenarios.size());

        if (fromIndex < filteredScenarios.size() && fromIndex < toIndex) {
            scenariosGrid.setItems(new ListDataProvider<>(filteredScenarios.subList(fromIndex, toIndex)));
        } else {
            scenariosGrid.setItems(new ArrayList<>());
        }
        getUI().ifPresent(ui -> ui.access(() -> scenariosGrid.setVisible(true)));
    }

    /**
     * Gestisce l'esportazione di uno scenario in formato PDF.
     * Mostra un dialog per selezionare gli elementi da includere nel PDF.
     *
     * @param scenario Lo scenario da esportare.
     */
    private void exportToPdf(Scenario scenario) {
        if (detached.get() || scenario == null) {
            return;
        }
        String scenarioType = scenarioService.getScenarioType(scenario.getId());

        // Se lo scenario non è avanzato o simulato, esporta tutto direttamente
        if (!"Advanced Scenario".equals(scenarioType) && !"Patient Simulated Scenario".equals(scenarioType)) {
            executeExport(scenario, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, zipExportService);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.setHeaderTitle("Seleziona elementi per il PDF");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        // Checkbox per la selezione degli elementi del PDF
        Checkbox descChk = new Checkbox("Descrizione", true);
        Checkbox briefingChk = new Checkbox("Briefing", true);
        Checkbox infoGenChk = new Checkbox("Informazioni dai genitori", true);
        Checkbox pattoChk = new Checkbox("Patto d'aula", true);
        Checkbox azioniChk = new Checkbox("Azioni chiave", true);
        Checkbox obiettiviChk = new Checkbox("Obiettivi didattici", true);
        Checkbox moulageChk = new Checkbox("Moulage", true);
        Checkbox liquidiChk = new Checkbox("Liquidi e farmaci in T0", true);
        Checkbox matNecChk = new Checkbox("Materiale necessario", true);
        Checkbox paramChk = new Checkbox("Parametri vitali", true);
        Checkbox accessiChk = new Checkbox("Accessi", true);
        Checkbox esameFisicoChk = new Checkbox("Esame fisico", true);
        Checkbox esamiERefertiChk = new Checkbox("Esami e referti", true);
        Checkbox timelineChk = new Checkbox("Timeline", true);

        // Disabilita i checkbox se i dati correlati non sono presenti nello scenario
        var scenarioNew = scenarioService.getScenarioById(scenario.getId());

        if (scenarioNew.getDescrizione() == null || scenarioNew.getDescrizione().isEmpty()) descChk.setEnabled(false);
        if (scenarioNew.getBriefing() == null || scenarioNew.getBriefing().isEmpty()) briefingChk.setEnabled(false);
        if (scenarioNew.getPatologia() == null || scenarioNew.getPatologia().isEmpty()) pattoChk.setEnabled(false);
        if (scenarioNew.getObiettivo() == null || scenarioNew.getObiettivo().isEmpty()) obiettiviChk.setEnabled(false);
        if (scenarioNew.getInfoGenitore() == null || scenarioNew.getInfoGenitore().isEmpty())
            infoGenChk.setEnabled(false);
        if (scenarioNew.getPattoAula() == null || scenarioNew.getPattoAula().isEmpty()) pattoChk.setEnabled(false);
        if (scenarioNew.getMoulage() == null || scenarioNew.getMoulage().isEmpty()) moulageChk.setEnabled(false);
        if (scenarioNew.getLiquidi() == null || scenarioNew.getLiquidi().isEmpty()) liquidiChk.setEnabled(false);
        if (azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioNew.getId()) == null || azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioNew.getId()).isEmpty()) {
            azioniChk.setEnabled(false);
        }

        var esameFisico = esameFisicoService.getEsameFisicoById(scenario.getId());
        if (esameFisico == null || esameFisico.getSections().values().stream().allMatch(value -> value == null || value.trim().isEmpty())) {
            esameFisicoChk.setEnabled(false);
            esameFisicoChk.setValue(false);
        }

        var pazienteT0 = pazienteT0Service.getPazienteT0ById(scenario.getId());
        if (pazienteT0 == null) {
            paramChk.setEnabled(false);
            paramChk.setValue(false);
            accessiChk.setEnabled(false);
            accessiChk.setValue(false);
        } else {
            boolean hasAccessi = (pazienteT0.getAccessiVenosi() != null && !pazienteT0.getAccessiVenosi().isEmpty()) ||
                    (pazienteT0.getAccessiArteriosi() != null && !pazienteT0.getAccessiArteriosi().isEmpty());
            if (!hasAccessi) {
                accessiChk.setEnabled(false);
                accessiChk.setValue(false);
            }
        }
        if (esameRefertoService.getEsamiRefertiByScenarioId(scenario.getId()) == null || esameRefertoService.getEsamiRefertiByScenarioId(scenario.getId()).isEmpty()) {
            esamiERefertiChk.setEnabled(false);
        }

        if (advancedScenarioService.getTempiByScenarioId(scenario.getId()) == null || advancedScenarioService.getTempiByScenarioId(scenario.getId()).isEmpty()) {
            timelineChk.setEnabled(false);
        }

        if (materialeService.getMaterialiByScenarioId(scenario.getId()) == null || materialeService.getMaterialiByScenarioId(scenario.getId()).isEmpty()) {
            matNecChk.setEnabled(false);
        }

        layout.add(descChk, briefingChk, infoGenChk, pattoChk, azioniChk, obiettiviChk,
                moulageChk, liquidiChk, matNecChk, paramChk, accessiChk, esameFisicoChk,
                esamiERefertiChk, timelineChk);

        // Aggiunge checkbox "Sceneggiatura" solo per scenari di tipo "Patient Simulated Scenario"
        Checkbox sceneggiaturaChk = new Checkbox("Sceneggiatura", true);
        if ("Patient Simulated Scenario".equals(scenarioType)) {
            if (patientSimulatedScenarioService.getPatientSimulatedScenarioById(scenario.getId()).getSceneggiatura() == null ||
                    patientSimulatedScenarioService.getPatientSimulatedScenarioById(scenario.getId()).getSceneggiatura().isEmpty()) {
                sceneggiaturaChk.setEnabled(false);
                sceneggiaturaChk.setValue(false);
            }
            layout.add(sceneggiaturaChk);
        }
        dialog.add(layout);

        Button confirmButton = new Button("Genera PDF", e -> {
            dialog.close();
            executeExport(scenario,
                    descChk.getValue(), pattoChk.getValue(), infoGenChk.getValue(),
                    pattoChk.getValue(), azioniChk.getValue(), obiettiviChk.getValue(),
                    moulageChk.getValue(), liquidiChk.getValue(), matNecChk.getValue(),
                    paramChk.getValue(), accessiChk.getValue(), esameFisicoChk.getValue(),
                    esamiERefertiChk.getValue(), timelineChk.getValue(), sceneggiaturaChk.getValue(), zipExportService);
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Annulla", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(cancelButton, confirmButton);

        dialog.open();
    }

    /**
     * Esegue l'esportazione di uno scenario in formato PDF con le opzioni selezionate.
     *
     * @param scenario         Lo scenario da esportare.
     * @param desc             True se includere la descrizione, false altrimenti.
     * @param brief            True se includere il briefing, false altrimenti.
     * @param infoGen          True se includere le informazioni dai genitori, false altrimenti.
     * @param patto            True se includere il patto d'aula, false altrimenti.
     * @param azioni           True se includere le azioni chiave, false altrimenti.
     * @param obiettivi        True se includere gli obiettivi didattici, false altrimenti.
     * @param moula            True se includere il moulage, false altrimenti.
     * @param liquidi            True se includere i liquidi e farmaci in T0, false altrimenti.
     * @param matNec           True se includere il materiale necessario, false altrimenti.
     * @param param            True se includere i parametri vitali, false altrimenti.
     * @param acces            True se includere gli accessi, false altrimenti.
     * @param fisic            True se includere l'esame fisico, false altrimenti.
     * @param esam             True se includere gli esami e referti, false altrimenti.
     * @param time             True se includere la timeline, false altrimenti.
     * @param scen             True se includere la sceneggiatura, false altrimenti.
     * @param zipExportService Il servizio per l'esportazione in ZIP.
     */
    private void executeExport(Scenario scenario, boolean desc, boolean brief, boolean infoGen,
                               boolean patto, boolean azioni, boolean obiettivi, boolean moula, boolean liquidi,
                               boolean matNec, boolean param, boolean acces, boolean fisic,
                               boolean esam, boolean time, boolean scen,
                               ZipExportService zipExportService) {
        Notification.show("Generazione del PDF...", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        logger.info("Inizio generazione PDF per lo scenario {}", scenario.getId());
        try {
            String fileName = "Pdf_scenario_" + limitLength(sanitizeFileName(scenario.getTitolo())) + ".zip";

            DynamicFileDownloader downloader = new DynamicFileDownloader(
                    "Clicca qui per scaricare: " + fileName,
                    fileName,
                    outputStream -> {
                        try {
                            logger.info("Generazione PDF per lo scenario {}", scenario.getId());
                            byte[] pdfBytes = zipExportService.exportScenarioPdfToZip(
                                    scenario.getId(), desc, brief, infoGen, patto,
                                    azioni, obiettivi, moula, liquidi, matNec, param, acces,
                                    fisic, esam, time, scen);
                            logger.info("PDF generato con successo per lo scenario {}", scenario.getId());
                            if (pdfBytes == null || pdfBytes.length == 0) {
                                logger.error("Generato lo scenario {}", scenario.getId());
                                throw new RuntimeException("Generated PDF bytes are null or empty.");
                            }
                            outputStream.write(pdfBytes);
                            logger.info("File ZIP generato con successo per lo scenario {}", scenario.getId());
                        } catch (IOException | RuntimeException e) {
                            logger.error("Errore durante la generazione del file ZIP per lo scenario {}: {}", scenario.getId(), e.getMessage(), e);
                            throw new RuntimeException("Errore durante la generazione del file ZIP: " + e.getMessage(), e);
                        }
                    }
            );

            downloader.withContentTypeGenerator(() -> "application/zip");

            downloader.addDownloadStartedListener(event -> {
                logger.info("Download iniziato per lo scenario {}", scenario.getId());
                getUI().ifPresent(ui -> ui.access(() -> Notification.show("Download iniziato...", 2000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS)));
            });

            downloader.addDownloadFailedListener(event -> {
                logger.error("Download fallito per lo scenario {}: {}", scenario.getId(), event.getException().getMessage(), event.getException());
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (downloader.getParent().isPresent()) {
                        ScenariosListView.this.getContent().remove(downloader);
                    }
                    Notification.show("Download fallito: " + event.getException().getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
            });

            downloader.addDownloadFinishedListener(event -> {
                logger.info("Download completato per lo scenario {}", scenario.getId());
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (downloader.getParent().isPresent()) {
                        ScenariosListView.this.getContent().remove(downloader);
                    }
                    Notification.show("Download completato.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }));
            });

            ScenariosListView.this.getContent().add(downloader);
            getUI().ifPresent(ui -> {
                if (!ui.isClosing()) {
                    ui.access(() -> downloader.getElement().callJsFunction("click"));
                }
            });
        } catch (Exception e) {
            logger.error("Errore durante la preparazione del PDF per lo scenario {}: {}", scenario.getId(), e.getMessage(), e);
            handleExportError("Errore preparazione PDF", e);
        }
    }

    /**
     * Gestisce l'esportazione di uno scenario in formato ZIP per l'esecuzione.
     *
     * @param scenario Lo scenario da esportare.
     */
    private void exportToSimExecution(Scenario scenario) {
        if (detached.get() || scenario == null) {
            return;
        }
        Notification.show("Generazione dell'archivio ZIP...", 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        try {
            String fileName = "Execution_scenario_" + limitLength(sanitizeFileName(scenario.getTitolo())) + ".zip";
            DynamicFileDownloader downloader = new DynamicFileDownloader(
                    "Clicca qui per scaricare: " + fileName,
                    fileName,
                    outputStream -> {
                        try {
                            byte[] zipBytes = zipExportService.exportScenarioToZip(scenario.getId());
                            outputStream.write(zipBytes);
                        } catch (IOException | RuntimeException e) {
                            throw new RuntimeException("Errore durante la generazione dello ZIP: " + e.getMessage(), e);
                        }
                    }
            ).withContentTypeGenerator(() -> "application/zip");

            downloader.addDownloadFinishedListener(event -> getUI().ifPresent(ui -> ui.access(() -> {
                if (downloader.getParent().isPresent()) {
                    ScenariosListView.this.getContent().remove(downloader);
                }
                Notification.show("Download ZIP completato.", 2000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            })));
            downloader.addDownloadFailedListener(event -> getUI().ifPresent(ui -> ui.access(() -> {
                if (downloader.getParent().isPresent()) {
                    ScenariosListView.this.getContent().remove(downloader);
                }
                Notification.show("Download ZIP fallito: " + event.getException().getMessage(), 4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            })));

            ScenariosListView.this.getContent().add(downloader);
            getUI().ifPresent(ui -> {
                if (!ui.isClosing()) {
                    ui.access(() -> downloader.getElement().callJsFunction("click"));
                }
            });
        } catch (Exception e) {
            handleExportError("Errore preparazione ZIP", e);
        }
    }

    /**
     * Gestisce e notifica gli errori di esportazione.
     *
     * @param message Messaggio di errore.
     * @param e       Eccezione che ha causato l'errore.
     */
    private void handleExportError(String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage(), e);
        if (!detached.get()) {
            getUI().ifPresent(ui -> ui.access(() ->
                    Notification.show(message + ": " + e.getMessage(), 5000, Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR)
            ));
        }
    }

    /**
     * Mostra un dialog di conferma prima di procedere con l'eliminazione di uno scenario.
     *
     * @param scenario Lo scenario da eliminare.
     */
    private void confirmAndDeleteScenario(Scenario scenario) {
        if (detached.get() || scenario == null) {
            return;
        }

        Dialog confirmDialog = new Dialog();
        confirmDialog.setCloseOnEsc(false);
        confirmDialog.setCloseOnOutsideClick(false);
        confirmDialog.setHeaderTitle("Conferma Eliminazione");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.add(new Paragraph("Sei sicuro di voler eliminare lo scenario:"));
        Span scenarioTitleSpan = new Span("\"" + scenario.getTitolo() + "\"?");
        scenarioTitleSpan.getStyle().set("font-weight", "bold");
        dialogLayout.add(scenarioTitleSpan);
        dialogLayout.add(new Paragraph("L'azione non è reversibile."));
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        confirmDialog.add(dialogLayout);

        Button confirmButton = new Button("Elimina", event -> {
            if (!detached.get()) {
                String scenarioName = scenario.getTitolo() != null ? scenario.getTitolo() : "Scenario";
                boolean deleted = scenarioDeletionService.deleteScenario(scenario.getId());
                getUI().ifPresent(ui -> ui.access(() -> {
                    if (deleted) {
                        Notification.show("Scenario " + scenarioName + " eliminato.", 3000, Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                        loadData(); // Ricarica i dati dopo l'eliminazione
                    } else {
                        Notification.show("Errore durante l'eliminazione.", 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }));
            }
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", event -> confirmDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    /**
     * Carica i dati degli scenari in modo asincrono, mostrando una barra di progresso
     * e aggiornando la griglia e i filtri all'occorrenza.
     */
    public void loadData() {
        if (detached.get()) {
            return;
        }
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }

        ui.access(() -> {
            progressBar.setVisible(true);
            scenariosGrid.setVisible(false);
            paginationControls.setVisible(false);
            searchPatientType.setEnabled(false);
            searchTitolo.setEnabled(false);
            searchAutori.setEnabled(false);
            searchTipo.setEnabled(false);
            searchPatologia.setEnabled(false);
            resetButton.setEnabled(false);
        });

        executorService.submit(() -> {
            try {
                List<Scenario> fetchedScenarios = scenarioService.getAllScenarios();
                allScenarios = (fetchedScenarios != null) ? new ArrayList<>(fetchedScenarios) : new ArrayList<>();
                // Ordina gli scenari per ID in ordine decrescente
                allScenarios.sort(Comparator.comparing(Scenario::getId, Comparator.nullsLast(Comparator.reverseOrder())));

                if (detached.get() || ui.isClosing()) {
                    return;
                }
                ui.access(() -> {
                    try {
                        applyFiltersAndRefreshGrid(); // Applica i filtri e aggiorna la griglia
                    } finally {
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                        paginationControls.setVisible(true);
                        // Riabilita i filtri
                        searchPatientType.setEnabled(true);
                        searchTitolo.setEnabled(true);
                        searchAutori.setEnabled(true);
                        searchTipo.setEnabled(true);
                        searchPatologia.setEnabled(true);
                        resetButton.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                logger.error("Errore durante il caricamento degli scenari: {}", e.getMessage(), e);
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        Notification.show("Errore caricamento dati: " + e.getMessage(), 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        progressBar.setVisible(false);
                        scenariosGrid.setVisible(true);
                        paginationControls.setVisible(true);
                        searchPatientType.setEnabled(true);
                        searchTitolo.setEnabled(true);
                        searchAutori.setEnabled(true);
                        searchTipo.setEnabled(true);
                        searchPatologia.setEnabled(true);
                        resetButton.setEnabled(true);
                        allScenarios.clear();
                        filteredScenarios.clear();
                        updateGridItems();
                        updatePaginationInfo();
                    });
                }
            }
        });
    }

    /**
     * Limita la lunghezza di una stringa a 30 caratteri.
     *
     * @param text La stringa da limitare.
     * @return La stringa troncata se più lunga di 30 caratteri, altrimenti la stringa originale.
     */
    private String limitLength(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= 30) {
            return text;
        }
        return text.substring(0, 30);
    }
}

