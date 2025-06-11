package it.uniupo.simnova.views.common.utils;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;

/**
 * Classe di utilità per la gestione degli stili e dei componenti UI comuni nell'applicazione.
 * Fornisce metodi statici per creare pulsanti, header, footer e layout con stili predefiniti.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class StyleApp extends HorizontalLayout {

    /**
     * Costruttore privato per evitare istanziazioni dirette della classe.
     */
    private StyleApp() {
        // Costruttore privato per evitare istanziazioni dirette.
        // Questa classe è pensata per essere utilizzata solo tramite metodi statici.
    }

    /**
     * Crea e restituisce un pulsante "Indietro" con un'icona a freccia sinistra e stili specifici.
     *
     * @return Un'istanza di {@link Button} configurata per la navigazione indietro.
     */
    public static Button getBackButton() {
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Stile terziario di Lumo.
        backButton.getStyle()
                .set("margin-right", "auto") // Spinge il pulsante a sinistra.
                .set("transition", "all 0.2s ease") // Animazione fluida al passaggio del mouse.
                .set("font-weight", "500");
        backButton.addClassName("hover-effect"); // Classe CSS per effetti di hover.

        return backButton;
    }

    /**
     * Crea un layout per l'header di una sezione con titolo, sottotitolo e un'icona.
     *
     * @param title         Titolo principale da visualizzare (convertito in maiuscolo).
     * @param subtitle      Sottotitolo descrittivo.
     * @param iconComponent L'{@link Icon} da visualizzare accanto al titolo.
     * @param iconColor     Colore dell'icona (può essere una variabile CSS Lumo o un codice esadecimale).
     * @return Un {@link VerticalLayout} completo che rappresenta la sezione dell'header.
     */
    public static VerticalLayout getTitleSubtitle(String title, String subtitle, Icon iconComponent, String iconColor) {
        H2 headerTitle = new H2(title.toUpperCase());
        headerTitle.addClassName(LumoUtility.Margin.Bottom.NONE);
        headerTitle.addClassName(LumoUtility.Margin.Top.NONE);

        VerticalLayout headerSection = new VerticalLayout();
        headerSection.setPadding(true);
        headerSection.setSpacing(false);
        headerSection.setWidthFull();
        headerSection.setAlignItems(FlexComponent.Alignment.CENTER); // Centra il contenuto.
        headerSection.getStyle()
                .set("background", "var(--lumo-base-color)") // Sfondo bianco.
                .set("border-radius", "8px")
                .set("margin-top", "1rem")
                .set("margin-bottom", "1rem")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0,0,0,0.1)"); // Ombra leggera.

        HorizontalLayout titleWithIconLayout = new HorizontalLayout();
        titleWithIconLayout.setSpacing(true);
        titleWithIconLayout.setPadding(false);
        titleWithIconLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleWithIconLayout.getStyle().set("margin-bottom", "0.5rem");

        iconComponent.setSize("3em"); // Dimensione dell'icona.
        iconComponent.getStyle()
                .set("margin-right", "0.25em")
                .set("color", iconColor) // Colore dell'icona.
                .set("background", iconColor + "1A") // Sfondo semi-trasparente per l'icona.
                .set("padding", "10px")
                .set("border-radius", "50%"); // Rende l'icona circolare.

        headerTitle.getStyle()
                .set("color", iconColor) // Colore del titolo uguale all'icona.
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px")
                .set("text-align", "center");

        titleWithIconLayout.add(iconComponent, headerTitle);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        headerLayout.add(titleWithIconLayout);

        HorizontalLayout subtitleContainer = new HorizontalLayout();
        subtitleContainer.setWidthFull();
        subtitleContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        subtitleContainer.setPadding(false);
        subtitleContainer.setSpacing(false);

        Paragraph subtitleParagraph = new Paragraph(subtitle);
        subtitleParagraph.addClassName(LumoUtility.Margin.Top.XSMALL);
        subtitleParagraph.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        subtitleParagraph.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("max-width", "750px")
                .set("text-align", "center")
                .set("font-weight", "400")
                .set("line-height", "1.6");

        subtitleContainer.add(subtitleParagraph);
        headerSection.add(headerLayout, subtitleContainer);
        return headerSection;
    }

    /**
     * Crea un layout orizzontale che funge da header personalizzato,
     * combinando un pulsante "Indietro" con l'{@link AppHeader} dell'applicazione.
     *
     * @param backButton Il pulsante "Indietro" da includere.
     * @param header     L'{@link AppHeader} dell'applicazione.
     * @return Un {@link HorizontalLayout} che compone l'header personalizzato.
     */
    public static HorizontalLayout getCustomHeader(Button backButton, AppHeader header) {
        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);
        customHeader.getStyle().set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)"); // Ombra leggera.
        return customHeader;
    }

    /**
     * Crea un layout orizzontale per il footer dell'applicazione.
     * Include un pulsante "Avanti" (opzionale) e il componente {@link CreditsComponent}.
     * Vengono aggiunti anche stili CSS globali per effetti di hover/active sui pulsanti.
     *
     * @param nextButton Il pulsante "Avanti" da includere nel footer (può essere null).
     * @return Un {@link HorizontalLayout} che compone il footer.
     */
    public static HorizontalLayout getFooterLayout(Button nextButton) {
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP); // Bordo superiore.
        footerLayout.getStyle()
                .set("border-color", "var(--lumo-contrast-10pct)")
                .set("background", "var(--lumo-contrast-5pct)") // Sfondo leggermente contrastato.
                .set("box-shadow", "0 -2px 10px rgba(0, 0, 0, 0.03)"); // Ombra leggera dal basso.

        // Inietta stili CSS globali per effetti di hover e active sui bottoni
        UI.getCurrent().getPage().executeJs(
                "if (!document.getElementById('custom-hover-active-styles')) {" +
                        "  const styleElement = document.createElement('style');" +
                        "  styleElement.id = 'custom-hover-active-styles';" +
                        "  styleElement.innerHTML = '" +
                        ".hover-effect:hover { transform: translateY(-2px); }" + // Sposta in alto al hover.
                        "button:active { transform: scale(0.98); }" + // Scala leggermente al click.
                        "  ';" +
                        "  document.head.appendChild(styleElement);" +
                        "}"
        );

        CreditsComponent creditsLayout = new CreditsComponent(); // Componente crediti.

        if (nextButton != null) {
            nextButton.addClassName("hover-effect"); // Aggiunge l'effetto hover.
            footerLayout.add(creditsLayout, nextButton);
        } else {
            footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Centra i crediti se non c'è il pulsante.
            footerLayout.add(creditsLayout);
        }

        return footerLayout;
    }

    /**
     * Crea e restituisce un pulsante "Avanti" stilizzato con un'icona a freccia destra.
     *
     * @return Un'istanza di {@link Button} configurata per la navigazione avanti.
     */
    public static Button getNextButton() {
        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Stile primario di Lumo.
        nextButton.setWidth("150px");
        nextButton.getStyle()
                .set("border-radius", "30px") // Pulsante arrotondato.
                .set("font-weight", "600")
                .set("transition", "transform 0.2s ease"); // Animazione fluida al passaggio del mouse.
        return nextButton;
    }

    /**
     * Crea un pulsante generico con etichetta, icona (opzionale), variante di stile e colore.
     * Applica effetti di hover personalizzati.
     *
     * @param label     Etichetta testuale del pulsante.
     * @param icon      Icona da visualizzare nel pulsante (può essere null).
     * @param variant   Variante di stile del pulsante (es. {@link ButtonVariant#LUMO_PRIMARY}).
     * @param iconColor Colore di riferimento per lo sfondo e il testo dell'icona (variabile CSS Lumo).
     * @return Un'istanza di {@link Button} configurata con gli stili specificati.
     */
    public static Button getButton(String label, VaadinIcon icon, ButtonVariant variant, String iconColor) {
        Button newButton;
        if (icon != null) {
            newButton = new Button(label, new Icon(icon));
        } else {
            newButton = new Button(label);
        }

        newButton.addThemeVariants(variant);
        newButton.setMaxWidth("280px");
        newButton.getStyle()
                .set("border-radius", "30px")
                .set("font-weight", "600")
                .set("transition", "all 0.2s ease")
                .set("background-color", "var(" + iconColor + "-10pct)") // Sfondo leggermente colorato.
                .set("color", "var(" + iconColor + ")") // Colore del testo/icona.
                .set("border", "1px solid var(" + iconColor + "-50pct)") // Bordo colorato.
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.05)"); // Ombra leggera.

        // Aggiunge effetti di hover tramite JavaScript per cambio colore e ombra.
        newButton.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.backgroundColor = 'var(" + iconColor + "-20pct)'; " +
                        "  this.style.boxShadow = '0 4px 8px rgba(0, 0, 0, 0.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.backgroundColor = 'var(" + iconColor + "-10pct)'; " +
                        "  this.style.boxShadow = '0 2px 4px rgba(0, 0, 0, 0.05)'; " +
                        "});"
        );

        newButton.addClassName("hover-effect");
        return newButton;
    }

    /**
     * Crea un layout principale verticale per l'applicazione, impostando stili per occupare
     * l'intera altezza della viewport e uno sfondo leggermente contrastato.
     *
     * @param content Il layout di contenuto da incorporare.
     * @return Un {@link VerticalLayout} configurato come layout principale.
     */
    public static VerticalLayout getMainLayout(VerticalLayout content) {
        content.setSizeUndefined(); // Lascia che le dimensioni siano determinate dal contenuto e dal CSS.
        content.setPadding(false);
        content.setSpacing(false);
        content.getStyle()
                .set("min-height", "100vh") // Altezza minima per occupare l'intera viewport.
                .set("background", "var(--lumo-contrast-5pct)"); // Sfondo leggermente grigio.
        return content;
    }

    /**
     * Crea un layout di contenuto verticale con stili predefiniti.
     * Questo layout è tipicamente centrato, ha una larghezza massima e si espande verticalmente.
     *
     * @return Un'istanza di {@link VerticalLayout} configurata come layout di contenuto.
     */
    public static VerticalLayout getContentLayout() {
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px"); // Larghezza massima per evitare layout troppo larghi.
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Centra orizzontalmente i componenti.
        contentLayout.getStyle()
                .set("margin", "0 auto") // Centra il layout orizzontalmente.
                .set("flex-grow", "1"); // Permette al layout di espandersi e riempire lo spazio verticale.
        return contentLayout;
    }

    /**
     * Crea un pulsante fluttuante per tornare all'inizio della pagina.
     * Il pulsante è posizionato fissamente sulla destra dello schermo.
     *
     * @return Un'istanza di {@link Button} configurata come pulsante di scroll verso l'alto.
     */
    public static Button getScrollButton() {
        Button scrollToTopButton = new Button(FontAwesome.Solid.ARROW_TURN_UP.create());
        scrollToTopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        scrollToTopButton.setTooltipText("Torna all'inizio della pagina");
        scrollToTopButton.getStyle()
                .set("position", "fixed") // Posizionamento fisso rispetto alla viewport.
                .set("top", "calc(50% - 50px)") // A metà altezza, leggermente sopra.
                .set("right", "20px") // A 20px dal bordo destro.
                .set("z-index", "1000") // Sempre sopra gli altri elementi.
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        scrollToTopButton.addClickListener(e ->
                // Esegue JavaScript per scorrere la pagina verso l'alto.
                UI.getCurrent().getPage().executeJs("window.scrollTo({top: 0, behavior: 'smooth'});")
        );

        return scrollToTopButton;
    }

    /**
     * Crea un pulsante fluttuante per scorrere verso il basso, fino alla fine della pagina.
     * Il pulsante è posizionato fissamente sulla destra dello schermo, sotto il pulsante di scroll verso l'alto.
     *
     * @return Un'istanza di {@link Button} configurata come pulsante di scroll verso il basso.
     */
    public static Button getScrollDownButton() {
        Button scrollToBottomButton = new Button(FontAwesome.Solid.ARROW_TURN_DOWN.create());
        scrollToBottomButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
        scrollToBottomButton.setTooltipText("Vai alla fine della pagina");
        scrollToBottomButton.getStyle()
                .set("position", "fixed")
                .set("top", "calc(50% + 10px)") // A metà altezza, leggermente sotto.
                .set("right", "20px")
                .set("z-index", "1000")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");
        scrollToBottomButton.addClickListener(e ->
                // Esegue JavaScript per scorrere la pagina verso il basso.
                UI.getCurrent().getPage().executeJs(
                        "window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});"
                )
        );

        return scrollToBottomButton;
    }

    /**
     * Crea e apre un dialog di conferma generico con un titolo, un messaggio,
     * e due pulsanti (conferma e annulla).
     *
     * @param title         Il titolo del dialog.
     * @param message       Il messaggio principale visualizzato nel dialog.
     * @param confirmText   Il testo del pulsante di conferma.
     * @param cancelText    Il testo del pulsante di annullamento.
     * @param confirmAction L'{@link Runnable} da eseguire quando l'utente conferma.
     */
    public static void createConfirmDialog(String title, String message,
                                           String confirmText, String cancelText,
                                           Runnable confirmAction) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setWidth("450px"); // Larghezza definita per un aspetto consistente
        // Applica bordi arrotondati direttamente all'elemento del dialogo per un look più morbido
        dialog.getElement().getStyle().set("border-radius", "var(--lumo-border-radius-l)");

        // --- Sezione Intestazione ---
        Icon titleIcon = VaadinIcon.QUESTION_CIRCLE_O.create(); // Icona per contestualizzare il titolo
        titleIcon.setSize("var(--lumo-icon-size-l)"); // Dimensione icona standard Lumo
        titleIcon.getStyle()
                .set("color", "var(--lumo-primary-color)") // Colore primario per l'icona
                .set("margin-right", "var(--lumo-space-s)"); // Spazio tra icona e testo del titolo

        H3 titleComponent = new H3(title);
        titleComponent.getStyle()
                .set("margin", "0") // Rimuove margini predefiniti di H3 per controllo più fine
                .set("font-size", "var(--lumo-font-size-xl)") // Titolo più grande e prominente
                .set("font-weight", "600") // Grassetto standard per titoli
                .set("color", "var(--lumo-header-text-color)"); // Colore standard per testo di intestazione

        HorizontalLayout headerLayout = new HorizontalLayout(titleIcon, titleComponent);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Allinea verticalmente icona e testo

        // --- Corpo del Messaggio ---
        Paragraph messageComponent = new Paragraph(message);
        messageComponent.getStyle()
                .set("color", "var(--lumo-secondary-text-color)") // Colore per testo secondario, meno enfasi
                .set("font-size", "var(--lumo-font-size-m)") // Dimensione font standard per testo
                .set("line-height", "var(--lumo-line-height-m)"); // Altezza linea per leggibilità
        // Il margine inferiore sarà gestito dalla spaziatura del VerticalLayout

        // --- Pulsanti di Azione ---
        Button confirmButton = new Button(confirmText);
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Stile primario per l'azione principale
        confirmButton.setIcon(VaadinIcon.CHECK_CIRCLE_O.create()); // Icona di conferma più evidente
        confirmButton.addClickListener(e -> {
            dialog.close();
            if (confirmAction != null) {
                confirmAction.run();
            }
        });

        Button cancelButton = new Button(cancelText);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Stile terziario per minor enfasi
        cancelButton.setIcon(VaadinIcon.CLOSE_CIRCLE_O.create()); // Icona di annullamento corrispondente
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, confirmButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END); // Allinea i pulsanti a destra
        buttonLayout.setWidthFull(); // Occupa tutta la larghezza per permettere la giustificazione
        buttonLayout.setSpacing(true); // Aggiunge spazio tra i pulsanti (cancelButton e confirmButton)

        // --- Layout Principale del Dialogo ---
        VerticalLayout mainLayout = new VerticalLayout(headerLayout, messageComponent, buttonLayout);

        mainLayout.getStyle().set("padding", "var(--lumo-space-l)");
        mainLayout.setSpacing(true); // Gestisce la spaziatura verticale tra header, messaggio e pulsanti
        mainLayout.setAlignItems(FlexComponent.Alignment.STRETCH); // Assicura che i figli si estendano se necessario

        dialog.add(mainLayout);
        dialog.open();
    }
}