package it.uniupo.simnova.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Vista di errore personalizzata per la gestione delle pagine non trovate (errore 404).
 * Mostra un messaggio di errore, un logo animato e un pulsante per tornare alla homepage.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Pagina non trovata")
public class ErrorView extends Composite<VerticalLayout>
        implements HasErrorParameter<NotFoundException> {

    /**
     * Paragrafo per visualizzare il messaggio di errore specifico.
     */
    private final Paragraph message;

    /**
     * Costruttore della vista di errore.
     * Inizializza il layout della pagina con header, logo animato, titolo, messaggio e pulsante per tornare alla home.
     *
     * @param fileStorageService Servizio per la gestione dei file, utilizzato per l'AppHeader.
     */
    public ErrorView(FileStorageService fileStorageService) {

        AppHeader header = new AppHeader(fileStorageService);
        // Posiziona l'header in modo fisso in alto
        header.getElement().getStyle()
                .set("position", "fixed")
                .set("top", "0")
                .set("width", "100%")
                .set("z-index", "1000");

        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.getStyle()
                .set("min-height", "100vh")
                .set("padding-top", "80px") // Spazio per l'header fisso
                .set("box-sizing", "border-box")
                .set("margin", "0")
                .set("padding", "0");

        // Immagine del logo animato
        Image logo = new Image("icons/404logo.png", "404 logo");
        logo.setWidth("280px");
        logo.setHeight("280px");
        logo.getStyle()
                .set("margin-bottom", "10px")
                .set("filter", "drop-shadow(0 4px 8px rgba(0,0,0,0.1))")
                .set("animation", "float 3s ease-in-out infinite"); // Applica l'animazione "float"

        // Inietta lo stile CSS per l'animazione direttamente nell'head del documento
        logo.getElement().executeJs("document.head.insertAdjacentHTML('beforeend', " +
                "'<style>@keyframes float {0%, 100% {transform: translateY(0);} 50% {transform: translateY(-10px);}}</style>')");

        H1 title = new H1("Ops! Pagina non trovata");
        title.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.FontWeight.BOLD
        );
        title.getStyle()
                .set("color", "#2c3e50")
                .set("text-shadow", "1px 1px 2px rgba(0,0,0,0.1)")
                .set("margin", "5px 0 10px 0");

        message = new Paragraph("Sembra che la pagina che stai cercando non esista o sia stata spostata.");
        message.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.FontSize.LARGE
        );
        message.getStyle()
                .set("color", "#7f8c8d")
                .set("max-width", "500px")
                .set("margin", "5px auto 20px");

        Button homeButton = new Button("Torna alla Home");
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.getStyle()
                .set("padding", "10px 20px")
                .set("font-weight", "600")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("transition", "all 0.3s ease")
                .set("margin-top", "10px");

        // Listener per navigare alla home page al click del pulsante
        homeButton.addClickListener(e -> homeButton.getUI().ifPresent(ui -> ui.navigate("")));

        // Configurazione del layout del contenuto
        contentLayout.setSizeFull();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(false);
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        contentLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        contentLayout.add(logo, title, message, homeButton);

        // Aggiunge l'header e il layout del contenuto al layout radice della vista
        getContent().removeAll(); // Pulisce eventuali componenti preesistenti
        getContent().setSizeFull();
        getContent().setPadding(false);
        getContent().setSpacing(false);
        getContent().add(header, contentLayout);
    }

    /**
     * Imposta il parametro di errore per la rotta non trovata (404).
     * Se un messaggio personalizzato è fornito, lo visualizza.
     *
     * @param event     L'evento di navigazione.
     * @param parameter Il parametro di errore contenente l'eccezione e un eventuale messaggio personalizzato.
     * @return Il codice di stato HTTP 404 (Not Found).
     */
    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        String customMessage = parameter.getCustomMessage();
        if (customMessage != null && !customMessage.isEmpty()) {
            message.setText(customMessage);
        }
        return HttpServletResponse.SC_NOT_FOUND;
    }
}