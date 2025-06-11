package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Classe di utility per la creazione di un componente visuale che indica
 * la mancanza di contenuto o la presenza di un errore.
 * Fornisce un layout standardizzato per i "messaggi di stato vuoto" (empty state).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class EmptySupport extends HorizontalLayout {

    /**
     * Costruttore vuoto per la classe EmptySupport.
     */
    public EmptySupport() {
        // Costruttore vuoto per la classe EmptySupport
    }

    /**
     * Crea un componente visivo standard per segnalare contenuto vuoto o un errore.
     * Include un'icona, un titolo e un messaggio personalizzato, con effetti visivi al passaggio del mouse.
     *
     * @param errorMessage Il messaggio di testo da visualizzare, che spiega l'assenza di contenuto.
     * @return Un {@link Div} stilizzato che rappresenta l'avviso di contenuto vuoto.
     */
    public static Div createErrorContent(String errorMessage) {
        Div emptyContainer = new Div();
        emptyContainer.addClassName("empty-state-container");
        emptyContainer.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "var(--lumo-space-l)")
                .set("width", "90%")
                .set("box-sizing", "border-box")
                .set("text-align", "center")
                .set("animation", "fadeIn 0.5s ease-in-out") // Applica animazione di fade-in
                .set("transition", "transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out") // Transizione per effetti hover
                .set("margin-right", "auto")
                .set("margin-left", "auto");

        // Inietta lo stile CSS per l'animazione di fade-in nell'head del documento
        emptyContainer.getElement().executeJs(
                "document.head.insertAdjacentHTML('beforeend', " +
                        "'<style>@keyframes fadeIn {from {opacity: 0;} to {opacity: 1;}}</style>');"
        );

        // Aggiunge listener JavaScript per effetti di scala e ombra al passaggio del mouse
        emptyContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() {" +
                        "  this.style.transform = 'scale(1.01)';" +
                        "  this.style.boxShadow = 'var(--lumo-box-shadow-xs)';" +
                        "});" +
                        "this.addEventListener('mouseout', function() {" +
                        "  this.style.transform = 'scale(1)';" +
                        "  this.style.boxShadow = 'none';" +
                        "});"
        );

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Contenitore circolare per l'icona
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("background-color", "var(--lumo-contrast-10pct)")
                .set("border-radius", "50%")
                .set("width", "64px")
                .set("height", "64px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "var(--lumo-space-m)");

        Icon infoIcon = new Icon(VaadinIcon.FILE_SEARCH); // Icona rappresentativa
        infoIcon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("width", "32px")
                .set("height", "32px");

        iconContainer.add(infoIcon);

        H4 title = new H4("Contenuto non disponibile");
        title.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontWeight.MEDIUM,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.Margin.Bottom.SMALL
        );

        Paragraph message = new Paragraph(errorMessage);
        message.addClassNames(
                LumoUtility.TextColor.SECONDARY
        );

        content.add(iconContainer, title, message);
        emptyContainer.add(content);

        return emptyContainer;
    }
}