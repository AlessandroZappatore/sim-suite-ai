package it.uniupo.simnova.views.ui.helper;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Classe di utility per la visualizzazione e modifica delle informazioni principali di uno scenario.
 * Genera "badge" interattivi per campi come paziente, patologia, durata e target,
 * permettendone la modifica direttamente in linea o tramite dialog.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class InfoSupport extends HorizontalLayout {

    /**
     * Mappa che associa etichette di campo a icone FontAwesome.
     */
    private static final Map<String, Supplier<Icon>> labelIconMap = new HashMap<>();
    /**
     * Mappa che associa tipologie di paziente a icone FontAwesome.
     */
    private static final Map<String, Supplier<Icon>> tipologiaIconMap = new HashMap<>();
    /**
     * Lista delle opzioni disponibili per la tipologia di paziente.
     */
    private static final List<String> TIPOLOGIA_OPTIONS = Arrays.asList("Adulto", "Pediatrico", "Neonatale", "Prematuro");
    /**
     * Lista delle opzioni disponibili per la durata del timer generale.
     * Queste opzioni sono in minuti e possono essere utilizzate per selezionare la durata di un evento.
     */
    private static final List<Integer> DURATION_OPTIONS = List.of(5, 10, 15, 20, 25, 30);

    // Inizializzazione statica delle mappe di icone
    static {
        labelIconMap.put("Paziente", FontAwesome.Solid.USER_INJURED::create);
        labelIconMap.put("Patologia", FontAwesome.Solid.DISEASE::create);
        labelIconMap.put("Durata", FontAwesome.Solid.STOPWATCH_20::create);
        labelIconMap.put("Target", FontAwesome.Solid.BULLSEYE::create);

        tipologiaIconMap.put("Adulto", FontAwesome.Solid.USER::create);
        tipologiaIconMap.put("Pediatrico", FontAwesome.Solid.CHILD::create);
        tipologiaIconMap.put("Neonatale", FontAwesome.Solid.BABY::create);
        tipologiaIconMap.put("Prematuro", FontAwesome.Solid.HANDS_HOLDING_CHILD::create);
    }

    /**
     * Costruttore privato per evitare istanziazioni dirette della classe.
     * Utilizzare i metodi statici per ottenere i badge informativi.
     */
    private InfoSupport() {
        // Costruttore privato per evitare istanziazioni dirette
    }

    /**
     * Applica stili CSS a un layout di badge, inclusi colori ed effetti al passaggio del mouse.
     *
     * @param badgeLayout Il layout orizzontale che rappresenta il badge.
     * @param textSpan    Lo Span che contiene il testo del badge.
     * @param color       Il colore base per il badge.
     * @param isEmpty     Indica se il badge rappresenta un valore vuoto, per differenziare lo stile.
     */
    private static void styleBadge(HorizontalLayout badgeLayout, Span textSpan, String color, boolean isEmpty) {
        badgeLayout.getStyle()
                .set("background-color", color + (isEmpty ? "08" : "10")) // Sfondo più chiaro se vuoto
                .set("border-radius", "16px")
                .set("padding", "6px 10px 6px 16px")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("border", "1px solid " + color + (isEmpty ? "30" : "40")) // Bordo più chiaro se vuoto
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");

        textSpan.getStyle()
                .set("color", color)
                .set("font-size", "16px")
                .set("font-weight", "500");

        // Aggiunge effetti di ombra al passaggio del mouse tramite JavaScript
        badgeLayout.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.boxShadow = '0 3px 6px rgba(0,0,0,0.15)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)'; " +
                        "});"
        );
    }

    /**
     * Formatta il testo da visualizzare all'interno del badge.
     *
     * @param label     L'etichetta del campo (es. "Durata").
     * @param value     Il valore effettivo del campo.
     * @param emptyText Il testo da mostrare se il valore è vuoto (es. "N/D").
     * @return La stringa formattata per il badge.
     */
    private static String formatBadgeText(String label, String value, String emptyText) {
        if (value != null && !value.trim().isEmpty()) {
            if ("Durata".equals(label)) {
                return value + " min"; // Aggiunge " min" per la durata
            }
            return label + ": " + value;
        } else {
            return label + ": " + emptyText;
        }
    }

    /**
     * Verifica se una stringa è nulla o vuota (dopo aver rimosso gli spazi).
     *
     * @param value La stringa da controllare.
     * @return {@code true} se la stringa è considerata vuota, {@code false} altrimenti.
     */
    private static boolean isValueEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Crea un badge generico che può essere modificato in linea tramite un TextField.
     *
     * @param scenario           Lo scenario di riferimento.
     * @param label              L'etichetta del campo (es. "Paziente").
     * @param rawValueGetter     Funzione per ottenere il valore grezzo del campo dallo scenario.
     * @param displayValueGetter Funzione per ottenere il valore formattato per la visualizzazione.
     * @param emptyText          Testo da mostrare se il valore è vuoto.
     * @param badgeColor         Colore di base del badge.
     * @param scenarioService    Servizio per l'aggiornamento del campo.
     * @return Un componente {@link HorizontalLayout} contenente il badge modificabile.
     */
    private static Component createEditableBadgeInternal(
            Scenario scenario,
            String label,
            SerializableFunction<Scenario, String> rawValueGetter,
            SerializableFunction<Scenario, String> displayValueGetter,
            String emptyText,
            String badgeColor,
            ScenarioService scenarioService) {

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        // Aggiunge l'icona se presente nella mappa
        Icon icon = labelIconMap.containsKey(label) ? labelIconMap.get(label).get() : null;
        if (icon != null) {
            icon.getStyle().set("margin-right", "8px");
            icon.addClassName(LumoUtility.TextColor.PRIMARY);
            badgeViewLayout.add(icon);
        }

        Span actualBadgeTextSpan = new Span();
        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");

        badgeViewLayout.add(actualBadgeTextSpan, editButton);
        badgeViewLayout.expand(actualBadgeTextSpan);

        TextField editField = FieldGenerator.createTextField(label, null, true);
        editField.getStyle().set("flex-grow", "1");

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editControlsLayout = new HorizontalLayout(editField, saveButton, cancelButton);
        editControlsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        editControlsLayout.setVisible(false); // Nascosto di default
        editControlsLayout.setWidthFull();

        // Runnable per aggiornare l'aspetto del badge
        Runnable updateBadgeAppearance = () -> {
            String displayValue = displayValueGetter.apply(scenario);
            boolean isEmpty = isValueEmpty(displayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, displayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);
        };

        updateBadgeAppearance.run(); // Esegue all'inizializzazione
        itemContainer.add(badgeViewLayout, editControlsLayout);

        // Listener per il pulsante "Modifica"
        editButton.addClickListener(e -> {
            badgeViewLayout.setVisible(false); // Nasconde il badge
            editControlsLayout.setVisible(true); // Mostra i controlli di modifica
            editField.setValue(rawValueGetter.apply(scenario) != null ? rawValueGetter.apply(scenario) : ""); // Popola il campo
            editField.focus();
        });

        // Listener per il pulsante "Annulla"
        cancelButton.addClickListener(e -> {
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            updateBadgeAppearance.run(); // Ripristina l'aspetto originale del badge
        });

        saveButton.addClickListener(e -> {
            String newValue = editField.getValue();
            scenarioService.updateSingleField(scenario.getId(), label, newValue);

            Notification.show(label + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Ricarica la pagina per riflettere le modifiche
            UI.getCurrent().getPage().reload();
        });
        return itemContainer;
    }

    /**
     * Crea un badge modificabile per campi di tipo String.
     *
     * @param scenario        Lo scenario.
     * @param label           L'etichetta del campo.
     * @param getter          Funzione per ottenere il valore dello scenario.
     * @param emptyText       Testo se il valore è vuoto.
     * @param badgeColor      Colore del badge.
     * @param scenarioService Servizio per aggiornare lo scenario.
     * @return Un componente {@link HorizontalLayout} contenente il badge.
     */
    private static Component createStringEditableBadge(Scenario scenario, String label,
                                                       SerializableFunction<Scenario, String> getter,
                                                       String emptyText, String badgeColor, ScenarioService scenarioService) {
        return createEditableBadgeInternal(scenario, label, getter, getter, emptyText, badgeColor, scenarioService);
    }

    /**
     * Crea un badge modificabile per il campo "Durata" utilizzando un ComboBox.
     *
     * @param scenario        Lo scenario.
     * @param numericGetter   Funzione per ottenere il valore numerico della durata.
     * @param emptyText       Testo se il valore è vuoto.
     * @param badgeColor      Colore del badge.
     * @param scenarioService Servizio per aggiornare lo scenario.
     * @return Un componente {@link HorizontalLayout} contenente il badge.
     */
    private static Component createDurationSelectBadge(
            Scenario scenario,
            SerializableFunction<Scenario, Number> numericGetter,
            String emptyText,
            String badgeColor,
            ScenarioService scenarioService) {

        String label = "Durata";

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        Icon icon = labelIconMap.getOrDefault(label, FontAwesome.Solid.QUESTION_CIRCLE::create).get();
        if (icon != null) {
            icon.getStyle().set("margin-right", "8px");
            icon.addClassName(LumoUtility.TextColor.PRIMARY);
            badgeViewLayout.add(icon);
        }

        Span actualBadgeTextSpan = new Span();
        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");

        badgeViewLayout.add(actualBadgeTextSpan, editButton);
        badgeViewLayout.expand(actualBadgeTextSpan);

        // ComboBox per la selezione della durata
        ComboBox<Integer> durationSelect = FieldGenerator.createComboBox("Durata", DURATION_OPTIONS,
                (scenario.getTimerGenerale() > 0) ? Math.round(scenario.getTimerGenerale()) : null,
                true);

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout editControlsLayout = new HorizontalLayout(durationSelect, saveButton, cancelButton);
        editControlsLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        editControlsLayout.setVisible(false);
        editControlsLayout.setWidthFull();

        // Funzione per formattare il valore numerico per la visualizzazione
        SerializableFunction<Scenario, String> displayValueFormatter = s -> {
            Number val = numericGetter.apply(s);
            if (val != null) {
                int intVal = val.intValue();
                if (val.doubleValue() == intVal && intVal > 0) {
                    return String.valueOf(intVal);
                } else if (val.doubleValue() > 0) {
                    return String.valueOf(intVal); // Mostra intero anche se float ma maggiore di 0
                }
            }
            return null;
        };

        Runnable updateBadgeAppearance = () -> {
            String currentDisplayValue = displayValueFormatter.apply(scenario);
            boolean isEmpty = isValueEmpty(currentDisplayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, currentDisplayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);
        };

        updateBadgeAppearance.run();
        itemContainer.add(badgeViewLayout, editControlsLayout);

        editButton.addClickListener(e -> {
            badgeViewLayout.setVisible(false);
            editControlsLayout.setVisible(true);
            Number currentValue = numericGetter.apply(scenario);
            if (currentValue != null) {
                int currentIntValue = currentValue.intValue();
                if (currentValue.doubleValue() == currentIntValue && DURATION_OPTIONS.contains(currentIntValue)) {
                    durationSelect.setValue(currentIntValue);
                } else {
                    durationSelect.clear(); // Pulisce se il valore non è tra le opzioni valide
                }
            } else {
                durationSelect.clear();
            }
            durationSelect.focus();
        });

        cancelButton.addClickListener(e -> {
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            updateBadgeAppearance.run();
        });

        saveButton.addClickListener(e -> {
            Integer selectedValue = durationSelect.getValue();
            // Converte il valore selezionato in String per il salvataggio
            String valueToSaveAndDisplay = (selectedValue != null) ? String.valueOf(selectedValue) : null;

            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            scenarioService.updateSingleField(scenario.getId(), label, valueToSaveAndDisplay); // Aggiorna il campo

            Notification.show(label + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().getPage().reload();
        });
        return itemContainer;
    }

    /**
     * Crea un badge modificabile per la "Tipologia" del paziente utilizzando un Select.
     * Include anche la gestione dinamica dell'icona in base al tipo di paziente selezionato.
     *
     * @param scenario           Lo scenario.
     * @param displayValueGetter Funzione per ottenere il valore di visualizzazione della tipologia.
     * @param emptyText          Testo se il valore è vuoto.
     * @param badgeColor         Colore del badge.
     * @param scenarioService    Servizio per aggiornare lo scenario.
     * @return Un componente {@link HorizontalLayout} contenente il badge.
     */
    private static Component createTipologiaSelectBadge(
            Scenario scenario,
            SerializableFunction<Scenario, String> displayValueGetter,
            String emptyText,
            String badgeColor,
            ScenarioService scenarioService) {

        String label = "Tipologia";

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        Span actualBadgeTextSpan = new Span();
        Button editButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");

        // Icona iniziale basata sulla tipologia corrente dello scenario
        Icon tipologiaIcon = tipologiaIconMap.getOrDefault(displayValueGetter.apply(scenario), FontAwesome.Solid.USERS::create).get();
        tipologiaIcon.getStyle().set("margin-right", "8px");
        tipologiaIcon.addClassName(LumoUtility.TextColor.PRIMARY);
        badgeViewLayout.add(tipologiaIcon);

        badgeViewLayout.add(actualBadgeTextSpan, editButton);
        badgeViewLayout.expand(actualBadgeTextSpan);

        // Select per la selezione della tipologia
        Select<String> selectBox = FieldGenerator.createSelect(label, TIPOLOGIA_OPTIONS, scenario.getTipologia(), true);
        selectBox.getStyle().set("flex-grow", "1");

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

        HorizontalLayout editControlsLayout = new HorizontalLayout(selectBox, saveButton, cancelButton);
        editControlsLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        editControlsLayout.setVisible(false);
        editControlsLayout.setWidthFull();

        Runnable updateBadgeAppearance = () -> {
            String displayValue = displayValueGetter.apply(scenario);
            boolean isEmpty = isValueEmpty(displayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, displayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);

            // Aggiorna l'icona del badge in base alla tipologia corrente
            Icon currentIconToSet = tipologiaIconMap.getOrDefault(displayValue, FontAwesome.Solid.USERS::create).get();
            currentIconToSet.getStyle().set("margin-right", "8px");
            currentIconToSet.addClassName(LumoUtility.TextColor.PRIMARY);
            // Sostituisce l'icona esistente o la aggiunge come primo componente
            if (badgeViewLayout.getComponentCount() > 1 && badgeViewLayout.getComponentAt(0) instanceof Icon) {
                badgeViewLayout.replace(badgeViewLayout.getComponentAt(0), currentIconToSet);
            } else {
                badgeViewLayout.addComponentAsFirst(currentIconToSet);
            }
        };

        updateBadgeAppearance.run();
        itemContainer.add(badgeViewLayout, editControlsLayout);

        editButton.addClickListener(e -> {
            badgeViewLayout.setVisible(false);
            editControlsLayout.setVisible(true);
            String currentTipologia = displayValueGetter.apply(scenario);
            selectBox.setValue(currentTipologia);
            selectBox.focus();
        });

        cancelButton.addClickListener(e -> {
            editControlsLayout.setVisible(false);
            badgeViewLayout.setVisible(true);
            updateBadgeAppearance.run();
        });

        saveButton.addClickListener(e -> {
            String newSelectedValue = selectBox.getValue();
            scenarioService.updateSingleField(scenario.getId(), label, newSelectedValue); // Aggiorna il campo
            Notification.show(label + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Ricarica la pagina per applicare le modifiche alla tipologia (che potrebbe influenzare altri elementi UI)
            UI.getCurrent().getPage().reload();
        });
        return itemContainer;
    }

    /**
     * Crea un badge che, al click del pulsante di modifica, apre un dialog con un IFrame
     * per modificare il Target dello scenario in una pagina separata.
     *
     * @param scenario           Lo scenario.
     * @param displayValueGetter Funzione per ottenere il valore di visualizzazione del Target.
     * @param emptyText          Testo se il valore è vuoto.
     * @param badgeColor         Colore del badge.
     * @return Un componente {@link HorizontalLayout} contenente il badge.
     */
    private static Component createTargetDialogBadge(
            Scenario scenario,
            SerializableFunction<Scenario, String> displayValueGetter,
            String emptyText,
            String badgeColor) {
        String label = "Target";

        // Messaggio di errore se l'ID scenario non è valido
        if (scenario.getId() < 0) {
            Span errorSpan = new Span("ID Scenario non disponibile per Target");
            errorSpan.getStyle().set("color", "red");
            return errorSpan;
        }

        HorizontalLayout itemContainer = new HorizontalLayout();
        itemContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        itemContainer.getStyle().set("margin", "6px");

        HorizontalLayout badgeViewLayout = new HorizontalLayout();
        badgeViewLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        badgeViewLayout.setSpacing(false);
        badgeViewLayout.getStyle().set("cursor", "default");

        Icon icon = labelIconMap.getOrDefault(label, FontAwesome.Solid.QUESTION_CIRCLE::create).get();
        icon.getStyle().set("margin-right", "8px");
        icon.addClassName(LumoUtility.TextColor.PRIMARY);

        Span actualBadgeTextSpan = new Span();
        Button openDialogButton = StyleApp.getButton("", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        openDialogButton.getStyle().set("margin-left", "auto"); // Spinge il pulsante a destra
        openDialogButton.getElement().setAttribute("title", "Modifica Target in finestra");

        Runnable updateBadgeAppearance = () -> {
            String displayValue = displayValueGetter.apply(scenario);
            boolean isEmpty = isValueEmpty(displayValue);
            actualBadgeTextSpan.setText(formatBadgeText(label, displayValue, emptyText));
            styleBadge(badgeViewLayout, actualBadgeTextSpan, badgeColor, isEmpty);
            // Assicura che l'icona sia sempre presente come primo componente
            if (!(badgeViewLayout.getComponentCount() > 0 && badgeViewLayout.getComponentAt(0) instanceof Icon)) {
                badgeViewLayout.addComponentAsFirst(icon);
            }
        };

        updateBadgeAppearance.run(); // Esegue all'inizializzazione

        badgeViewLayout.add(actualBadgeTextSpan, openDialogButton);
        badgeViewLayout.expand(actualBadgeTextSpan); // Espande il testo per riempire lo spazio

        itemContainer.add(badgeViewLayout); // Aggiunge il layout del badge al contenitore

        // Listener per l'apertura del dialog di modifica target
        openDialogButton.addClickListener(e -> {
            String targetId = String.valueOf(scenario.getId());
            String iframeUrl = "target/" + targetId + "/edit"; // URL dell'editor del target

            IFrame iframe = new IFrame(iframeUrl);
            iframe.setWidth("100%");
            iframe.setHeight("100%");
            iframe.getStyle().set("border", "none");

            Dialog dialog = new Dialog();
            dialog.add(iframe); // Aggiunge l'iframe al dialog

            dialog.setWidth("80vw"); // Larghezza dell'80% della viewport
            dialog.setHeight("70vh"); // Altezza del 70% della viewport
            dialog.setModal(true);
            dialog.setDraggable(true);
            dialog.setResizable(true);
            dialog.setCloseOnEsc(true);
            dialog.setCloseOnOutsideClick(false);

            // Header personalizzato per il dialog
            HorizontalLayout headerLayout = new HorizontalLayout();
            Span dialogTitle = new Span("Modifica Target: " + displayValueGetter.apply(scenario));
            Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), event -> dialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            headerLayout.add(dialogTitle, closeButton);
            headerLayout.setFlexGrow(1, dialogTitle);
            headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            headerLayout.setWidthFull();
            headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            // Sostituisce l'header predefinito del dialog con quello personalizzato
            if (dialog.getHeader() != null) {
                dialog.getHeader().removeAll();
                dialog.getHeader().add(headerLayout);
            }

            // Listener per la chiusura del dialog
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) { // Quando il dialog si chiude
                    updateBadgeAppearance.run(); // Aggiorna il badge
                    Notification.show("Finestra Modifica Target chiusa.", 2000, Notification.Position.BOTTOM_START);
                    UI.getCurrent().getPage().reload(); // Ricarica la pagina per aggiornare completamente i dati
                }
            });

            dialog.open();
            Notification.show("Apertura finestra per modifica Target...", 2000, Notification.Position.BOTTOM_START);
        });

        return itemContainer;
    }

    /**
     * Crea un layout orizzontale contenente tutti i "badge" informativi principali dello scenario.
     * Ogni badge è cliccabile e permette la modifica in linea o l'apertura di un dialog specifico.
     *
     * @param scenario        L'oggetto {@link Scenario} di cui visualizzare le informazioni.
     * @param scenarioService Il servizio per la gestione dello scenario, usato per gli aggiornamenti.
     * @return Un componente {@link HorizontalLayout} che aggrega tutti i badge informativi.
     */
    public static Component getInfo(Scenario scenario, ScenarioService scenarioService) {
        HorizontalLayout badgesContainer = new HorizontalLayout();
        badgesContainer.setWidthFull();
        badgesContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        badgesContainer.setSpacing(false);
        badgesContainer.getStyle().set("flex-wrap", "wrap"); // Permette ai badge di andare a capo

        String badgeColor = "var(--lumo-primary-color)";
        String emptyDefaultText = "N/D";

        // Aggiunta dei badge per le diverse informazioni dello scenario
        badgesContainer.add(createStringEditableBadge(scenario, "Paziente",
                Scenario::getNomePaziente, // Getter per il valore grezzo
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createTipologiaSelectBadge(scenario,
                Scenario::getTipologia, // Getter per il valore di visualizzazione
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createStringEditableBadge(scenario, "Patologia",
                Scenario::getPatologia,
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createDurationSelectBadge(scenario,
                Scenario::getTimerGenerale,
                emptyDefaultText, badgeColor, scenarioService));

        badgesContainer.add(createTargetDialogBadge(scenario,
                Scenario::getTarget,
                emptyDefaultText, badgeColor));

        return badgesContainer;
    }
}