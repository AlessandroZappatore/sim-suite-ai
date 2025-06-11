package it.uniupo.simnova.views.ui.helper;

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
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.vaadin.tinymce.TinyMce;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Classe di utility per la creazione di sezioni di overview dello scenario.
 * Gestisce la visualizzazione e la modifica di informazioni generali, azioni chiave e materiale necessario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class GeneralSupport extends HorizontalLayout {

    /**
     * Costruttore privato per evitare istanziazioni dirette.
     */
    private GeneralSupport() {
        // Costruttore privato per evitare istanziazioni dirette
    }

    /**
     * Crea e popola un layout verticale con le informazioni generali di uno scenario.
     * Include sezioni modificabili per descrizione, briefing, informazioni dai genitori (se pediatriche),
     * patto d'aula, azioni chiave, obiettivi didattici, moulage, liquidi e farmaci, e materiale necessario.
     *
     * @param scenario            L'oggetto {@link Scenario} da cui recuperare i dati.
     * @param isPediatricScenario Flag che indica se lo scenario è di tipo pediatrico, per mostrare info specifiche.
     * @param infoGenitore        Contenuto delle informazioni dai genitori.
     * @param scenarioService     Il servizio per la gestione dello scenario.
     * @param materialeService    Il servizio per la gestione del materiale.
     * @param azioneChiaveService Il servizio per la gestione delle azioni chiave.
     * @return Un {@link VerticalLayout} contenente tutte le sezioni di overview.
     */
    public static VerticalLayout createOverviewContentWithData(
            Scenario scenario,
            boolean isPediatricScenario,
            String infoGenitore,
            ScenarioService scenarioService,
            MaterialeService materialeService,
            AzioneChiaveService azioneChiaveService) {

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

        // Aggiunge effetti di hover al contenitore della card
        card.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-l)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-m)'; });"
        );

        VerticalLayout cardContentLayout = new VerticalLayout();
        cardContentLayout.setPadding(false);
        cardContentLayout.setSpacing(false);
        cardContentLayout.setWidthFull();

        // Aggiunge le varie sezioni informative (descrizione, briefing, etc.)
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

        addMaterialeNecessarioItem(scenario.getId(), cardContentLayout, materialeService);

        card.add(cardContentLayout);
        mainLayout.add(card);
        return mainLayout;
    }

    /**
     * Aggiunge una sezione informativa modificabile al layout, se il contenuto non è vuoto.
     * Questa versione supporta l'aggiornamento tramite {@link ScenarioService}.
     *
     * @param scenarioId      L'ID dello scenario.
     * @param container       Il layout verticale a cui aggiungere la sezione.
     * @param title           Il titolo della sezione.
     * @param content         Il contenuto testuale della sezione.
     * @param iconType        L'icona da visualizzare accanto al titolo.
     * @param isFirstItem     Indica se è il primo elemento aggiunto per gestire i divisori.
     * @param scenarioService Il servizio per aggiornare il contenuto.
     */
    private static void addInfoItemIfNotEmpty(Integer scenarioId, VerticalLayout container, String title, String content, VaadinIcon iconType, boolean isFirstItem, ScenarioService scenarioService) {
        // Aggiunge un divisore se non è il primo elemento
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
                .set("white-space", "pre-wrap") // Mantiene la formattazione (es. a capo)
                .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                .set("width", "100%")
                .set("box-sizing", "border-box");
        if (content == null || content.trim().isEmpty()) {
            contentDisplay.setText("Sezione vuota");
            contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
        } else {
            contentDisplay.getElement().setProperty("innerHTML", content.replace("\n", "<br />")); // Converte \n in <br /> per HTML
        }
        itemLayout.add(contentDisplay);

        TinyMce contentEditor = TinyEditor.getEditor(); // Editor TinyMCE per la modifica
        contentEditor.setValue(content == null ? "" : content);
        contentEditor.setVisible(false); // Nascosto di default

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancelButton = new Button("Annulla");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editorActions = new HorizontalLayout(saveButton, cancelButton);
        editorActions.setVisible(false); // Nascosto di default
        editorActions.getStyle()
                .set("margin-top", "var(--lumo-space-xs)")
                .set("padding-left", "calc(var(--lumo-icon-size-m) + var(--lumo-space-m))");

        itemLayout.add(contentEditor, editorActions);

        // Listener per il pulsante "Modifica"
        editButton.addClickListener(e -> {
            contentDisplay.setVisible(false);
            String currentHtml = contentDisplay.getElement().getProperty("innerHTML");
            String editorValue;
            // Prepara il valore per l'editor, convertendo <br /> in \n
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

        // Listener per il pulsante "Salva"
        saveButton.addClickListener(e -> {
            String newContent = contentEditor.getValue();
            boolean success;

            // Aggiorna il contenuto tramite il servizio appropriato
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
                    // Ripristina lo stato precedente
                    contentEditor.setVisible(false);
                    editorActions.setVisible(false);
                    contentDisplay.setVisible(true);
                    editButton.setVisible(true);
                    return;
            }

            if (success) {
                // Aggiorna la visualizzazione del contenuto
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
            // Nasconde l'editor e mostra il display
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        // Listener per il pulsante "Annulla"
        cancelButton.addClickListener(e -> {
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        container.add(itemLayout);
    }

    /**
     * Aggiunge una sezione per la gestione delle azioni chiave.
     * Permette di visualizzare le azioni come lista e di modificarle tramite un editor dinamico.
     *
     * @param scenarioId          L'ID dello scenario.
     * @param container           Il layout a cui aggiungere la sezione.
     * @param azioneChiaveService Il servizio per la gestione delle azioni chiave.
     */
    private static void addAzioniChiaveItem(
            Integer scenarioId,
            VerticalLayout container,
            AzioneChiaveService azioneChiaveService
    ) {
        final String TITLE = "Azioni Chiave";
        final VaadinIcon ICON_TYPE = VaadinIcon.KEY;

        // Aggiunge un divisore se non è il primo elemento
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
                .set("box-sizing", "border-sizing"); // Correzione da "border-box" a "border-sizing" se è typo

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

        List<TextField> actionFieldsList = new ArrayList<>(); // Lista per tenere traccia dei TextField delle azioni
        AtomicInteger actionCounter = new AtomicInteger(1); // Contatore per il numero delle azioni

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

        // Runnable per caricare e visualizzare le azioni chiave
        Runnable loadAndDisplayCurrentAzioni = () -> {
            List<String> currentAzioni = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);
            if (currentAzioni == null || currentAzioni.isEmpty()) {
                contentDisplay.setText("Sezione vuota");
                contentDisplay.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
            } else {
                String textContent = currentAzioni.stream()
                        .map(azione -> "• " + azione) // Formatta come lista puntata
                        .collect(Collectors.joining("\n"));
                contentDisplay.getElement().setProperty("innerHTML", textContent.replace("\n", "<br />"));
                contentDisplay.getStyle().remove("color");
                contentDisplay.getStyle().remove("font-style");
            }
        };

        loadAndDisplayCurrentAzioni.run(); // Esegue al caricamento iniziale
        itemLayout.add(contentDisplay, editorLayout, editorActions);

        // Listener per il pulsante "Modifica" delle azioni chiave
        editButton.addClickListener(e -> {
            contentDisplay.setVisible(false);
            editButton.setVisible(false);

            actionFieldsContainer.removeAll(); // Pulisce i campi esistenti
            actionFieldsList.clear(); // Resetta la lista dei campi
            actionCounter.set(1); // Resetta il contatore

            List<String> currentAzioni = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);
            if (currentAzioni != null && !currentAzioni.isEmpty()) {
                currentAzioni.forEach(azione ->
                        addNewActionFieldToEditorLayout(actionFieldsContainer, actionFieldsList, azione, actionCounter, azioneChiaveService, scenarioId)
                );
            }

            if (actionFieldsList.isEmpty()) {
                // Aggiunge almeno un campo vuoto se non ci sono azioni
                addNewActionFieldToEditorLayout(actionFieldsContainer, actionFieldsList, "", actionCounter, azioneChiaveService, scenarioId);
            }

            editorLayout.setVisible(true);
            editorActions.setVisible(true);
        });

        // Listener per il pulsante "Salva" delle azioni chiave
        saveButton.addClickListener(e -> {
            List<String> newActionValues = actionFieldsList.stream()
                    .map(TextField::getValue)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty()) // Ignora campi vuoti
                    .distinct() // Rimuove duplicati
                    .collect(Collectors.toList());

            boolean success = azioneChiaveService.updateAzioniChiaveForScenario(scenarioId, newActionValues);

            if (success) {
                loadAndDisplayCurrentAzioni.run(); // Ricarica e aggiorna la visualizzazione
                Notification.show(TITLE + " aggiornate.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Errore durante il salvataggio di " + TITLE + ".", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            // Nasconde l'editor e mostra il display
            editorLayout.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        // Listener per il pulsante "Annulla" delle azioni chiave
        cancelButton.addClickListener(e -> {
            editorLayout.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        container.add(itemLayout);
    }

    /**
     * Aggiunge un nuovo campo di testo per un'azione chiave all'editor.
     *
     * @param actionFieldsContainer  Il layout verticale che contiene i campi delle azioni.
     * @param actionFieldsList       La lista dei {@link TextField} per le azioni.
     * @param initialValue           Il valore iniziale del campo.
     * @param actionCounterReference Riferimento atomico al contatore delle azioni.
     * @param azioneChiaveService    Il servizio per la gestione delle azioni chiave.
     * @param scenarioId             L'ID dello scenario.
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
            // La rimozione dal servizio avviene al salvataggio, qui solo rimozione UI per coerenza con save logica.
            // Se si volesse eliminazione istantanea, sarebbe qui la chiamata al servizio.
            azioneChiaveService.deleteAzioneChiaveByName(scenarioId, actionField.getValue().trim());
        });

        fieldLayout.addAndExpand(actionField);
        fieldLayout.add(removeButton);
        actionFieldsContainer.add(fieldLayout);
    }

    /**
     * Aggiunge una sezione per la gestione del materiale necessario.
     * Permette di visualizzare il materiale e di aprirlo in un editor esterno (IFrame).
     *
     * @param scenarioId       L'ID dello scenario.
     * @param container        Il layout a cui aggiungere la sezione.
     * @param materialeService Il servizio per la gestione del materiale.
     */
    private static void addMaterialeNecessarioItem(Integer scenarioId, VerticalLayout container, MaterialeService materialeService) {
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
        Icon icon = new Icon(VaadinIcon.TOOLS);
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        icon.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-xs)");

        H5 titleLabel = new H5("Materiale necessario");
        titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        titleLabel.getStyle().set("font-weight", "600");
        titleGroup.add(icon, titleLabel);

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica Materiale necessario");
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

        // Runnable per aggiornare il display del materiale
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

        updateContentDisplay.run(); // Esegue al caricamento iniziale
        itemLayout.add(contentDisplay);

        IFrame iframe = new IFrame(); // IFrame per l'editor esterno
        iframe.setWidth("100%");
        iframe.setHeight("600px");
        iframe.setVisible(false); // Nascosto di default
        iframe.getStyle().set("border", "none");

        Button closeIframeButton = StyleApp.getButton("Chiudi Editor", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color)");
        closeIframeButton.setVisible(false);

        HorizontalLayout iframeControls = new HorizontalLayout(closeIframeButton);
        iframeControls.setWidthFull();
        iframeControls.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        iframeControls.setVisible(false);

        itemLayout.add(iframe, iframeControls);

        // Listener per il pulsante "Modifica" del materiale
        editButton.addClickListener(e -> {
            String url = "materialeNecessario/" + scenarioId + "/edit"; // URL dell'editor del materiale
            iframe.setSrc(url);
            iframe.setVisible(true);
            closeIframeButton.setVisible(true);
            iframeControls.setVisible(true);
            contentDisplay.setVisible(false);
            editButton.setVisible(false);
            Notification.show("Apertura editor materiali.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        // Listener per il pulsante "Chiudi Editor" dell'IFrame
        closeIframeButton.addClickListener(e -> {
            iframe.setVisible(false);
            closeIframeButton.setVisible(false);
            iframeControls.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
            updateContentDisplay.run(); // Aggiorna il display dopo la chiusura dell'editor
            Notification.show("Editor materiali chiuso e contenuto aggiornato.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        container.add(itemLayout);
    }

    /**
     * Overload del metodo {@code addInfoItemIfNotEmpty} che imposta {@code isFirstItem} a {@code false}.
     * Usato per aggiungere elementi successivi al primo, con divisore.
     *
     * @param scenarioId      L'ID dello scenario.
     * @param container       Il layout a cui aggiungere la sezione.
     * @param title           Il titolo della sezione.
     * @param content         Il contenuto testuale della sezione.
     * @param iconType        L'icona da visualizzare accanto al titolo.
     * @param scenarioService Il servizio per aggiornare il contenuto.
     */
    private static void addInfoItemIfNotEmpty(Integer scenarioId, VerticalLayout container, String title, String content, VaadinIcon iconType, ScenarioService scenarioService) {
        addInfoItemIfNotEmpty(scenarioId, container, title, content, iconType, false, scenarioService);
    }
}