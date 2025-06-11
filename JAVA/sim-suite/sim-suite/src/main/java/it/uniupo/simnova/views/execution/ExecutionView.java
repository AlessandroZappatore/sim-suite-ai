package it.uniupo.simnova.views.execution;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;

/**
 * Vista di esecuzione dell'applicazione SIM SUITE.
 * Fornisce la struttura base della pagina, indicando che la funzionalità non è ancora implementata.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Execution")
@Route(value = "execution", layout = MainLayout.class)
public class ExecutionView extends Composite<VerticalLayout> {

    /**
     * Costruttore della vista di esecuzione.
     * Inizializza la pagina con header, pulsante di ritorno e un messaggio centrale.
     *
     * @param fileStorageService Servizio per la gestione dei file, utilizzato per l'AppHeader.
     */
    public ExecutionView(FileStorageService fileStorageService) {

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        Button backButton = StyleApp.getBackButton();
        // Listener per navigare alla home page al click del pulsante "Indietro"
        backButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate("")));

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Sezione dell'header specifico per questa vista con titolo e sottotitolo informativo
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "SIM EXECUTION", // Titolo della sezione
                "Funzionalità non implementata", // Sottotitolo informativo
                VaadinIcon.BUILDING.create(), // Icona rappresentativa
                "var(--lumo-primary-color)" // Colore dell'icona/titolo
        );

        contentLayout.add(headerSection);

        HorizontalLayout footerSection = StyleApp.getFooterLayout(null);

        // Aggiunge i componenti principali al layout radice della vista
        mainLayout.add(customHeader, contentLayout, footerSection);
    }
}

/*
 * Note per l'implementazione futura:
 *
 * 1. Per visualizzare parametri e informazioni temporali:
 *    - Utilizzare l'implementazione esistente in detailView (vedere @MonitorSupport in ui.helper)
 *
 * 2. Per l'editor di note:
 *    - Utilizzare il componente text editor già disponibile (@TinyEditor in views.utils)
 *
 * 3. Per il timer dei vari tempi:
 *    - Si può utilizzare un addon di Vaadin
 */