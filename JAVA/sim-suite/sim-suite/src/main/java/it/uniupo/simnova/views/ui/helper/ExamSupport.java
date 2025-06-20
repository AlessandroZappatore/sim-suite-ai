package it.uniupo.simnova.views.ui.helper;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.Element;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.domain.respons_model.LabExamSet;
import it.uniupo.simnova.domain.respons_model.ReportSet;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.ActiveNotifierManager;
import it.uniupo.simnova.service.NotifierService;
import it.uniupo.simnova.service.ai_api.ExternalApiService;
import it.uniupo.simnova.service.ai_api.LabExamService;
import it.uniupo.simnova.service.ai_api.model.LabExamGenerationRequest;
import it.uniupo.simnova.service.ai_api.model.ReportGenerationRequest;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static it.uniupo.simnova.views.constant.ExamConst.ALLINSTREXAMS;
import static it.uniupo.simnova.views.ui.helper.support.ErrorExtractor.extractErrorReasonFromJson;

/**
 * Classe di supporto per la gestione degli esami e referti nella UI.
 * Fornisce metodi per creare contenuti dinamici relativi agli esami e gestire le interazioni con l'utente.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ExamSupport {
    /**
     * Logger per la registrazione delle operazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ExamSupport.class);
    /**
     * Istanza di Gson per la serializzazione/deserializzazione JSON.
     */
    private static final Gson gson = new Gson();

    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    private ExamSupport() {

    }

    /**
     * Crea il contenuto della sezione Esami e Referti per uno scenario specifico.
     *
     * @param esameRefertoService   servizio per la gestione degli esami e referti
     * @param fileStorageService    servizio per la gestione dei file
     * @param scenarioId            ID dello scenario corrente
     * @param scenario              scenario corrente
     * @param externalApiService    servizio per le API esterne
     * @param labExamService        servizio per la gestione degli esami di laboratorio
     * @param executorService       servizio per l'esecuzione di task in background
     * @param notifierService       servizio per la gestione delle notifiche
     * @param esameFisicoService    servizio per la gestione degli esami fisici
     * @param activeNotifierManager gestore delle notifiche attive
     * @return VerticalLayout contenente gli esami e referti dello scenario
     */
    public static VerticalLayout createExamsContent(EsameRefertoService esameRefertoService,
                                                    FileStorageService fileStorageService,
                                                    Integer scenarioId,
                                                    Scenario scenario,
                                                    ExternalApiService externalApiService,
                                                    LabExamService labExamService,
                                                    ExecutorService executorService,
                                                    NotifierService notifierService,
                                                    EsameFisicoService esameFisicoService,
                                                    ActiveNotifierManager activeNotifierManager) {
        List<EsameReferto> esami = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        if (esami != null && !esami.isEmpty()) {
            for (EsameReferto esame : esami) {
                Div examCard = new Div();
                examCard.addClassName("exam-card");
                examCard.getStyle()
                        .set("background-color", "var(--lumo-base-color)")
                        .set("border-radius", "var(--lumo-border-radius-l)")
                        .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                        .set("padding", "var(--lumo-space-m)")
                        .set("margin-bottom", "var(--lumo-space-m)")
                        .set("width", "95%")
                        .set("box-sizing", "border-box")
                        .set("margin-left", "auto")
                        .set("margin-right", "auto");

                HorizontalLayout cardHeader = new HorizontalLayout();
                cardHeader.setWidthFull();
                cardHeader.setAlignItems(FlexComponent.Alignment.CENTER);
                cardHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                H3 examTitle = new H3(esame.getTipo());
                examTitle.getStyle()
                        .set("margin-top", "0")
                        .set("margin-bottom", "0")
                        .set("color", "var(--lumo-primary-text-color)")
                        .set("flex-grow", "1");

                Button editMediaButton = StyleApp.getButton("Modifica Media", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
                editMediaButton.setTooltipText("Modifica file multimediale per " + esame.getTipo());
                editMediaButton.getElement().setAttribute("aria-label", "Modifica file multimediale per " + esame.getTipo());

                HorizontalLayout titleAndEditMedia = new HorizontalLayout(examTitle, editMediaButton);
                titleAndEditMedia.setAlignItems(FlexComponent.Alignment.CENTER);
                titleAndEditMedia.setSpacing(true);
                titleAndEditMedia.getStyle().set("flex-grow", "1");

                Button deleteButton = new Button(VaadinIcon.TRASH.create());
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                deleteButton.setTooltipText("Elimina Esame " + esame.getTipo());
                deleteButton.getElement().setAttribute("aria-label", "Elimina Esame " + esame.getTipo());
                deleteButton.addClickListener(e -> {
                    Dialog confirmDialog = new Dialog();
                    confirmDialog.setCloseOnEsc(true);
                    confirmDialog.setCloseOnOutsideClick(true);

                    confirmDialog.add(new H4("Conferma Eliminazione"));
                    confirmDialog.add(new Paragraph("Sei sicuro di voler eliminare l'esame/referto '" + esame.getTipo() + "'? Questa operazione non può essere annullata."));

                    Button confirmDeleteButton = new Button("Elimina", VaadinIcon.TRASH.create());
                    confirmDeleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
                    confirmDeleteButton.addClickListener(confirmEvent -> {
                        boolean deleted = esameRefertoService.deleteEsameReferto(esame.getIdEsame(), scenarioId);
                        if (deleted) {
                            Notification.show("Esame '" + esame.getTipo() + "' eliminato con successo.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            layout.remove(examCard);
                        } else {
                            Notification.show("Errore durante l'eliminazione dell'esame.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                        confirmDialog.close();
                    });

                    Button cancelDeleteButton = new Button("Annulla");
                    cancelDeleteButton.addClickListener(cancelEvent -> confirmDialog.close());

                    HorizontalLayout dialogButtons = new HorizontalLayout(confirmDeleteButton, cancelDeleteButton);
                    dialogButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
                    dialogButtons.setWidthFull();
                    confirmDialog.add(dialogButtons);
                    confirmDialog.open();
                });

                cardHeader.add(titleAndEditMedia, deleteButton);
                examCard.add(cardHeader);

                VerticalLayout mediaSectionContainer = new VerticalLayout();
                mediaSectionContainer.setPadding(false);
                mediaSectionContainer.setSpacing(true);
                mediaSectionContainer.setWidthFull();

                Div mediaPreviewWrapper = new Div();
                mediaPreviewWrapper.setWidthFull();
                if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                    mediaPreviewWrapper.add(createMediaPreview(esame.getMedia()));
                }
                mediaSectionContainer.add(mediaPreviewWrapper);

                VerticalLayout mediaEditLayout = new VerticalLayout();
                mediaEditLayout.setPadding(false);
                mediaEditLayout.setSpacing(true);
                mediaEditLayout.setVisible(false);
                mediaEditLayout.getStyle().set("border", "1px dashed var(--lumo-contrast-20pct)").set("padding", "var(--lumo-space-s)");

                RadioButtonGroup<String> mediaSourceGroupEdit = new RadioButtonGroup<>();
                mediaSourceGroupEdit.setLabel("Sorgente Media");
                mediaSourceGroupEdit.setItems("Carica nuovo file", "Seleziona da esistenti");

                MemoryBuffer bufferEdit = new MemoryBuffer();
                Upload uploadMediaEdit = new Upload(bufferEdit);
                uploadMediaEdit.setAcceptedFileTypes("image/*", "video/*", "audio/*", ".pdf");
                uploadMediaEdit.setMaxFiles(1);
                uploadMediaEdit.setVisible(false);

                ComboBox<String> selectExistingMediaEdit = new ComboBox<>("Seleziona Media Esistente");
                selectExistingMediaEdit.setWidthFull();

                try {
                    List<String> availableFiles = fileStorageService.getAllFiles();
                    if (availableFiles != null) {
                        selectExistingMediaEdit.setItems(availableFiles);
                    } else {
                        selectExistingMediaEdit.setItems(new ArrayList<>());
                        logger.warn("No available files found from FileStorageService for scenario {}", scenarioId);
                    }
                } catch (Exception ex) {
                    logger.error("Error fetching available files for media editing", ex);
                    selectExistingMediaEdit.setItems(new ArrayList<>());
                }
                selectExistingMediaEdit.setVisible(false);

                mediaSourceGroupEdit.addValueChangeListener(event -> {
                    String value = event.getValue();
                    uploadMediaEdit.setVisible("Carica nuovo file".equals(value));
                    selectExistingMediaEdit.setVisible("Seleziona da esistenti".equals(value));
                });

                if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                    mediaSourceGroupEdit.setValue("Seleziona da esistenti");
                    selectExistingMediaEdit.setValue(esame.getMedia());
                } else {
                    mediaSourceGroupEdit.setValue("Carica nuovo file");
                }

                Button saveMediaButton = StyleApp.getButton("Salva Media", VaadinIcon.CHECK, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
                Button cancelMediaButton = StyleApp.getButton("Annulla", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");

                HorizontalLayout mediaEditActions = new HorizontalLayout(saveMediaButton, cancelMediaButton);
                mediaEditActions.setSpacing(true);

                mediaEditLayout.add(mediaSourceGroupEdit, uploadMediaEdit, selectExistingMediaEdit, mediaEditActions);
                mediaSectionContainer.add(mediaEditLayout);

                editMediaButton.addClickListener(ev -> {
                    mediaPreviewWrapper.setVisible(false);
                    editMediaButton.setVisible(false);
                    mediaEditLayout.setVisible(true);

                    try {
                        List<String> availableFiles = fileStorageService.getAllFiles();
                        if (availableFiles != null) {
                            selectExistingMediaEdit.setItems(availableFiles);
                            if (esame.getMedia() != null && !esame.getMedia().isEmpty() && availableFiles.contains(esame.getMedia())) {
                                selectExistingMediaEdit.setValue(esame.getMedia());
                                mediaSourceGroupEdit.setValue("Seleziona da esistenti");
                            } else if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                                logger.warn("Current media '{}' for exam '{}' not in available files list. Defaulting to upload.", esame.getMedia(), esame.getTipo());
                                mediaSourceGroupEdit.setValue("Carica nuovo file");
                                selectExistingMediaEdit.clear();
                            } else {
                                mediaSourceGroupEdit.setValue("Carica nuovo file");
                            }
                        } else {
                            selectExistingMediaEdit.setItems(new ArrayList<>());
                            mediaSourceGroupEdit.setValue("Carica nuovo file");
                        }
                    } catch (Exception ex) {
                        logger.error("Error re-fetching available files for media editing", ex);
                        selectExistingMediaEdit.setItems(new ArrayList<>());
                        mediaSourceGroupEdit.setValue("Carica nuovo file");
                    }

                    mediaSourceGroupEdit.getOptionalValue().ifPresentOrElse(
                            currentValue -> {
                                uploadMediaEdit.setVisible("Carica nuovo file".equals(currentValue));
                                selectExistingMediaEdit.setVisible("Seleziona da esistenti".equals(currentValue));
                            },
                            () -> {
                                mediaSourceGroupEdit.setValue("Carica nuovo file");
                                uploadMediaEdit.setVisible(true);
                                selectExistingMediaEdit.setVisible(false);
                            }
                    );
                });

                cancelMediaButton.addClickListener(ev -> {
                    mediaEditLayout.setVisible(false);
                    mediaPreviewWrapper.setVisible(true);
                    editMediaButton.setVisible(true);
                    uploadMediaEdit.getElement().executeJs("this.files=[]");
                });

                saveMediaButton.addClickListener(ev -> {
                    String newMediaFileName;
                    boolean success = false;

                    if ("Carica nuovo file".equals(mediaSourceGroupEdit.getValue())) {
                        if (bufferEdit.getFileName() != null && !bufferEdit.getFileName().isEmpty()) {
                            try (InputStream fileData = bufferEdit.getInputStream()) {
                                newMediaFileName = fileStorageService.storeFile(fileData, bufferEdit.getFileName());
                                logger.info("Nuovo file caricato: {}", newMediaFileName);
                            } catch (Exception ex) {
                                logger.error("Errore durante il salvataggio del file caricato", ex);
                                Notification.show("Errore salvataggio file: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                return;
                            }
                        } else {
                            Notification.show("Nessun file selezionato per il caricamento.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
                            return;
                        }
                    } else {
                        newMediaFileName = selectExistingMediaEdit.getValue();
                        if (newMediaFileName == null || newMediaFileName.trim().isEmpty()) {
                            Notification.show("Nessun file esistente selezionato.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_WARNING);
                            return;
                        }
                        logger.info("File esistente selezionato: {}", newMediaFileName);
                    }

                    if (newMediaFileName != null && !newMediaFileName.isEmpty()) {
                        success = esameRefertoService.updateMedia(esame.getIdEsame(), scenarioId, newMediaFileName);
                        if (success) {
                            esame.setMedia(newMediaFileName);
                            mediaPreviewWrapper.removeAll();
                            mediaPreviewWrapper.add(createMediaPreview(newMediaFileName));
                            Notification.show("Media aggiornato con successo.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } else {
                            Notification.show("Errore durante l'aggiornamento del media.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    } else if (!"Carica nuovo file".equals(mediaSourceGroupEdit.getValue()) && esame.getMedia() != null && !esame.getMedia().isEmpty()) {

                        success = esameRefertoService.updateMedia(esame.getIdEsame(), scenarioId, null);
                        if (success) {
                            esame.setMedia(null);
                            mediaPreviewWrapper.removeAll();
                            Notification.show("Media rimosso con successo.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } else {
                            Notification.show("Errore durante la rimozione del media.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    }

                    if (success || (newMediaFileName == null && !"Carica nuovo file".equals(mediaSourceGroupEdit.getValue()))) {
                        mediaEditLayout.setVisible(false);
                        mediaPreviewWrapper.setVisible(true);
                        editMediaButton.setVisible(true);
                        uploadMediaEdit.getElement().executeJs("this.files=[]");
                    }
                });

                VerticalLayout examContent = new VerticalLayout();
                examContent.setPadding(false);
                examContent.setSpacing(true);
                examContent.setWidthFull();

                Div refertoDisplayContainer = new Div();
                refertoDisplayContainer.setWidthFull();

                Div refertoContainer = new Div();
                refertoContainer.getStyle()
                        .set("background-color", "var(--lumo-shade-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("padding", "var(--lumo-space-m)")
                        .set("margin-top", "var(--lumo-space-m)")
                        .set("border-left", "3px solid var(--lumo-primary-color)")
                        .set("width", "90%")
                        .set("box-sizing", "border-box")
                        .set("margin-left", "auto")
                        .set("margin-right", "auto");

                HorizontalLayout refertoHeader = new HorizontalLayout();
                refertoHeader.setPadding(false);
                refertoHeader.setSpacing(true);
                refertoHeader.setAlignItems(FlexComponent.Alignment.CENTER);
                refertoHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

                Icon refertoIcon = new Icon(VaadinIcon.FILE_TEXT);
                refertoIcon.getStyle().set("color", "var(--lumo-primary-color)");

                H4 refertoTitle = new H4("Referto");
                refertoTitle.getStyle().set("margin", "0").set("color", "var(--lumo-primary-color)");

                HorizontalLayout refertoTitleLayout = new HorizontalLayout(refertoIcon, refertoTitle);
                refertoTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                refertoTitleLayout.setSpacing(true);

                Button editRefertoButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
                editRefertoButton.setTooltipText("Modifica Referto");

                refertoHeader.add(refertoTitleLayout, editRefertoButton);

                String refertoTestuale = esame.getRefertoTestuale();
                if (refertoTestuale == null || refertoTestuale.isEmpty()) {
                    refertoTestuale = "Nessun referto disponibile.";
                }
                Paragraph refertoText = new Paragraph(refertoTestuale);
                refertoText.getStyle()
                        .set("margin", "var(--lumo-space-s) 0 0 0")
                        .set("color", "var(--lumo-body-text-color)")
                        .set("white-space", "pre-wrap")
                        .set("box-sizing", "border-box");

                refertoContainer.add(refertoHeader, refertoText);
                refertoDisplayContainer.add(refertoContainer);

                VerticalLayout refertoEditLayout = new VerticalLayout();
                refertoEditLayout.setWidth("90%");
                refertoEditLayout.getStyle()
                        .set("margin-left", "auto")
                        .set("margin-right", "auto")
                        .set("margin-top", "var(--lumo-space-m)");
                refertoEditLayout.setPadding(false);
                refertoEditLayout.setSpacing(true);
                refertoEditLayout.setVisible(false);

                TextArea editRefertoArea = new TextArea("Modifica Referto");
                editRefertoArea.setWidthFull();
                editRefertoArea.setMinHeight("150px");
                editRefertoArea.setValue(esame.getRefertoTestuale());

                Button saveRefertoButton = StyleApp.getButton("Salva Referto", VaadinIcon.CHECK, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
                Button cancelRefertoButton = StyleApp.getButton("Annulla", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");
                HorizontalLayout refertoEditActions = new HorizontalLayout(saveRefertoButton, cancelRefertoButton);
                refertoEditActions.setSpacing(true);

                refertoEditLayout.add(editRefertoArea, refertoEditActions);

                editRefertoButton.addClickListener(ev -> {
                    refertoDisplayContainer.setVisible(false);
                    refertoEditLayout.setVisible(true);
                    editRefertoArea.setValue(esame.getRefertoTestuale());
                });

                cancelRefertoButton.addClickListener(ev -> {
                    refertoEditLayout.setVisible(false);
                    refertoDisplayContainer.setVisible(true);
                });

                saveRefertoButton.addClickListener(ev -> {
                    String nuovoReferto = editRefertoArea.getValue();

                    boolean updated = esameRefertoService.updateRefertoTestuale(esame.getIdEsame(), scenarioId, nuovoReferto);
                    if (updated) {
                        esame.setRefertoTestuale(nuovoReferto);
                        refertoText.setText(nuovoReferto);
                        Notification.show("Referto aggiornato con successo.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        refertoEditLayout.setVisible(false);
                        refertoDisplayContainer.setVisible(true);
                    } else {
                        Notification.show("Errore durante l'aggiornamento del referto.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });

                examContent.add(refertoDisplayContainer, refertoEditLayout);
                examContent.setAlignItems(FlexComponent.Alignment.CENTER);
                examCard.add(mediaSectionContainer, examContent);

                layout.add(examCard);
            }
        } else {
            Div errorDiv = EmptySupport.createErrorContent("Nessun esame disponibile");
            layout.add(errorDiv);
        }

        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.setWidthFull();
        buttonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button addNewExamButton = StyleApp.getButton("Aggiungi Nuovo Esame", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addNewExamButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
        addNewExamButton.getStyle().set("background-color", "var(--lumo-success-color");
        addNewExamButton.addClickListener(ev -> UI.getCurrent().navigate("esamiReferti/" + scenarioId + "/edit"));
        buttonContainer.add(addNewExamButton);

        HorizontalLayout buttonContainer2 = new HorizontalLayout();
        buttonContainer2.setWidthFull();
        buttonContainer2.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);


        Button createExamButton = StyleApp.getButton("Crea Esami di laboratorio", VaadinIcon.CLIPBOARD_TEXT, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        createExamButton.addClickListener(event -> {
            final String notificationId = activeNotifierManager.show("Generazione esami di laboratorio in corso...");
            final UI ui = UI.getCurrent();

            executorService.submit(() -> {
                try {
                    LabExamGenerationRequest request = new LabExamGenerationRequest(
                            scenario.getDescrizione(),
                            scenario.getTipologia(),
                            esameFisicoService.getEsameFisicoById(scenarioId).toString(),
                            scenario.getPatologia()
                    );
                    Optional<LabExamSet> labExamSetOptional = externalApiService.generateLabExamsFromScenario(request);

                    if (labExamSetOptional.isPresent()) {
                        boolean success = labExamService.saveLabExamsAndGeneratePdf(scenarioId, labExamSetOptional.get());
                        if (success) {

                            notifierService.notify(ui, new NotifierService.NotificationPayload(
                                    NotifierService.Status.SUCCESS,
                                    "Generazione Completata",
                                    "Esami di laboratorio creati con successo!",
                                    notificationId
                            ));
                        } else {

                            notifierService.notify(ui, new NotifierService.NotificationPayload(
                                    NotifierService.Status.ERROR,
                                    "Errore di Salvataggio",
                                    "Fallimento durante il salvataggio degli esami di laboratorio.",
                                    notificationId
                            ));
                        }
                    } else {

                        notifierService.notify(ui, new NotifierService.NotificationPayload(
                                NotifierService.Status.ERROR,
                                "Errore Servizio AI",
                                "Il servizio AI per gli esami non ha risposto.",
                                notificationId
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Fallimento nel task di generazione esami in background.", e);
                    String errorTitle = "Errore Critico";
                    String errorDetails;
                    if (e instanceof HttpClientErrorException hcee) {
                        errorTitle = "Errore nella Richiesta";
                        errorDetails = extractErrorReasonFromJson(hcee.getResponseBodyAsString(), gson);
                    } else {
                        errorDetails = "Si è verificato un problema tecnico. Controllare i log per maggiori dettagli.";
                    }

                    notifierService.notify(ui, new NotifierService.NotificationPayload(
                            NotifierService.Status.ERROR,
                            errorTitle,
                            errorDetails,
                            notificationId
                    ));
                }
            });
        });


        Button createRefertoButton = StyleApp.getButton("Crea Referti per esami", VaadinIcon.CLIPBOARD_PULSE, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        createRefertoButton.addClickListener(event -> {
            Dialog selectExamTypeDialog = new Dialog();
            selectExamTypeDialog.setCloseOnEsc(true);
            selectExamTypeDialog.setCloseOnOutsideClick(true);
            selectExamTypeDialog.setHeaderTitle("Seleziona Tipo Esame per Referto");

            ComboBox<String> examTypeComboBox = new ComboBox<>("Tipo di Esame Strumentale");
            examTypeComboBox.setItems(ALLINSTREXAMS);
            examTypeComboBox.setWidth("100%");
            examTypeComboBox.setPlaceholder("Seleziona un tipo di esame...");

            Button generateButton = new Button("Genera Referto", VaadinIcon.CHECK.create());
            generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            generateButton.setEnabled(false);
            examTypeComboBox.addValueChangeListener(e -> generateButton.setEnabled(e.getValue() != null && !e.getValue().isEmpty()));

            generateButton.addClickListener(e -> {
                final String selectedExamType = examTypeComboBox.getValue();
                selectExamTypeDialog.close();
                final String notificationId = activeNotifierManager.show("Generazione referto per '" + selectedExamType + "' in corso...");
                final UI ui = UI.getCurrent();

                executorService.submit(() -> {
                    try {
                        ReportGenerationRequest request = new ReportGenerationRequest(
                                scenario.getDescrizione(),
                                scenario.getTipologia(),
                                selectedExamType,
                                esameFisicoService.getEsameFisicoById(scenarioId).toString());
                        Optional<ReportSet> refertoContent = externalApiService.generateReport(request);

                        if (refertoContent.isPresent()) {
                            boolean success = esameRefertoService.createRefertoByJSON(scenarioId, refertoContent);
                            if (success) {

                                notifierService.notify(ui, new NotifierService.NotificationPayload(
                                        NotifierService.Status.SUCCESS,
                                        "Generazione Completata",
                                        "Nuovo referto per '" + selectedExamType + "' creato con successo!",
                                        notificationId
                                ));
                            } else {

                                notifierService.notify(ui, new NotifierService.NotificationPayload(
                                        NotifierService.Status.ERROR,
                                        "Errore di Salvataggio",
                                        "Fallimento durante il salvataggio del referto per '" + selectedExamType + "'.",
                                        notificationId
                                ));
                            }
                        } else {

                            notifierService.notify(ui, new NotifierService.NotificationPayload(
                                    NotifierService.Status.ERROR,
                                    "Errore Servizio AI",
                                    "Il servizio AI per i referti non ha risposto.",
                                    notificationId
                            ));
                        }
                    } catch (Exception ex) {
                        logger.error("Errore nel task di generazione referto.", ex);
                        String errorTitle = "Errore Critico";
                        String errorDetails;
                        if (ex instanceof HttpClientErrorException hcee) {
                            errorTitle = "Errore nella Richiesta";
                            errorDetails = extractErrorReasonFromJson(hcee.getResponseBodyAsString(), gson);
                        } else {
                            errorDetails = "Si è verificato un problema tecnico. Controllare i log per maggiori dettagli.";
                        }

                        notifierService.notify(ui, new NotifierService.NotificationPayload(
                                NotifierService.Status.ERROR,
                                errorTitle,
                                errorDetails,
                                notificationId
                        ));
                    }
                });
            });

            Button cancelButton = new Button("Annulla", VaadinIcon.CLOSE.create());
            cancelButton.addClickListener(e -> selectExamTypeDialog.close());
            HorizontalLayout dialogActions = new HorizontalLayout(generateButton, cancelButton);
            dialogActions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            dialogActions.setWidthFull();
            selectExamTypeDialog.add(examTypeComboBox, dialogActions);
            selectExamTypeDialog.setWidth("400px");
            selectExamTypeDialog.open();
        });

        buttonContainer2.add(createExamButton, createRefertoButton);
        layout.add(buttonContainer, buttonContainer2);
        return layout;
    }

    private static Component createMediaPreview(String fileName) {
        String fileExtension;
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            logger.warn("Impossibile determinare l'estensione del file per l'anteprima: {}", fileName);
            return createErrorPreview("Tipo file non riconosciuto: " + fileName);
        }

        logger.debug("Creazione anteprima media per file: {}, estensione: {}", fileName, fileExtension);

        Div previewContainer = new Div();
        previewContainer.setWidthFull();
        previewContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin", "var(--lumo-space-s) 0")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
                .set("box-sizing", "border-box");


        previewContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        " this.style.transform = 'translateY(-2px)'; " +
                        " this.style.boxShadow = '0 6px 15px rgba(0, 0, 0, 0.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        " this.style.transform = 'translateY(0)'; " +
                        " this.style.boxShadow = '0 3px 10px rgba(0, 0, 0, 0.08)'; " +
                        "});"
        );

        HorizontalLayout mediaHeader = new HorizontalLayout();
        mediaHeader.setWidthFull();
        mediaHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        mediaHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        mediaHeader.setPadding(false);
        mediaHeader.setSpacing(true);
        mediaHeader.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        Span fileTypeLabel = new Span(fileExtension.toUpperCase());
        fileTypeLabel.getStyle()
                .set("background-color", getColorForFileType(fileExtension))
                .set("color", "white")
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("text-transform", "uppercase");

        Span fileNameLabel = new Span(getShortFileName(fileName));
        fileNameLabel.getStyle()
                .set("margin-left", "var(--lumo-space-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("max-width", "200px");

        Div mediaContentContainer = new Div();
        mediaContentContainer.setWidthFull();
        mediaContentContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("min-height", "200px")
                .set("max-width", "800px")
                .set("margin", "0 auto")
                .set("box-sizing", "border-box");

        String mediaPath = "/" + fileName;
        logger.debug("Percorso media per anteprima: {}", mediaPath);

        Component mediaComponent;
        Icon typeIcon = getIconForFileType(fileExtension);

        Button fullscreenButton = new Button("Apri in una nuova pagina", new Icon(VaadinIcon.EXPAND_FULL));
        fullscreenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fullscreenButton.getStyle()
                .set("border-radius", "30px")
                .set("margin-top", "var(--lumo-space-m)")
                .set("transition", "transform 0.2s ease")
                .set("cursor", "pointer");


        fullscreenButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.transform = 'scale(1.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.transform = 'scale(1)'; " +
                        "});"
        );

        fullscreenButton.addClassName("hover-effect");
        fullscreenButton.addClickListener(e -> openFullMedia(fileName));


        switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp":
                Image image = new Image(mediaPath, fileName);
                image.setMaxWidth("100%");
                image.setHeight("auto");
                image.getStyle()
                        .set("max-height", "320px")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("object-fit", "contain")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
                mediaComponent = image;
                break;

            case "pdf":
                IFrame pdfPreview = new IFrame();
                pdfPreview.setSrc(mediaPath);
                pdfPreview.setWidth("100%");
                pdfPreview.setHeight("500px");
                pdfPreview.getStyle()
                        .set("border", "none")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
                mediaComponent = pdfPreview;
                break;

            case "mp4", "webm", "mov":
                Div videoContainer = new Div();
                videoContainer.getStyle()
                        .set("width", "100%")
                        .set("position", "relative")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("overflow", "hidden")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

                NativeVideo video = new NativeVideo();
                video.setSrc(mediaPath);
                video.setControls(true);
                video.setWidth("100%");
                video.getStyle().set("display", "block");

                videoContainer.add(video);
                mediaComponent = videoContainer;
                break;

            case "mp3", "wav", "ogg":
                Div audioContainer = new Div();
                audioContainer.getStyle()
                        .set("width", "100%")
                        .set("padding", "var(--lumo-space-m)")
                        .set("background-color", "var(--lumo-shade-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("align-items", "center")
                        .set("box-sizing", "border-box");

                Icon musicIcon = new Icon(VaadinIcon.MUSIC);
                musicIcon.setSize("3em");
                musicIcon.getStyle()
                        .set("color", "var(--lumo-primary-color)")
                        .set("margin-bottom", "var(--lumo-space-s)");

                NativeAudio audio = new NativeAudio();
                audio.setSrc(mediaPath);
                audio.setControls(true);
                audio.setWidth("100%");

                audioContainer.add(musicIcon, audio);
                mediaComponent = audioContainer;
                break;

            default:

                Div unknownContainer = new Div();
                unknownContainer.getStyle()
                        .set("padding", "var(--lumo-space-l)")
                        .set("text-align", "center")
                        .set("width", "100%")
                        .set("box-sizing", "border-box");

                Icon fileIcon = new Icon(VaadinIcon.FILE_O);
                fileIcon.setSize("4em");
                fileIcon.getStyle()
                        .set("color", "var(--lumo-contrast-50pct)")
                        .set("margin-bottom", "var(--lumo-space-m)");

                Span message = new Span("Anteprima non disponibile per: " + fileExtension.toUpperCase());
                message.getStyle()
                        .set("display", "block")
                        .set("color", "var(--lumo-secondary-text-color)");

                unknownContainer.add(fileIcon, message);
                mediaComponent = unknownContainer;
                break;
        }

        HorizontalLayout fileInfoLayout = new HorizontalLayout(typeIcon, fileTypeLabel, fileNameLabel);
        fileInfoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        fileInfoLayout.setSpacing(true);

        mediaHeader.removeAll();
        mediaHeader.add(fileInfoLayout);

        mediaContentContainer.add(mediaComponent);

        previewContainer.add(mediaHeader, mediaContentContainer, fullscreenButton);
        return previewContainer;
    }

    private static Component createErrorPreview(String message) {
        Div errorContainer = new Div();
        errorContainer.getStyle()
                .set("width", "100%")
                .set("padding", "var(--lumo-space-l)")
                .set("text-align", "center")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("margin", "var(--lumo-space-s) 0")
                .set("box-sizing", "border-box");

        Icon errorIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
        errorIcon.setSize("3em");
        errorIcon.getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("margin-bottom", "var(--lumo-space-m)");

        Paragraph errorMessage = new Paragraph(message);
        errorMessage.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin", "0");
        errorContainer.add(errorIcon, errorMessage);
        return errorContainer;
    }

    private static String getShortFileName(String fileName) {
        String shortName = fileName;
        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash > -1 && lastSlash < fileName.length() - 1) {
            shortName = fileName.substring(lastSlash + 1);
        }

        return shortName.length() > 30 ? shortName.substring(0, 27) + "..." : shortName;
    }

    private static Icon getIconForFileType(String fileExtension) {
        Icon icon;
        switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> {
                icon = new Icon(VaadinIcon.PICTURE);
                icon.getStyle().set("color", "var(--lumo-primary-color)");
            }
            case "pdf" -> {
                icon = new Icon(VaadinIcon.FILE_TEXT);
                icon.getStyle().set("color", "var(--lumo-error-color)");
            }
            case "mp4", "webm", "mov" -> {
                icon = new Icon(VaadinIcon.FILM);
                icon.getStyle().set("color", "var(--lumo-success-color)");
            }
            case "mp3", "wav", "ogg" -> {
                icon = new Icon(VaadinIcon.MUSIC);
                icon.getStyle().set("color", "var(--lumo-warning-color)");
            }
            default -> {
                icon = new Icon(VaadinIcon.FILE_O);
                icon.getStyle().set("color", "var(--lumo-contrast-50pct)");
            }
        }
        return icon;
    }

    private static String getColorForFileType(String fileExtension) {
        return switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> "var(--lumo-primary-color)";
            case "pdf" -> "var(--lumo-error-color)";
            case "mp4", "webm", "mov" -> "var(--lumo-success-color)";
            case "mp3", "wav", "ogg" -> "var(--lumo-warning-color)";
            default -> "var(--lumo-contrast-50pct)";
        };
    }

    private static void openFullMedia(String fileName) {
        logger.debug("Opening full media for file: {}", fileName);
        UI.getCurrent().getPage().open("media/" + fileName, "_blank");
    }

    private static class NativeVideo extends Component {
        public NativeVideo() {
            super(new Element("video"));
        }

        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        public void setWidth(String width) {
            getElement().setAttribute("width", width);
        }
    }

    private static class NativeAudio extends Component {
        public NativeAudio() {
            super(new Element("audio"));
        }

        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        public void setWidth(String width) {
            getElement().getStyle().set("width", width);
        }
    }
}