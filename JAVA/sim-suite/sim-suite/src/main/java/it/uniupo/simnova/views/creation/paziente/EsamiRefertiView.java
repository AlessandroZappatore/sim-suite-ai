package it.uniupo.simnova.views.creation.paziente;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.support.FormRow;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.uniupo.simnova.views.constant.ColorsConst.BORDER_COLORS;
import static it.uniupo.simnova.views.constant.ExamConst.ALLINSTREXAMS;
import static it.uniupo.simnova.views.constant.ExamConst.ALLLABSEXAMS;

/**
 * Vista per la gestione degli esami e referti nello scenario di simulazione.
 * Permette di aggiungere, modificare e rimuovere esami clinici e relativi referti,
 * sia testuali che multimediali. Supporta l'upload di file e la selezione da un elenco
 * predefinito di esami di laboratorio e strumentali.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Esami e Referti")
@Route(value = "esamiReferti", layout = MainLayout.class)
public class EsamiRefertiView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione delle attività e degli errori nella vista {@code EsamiRefertiView}.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsamiRefertiView.class);

    /**
     * Costanti per i colori dei bordi delle righe, utilizzate per differenziare visivamente le righe degli esami.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione degli esami e referti associati agli scenari.
     */
    private final EsameRefertoService esameRefertoService;
    /**
     * Servizio per la gestione dei file, utilizzato per l'upload e il recupero dei media associati agli esami.
     */
    private final FileStorageService fileStorageService;

    /**
     * Contenitore principale per le righe dei form di esame/referto.
     * Ogni riga rappresenta un esame o referto da inserire o modificare.
     */
    private final VerticalLayout rowsContainer;
    /**
     * Lista che contiene gli oggetti {@link FormRow} per gestire le righe degli esami e referti.
     * Ogni {@link FormRow} rappresenta un singolo esame o referto con i relativi campi di input.
     */
    private final List<FormRow> formRows = new ArrayList<>();
    /**
     * Pulsante per navigare alla vista successiva dopo il salvataggio degli esami e referti.
     * Il testo e l'icona del pulsante cambiano a seconda della modalità corrente (create/edit).
     */
    private final Button nextButton = StyleApp.getNextButton();

    /**
     * ID dello scenario corrente, utilizzato per associare gli esami e referti allo scenario specifico.
     * Viene impostato tramite il parametro dell'URL quando la vista viene caricata.
     */
    private Integer scenarioId;
    /**
     * Modalità corrente della vista, che può essere "create" per la creazione di nuovi esami/referti
     * o "edit" per la modifica di esami/referti esistenti.
     */
    private String mode;
    /**
     * Contatore per il numero di righe degli esami, utilizzato per generare titoli univoci per ogni riga.
     * Inizializzato a 1 per la prima riga.
     */
    private int rowCount = 1;

    /**
     * Costruttore della vista {@code EsamiRefertiView}.
     * Inizializza i servizi e configura la struttura base dell'interfaccia utente.
     *
     * @param scenarioService     Il servizio per la gestione degli scenari.
     * @param fileStorageService  Il servizio per la gestione dei file.
     * @param esameRefertoService Il servizio per la gestione degli esami e referti.
     */
    public EsamiRefertiView(ScenarioService scenarioService, FileStorageService fileStorageService, EsameRefertoService esameRefertoService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.esameRefertoService = esameRefertoService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna ai materiali necessari");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Esami e Referti",
                "Aggiungi gli esami e referti per il tuo scenario",
                VaadinIcon.FILE_TEXT_O.create(),
                "var(--lumo-primary-color)"
        );

        rowsContainer = new VerticalLayout();
        rowsContainer.setWidthFull();
        rowsContainer.setSpacing(true);

        Button addButton = new Button("Aggiungi Esame/Referto", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName(LumoUtility.Margin.Top.MEDIUM);
        addButton.addClickListener(event -> addNewRow()); // Listener per aggiungere una nuova riga

        contentLayout.add(headerSection, rowsContainer, addButton);

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione indietro
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("materialeNecessario/" + scenarioId)));
        // Listener per il salvataggio e la navigazione successiva
        nextButton.addClickListener(e -> saveEsamiRefertiAndNavigate(nextButton.getUI()));
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
                throw new NumberFormatException("Scenario ID è richiesto");
            }

            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];

            this.scenarioId = Integer.parseInt(scenarioIdStr);
            // Verifica che lo scenario esista nel servizio
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Scenario ID non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("Scenario ID non valido");
            }

            // Determina la modalità: "edit" se il secondo segmento è "edit", altrimenti "create"
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            // Ottiene il layout principale (Composite) per accedere ai suoi figli
            VerticalLayout mainLayout = getContent();

            // Rende visibili o invisibili componenti specifici a seconda della modalità
            // L'header (first HorizontalLayout) viene nascosto in modalità "edit"
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(header -> header.setVisible(!"edit".equals(mode)));

            // Il CreditsComponent nel footer (secondo HorizontalLayout, o ultimo) viene nascosto in modalità "edit"
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second) // Prende l'ultimo HorizontalLayout (il footer)
                    .ifPresent(footer -> {
                        HorizontalLayout footerLayout = (HorizontalLayout) footer;
                        footerLayout.getChildren()
                                .filter(component -> component instanceof CreditsComponent)
                                .forEach(credits -> credits.setVisible(!"edit".equals(mode)));
                    });

            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT: caricamento dati esistenti per scenario {}", this.scenarioId);
                nextButton.setText("Salva");
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                loadExistingData(); // Carica i dati dal database
            } else {
                logger.info("Modalità CREATE: aggiunta prima riga vuota per scenario {}", this.scenarioId);
                if (formRows.isEmpty()) { // Aggiunge una riga vuota solo se non ce ne sono già
                    addNewRow();
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dello Scenario ID: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
        }
    }

    /**
     * Carica gli esami e referti esistenti dallo scenario corrente e popola la UI.
     */
    private void loadExistingData() {
        List<EsameReferto> existingData = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);

        if (existingData == null || existingData.isEmpty()) {
            logger.warn("Nessun dato esistente trovato per scenario {} in modalità edit. Aggiungo una riga vuota.", this.scenarioId);
            addNewRow();
        } else {
            rowsContainer.removeAll(); // Rimuove tutte le righe esistenti nella UI
            formRows.clear(); // Pulisce la lista interna di FormRow
            rowCount = 1; // Resetta il contatore delle righe per ripartire
            for (EsameReferto data : existingData) {
                populateRow(data); // Popola una nuova riga con i dati esistenti
            }
            logger.info("Popolate {} righe con dati esistenti.", existingData.size());
        }
    }

    /**
     * Aggiunge una nuova riga vuota al form per l'inserimento di un esame/referto.
     */
    private void addNewRow() {
        FormRow newRow = new FormRow(rowCount++, fileStorageService); // Crea un nuovo FormRow
        formRows.add(newRow); // Aggiunge alla lista interna

        VerticalLayout rowContainer = new VerticalLayout();
        rowContainer.addClassName(LumoUtility.Padding.MEDIUM);
        rowContainer.addClassName(LumoUtility.Border.ALL);
        rowContainer.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        rowContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        rowContainer.setPadding(true);
        rowContainer.setSpacing(false);
        rowContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("border-left", "6px solid " + getBorderColor(rowCount)) // Bordo colorato per distinguere le righe
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        HorizontalLayout rowHeader = new HorizontalLayout();
        rowHeader.setWidthFull();
        rowHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        rowHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> {
            formRows.remove(newRow); // Rimuove dalla lista interna
            rowsContainer.remove(rowContainer); // Rimuove dalla UI
            if (formRows.isEmpty()) { // Se non rimangono righe, ne aggiunge una vuota
                addNewRow();
            }
        });

        rowHeader.add(newRow.getRowTitle(), deleteButton); // Aggiunge titolo e pulsante elimina
        rowContainer.add(rowHeader, newRow.getRowLayout()); // Aggiunge header e layout dei campi
        rowsContainer.add(rowContainer); // Aggiunge il contenitore della riga al layout generale
    }

    /**
     * Restituisce un colore del bordo basato sull'indice della riga, per dare varietà visiva.
     *
     * @param rowCount Il numero della riga.
     * @return Una stringa CSS per il colore del bordo.
     */
    private String getBorderColor(int rowCount) {
        return BORDER_COLORS[(rowCount) % BORDER_COLORS.length];
    }

    /**
     * Popola una riga del form con i dati esistenti di un {@link EsameReferto}.
     * Determina se l'esame è personalizzato o predefinito e imposta i campi di conseguenza.
     *
     * @param data L'oggetto {@link EsameReferto} contenente i dati da popolare.
     */
    private void populateRow(EsameReferto data) {
        FormRow existingRow = new FormRow(rowCount++, fileStorageService);
        formRows.add(existingRow);

        // Determina se il tipo di esame è personalizzato o proviene dalle liste predefinite
        boolean isCustom = !ALLLABSEXAMS.contains(data.getTipo()) && !ALLINSTREXAMS.contains(data.getTipo());

        if (isCustom) {
            existingRow.examTypeGroup.setValue("Inserisci manualmente");
            existingRow.customExamField.setValue(data.getTipo() != null ? data.getTipo() : "");
        } else {
            existingRow.examTypeGroup.setValue("Seleziona da elenco");
            existingRow.selectedExamField.setValue(data.getTipo() != null ? data.getTipo() : "");
        }
        existingRow.updateExamFieldVisibility(); // Aggiorna la visibilità dei campi di esame

        existingRow.getReportField().setValue(data.getRefertoTestuale() != null ? data.getRefertoTestuale() : "");

        // Popola i campi relativi al media
        if (data.getMedia() != null && !data.getMedia().isEmpty()) {
            existingRow.mediaSourceGroup.setValue("Seleziona da esistenti");
            existingRow.selectedMediaField.setValue(data.getMedia());
            existingRow.selectedExistingMedia = data.getMedia(); // Memorizza il nome del file esistente
            existingRow.updateMediaFieldVisibility(); // Aggiorna la visibilità dei campi media
        }

        VerticalLayout rowContainer = new VerticalLayout();
        rowContainer.addClassName(LumoUtility.Padding.MEDIUM);
        rowContainer.addClassName(LumoUtility.Border.ALL);
        rowContainer.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        rowContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        rowContainer.setPadding(true);
        rowContainer.setSpacing(false);
        // Aggiunge stili specifici per la riga popolata (simili a addNewRow)
        rowContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("border-left", "6px solid " + getBorderColor(rowCount))
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        HorizontalLayout rowHeader = new HorizontalLayout();
        rowHeader.setWidthFull();
        rowHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        rowHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> {
            formRows.remove(existingRow);
            rowsContainer.remove(rowContainer);
            if (formRows.isEmpty()) {
                addNewRow();
            }
        });

        rowHeader.add(existingRow.getRowTitle(), deleteButton);
        rowContainer.add(rowHeader, existingRow.getRowLayout());
        rowsContainer.add(rowContainer);
    }

    /**
     * Salva tutti gli esami e referti inseriti nel form.
     * Gestisce sia i nuovi upload che i riferimenti a file esistenti.
     * Dopo il salvataggio, naviga alla vista successiva in base alla modalità corrente.
     *
     * @param uiOptional L'istanza opzionale dell'UI corrente per la navigazione.
     */
    private void saveEsamiRefertiAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar); // Mostra una progress bar durante il salvataggio

            try {
                List<EsameReferto> esamiReferti = new ArrayList<>();
                boolean hasValidDataToSave = false; // Flag per controllare se ci sono dati significativi

                for (FormRow row : formRows) {
                    String fileName = "";
                    String selectedExam = row.getSelectedExam();
                    String reportText = row.getReportField().getValue();

                    // Gestione dell'upload o della selezione del media
                    if ("Carica nuovo file".equals(row.mediaSourceGroup.getValue())) {
                        if (row.getUpload().getReceiver() instanceof MemoryBuffer buffer) {
                            if (buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                                try (InputStream fileData = buffer.getInputStream()) {
                                    fileName = fileStorageService.storeFile(fileData, buffer.getFileName()); // Carica il file
                                    hasValidDataToSave = true;
                                }
                            }
                        }
                    } else { // Seleziona da esistenti
                        fileName = row.getSelectedMedia(); // Prende il nome del file già esistente
                        if (fileName != null && !fileName.isEmpty()) {
                            hasValidDataToSave = true;
                        }
                    }

                    // Se almeno uno dei campi principali (esame, referto, media) ha un valore, considera la riga valida
                    if ((selectedExam != null && !selectedExam.trim().isEmpty()) ||
                            (reportText != null && !reportText.trim().isEmpty()) ||
                            (fileName != null && !fileName.isEmpty())) {
                        hasValidDataToSave = true;
                    }

                    EsameReferto esameReferto = new EsameReferto(
                            row.getRowNumber(), // L'ID temporaneo della riga
                            scenarioId,
                            selectedExam,
                            fileName,
                            reportText
                    );
                    esamiReferti.add(esameReferto);
                }

                if (hasValidDataToSave) {
                    boolean success = esameRefertoService.saveEsamiReferti(scenarioId, esamiReferti); // Salva tutti gli esami
                    if (success) {
                        Notification.show("Esami e referti salvati con successo", 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }
                } else {
                    logger.info("Nessun dato significativo da salvare per gli esami e referti dello scenario {}", scenarioId);
                }

                // Navigazione finale in base alla modalità
                boolean isEditMode = "edit".equals(mode);
                if (!isEditMode) {
                    ui.navigate("moulage/" + scenarioId); // Naviga alla fase successiva (Moulage)
                } else {
                    ui.navigate("scenari/" + scenarioId); // In modalità edit, torna alla vista dettaglio scenario
                }

            } catch (Exception e) {
                logger.error("Errore durante il salvataggio degli esami e referti", e);
                Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                getContent().remove(progressBar); // Rimuove la progress bar
            }
        });
    }
}

