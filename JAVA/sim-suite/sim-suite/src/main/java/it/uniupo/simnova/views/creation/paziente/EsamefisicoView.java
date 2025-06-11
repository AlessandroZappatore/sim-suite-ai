package it.uniupo.simnova.views.creation.paziente;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.TinyEditor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.tinymce.TinyMce;

/**
 * Vista per la gestione dell'esame fisico del paziente all'interno di uno scenario di simulazione.
 * Questa vista permette l'inserimento e la modifica dettagliata dei risultati dell'esame fisico,
 * suddivisi per sezioni anatomiche, utilizzando un editor WYSIWYG per ogni sezione.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Esame Fisico")
@Route(value = "esameFisico", layout = MainLayout.class)
public class EsamefisicoView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori nella vista.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsamefisicoView.class);

    /**
     * Servizi utilizzati per la gestione degli scenari, degli esami fisici e del caricamento dei file.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione degli esami fisici.
     */
    private final EsameFisicoService esameFisicoService;
    /**
     * Servizio per la gestione dei file, utilizzato per l'header dell'applicazione.
     */
    private final FileStorageService fileStorageService;

    /**
     * Mappa che associa il nome di ogni sezione dell'esame fisico a un editor TinyMCE.
     * Le sezioni sono: Generale, Pupille, Collo, Torace, Cuore, Addome, Retto, Cute, Estremità, Neurologico e FAST.
     */
    private final Map<String, TinyMce> examSections = new HashMap<>();
    /**
     * ID dello scenario corrente, utilizzato per caricare e salvare i dati dell'esame fisico.
     */
    private Integer scenarioId;

    /**
     * Costruttore della vista {@code EsamefisicoView}.
     * Inizializza i servizi e imposta la struttura base dell'interfaccia utente.
     *
     * @param scenarioService    Il servizio per la gestione degli scenari.
     * @param fileStorageService Il servizio per la gestione dei file.
     * @param esameFisicoService Il servizio per la gestione degli esami fisici.
     */
    public EsamefisicoView(ScenarioService scenarioService, FileStorageService fileStorageService, EsameFisicoService esameFisicoService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.esameFisicoService = esameFisicoService;
        setupView();
    }

    /**
     * Restituisce l'icona {@link Icon} appropriata per una specifica sezione dell'esame fisico.
     *
     * @param sectionTitle Il titolo della sezione (es. "Generale", "Pupille").
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
            default -> new Icon(VaadinIcon.INFO); // Icona di default
        };
    }

    /**
     * Configura la struttura principale della vista, inclusi header, layout dei contenuti
     * e pulsanti di navigazione.
     */
    private void setupView() {
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna al paziente in T0");

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "ESAME FISICO",
                "Definisci i risultati dell'esame fisico del paziente, suddivisi per sezioni anatomiche.",
                VaadinIcon.STETHOSCOPE.create(),
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();
        contentLayout.add(headerSection);

        setupExamSections(contentLayout); // Imposta le sezioni dell'esame fisico

        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Pulsanti di scroll rapido per pagine lunghe
        Button scrollToTopButton = StyleApp.getScrollButton();
        Button scrollDownButton = StyleApp.getScrollDownButton();
        VerticalLayout scrollLayout = new VerticalLayout(scrollToTopButton, scrollDownButton);
        mainLayout.add(scrollLayout);

        // Aggiunge i componenti principali al layout radice
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("pazienteT0/" + scenarioId)));
        nextButton.addClickListener(e -> saveExamAndNavigate(nextButton.getUI()));
    }

    /**
     * Configura e aggiunge al layout le sezioni dell'esame fisico, ciascuna con un editor TinyMCE.
     * Ogni sezione ha un titolo, una descrizione e un'icona.
     *
     * @param contentLayout Il layout verticale a cui aggiungere le sezioni dell'esame.
     */
    private void setupExamSections(VerticalLayout contentLayout) {
        VerticalLayout examSectionsLayout = new VerticalLayout();
        examSectionsLayout.setWidthFull();
        examSectionsLayout.setSpacing(true);
        examSectionsLayout.setPadding(false);

        // Definizioni delle sezioni dell'esame fisico: Nome, Descrizione, Colore tema
        String[][] sections = {
                {"Generale", "Stato generale, livello di coscienza, postura, facies, cute e mucose, etc.", "#4285F4"}, // Blu
                {"Pupille", "Dimensione, forma, simmetria, reattività alla luce, movimenti oculari.", "#4285F4"},
                {"Collo", "Ispezione, palpazione, mobilità, vasi, linfonodi.", "#0F9D58"}, // Verde
                {"Torace", "Ispezione, palpazione, percussione, auscultazione polmonare (murmure vescicolare, rumori aggiunti).", "#0F9D58"},
                {"Cuore", "Ispezione, palpazione (itto della punta, fremiti), auscultazione (toni, soffi), frequenza e ritmo.", "#0F9D58"},
                {"Addome", "Ispezione, auscultazione, percussione, palpazione superficiale e profonda, dolorabilità, organomegalie.", "#DB4437"}, // Rosso
                {"Retto", "Ispezione perianale, esplorazione digitale rettale (se indicato).", "#DB4437"},
                {"Cute", "Colorito, turgore, elasticità, lesioni, alterazioni ungueali e pilifere.", "#F4B400"}, // Giallo
                {"Estremità", "Ispezione, palpazione (edemi, polsi periferici), motilità attiva e passiva, forza muscolare, riflessi.", "#F4B400"},
                {"Neurologico", "Stato mentale, nervi cranici, sistema motorio, sistema sensitivo, riflessi, coordinazione, equilibrio.", "#673AB7"}, // Viola
                {"FAST", "Focused Assessment with Sonography for Trauma (EFAST).", "#673AB7"}
        };

        for (String[] section : sections) {
            String sectionName = section[0];
            String sectionDesc = section[1];
            String sectionColor = section[2];

            VerticalLayout sectionLayout = new VerticalLayout();
            sectionLayout.setWidthFull();
            sectionLayout.setPadding(true);
            sectionLayout.setSpacing(false);
            sectionLayout.getStyle()
                    .set("background", "var(--lumo-base-color)")
                    .set("border-radius", "12px")
                    .set("margin-bottom", "1.5rem")
                    .set("border-left", "4px solid " + sectionColor) // Bordo colorato a sinistra
                    .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)"); // Ombra leggera

            HorizontalLayout headerLayout = new HorizontalLayout();
            headerLayout.setWidthFull();
            headerLayout.setPadding(false);
            headerLayout.setSpacing(true);
            headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            Icon sectionIcon = getSectionIcon(sectionName); // Icona per la sezione
            Div iconCircle = new Div();
            iconCircle.add(sectionIcon);
            iconCircle.getStyle()
                    .set("background-color", sectionColor)
                    .set("color", "white")
                    .set("border-radius", "50%")
                    .set("width", "36px")
                    .set("height", "36px")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("margin-right", "12px");

            sectionIcon.getStyle()
                    .set("color", "white")
                    .set("width", "20px")
                    .set("height", "20px");

            VerticalLayout titleDescLayout = new VerticalLayout();
            titleDescLayout.setPadding(false);
            titleDescLayout.setSpacing(false);

            H3 sectionTitle = new H3(sectionName);
            sectionTitle.getStyle()
                    .set("margin", "0")
                    .set("font-size", "18px")
                    .set("color", sectionColor) // Colore del titolo.
                    .set("font-weight", "600");

            Paragraph sectionDescription = new Paragraph(sectionDesc);
            sectionDescription.getStyle()
                    .set("margin", "0")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "14px")
                    .set("font-weight", "400");

            titleDescLayout.add(sectionTitle, sectionDescription);
            headerLayout.add(iconCircle, titleDescLayout);

            TinyMce editor = TinyEditor.getEditor(); // Editor WYSIWYG per il contenuto della sezione.
            editor.getStyle()
                    .set("margin-top", "12px")
                    .set("border", "1px solid " + sectionColor + "30")
                    .set("border-radius", "8px");

            examSections.put(sectionName, editor); // Mappa il nome della sezione al suo editor.

            sectionLayout.add(headerLayout, editor);
            examSectionsLayout.add(sectionLayout);
        }

        contentLayout.add(examSectionsLayout);
    }

    /**
     * Gestisce il parametro dell'URL (ID dello scenario).
     * Se l'ID non è valido o lo scenario non esiste, reindirizza a una pagina di errore 404.
     *
     * @param event     L'evento di navigazione.
     * @param parameter L'ID dello scenario come stringa.
     * @throws NotFoundException Se l'ID dello scenario non è valido o lo scenario non esiste.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            // Verifica che lo scenario esista nel servizio
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException(); // Tratta come ID non valido se non esiste
            }

            loadExistingExamData(); // Carica i dati dell'esame fisico se già esistenti
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            // Reindirizza a una pagina di errore 404
            event.rerouteToError(NotFoundException.class, "ID scenario " + parameter + " non valido.");
        }
    }

    /**
     * Carica i dati dell'esame fisico esistente per lo scenario corrente.
     * Popola gli editor TinyMCE con i valori recuperati dal database.
     */
    private void loadExistingExamData() {
        EsameFisico esameFisico = esameFisicoService.getEsameFisicoById(scenarioId);

        if (esameFisico != null) {
            Map<String, String> savedSections = esameFisico.getSections();
            // Popola ogni editor con il contenuto salvato per la rispettiva sezione
            savedSections.forEach((sectionName, value) -> {
                TinyMce editor = examSections.get(sectionName);
                if (editor != null) {
                    editor.setValue(value != null ? value : "");
                }
            });
        } else {
            // Se non esiste un esame fisico, inizializza tutti gli editor con stringhe vuote
            examSections.values().forEach(editor -> editor.setValue(""));
        }
    }

    /**
     * Salva i dati dell'esame fisico inseriti negli editor e naviga alla vista successiva
     * in base al tipo di scenario. Mostra notifiche di stato e di errore.
     *
     * @param uiOptional L'istanza opzionale dell'UI corrente per la navigazione.
     */
    private void saveExamAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar); // Mostra una progress bar durante il salvataggio

            try {
                Map<String, String> examData = new HashMap<>();
                // Raccoglie i valori da tutti gli editor
                examSections.forEach((section, editor) -> examData.put(section, editor.getValue()));

                boolean success = esameFisicoService.addEsameFisico(
                        scenarioId, examData // Salva l'esame fisico nel servizio
                );

                // Aggiorna l'UI dopo il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar); // Rimuove la progress bar
                    if (!success) {
                        Notification.show("Errore durante il salvataggio dell'esame fisico",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.error("Errore durante il salvataggio dell'esame fisico per lo scenario con ID: {}", scenarioId);
                        return;
                    }

                    // Naviga alla vista successiva in base al tipo di scenario
                    String scenarioType = scenarioService.getScenarioType(scenarioId);
                    if ("Quick Scenario".equals(scenarioType)) {
                        ui.navigate("scenari/" + scenarioId); // Naviga alla vista dettaglio scenario
                    } else if ("Advanced Scenario".equals(scenarioType) ||
                            "Patient Simulated Scenario".equals(scenarioType)) {
                        ui.navigate("tempi/" + scenarioId + "/create"); // Naviga alla creazione dei tempi
                    }
                });
            } catch (Exception e) {
                // Gestisce errori durante il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio dell'esame fisico per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }
}