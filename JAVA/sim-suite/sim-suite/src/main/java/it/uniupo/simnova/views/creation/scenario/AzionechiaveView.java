package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista per la gestione e definizione delle azioni chiave in uno scenario di simulazione.
 * Questa vista permette agli utenti di inserire le azioni principali che saranno
 * utilizzate e valutate durante la fase di debriefing dello scenario.
 * <p>
 * Implementa {@link HasUrlParameter} per ricevere l'ID dello scenario tramite l'URL,
 * garantendo la continuità nel flusso di creazione o modifica dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
@PageTitle("Azioni Chiave")
@Route(value = "azionechiave", layout = MainLayout.class)
public class AzionechiaveView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(AzionechiaveView.class);

    /**
     * Il servizio per la gestione delle operazioni sugli scenari.
     */
    private final ScenarioService scenarioService;

    /**
     * Il servizio specifico per la gestione delle azioni chiave.
     */
    private final AzioneChiaveService azioneChiaveService;

    /**
     * Il layout verticale che contiene dinamicamente i campi di testo per le azioni chiave.
     */
    private final VerticalLayout actionFieldsContainer;

    /**
     * Una lista che tiene traccia di tutti i campi di testo delle azioni chiave attualmente visualizzati.
     */
    private final List<TextField> actionFields = new ArrayList<>();

    /**
     * Un contatore per numerare progressivamente i campi di testo delle azioni chiave (es. "Azione Chiave #1", "#2").
     * Inizializzato a 1 per il primo campo.
     */
    private int size = 1;

    /**
     * L'ID dello scenario corrente, passato come parametro URL.
     */
    private Integer scenarioId;

    /**
     * Costruisce una nuova istanza di <code>AzionechiaveView</code>.
     * Inizializza l'interfaccia utente e configura i listener per i bottoni di navigazione e aggiunta/rimozione azioni.
     *
     * @param scenarioService     Il servizio per la gestione degli scenari.
     * @param fileStorageService  Il servizio per la gestione dei file, utilizzato per l'intestazione dell'applicazione.
     * @param azioneChiaveService Il servizio per la gestione delle azioni chiave.
     */
    public AzionechiaveView(ScenarioService scenarioService, FileStorageService fileStorageService, AzioneChiaveService azioneChiaveService) {
        this.scenarioService = scenarioService;
        this.azioneChiaveService = azioneChiaveService;

        // Configura il layout principale della vista.
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // Configura l'header personalizzato con un bottone "Indietro" e l'header dell'app.
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna al patto d'aula");

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // Configura il layout per il contenuto centrale.
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Sezione dell'intestazione visuale per la vista, con titolo, sottotitolo e icona.
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "AZIONI CHIAVE",
                "Definisci le azioni principali che saranno valutate durante il debriefing.",
                VaadinIcon.KEY.create(),
                "var(--lumo-primary-color)"
        );

        // Contenitore per i campi di testo delle azioni chiave.
        actionFieldsContainer = new VerticalLayout();
        actionFieldsContainer.setWidthFull();
        actionFieldsContainer.setPadding(false);
        actionFieldsContainer.setSpacing(true);

        // Bottone per aggiungere nuovi campi di azione chiave.
        Button addButton = new Button("Aggiungi azione chiave", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.getStyle().set("margin-top", "var(--lumo-space-m)");
        addButton.addClickListener(e -> addNewActionField("")); // Aggiunge un campo vuoto.

        // Aggiunge le sezioni al layout del contenuto.
        contentLayout.add(headerSection, actionFieldsContainer, addButton);

        // Configura il footer con il bottone "Avanti".
        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunge tutte le sezioni al layout principale.
        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per il bottone "Indietro".
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("pattoaula/" + scenarioId)));

        // Listener per il bottone "Avanti".
        nextButton.addClickListener(e -> {
            // Estrae e filtra i valori dei campi di testo (rimuove duplicati e stringhe vuote/solo spazi).
            List<String> content = actionFields.stream()
                    .map(TextField::getValue)
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            // Se non ci sono azioni chiave definite, chiede conferma all'utente.
            boolean isEmpty = content.isEmpty();
            if (isEmpty) {
                StyleApp.createConfirmDialog(
                        "Azioni Chiave Vuote",
                        "Sei sicuro di voler continuare senza definire alcuna azione chiave? Queste sono importanti per il debriefing.",
                        "Prosegui",
                        "Annulla",
                        () -> saveAzioniChiaveAndNavigate(nextButton.getUI(), content) // Callback se l'utente conferma.
                );
            } else {
                // Se ci sono azioni chiave, procede direttamente al salvataggio e alla navigazione.
                saveAzioniChiaveAndNavigate(nextButton.getUI(), content);
            }
        });
    }

    /**
     * Aggiunge un nuovo campo di input {@link TextField} per un'azione chiave alla vista.
     * Ogni campo include un bottone per rimuoverlo.
     *
     * @param initialValue Il valore iniziale (<code>String</code>) da pre-popolare nel campo di testo.
     *                     Può essere una stringa vuota se si vuole un campo vuoto.
     */
    private void addNewActionField(String initialValue) {
        HorizontalLayout fieldLayout = new HorizontalLayout();
        fieldLayout.setWidthFull();
        fieldLayout.setSpacing(true);
        fieldLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Allinea verticalmente gli elementi al centro.

        // Crea il campo di testo con un label progressivo.
        TextField actionField = FieldGenerator.createTextField("Azione Chiave #" + size++,
                "Inserisci un'azione chiave (es. 'Valutare il paziente')",
                false);
        actionField.setValue(initialValue != null ? initialValue : ""); // Imposta il valore iniziale.

        actionFields.add(actionField); // Aggiunge il campo alla lista di tracciamento.

        // Bottone per rimuovere il campo di testo.
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        removeButton.addClickListener(e -> {
            actionFields.remove(actionField);       // Rimuove il campo dalla lista.
            actionFieldsContainer.remove(fieldLayout); // Rimuove il layout del campo dalla vista.
            size--; // Decrementa il contatore per i nuovi campi.
        });

        fieldLayout.addAndExpand(actionField); // Aggiunge il campo e lo espande.
        fieldLayout.add(removeButton);         // Aggiunge il bottone di rimozione.
        actionFieldsContainer.add(fieldLayout); // Aggiunge il layout completo alla vista.
    }

    /**
     * Implementazione del metodo {@link HasUrlParameter#setParameter(BeforeEvent, Object)}
     * per gestire l'ID dello scenario passato tramite l'URL.
     * Questo metodo è invocato automaticamente da Vaadin all'apertura della vista.
     *
     * @param event     L'evento di navigazione.
     * @param parameter L'ID dello scenario come {@link String}. Se <code>null</code> o non valido,
     *                  la navigazione verrà reindirizzata a una pagina di errore.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("ID scenario non fornito nell'URL per la vista Azioni Chiave.");
                event.rerouteToError(NotFoundException.class, "ID scenario non fornito. Impossibile caricare le azioni chiave.");
                return;
            }

            this.scenarioId = Integer.parseInt(parameter); // Converte l'ID da String a Integer.
            // Verifica se lo scenario con l'ID fornito esiste nel database.
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Tentativo di accesso a scenario non esistente o con ID non valido: {}. Re indirizzamento a pagina di errore.", scenarioId);
                event.rerouteToError(NotFoundException.class, "Scenario con ID " + scenarioId + " non trovato o non valido.");
                return;
            }
            logger.info("Caricamento azioni chiave per lo scenario ID: {}.", scenarioId);
            loadExistingAzioniChiave(); // Carica le azioni chiave associate allo scenario.

        } catch (NumberFormatException e) {
            logger.error("Formato ID scenario non valido ricevuto come parametro: '{}'. Deve essere un numero intero.", parameter, e);
            event.rerouteToError(NotFoundException.class, "Formato ID scenario non valido: " + parameter + ". Deve essere un numero intero.");
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'impostazione dei parametri o il caricamento delle azioni chiave per lo scenario ID: {}. Dettagli: {}", parameter, e.getMessage(), e);
            event.rerouteToError(NotFoundException.class, "Errore durante il caricamento della pagina delle azioni chiave. Riprova più tardi.");
        }
    }

    /**
     * Carica le azioni chiave esistenti per lo scenario corrente dal database
     * e le visualizza come campi di testo nella vista.
     * Se non ci sono azioni chiave esistenti, aggiunge un campo vuoto predefinito.
     */
    private void loadExistingAzioniChiave() {
        actionFields.clear();           // Pulisce la lista dei campi di testo gestiti.
        actionFieldsContainer.removeAll(); // Rimuove tutti i componenti dal layout contenitore.
        size = 1; // Resetta il contatore per i nuovi campi.

        // Recupera le azioni chiave dal servizio.
        List<String> nomiAzioni = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);

        if (nomiAzioni != null && !nomiAzioni.isEmpty()) {
            // Aggiunge un campo di testo per ogni azione chiave recuperata.
            for (String nomeAzione : nomiAzioni) {
                if (nomeAzione != null && !nomeAzione.trim().isEmpty()) {
                    addNewActionField(nomeAzione.trim());
                }
            }
            logger.info("Caricate {} azioni chiave esistenti per lo scenario ID {}.", nomiAzioni.size(), scenarioId);
        } else {
            logger.info("Nessuna azione chiave esistente trovata per lo scenario ID {}.", scenarioId);
        }

        // Se, dopo il caricamento, non ci sono campi (o la lista era vuota), aggiunge un campo vuoto di default.
        if (actionFields.isEmpty()) {
            addNewActionField("");
            logger.debug("Aggiunto un campo azione chiave vuoto di default per lo scenario ID {}.", scenarioId);
        }
    }

    /**
     * Salva le azioni chiave definite dall'utente nel database e naviga alla vista successiva
     * (Obiettivi Didattici). Visualizza una ProgressBar durante il salvataggio.
     *
     * @param uiOptional          L'{@link Optional} che incapsula l'istanza di {@link UI} corrente.
     * @param nomiAzioniDaSalvare La {@link List} di {@link String} contenente i nomi delle azioni chiave da salvare.
     */
    private void saveAzioniChiaveAndNavigate(Optional<UI> uiOptional, List<String> nomiAzioniDaSalvare) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                boolean success = azioneChiaveService.updateAzioniChiaveForScenario(scenarioId, nomiAzioniDaSalvare);

                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    if (success) {
                        ui.navigate("obiettivididattici/" + scenarioId);
                    } else {
                        Notification.show("Errore durante il salvataggio delle azioni chiave.", 5000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        logger.warn("Salvataggio azioni chiave fallito per scenario ID: {}", scenarioId);
                    }
                });
            } catch (Exception e) {
                getContent().remove(progressBar);
                logger.error("Errore durante il salvataggio delle azioni chiave per scenario ID: {}", scenarioId, e);
                ui.accessSynchronously(() -> Notification.show("Errore critico: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));
            }
        });
    }
}