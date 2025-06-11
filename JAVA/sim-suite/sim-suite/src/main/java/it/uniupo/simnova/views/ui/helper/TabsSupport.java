package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;

/**
 * Classe di utility per la creazione di Tab personalizzati con icone e testo.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class TabsSupport {

    /**
     * Costruttore privato per impedire l'istanza della classe.
     */
    private TabsSupport() {
        // Costruttore privato per impedire l'istanza della classe.
    }

    /**
     * Crea un componente {@link Tab} con un'icona e un testo, centrati all'interno.
     * Questo metodo semplifica la creazione di tab visivamente accattivanti e coerenti.
     *
     * @param text     Il testo da visualizzare nel tab.
     * @param iconType L'{@link VaadinIcon} da utilizzare nel tab.
     * @return Un {@link Tab} configurato con l'icona e il testo specificati.
     */
    public static Tab createTabWithIcon(String text, VaadinIcon iconType) {
        Span tabText = new Span(text);
        tabText.getStyle().set("margin-left", "var(--lumo-space-s)"); // Margine a sinistra per separare testo da icona

        HorizontalLayout tabContent = new HorizontalLayout();
        tabContent.setSizeFull(); // Occupa l'intera larghezza e altezza disponibile nel tab.
        tabContent.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Centra orizzontalmente il contenuto.
        tabContent.setAlignItems(FlexComponent.Alignment.CENTER); // Centra verticalmente il contenuto.
        tabContent.setSpacing(false); // Rimuove lo spazio tra i componenti interni.
        tabContent.add(new com.vaadin.flow.component.icon.Icon(iconType), tabText); // Aggiunge icona e testo.
        tabContent.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("text-align", "center");

        Tab tab = new Tab(tabContent);
        tab.getStyle().set("flex-grow", "1"); // Permette al tab di espandersi e occupare lo spazio.

        return tab;
    }
}