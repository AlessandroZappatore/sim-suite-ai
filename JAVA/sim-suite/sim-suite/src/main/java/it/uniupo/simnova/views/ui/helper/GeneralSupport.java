package it.uniupo.simnova.views.ui.helper;

import com.google.gson.Gson;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.respons_model.MatSet;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.ActiveNotifierManager;
import it.uniupo.simnova.service.NotifierService;
import it.uniupo.simnova.service.ai_api.ExternalApiService;
import it.uniupo.simnova.service.ai_api.model.MatGenerationRequest;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.vaadin.tinymce.TinyMce;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static it.uniupo.simnova.views.ui.helper.support.ErrorExtractor.extractErrorReasonFromJson;

/**
 * Classe di supporto generale per la visualizzazione e modifica delle informazioni di un scenario.
 * Fornisce metodi per creare contenuti di overview, gestire azioni chiave e materiali necessari.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class GeneralSupport extends HorizontalLayout {
    /**
     * Logger per il tracciamento delle operazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(GeneralSupport.class);
    /**
     * Gson per la serializzazione e deserializzazione di oggetti JSON.
     */
    private static final Gson gson = new Gson();

    /**
     * Costruttore privato per evitare l'istanza della classe.
     * Utilizzare i metodi statici per accedere alle funzionalità.
     */
    private GeneralSupport() {
    }

    /**
     * Crea il layout principale per la visualizzazione delle informazioni di overview di uno scenario.
     *
     * @param scenario              lo scenario da visualizzare
     * @param isPediatricScenario   indica se lo scenario è pediatrico
     * @param infoGenitore          informazioni aggiuntive dai genitori, se disponibili
     * @param scenarioService       servizio per la gestione degli scenari
     * @param materialeService      servizio per la gestione dei materiali
     * @param azioneChiaveService   servizio per la gestione delle azioni chiave
     * @param executorService       servizio per l'esecuzione di task in background
     * @param notifierService       servizio per la gestione delle notifiche
     * @param esameFisicoService    servizio per la gestione degli esami fisici
     * @param externalApiService    servizio per l'interazione con API esterne
     * @param activeNotifierManager gestore per le notifiche attive
     * @return un layout verticale contenente le informazioni di overview dello scenario
     */
    public static VerticalLayout createOverviewContentWithData(
            Scenario scenario,
            boolean isPediatricScenario,
            String infoGenitore,
            ScenarioService scenarioService,
            MaterialeService materialeService,
            AzioneChiaveService azioneChiaveService,
            ExecutorService executorService,
            NotifierService notifierService,
            EsameFisicoService esameFisicoService,
            ExternalApiService externalApiService,
            ActiveNotifierManager activeNotifierManager) {

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(false);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Div card = new Div();
        card.addClassName("info-card");
        card.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "var(--lumo-space-l)")
                .set("background-color", "var(--lumo-base-color)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("width", "100%")
                .set("max-width", "800px")
                .set("margin", "var(--lumo-space-l) 0");

        card.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-l)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-m)'; });"
        );

        VerticalLayout cardContentLayout = new VerticalLayout();
        cardContentLayout.setPadding(false);
        cardContentLayout.setSpacing(false);
        cardContentLayout.setWidthFull();

        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Descrizione", scenario.getDescrizione(), VaadinIcon.PENCIL, true, scenarioService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Briefing", scenario.getBriefing(), VaadinIcon.GROUP, scenarioService);

        if (isPediatricScenario) {
            addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Informazioni dai genitori", infoGenitore, VaadinIcon.FAMILY, scenarioService);
        }

        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Patto Aula", scenario.getPattoAula(), VaadinIcon.HANDSHAKE, scenarioService);
        addAzioniChiaveItem(scenario.getId(), cardContentLayout, azioneChiaveService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Obiettivi Didattici", scenario.getObiettivo(), VaadinIcon.BOOK, scenarioService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Moulage", scenario.getMoulage(), VaadinIcon.EYE, scenarioService);
        addInfoItemIfNotEmpty(scenario.getId(), cardContentLayout, "Liquidi e dosi farmaci", scenario.getLiquidi(), VaadinIcon.DROP, scenarioService);
        addMaterialeNecessarioItem(scenario.getId(), cardContentLayout, materialeService, executorService, notifierService, scenario, esameFisicoService, externalApiService, activeNotifierManager);

        card.add(cardContentLayout);
        mainLayout.add(card);
        return mainLayout;
    }

    /**
     * Aggiunge un elemento informativo al layout se il contenuto non è vuoto.
     *
     * @param scenarioId      l'ID dello scenario a cui appartiene l'informazione
     * @param container       il layout in cui aggiungere l'elemento
     * @param title           il titolo dell'elemento informativo
     * @param content         il contenuto dell'elemento informativo
     * @param iconType        il tipo di icona da visualizzare
     * @param isFirstItem     indica se l'elemento è il primo della lista (per gestire i divider)
     * @param scenarioService servizio per la gestione degli scenari
     */
    private static void addInfoItemIfNotEmpty(Integer scenarioId, VerticalLayout container, String title, String content, VaadinIcon iconType, boolean isFirstItem, ScenarioService scenarioService) {
        if (!isFirstItem && container.getComponentCount() > 0) {
            Hr divider = new Hr();
            divider.getStyle()
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-bottom", "var(--lumo-space-s)")
                    .set("border-color", "var(--lumo-contrast-10pct)");
            container.add(divider);
        }

        VerticalLayout itemLayout = new VerticalLayout();
        itemLayout.setPadding(false);
        itemLayout.setSpacing(false);
        itemLayout.setWidthFull();

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon icon = new Icon(iconType);
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        icon.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-xs)");

        H5 titleLabel = new H5(title);
        titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        titleLabel.getStyle().set("font-weight", "600");
        titleGroup.add(icon, titleLabel);

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica " + title);
        headerRow.add(titleGroup, editButton);
        itemLayout.add(headerRow);

        Div contentDisplay = new Div();
        contentDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-m)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                .set("width", "100%")
                .set("box-sizing", "border-box");
        if (content == null || content.trim().isEmpty()) {
            contentDisplay.setText("Sezione vuota");
            contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
        } else {
            contentDisplay.getElement().setProperty("innerHTML", content.replace("\n", "<br />"));
        }
        itemLayout.add(contentDisplay);

        TinyMce contentEditor = TinyEditor.getEditor();
        contentEditor.setValue(content == null ? "" : content);
        contentEditor.setVisible(false);

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancelButton = new Button("Annulla");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editorActions = new HorizontalLayout(saveButton, cancelButton);
        editorActions.setVisible(false);
        editorActions.getStyle()
                .set("margin-top", "var(--lumo-space-xs)")
                .set("padding-left", "calc(var(--lumo-icon-size-m) + var(--lumo-space-m))");

        itemLayout.add(contentEditor, editorActions);

        editButton.addClickListener(e -> {
            contentDisplay.setVisible(false);
            String currentHtml = contentDisplay.getElement().getProperty("innerHTML");
            String editorValue;
            if (content == null || content.trim().isEmpty() || "Sezione vuota".equals(contentDisplay.getText())) {
                editorValue = "";
            } else {
                editorValue = currentHtml.replace("<br />", "\n").replace("<br>", "\n");
            }
            contentEditor.setValue(editorValue);
            contentEditor.setVisible(true);
            editorActions.setVisible(true);
            editButton.setVisible(false);
        });

        saveButton.addClickListener(e -> {
            String newContent = contentEditor.getValue();
            boolean success;

            switch (title) {
                case "Descrizione":
                    success = scenarioService.updateScenarioDescription(scenarioId, newContent);
                    break;
                case "Briefing":
                    success = scenarioService.updateScenarioBriefing(scenarioId, newContent);
                    break;
                case "Informazioni dai genitori":
                    success = scenarioService.updateScenarioGenitoriInfo(scenarioId, newContent);
                    break;
                case "Patto Aula":
                    success = scenarioService.updateScenarioPattoAula(scenarioId, newContent);
                    break;
                case "Obiettivi Didattici":
                    success = scenarioService.updateScenarioObiettiviDidattici(scenarioId, newContent);
                    break;
                case "Moulage":
                    success = scenarioService.updateScenarioMoulage(scenarioId, newContent);
                    break;
                case "Liquidi e dosi farmaci":
                    success = scenarioService.updateScenarioLiquidi(scenarioId, newContent);
                    break;
                default:
                    Notification.show("Errore: Titolo sezione non riconosciuto.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    contentEditor.setVisible(false);
                    editorActions.setVisible(false);
                    contentDisplay.setVisible(true);
                    editButton.setVisible(true);
                    return;
            }

            if (success) {
                if (newContent == null || newContent.trim().isEmpty()) {
                    contentDisplay.setText("Sezione vuota");
                    contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
                } else {
                    contentDisplay.getElement().setProperty("innerHTML", newContent.replace("\n", "<br />"));
                    contentDisplay.getStyle().remove("color");
                    contentDisplay.getStyle().remove("font-style");
                }
                Notification.show("Sezione '" + title + "' aggiornata.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Errore durante l'aggiornamento della sezione '" + title + "'.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        cancelButton.addClickListener(e -> {
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        container.add(itemLayout);
    }

    /**
     * Aggiunge un elemento per le azioni chiave al layout.
     *
     * @param scenarioId          l'ID dello scenario a cui appartiene l'azione chiave
     * @param container           il layout in cui aggiungere l'elemento
     * @param azioneChiaveService servizio per la gestione delle azioni chiave
     */
    private static void addAzioniChiaveItem(
            Integer scenarioId,
            VerticalLayout container,
            AzioneChiaveService azioneChiaveService
    ) {
        final String TITLE = "Azioni Chiave";
        final VaadinIcon ICON_TYPE = VaadinIcon.KEY;

        if (container.getComponentCount() > 0) {
            Hr divider = new Hr();
            divider.getStyle()
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("margin-bottom", "var(--lumo-space-s)")
                    .set("border-color", "var(--lumo-contrast-10pct)");
            container.add(divider);
        }

        VerticalLayout itemLayout = new VerticalLayout();
        itemLayout.setPadding(false);
        itemLayout.setSpacing(false);
        itemLayout.setWidthFull();

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon icon = new Icon(ICON_TYPE);
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        icon.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-xs)");
        H5 titleLabel = new H5(TITLE);
        titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        titleLabel.getStyle().set("font-weight", "600");
        titleGroup.add(icon, titleLabel);

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica " + TITLE);
        headerRow.add(titleGroup, editButton);
        itemLayout.add(headerRow);

        Div contentDisplay = new Div();
        contentDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-m)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                .set("width", "100%")
                .set("box-sizing", "border-sizing");

        VerticalLayout editorLayout = new VerticalLayout();
        editorLayout.setPadding(false);
        editorLayout.setSpacing(true);
        editorLayout.setWidthFull();
        editorLayout.setVisible(false);
        editorLayout.getStyle().set("padding-left", "calc(var(--lumo-icon-size-m) + var(--lumo-space-m))");

        VerticalLayout actionFieldsContainer = new VerticalLayout();
        actionFieldsContainer.setWidthFull();
        actionFieldsContainer.setPadding(false);
        actionFieldsContainer.setSpacing(true);

        List<TextField> actionFieldsList = new ArrayList<>();
        AtomicInteger actionCounter = new AtomicInteger(1);

        Button addActionButton = new Button("Aggiungi azione chiave", new Icon(VaadinIcon.PLUS_CIRCLE));
        addActionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addActionButton.getStyle().set("margin-top", "var(--lumo-space-s)");
        addActionButton.addClickListener(e ->
                addNewActionFieldToEditorLayout(actionFieldsContainer, actionFieldsList, "", actionCounter, azioneChiaveService, scenarioId)
        );

        editorLayout.add(actionFieldsContainer, addActionButton);

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancelButton = new Button("Annulla");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editorActions = new HorizontalLayout(saveButton, cancelButton);
        editorActions.setVisible(false);
        editorActions.getStyle().set("margin-top", "var(--lumo-space-s)");

        Runnable loadAndDisplayCurrentAzioni = () -> {
            List<String> currentAzioni = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);
            if (currentAzioni == null || currentAzioni.isEmpty()) {
                contentDisplay.setText("Sezione vuota");
                contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
            } else {
                String textContent = currentAzioni.stream()
                        .map(azione -> "• " + azione)
                        .collect(Collectors.joining("\n"));
                contentDisplay.getElement().setProperty("innerHTML", textContent.replace("\n", "<br />"));
                contentDisplay.getStyle().remove("color");
                contentDisplay.getStyle().remove("font-style");
            }
        };

        loadAndDisplayCurrentAzioni.run();
        itemLayout.add(contentDisplay, editorLayout, editorActions);

        editButton.addClickListener(e -> {
            contentDisplay.setVisible(false);
            editButton.setVisible(false);

            actionFieldsContainer.removeAll();
            actionFieldsList.clear();
            actionCounter.set(1);

            List<String> currentAzioni = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);
            if (currentAzioni != null && !currentAzioni.isEmpty()) {
                currentAzioni.forEach(azione ->
                        addNewActionFieldToEditorLayout(actionFieldsContainer, actionFieldsList, azione, actionCounter, azioneChiaveService, scenarioId)
                );
            }

            if (actionFieldsList.isEmpty()) {
                addNewActionFieldToEditorLayout(actionFieldsContainer, actionFieldsList, "", actionCounter, azioneChiaveService, scenarioId);
            }

            editorLayout.setVisible(true);
            editorActions.setVisible(true);
        });

        saveButton.addClickListener(e -> {
            List<String> newActionValues = actionFieldsList.stream()
                    .map(TextField::getValue)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            boolean success = azioneChiaveService.updateAzioniChiaveForScenario(scenarioId, newActionValues);

            if (success) {
                loadAndDisplayCurrentAzioni.run();
                Notification.show(TITLE + " aggiornate.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Errore durante il salvataggio di " + TITLE + ".", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            editorLayout.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        cancelButton.addClickListener(e -> {
            editorLayout.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        container.add(itemLayout);
    }

    /**
     * Aggiunge un nuovo campo di azione chiave al layout dell'editor.
     *
     * @param actionFieldsContainer  il layout in cui aggiungere il campo
     * @param actionFieldsList       la lista dei campi di azione chiave
     * @param initialValue           il valore iniziale del campo (può essere null)
     * @param actionCounterReference riferimento atomico per il conteggio delle azioni
     * @param azioneChiaveService    servizio per la gestione delle azioni chiave
     * @param scenarioId             l'ID dello scenario a cui appartiene l'azione chiave
     */
    private static void addNewActionFieldToEditorLayout(
            VerticalLayout actionFieldsContainer,
            List<TextField> actionFieldsList,
            String initialValue,
            AtomicInteger actionCounterReference,
            AzioneChiaveService azioneChiaveService,
            Integer scenarioId
    ) {
        HorizontalLayout fieldLayout = new HorizontalLayout();
        fieldLayout.setWidthFull();
        fieldLayout.setSpacing(true);
        fieldLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        TextField actionField = new TextField();
        actionField.setLabel("Azione Chiave #" + actionCounterReference.getAndIncrement());
        actionField.setPlaceholder("Inserisci un'azione chiave");
        actionField.setValue(initialValue != null ? initialValue.trim() : "");
        actionField.setWidthFull();

        actionFieldsList.add(actionField);

        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        removeButton.setAriaLabel("Rimuovi azione");
        removeButton.addClickListener(ev -> {
            actionFieldsList.remove(actionField);
            actionFieldsContainer.remove(fieldLayout);
            azioneChiaveService.deleteAzioneChiaveByName(scenarioId, actionField.getValue().trim());
        });

        fieldLayout.addAndExpand(actionField);
        fieldLayout.add(removeButton);
        actionFieldsContainer.add(fieldLayout);
    }

    /**
     * Aggiunge un elemento per i materiali necessari al layout.
     *
     * @param scenarioId            l'ID dello scenario a cui appartiene il materiale necessario
     * @param container             il layout in cui aggiungere l'elemento
     * @param materialeService      servizio per la gestione dei materiali
     * @param executorService       servizio per l'esecuzione di task in background
     * @param notifierService       servizio per la gestione delle notifiche
     * @param scenario              lo scenario a cui appartiene il materiale necessario
     * @param esameFisicoService    servizio per la gestione degli esami fisici
     * @param externalApiService    servizio per l'interazione con API esterne
     * @param activeNotifierManager gestore per le notifiche attive
     */
    private static void addMaterialeNecessarioItem(Integer scenarioId,
                                                   VerticalLayout container,
                                                   MaterialeService materialeService,
                                                   ExecutorService executorService,
                                                   NotifierService notifierService,
                                                   Scenario scenario,
                                                   EsameFisicoService esameFisicoService,
                                                   ExternalApiService externalApiService,
                                                   ActiveNotifierManager activeNotifierManager) {
        if (container.getComponentCount() > 0) {
            Hr divider = new Hr();
            divider.getStyle().set("margin-top", "var(--lumo-space-s)").set("margin-bottom", "var(--lumo-space-s)").set("border-color", "var(--lumo-contrast-10pct)");
            container.add(divider);
        }

        VerticalLayout itemLayout = new VerticalLayout();
        itemLayout.setPadding(false);
        itemLayout.setSpacing(false);
        itemLayout.setWidthFull();

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon icon = new Icon(VaadinIcon.TOOLS);
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        icon.getStyle().set("background-color", "var(--lumo-primary-color-10pct)").set("padding", "var(--lumo-space-s)").set("border-radius", "var(--lumo-border-radius-l)").set("font-size", "var(--lumo-icon-size-m)").set("margin-right", "var(--lumo-space-xs)");
        H5 titleLabel = new H5("Materiale necessario");
        titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        titleLabel.getStyle().set("font-weight", "600");
        titleGroup.add(icon, titleLabel);

        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setPadding(false);
        buttonLayout.setSpacing(false);
        buttonLayout.setWidthFull();
        buttonLayout.setAlignItems(FlexComponent.Alignment.END);

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica Materiale necessario");
        buttonLayout.add(editButton);

        Button aiMaterialButton = StyleApp.getButton("Genera", VaadinIcon.PLUS_CIRCLE, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        aiMaterialButton.setTooltipText("Genera i materiali necessari tramite AI");
        buttonLayout.add(aiMaterialButton);

        headerRow.add(titleGroup, buttonLayout);
        itemLayout.add(headerRow);

        aiMaterialButton.addClickListener(event -> {
            final String notificationId = activeNotifierManager.show("Generazione materiali in corso...");
            final UI ui = UI.getCurrent();

            executorService.submit(() -> {
                try {
                    MatGenerationRequest request = new MatGenerationRequest(
                            scenario.getDescrizione(),
                            scenario.getTipologia(),
                            scenario.getTarget(),
                            esameFisicoService.getEsameFisicoById(scenarioId).toString()
                    );

                    Optional<List<MatSet>> materialiOptional = externalApiService.generateMaterial(request);

                    if (materialiOptional.isPresent()) {
                        boolean success = materialeService.saveAImaterials(scenarioId, materialiOptional.get());
                        if (success) {

                            notifierService.notify(ui, new NotifierService.NotificationPayload(
                                    NotifierService.Status.SUCCESS,
                                    "Generazione Completata",
                                    "Materiali necessari creati e associati!",
                                    notificationId
                            ));
                        } else {

                            notifierService.notify(ui, new NotifierService.NotificationPayload(
                                    NotifierService.Status.ERROR,
                                    "Errore di Salvataggio",
                                    "Errore durante il salvataggio dei materiali.",
                                    notificationId
                            ));
                        }
                    } else {

                        notifierService.notify(ui, new NotifierService.NotificationPayload(
                                NotifierService.Status.ERROR,
                                "Errore Servizio AI",
                                "Il servizio AI per i materiali non ha risposto.",
                                notificationId
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Fallimento nel task di generazione materiali in background.", e);
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

        Div contentDisplay = new Div();
        contentDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-m)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        Runnable updateContentDisplay = () -> {
            String updatedContent = materialeService.toStringAllMaterialsByScenarioId(scenarioId);
            if (updatedContent == null || updatedContent.trim().isEmpty()) {
                contentDisplay.setText("Sezione vuota");
                contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
            } else {
                contentDisplay.getElement().setProperty("innerHTML", updatedContent.replace("\n", "<br />"));
                contentDisplay.getStyle().remove("color");
                contentDisplay.getStyle().remove("font-style");
            }
        };

        updateContentDisplay.run();
        itemLayout.add(contentDisplay);

        IFrame iframe = new IFrame();
        iframe.setWidth("100%");
        iframe.setHeight("600px");
        iframe.setVisible(false);
        iframe.getStyle().set("border", "none");

        Button closeIframeButton = StyleApp.getButton("Chiudi Editor", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");
        closeIframeButton.setVisible(false);

        HorizontalLayout iframeControls = new HorizontalLayout(closeIframeButton);
        iframeControls.setWidthFull();
        iframeControls.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        iframeControls.setVisible(false);

        itemLayout.add(iframe, iframeControls);

        editButton.addClickListener(e -> {
            String url = "materialeNecessario/" + scenarioId + "/edit";
            iframe.setSrc(url);
            iframe.setVisible(true);
            closeIframeButton.setVisible(true);
            iframeControls.setVisible(true);
            contentDisplay.setVisible(false);
            editButton.setVisible(false);
            Notification.show("Apertura editor materiali.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        closeIframeButton.addClickListener(e -> {
            iframe.setVisible(false);
            closeIframeButton.setVisible(false);
            iframeControls.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
            updateContentDisplay.run();
            Notification.show("Editor materiali chiuso e contenuto aggiornato.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        container.add(itemLayout);
    }

    /**
     * Aggiunge un elemento informativo al layout se il contenuto non è vuoto.
     *
     * @param scenarioId      l'ID dello scenario a cui appartiene l'informazione
     * @param container       il layout in cui aggiungere l'elemento
     * @param title           il titolo dell'elemento informativo
     * @param content         il contenuto dell'elemento informativo
     * @param iconType        il tipo di icona da visualizzare
     * @param scenarioService servizio per la gestione degli scenari
     */
    private static void addInfoItemIfNotEmpty(Integer scenarioId, VerticalLayout container, String title, String content, VaadinIcon iconType, ScenarioService scenarioService) {
        addInfoItemIfNotEmpty(scenarioId, container, title, content, iconType, false, scenarioService);
    }
}