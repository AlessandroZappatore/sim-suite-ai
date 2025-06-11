package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.uniupo.simnova.views.constant.AdditionParametersConst.ADDITIONAL_PARAMETERS;
import static it.uniupo.simnova.views.constant.AdditionParametersConst.CUSTOM_PARAMETER_KEY;
import static it.uniupo.simnova.views.constant.ColorsConst.BORDER_COLORS;

/**
 * Rappresenta una singola sezione temporale (T0, T1, T2, ecc.) di uno scenario di simulazione.
 * Questa classe gestisce i campi di input per i parametri medici, le azioni, le transizioni
 * e i parametri aggiuntivi specifici per quel tempo.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class TimeSection {
    /**
     * Logger per la classe {@link TimeSection}.
     */
    private static final Logger logger = LoggerFactory.getLogger(TimeSection.class);

    /**
     * Numero identificativo del tempo (es. 0 per T0, 1 per T1, ...).
     */
    public final int timeNumber;
    /**
     * Layout principale della sezione temporale, che contiene tutti i campi e le informazioni.
     */
    public final VerticalLayout layout;

    /**
     * Selettore per il timer associato a questo tempo, utile per gestire la durata di azioni o attese.
     */
    public final TimePicker timerPicker;
    /**
     * Campo di input per la Pressione Arteriosa (PA) in formato "Sist/Diast".
     */
    public final TextField paField;
    /**
     * Campo di input per la Frequenza Cardiaca (FC).
     */
    public final NumberField fcField;
    /**
     * Campo di input per la Frequenza Respiratoria (RR).
     */
    public final NumberField rrField;
    /**
     * Campo di input per la Temperatura Corporea (T), arrotondata a una cifra decimale.
     */
    public final NumberField tField;
    /**
     * Campo di input per la Saturazione dell'Ossigeno (SpO₂).
     */
    public final NumberField spo2Field;
    /**
     * Campo di input per la Frazione inspiratoria di Ossigeno (FiO₂).
     */
    public final NumberField fio2Field;
    /**
     * Campo di input per i Litri di Ossigeno (Litri O₂).
     */
    public final NumberField litriO2Field;
    /**
     * Campo di input per la Capnometria di fine espirazione (EtCO₂).
     */
    public final NumberField etco2Field;
    /**
     * Area di testo per descrivere l'azione richiesta o l'evento scatenante per questo tempo.
     */
    public final TextArea actionDetailsArea;
    /**
     * Campo per la navigazione condizionale al tempo successivo se la condizione è "SI".
     */
    public final IntegerField timeIfYesField;
    /**
     * Campo per la navigazione condizionale al tempo successivo se la condizione è "NO".
     */
    public final IntegerField timeIfNoField;
    /**
     * Area di testo per ulteriori dettagli o note specifiche per questo tempo.
     */
    public final TextArea additionalDetailsArea;
    /**
     * Area di testo per il ruolo del genitore, visibile solo in scenari pediatrici.
     * Può contenere informazioni fornite dal genitore sul paziente.
     */
    public final TextArea ruoloGenitoreArea;
    /**
     * Layout che contiene i campi per i parametri medici base.
     */
    public final FormLayout medicalParamsForm;
    /**
     * Contenitore per i campi dei parametri aggiuntivi/personalizzati.
     * Permette di aggiungere dinamicamente nuovi parametri con unità di misura specifiche.
     */
    public final VerticalLayout customParamsContainer;
    /**
     * Mappa che associa i parametri aggiuntivi (predefiniti o personalizzati) ai loro campi di input.
     */
    public final Map<String, NumberField> customParameters = new HashMap<>();
    /**
     * Mappa che associa i parametri aggiuntivi (predefiniti o personalizzati) alle loro unità di misura.
     */
    public final Map<String, String> customParameterUnits = new HashMap<>();
    /**
     * Mappa che associa i parametri aggiuntivi (predefiniti o personalizzati) ai loro layout di campo,
     * inclusi i pulsanti per rimuoverli dinamicamente.
     */
    public final Map<String, HorizontalLayout> customParameterLayouts = new HashMap<>();

    /**
     * Costruttore per creare una nuova sezione temporale.
     *
     * @param timeNumber            Il numero identificativo di questo tempo.
     * @param scenarioService       Il servizio per la gestione dello scenario (usato per controllare tipo pediatrico).
     * @param timeSections          La lista di tutte le sezioni temporali (per la rimozione).
     * @param timeSectionsContainer Il layout contenitore di tutte le sezioni (per la rimozione).
     * @param scenarioId            L'ID dello scenario a cui appartiene questa sezione.
     */
    public TimeSection(int timeNumber, ScenarioService scenarioService, List<TimeSection> timeSections, VerticalLayout timeSectionsContainer, Integer scenarioId) {
        this.timeNumber = timeNumber;

        layout = new VerticalLayout();
        layout.addClassName(LumoUtility.Padding.MEDIUM);
        layout.addClassName(LumoUtility.Border.ALL);
        layout.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        layout.addClassName(LumoUtility.BorderRadius.MEDIUM);
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.getStyle().set("border-left", "4px solid " + getBorderColor(timeNumber)); // Bordo colorato per distinguere i tempi

        Paragraph sectionTitle = new Paragraph("Tempo T" + timeNumber);
        sectionTitle.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.MEDIUM);

        HorizontalLayout timerLayout = FieldGenerator.createTimerPickerWithPresets(
                "Timer associato a T" + timeNumber + " (opzionale)"
        );
        timerPicker = (TimePicker) timerLayout.getComponentAt(0); // Il TimePicker è il primo componente nel layout

        medicalParamsForm = new FormLayout();
        medicalParamsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2) // Due colonne su schermi larghi
        );
        medicalParamsForm.setWidthFull();

        // Inizializzazione dei campi per i parametri medici base
        paField = FieldGenerator.createTextField("PA (Sist/Diast)", "es. 120/80", true);
        paField.setSuffixComponent(new Paragraph("mmHg"));

        fcField = FieldGenerator.createMedicalField("FC", "(es. 80)", true, "bpm");
        rrField = FieldGenerator.createMedicalField("FR", "(es. 16)", true, "atti/min");
        tField = FieldGenerator.createMedicalField("Temp.", "(es. 36.5)", true, "°C");
        spo2Field = FieldGenerator.createMedicalField("SpO₂", "(es. 98)", true, "%");
        fio2Field = FieldGenerator.createMedicalField("FiO₂", "(es. 21)", false, "%");
        litriO2Field = FieldGenerator.createMedicalField("Litri O₂", "(es. 5)", false, "L/min");
        etco2Field = FieldGenerator.createMedicalField("EtCO₂", "(es. 35)", false, "mmHg");

        medicalParamsForm.add(paField, fcField, rrField, tField, spo2Field, fio2Field, litriO2Field, etco2Field);
        medicalParamsForm.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        customParamsContainer = new VerticalLayout();
        customParamsContainer.setWidthFull();
        customParamsContainer.setPadding(false);
        customParamsContainer.setSpacing(false);

        Hr divider = new Hr(); // Divisore tra sezioni
        divider.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        Paragraph actionTitle = new Paragraph(timeNumber == 0 ?
                "DETTAGLI INIZIALI T0" : "AZIONE E TRANSIZIONI PER T" + timeNumber);
        actionTitle.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

        actionDetailsArea = FieldGenerator.createTextArea(
                "Azione richiesta / Evento scatenante per procedere da T" + timeNumber,
                "Es. Somministrare farmaco X, Rilevare parametro Y, Domanda al paziente...",
                false
        );

        HorizontalLayout timeSelectionContainer = new HorizontalLayout();
        timeSelectionContainer.setWidthFull();
        timeSelectionContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        HorizontalLayout timeSelectionLayout = new HorizontalLayout();
        timeSelectionLayout.setWidthFull();
        timeSelectionLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        timeSelectionLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        timeSelectionLayout.setSpacing(true);

        // Campi per la navigazione condizionale al tempo successivo
        timeIfYesField = FieldGenerator.createTimeNavigationField(
                "Se SI, vai a T:",
                "ID Tempo",
                true
        );

        timeIfNoField = FieldGenerator.createTimeNavigationField(
                "Se NO, vai a T:",
                "ID Tempo",
                true
        );

        timeSelectionLayout.add(timeIfYesField, timeIfNoField);
        timeSelectionContainer.add(timeSelectionLayout);
        timeSelectionContainer.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        additionalDetailsArea = FieldGenerator.createTextArea(
                "Altri dettagli / Note per T" + timeNumber + " (opzionale)",
                "Es. Note per il docente, trigger specifici, stato emotivo del paziente...",
                false
        );

        // Pulsante per rimuovere la sezione temporale (non visibile per T0)
        Button removeButton = new Button("Rimuovi T" + timeNumber, new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removeButton.addClickListener(event -> {
            timeSections.remove(this); // Rimuove dalla lista logica
            timeSectionsContainer.remove(layout); // Rimuove dalla UI
            Notification.show("Tempo T" + timeNumber + " rimosso.", 2000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        // Campo per il ruolo del genitore (visibile solo per scenari pediatrici)
        ruoloGenitoreArea = FieldGenerator.createTextArea(
                "Ruolo Genitore (opzionale)",
                "Es. Genitore riferisce delle informazioni sul paziente...",
                false
        );

        // Aggiunta dei componenti al layout della sezione
        layout.add(sectionTitle, timerLayout, medicalParamsForm, customParamsContainer, divider,
                actionTitle, actionDetailsArea, timeSelectionContainer,
                additionalDetailsArea);
        if (scenarioService.isPediatric(scenarioId)) {
            layout.add(ruoloGenitoreArea);
        }

        // Aggiunge il pulsante di rimozione solo per tempi diversi da T0
        if (timeNumber > 0) {
            layout.add(removeButton);
            layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END, removeButton);
        }
    }

    /**
     * Restituisce il layout principale ({@link VerticalLayout}) di questa sezione temporale.
     *
     * @return Il layout della sezione.
     */
    public VerticalLayout getLayout() {
        return layout;
    }

    /**
     * Restituisce il numero identificativo di questo tempo (0 per T0, 1 per T1, ...).
     *
     * @return Il numero del tempo.
     */
    public int getTimeNumber() {
        return timeNumber;
    }

    /**
     * Restituisce il {@link FormLayout} che contiene i campi dei parametri medici base.
     *
     * @return Il FormLayout dei parametri medici.
     */
    public FormLayout getMedicalParamsForm() {
        return medicalParamsForm;
    }

    /**
     * Restituisce la mappa dei parametri aggiuntivi/personalizzati (chiave -> campo {@link NumberField}).
     *
     * @return La mappa dei campi dei parametri aggiuntivi.
     */
    public Map<String, NumberField> getCustomParameters() {
        return customParameters;
    }

    /**
     * Nasconde il pulsante "Rimuovi Tempo" per questa sezione.
     * Utile per la sezione T0 che non può essere rimossa.
     */
    public void hideRemoveButton() {
        layout.getChildren().filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getText().startsWith("Rimuovi T"))
                .findFirst()
                .ifPresent(button -> button.setVisible(false));
    }

    /**
     * Aggiunge un parametro (predefinito o personalizzato) alla sezione temporale.
     * Crea un campo numerico {@link NumberField} con un pulsante per rimuoverlo,
     * e memorizza l'unità di misura associata.
     *
     * @param key   La chiave identificativa del parametro (es. "PVC" o "CUSTOM_Nome_Parametro").
     * @param label L'etichetta completa da visualizzare per il campo (es. "Nome Parametro (unit)").
     * @param unit  L'unità di misura (stringa) da associare a questo parametro per il salvataggio.
     */
    public void addCustomParameter(String key, String label, String unit) {
        if (!customParameters.containsKey(key)) {
            NumberField field = FieldGenerator.createMedicalField(label, "", false, unit); // Crea il campo numerico
            customParameters.put(key, field); // Aggiunge il campo alla mappa dei parametri

            if (unit != null) {
                customParameterUnits.put(key, unit); // Memorizza l'unità di misura
            }

            // Pulsante per rimuovere il parametro aggiunto dinamicamente
            Button removeParamButton = new Button(new Icon(VaadinIcon.TRASH));
            removeParamButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            removeParamButton.getElement().setAttribute("aria-label", "Rimuovi " + label);
            removeParamButton.setTooltipText("Rimuovi " + label);
            removeParamButton.addClassName(LumoUtility.Margin.Left.SMALL);

            HorizontalLayout paramLayout = new HorizontalLayout(field, removeParamButton);
            paramLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
            paramLayout.setWidthFull();
            paramLayout.setFlexGrow(1, field); // Il campo si espande

            removeParamButton.addClickListener(e -> {
                HorizontalLayout layoutToRemove = customParameterLayouts.get(key);
                if (layoutToRemove != null) {
                    customParamsContainer.remove(layoutToRemove); // Rimuove dalla UI
                    customParameters.remove(key); // Rimuove dalla mappa dei campi
                    customParameterLayouts.remove(key); // Rimuove dalla mappa dei layout
                    customParameterUnits.remove(key); // Rimuove l'unità
                }
            });

            customParameterLayouts.put(key, paramLayout); // Aggiunge il layout del parametro alla mappa
            customParamsContainer.add(paramLayout); // Aggiunge il layout del parametro al contenitore UI
        } else {
            logger.warn("Tentativo di aggiungere un parametro con chiave duplicata: {}", key);
        }
    }

    /**
     * Prepara i dati di questa sezione temporale per il salvataggio nel database.
     * Raccoglie i valori da tutti i campi dell'interfaccia utente (parametri medici e aggiuntivi),
     * e li assembla in un oggetto {@link Tempo}.
     *
     * @return Un oggetto {@link Tempo} pronto per essere salvato, contenente tutti i dati della sezione.
     */
    public Tempo prepareDataForSave() {
        // Recupero dei valori dai campi di input
        LocalTime time = timerPicker.getValue();
        String pa = paField.getValue() != null ? paField.getValue().trim() : "";
        Integer fc = fcField.getValue() != null ? fcField.getValue().intValue() : null;
        Integer rr = rrField.getValue() != null ? rrField.getValue().intValue() : null;
        double rawT = tField.getValue() != null ? tField.getValue() : 0.0;
        double t = Math.round(rawT * 10.0) / 10.0; // Arrotonda la temperatura a una cifra decimale
        Integer spo2 = spo2Field.getValue() != null ? spo2Field.getValue().intValue() : null;
        Integer fiO2 = fio2Field.getValue() != null ? fio2Field.getValue().intValue() : null;
        Double litriO2 = litriO2Field.getValue() != null ? litriO2Field.getValue() : null;
        Integer etco2 = etco2Field.getValue() != null ? etco2Field.getValue().intValue() : null;

        String actionDescription = actionDetailsArea.getValue() != null ? actionDetailsArea.getValue().trim() : "";
        int nextTimeIfYes = timeIfYesField.getValue() != null ? timeIfYesField.getValue() : 0; // Default 0 se non specificato
        int nextTimeIfNo = timeIfNoField.getValue() != null ? timeIfNoField.getValue() : 0; // Default 0 se non specificato
        String additionalDetails = additionalDetailsArea.getValue() != null ? additionalDetailsArea.getValue().trim() : "";
        long timerSeconds = (time != null) ? time.toSecondOfDay() : 0L; // Converte il TimePicker in secondi
        String ruoloGenitoreValue = this.ruoloGenitoreArea.getValue() != null ? this.ruoloGenitoreArea.getValue().trim() : "";

        // Raccolta dei parametri aggiuntivi
        List<ParametroAggiuntivo> additionalParamsList = new ArrayList<>();
        customParameters.forEach((key, field) -> {
            double value = field.getValue() != null ? field.getValue() : 0.0;
            String unit;
            String paramNameForDb;

            if (key.startsWith(CUSTOM_PARAMETER_KEY)) {
                // Per i parametri personalizzati, estrai il nome pulito dalla chiave
                paramNameForDb = key.substring(CUSTOM_PARAMETER_KEY.length() + 1).replace('_', ' ');
                unit = customParameterUnits.getOrDefault(key, "");
            } else {
                // Per i parametri predefiniti, usa la chiave come nome e recupera l'unità
                paramNameForDb = key;
                unit = customParameterUnits.get(key);
                if (unit == null) { // Se l'unità non è nella mappa customParameterUnits, prova a recuperarla da ADDITIONAL_PARAMETERS
                    String fullLabel = ADDITIONAL_PARAMETERS.getOrDefault(key, "");
                    if (fullLabel.contains("(") && fullLabel.contains(")")) {
                        try {
                            unit = fullLabel.substring(fullLabel.indexOf("(") + 1, fullLabel.indexOf(")"));
                        } catch (IndexOutOfBoundsException e) {
                            unit = ""; // Nessuna unità trovata nel formato atteso
                        }
                    } else {
                        unit = "";
                    }
                }
            }

            additionalParamsList.add(new ParametroAggiuntivo(
                    paramNameForDb,
                    value,
                    unit != null ? unit : ""
            ));
        });

        // Creazione dell'oggetto Tempo
        Tempo tempo = new Tempo(
                timeNumber,
                0, // Scenario ID (sarà impostato esternamente o dal servizio)
                pa.isEmpty() ? null : pa, // Imposta a null se vuoto per evitare stringhe vuote nel DB
                fc,
                rr,
                t,
                spo2,
                fiO2,
                litriO2,
                etco2,
                actionDescription,
                nextTimeIfYes,
                nextTimeIfNo,
                additionalDetails.isEmpty() ? null : additionalDetails,
                timerSeconds,
                ruoloGenitoreValue.isEmpty() ? null : ruoloGenitoreValue
        );

        tempo.setParametriAggiuntivi(additionalParamsList);
        return tempo;
    }

    /**
     * Imposta il valore del campo Pressione Arteriosa (PA) e lo rende non modificabile.
     *
     * @param value Il valore PA da impostare (può essere null).
     */
    public void setPaValue(String value) {
        paField.setValue(value != null ? value : "");
        paField.setReadOnly(true);
        paField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore del campo Frequenza Cardiaca (FC) e lo rende non modificabile.
     *
     * @param value Il valore FC da impostare (può essere null).
     */
    public void setFcValue(Integer value) {
        fcField.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        fcField.setReadOnly(true);
        fcField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore del campo Frequenza Respiratoria (RR) e lo rende non modificabile.
     *
     * @param value Il valore RR da impostare (può essere null).
     */
    public void setRrValue(Integer value) {
        rrField.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        rrField.setReadOnly(true);
        rrField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore del campo Temperatura Corporea (T) e lo rende non modificabile.
     * Il valore viene arrotondato a una cifra decimale.
     *
     * @param value Il valore T da impostare (può essere null).
     */
    public void setTValue(Double value) {
        if (value != null) {
            double roundedValue = Math.round(value * 10.0) / 10.0;
            tField.setValue(roundedValue);
        } else {
            tField.setValue(null);
        }
        tField.setReadOnly(true);
        tField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore del campo Saturazione dell'Ossigeno (SpO2) e lo rende non modificabile.
     *
     * @param value Il valore SpO2 da impostare (può essere null).
     */
    public void setSpo2Value(Integer value) {
        spo2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        spo2Field.setReadOnly(true);
        spo2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore del campo Frazione inspiratoria di Ossigeno (FiO2) e lo rende non modificabile.
     *
     * @param value Il valore FiO2 da impostare (può essere null).
     */
    public void setFio2Value(Integer value) {
        fio2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        fio2Field.setReadOnly(true);
        fio2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore del campo Litri di Ossigeno (Litri O2) e lo rende non modificabile.
     *
     * @param value Il valore Litri O2 da impostare (può essere null).
     */
    public void setLitriO2Value(Double value) {
        litriO2Field.setValue(value);
        litriO2Field.setReadOnly(true);
        litriO2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Imposta il valore del campo Capnometria di fine espirazione (EtCO2) e lo rende non modificabile.
     *
     * @param value Il valore EtCO2 da impostare (può essere null).
     */
    public void setEtco2Value(Integer value) {
        etco2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
        etco2Field.setReadOnly(true);
        etco2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
    }

    /**
     * Restituisce il colore del bordo per la sezione temporale, basandosi sul suo numero.
     * Utilizza un array predefinito di colori per una variazione cromatica.
     *
     * @param timeNumber Il numero del tempo.
     * @return Una stringa CSS che rappresenta il colore del bordo.
     */
    private String getBorderColor(int timeNumber) {
        return BORDER_COLORS[(timeNumber) % BORDER_COLORS.length];
    }
}