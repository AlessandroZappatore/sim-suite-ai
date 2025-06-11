package it.uniupo.simnova.views.creation;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.ValidationError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vista per l'inizio della creazione di uno scenario.
 * Permette l'inserimento dei dettagli iniziali come titolo, paziente, patologia e durata.
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
@PageTitle("StartCreation")
@Route(value = "startCreation", layout = MainLayout.class)
public class StartCreationView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori nella vista di creazione dello scenario.
     */
    private static final Logger logger = LoggerFactory.getLogger(StartCreationView.class);

    /**
     * Servizi utilizzati per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione degli scenari avanzati.
     */
    private final AdvancedScenarioService advancedScenarioService;
    /**
     * Servizio per la gestione degli scenari con paziente simulato.
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;

    /**
     * Campo di input per il titolo dello scenario.
     */
    private final TextField scenarioTitle;
    /**
     * Campo di input per il nome del paziente.
     */
    private final TextField patientName;
    /**
     * Campo di input per la patologia o malattia del paziente.
     */
    private final TextField pathology;
    /**
     * Campo di input per il nome dell'autore dello scenario.
     */
    private final ComboBox<Integer> durationField;
    /**
     * Campo di input per il tipo di scenario (es. Adulto, Pediatrico, Neonatale, Prematuro).
     */
    private final TextField authorField;
    /**
     * Campo di selezione per il tipo di scenario.
     */
    private final Select<String> typeField;

    /**
     * Tipo di scenario da creare, passato come parametro nell'URL.
     * Può essere "quickscenario", "advancedscenario" o "patientsimulatedscenario".
     */
    private String scenarioType;

    /**
     * Costruttore che inizializza la vista e configura i campi di input.
     *
     * @param scenarioService                 Servizio per la gestione degli scenari.
     * @param fileStorageService              Servizio per la gestione dei file.
     * @param advancedScenarioService         Servizio specifico per scenari avanzati.
     * @param patientSimulatedScenarioService Servizio specifico per scenari con paziente simulato.
     */
    public StartCreationView(ScenarioService scenarioService, FileStorageService fileStorageService,
                             AdvancedScenarioService advancedScenarioService, PatientSimulatedScenarioService patientSimulatedScenarioService) {
        this.scenarioService = scenarioService;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna alla pagina di creazione");

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "INIZIO CREAZIONE SCENARIO",
                "Compila i campi richiesti per iniziare la creazione del tuo scenario.",
                VaadinIcon.START_COG.create(),
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Inizializzazione dei campi di input con icone e layout personalizzati
        scenarioTitle = FieldGenerator.createTextField("TITOLO SCENARIO", "Inserisci il titolo dello scenario", true);
        HorizontalLayout scenarioTitleLayout = createFieldWithIconLayout(FontAwesome.Solid.TAGS.create(), scenarioTitle);
        scenarioTitleLayout.setWidthFull();
        scenarioTitleLayout.getStyle().set("max-width", "500px").set("margin", "0 auto");

        patientName = FieldGenerator.createTextField("NOME PAZIENTE", "Inserisci il nome del paziente", true);
        HorizontalLayout patientNameLayout = createFieldWithIconLayout(FontAwesome.Solid.USER_INJURED.create(), patientName);
        patientNameLayout.setWidthFull();
        patientNameLayout.getStyle().set("max-width", "500px").set("margin", "0 auto");

        pathology = FieldGenerator.createTextField("PATOLOGIA/MALATTIA", "Inserisci la patologia", true);
        HorizontalLayout pathologyLayout = createFieldWithIconLayout(FontAwesome.Solid.DISEASE.create(), pathology);
        pathologyLayout.setWidthFull();
        pathologyLayout.getStyle().set("max-width", "500px").set("margin", "0 auto");

        authorField = FieldGenerator.createTextField("AUTORE", "Inserisci il tuo nome", true);
        HorizontalLayout authorLayout = createFieldWithIconLayout(FontAwesome.Solid.SIGNATURE.create(), authorField);
        authorLayout.setWidthFull();
        authorLayout.getStyle().set("max-width", "500px").set("margin", "0 auto");

        List<Integer> durations = List.of(5, 10, 15, 20, 25, 30);
        durationField = FieldGenerator.createComboBox("DURATA SIMULAZIONE (minuti)", durations, 10, true);
        HorizontalLayout durationLayout = createFieldWithIconLayout(FontAwesome.Solid.STOPWATCH_20.create(), durationField);
        durationLayout.setWidthFull();
        durationLayout.getStyle().set("max-width", "500px").set("margin", "0 auto");

        List<String> scenarioTypes = List.of("Adulto", "Pediatrico", "Neonatale", "Prematuro");
        typeField = FieldGenerator.createSelect("TIPO SCENARIO", scenarioTypes, "Adulto", true);

        // Mappa per associare icone ai tipi di paziente
        Map<String, Icon> iconMap = new HashMap<>();
        iconMap.put("Adulto", FontAwesome.Solid.USER.create());
        iconMap.put("Pediatrico", FontAwesome.Solid.CHILD.create());
        iconMap.put("Neonatale", FontAwesome.Solid.BABY.create());
        iconMap.put("Prematuro", FontAwesome.Solid.HANDS_HOLDING_CHILD.create());

        final Icon[] scenarioTypeIcon = {iconMap.get(typeField.getValue())};
        scenarioTypeIcon[0].getStyle().set("margin-right", "10px");
        scenarioTypeIcon[0].setSize("24px");

        HorizontalLayout typeFieldWithIcon = new HorizontalLayout(scenarioTypeIcon[0], typeField);
        typeFieldWithIcon.setAlignItems(FlexComponent.Alignment.BASELINE);
        typeFieldWithIcon.setWidthFull();
        typeFieldWithIcon.getStyle().set("max-width", "500px").set("margin", "0 auto");
        typeFieldWithIcon.expand(typeField);

        // Aggiorna l'icona in base alla selezione del tipo di paziente
        typeField.addValueChangeListener(event -> {
            Icon newIcon = iconMap.getOrDefault(event.getValue(), FontAwesome.Solid.QUESTION.create());
            newIcon.getStyle().set("margin-right", "10px");
            newIcon.setSize("24px");
            int iconIndex = typeFieldWithIcon.indexOf(scenarioTypeIcon[0]);
            if (iconIndex != -1) {
                typeFieldWithIcon.replace(scenarioTypeIcon[0], newIcon);
            } else {
                typeFieldWithIcon.addComponentAsFirst(newIcon);
            }
            scenarioTypeIcon[0] = newIcon;
        });

        contentLayout.add(
                headerSection,
                scenarioTitleLayout,
                patientNameLayout,
                pathologyLayout,
                authorLayout,
                durationLayout,
                typeFieldWithIcon
        );
        contentLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        // Listener per la navigazione
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("")));
        nextButton.addClickListener(e -> {
            if (validateFields()) {
                saveScenarioAndNavigate(nextButton.getUI());
            }
        });
    }

    /**
     * Crea un layout orizzontale con un'icona e un componente campo.
     *
     * @param icon  L'icona da visualizzare.
     * @param field Il componente campo.
     * @return Un {@link HorizontalLayout} contenente l'icona e il campo.
     */
    private HorizontalLayout createFieldWithIconLayout(Icon icon, Component field) {
        icon.setSize("24px");
        icon.getStyle().set("margin-right", "8px");

        HorizontalLayout layout = new HorizontalLayout(icon, field);
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.setWidthFull();
        layout.expand(field);
        return layout;
    }

    /**
     * Gestisce il parametro dell'URL, impostando il tipo di scenario.
     *
     * @param event     L'evento di navigazione.
     * @param parameter Il tipo di scenario ricevuto dall'URL.
     */
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        this.scenarioType = (parameter != null) ? parameter.toLowerCase() : "quickscenario";
    }

    /**
     * Valida i campi del form.
     *
     * @return {@code true} se tutti i campi sono validi, altrimenti {@code false}.
     */
    private boolean validateFields() {
        boolean isValid = true;

        if (scenarioTitle.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(scenarioTitle, "Compila il campo TITOLO SCENARIO");
        }
        if (patientName.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(patientName, "Compila il campo NOME PAZIENTE");
        }
        if (pathology.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(pathology, "Compila il campo PATOLOGIA/MALATTIA");
        }
        if (authorField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(authorField, "Compila il campo AUTORE");
        }
        if (durationField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(durationField, "Compila il campo DURATA SIMULAZIONE");
        } else if (durationField.getValue() == null || durationField.getValue() <= 0) {
            isValid = ValidationError.showErrorAndReturnFalse(durationField, "Inserisci una durata valida (maggiore di 0)");
        }
        if (typeField.isEmpty()) {
            isValid = ValidationError.showErrorAndReturnFalse(typeField, "Compila il campo TIPO SCENARIO");
        }
        return isValid;
    }

    /**
     * Salva i dati iniziali dello scenario in base al tipo selezionato
     * e naviga alla vista successiva per la configurazione avanzata.
     *
     * @param uiOptional L'istanza opzionale dell'UI corrente per la navigazione.
     */
    private void saveScenarioAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            try {
                int scenarioId;
                switch (scenarioType) {
                    case "quickscenario":
                        scenarioId = scenarioService.startQuickScenario(
                                -1, // ID temporaneo, verrà assegnato dal servizio
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    case "advancedscenario":
                        scenarioId = advancedScenarioService.startAdvancedScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    case "patientsimulatedscenario":
                        scenarioId = patientSimulatedScenarioService.startPatientSimulatedScenario(
                                scenarioTitle.getValue(),
                                patientName.getValue(),
                                pathology.getValue(),
                                authorField.getValue(),
                                durationField.getValue().floatValue(),
                                typeField.getValue()
                        );
                        break;
                    default:
                        Notification.show("Tipo di scenario non riconosciuto: " + scenarioType,
                                3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                }

                logger.info("ID scenario creato: {}", scenarioId);

                if (scenarioId > 0) {
                    logger.info("Navigando a target/{}", scenarioId);
                    ui.navigate("target/" + scenarioId);
                } else {
                    Notification.show("Errore durante il salvataggio dello scenario (ID non valido)",
                            3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                Notification.show("Errore durante il salvataggio: " + e.getMessage(),
                        5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                logger.error("Errore durante il salvataggio dello scenario", e);
            }
        });
    }
}