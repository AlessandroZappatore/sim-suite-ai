package it.uniupo.simnova.views.common.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static it.uniupo.simnova.views.constant.CreditConst.*; // Importa le costanti dei crediti

/**
 * Componente riutilizzabile per visualizzare i crediti dell'applicazione.
 * Include informazioni sull'ideatore, l'università e i contatti dello sviluppatore,
 * visualizzati in un popup dialog.
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
public class CreditsComponent extends VerticalLayout {

    /**
     * Costruttore che inizializza il componente dei crediti.
     * Imposta il layout e aggiunge le righe informative (ideatore, sviluppatore, università, versione).
     */
    public CreditsComponent() {
        this.setPadding(false);
        this.setSpacing(false);
        this.setMargin(false);
        this.setWidthFull();
        this.setAlignItems(FlexComponent.Alignment.START);

        Paragraph creditsTitle = new Paragraph("Crediti");
        creditsTitle.addClassNames(
                LumoUtility.FontWeight.BOLD,
                LumoUtility.FontSize.XSMALL,
                LumoUtility.TextColor.SECONDARY
        );
        creditsTitle.getStyle().set("margin", "0 0 4px 0");

        // Righe delle informazioni sui crediti
        HorizontalLayout ideatorRow = createIdeatorRow();
        HorizontalLayout developerRow = createDeveloperRow();
        HorizontalLayout universityRow = createUniversityRow();
        HorizontalLayout versionRow = createVersionRow();

        this.add(creditsTitle, ideatorRow, developerRow, universityRow, versionRow);
    }

    /**
     * Crea una riga orizzontale con un'icona e un link all'ideatore.
     * Ora include un bottone per aprire il dialogo dei contatti.
     *
     * @return Un {@link HorizontalLayout} contenente l'icona e il bottone.
     */
    private HorizontalLayout createIdeatorRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(Alignment.CENTER); // Ensures vertical alignment of items in the row

        Icon ideatorIcon = VaadinIcon.LIGHTBULB.create();
        ideatorIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        ideatorIcon.getStyle().set("margin-right", "4px");

        // Use a Span wrapped in a Button to control styling more precisely
        Span ideatorText = new Span("Ideatore: Antonio Scalogna");
        ideatorText.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.XSMALL
        );
        // The button itself will still have some default padding, so we try to minimize it.
        Button ideatorNameButton = new Button(ideatorText);
        ideatorNameButton.addClassNames(
                LumoUtility.Padding.Vertical.NONE, // Remove vertical padding
                LumoUtility.Padding.Horizontal.NONE, // Remove horizontal padding
                LumoUtility.Background.TRANSPARENT // Make the button transparent
        );
        ideatorNameButton.getStyle().set("min-width", "unset"); // Prevents minimum width
        ideatorNameButton.getStyle().set("cursor", "pointer");
        ideatorNameButton.addClickListener(e -> openIdeatorInfoDialog());

        row.add(ideatorIcon, ideatorNameButton);
        return row;
    }

    /**
     * Crea una riga orizzontale con le informazioni dello sviluppatore.
     * Include un'icona, il nome dello sviluppatore come pulsante che apre un dialogo con i contatti.
     *
     * @return Un {@link HorizontalLayout} contenente le informazioni dello sviluppatore.
     */
    private HorizontalLayout createDeveloperRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(Alignment.CENTER); // Ensures vertical alignment of items in the row

        Icon developerIcon = VaadinIcon.USER.create();
        developerIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        developerIcon.getStyle().set("margin-right", "4px");

        // Use a Span wrapped in a Button to control styling more precisely
        Span developerText = new Span("Sviluppatore: Alessandro Zappatore");
        developerText.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.XSMALL
        );
        Button developerNameButton = new Button(developerText);
        developerNameButton.addClassNames(
                LumoUtility.Padding.Vertical.NONE, // Remove vertical padding
                LumoUtility.Padding.Horizontal.NONE, // Remove horizontal padding
                LumoUtility.Background.TRANSPARENT // Make the button transparent
        );

        developerNameButton.getStyle().set("min-width", "unset"); // Prevents minimum width
        developerNameButton.getStyle().set("cursor", "pointer");
        developerNameButton.addClickListener(e -> openDeveloperInfoDialog());

        row.add(developerIcon, developerNameButton);
        return row;
    }

    /**
     * Crea una riga orizzontale con le informazioni sull'università.
     * Include un'icona e un link all'università.
     *
     * @return Un {@link HorizontalLayout} contenente le informazioni sull'università.
     */
    private HorizontalLayout createUniversityRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon universityIcon = new Icon(VaadinIcon.ACADEMY_CAP);
        universityIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        universityIcon.getStyle().set("margin-right", "4px");

        Anchor universityLink = new Anchor(UNIVERSITYLINK, "Università del Piemonte Orientale");
        universityLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        universityLink.getElement().setAttribute("target", "_blank");
        universityLink.getElement().setAttribute("rel", "noopener noreferrer");

        row.add(universityIcon, universityLink);
        return row;
    }

    /**
     * Crea una riga orizzontale con le informazioni sulla versione dell'applicazione e la data di rilascio.
     * Include icone per la versione e la data.
     *
     * @return Un {@link HorizontalLayout} contenente le informazioni sulla versione.
     */
    private HorizontalLayout createVersionRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setPadding(false);
        row.setMargin(false);
        row.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon versionIcon = new Icon(VaadinIcon.INFO_CIRCLE);
        versionIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        versionIcon.getStyle().set("margin-right", "4px");

        Anchor versionLink = new Anchor(RELEASELINK, "Versione: " + VERSION);
        versionLink.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        versionLink.getElement().setAttribute("target", "_blank");
        versionLink.getElement().setAttribute("rel", "noopener noreferrer");

        Icon dateIcon = new Icon(VaadinIcon.CALENDAR);
        dateIcon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        dateIcon.getStyle().set("margin-left", "8px");
        dateIcon.getStyle().set("margin-right", "4px");

        Span dateText = new Span("Data: " + DATE);
        dateText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        row.add(versionIcon, versionLink, dateIcon, dateText);
        return row;
    }

    /**
     * Apre un dialogo modale che visualizza le informazioni di contatto dello sviluppatore.
     */
    private void openDeveloperInfoDialog() {
        List<Supplier<HorizontalLayout>> contactRows = Arrays.asList(
                () -> createContactRow(VaadinIcon.ENVELOPE.create(), DEVELOPERMAIL, "alessandrozappatore03@gmail.com"),
                () -> createContactRow(FontAwesome.Brands.GITHUB.create(), GITHUBLINK, "Github: AlessandroZappatore"),
                () -> createContactRow(FontAwesome.Brands.LINKEDIN.create(), DEVELOPERLINK, "LinkedIn: Alessandro Zappatore")
        );
        openContactDialog("Contatti Sviluppatore", contactRows);
    }

    /**
     * Apre un dialogo modale che visualizza le informazioni di contatto dell'ideatore.
     */
    private void openIdeatorInfoDialog() {
        List<Supplier<HorizontalLayout>> contactRows = Arrays.asList(
                () -> createContactRow(VaadinIcon.ENVELOPE.create(), IDEATORMAIL, "antonio.scalogna@uniupo.it"),
                () -> createContactRow(FontAwesome.Brands.LINKEDIN.create(), IDEATORLINK, "LinkedIn: Antonio Scalogna")
        );
        openContactDialog("Contatti Ideatore", contactRows);
    }

    /**
     * Crea una riga di contatto con un'icona e un link.
     *
     * @param icon L'icona da visualizzare.
     * @param href L'URL del link.
     * @param text Il testo del link.
     * @return Un {@link HorizontalLayout} contenente l'icona e il link.
     */
    private HorizontalLayout createContactRow(Icon icon, String href, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(false);
        row.setAlignItems(Alignment.CENTER);
        icon.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        icon.getStyle().set("margin-right", "4px");
        Anchor link = new Anchor(href, text);
        link.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);
        link.getElement().setAttribute("target", "_blank");
        link.getElement().setAttribute("rel", "noopener noreferrer");
        row.add(icon, link);
        return row;
    }

    /**
     * Apre un dialogo modale generico con un titolo e un elenco di righe di contatto.
     *
     * @param title       Il titolo del dialogo.
     * @param contactRows Una lista di {@link Supplier} che restituiscono {@link HorizontalLayout} per le righe di contatto.
     */
    private void openContactDialog(String title, List<Supplier<HorizontalLayout>> contactRows) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);
        dialogContent.setAlignItems(Alignment.START);
        dialogContent.setWidthFull();

        for (Supplier<HorizontalLayout> rowSupplier : contactRows) {
            dialogContent.add(rowSupplier.get());
        }

        dialog.add(dialogContent);

        // Pulsante di chiusura del dialogo
        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE), e -> dialog.close());
        closeButton.addThemeVariants();
        dialog.getHeader().add(closeButton);

        dialog.open();
    }
}