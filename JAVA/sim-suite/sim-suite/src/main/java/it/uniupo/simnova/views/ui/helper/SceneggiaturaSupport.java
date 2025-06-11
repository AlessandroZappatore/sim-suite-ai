package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.vaadin.tinymce.TinyMce;

/**
 * Classe di supporto per la gestione e visualizzazione della sceneggiatura di uno scenario simulato.
 * Fornisce un componente UI che permette di visualizzare e modificare il testo della sceneggiatura
 * tramite un editor WYSIWYG.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class SceneggiaturaSupport extends HorizontalLayout {

    /**
     * Costruttore privato per evitare istanziazioni dirette della classe.
     */
    private SceneggiaturaSupport() {
        // Costruttore privato per evitare istanziazioni dirette.
    }

    /**
     * Crea un componente per visualizzare e modificare la sceneggiatura.
     * La sceneggiatura è presentata in una card con titolo, icona e pulsanti di modifica.
     *
     * @param scenarioId                      L'ID dello scenario a cui è associata la sceneggiatura.
     * @param sceneggiaturaText               Il testo attuale della sceneggiatura.
     * @param patientSimulatedScenarioService Il servizio per la gestione degli scenari simulati con paziente.
     * @return Un {@link Component} (VerticalLayout) che rappresenta la sezione della sceneggiatura.
     */
    public static Component createSceneggiaturaContent(Integer scenarioId, String sceneggiaturaText,
                                                       PatientSimulatedScenarioService patientSimulatedScenarioService) {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(false);
        mainLayout.setWidthFull();
        mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Array per mantenere un riferimento mutabile al testo della sceneggiatura
        final String[] currentSceneggiatura = {sceneggiaturaText};

        Div sceneggiaturaCard = new Div();
        sceneggiaturaCard.setId("sceneggiatura-view-card");
        sceneggiaturaCard.addClassName("sceneggiatura-card");
        sceneggiaturaCard.getStyle()
                .set("width", "100%")
                .set("max-width", "800px")
                .set("margin", "var(--lumo-space-l) 0")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "var(--lumo-space-l)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
                .set("box-sizing", "border-box");

        // Aggiunge effetti di hover alla card.
        sceneggiaturaCard.getElement().executeJs(
                "this.addEventListener('mouseover', function() {" +
                        "  this.style.transform = 'translateY(-2px)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-l)';" +
                        "});" +
                        "this.addEventListener('mouseout', function() {" +
                        "  this.style.transform = 'translateY(0)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-m)';" +
                        "});"
        );

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setPadding(false);
        headerLayout.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("padding-bottom", "var(--lumo-space-m)");

        Icon scriptIcon = new Icon(VaadinIcon.FILE_TEXT_O);
        scriptIcon.addClassName(LumoUtility.TextColor.PRIMARY);
        scriptIcon.getStyle()
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("margin-right", "var(--lumo-space-m)");

        H3 scriptTitle = new H3("Sceneggiatura");
        scriptTitle.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        scriptTitle.getStyle().set("font-weight", "600");

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SUCCESS, "var(--lumo-base-color)");
        editButton.setTooltipText("Modifica la sceneggiatura");
        editButton.getStyle().set("margin-left", "auto"); // Spinge il pulsante a destra.

        headerLayout.add(scriptIcon, scriptTitle, editButton);
        sceneggiaturaCard.add(headerLayout);

        Div scriptTextDisplay = new Div();
        scriptTextDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-l)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap") // Mantiene la formattazione (es. a capo).
                .set("padding", "var(--lumo-space-m)")
                .set("max-height", "60vh")
                .set("overflow-y", "auto")
                .set("border-left", "3px solid var(--lumo-primary-color)")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        // Imposta il testo visualizzato o un messaggio di "non disponibile".
        if (sceneggiaturaText == null || sceneggiaturaText.trim().isEmpty()) {
            scriptTextDisplay.getElement().setProperty("innerHTML", "Sceneggiatura non disponibile");
        } else {
            scriptTextDisplay.getElement().setProperty("innerHTML", sceneggiaturaText.replace("\n", "<br />"));
        }
        sceneggiaturaCard.add(scriptTextDisplay);

        TinyMce editor = TinyEditor.getEditor(); // Inizializza l'editor WYSIWYG.
        editor.setValue((sceneggiaturaText == null || sceneggiaturaText.trim().isEmpty()) ? "" : sceneggiaturaText);
        editor.setVisible(false); // Nascosto di default.
        editor.getStyle()
                .set("width", "100%")
                .set("padding", "var(--lumo-space-m)")
                .set("max-height", "60vh")
                .set("overflow-y", "auto")
                .set("border-left", "3px solid var(--lumo-primary-color)")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("box-sizing", "border-box");
        sceneggiaturaCard.add(editor);

        Button saveButton = new Button("Salva", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        Button cancelButton = new Button("Annulla", new Icon(VaadinIcon.CLOSE_SMALL));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setVisible(false); // Nascosto di default.
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        sceneggiaturaCard.add(buttonLayout);

        // Listener per il pulsante "Modifica": mostra l'editor e i pulsanti, nasconde il display.
        editButton.addClickListener(e -> {
            scriptTextDisplay.setVisible(false);
            editor.setVisible(true);
            buttonLayout.setVisible(true);
            editor.setValue((currentSceneggiatura[0] == null || currentSceneggiatura[0].trim().isEmpty()) ? "" : currentSceneggiatura[0]);
        });

        // Listener per il pulsante "Annulla": nasconde l'editor e ripristina il display.
        cancelButton.addClickListener(e -> {
            editor.setVisible(false);
            buttonLayout.setVisible(false);
            scriptTextDisplay.setVisible(true);
        });

        // Listener per il pulsante "Salva": aggiorna la sceneggiatura e ripristina il display.
        saveButton.addClickListener(e -> {
            String updatedText = editor.getValue();
            // Salva la sceneggiatura tramite il servizio.
            patientSimulatedScenarioService.updateScenarioSceneggiatura(scenarioId, updatedText);

            currentSceneggiatura[0] = updatedText; // Aggiorna il riferimento locale.

            // Aggiorna il testo visualizzato nel display.
            if (currentSceneggiatura[0] == null || currentSceneggiatura[0].trim().isEmpty()) {
                scriptTextDisplay.getElement().setProperty("innerHTML", "Sceneggiatura non disponibile");
            } else {
                scriptTextDisplay.getElement().setProperty("innerHTML", currentSceneggiatura[0].replace("\n", "<br />"));
            }

            editor.setVisible(false);
            buttonLayout.setVisible(false);
            scriptTextDisplay.setVisible(true);
            Notification.show("Sceneggiatura aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        mainLayout.add(sceneggiaturaCard);

        return mainLayout;
    }
}