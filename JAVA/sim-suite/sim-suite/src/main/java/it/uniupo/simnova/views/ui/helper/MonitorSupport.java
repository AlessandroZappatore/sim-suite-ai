package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Classe di utility per la creazione e gestione di monitor virtuali dei parametri vitali.
 * Supporta la visualizzazione di parametri predefiniti e aggiuntivi,
 * inclusa la gestione di soglie di allarme e la modifica in linea dei valori.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class MonitorSupport {
    /**
     * Logger per il monitoraggio delle operazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(MonitorSupport.class);
    /**
     * Colori predefiniti per i parametri aggiuntivi.
     * Questi colori vengono utilizzati per differenziare visivamente i parametri aggiuntivi nel monitor.
     */
    private static final List<String> ADDITIONAL_PARAM_COLORS = List.of(
            "var(--lumo-contrast-70pct)",
            "var(--lumo-shade-50pct)",
            "var(--lumo-tertiary-color)"
    );
    /**
     * Stile CSS per l'animazione di flash degli alert.
     */
    private static final String FLASH_ANIMATION_CSS =
            "@keyframes flash-outline-anim {" +
                    "  0% { outline: 2px solid transparent; outline-offset: 0px; }" +
                    "  50% { outline: 3px solid var(--lumo-error-color); outline-offset: 2px; }" +
                    "  100% { outline: 2px solid transparent; outline-offset: 0px; }" +
                    "} " +
                    ".flash-alert-box {" +
                    "  animation: flash-outline-anim 1.2s infinite;" +
                    "  border-color: var(--lumo-error-color) !important;" +
                    "}";
    /**
     * ID per lo stile dinamico dell'animazione di flash.
     */
    private static final String FLASH_STYLE_ID = "global-flash-alert-style";
    /**
     * Valore di default per i display vuoti nei parametri vitali.
     * Utilizzato quando un parametro non ha un valore definito o è nullo.
     */
    private static final String NULL_DISPLAY_VALUE = "-";

    /**
     * Costruttore privato per evitare istanziazioni dirette della classe.
     */
    private MonitorSupport() {
        // Costruttore privato per evitare istanziazioni
    }

    /**
     * Converte un {@link Number} in {@link Double}.
     *
     * @param number Il numero da convertire.
     * @return Il valore {@code Double}, o {@code null} se il numero è {@code null}.
     */
    private static Double toDouble(Number number) {
        return number == null ? null : number.doubleValue();
    }

    /**
     * Formatta un valore numerico per la visualizzazione con un formato specifico.
     *
     * @param number Il numero da formattare.
     * @param format Il formato stringa (es. "%.1f" per un decimale).
     * @return La stringa formattata, o {@link #NULL_DISPLAY_VALUE} se il numero è {@code null}.
     */
    private static String formatDisplayValue(Number number, @SuppressWarnings("SameParameterValue") String format) {
        if (number == null) {
            return NULL_DISPLAY_VALUE;
        }
        if (number instanceof Double || number instanceof Float) {
            return String.format(format, number.doubleValue());
        }
        return String.valueOf(number);
    }

    /**
     * Formatta un valore numerico per la visualizzazione.
     *
     * @param number Il numero da formattare.
     * @return La stringa del numero, o {@link #NULL_DISPLAY_VALUE} se il numero è {@code null}.
     */
    private static String formatDisplayValue(Number number) {
        if (number == null) {
            return NULL_DISPLAY_VALUE;
        }
        return String.valueOf(number);
    }

    /**
     * Crea un componente monitor dei parametri vitali.
     * Visualizza i parametri vitali principali (PA, FC, T, RR, SpO2, FiO2, Litri O2, EtCO2)
     * e parametri aggiuntivi, con soglie di allarme e possibilità di modifica.
     *
     * @param dataProvider            Fornitore dei dati dei parametri vitali.
     * @param scenarioId              L'ID dello scenario.
     * @param isT0                    Indica se è la sezione T0 (per mostrare i presidi).
     * @param presidiService          Servizio per la gestione dei presidi.
     * @param pazienteT0Service       Servizio per la gestione del paziente T0.
     * @param advancedScenarioService Servizio per la gestione degli scenari avanzati (per parametri aggiuntivi).
     * @param tempoId                 L'ID del tempo corrente (per i parametri aggiuntivi).
     * @return Un {@link Component} che rappresenta il monitor.
     */
    public static Component createVitalSignsMonitor(VitalSignsDataProvider dataProvider,
                                                    Integer scenarioId,
                                                    boolean isT0,
                                                    PresidiService presidiService,
                                                    PazienteT0Service pazienteT0Service,
                                                    AdvancedScenarioService advancedScenarioService,
                                                    Integer tempoId) {
        // Inietta lo stile CSS per l'animazione di flash una sola volta nell'head del documento
        UI currentUI = UI.getCurrent();
        if (currentUI != null && currentUI.getPage() != null) {
            currentUI.getPage().executeJs(
                    "if (!document.getElementById('" + FLASH_STYLE_ID + "')) {" +
                            "  const style = document.createElement('style');" +
                            "  style.id = '" + FLASH_STYLE_ID + "';" +
                            "  style.textContent = '" + FLASH_ANIMATION_CSS.replace("'", "\\'") + "';" +
                            "  document.head.appendChild(style);" +
                            "}"
            );
        }

        Div monitorContainer = new Div();
        monitorContainer.setWidthFull();
        monitorContainer.getStyle()
                .set("background-color", "var(--lumo-shade-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("border", "2px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                .set("padding", "var(--lumo-space-m)")
                .set("max-width", "700px")
                .set("margin", "0 auto")
                .set("box-sizing", "border-box");

        HorizontalLayout monitorHeader = new HorizontalLayout();
        monitorHeader.setWidthFull();
        monitorHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        monitorHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        monitorHeader.setPadding(false);
        monitorHeader.setSpacing(true);

        H3 monitorTitle = new H3("Parametri Vitali");
        monitorTitle.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-primary-color)")
                .set("font-weight", "600");

        Div statusLed = new Div();
        statusLed.getStyle()
                .set("width", "12px")
                .set("height", "12px")
                .set("background-color", "var(--lumo-success-color)")
                .set("border-radius", "50%")
                .set("box-shadow", "0 0 5px var(--lumo-success-color)")
                .set("box-sizing", "border-box");

        // Aggiunge animazione "pulse" al LED di stato
        if (currentUI != null && currentUI.getPage() != null) {
            statusLed.getElement().executeJs(
                    "this.style.animation = 'pulse 2s infinite';" +
                            "if (!document.getElementById('led-style')) {" +
                            "  const style = document.createElement('style');" +
                            "  style.id = 'led-style';" +
                            "  style.textContent = '@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.6; } 100% { opacity: 1; } }';" +
                            "  document.head.appendChild(style);" +
                            "}"
            );
        }
        monitorHeader.add(monitorTitle, statusLed);

        HorizontalLayout vitalSignsLayout = new HorizontalLayout();
        vitalSignsLayout.setWidthFull();
        vitalSignsLayout.setPadding(false);
        vitalSignsLayout.setSpacing(false);
        vitalSignsLayout.getStyle().set("flex-wrap", "wrap");
        vitalSignsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Creazione dei box per i parametri vitali predefiniti
        if (dataProvider.getPA() != null && !dataProvider.getPA().isEmpty()) {
            vitalSignsLayout.add(createVitalSignBox("PA", dataProvider.getPA(), "mmHg",
                    "var(--lumo-primary-color)", null, null, null, null, null, advancedScenarioService, scenarioId, tempoId, null));
        } else {
            vitalSignsLayout.add(createVitalSignBox("PA", NULL_DISPLAY_VALUE, "mmHg",
                    "var(--lumo-secondary-text-color)", null, null, null, null, null, advancedScenarioService, scenarioId, tempoId, null));
        }

        final Double FC_CRITICAL_LOW = 40.0;
        final Double FC_CRITICAL_HIGH = 130.0;
        final Double FC_WARNING_LOW = 50.0;
        final Double FC_WARNING_HIGH = 110.0;
        vitalSignsLayout.add(createVitalSignBox("FC",
                formatDisplayValue(dataProvider.getFC()), "bpm",
                "var(--lumo-primary-color)", toDouble(dataProvider.getFC()),
                FC_CRITICAL_LOW, FC_CRITICAL_HIGH, FC_WARNING_LOW, FC_WARNING_HIGH, advancedScenarioService, scenarioId, tempoId, null));

        if (dataProvider.getT() != null && dataProvider.getT() > -50) { // Valore -50 come discriminante per temperatura valida
            final double MIN_CRITICAL_TEMP = 35.0;
            final double MAX_CRITICAL_TEMP = 39.0;
            final double MIN_WARNING_TEMP = 36.0;
            final double MAX_WARNING_TEMP = 37.5;
            vitalSignsLayout.add(createVitalSignBox("T",
                    formatDisplayValue(dataProvider.getT(), "%.1f"), "°C",
                    "var(--lumo-success-color)", toDouble(dataProvider.getT()),
                    MIN_CRITICAL_TEMP, MAX_CRITICAL_TEMP, MIN_WARNING_TEMP, MAX_WARNING_TEMP, advancedScenarioService, scenarioId, tempoId, null));
        } else {
            vitalSignsLayout.add(createVitalSignBox("T", NULL_DISPLAY_VALUE, "°C",
                    "var(--lumo-secondary-text-color)", null,
                    null, null, null, null, advancedScenarioService, scenarioId, tempoId, null));
        }

        final Double RR_CRITICAL_LOW = 10.0;
        final Double RR_CRITICAL_HIGH = 30.0;
        final Double RR_WARNING_LOW = 12.0;
        final Double RR_WARNING_HIGH = 25.0;
        vitalSignsLayout.add(createVitalSignBox("RR",
                formatDisplayValue(dataProvider.getRR()), "rpm",
                "var(--lumo-tertiary-color)", toDouble(dataProvider.getRR()),
                RR_CRITICAL_LOW, RR_CRITICAL_HIGH, RR_WARNING_LOW, RR_WARNING_HIGH, advancedScenarioService, scenarioId, tempoId, null));

        final Double SPO2_CRITICAL_LOW = 90.0;
        final Double SPO2_WARNING_LOW = 94.0;
        vitalSignsLayout.add(createVitalSignBox("SpO₂",
                formatDisplayValue(dataProvider.getSpO2()), "%",
                "var(--lumo-contrast)", toDouble(dataProvider.getSpO2()),
                SPO2_CRITICAL_LOW, null, SPO2_WARNING_LOW, null, advancedScenarioService, scenarioId, tempoId, null));

        vitalSignsLayout.add(createVitalSignBox("FiO₂",
                formatDisplayValue(dataProvider.getFiO2()), "%",
                "var(--lumo-primary-color-50pct)", toDouble(dataProvider.getFiO2()),
                null, null, null, null, advancedScenarioService, scenarioId, tempoId, null));

        vitalSignsLayout.add(createVitalSignBox("Litri O₂",
                formatDisplayValue(dataProvider.getLitriO2()), "Litri/m",
                "var(--lumo-contrast-70pct)", toDouble(dataProvider.getLitriO2()),
                null, null, null, null, advancedScenarioService, scenarioId, tempoId, null));

        final Double ETCO2_CRITICAL_LOW = 25.0;
        final Double ETCO2_CRITICAL_HIGH = 60.0;
        final Double ETCO2_WARNING_LOW = 35.0;
        final Double ETCO2_WARNING_HIGH = 45.0;
        vitalSignsLayout.add(createVitalSignBox("EtCO₂",
                formatDisplayValue(dataProvider.getEtCO2()), "mmHg",
                "var(--lumo-warning-color)", toDouble(dataProvider.getEtCO2()),
                ETCO2_CRITICAL_LOW, ETCO2_CRITICAL_HIGH, ETCO2_WARNING_LOW, ETCO2_WARNING_HIGH, advancedScenarioService, scenarioId, tempoId, null));

        // Gestione dei parametri aggiuntivi
        List<ParametroAggiuntivo> additionalParamsList = dataProvider.getAdditionalParameters();
        if (additionalParamsList == null) {
            additionalParamsList = new ArrayList<>();
        }

        final List<ParametroAggiuntivo> finalAdditionalParamsList = additionalParamsList;

        if (!finalAdditionalParamsList.isEmpty()) {
            AtomicInteger colorIndex = new AtomicInteger(0);
            for (ParametroAggiuntivo param : finalAdditionalParamsList) {
                String label = param.getNome();
                String value = param.getValore() != null ? param.getValore() : NULL_DISPLAY_VALUE;
                String unit = param.getUnitaMisura() != null ? param.getUnitaMisura() : "";
                String color = ADDITIONAL_PARAM_COLORS.get(colorIndex.getAndIncrement() % ADDITIONAL_PARAM_COLORS.size());

                // Utilizzo di un array per permettere la modifica del riferimento al box all'interno della lambda
                final Div[] boxHolder = new Div[1];

                // Callback per l'eliminazione di un parametro aggiuntivo
                Runnable onDelete = () -> {
                    ConfirmDialog confirmDialog = new ConfirmDialog(
                            "Conferma Eliminazione",
                            "Sei sicuro di voler eliminare il parametro aggiuntivo '" + param.getNome() + "'?",
                            "Elimina", event -> {
                        try {
                            advancedScenarioService.deleteAdditionalParam(scenarioId, tempoId, param.getNome());
                            if (boxHolder[0] != null) {
                                vitalSignsLayout.remove(boxHolder[0]); // Rimuove il box dalla UI
                            }
                            finalAdditionalParamsList.remove(param); // Rimuove dalla lista
                            Notification.show("Parametro '" + param.getNome() + "' eliminato.", 3000, Notification.Position.BOTTOM_CENTER)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } catch (Exception ex) {
                            logger.error("Errore eliminazione parametro aggiuntivo '{}': {}", param.getNome(), ex.getMessage());
                            Notification.show("Errore eliminazione: " + ex.getMessage(), 3000, Notification.Position.BOTTOM_CENTER)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    },
                            "Annulla", event -> {
                    }
                    );
                    confirmDialog.setConfirmButtonTheme("error primary");
                    confirmDialog.open();
                };

                Div paramBox = createVitalSignBox(label, value, unit, color,
                        null, null, null, null, null, // Nessuna soglia per i custom params
                        advancedScenarioService, scenarioId, tempoId,
                        onDelete
                );
                boxHolder[0] = paramBox;
                vitalSignsLayout.add(paramBox);
            }
        }
        monitorContainer.add(monitorHeader, vitalSignsLayout);

        // Pulsante per aggiungere nuovi parametri aggiuntivi
        Button addParamButton = new Button("Aggiungi Parametro", VaadinIcon.PLUS_CIRCLE_O.create());
        addParamButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        addParamButton.getStyle().set("margin-top", "var(--lumo-space-m)").set("margin-left", "auto").set("margin-right", "auto").set("display", "block");

        addParamButton.addClickListener(e -> showAddAdditionalParamDialog(
                scenarioId,
                tempoId,
                advancedScenarioService,
                finalAdditionalParamsList,
                vitalSignsLayout
        ));
        monitorContainer.add(addParamButton);

        // Sezione per il testo aggiuntivo del monitoraggio
        String additionalText = dataProvider.getAdditionalMonitorText();
        if (additionalText != null && !additionalText.isEmpty()) {
            Div monitorTextContainer = new Div();
            monitorTextContainer.setWidthFull();
            monitorTextContainer.getStyle()
                    .set("margin-top", "var(--lumo-space-m)")
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("box-shadow", "inset 0 1px 3px rgba(0, 0, 0, 0.1)")
                    .set("box-sizing", "border-box");

            HorizontalLayout monitorTextHeader = new HorizontalLayout();
            monitorTextHeader.setWidthFull();
            monitorTextHeader.setAlignItems(FlexComponent.Alignment.CENTER);
            monitorTextHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Icon monitorIcon = new Icon(VaadinIcon.LAPTOP);
            monitorIcon.getStyle()
                    .set("color", "var(--lumo-tertiary-color)")
                    .set("margin-right", "var(--lumo-space-xs)");

            H4 monitorTextTitle = new H4("Monitoraggio");
            monitorTextTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("color", "var(--lumo-tertiary-text-color)");

            Button editMonitorButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color");
            editMonitorButton.setTooltipText("Modifica il monitoraggio");
            HorizontalLayout titleAndIconMonitor = new HorizontalLayout(monitorIcon, monitorTextTitle);
            titleAndIconMonitor.setAlignItems(FlexComponent.Alignment.CENTER);
            titleAndIconMonitor.setSpacing(true);
            monitorTextHeader.add(titleAndIconMonitor, editMonitorButton);

            Span monitorText = new Span(additionalText);
            monitorText.getStyle()
                    .set("display", "block")
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("white-space", "pre-wrap") // Mantiene la formattazione (es. a capo)
                    .set("font-family", "var(--lumo-font-family-monospace)")
                    .set("color", "var(--lumo-body-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("line-height", "1.5");
            monitorTextContainer.add(monitorTextHeader, monitorText);

            TextArea monitorTextArea = new TextArea();
            monitorTextArea.setWidthFull();
            monitorTextArea.setVisible(false); // Nascosto di default
            monitorTextArea.setValue(additionalText);
            Button saveMonitorButton = StyleApp.getButton("Salva", VaadinIcon.CHECK, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color");
            Button cancelMonitorButton = StyleApp.getButton("Annulla", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color");
            HorizontalLayout monitorActions = new HorizontalLayout(saveMonitorButton, cancelMonitorButton);
            monitorActions.setVisible(false); // Nascosto di default
            monitorTextContainer.add(monitorTextArea, monitorActions);

            editMonitorButton.addClickListener(ev -> {
                monitorText.setVisible(false);
                monitorTextArea.setValue(monitorText.getText()); // Popola la textarea con il testo attuale
                monitorTextArea.setVisible(true);
                monitorActions.setVisible(true);
                editMonitorButton.setVisible(false);
            });
            saveMonitorButton.addClickListener(ev -> {
                String newText = monitorTextArea.getValue();
                monitorText.setText(newText);
                pazienteT0Service.saveMonitor(scenarioId, newText); // Salva il testo nel servizio
                monitorText.setVisible(true);
                monitorTextArea.setVisible(false);
                monitorActions.setVisible(false);
                editMonitorButton.setVisible(true);
                Notification.show("Monitoraggio aggiornato.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            cancelMonitorButton.addClickListener(ev -> {
                monitorTextArea.setVisible(false);
                monitorActions.setVisible(false);
                monitorText.setVisible(true);
                editMonitorButton.setVisible(true);
            });

            Div textWrapper = new Div(monitorTextContainer);
            textWrapper.setWidthFull();
            textWrapper.getStyle().set("padding-left", "var(--lumo-space-xs)")
                    .set("padding-right", "var(--lumo-space-xs)");
            monitorContainer.add(textWrapper);
        }

        // Sezione per i presidi utilizzati (visibile solo per T0)
        List<String> presidiList = PresidiService.getPresidiByScenarioId(scenarioId);
        if (isT0) {
            Div presidiOuterContainer = new Div();
            presidiOuterContainer.setWidthFull();
            presidiOuterContainer.getStyle()
                    .set("padding-left", "var(--lumo-space-xs)")
                    .set("padding-right", "var(--lumo-space-xs)");

            Div presidiInnerContainer = new Div();
            presidiInnerContainer.setWidthFull();
            presidiInnerContainer.getStyle()
                    .set("margin-top", "var(--lumo-space-m)")
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("box-shadow", "inset 0 1px 3px rgba(0, 0, 0, 0.1)")
                    .set("box-sizing", "border-box");

            HorizontalLayout presidiHeader = new HorizontalLayout();
            presidiHeader.setWidthFull();
            presidiHeader.setAlignItems(FlexComponent.Alignment.CENTER);
            presidiHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            Icon presidiIcon = new Icon(VaadinIcon.TOOLS);
            presidiIcon.getStyle()
                    .set("color", "var(--lumo-tertiary-color)")
                    .set("margin-right", "var(--lumo-space-xs)");

            H4 presidiTitle = new H4("Presidi Utilizzati");
            presidiTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("color", "var(--lumo-tertiary-text-color)");

            Button editPresidiButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color");
            editPresidiButton.setTooltipText("Modifica i presidi");
            HorizontalLayout titleAndIconPresidi = new HorizontalLayout(presidiIcon, presidiTitle);
            titleAndIconPresidi.setAlignItems(FlexComponent.Alignment.CENTER);
            titleAndIconPresidi.setSpacing(true);
            presidiHeader.add(titleAndIconPresidi, editPresidiButton);

            Div presidiItemsDiv = new Div();
            presidiItemsDiv.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("padding-left", "var(--lumo-space-xs)");
            // Popola la lista dei presidi attuali
            for (String presidio : presidiList) {
                HorizontalLayout itemLayout = new HorizontalLayout();
                itemLayout.setSpacing(false);
                itemLayout.setPadding(false);
                itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                itemLayout.getStyle()
                        .set("margin-bottom", "var(--lumo-space-xxs)");
                Span bulletPoint = new Span("•");
                bulletPoint.getStyle()
                        .set("color", "var(--lumo-tertiary-color)")
                        .set("font-size", "var(--lumo-font-size-m)")
                        .set("line-height", "1")
                        .set("margin-right", "var(--lumo-space-xs)");
                Span presidioSpan = new Span(presidio);
                presidioSpan.getStyle()
                        .set("font-family", "var(--lumo-font-family)")
                        .set("color", "var(--lumo-body-text-color)")
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("line-height", "1.5");
                itemLayout.add(bulletPoint, presidioSpan);
                presidiItemsDiv.add(itemLayout);
            }
            presidiInnerContainer.add(presidiHeader, presidiItemsDiv);

            // ComboBox per la modifica dei presidi (multi-selezione)
            List<String> allPresidi = PresidiService.getAllPresidi();
            MultiSelectComboBox<String> presidiComboBox = new MultiSelectComboBox<>();
            presidiComboBox.setItems(allPresidi);
            presidiComboBox.setWidthFull();
            presidiComboBox.setVisible(false); // Nascosto di default
            presidiComboBox.setValue(Set.copyOf(presidiList)); // Imposta i valori attuali

            Button savePresidiButton = StyleApp.getButton("Salva", VaadinIcon.CHECK, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color");
            Button cancelPresidiButton = StyleApp.getButton("Annulla", VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color");
            HorizontalLayout presidiActions = new HorizontalLayout(savePresidiButton, cancelPresidiButton);
            presidiActions.setVisible(false); // Nascosto di default
            presidiInnerContainer.add(presidiComboBox, presidiActions);

            editPresidiButton.addClickListener(ev -> {
                presidiItemsDiv.setVisible(false); // Nasconde la lista visualizzata
                presidiComboBox.setVisible(true); // Mostra il ComboBox
                presidiActions.setVisible(true);
                editPresidiButton.setVisible(false);
            });
            savePresidiButton.addClickListener(ev -> {
                Set<String> newPresidi = presidiComboBox.getValue();
                presidiItemsDiv.removeAll(); // Pulisce la lista visualizzata
                // Popola nuovamente la lista visualizzata con i nuovi presidi
                for (String presidio : newPresidi) {
                    HorizontalLayout itemLayout = new HorizontalLayout();
                    itemLayout.setSpacing(false);
                    itemLayout.setPadding(false);
                    itemLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    itemLayout.getStyle().set("margin-bottom", "var(--lumo-space-xxs)");
                    Span bulletPoint = new Span("•");
                    bulletPoint.getStyle().set("color", "var(--lumo-tertiary-color)").set("font-size", "var(--lumo-font-size-m)").set("line-height", "1").set("margin-right", "var(--lumo-space-xs)");
                    Span presidioSpan = new Span(presidio);
                    presidioSpan.getStyle().set("font-family", "var(--lumo-font-family)").set("color", "var(--lumo-body-text-color)").set("font-size", "var(--lumo-font-size-s)").set("line-height", "1.5");
                    itemLayout.add(bulletPoint, presidioSpan);
                    presidiItemsDiv.add(itemLayout);
                }
                presidiService.savePresidi(scenarioId, newPresidi); // Salva i presidi nel servizio
                presidiItemsDiv.setVisible(true);
                presidiComboBox.setVisible(false);
                presidiActions.setVisible(false);
                editPresidiButton.setVisible(true);
                Notification.show("Presidi aggiornati.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            cancelPresidiButton.addClickListener(ev -> {
                presidiComboBox.setVisible(false);
                presidiActions.setVisible(false);
                presidiItemsDiv.setVisible(true);
                editPresidiButton.setVisible(true);
            });

            presidiOuterContainer.add(presidiInnerContainer);
            monitorContainer.add(presidiOuterContainer);
        }

        return monitorContainer;
    }

    /**
     * Mostra un dialog per aggiungere un nuovo parametro aggiuntivo personalizzato.
     * Permette all'utente di definire nome, valore e unità di misura.
     *
     * @param scenarioId              L'ID dello scenario.
     * @param tempoId                 L'ID del tempo a cui aggiungere il parametro.
     * @param advancedScenarioService Servizio per la gestione degli scenari avanzati.
     * @param currentAdditionalParams La lista attuale dei parametri aggiuntivi.
     * @param vitalSignsLayout        Il layout dei parametri vitali a cui aggiungere il nuovo box.
     */
    private static void showAddAdditionalParamDialog(
            Integer scenarioId,
            Integer tempoId,
            AdvancedScenarioService advancedScenarioService,
            List<ParametroAggiuntivo> currentAdditionalParams,
            HorizontalLayout vitalSignsLayout) {

        Dialog addParamDialog = new Dialog();
        addParamDialog.setHeaderTitle("Nuovo Parametro Aggiuntivo");
        addParamDialog.setWidth("400px");

        TextField paramNameField = new TextField("Nome Parametro");
        paramNameField.setWidthFull();
        paramNameField.setRequiredIndicatorVisible(true);
        paramNameField.setErrorMessage("Il nome è obbligatorio");

        TextField paramValueField = new TextField("Valore");
        paramValueField.setWidthFull();

        TextField paramUnitField = new TextField("Unità di Misura (opzionale)");
        paramUnitField.setWidthFull();

        VerticalLayout dialogLayout = new VerticalLayout(paramNameField, paramValueField, paramUnitField);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        addParamDialog.add(dialogLayout);

        Button saveNewParamButton = new Button("Salva", VaadinIcon.CHECK.create());
        saveNewParamButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelNewParamButton = new Button("Annulla", event -> addParamDialog.close());
        addParamDialog.getFooter().add(cancelNewParamButton, saveNewParamButton);

        saveNewParamButton.addClickListener(saveEvent -> {
            String newName = paramNameField.getValue();
            String newValueStr = paramValueField.getValue();
            String newUnit = paramUnitField.getValue();

            // Validazione nome parametro
            if (newName == null || newName.isBlank()) {
                paramNameField.setInvalid(true);
                Notification.show(paramNameField.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                paramNameField.focus();
                return;
            }
            paramNameField.setInvalid(false);

            // Controllo duplicati e nomi standard
            boolean nameExists = currentAdditionalParams.stream().anyMatch(p -> p.getNome().equalsIgnoreCase(newName)) ||
                    Stream.of("PA", "FC", "T", "RR", "SpO₂", "SpO2", "FiO₂", "FiO2", "Litri O₂", "Litri O2", "EtCO₂", "EtCO2")
                            .anyMatch(stdName -> stdName.equalsIgnoreCase(newName));
            if (nameExists) {
                paramNameField.setInvalid(true);
                paramNameField.setErrorMessage("Un parametro con questo nome esiste già.");
                Notification.show("Un parametro con nome '" + newName + "' esiste già.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                paramNameField.focus();
                return;
            }
            paramNameField.setInvalid(false);

            Double parsedValueDbl = null;
            if (newValueStr != null && !newValueStr.isBlank() && !newValueStr.equals(NULL_DISPLAY_VALUE)) {
                try {
                    parsedValueDbl = Double.parseDouble(newValueStr.replace(",", "."));
                } catch (NumberFormatException nfe) {
                    // Non è un errore bloccante se il valore non è numerico, ma avvisa l'utente
                    Notification.show("Valore del parametro non numerico: '" + newValueStr + "'. Sarà salvato come testo se non è un numero.",
                            4000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                    Notification.show("Il valore del parametro '" + newValueStr + "' non è un numero valido.", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    paramValueField.focus();
                    return;
                }
            }

            @SuppressWarnings("DataFlowIssue") ParametroAggiuntivo newParam = new ParametroAggiuntivo(newName, parsedValueDbl, newUnit);

            try {
                advancedScenarioService.addAdditionalParam(scenarioId, tempoId, newParam);
                currentAdditionalParams.add(newParam); // Aggiunge il parametro alla lista locale

                String color = ADDITIONAL_PARAM_COLORS.get(currentAdditionalParams.size() % ADDITIONAL_PARAM_COLORS.size());
                final Div[] newBoxHolder = new Div[1]; // Riferimento al nuovo box creato

                // Callback per l'eliminazione del nuovo parametro
                Runnable onDeleteNewParam = () -> {
                    ConfirmDialog confirmDelDialog = new ConfirmDialog(
                            "Conferma Eliminazione",
                            "Sei sicuro di voler eliminare il parametro '" + newParam.getNome() + "'?",
                            "Elimina", delEvent -> {
                        try {
                            advancedScenarioService.deleteAdditionalParam(scenarioId, tempoId, newParam.getNome());
                            if (newBoxHolder[0] != null) {
                                vitalSignsLayout.remove(newBoxHolder[0]);
                            }
                            currentAdditionalParams.remove(newParam);
                            Notification.show("Parametro '" + newParam.getNome() + "' eliminato.", 3000, Notification.Position.BOTTOM_CENTER)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } catch (Exception exDel) {
                            logger.error("Errore eliminazione parametro aggiuntivo '{}': {}", newParam.getNome(), exDel.getMessage());
                            Notification.show("Errore eliminazione: " + exDel.getMessage(), 3000, Notification.Position.BOTTOM_CENTER)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    },
                            "Annulla", cancelDelEvent -> {
                    }
                    );
                    confirmDelDialog.setConfirmButtonTheme("error primary");
                    confirmDelDialog.open();
                };

                // Crea e aggiunge il box per il nuovo parametro alla UI
                Div newParamBox = createVitalSignBox(
                        newParam.getNome(),
                        newParam.getValore() != null ? newParam.getValore() : NULL_DISPLAY_VALUE,
                        newParam.getUnitaMisura() != null ? newParam.getUnitaMisura() : "",
                        color,
                        null, null, null, null, null,
                        advancedScenarioService, scenarioId, tempoId,
                        onDeleteNewParam
                );
                newBoxHolder[0] = newParamBox;
                vitalSignsLayout.add(newParamBox);

                Notification.show("Parametro '" + newName + "' aggiunto.", 3000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                addParamDialog.close(); // Chiude il dialog di aggiunta

            } catch (Exception exAdd) {
                logger.error("Errore durante l'aggiunta del parametro aggiuntivo '{}': {}", newName, exAdd.getMessage(), exAdd);
                Notification.show("Errore aggiunta parametro: " + exAdd.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        addParamDialog.open();
    }

    /**
     * Crea un singolo box di visualizzazione/modifica per un parametro vitale o aggiuntivo.
     * Gestisce la visualizzazione del valore, unità di misura, colorazione in base a soglie
     * e un pulsante per la modifica in linea.
     *
     * @param label                   La label del parametro (es. "FC", "Temperatura").
     * @param displayValue            Il valore da visualizzare (stringa).
     * @param unit                    L'unità di misura (es. "bpm", "mmHg").
     * @param defaultNormalColor      Il colore del testo in condizioni normali.
     * @param numericValue            Il valore numerico del parametro, usato per il confronto con le soglie.
     * @param criticalLowThreshold    Soglia critica inferiore.
     * @param criticalHighThreshold   Soglia critica superiore.
     * @param warningLowThreshold     Soglia di attenzione inferiore.
     * @param warningHighThreshold    Soglia di attenzione superiore.
     * @param advancedScenarioService Servizio per salvare le modifiche.
     * @param scenarioId              L'ID dello scenario.
     * @param tempoId                 L'ID del tempo.
     * @param onDeleteCallback        Callback da eseguire in caso di eliminazione (solo per parametri aggiuntivi).
     * @return Un {@link Div} che rappresenta il box del parametro.
     */
    private static Div createVitalSignBox(String label, String displayValue, String unit, String defaultNormalColor,
                                          Double numericValue,
                                          Double criticalLowThreshold, Double criticalHighThreshold,
                                          Double warningLowThreshold, Double warningHighThreshold,
                                          AdvancedScenarioService advancedScenarioService, Integer scenarioId, Integer tempoId,
                                          Runnable onDeleteCallback) {
        Div box = new Div();
        box.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("margin", "var(--lumo-space-xs)")
                .set("text-align", "center")
                .set("min-width", "130px")
                .set("flex-grow", "1")
                .set("flex-basis", "130px")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease, border-color 0.3s ease");

        // Aggiunge effetti di hover tramite JavaScript
        UI currentUI = UI.getCurrent();
        if (currentUI != null && currentUI.getPage() != null) {
            box.getElement().executeJs(
                    "this.addEventListener('mouseover', function() {" +
                            "  this.style.transform = 'translateY(-2px)';" +
                            "  this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';" +
                            "});" +
                            "this.addEventListener('mouseout', function() {" +
                            "  this.style.transform = 'translateY(0)';" +
                            "  this.style.boxShadow = '0 2px 4px rgba(0,0,0,0.05)';" +
                            "});"
            );
        }

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("display", "block")
                .set("font-size", "14px")
                .set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "4px")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");

        Span valueSpan = new Span(Objects.requireNonNullElse(displayValue, NULL_DISPLAY_VALUE));
        String finalValueColor = defaultNormalColor;

        // Logica per la colorazione e l'animazione di allarme
        if (numericValue != null) {
            boolean isCritical = criticalLowThreshold != null && numericValue < criticalLowThreshold;
            if (!isCritical && criticalHighThreshold != null && numericValue > criticalHighThreshold) {
                isCritical = true;
            }

            // Eccezione per EtCO2 a 0 (considerato normale se non ci sono altri allarmi)
            if ("EtCO₂".equals(label) && numericValue == 0) {
                isCritical = false;
            }

            boolean isWarning = false;
            if (!isCritical) { // Controlla warning solo se non già critico
                if (warningLowThreshold != null && numericValue < warningLowThreshold) {
                    isWarning = true;
                }
                if (!isWarning && warningHighThreshold != null && numericValue > warningHighThreshold) {
                    isWarning = true;
                }

                if ("EtCO₂".equals(label) && numericValue == 0) {
                    isWarning = false;
                }
            }

            if (isCritical) {
                box.addClassName("flash-alert-box"); // Applica animazione flash
                finalValueColor = "var(--lumo-error-color)";
            } else if (isWarning) {
                finalValueColor = "var(--lumo-warning-color)";
                box.getStyle().set("border-color", "var(--lumo-warning-color-50pct)");
            }
        }

        Span unitSpan = new Span(unit);
        // Stile specifico per FiO2 e Litri O2 quando il valore è 0 o N/D
        if (("FiO₂".equals(label) || "Litri O₂".equals(label) || "EtCO₂".equals(label)) && numericValue != null && numericValue == 0.0) {
            box.getStyle()
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("border", "1.5px dashed var(--lumo-contrast-30pct)");
            valueSpan.getStyle().set("color", "var(--lumo-disabled-text-color)");
            unitSpan.getStyle().set("color", "var(--lumo-disabled-text-color)");
            labelSpan.getStyle().set("color", "var(--lumo-disabled-text-color)");
            valueSpan.setText(NULL_DISPLAY_VALUE); // Forza "- " se il valore è 0
        }
        valueSpan.getStyle()
                .set("display", "block")
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", finalValueColor)
                .set("line-height", "1.2");

        unitSpan.getStyle()
                .set("display", "block")
                .set("font-size", "12px")
                .set("color", "var(--lumo-tertiary-text-color)");

        // Campo di testo per la modifica del valore
        TextField valueEditField = new TextField();
        valueEditField.setVisible(false); // Nascosto di default
        valueEditField.setWidthFull();
        valueEditField.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        // Controlli in alto a destra (modifica/elimina)
        HorizontalLayout topRightControls = new HorizontalLayout();
        topRightControls.setSpacing(true);
        topRightControls.setPadding(false);
        topRightControls.setAlignItems(FlexComponent.Alignment.CENTER);
        topRightControls.getStyle().set("margin-left", "auto");

        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color");
        editButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        editButton.setTooltipText("Modifica " + label);

        topRightControls.add(editButton);

        // Aggiunge il pulsante di eliminazione se un callback è fornito (per parametri aggiuntivi)
        if (onDeleteCallback != null) {
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            deleteButton.setTooltipText("Elimina " + label);
            deleteButton.addClickListener(e -> onDeleteCallback.run());
            topRightControls.add(deleteButton);
        }

        HorizontalLayout labelAndControlsLayout = new HorizontalLayout(labelSpan, topRightControls);
        labelSpan.getStyle().set("flex-grow", "1");
        labelAndControlsLayout.setWidthFull();
        labelAndControlsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        labelAndControlsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        labelAndControlsLayout.setSpacing(true);

        // Pulsanti Salva e Annulla per la modalità di modifica
        Button saveButton = StyleApp.getButton("Salva", null, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color");
        Button cancelButton = StyleApp.getButton("Annulla", null, ButtonVariant.LUMO_TERTIARY, "var(--lumo-base-color");

        saveButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        saveButton.getStyle()
                .set("padding-left", "var(--lumo-space-s)")
                .set("padding-right", "var(--lumo-space-s)")
                .set("min-width", "auto");
        cancelButton.getStyle()
                .set("padding-left", "var(--lumo-space-s)")
                .set("padding-right", "var(--lumo-space-s)")
                .set("min-width", "auto");

        HorizontalLayout editActions = new HorizontalLayout(saveButton, cancelButton);
        editActions.setVisible(false); // Nascosto di default
        editActions.setSpacing(true);
        editActions.getStyle()
                .set("margin-top", "var(--lumo-space-xs)")
                .set("justify-content", "center");

        // Aggiunge tutti i componenti al box
        box.add(labelAndControlsLayout, valueSpan, valueEditField, unitSpan, editActions);

        // Listener per il pulsante "Modifica"
        editButton.addClickListener(e -> {
            topRightControls.setVisible(false);
            valueSpan.setVisible(false);
            unitSpan.setVisible(false);
            // Popola il campo di modifica con il valore attuale, gestendo NULL_DISPLAY_VALUE
            valueEditField.setValue(valueSpan.getText().equals(NULL_DISPLAY_VALUE) ? "" : valueSpan.getText());
            valueEditField.setVisible(true);
            valueEditField.focus();
            editActions.setVisible(true);
        });

        // Runnable per uscire dalla modalità di modifica
        Runnable endEditMode = () -> {
            valueSpan.setVisible(true);
            unitSpan.setVisible(true);
            valueEditField.setVisible(false);
            editActions.setVisible(false);
            topRightControls.setVisible(true);
        };

        // Listener per il pulsante "Salva" in modalità modifica
        saveButton.addClickListener(e -> {
            String editedValueStr = valueEditField.getValue();

            // Validazione e aggiornamento del valore in base alla label del parametro
            switch (label) {
                case "PA" -> {
                    // Formato "sistolica/diastolica"
                    if (!editedValueStr.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$") && !editedValueStr.equals(NULL_DISPLAY_VALUE) && !editedValueStr.isBlank()) {
                        Notification.show("Formato PA non valido. Usa 'sistolica/diastolica'.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return; // Non salvare e resta in modalità modifica
                    }
                    valueSpan.setText(editedValueStr.isBlank() ? NULL_DISPLAY_VALUE : editedValueStr);
                }
                case "FC", "RR", "EtCO₂" -> {
                    try {
                        if (!editedValueStr.isBlank() && !editedValueStr.equals(NULL_DISPLAY_VALUE)) {
                            double doubleValue = Double.parseDouble(editedValueStr.replace(",", "."));
                            if (doubleValue < 0) {
                                Notification.show("Il valore di " + label + " deve essere un numero positivo.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                return;
                            }
                        }
                        valueSpan.setText(editedValueStr.isBlank() ? NULL_DISPLAY_VALUE : editedValueStr);
                    } catch (NumberFormatException ex) {
                        Notification.show("Il valore di " + label + " deve essere un numero valido.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                }
                case "SpO₂", "FiO₂", "Litri O₂" -> {
                    try {
                        if (!editedValueStr.isBlank() && !editedValueStr.equals(NULL_DISPLAY_VALUE)) {
                            int intValue = Integer.parseInt(editedValueStr.trim());
                            if (("SpO₂".equals(label) || "FiO₂".equals(label)) && (intValue < 0 || intValue > 100)) {
                                Notification.show("Il valore di " + label + " deve essere compreso tra 0 e 100.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                return;
                            }
                            if ("Litri O₂".equals(label) && intValue < 0) {
                                Notification.show("Il valore di " + label + " deve essere positivo.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                return;
                            }
                        }
                        valueSpan.setText(editedValueStr.isBlank() ? NULL_DISPLAY_VALUE : editedValueStr);
                    } catch (NumberFormatException ex) {
                        Notification.show("Il valore di " + label + " deve essere un numero intero.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                }
                default -> valueSpan.setText(editedValueStr.isBlank() ? NULL_DISPLAY_VALUE : editedValueStr);
            }

            String valueToSave = valueSpan.getText();
            // Salva il valore nel servizio
            advancedScenarioService.saveVitalSign(scenarioId, tempoId, label, valueToSave);

            // Ricalcola la colorazione e l'animazione in base al nuovo valore
            Double newNumericValueForColoring = null;
            if (!valueToSave.equals(NULL_DISPLAY_VALUE) && !valueToSave.isBlank()) {
                try {
                    if (!label.equals("PA")) { // La PA ha un formato specifico, non è un singolo numero per la colorazione
                        newNumericValueForColoring = Double.parseDouble(valueToSave.replace(",", "."));
                    }
                } catch (Exception ex) {
                    logger.warn("Errore nel parsing del valore numerico per colorazione {}: {} (valore: '{}')", label, ex.getMessage(), valueToSave);
                }
            }

            box.getClassNames().remove("flash-alert-box"); // Rimuove animazione precedente
            box.getStyle().remove("border-color"); // Rimuove bordo di allarme precedente
            String newDisplayColor = defaultNormalColor; // Colore di default

            if (newNumericValueForColoring != null) {
                boolean isCritical = criticalLowThreshold != null && newNumericValueForColoring < criticalLowThreshold;
                if (!isCritical && criticalHighThreshold != null && newNumericValueForColoring > criticalHighThreshold) {
                    isCritical = true;
                }

                boolean isWarning = false;
                if (!isCritical) {
                    if (warningLowThreshold != null && newNumericValueForColoring < warningLowThreshold) {
                        isWarning = true;
                    }
                    if (!isWarning && warningHighThreshold != null && newNumericValueForColoring > warningHighThreshold) {
                        isWarning = true;
                    }
                }

                if (isCritical) {
                    box.addClassName("flash-alert-box");
                    newDisplayColor = "var(--lumo-error-color)";
                } else if (isWarning) {
                    newDisplayColor = "var(--lumo-warning-color)";
                    box.getStyle().set("border-color", "var(--lumo-warning-color-50pct)");
                }
            } else if (valueToSave.equals(NULL_DISPLAY_VALUE) || valueToSave.isBlank()) {
                newDisplayColor = "var(--lumo-secondary-text-color)"; // Colore per valori N/D o vuoti
            }
            valueSpan.getStyle().set("color", newDisplayColor); // Aggiorna il colore del valore

            // Gestione specifica di FiO2 e Litri O2 per lo stato "assente/spento"
            boolean isFio2OrLitriO2 = "FiO₂".equals(label) || "Litri O₂".equals(label) || "EtCO₂".equals(label);
            boolean isZeroOrNullEquivalent = valueToSave.equals("0") || valueToSave.equals("0.0") || valueToSave.equals(NULL_DISPLAY_VALUE) || valueToSave.isBlank();

            if (isFio2OrLitriO2) {
                if (isZeroOrNullEquivalent) {
                    box.getStyle()
                            .set("background-color", "var(--lumo-contrast-5pct)")
                            .set("border", "1.5px dashed var(--lumo-contrast-30pct)");
                    valueSpan.getStyle().set("color", "var(--lumo-disabled-text-color)");
                    unitSpan.getStyle().set("color", "var(--lumo-disabled-text-color)");
                    labelSpan.getStyle().set("color", "var(--lumo-disabled-text-color)");
                    valueSpan.setText(NULL_DISPLAY_VALUE); // Forza "- " se il valore è 0
                } else {
                    box.getStyle().remove("background-color");
                    box.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)"); // Ripristina bordo normale
                    unitSpan.getStyle().set("color", "var(--lumo-tertiary-text-color)");
                    labelSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
                }
            }

            endEditMode.run(); // Esce dalla modalità di modifica
            Notification.show(label + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        // Listener per il pulsante "Annulla" in modalità modifica
        cancelButton.addClickListener(e -> endEditMode.run());
        return box;
    }
}