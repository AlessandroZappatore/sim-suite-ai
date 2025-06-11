package it.uniupo.simnova.views.common.utils;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

/**
 * Classe di supporto per la ***generazione e stilizzazione avanzata dei campi di input**
 * di Vaadin Flow. Fornisce metodi statici per creare vari tipi di campi
 * (TextField, NumberField, ComboBox, Select, TextArea, TimePicker, Checkbox, IntegerField, MultiSelectComboBox)
 * con stili e comportamenti predefiniti, come ombreggiatura al passaggio del mouse,
 * bordi colorati in base alla obbligatorietà e larghezza dinamica.
 *
 * @author Alessandro Zappatore
 * @version 1.3
 */
@SuppressWarnings("ThisExpressionReferencesGlobalObjectJS") // Sopprime avvisi per l'uso di 'this' in JavaScript.
public class FieldGenerator extends HorizontalLayout {

    /**
     * Costruttore privato per evitare istanziazioni dirette.
     */
    private FieldGenerator() {
        // Costruttore privato per evitare istanziazioni dirette.
        // Tutti i metodi sono statici e non richiedono un'istanza della classe.
    }

    /**
     * Crea e configura un ***campo di testo ({@link TextField})** con stili avanzati.
     * Include effetti di ombra al passaggio del mouse e bordi colorati.
     *
     * @param label       Etichetta visualizzata sopra il campo.
     * @param placeholder Testo suggerito visualizzato all'interno del campo quando vuoto.
     * @param required    Se {@code true}, il campo è obbligatorio e ha un bordo primario.
     *                    Se {@code false}, il campo è opzionale e ha un bordo di successo.
     *                    Se {@code null}, il campo non ha un colore di bordo specifico (contrasto).
     * @return Il campo di testo configurato.
     */
    public static TextField createTextField(String label, String placeholder, Boolean required) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull(); // Il campo occupa la larghezza disponibile.
        field.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)") // Ombra predefinita.
                .set("transition", "box-shadow 0.3s ease-in-out") // Transizione per l'ombra.
                .set("flex-grow", "1") // Permette al campo di espandersi.
                .set("flex-shrink", "1"); // Permette al campo di restringersi.

        // Eccezione per "Azione Chiave" per non limitare la larghezza massima.
        if (!label.equals("Azione Chiave")) {
            field.getStyle().set("max-width", "500px");
        }

        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)"); // Colore di sfondo interno del campo.

        // Aggiunge effetti di ombra al passaggio del mouse tramite JavaScript.
        field.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        field.addClassName(LumoUtility.Margin.Top.LARGE); // Margine superiore.
        field.addClassName(LumoUtility.Padding.SMALL); // Padding interno.

        // Configura il bordo sinistro in base alla obbligatorietà.
        if (required == null) {
            field.getStyle().set("border-left", "3px solid var(--lumo-contrast-30pct)");
        } else if (required) {
            field.setRequired(true);
            field.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            field.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return field;
    }

    /**
     * Crea e configura un ***campo numerico ({@link NumberField})** con stili avanzati.
     *
     * @param label       Etichetta visualizzata sopra il campo.
     * @param placeholder Testo suggerito visualizzato all'interno del campo quando vuoto.
     * @param required    Se {@code true}, il campo è obbligatorio e ha un bordo primario;
     *                    altrimenti, ha un bordo di successo.
     * @return Il campo numerico configurato.
     */
    public static NumberField createNumberField(String label, String placeholder, boolean required) {
        NumberField field = new NumberField(label);
        field.setPlaceholder(placeholder);
        field.setWidthFull();
        field.getStyle()
                .set("max-width", "500px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("flex-grow", "1")
                .set("flex-shrink", "1");

        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        field.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        field.addClassName(LumoUtility.Margin.Top.LARGE);
        field.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            field.setRequired(true);
            field.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            field.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }
        return field;
    }

    /**
     * Crea e configura un ***ComboBox ({@link ComboBox})** generico con stili avanzati.
     *
     * @param <T>          Il tipo di dati contenuti nel ComboBox.
     * @param label        Etichetta visualizzata sopra il ComboBox.
     * @param items        Collezione di elementi disponibili per la selezione.
     * @param defaultValue Valore predefinito da impostare all'avvio.
     * @param required     Se {@code true}, il ComboBox è obbligatorio e ha un bordo primario;
     *                     altrimenti, ha un bordo di successo.
     * @return Il ComboBox configurato.
     */
    public static <T> ComboBox<T> createComboBox(String label, Collection<T> items, T defaultValue, boolean required) {
        ComboBox<T> comboBox = new ComboBox<>(label);
        comboBox.setItems(items);

        if (defaultValue != null) {
            comboBox.setValue(defaultValue);
        }

        comboBox.setWidthFull();
        comboBox.getStyle()
                .set("max-width", "500px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("flex-grow", "1")
                .set("flex-shrink", "1");

        comboBox.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        comboBox.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        comboBox.addClassName(LumoUtility.Margin.Top.LARGE);
        comboBox.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            comboBox.setRequired(true);
            comboBox.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            comboBox.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return comboBox;
    }

    /**
     * Crea e configura un ***Select ({@link Select})** generico con stili avanzati.
     *
     * @param <T>          Il tipo di dati contenuti nel Select.
     * @param label        Etichetta visualizzata sopra il Select.
     * @param items        Collezione di elementi disponibili per la selezione.
     * @param defaultValue Valore predefinito da impostare all'avvio.
     * @param required     Se {@code true}, il Select è obbligatorio e ha un bordo primario;
     *                     altrimenti, ha un bordo di successo.
     * @return Il Select configurato.
     */
    public static <T> Select<T> createSelect(String label, Collection<T> items, T defaultValue, boolean required) {
        Select<T> select = new Select<>();
        select.setLabel(label);
        select.setItems(items);

        if (defaultValue != null) {
            select.setValue(defaultValue);
        }

        select.setWidthFull();
        select.getStyle()
                .set("max-width", "500px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("flex-grow", "1")
                .set("flex-shrink", "1");

        select.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        select.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        select.addClassName(LumoUtility.Margin.Top.LARGE);
        select.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            select.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            select.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return select;
    }

    /**
     * Crea e configura un ***campo numerico ({@link NumberField}) specifico per dati medici**
     * con unità di misura opzionale e stili avanzati.
     *
     * @param label       Etichetta visualizzata sopra il campo.
     * @param placeholder Testo suggerito visualizzato all'interno del campo quando vuoto.
     * @param required    Se {@code true}, il campo è obbligatorio e ha un bordo primario;
     *                    altrimenti, ha un bordo di successo.
     * @param unit        Unità di misura da visualizzare come suffisso (es. "bpm", "mmHg").
     * @return Il campo numerico configurato.
     */
    public static NumberField createMedicalField(String label, String placeholder, boolean required, String unit) {
        NumberField field = createNumberField(label, placeholder, required); // Riutilizza la creazione di NumberField.

        if (unit != null && !unit.isEmpty()) {
            Paragraph unitLabel = new Paragraph(unit);
            field.setSuffixComponent(unitLabel); // Aggiunge l'unità come suffisso.
        }

        return field;
    }

    /**
     * Crea e configura un'**area di testo ({@link TextArea})** con stili avanzati.
     *
     * @param label       Etichetta visualizzata sopra l'area di testo.
     * @param placeholder Testo suggerito visualizzato all'interno dell'area di testo quando vuota.
     * @param required    Se {@code true}, l'area di testo è obbligatoria e ha un bordo primario;
     *                    altrimenti, ha un bordo di successo.
     * @return L'area di testo configurata.
     */
    public static TextArea createTextArea(String label, String placeholder, boolean required) {
        TextArea textArea = new TextArea(label);
        textArea.setPlaceholder(placeholder);
        textArea.setWidthFull();
        textArea.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("min-height", "100px") // Altezza minima.
                .set("flex-grow", "1")
                .set("flex-shrink", "1");
        textArea.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        textArea.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        textArea.addClassName(LumoUtility.Margin.Top.LARGE);
        textArea.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            textArea.setRequired(true);
            textArea.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            textArea.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return textArea;
    }

    /**
     * Crea e configura un ***TimePicker ({@link TimePicker})** con preset di tempo rapidi
     * e stili avanzati.
     *
     * @param label Etichetta visualizzata sopra il TimePicker.
     * @return Un {@link HorizontalLayout} contenente il TimePicker e i pulsanti per i preset.
     */
    public static HorizontalLayout createTimerPickerWithPresets(String label) {
        TimePicker timerPicker = new TimePicker(label);
        timerPicker.setStep(Duration.ofSeconds(1)); // Incremento di un secondo.
        timerPicker.setPlaceholder("hh:mm:ss");
        timerPicker.setClearButtonVisible(true);

        timerPicker.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("border-left", "3px solid var(--lumo-success-color-50pct)") // Bordo fisso per i timer.
                .set("flex-grow", "1")
                .set("flex-shrink", "1");

        timerPicker.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        timerPicker.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        timerPicker.addClassName(LumoUtility.Margin.Top.LARGE);
        timerPicker.addClassName(LumoUtility.Padding.SMALL);

        HorizontalLayout timerLayout = new HorizontalLayout();
        timerLayout.setWidthFull();
        timerLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

        // Pulsanti per impostare rapidamente il tempo.
        Button oneMinBtn = new Button("2 min", e -> timerPicker.setValue(LocalTime.of(0, 2, 0)));
        Button fiveMinBtn = new Button("5 min", e -> timerPicker.setValue(LocalTime.of(0, 5, 0)));
        Button tenMinBtn = new Button("10 min", e -> timerPicker.setValue(LocalTime.of(0, 10, 0)));

        for (Button btn : new Button[]{oneMinBtn, fiveMinBtn, tenMinBtn}) {
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            btn.getStyle().set("min-width", "60px");
        }

        HorizontalLayout presetButtons = new HorizontalLayout(oneMinBtn, fiveMinBtn, tenMinBtn);
        presetButtons.setSpacing(true);

        timerLayout.add(timerPicker, presetButtons);
        timerLayout.setFlexGrow(1, timerPicker); // Il timer picker si espande.

        return timerLayout;
    }

    /**
     * Crea e configura un ***Checkbox ({@link Checkbox})** con stili avanzati.
     *
     * @param label Etichetta visualizzata accanto al checkbox.
     * @return Il Checkbox configurato.
     */
    public static Checkbox createCheckbox(String label) {
        Checkbox checkbox = new Checkbox(label);

        checkbox.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("transition", "opacity 0.3s ease-in-out"); // Transizione per l'opacità al passaggio del mouse.

        checkbox.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.opacity = '0.85'; });" +
                        "this.addEventListener('mouseout', function() { this.style.opacity = '1'; });"
        );

        checkbox.addClassName(LumoUtility.Margin.Top.MEDIUM);

        return checkbox;
    }

    /**
     * Crea e configura un ***campo intero ({@link IntegerField}) per la navigazione temporale**
     * con stili avanzati.
     *
     * @param label       Etichetta visualizzata sopra il campo.
     * @param placeholder Testo suggerito visualizzato all'interno del campo quando vuoto.
     * @param required    Se {@code true}, il campo è obbligatorio e ha un bordo primario;
     *                    altrimenti, ha un bordo di successo.
     * @return Il campo intero configurato.
     */
    public static IntegerField createTimeNavigationField(String label, String placeholder, boolean required) {
        IntegerField field = new IntegerField(label);
        field.setMin(0); // Valore minimo consentito.
        field.setStepButtonsVisible(true); // Mostra i pulsanti per incrementare/decrementare.
        field.setWidth("150px"); // Larghezza fissa.
        field.setPlaceholder(placeholder != null ? placeholder : "ID Tempo");

        field.getStyle()
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("flex-grow", "1")
                .set("flex-shrink", "1");

        field.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        field.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        field.addClassName(LumoUtility.Margin.Top.LARGE);
        field.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            field.setRequired(true);
            field.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            field.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return field;
    }

    /**
     * Crea e configura un ***MultiSelectComboBox ({@link MultiSelectComboBox})**
     * con stili avanzati per la selezione multipla di elementi.
     *
     * @param label    Etichetta visualizzata sopra il campo.
     * @param items    Lista di valori disponibili per la selezione.
     * @param required Se {@code true}, il campo è obbligatorio e ha un bordo primario;
     *                 altrimenti, ha un bordo di successo.
     * @return Il MultiSelectComboBox configurato.
     */
    public static MultiSelectComboBox<String> createMultiSelectComboBox(String label, List<String> items, boolean required) {
        MultiSelectComboBox<String> multiSelectComboBox = new MultiSelectComboBox<>(label);
        multiSelectComboBox.setItems(items);
        multiSelectComboBox.setPlaceholder("Seleziona uno o più elementi");
        multiSelectComboBox.setWidthFull();
        multiSelectComboBox.getStyle()
                .set("max-width", "500px")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("flex-grow", "1")
                .set("flex-shrink", "1");

        multiSelectComboBox.getElement().getStyle()
                .set("--lumo-contrast-10pct", "rgba(0, 0, 0, 0.05)");
        multiSelectComboBox.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        multiSelectComboBox.addClassName(LumoUtility.Margin.Top.LARGE);
        multiSelectComboBox.addClassName(LumoUtility.Padding.SMALL);

        if (required) {
            multiSelectComboBox.setRequired(true);
            multiSelectComboBox.getStyle().set("border-left", "3px solid var(--lumo-primary-color)");
        } else {
            multiSelectComboBox.getStyle().set("border-left", "3px solid var(--lumo-success-color-50pct)");
        }

        return multiSelectComboBox;
    }
}