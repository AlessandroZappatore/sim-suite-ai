package it.uniupo.simnova.views.common.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Componente per l'header dell'applicazione.
 * Contiene il logo dell'applicazione, il titolo, un'area per il logo del centro (con upload/gestione)
 * e un pulsante per cambiare il tema (modalità scura/chiara).
 *
 * @author Alessandro Zappatore
 * @version 1.5
 */
public class AppHeader extends HorizontalLayout {
    /**
     * Logger per la registrazione degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(AppHeader.class);
    /**
     * Nome del file del logo del centro.
     */
    private static final String CENTER_LOGO_FILENAME = "center_logo.png";
    /**
     * URL del logo dell'applicazione.
     */
    private static final String LOGO_URL = "icons/icon.png";

    /**
     * Servizio per la gestione dei file, utilizzato per caricare e gestire il logo del centro.
     */
    private final FileStorageService fileStorageService;

    /**
     * Contenitore per il logo del centro, che può essere un'immagine o un componente di upload.
     */
    private final Div centerLogoContainer;
    /**
     * Pulsante per il cambio tema dell'applicazione (modalità scura/chiara).
     * Mostra un'icona che cambia in base al tema attivo.
     */
    private final Button toggleThemeButton;

    /**
     * Stato attuale del tema dell'applicazione.
     * {@code true} se il tema è scuro, {@code false} se è chiaro.
     */
    private boolean isDarkMode = false;

    /**
     * Costruttore che inizializza l'header dell'applicazione.
     *
     * @param fileStorageService Servizio per la gestione dei file, utilizzato per caricare e gestire il logo del centro.
     */
    public AppHeader(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;

        // Stili di base dell'header
        addClassName(LumoUtility.Padding.SMALL);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        getStyle().set("background", "var(--lumo-primary-color-10pct)")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05), 0 1px 3px rgba(0,0,0,0.1)")
                .set("padding", "10px");

        // Logo SIM SUITE (a sinistra)
        Image simSuiteLogo = new Image(LOGO_URL, "SIM SUITE Logo");
        simSuiteLogo.setHeight("40px");
        simSuiteLogo.getStyle().set("cursor", "pointer");
        simSuiteLogo.addClickListener(e -> UI.getCurrent().navigate("")); // Naviga alla home al click
        Tooltip.forComponent(simSuiteLogo)
                .withText("Torna alla Home")
                .withPosition(Tooltip.TooltipPosition.BOTTOM);

        // Titolo dell'applicazione
        Div appTitle = new Div();
        appTitle.setText("SIM SUITE");
        appTitle.addClassNames(
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextColor.PRIMARY
        );

        // Contenitore per il logo del centro (gestito dinamicamente)
        centerLogoContainer = new Div();
        centerLogoContainer.getStyle()
                .set("margin-left", LumoUtility.Margin.MEDIUM)
                .set("display", "flex")
                .set("align-items", "center");
        updateCenterLogoArea(); // Aggiorna l'area del logo all'inizializzazione

        showMissingLogoPopoverIfNeeded(); // Mostra un popover se il logo del centro manca

        // Sezione sinistra dell'header (logo app, titolo, logo centro)
        HorizontalLayout leftSection = new HorizontalLayout(simSuiteLogo, appTitle, centerLogoContainer);
        leftSection.setSpacing(true);
        leftSection.setAlignItems(Alignment.CENTER);

        // Pulsante per il cambio tema (modalità scura/chiara)
        toggleThemeButton = new Button();
        toggleThemeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleThemeButton.getStyle()
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)")
                .set("border-radius", "50%");

        checkInitialTheme(); // Controlla il tema all'avvio e imposta l'icona
        toggleThemeButton.setTooltipText("Cambia Tema");
        toggleThemeButton.addClickListener(e -> toggleTheme()); // Listener per cambiare tema al click

        // Aggiunge le sezioni all'header principale
        add(leftSection, toggleThemeButton);
    }

    /**
     * Aggiorna l'area destinata al logo del centro.
     * Visualizza il logo se esiste nel file system; altrimenti, mostra un componente {@link Upload}
     * per permettere all'utente di caricare un nuovo logo.
     */
    private void updateCenterLogoArea() {
        centerLogoContainer.removeAll(); // Pulisce il contenitore prima di aggiornarlo

        if (fileStorageService.fileExists(CENTER_LOGO_FILENAME)) {
            // Se il logo esiste, visualizza l'immagine e un pulsante per eliminarla
            String logoUrl = "/" + CENTER_LOGO_FILENAME; // URL relativo per accedere al file statico
            Image centerLogo = new Image(logoUrl, "Logo Centro");
            centerLogo.setHeight("40px");

            Button deleteButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
            deleteButton.getStyle().set("margin-left", LumoUtility.Margin.XSMALL);
            Tooltip.forComponent(deleteButton).withText("Elimina Logo Centro");

            deleteButton.addClickListener(e -> showDeleteConfirmation()); // Mostra conferma prima di eliminare

            centerLogoContainer.add(centerLogo, deleteButton);

        } else {
            // Se il logo non esiste, mostra un componente di upload
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/webp");
            upload.setMaxFiles(1);

            Button uploadButton = new Button("Carica Logo Centro");
            upload.setUploadButton(uploadButton);
            upload.setDropLabel(new Div(new Text("o trascina qui"))); // Testo per il drag-and-drop

            // Stili per il componente upload
            upload.getStyle()
                    .set("min-width", "180px")
                    .set("height", "40px")
                    .set("display", "flex")
                    .set("align-items", "center");
            uploadButton.getStyle().set("height", "40px"); // Allinea il pulsante di upload

            upload.addSucceededListener(event -> {
                try (InputStream inputStream = buffer.getInputStream()) {
                    fileStorageService.store(inputStream, CENTER_LOGO_FILENAME); // Salva il file caricato
                    Notification.show("Logo caricato con successo!", 2000, Notification.Position.MIDDLE);
                    UI.getCurrent().access(this::updateCenterLogoArea); // Aggiorna l'UI dopo il caricamento
                } catch (Exception ex) {
                    logger.error("Errore durante il salvataggio del logo caricato.", ex);
                    Notification.show("Errore caricamento logo: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            upload.addFileRejectedListener(event -> Notification.show("File rifiutato: " + event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR));

            centerLogoContainer.add(upload);
        }
    }

    /**
     * Mostra una notifica di conferma all'utente prima di eliminare il logo del centro.
     * In caso di conferma, procede con l'eliminazione.
     */
    private void showDeleteConfirmation() {
        Button confirmButton = new Button("Elimina", e -> deleteCenterLogo());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Annulla");

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        Notification notification = new Notification();
        Paragraph question = new Paragraph("Sei sicuro di voler eliminare il logo del centro?");
        notification.add(question, buttons);
        notification.setDuration(0); // Notifica persistente
        notification.setPosition(Notification.Position.MIDDLE);

        cancelButton.addClickListener(e -> notification.close());
        confirmButton.addClickListener(e -> notification.close()); // Chiude la notifica dopo la conferma

        notification.open();
    }

    /**
     * Elimina il file del logo del centro dal file system.
     * Aggiorna l'area del logo e mostra una notifica di successo o errore.
     */
    private void deleteCenterLogo() {
        try {
            fileStorageService.deleteFile(CENTER_LOGO_FILENAME);
            Notification.show("Logo eliminato con successo.", 2000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().access(this::updateCenterLogoArea); // Aggiorna l'UI dopo l'eliminazione
        } catch (Exception ex) {
            logger.error("Errore durante l'eliminazione del logo del centro.", ex);
            Notification.show("Errore eliminazione logo: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    /**
     * Mostra un {@link Popover} informativo all'utente se il logo del centro non è presente.
     * Il popover appare solo sulla homepage e suggerisce di caricare il logo.
     */
    private void showMissingLogoPopoverIfNeeded() {
        UI.getCurrent().getPage().fetchCurrentURL(currentUrl -> {
            String path = currentUrl.getPath();
            // Il popover viene mostrato solo sulla homepage ("" o "/") e se il logo non esiste
            if ((path.isEmpty() || "/".equals(path)) && !fileStorageService.fileExists(CENTER_LOGO_FILENAME)) {
                UI.getCurrent().access(() -> {
                    Popover popover = new Popover();
                    popover.setOpened(true); // Apri il popover automaticamente
                    Paragraph message = new Paragraph("⚠️ Carica il logo del centro per averlo nei PDF.");
                    Button closeBtn = new Button("Chiudi", e -> popover.setOpened(false));
                    closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

                    HorizontalLayout layout = new HorizontalLayout(message, closeBtn);
                    layout.setAlignItems(Alignment.CENTER);

                    popover.add(layout);
                    popover.setTarget(centerLogoContainer); // Collega il popover al contenitore del logo
                    popover.open();
                });
            }
        });
    }

    /**
     * Controlla il tema iniziale del documento HTML e imposta lo stato {@code isDarkMode} di conseguenza.
     * Aggiorna l'icona del pulsante di cambio tema.
     */
    private void checkInitialTheme() {
        UI.getCurrent().getPage().executeJs(
                "return document.documentElement.getAttribute('theme') || 'light';" // Legge l'attributo 'theme' o default 'light'
        ).then(String.class, theme -> {
            isDarkMode = "dark".equals(theme);
            updateThemeIcon();
        });
    }

    /**
     * Alterna il tema dell'applicazione tra modalità scura e chiara.
     * Aggiorna l'attributo 'theme' e 'color-scheme' del documento HTML e l'icona del pulsante.
     */
    private void toggleTheme() {
        isDarkMode = !isDarkMode; // Inverte lo stato del tema

        String themeToSet = isDarkMode ? "dark" : "light";

        // Imposta l'attributo 'theme' sull'elemento <html>
        UI.getCurrent().getPage().executeJs(
                "document.documentElement.setAttribute('theme', $0)",
                themeToSet
        );

        // Imposta 'color-scheme' per il supporto nativo del browser (es. scrollbar)
        UI.getCurrent().getPage().executeJs(
                "document.documentElement.style.setProperty('color-scheme', $0)",
                themeToSet
        );

        updateThemeIcon(); // Aggiorna l'icona del pulsante
    }

    /**
     * Aggiorna l'icona visualizzata sul pulsante di cambio tema in base allo stato attuale di {@code isDarkMode}.
     * Mostra un sole se il tema è scuro, una luna se è chiaro.
     */
    private void updateThemeIcon() {
        Icon icon;
        if (isDarkMode) {
            icon = VaadinIcon.SUN_O.create();
            icon.setColor("var(--lumo-warning-color)"); // Colore giallo per il sole
        } else {
            icon = VaadinIcon.MOON_O.create();
            icon.setColor("var(--lumo-contrast)"); // Colore scuro per la luna
        }

        icon.getStyle()
                .set("width", "var(--lumo-icon-size-m)")
                .set("height", "var(--lumo-icon-size-m)");

        toggleThemeButton.setIcon(icon); // Imposta l'icona sul pulsante
    }
}