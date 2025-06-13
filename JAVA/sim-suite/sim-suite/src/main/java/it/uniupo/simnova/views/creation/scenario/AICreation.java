package it.uniupo.simnova.views.creation.scenario;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
// Aggiunto import per ButtonVariant
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField; // Aggiunto import per TextField
import com.vaadin.flow.data.value.ValueChangeMode; // Aggiunto import
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.service.NotifierService;
import it.uniupo.simnova.service.ai_api.ExternalApiService;
import it.uniupo.simnova.service.ai_api.model.ScenarioGenerationRequest;
import it.uniupo.simnova.service.scenario.operations.ScenarioImportService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@PageTitle("AI Creation")
@Route(value = "ai-creation", layout = MainLayout.class)
public class AICreation extends Composite<VerticalLayout> {

    private static final Logger logger = LoggerFactory.getLogger(AICreation.class);

    private final ScenarioImportService scenarioImportService; // Aggiunto per importazione
    private final FileStorageService fileStorageService;
    private final ExternalApiService externalApiService; // Aggiunto per chiamate API
    private final NotifierService notifierService; // Aggiunto per notifiche
    private final ExecutorService executorService; // Aggiunto per esecuzione in background
    // Icone riutilizzabili
    private final Icon aiIcon = FontAwesome.Solid.ROBOT.create();
    private int step = 0; // 0: tipo scenario, 1: target scenario, 2: descrizione, 3: difficoltà, 4: esami lab, 5: fine
    // Componenti UI riutilizzabili per i messaggi AI
    private HorizontalLayout aiPresentationMsg;
    private HorizontalLayout aiMsgStep0;
    private HorizontalLayout aiMsgStep1;
    private HorizontalLayout aiMsgStepTarget; // Nuovo messaggio AI per il target
    private HorizontalLayout aiMsgStep2;

    // Componenti UI riutilizzabili per gli input utente
    private Select<String> scenarioTypeSelect;
    private TextField scenarioTargetField; // Nuovo campo per il target
    private TextArea shortDescription;
    private Select<String> difficultySelect;


    private VerticalLayout inputTypeLayout;
    private VerticalLayout inputTargetLayout; // Nuovo layout per l'input del target
    private VerticalLayout inputDescLayout;
    private VerticalLayout inputDiffLayout;

    // Pulsanti di invio riutilizzabili come campi di classe
    private Button sendType;
    private Button sendTarget; // Nuovo pulsante di invio per il target
    private Button sendDesc;
    private Button sendDiff;


    public AICreation(FileStorageService fileStorageService,
                      ScenarioImportService scenarioImportService,
                      ExternalApiService externalApiService,
                      NotifierService notifierService,
                      ExecutorService executorService) {
        this.fileStorageService = fileStorageService;
        this.scenarioImportService = scenarioImportService;
        this.externalApiService = externalApiService;
        this.notifierService = notifierService;
        this.executorService = executorService;
        aiIcon.setSize("20px");
        Icon userIcon = FontAwesome.Solid.USER.create();
        userIcon.setSize("20px");

        initView();
    }

    void initView() {
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna alla pagina di creazione");

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "CREA UNO SCENARIO CON L'AIUTO DELL'INTELLIGENZA ARTIFICIALE",
                "Compila i campi richiesti al resto ci penserà l'AI",
                aiIcon,
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout chatLayout = new VerticalLayout();
        chatLayout.setWidthFull();
        chatLayout.setPadding(false);
        chatLayout.setSpacing(false);
        chatLayout.getStyle().set("background", "var(--lumo-contrast-5pct)").set("border-radius", "12px").set("min-height", "300px");
        chatLayout.setId("ai-chat-layout");

        // Inizializza i componenti riutilizzabili
        initComponents();

        // Layout dinamico iniziale
        updateChatLayout(chatLayout);

        contentLayout.add(headerSection, chatLayout);
        contentLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, chatLayout);

        Button nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);
        nextButton.setVisible(false);
        nextButton.addClickListener(e -> nextButton.getUI().ifPresent(ui -> ui.navigate("scenari")));
        mainLayout.add(customHeader, contentLayout, footerLayout);

        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("")));

        // Gestione avanzamento step
        sendType.addClickListener(e -> {
            step = 1; // Passa allo step del target
            updateChatLayout(chatLayout);
        });
        sendTarget.addClickListener(e -> { // Nuovo listener per il pulsante sendTarget
            if (scenarioTargetField.getValue() != null && !scenarioTargetField.getValue().trim().isEmpty()) {
                step = 2; // Passa allo step della descrizione
                updateChatLayout(chatLayout);
            }
        });
        sendDesc.addClickListener(e -> {
            if (!shortDescription.isEmpty()) {
                step = 3; // Passa allo step della difficoltà
                updateChatLayout(chatLayout);
            }
        });
        sendDiff.addClickListener(e -> {
            step = 4; // Passa allo step degli esami di laboratorio
            updateChatLayout(chatLayout);
            startScenarioGeneration();
        });
    }

    private void startScenarioGeneration() {
        // Mostra un feedback immediato e non bloccante
        Notification.show("Generazione dello scenario avviata. Riceverai una notifica al termine.", 4000, Notification.Position.MIDDLE);

        // Cattura l'istanza UI corrente da passare al thread
        final UI ui = UI.getCurrent();

        // Raccogli i dati dal form
        ScenarioGenerationRequest request = new ScenarioGenerationRequest(
                shortDescription.getValue(),
                scenarioTypeSelect.getValue(),
                scenarioTargetField.getValue()
        );

        // Avvia l'operazione nel thread pool gestito
        executorService.submit(() -> {
            String notificationMessage;
            try {
                // Esegui la chiamata API e l'importazione
                Optional<String> jsonResponseOptional = externalApiService.generateScenario(request);

                if (jsonResponseOptional.isPresent()) {
                    scenarioImportService.createScenarioByJSON(jsonResponseOptional.get().getBytes(StandardCharsets.UTF_8));
                    notificationMessage = "Scenario creato con successo!";
                } else {
                    notificationMessage = "Errore: Il servizio AI non ha restituito una risposta valida.";
                }
            } catch (Exception e) {
                logger.error("Fallimento nel task di generazione scenario in background.", e);
                notificationMessage = "Errore critico durante la generazione dello scenario.";
            }

            // Al termine del task, invia la notifica all'utente specifico
            notifierService.notify(ui, notificationMessage);
        });
    }

    private void initComponents() {
        // Messaggio di presentazione AI
        aiPresentationMsg = createAiMessage("Ciao! Sono Leo, il tuo assistente AI, ti aiuterò a creare uno scenario.");

        // STEP 0: Tipologia scenario
        aiMsgStep0 = createAiMessage("Che tipo di scenario vuoi creare?");
        List<String> scenarioTypes = List.of("Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario");
        scenarioTypeSelect = FieldGenerator.createSelect("Tipologia Scenario", scenarioTypes, "Quick Scenario", true);
        sendType = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create()); // Assegna al campo di classe
        styleSendButton(sendType);
        inputTypeLayout = createInputLayout(scenarioTypeSelect, sendType);

        // STEP 1: Target Scenario
        aiMsgStepTarget = createAiMessage("Qual è il target primario di questo scenario? (es. Studenti di medicina, Infermieri specializzati, etc.)");
        scenarioTargetField = FieldGenerator.createTextField("Target Scenario", "Inserisci il target dello scenario", true); // Corretto: aggiunto true per required
        scenarioTargetField.setValueChangeMode(ValueChangeMode.EAGER);
        sendTarget = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create());
        styleSendButton(sendTarget);
        sendTarget.setEnabled(false);
        scenarioTargetField.addValueChangeListener(e -> sendTarget.setEnabled(e.getValue() != null && !e.getValue().trim().isEmpty()));
        inputTargetLayout = createInputLayout(scenarioTargetField, sendTarget);

        // STEP 2: Descrizione
        aiMsgStep1 = createAiMessage("Descrivimi brevemente lo scenario che vuoi che io crei");
        shortDescription = FieldGenerator.createTextArea("Breve Descrizione", "Inserisci una breve descrizione dello scenario che vuoi creare", true);
        shortDescription.setMaxLength(200);
        shortDescription.setValueChangeMode(ValueChangeMode.EAGER);
        sendDesc = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create()); // Assegna al campo di classe
        styleSendButton(sendDesc);
        sendDesc.setEnabled(false);
        shortDescription.addValueChangeListener(e -> sendDesc.setEnabled(shortDescription.getValue() != null && !shortDescription.getValue().trim().isEmpty()));
        inputDescLayout = createInputLayout(shortDescription, sendDesc);

        // STEP 3: Difficoltà
        aiMsgStep2 = createAiMessage("Quanto vuoi che sia difficile?");
        List<String> difficulties = List.of("Facile", "Media", "Difficile");
        difficultySelect = FieldGenerator.createSelect("Difficoltà", difficulties, "Facile", true);
        sendDiff = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create()); // Assegna al campo di classe
        styleSendButton(sendDiff);
        inputDiffLayout = createInputLayout(difficultySelect, sendDiff);
    }

    private void styleSendButton(Button button) {
        button.setWidthFull();
        button.getStyle().set("margin-top", "0.5em");
        button.setEnabled(true);
    }

    private VerticalLayout createInputLayout(com.vaadin.flow.component.Component field, Button button) {
        VerticalLayout layout = new VerticalLayout(field, button);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();
        layout.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.04)")
                .set("padding", "1em");
        return layout;
    }

    private HorizontalLayout createAiMessage(String text) {
        HorizontalLayout messageLayout = new HorizontalLayout();
        messageLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        Icon newAiIcon = FontAwesome.Solid.ROBOT.create(); // Nuova istanza
        newAiIcon.setSize("20px");
        newAiIcon.getStyle().set("margin-left", "0.5em"); // Margine per l'icona AI
        Div bubble = createBubble(text, false);
        messageLayout.add(newAiIcon, bubble);
        return messageLayout;
    }

    private Div createBubble(String text, boolean isUser) {
        Div bubble = new Div(new Span(text));
        bubble.getStyle()
                .set("padding", "0.7em 1em")
                .set("border-radius", "16px")
                .set("margin-top", "0.5em") // Margine solo sopra e sotto per impostazione predefinita
                .set("margin-bottom", "0.5em");

        if (isUser) {
            bubble.getStyle()
                    .set("background", "var(--lumo-primary-color)")
                    .set("color", "white")
                    .set("margin-right", "0.5em"); // Margine tra bolla utente e icona utente
        } else {
            bubble.getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("color", "black")
                    .set("margin-left", "0.5em"); // Margine tra icona AI e bolla AI
        }
        return bubble;
    }

    private void updateChatLayout(VerticalLayout chatLayout) {
        chatLayout.removeAll();
        chatLayout.add(aiPresentationMsg, aiMsgStep0);

        if (step >= 1 && scenarioTypeSelect.getValue() != null) {
            chatLayout.add(createUserMessage(scenarioTypeSelect.getValue()));
        }

        if (step == 0) {
            chatLayout.add(inputTypeLayout);
        } else if (step == 1) {
            chatLayout.add(aiMsgStepTarget, inputTargetLayout); // Mostra input per il target
        } else if (step >= 2 && scenarioTargetField.getValue() != null && !scenarioTargetField.getValue().trim().isEmpty()) {
            chatLayout.add(aiMsgStepTarget); // Mostra il messaggio AI precedente per il target
            chatLayout.add(createUserMessage(scenarioTargetField.getValue())); // Mostra il messaggio utente per il target
        }

        if (step == 2) {
            chatLayout.add(aiMsgStep1, inputDescLayout); // Mostra input per la descrizione
        } else if (step >= 3 && !shortDescription.isEmpty()) {
            chatLayout.add(aiMsgStep1); // Mostra il messaggio AI precedente per la descrizione
            chatLayout.add(createUserMessage(shortDescription.getValue()));
        }

        if (step == 3) {
            chatLayout.add(aiMsgStep2, inputDiffLayout);
        } else if (step >= 4 && difficultySelect.getValue() != null) {
            chatLayout.add(aiMsgStep2); // Mostra il messaggio AI precedente
            chatLayout.add(createUserMessage(difficultySelect.getValue()));
        }
    }

    private HorizontalLayout createUserMessage(String messageText) {
        HorizontalLayout userMsg = new HorizontalLayout();
        userMsg.setAlignItems(FlexComponent.Alignment.BASELINE);
        userMsg.setWidthFull();
        userMsg.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        Div userBubble = createBubble(messageText, true);
        Icon newUserIcon = FontAwesome.Solid.USER.create(); // Nuova istanza
        newUserIcon.setSize("20px");
        newUserIcon.getStyle().set("margin-right", "0.5em"); // Margine per l'icona utente
        userMsg.add(userBubble, newUserIcon);
        return userMsg;
    }
}
