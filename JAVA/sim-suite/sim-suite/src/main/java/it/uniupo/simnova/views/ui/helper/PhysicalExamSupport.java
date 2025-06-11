package it.uniupo.simnova.views.ui.helper;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;
import org.vaadin.tinymce.TinyMce;

import java.util.Map;

/**
 * Classe di supporto per la visualizzazione e modifica delle sezioni dell'esame fisico.
 * Genera una card riassuntiva con tutte le sezioni dell'esame fisico,
 * permettendo la modifica del contenuto di ogni sezione tramite un editor WYSIWYG.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class PhysicalExamSupport {

    /**
     * Costruttore privato per evitare l'istanza della classe.
     * Questa classe è un utility e non dovrebbe essere istanziata.
     */
    private PhysicalExamSupport() {
        // Costruttore privato per evitare l'istanza della classe.
    }

    /**
     * Crea una card riassuntiva che mostra tutte le sezioni dell'esame fisico.
     * Ogni sezione può essere modificata individualmente.
     *
     * @param esame              L'oggetto {@link EsameFisico} da visualizzare e modificare.
     * @param esameFisicoService Il servizio per la gestione degli esami fisici.
     * @param scenarioId         L'ID dello scenario a cui l'esame fisico è associato.
     * @return Un {@link Div} contenente la card dell'esame fisico.
     */
    public static Div getExamCard(EsameFisico esame, EsameFisicoService esameFisicoService, Integer scenarioId) {
        // Assicura che l'oggetto EsameFisico esista per lo scenario.
        if (esame == null) {
            esameFisicoService.addEsameFisico(scenarioId, null);
        }
        // Ricarica l'esame fisico per assicurare che sia aggiornato.
        esame = esameFisicoService.getEsameFisicoById(scenarioId);

        Div examCard = new Div();
        examCard.addClassName("info-card");
        examCard.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("transition", "box-shadow 0.3s ease-in-out")
                .set("width", "80%")
                .set("max-width", "800px");

        // Aggiunge effetti di hover alla card.
        examCard.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        HorizontalLayout examTitleLayout = new HorizontalLayout();
        examTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        examTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        examTitleLayout.setWidthFull();
        examTitleLayout.setSpacing(true);

        Icon examIcon = new Icon(VaadinIcon.STETHOSCOPE);
        examIcon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-xs)")
                .set("border-radius", "50%");

        H3 examTitle = new H3("Esame Fisico");
        examTitle.getStyle()
                .set("margin", "0")
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");

        examTitleLayout.add(examIcon, examTitle);
        examCard.add(examTitleLayout);

        Map<String, String> sections = esame.getSections();
        VerticalLayout examLayout = new VerticalLayout();
        examLayout.setPadding(false);
        examLayout.setSpacing(true);
        examLayout.getStyle().set("margin-top", "var(--lumo-space-m)");
        examLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Aggiunge ogni sezione dell'esame fisico.
        addSection(examLayout, "Generale", sections.get("Generale"), esameFisicoService, scenarioId);
        addSection(examLayout, "Pupille", sections.get("Pupille"), esameFisicoService, scenarioId);
        addSection(examLayout, "Collo", sections.get("Collo"), esameFisicoService, scenarioId);
        addSection(examLayout, "Torace", sections.get("Torace"), esameFisicoService, scenarioId);
        addSection(examLayout, "Cuore", sections.get("Cuore"), esameFisicoService, scenarioId);
        addSection(examLayout, "Addome", sections.get("Addome"), esameFisicoService, scenarioId);
        addSection(examLayout, "Retto", sections.get("Retto"), esameFisicoService, scenarioId);
        addSection(examLayout, "Cute", sections.get("Cute"), esameFisicoService, scenarioId);
        addSection(examLayout, "Estremità", sections.get("Estremità"), esameFisicoService, scenarioId);
        addSection(examLayout, "Neurologico", sections.get("Neurologico"), esameFisicoService, scenarioId);
        addSection(examLayout, "FAST", sections.get("FAST"), esameFisicoService, scenarioId);

        // Aggiunge il layout delle sezioni alla card solo se ci sono componenti.
        if (examLayout.getComponentCount() > 0) {
            examCard.add(examLayout);
        }

        return examCard;
    }

    /**
     * Aggiunge una singola sezione dell'esame fisico al layout fornito.
     * La sezione include un titolo, un'icona, il contenuto testuale e la possibilità di modificarlo.
     *
     * @param content            La {@link VerticalLayout} in cui aggiungere la sezione.
     * @param title              Il titolo della sezione (es. "Generale", "Pupille").
     * @param value              Il contenuto testuale della sezione.
     * @param esameFisicoService Il servizio per la gestione degli esami fisici.
     * @param scenarioId         L'ID dello scenario corrente.
     */
    private static void addSection(VerticalLayout content, String title, String value, EsameFisicoService esameFisicoService, Integer scenarioId) {
        Icon sectionIcon = getSectionIcon(title);
        VerticalLayout sectionLayout = new VerticalLayout();
        sectionLayout.setPadding(false);
        sectionLayout.setSpacing(false);
        sectionLayout.setWidthFull();

        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleGroup = new HorizontalLayout();
        titleGroup.setAlignItems(FlexComponent.Alignment.CENTER);
        sectionIcon.addClassName(LumoUtility.TextColor.PRIMARY);
        sectionIcon.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("font-size", "var(--lumo-icon-size-m)")
                .set("margin-right", "var(--lumo-space-xs)");

        H4 titleLabel = new H4(title);
        titleLabel.addClassNames(LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);
        titleLabel.getStyle().set("font-weight", "600");
        titleGroup.add(sectionIcon, titleLabel);

        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.EDIT, ButtonVariant.LUMO_SMALL, "var(--lumo-base-color");
        editButton.setTooltipText("Modifica " + title);
        editButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        headerRow.add(titleGroup, editButton);
        sectionLayout.add(headerRow);

        Div contentDisplay = new Div();
        contentDisplay.getStyle()
                .set("font-family", "var(--lumo-font-family)")
                .set("line-height", "var(--lumo-line-height-m)")
                .set("color", "var(--lumo-body-text-color)")
                .set("white-space", "pre-wrap") // Mantiene la formattazione (es. a capo).
                .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s) calc(var(--lumo-icon-size-m) + var(--lumo-space-m))")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        // Imposta il testo di visualizzazione, con un messaggio di sezione vuota se il valore è nullo o vuoto.
        String displayValue = (value == null || value.trim().isEmpty())
                ? "<i>Sezione vuota</i>" // Testo italic per sezione vuota.
                : value.replace("\n", "<br />"); // Converte a capo in tag HTML per la visualizzazione.
        contentDisplay.getElement().setProperty("innerHTML", displayValue);
        sectionLayout.add(contentDisplay);

        TinyMce contentEditor = TinyEditor.getEditor(); // Inizializza l'editor WYSIWYG.
        contentEditor.setValue(value != null ? value : "");
        contentEditor.setVisible(false); // Nascosto di default.

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancelButton = new Button("Annulla");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout editorActions = new HorizontalLayout(saveButton, cancelButton);
        editorActions.setVisible(false); // Nascosto di default.
        editorActions.getStyle()
                .set("margin-top", "var(--lumo-space-xs)")
                .set("padding-left", "calc(var(--lumo-icon-size-m) + var(--lumo-space-m))");

        sectionLayout.add(contentEditor, editorActions);

        // Listener per il pulsante "Modifica": mostra l'editor e nasconde il display.
        editButton.addClickListener(e -> {
            contentDisplay.setVisible(false);
            // Prepara il contenuto per l'editor, riconvertendo i tag HTML in a capo.
            String currentContent = value != null && !value.trim().isEmpty()
                    ? contentDisplay.getElement().getProperty("innerHTML").replace("<br />", "\n").replace("<br>", "\n")
                    : "";
            contentEditor.setValue(currentContent);
            contentEditor.setVisible(true);
            editorActions.setVisible(true);
            editButton.setVisible(false);
        });

        // Listener per il pulsante "Salva": aggiorna il contenuto e ripristina il display.
        saveButton.addClickListener(e -> {
            String newContent = contentEditor.getValue();
            String displayContent = newContent.trim().isEmpty()
                    ? "<i>Sezione vuota</i>"
                    : newContent.replace("\n", "<br />");
            contentDisplay.getElement().setProperty("innerHTML", displayContent);
            // Salva la modifica tramite il servizio.
            esameFisicoService.updateSingleEsameFisico(scenarioId, title, newContent);
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
            Notification.show("Sezione " + title + " aggiornata.", 3000, Notification.Position.BOTTOM_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        // Listener per il pulsante "Annulla": ripristina il display senza salvare.
        cancelButton.addClickListener(e -> {
            contentEditor.setVisible(false);
            editorActions.setVisible(false);
            contentDisplay.setVisible(true);
            editButton.setVisible(true);
        });

        content.add(sectionLayout);
    }

    /**
     * Restituisce un'icona {@link Icon} appropriata per la sezione dell'esame fisico specificata.
     *
     * @param sectionTitle Il titolo della sezione (es. "Generale", "Cuore").
     * @return L'icona corrispondente alla sezione.
     */
    private static Icon getSectionIcon(String sectionTitle) {
        return switch (sectionTitle) {
            case "Generale" -> new Icon(VaadinIcon.CLIPBOARD_PULSE);
            case "Pupille" -> new Icon(VaadinIcon.EYE);
            case "Collo" -> new Icon(VaadinIcon.USER);
            case "Torace" -> FontAwesome.Solid.LUNGS.create();
            case "Cuore" -> new Icon(VaadinIcon.HEART);
            case "Addome" -> FontAwesome.Solid.A.create(); // Icona generica per 'Addome'
            case "Retto" -> FontAwesome.Solid.POOP.create();
            case "Cute" -> FontAwesome.Solid.HAND_DOTS.create();
            case "Estremità" -> FontAwesome.Solid.HANDS.create();
            case "Neurologico" -> FontAwesome.Solid.BRAIN.create();
            case "FAST" -> new Icon(VaadinIcon.AMBULANCE);
            default -> new Icon(VaadinIcon.INFO); // Icona di default per sezioni non mappate.
        };
    }
}