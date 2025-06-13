package it.uniupo.simnova.views.creation.risorse;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.Materiale;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Vista per la gestione del ***materiale necessario** per l'allestimento dello scenario di simulazione.
 * Permette di selezionare materiali da una lista esistente, aggiungere nuovi materiali
 * e rimuovere materiali dalla lista selezionata o dal database.
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@PageTitle("Materiale Necessario")
@Route(value = "materialeNecessario", layout = MainLayout.class)
public class MaterialenecessarioView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione delle attività e degli errori nella vista {@code MaterialenecessarioView}.
     */
    private static final Logger logger = LoggerFactory.getLogger(MaterialenecessarioView.class);

    /**
     * Servizi utilizzati per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione dei materiali, che consente di recuperare, aggiungere ed eliminare materiali.
     */
    private final MaterialeService materialeService;

    /**
     * Griglie per visualizzare i materiali disponibili e quelli selezionati.
     * La griglia dei materiali disponibili mostra tutti i materiali non ancora associati allo scenario,
     * mentre la griglia dei materiali selezionati mostra solo quelli già associati.
     */
    private final Grid<Materiale> materialiDisponibiliGrid;
    /**
     * Griglia per i materiali selezionati, che mostra solo quelli associati allo scenario corrente.
     * Permette di rimuovere i materiali dalla selezione.
     */
    private final Grid<Materiale> materialiSelezionatiGrid;

    /**
     * Lista dei materiali selezionati per lo scenario corrente.
     * Questa lista viene popolata con i materiali già associati allo scenario
     * e aggiornata quando l'utente aggiunge o rimuove materiali.
     */
    private final List<Materiale> materialiSelezionati = new ArrayList<>();
    /**
     * Pulsante per navigare alla vista successiva (Esami e Referti).
     * Viene utilizzato per salvare i materiali selezionati e procedere alla configurazione degli esami.
     */
    private final Button nextButton = StyleApp.getNextButton();
    /**
     * Lista di tutti i materiali disponibili nel database.
     * Viene utilizzata per popolare la griglia dei materiali disponibili e per gestire le operazioni di ricerca.
     */
    private List<Materiale> tuttiMateriali = new ArrayList<>();
    /**
     * ID dello scenario corrente, che viene passato come parametro nell'URL.
     * Viene utilizzato per caricare i materiali associati e per salvare le modifiche.
     */
    private Integer scenarioId;
    /**
     * Campo di ricerca per filtrare i materiali disponibili.
     * Permette all'utente di cercare materiali per nome o descrizione.
     */
    private TextField searchField;
    /**
     * Modalità della vista, che può essere "create" per la creazione di un nuovo scenario
     * o "edit" per la modifica di uno scenario esistente.
     * Viene determinata in base ai parametri dell'URL.
     */
    private String mode;

    /**
     * Costruttore della vista {@code MaterialenecessarioView}.
     * Inizializza i servizi e configura la struttura base dell'interfaccia utente,
     * incluse le griglie per i materiali disponibili e selezionati.
     *
     * @param scenarioService    Il servizio per la gestione degli scenari.
     * @param materialeService   Il servizio per la gestione dei materiali.
     * @param fileStorageService Il servizio per la gestione dei file, utilizzato per l'AppHeader.
     */
    public MaterialenecessarioView(ScenarioService scenarioService, MaterialeService materialeService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        this.materialeService = materialeService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna agli obiettivi didattici");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Materiale necessario",
                "Seleziona i materiali necessari per l'allestimento della sala o aggiungine di nuovi",
                VaadinIcon.BED.create(), // Icona a forma di letto
                "var(--lumo-primary-color)"
        );

        // Griglia per i materiali disponibili
        materialiDisponibiliGrid = new Grid<>();
        materialiDisponibiliGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        materialiDisponibiliGrid.setAllRowsVisible(true);
        materialiDisponibiliGrid.addColumn(Materiale::nome).setHeader("Materiale disponibile").setFlexGrow(1);
        materialiDisponibiliGrid.addColumn(Materiale::descrizione).setHeader("Descrizione").setFlexGrow(2);
        // Colonna "Aggiungi" con pulsante
        materialiDisponibiliGrid.addColumn(
                        new ComponentRenderer<>(materiale -> {
                            Div buttonContainer = new Div();
                            // Mostra il pulsante "Aggiungi" solo se il materiale non è già selezionato
                            boolean isSelected = materialiSelezionati.stream().anyMatch(m -> m.getId().equals(materiale.getId()));
                            if (!isSelected) {
                                Button addButton = new Button(new Icon(VaadinIcon.PLUS));
                                addButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
                                addButton.addClickListener(e -> {
                                    // Aggiunge solo se non duplicato (gestione visuale)
                                    if (materialiSelezionati.stream().noneMatch(m -> m.getId().equals(materiale.getId()))) {
                                        materialiSelezionati.add(materiale);
                                        aggiornaGrids(); // Aggiorna entrambe le griglie
                                    }
                                });
                                buttonContainer.add(addButton);
                            }
                            return buttonContainer;
                        })
                ).setHeader("Aggiungi")
                .setWidth("90px")
                .setFlexGrow(0);
        // Colonna "Elimina" per i materiali disponibili (elimina dal DB)
        materialiDisponibiliGrid.addColumn(
                        new ComponentRenderer<>(materiale -> {
                            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
                            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                            deleteButton.addClickListener(e -> showDeleteConfirmDialog(materiale)); // Mostra conferma
                            return deleteButton;
                        })
                ).setHeader("Elimina")
                .setWidth("90px")
                .setFlexGrow(0);

        // Griglia per i materiali selezionati
        materialiSelezionatiGrid = new Grid<>();
        materialiSelezionatiGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        materialiSelezionatiGrid.setAllRowsVisible(true);
        materialiSelezionatiGrid.addColumn(Materiale::nome).setHeader("Materiale selezionato").setFlexGrow(1);
        materialiSelezionatiGrid.addColumn(Materiale::descrizione).setHeader("Descrizione").setFlexGrow(2);
        // Colonna "Rimuovi" per i materiali selezionati (rimuove dalla lista dello scenario)
        materialiSelezionatiGrid.addColumn(
                        new ComponentRenderer<>(materiale -> {
                            Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
                            removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                            removeButton.addClickListener(e -> {
                                materialiSelezionati.removeIf(m -> m.getId().equals(materiale.getId()));
                                aggiornaGrids(); // Aggiorna entrambe le griglie
                            });
                            return removeButton;
                        })
                ).setHeader("Rimuovi")
                .setWidth("90px")
                .setFlexGrow(0);

        // Layout per le due griglie (disponibili e selezionati)
        HorizontalLayout gridsLayout = new HorizontalLayout();
        gridsLayout.setWidthFull();
        gridsLayout.setSpacing(true);
        gridsLayout.setAlignItems(FlexComponent.Alignment.START);

        // Layout per la griglia dei materiali disponibili
        VerticalLayout disponibiliLayout = getLayout(); // Layout con larghezza 50%
        setupMaterialiDisponibiliLayout(disponibiliLayout);

        // Layout per la griglia dei materiali selezionati
        VerticalLayout selezionatiLayout = getLayout(); // Layout con larghezza 50%

        HorizontalLayout titleSelezionatiLayout = new HorizontalLayout(new Paragraph("Materiali selezionati:"));
        titleSelezionatiLayout.setHeight("40px"); // Allinea i titoli delle griglie
        titleSelezionatiLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSelezionatiLayout.setPadding(false);
        titleSelezionatiLayout.setMargin(false);

        // Spacer per allineare le griglie verticalmente
        Div spacer = new Div();
        spacer.setHeight("52px"); // Altezza basata sui controlli della griglia superiore
        spacer.getStyle().set("visibility", "hidden");

        selezionatiLayout.add(
                titleSelezionatiLayout,
                spacer, // Aggiunge lo spacer per l'allineamento
                materialiSelezionatiGrid
        );
        gridsLayout.add(disponibiliLayout, selezionatiLayout);
        gridsLayout.expand(disponibiliLayout, selezionatiLayout); // Le due metà si espandono per riempire lo spazio

        contentLayout.add(headerSection, gridsLayout);

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione indietro (alla vista obiettivi didattici)
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("obiettivididattici/" + scenarioId)));
        // Listener per il salvataggio e la navigazione successiva
        nextButton.addClickListener(e -> saveMaterialiAndNavigate(nextButton.getUI()));
    }

    /**
     * Gestisce il parametro dell'URL, che può includere l'ID dello scenario e la modalità (edit/create).
     * Carica i dati esistenti se in modalità "edit".
     *
     * @param event     L'evento di navigazione.
     * @param parameter La stringa del parametro URL (es. "123/edit").
     * @throws NotFoundException Se l'ID dello scenario non è valido o mancante.
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("ID Scenario è richiesto");
            }

            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];

            this.scenarioId = Integer.parseInt(scenarioIdStr.trim());
            // Verifica che lo scenario esista nel servizio
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("ID Scenario non valido o non esistente: {}", scenarioId);
                throw new NotFoundException("Scenario con ID " + scenarioId + " non trovato.");
            }

            // Determina la modalità: "edit" se il secondo segmento è "edit", altrimenti "create"
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            // Ottiene il layout principale (Composite) per accedere ai suoi figli
            VerticalLayout mainLayout = getContent();

            // Rende visibili o invisibili componenti specifici a seconda della modalità
            mainLayout.getChildren().forEach(component -> {
                if (component instanceof HorizontalLayout layout) {
                    // L'header (secondo componente del mainLayout, che è un HorizontalLayout con AppHeader)
                    // viene nascosto in modalità "edit"
                    if (layout.getComponentCount() > 1 && layout.getComponentAt(1) instanceof AppHeader) {
                        layout.setVisible(!"edit".equals(mode));
                    }
                    // Il CreditsComponent nel footer viene nascosto in modalità "edit"
                    if (layout.getComponentCount() > 0 && layout.getComponentAt(0) instanceof CreditsComponent) {
                        if ("edit".equals(mode)) {
                            logger.info("Modalità EDIT: caricamento dati esistenti per scenario {}", this.scenarioId);
                            nextButton.setText("Salva");
                            nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                            nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                            layout.getComponentAt(0).setVisible(false); // Nasconde CreditsComponent
                        }
                    }
                }
            });
            loadData(); // Carica i dati delle griglie
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido o mancante: '{}'. Errore: {}", parameter, e.getMessage());
            event.rerouteToError(NotFoundException.class, "ID scenario non valido: " + parameter);
        } catch (NotFoundException e) {
            event.rerouteToError(NotFoundException.class, e.getMessage());
        }
    }

    /**
     * Carica i materiali disponibili dal database e quelli già selezionati per lo scenario corrente.
     * Aggiorna le griglie di visualizzazione.
     */
    private void loadData() {
        try {
            tuttiMateriali = materialeService.getAllMaterials(); // Carica tutti i materiali dal DB
            List<Materiale> materialiScenario = materialeService.getMaterialiByScenarioId(scenarioId); // Carica i materiali già associati
            materialiSelezionati.clear();
            materialiSelezionati.addAll(materialiScenario);
            aggiornaGrids(); // Aggiorna la visualizzazione delle griglie
        } catch (Exception e) {
            logger.error("Errore durante il caricamento dei dati per lo scenario {}", scenarioId, e);
            Notification.show("Errore nel caricamento dei materiali", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Aggiorna il contenuto di entrambe le griglie:
     * - {@code materialiDisponibiliGrid}: mostra tutti i materiali non ancora selezionati.
     * - {@code materialiSelezionatiGrid}: mostra solo i materiali aggiunti allo scenario.
     * Applica anche il filtro di ricerca ai materiali disponibili.
     */
    private void aggiornaGrids() {
        Set<Integer> idsSelezionati = materialiSelezionati.stream()
                .map(Materiale::getId)
                .filter(Objects::nonNull) // Filtra gli ID nulli
                .collect(Collectors.toSet());

        // Materiali disponibili sono quelli totali meno quelli già selezionati
        List<Materiale> materialiDisponibiliNonSelezionati = tuttiMateriali.stream()
                .filter(m -> m.getId() != null && !idsSelezionati.contains(m.getId()))
                .collect(Collectors.toList());

        // Applica il filtro di ricerca se il campo searchField è popolato
        String searchTerm = (searchField != null) ? searchField.getValue() : null;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String term = searchTerm.toLowerCase().trim();
            materialiDisponibiliNonSelezionati = materialiDisponibiliNonSelezionati.stream()
                    .filter(m -> (m.nome() != null && m.nome().toLowerCase().contains(term)) ||
                            (m.descrizione() != null && m.descrizione().toLowerCase().contains(term)))
                    .collect(Collectors.toList());
        }

        materialiDisponibiliGrid.setItems(materialiDisponibiliNonSelezionati);
        materialiSelezionatiGrid.setItems(new ArrayList<>(materialiSelezionati)); // Crea una nuova lista per triggerare l'aggiornamento della griglia
    }

    /**
     * Configura il layout della griglia dei materiali disponibili,
     * includendo il campo di ricerca e il pulsante per aggiungere nuovi materiali.
     *
     * @param disponibiliLayout Il {@link VerticalLayout} in cui inserire i materiali disponibili.
     */
    private void setupMaterialiDisponibiliLayout(VerticalLayout disponibiliLayout) {
        searchField = new TextField();
        searchField.setPlaceholder("Cerca materiali...");
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.addValueChangeListener(e -> aggiornaGrids()); // Aggiorna le griglie al cambio del testo di ricerca

        Button addNewMaterialButton = new Button("Aggiungi nuovo materiale", new Icon(VaadinIcon.PLUS));
        addNewMaterialButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        addNewMaterialButton.addClickListener(e -> showNuovoMaterialeDialog()); // Apre il dialog per nuovo materiale

        HorizontalLayout actionsLayout = new HorizontalLayout(searchField, addNewMaterialButton);
        actionsLayout.setWidthFull();
        actionsLayout.setPadding(false);
        actionsLayout.setSpacing(true);

        HorizontalLayout titleLayout = new HorizontalLayout(new Paragraph("Materiali disponibili:"));
        titleLayout.setHeight("40px");
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setPadding(false);
        titleLayout.setMargin(false);

        disponibiliLayout.add(
                titleLayout,
                actionsLayout,
                materialiDisponibiliGrid
        );
        disponibiliLayout.setFlexGrow(1.0, materialiDisponibiliGrid); // Permette alla griglia di espandersi
    }

    /**
     * Mostra un dialog modale per l'aggiunta di un nuovo materiale al database.
     * Permette di inserire il nome e una descrizione del materiale.
     */
    private void showNuovoMaterialeDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi nuovo materiale");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        TextField nomeField = new TextField("Nome");
        nomeField.setRequiredIndicatorVisible(true);
        nomeField.setErrorMessage("Il nome è obbligatorio");

        TextField descrizioneField = new TextField("Descrizione");

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            if (nomeField.getValue() == null || nomeField.getValue().trim().isEmpty()) {
                nomeField.setInvalid(true);
                return; // Non procede se il nome è vuoto
            }
            nomeField.setInvalid(false); // Resetta lo stato di invalidità

            try {
                Materiale nuovoMateriale = new Materiale(
                        -1, // ID temporaneo, verrà assegnato dal DB
                        nomeField.getValue().trim(),
                        descrizioneField.getValue() != null ? descrizioneField.getValue().trim() : ""
                );

                Materiale savedMateriale = materialeService.saveMateriale(nuovoMateriale); // Salva il materiale
                if (savedMateriale != null && savedMateriale.getId() != null) {
                    tuttiMateriali.add(savedMateriale); // Aggiunge alla lista di tutti i materiali
                    materialiSelezionati.add(savedMateriale); // Seleziona automaticamente il nuovo materiale
                    aggiornaGrids(); // Aggiorna le griglie
                    dialog.close();
                    Notification.show("Materiale aggiunto e selezionato", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    Notification.show("Errore: Salvataggio del materiale fallito", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception ex) {
                logger.error("Errore durante il salvataggio del nuovo materiale", ex);
                Notification.show("Errore tecnico: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        dialogLayout.add(nomeField, descrizioneField, buttonLayout);
        dialog.add(dialogLayout);
        dialog.open();
    }

    /**
     * Salva l'associazione dei materiali selezionati allo scenario corrente.
     * Mostra una progress bar durante il salvataggio e notifiche di stato/errore.
     * Dopo il salvataggio, naviga alla vista successiva.
     *
     * @param uiOptional L'istanza opzionale dell'UI corrente per la navigazione.
     */
    private void saveMaterialiAndNavigate(Optional<UI> uiOptional) {
        if (scenarioId == null) {
            Notification.show("ID Scenario non disponibile. Impossibile salvare.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        uiOptional.ifPresent(ui -> {
            Dialog progressDialog = new Dialog();
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            progressDialog.add(new H2("Salvataggio..."), progressBar);
            progressDialog.setCloseOnEsc(false);
            progressDialog.setCloseOnOutsideClick(false);
            progressDialog.open();

            try {
                // Estrae gli ID dei materiali selezionati per l'associazione
                List<Integer> idsMateriali = materialiSelezionati.stream()
                        .map(Materiale::getId)
                        .filter(Objects::nonNull) // Assicura che l'ID non sia nullo
                        .collect(Collectors.toList());

                boolean success = materialeService.associaMaterialiToScenario(scenarioId, idsMateriali); // Associa i materiali

                ui.access(() -> {
                    progressDialog.close(); // Chiude la progress bar
                    if (success) {
                        Notification.show("Materiali salvati.", 2000, Notification.Position.BOTTOM_START)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                        if (!"edit".equals(mode)) {
                            // Se in modalità "create", naviga alla vista successiva (Esami e Referti)
                            ui.navigate("esamiReferti/" + scenarioId + "/create");
                        }
                    } else {
                        Notification.show("Errore durante il salvataggio dei materiali.", 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                logger.error("Errore grave durante il salvataggio dei materiali per scenario {}", scenarioId, e);
                ui.access(() -> {
                    progressDialog.close();
                    Notification.show("Errore tecnico: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                });
            }
        });
    }

    /**
     * Mostra un dialog di conferma prima di eliminare un materiale dal database.
     * L'eliminazione è definitiva e rimuove il materiale da tutti gli scenari che lo utilizzano.
     *
     * @param materiale Il materiale da eliminare.
     */
    private void showDeleteConfirmDialog(Materiale materiale) {
        if (materiale == null || materiale.getId() == null) {
            logger.warn("Tentativo di eliminare materiale nullo o senza ID.");
            return;
        }

        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Conferma eliminazione");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        Paragraph message = new Paragraph("Sei sicuro di voler eliminare definitivamente il materiale \"" + materiale.nome() + "\"?");

        Paragraph warning = new Paragraph("L'operazione non può essere annullata e rimuoverà il materiale da TUTTI gli scenari che lo utilizzano.");
        warning.addClassNames(LumoUtility.TextColor.ERROR, LumoUtility.FontSize.SMALL);

        Button deleteButton = new Button("Elimina");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle().set("margin-left", "auto"); // Spinge il pulsante a destra
        deleteButton.addClickListener(e -> {
            deleteMateriale(materiale); // Chiama il metodo di eliminazione
            confirmDialog.close();
        });

        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> confirmDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, deleteButton);
        buttonLayout.setWidthFull();

        dialogLayout.add(message, warning, buttonLayout);
        confirmDialog.add(dialogLayout);
        confirmDialog.open();
    }

    /**
     * Esegue l'eliminazione effettiva di un materiale dal database.
     * Dopo l'eliminazione, aggiorna le griglie.
     *
     * @param materiale Il materiale da eliminare.
     */
    private void deleteMateriale(Materiale materiale) {
        if (materiale == null || materiale.getId() == null) {
            return;
        }
        Integer materialeId = materiale.getId();

        try {
            boolean success = materialeService.deleteMateriale(materialeId); // Elimina il materiale dal DB
            if (success) {
                tuttiMateriali.removeIf(m -> materialeId.equals(m.getId())); // Rimuove dalla lista globale
                materialiSelezionati.removeIf(m -> materialeId.equals(m.getId())); // Rimuove dalla lista selezionata

                aggiornaGrids(); // Aggiorna le UI delle griglie

                Notification.show("Materiale \"" + materiale.nome() + "\" eliminato.",
                                3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                String nomeMateriale = (materiale.nome() != null) ? materiale.nome() : "ID " + materialeId;
                Notification.show("Impossibile eliminare il materiale \"" + nomeMateriale + "\". Potrebbe essere in uso in altri scenari.",
                                4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        } catch (Exception ex) {
            String nomeMateriale = (materiale.nome() != null) ? materiale.nome() : "ID " + materialeId;
            logger.error("Errore durante l'eliminazione del materiale {}", nomeMateriale, ex);
            Notification.show("Errore tecnico durante l'eliminazione: " + ex.getMessage(),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Crea un layout verticale standard con larghezza fissa (50%) per le griglie.
     *
     * @return Un {@link VerticalLayout} preconfigurato.
     */
    VerticalLayout getLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("50%");
        layout.setSpacing(true);
        layout.setPadding(false);

        return layout;
    }
}