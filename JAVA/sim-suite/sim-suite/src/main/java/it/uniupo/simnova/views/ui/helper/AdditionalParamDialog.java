package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static it.uniupo.simnova.views.constant.AdditionParametersConst.ADDITIONAL_PARAMETERS;
import static it.uniupo.simnova.views.constant.AdditionParametersConst.CUSTOM_PARAMETER_KEY;

/**
 * Classe di utility per la gestione dei dialog di selezione e aggiunta di parametri aggiuntivi.
 * Permette agli utenti di scegliere tra parametri predefiniti o di crearne di personalizzati
 * per una specifica sezione temporale di uno scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class AdditionalParamDialog {

    /**
     * Costruttore privato per evitare l'istanza diretta di questa utility.
     */
    public AdditionalParamDialog() {
        // Costruttore vuoto, non necessario per questa utility
    }
    /**
     * Mostra un dialog per selezionare parametri aggiuntivi predefiniti per una data sezione temporale.
     * Permette anche di cercare tra i parametri disponibili e di accedere alla creazione di parametri personalizzati.
     *
     * @param timeSection La sezione temporale a cui verranno aggiunti i parametri.
     */
    public static void showAdditionalParamsDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleziona Parametri Aggiuntivi per T" + timeSection.getTimeNumber());
        dialog.setWidth("600px");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca parametri...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);

        Button addCustomParamButton = new Button("Crea Nuovo Parametro Personalizzato",
                new Icon(VaadinIcon.PLUS_CIRCLE));
        addCustomParamButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addCustomParamButton.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        addCustomParamButton.addClickListener(e -> {
            dialog.close(); // Chiude il dialog corrente
            showCustomParamDialog(timeSection); // Apre il dialog per il parametro personalizzato
        });

        // Ottiene le chiavi dei parametri già selezionati nella sezione temporale
        Set<String> alreadySelectedKeys = timeSection.getCustomParameters().keySet();

        // Filtra i parametri predefiniti per escludere quelli già presenti
        List<String> availableParamsLabels = ADDITIONAL_PARAMETERS.entrySet().stream()
                .filter(entry -> !alreadySelectedKeys.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        CheckboxGroup<String> paramsSelector = new CheckboxGroup<>();
        paramsSelector.setItems(availableParamsLabels);
        paramsSelector.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        paramsSelector.setWidthFull();
        paramsSelector.getStyle().set("max-height", "300px").set("overflow-y", "auto");

        // Listener per il campo di ricerca: filtra i parametri visualizzati
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue() != null ? e.getValue().trim().toLowerCase() : "";
            List<String> filteredParams = availableParamsLabels.stream()
                    .filter(paramLabel -> paramLabel.toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
            paramsSelector.setItems(filteredParams);
        });

        Button confirmButton = new Button("Aggiungi Selezionati", e -> {
            paramsSelector.getSelectedItems().forEach(selectedLabel -> {
                // Trova la chiave del parametro basandosi sulla label selezionata
                String paramKey = ADDITIONAL_PARAMETERS.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(selectedLabel))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse("");

                if (!paramKey.isEmpty()) {
                    // Estrae l'unità di misura dalla label, se presente (es. "Pressione (mmHg)")
                    String unit = "";
                    if (selectedLabel.contains("(") && selectedLabel.contains(")")) {
                        try {
                            unit = selectedLabel.substring(selectedLabel.indexOf("(") + 1, selectedLabel.indexOf(")"));
                        } catch (IndexOutOfBoundsException ex) {
                            unit = ""; // Nessuna unità trovata o formato non valido
                        }
                    }
                    // Aggiunge il parametro alla sezione temporale
                    timeSection.addCustomParameter(paramKey, selectedLabel, unit);
                }
            });
            dialog.close(); // Chiude il dialog dopo l'aggiunta
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(
                addCustomParamButton,
                searchField,
                new Paragraph("Seleziona dai parametri predefiniti:"),
                paramsSelector
        );
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);

        dialog.add(dialogContent, buttonsLayout);
        dialog.open();
    }

    /**
     * Mostra un dialog per aggiungere un nuovo parametro completamente personalizzato.
     * Permette all'utente di definire il nome, l'unità di misura (opzionale) e il valore iniziale (opzionale).
     *
     * @param timeSection La sezione temporale a cui aggiungere il parametro personalizzato.
     */
    private static void showCustomParamDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi Parametro Personalizzato a T" + timeSection.getTimeNumber());
        dialog.setWidth("450px");

        TextField nameField = new TextField("Nome parametro");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        nameField.setErrorMessage("Il nome del parametro è obbligatorio");

        TextField unitField = new TextField("Unità di misura (opzionale)");
        unitField.setWidthFull();

        NumberField valueField = new NumberField("Valore iniziale (opzionale)");
        valueField.setWidthFull();

        Button saveButton = new Button("Salva Parametro", e -> {
            String paramName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            String unit = unitField.getValue() != null ? unitField.getValue().trim() : "";
            Double initialValue = valueField.getValue();

            // Validazione del nome del parametro
            if (paramName.isEmpty()) {
                nameField.setInvalid(true);
                Notification.show(nameField.getErrorMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            nameField.setInvalid(false);

            // Genera una chiave univoca per il parametro personalizzato
            String paramKey = CUSTOM_PARAMETER_KEY + "_" + paramName.replaceAll("\\s+", "_");

            // Controlla se un parametro con la stessa chiave esiste già
            if (timeSection.getCustomParameters().containsKey(paramKey)) {
                Notification.show("Un parametro con questo nome esiste già.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            // Costruisce la label completa del parametro (es. "Temperatura (°C)")
            String fullLabel = paramName + (unit.isEmpty() ? "" : " (" + unit + ")");

            // Aggiunge il parametro personalizzato alla sezione temporale
            timeSection.addCustomParameter(paramKey, fullLabel, unit);

            // Imposta il valore iniziale, se fornito, altrimenti 0.0
            if (initialValue != null && timeSection.getCustomParameters().containsKey(paramKey)) {
                timeSection.getCustomParameters().get(paramKey).setValue(initialValue);
            } else if (timeSection.getCustomParameters().containsKey(paramKey)) {
                timeSection.getCustomParameters().get(paramKey).setValue(0.0);
            }

            dialog.close(); // Chiude il dialog dopo il salvataggio
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogContent = new VerticalLayout(
                new Paragraph("Definisci un nuovo parametro non presente nella lista:"),
                nameField,
                unitField,
                valueField
        );
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);

        dialog.add(dialogContent, buttonsLayout);
        dialog.open();
    }
}