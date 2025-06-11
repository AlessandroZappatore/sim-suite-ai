package it.uniupo.simnova.views.creation.paziente;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.components.PresidiService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.ValidationError;
import it.uniupo.simnova.views.ui.helper.AccessoComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vista per la gestione dei ***parametri del paziente al tempo T0** (iniziale) nello scenario di simulazione.
 * Permette di definire i parametri vitali principali e di gestire gli accessi venosi e arteriosi,
 * oltre a un campo per il monitoraggio e la selezione di presidi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Parametri Paziente T0")
@Route(value = "pazienteT0", layout = MainLayout.class)
public class PazienteT0View extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione delle operazioni e degli errori nella vista PazienteT0View.
     */
    private static final Logger logger = LoggerFactory.getLogger(PazienteT0View.class);

    /**
     * Lista statica per gestire gli accessi venosi e arteriosi.
     */
    private static final List<AccessoComponent> venosiAccessi = new ArrayList<>();
    /**
     * Lista statica per gestire gli accessi arteriosi.
     */
    private static final List<AccessoComponent> arteriosiAccessi = new ArrayList<>();
    /**
     * Contenitori per gli accessi venosi e arteriosi, gestiti dinamicamente.
     * Inizializzati a null per essere creati al momento della costruzione della vista.
     */
    private static VerticalLayout venosiContainer = null;
    /**
     * Contenitore per gli accessi arteriosi, gestito dinamicamente.
     * Inizializzato a null per essere creato al momento della costruzione della vista.
     */
    private static VerticalLayout arteriosiContainer = null;

    /**
     * Servizi utilizzati per la gestione degli scenari, dei presidi e dei parametri del paziente T0.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione dei presidi (dispositivi medici e attrezzature).
     */
    private final PresidiService presidiService;
    /**
     * Servizio per la gestione dei parametri del paziente al tempo T0.
     * Utilizzato per salvare e recuperare i dati del paziente all'inizio dello scenario.
     */
    private final PazienteT0Service pazienteT0Service;

    /**
     * Campi per i parametri vitali principali del paziente al tempo T0.
     */
    private final TextField paField;
    /**
     * Campo per la Frequenza Cardiaca (FC) del paziente.
     */
    private final NumberField fcField;
    /**
     * Campo per la Frequenza Respiratoria (RR) del paziente.
     */
    private final NumberField rrField;
    /**
     * Campo per la Temperatura Corporea del paziente.
     */
    private final NumberField tempField;
    /**
     * Campo per la Saturazione di Ossigeno (SpO₂) del paziente.
     */
    private final NumberField spo2Field;
    /**
     * Campo per la Frazione Inspiratoria di Ossigeno (FiO₂) del paziente.
     * Non obbligatorio, può essere nullo.
     */
    private final NumberField fio2Field;
    /**
     * Campo per il Flusso di Ossigeno in Litri/min (L/min) del paziente.
     * Non obbligatorio, può essere nullo.
     */
    private final NumberField litrio2Field;
    /**
     * Campo per la Pressione parziale di Anidride Carbonica di fine espirazione (EtCO₂) del paziente.
     * Non obbligatorio, può essere nullo.
     */
    private final NumberField etco2Field;
    /**
     * Area di testo per il monitoraggio del paziente, dove è possibile inserire dettagli come ECG o altri parametri.
     */
    private final TextArea monitorArea;
    /**
     * Campo per la selezione multipla dei presidi (dispositivi medici e attrezzature) associati al paziente.
     * Utilizza un {@link MultiSelectComboBox} per permettere la selezione di più opzioni contemporaneamente.
     */
    private final MultiSelectComboBox<String> presidiField;

    /**
     * Pulsante per avanzare alla schermata successiva dopo aver inserito i dati del paziente T0.
     * Utilizza un {@link Button} con stili e icone personalizzate.
     */
    private final Button nextButton;
    /**
     * Checkbox per gestire la visibilità e l'aggiunta di accessi venosi.
     * Permette di mostrare o nascondere la sezione degli accessi venosi nella vista.
     */
    Checkbox venosiCheckbox = FieldGenerator.createCheckbox("Accessi venosi");
    /**
     * Checkbox per gestire la visibilità e l'aggiunta di accessi arteriosi.
     * Permette di mostrare o nascondere la sezione degli accessi arteriosi nella vista.
     */
    Checkbox arteriosiCheckbox = FieldGenerator.createCheckbox("Accessi arteriosi");
    /**
     * ID dello scenario corrente, utilizzato per identificare lo scenario in cui si sta lavorando.
     * Viene impostato tramite il parametro dell'URL e utilizzato per salvare i dati del paziente T0.
     */
    private Integer scenarioId;
    /**
     * Modalità della vista, che può essere "create" per la creazione di nuovi dati o "edit" per modificare dati esistenti.
     * Viene determinata in base al parametro dell'URL e utilizzata per configurare l'interfaccia utente e le operazioni di salvataggio.
     */
    private String mode;

    /**
     * Costruttore della vista {@code PazienteT0View}.
     * Inizializza i servizi e configura la struttura base dell'interfaccia utente,
     * inclusi i campi per i parametri vitali e i controlli per gli accessi e i presidi.
     *
     * @param scenarioService    Servizio per la gestione degli scenari.
     * @param fileStorageService Servizio per la gestione dei file, utilizzato per l'AppHeader.
     * @param presidiService     Servizio per la gestione dei presidi.
     * @param pazienteT0Service  Servizio per la gestione dei parametri del paziente T0.
     */
    public PazienteT0View(ScenarioService scenarioService, FileStorageService fileStorageService, PresidiService presidiService, PazienteT0Service pazienteT0Service) {
        this.scenarioService = scenarioService;
        this.presidiService = presidiService;
        this.pazienteT0Service = pazienteT0Service;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna ai liquidi e dosi farmaci");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "PARAMETRI VITALI PRINCIPALI IN T0",
                "Definisci i parametri vitali principali del paziente al tempo T0 (iniziale).",
                VaadinIcon.HEART.create(), // Icona a forma di cuore
                "var(--lumo-primary-color)"
        );

        // Inizializzazione dei campi per i parametri vitali con stili predefiniti
        paField = FieldGenerator.createTextField("PA (mmHg)", "(es. 120/80)", true);
        fcField = FieldGenerator.createNumberField("FC (battiti/min)", "(es. 80)", true);
        rrField = FieldGenerator.createNumberField("RR (att/min)", "(es. 16)", true);
        tempField = FieldGenerator.createNumberField("Temp. (°C)", "(es. 36.5)", true);
        spo2Field = FieldGenerator.createNumberField("SpO₂ (%)", "(es. 98)", true);
        fio2Field = FieldGenerator.createNumberField("FiO₂ (%)", "(es. 21)", false);
        litrio2Field = FieldGenerator.createNumberField("L/min O₂", "(es. 5)", false);
        etco2Field = FieldGenerator.createNumberField("EtCO₂ (mmHg)", "(es. 35)", false);

        // Contenitori per gli accessi venosi e arteriosi (gestiti dinamicamente)
        venosiContainer = new VerticalLayout();
        venosiContainer.setWidthFull();
        venosiContainer.setSpacing(true);
        venosiContainer.setPadding(false);
        venosiContainer.setVisible(false); // Nascosto di default

        arteriosiContainer = new VerticalLayout();
        arteriosiContainer.setWidthFull();
        arteriosiContainer.setSpacing(true);
        arteriosiContainer.setPadding(false);
        arteriosiContainer.setVisible(false); // Nascosto di default

        // Pulsanti per aggiungere nuovi accessi
        Button addVenosiButton = StyleApp.getButton("Aggiungi accesso venoso", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addVenosiButton.setVisible(false); // Nascosto di default
        addVenosiButton.addClickListener(e -> addAccessoVenoso());

        Button addArteriosiButton = StyleApp.getButton("Aggiungi accesso arterioso", VaadinIcon.PLUS, ButtonVariant.LUMO_PRIMARY, "var(--lumo-base-color)");
        addArteriosiButton.setVisible(false); // Nascosto di default
        addArteriosiButton.addClickListener(e -> addAccessoArterioso());

        // Checkbox per mostrare/nascondere le sezioni degli accessi
        venosiCheckbox.addValueChangeListener(e -> {
            venosiContainer.setVisible(e.getValue());
            addVenosiButton.setVisible(e.getValue());
            if (!e.getValue()) { // Se deselezionato, pulisce gli accessi
                venosiAccessi.clear();
                venosiContainer.removeAll();
            }
        });

        arteriosiCheckbox.addValueChangeListener(e -> {
            arteriosiContainer.setVisible(e.getValue());
            addArteriosiButton.setVisible(e.getValue());
            if (!e.getValue()) { // Se deselezionato, pulisce gli accessi
                arteriosiAccessi.clear();
                arteriosiContainer.removeAll();
            }
        });

        monitorArea = FieldGenerator.createTextArea("Monitoraggio", "Specificare dettagli ECG o altri parametri...", false);

        List<String> allPresidiList = PresidiService.getAllPresidi();
        presidiField = FieldGenerator.createMultiSelectComboBox("Presidi", allPresidiList, false);

        // Aggiunta di tutti i componenti al layout di contenuto
        contentLayout.add(
                headerSection,
                paField, fcField, rrField, tempField, spo2Field, fio2Field, litrio2Field, etco2Field,
                venosiCheckbox, venosiContainer, addVenosiButton,
                arteriosiCheckbox, arteriosiContainer, addArteriosiButton,
                monitorArea, presidiField
        );

        nextButton = StyleApp.getNextButton(); // Pulsante per la navigazione successiva
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunta dei layout all'UI principale
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione indietro (alla vista liquidi)
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("liquidi/" + scenarioId)));

        // Listener per il pulsante "Avanti" (validazione e salvataggio)
        nextButton.addClickListener(e -> {
            if (!validateInput()) { // Esegue la validazione dei campi
                return;
            }
            saveDataAndNavigate(nextButton.getUI()); // Salva i dati e naviga
        });
    }

    /**
     * Gestisce il parametro dell'URL, che può includere l'ID dello scenario e la modalità (edit/create).
     *
     * @param event     L'evento di navigazione.
     * @param parameter La stringa del parametro URL (es. "123/edit").
     * @throws NotFoundException Se l'ID dello scenario non è valido o mancante.
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("ID Scenario è richiesto");
            }

            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];

            this.scenarioId = Integer.parseInt(scenarioIdStr.trim());
            // Verifica che lo scenario esista nel servizio
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("ID Scenario non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("ID Scenario non valido");
            }

            // Determina la modalità: "edit" se il secondo segmento è "edit", altrimenti "create"
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            // Ottiene il layout principale (Composite) per accedere ai suoi figli
            VerticalLayout mainLayout = getContent();

            // Rende visibili o invisibili componenti specifici a seconda della modalità
            // L'header (first HorizontalLayout) viene nascosto in modalità "edit"
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(headerLayout -> headerLayout.setVisible(!"edit".equals(mode)));

            // Il CreditsComponent nel footer (secondo HorizontalLayout, o ultimo) viene nascosto in modalità "edit"
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second) // Prende l'ultimo HorizontalLayout (il footer)
                    .ifPresent(footerLayout -> footerLayout.getChildren()
                            .filter(component -> component instanceof CreditsComponent)
                            .forEach(credits -> credits.setVisible(!"edit".equals(mode))));

            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT: caricamento dati Tempi esistenti per scenario {}", this.scenarioId);
                nextButton.setText("Salva");
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                nextButton.setIconAfterText(false); // Posiziona l'icona prima del testo
            } else {
                logger.info("Modalità CREATE: caricamento dati iniziali T0 e preparazione per nuovi tempi per scenario {}", this.scenarioId);
            }
            loadData();
        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dell'ID Scenario: '{}'", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
        }
    }

    /**
     * Aggiunge un nuovo componente {@link AccessoComponent} per un accesso venoso
     * al layout e alla lista degli accessi venosi.
     */
    private void addAccessoVenoso() {
        AccessoComponent accesso = new AccessoComponent("Venoso", true); // Passa true per abilitare il pulsante elimina
        venosiAccessi.add(accesso);
        venosiContainer.add(accesso);
    }

    /**
     * Aggiunge un nuovo componente {@link AccessoComponent} per un accesso arterioso
     * al layout e alla lista degli accessi arteriosi.
     */
    private void addAccessoArterioso() {
        AccessoComponent accesso = new AccessoComponent("Arterioso", true); // Passa true per abilitare il pulsante elimina
        arteriosiAccessi.add(accesso);
        arteriosiContainer.add(accesso);
    }

    /**
     * Valida i campi di input principali per assicurarsi che siano compilati correttamente.
     * Utilizza {@link ValidationError} per mostrare messaggi di errore e impostare il focus.
     *
     * @return {@code true} se tutti i campi obbligatori sono validi, {@code false} altrimenti.
     */
    private boolean validateInput() {
        boolean isValid = true;
        // La validazione showErrorAndReturnFalse aggiorna isValid e mostra l'errore.
        if (paField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(paField, "La Pressione Arteriosa (PA) è obbligatoria.");
        }
        if (fcField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(fcField, "La Frequenza Cardiaca (FC) è obbligatoria.");
        }
        if (rrField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(rrField, "La Frequenza Respiratoria (RR) è obbligatoria.");
        }
        if (tempField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(tempField, "La Temperatura è obbligatoria.");
        }
        if (spo2Field.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(spo2Field, "La Saturazione di Ossigeno (SpO₂) è obbligatoria.");
        }
        // Validazioni specifiche sui range dei valori possono essere aggiunte qui se necessarie.

        return isValid;
    }

    /**
     * Salva i dati del paziente T0 e naviga alla vista successiva.
     * L'operazione mostra una progress bar e notifiche di stato/errore.
     *
     * @param uiOptional L'oggetto {@link UI} opzionale per l'accesso e la navigazione.
     */
    private void saveDataAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar); // Mostra una progress bar durante il salvataggio

            try {
                // Raccoglie i dati dagli AccessoComponent
                List<Accesso> venosi = new ArrayList<>();
                for (AccessoComponent comp : venosiAccessi) {
                    venosi.add(comp.getAccesso());
                }

                List<Accesso> arteriosi = new ArrayList<>();
                for (AccessoComponent comp : arteriosiAccessi) {
                    arteriosi.add(comp.getAccesso());
                }

                // Salva i dati del paziente T0
                boolean success = pazienteT0Service.savePazienteT0(
                        scenarioId,
                        paField.getValue(),
                        fcField.getValue().intValue(),
                        rrField.getValue().intValue(),
                        tempField.getValue(),
                        spo2Field.getValue().intValue(),
                        fio2Field.getValue() != null ? fio2Field.getValue().intValue() : 0, // 0 se nullo
                        litrio2Field.getValue() != null ? litrio2Field.getValue().intValue() : 0, // 0 se nullo
                        etco2Field.getValue() != null ? etco2Field.getValue().intValue() : 0, // 0 se nullo
                        monitorArea.getValue(),
                        venosi,
                        arteriosi
                );

                // Salva i presidi selezionati
                boolean successPresidi = presidiService.savePresidi(scenarioId, presidiField.getValue());

                // Aggiorna l'UI dopo il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar); // Rimuove la progress bar
                    if (success && successPresidi) {
                        if (!mode.equals("edit")) {
                            Notification.show("Dati T0 salvati con successo",
                                    3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            ui.navigate("esameFisico/" + scenarioId); // Naviga alla vista Esame Fisico
                        } else {
                            Notification.show("Dati T0 aggiornati con successo",
                                    3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                            ui.navigate("scenari/" + scenarioId); // In modalità edit, torna alla vista dettaglio scenario
                        }
                    } else {
                        Notification.show("Errore durante il salvataggio dei dati. Riprova.",
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                });
            } catch (Exception e) {
                // Gestisce eventuali eccezioni durante il salvataggio
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Si è verificato un errore inaspettato: " + e.getMessage(),
                            5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio dei dati del paziente T0 per lo scenario con ID: {}", scenarioId, e);
                });
            }
        });
    }

    private void loadData() {
        // Carica i dati del paziente T0 se in modalità "edit"
        PazienteT0 p = pazienteT0Service.getPazienteT0ById(scenarioId);
        if (p != null) {
            paField.setValue(p.getPA());
            fcField.setValue((double) p.getFC());
            rrField.setValue((double) p.getRR());
            tempField.setValue(Math.round(p.getT() * 10.0) / 10.0);
            spo2Field.setValue((double) p.getSpO2());
            fio2Field.setValue((double) p.getFiO2());
            litrio2Field.setValue(p.getLitriO2());
            etco2Field.setValue((double) p.getEtCO2());
            monitorArea.setValue(p.getMonitor());

            // Carica gli accessi venosi e arteriosi
            venosiAccessi.clear();
            venosiContainer.removeAll();
            if (!p.getAccessiVenosi().isEmpty()) {
                venosiCheckbox.setValue(true);
                for (Accesso a : p.getAccessiVenosi()) {
                    AccessoComponent comp = new AccessoComponent(a, "Venoso", true);
                    venosiAccessi.add(comp);
                    venosiContainer.add(comp);
                }
                venosiContainer.setVisible(true);
            } else {
                venosiCheckbox.setValue(false);
                venosiContainer.setVisible(false);
            }

            arteriosiAccessi.clear();
            arteriosiContainer.removeAll();
            if (!p.getAccessiArteriosi().isEmpty()) {
                arteriosiCheckbox.setValue(true);
                for (Accesso a : p.getAccessiArteriosi()) {
                    AccessoComponent comp = new AccessoComponent(a, "Arterioso", true);
                    arteriosiAccessi.add(comp);
                    arteriosiContainer.add(comp);
                }
                arteriosiContainer.setVisible(true);
            } else {
                arteriosiCheckbox.setValue(false);
                arteriosiContainer.setVisible(false);
            }

            // Carica i presidi selezionati
            presidiField.setValue(PresidiService.getPresidiByScenarioId(scenarioId));
        } else {
            logger.warn("Nessun dato T0 trovato per lo scenario con ID: {}", scenarioId);
        }
    }
}

