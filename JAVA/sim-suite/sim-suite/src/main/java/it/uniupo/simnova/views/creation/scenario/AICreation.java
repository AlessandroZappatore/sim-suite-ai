package it.uniupo.simnova.views.creation.scenario;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;

import com.google.gson.Gson;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.uniupo.simnova.service.ActiveNotifierManager;
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
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static it.uniupo.simnova.views.ui.helper.support.ErrorExtractor.extractErrorReasonFromJson;

/**
 * Classe che gestisce la creazione di uno scenario utilizzando l'intelligenza artificiale.
 *
 * @author Alessandro Zappatore
 * @version 1.4
 */
@PageTitle("AI Creation")
@Route(value = "ai-creation", layout = MainLayout.class)
public class AICreation extends Composite<VerticalLayout> {
    private static final Logger logger = LoggerFactory.getLogger(AICreation.class);
    private static final Gson gson = new Gson();
    private final ScenarioImportService scenarioImportService;
    private final FileStorageService fileStorageService;
    private final ExternalApiService externalApiService;
    private final NotifierService notifierService;
    private final ExecutorService executorService;
    private final ActiveNotifierManager activeNotifierManager;
    private final Icon aiIcon = FontAwesome.Solid.ROBOT.create();
    private int step = 0;
    private HorizontalLayout aiPresentationMsg, aiMsgStep0, aiMsgStep1, aiMsgStepTarget, aiMsgStep2, aiFinalMsg;
    private Select<String> scenarioTypeSelect;
    private TextField scenarioTargetField;
    private TextArea shortDescription;
    private Select<String> difficultySelect;
    private HorizontalLayout inputTypeLayout, inputTargetLayout, inputDescLayout, inputDiffLayout;
    private Button sendType, sendTarget, sendDesc, sendDiff, nextButton;

    public AICreation(FileStorageService fileStorageService,
                      ScenarioImportService scenarioImportService,
                      ExternalApiService externalApiService,
                      NotifierService notifierService,
                      ExecutorService executorService, ActiveNotifierManager activeNotifierManager) {
        this.fileStorageService = fileStorageService;
        this.scenarioImportService = scenarioImportService;
        this.externalApiService = externalApiService;
        this.notifierService = notifierService;
        this.executorService = executorService;
        this.activeNotifierManager = activeNotifierManager;
        aiIcon.setSize("20px");
        initView();
    }

    private void startScenarioGeneration() {
        final String notificationId = activeNotifierManager.show("Generazione scenario in corso...");
        final UI ui = UI.getCurrent();
        ScenarioGenerationRequest request = new ScenarioGenerationRequest(
                shortDescription.getValue(),
                scenarioTypeSelect.getValue(),
                scenarioTargetField.getValue()
        );
        executorService.submit(() -> {
            try {
                Optional<String> jsonResponseOptional = externalApiService.generateScenario(request);
                if (jsonResponseOptional.isPresent()) {
                    scenarioImportService.createScenarioByJSON(jsonResponseOptional.get().getBytes(StandardCharsets.UTF_8));
                    notifierService.notify(ui, new NotifierService.NotificationPayload(
                            NotifierService.Status.SUCCESS,
                            "Generazione Completata",
                            "Nuovo Scenario creato con successo!",
                            notificationId
                    ));
                } else {
                    notifierService.notify(ui, new NotifierService.NotificationPayload(
                            NotifierService.Status.ERROR,
                            "Errore di Generazione",
                            "Il servizio AI non ha restituito una risposta valida.",
                            notificationId
                    ));
                }
            } catch (Exception e) {
                logger.error("Fallimento nel task di generazione scenario in background.", e);
                String errorTitle = "Errore Critico";
                String errorDetails;
                if (e instanceof HttpClientErrorException hcee) {
                    errorTitle = "Errore nella Richiesta";
                    errorDetails = extractErrorReasonFromJson(hcee.getResponseBodyAsString(), gson);
                } else {
                    errorDetails = "Si è verificato un problema tecnico. Controllare i log per maggiori dettagli.";
                }
                notifierService.notify(ui, new NotifierService.NotificationPayload(
                        NotifierService.Status.ERROR,
                        errorTitle,
                        errorDetails,
                        notificationId
                ));
            }
        });
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
        initComponents();
        updateChatLayout(chatLayout);
        contentLayout.add(headerSection, chatLayout);
        contentLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, chatLayout);
        nextButton = StyleApp.getNextButton();
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);
        nextButton.setVisible(false);
        nextButton.addClickListener(e -> nextButton.getUI().ifPresent(ui -> ui.navigate("scenari")));
        mainLayout.add(customHeader, contentLayout, footerLayout);
        backButton.addClickListener(e -> backButton.getUI().ifPresent(ui -> ui.navigate("")));
        sendType.addClickListener(e -> {
            step = 1;
            updateChatLayout(chatLayout);
        });
        sendTarget.addClickListener(e -> {
            if (scenarioTargetField.getValue() != null && !scenarioTargetField.getValue().trim().isEmpty()) {
                step = 2;
                updateChatLayout(chatLayout);
            }
        });
        sendDesc.addClickListener(e -> {
            if (!shortDescription.isEmpty()) {
                step = 3;
                updateChatLayout(chatLayout);
            }
        });
        sendDiff.addClickListener(e -> {
            step = 4;
            updateChatLayout(chatLayout);
            startScenarioGeneration();
        });
    }

    private void initComponents() {
        aiPresentationMsg = createAiMessage("Ciao! Sono Leo, il tuo assistente AI, ti aiuterò a creare uno scenario.");
        aiMsgStep0 = createAiMessage("Che tipo di scenario vuoi creare?");
        List<String> scenarioTypes = List.of("Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario");
        scenarioTypeSelect = FieldGenerator.createSelect("Tipologia Scenario", scenarioTypes, "Quick Scenario", true);
        sendType = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create());
        styleSendButton(sendType);
        inputTypeLayout = createUserInputArea(scenarioTypeSelect, sendType);
        aiMsgStepTarget = createAiMessage("Qual è il target primario di questo scenario? (es. Studenti di medicina, Infermieri specializzati, etc.)");
        scenarioTargetField = FieldGenerator.createTextField("Target Scenario", "Inserisci il target dello scenario", true);
        scenarioTargetField.setValueChangeMode(ValueChangeMode.EAGER);
        sendTarget = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create());
        styleSendButton(sendTarget);
        sendTarget.setEnabled(false);
        scenarioTargetField.addValueChangeListener(e -> sendTarget.setEnabled(e.getValue() != null && !e.getValue().trim().isEmpty()));
        inputTargetLayout = createUserInputArea(scenarioTargetField, sendTarget);
        aiMsgStep1 = createAiMessage("Descrivimi brevemente lo scenario che vuoi che io crei");
        shortDescription = FieldGenerator.createTextArea("Breve Descrizione", "Inserisci una breve descrizione dello scenario che vuoi creare", true);
        shortDescription.setMaxLength(500);
        shortDescription.setValueChangeMode(ValueChangeMode.EAGER);
        sendDesc = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create());
        styleSendButton(sendDesc);
        sendDesc.setEnabled(false);
        shortDescription.addValueChangeListener(e -> sendDesc.setEnabled(shortDescription.getValue() != null && !shortDescription.getValue().trim().isEmpty()));
        inputDescLayout = createUserInputArea(shortDescription, sendDesc);
        aiMsgStep2 = createAiMessage("Quanto vuoi che sia difficile?");
        List<String> difficulties = List.of("Facile", "Media", "Difficile");
        difficultySelect = FieldGenerator.createSelect("Difficoltà", difficulties, "Facile", true);
        sendDiff = new Button("Invia", FontAwesome.Solid.PAPER_PLANE.create());
        styleSendButton(sendDiff);
        inputDiffLayout = createUserInputArea(difficultySelect, sendDiff);
        aiFinalMsg = createAiMessage("Perfetto! Ho tutte le informazioni che mi servono. Sto generando il tuo scenario...");
    }

    private void styleSendButton(Button button) {
        button.setWidthFull();
    }

    /**
     * Crea l'area di input per l'utente, raggruppando campo, pulsante e icona
     * in un'unica "bolla" con sfondo e ombra coerenti.
     *
     * @param field  il campo di input (es. TextField, Select)
     * @param button il pulsante di invio
     * @return un HorizontalLayout che contiene l'area di input, allineato a destra.
     */
    private HorizontalLayout createUserInputArea(com.vaadin.flow.component.Component field, Button button) {
        VerticalLayout fieldAndButtonStack = new VerticalLayout(field, button);
        fieldAndButtonStack.setPadding(false);
        fieldAndButtonStack.setSpacing(true);
        fieldAndButtonStack.setWidthFull();

        Icon newUserIcon = FontAwesome.Solid.USER.create();
        newUserIcon.setSize("20px");

        HorizontalLayout bubbleContainer = new HorizontalLayout(fieldAndButtonStack, newUserIcon);
        bubbleContainer.setPadding(true);
        bubbleContainer.setBoxSizing(BoxSizing.BORDER_BOX);
        bubbleContainer.setAlignItems(FlexComponent.Alignment.START);
        bubbleContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("width", "100%")
                .set("max-width", "450px");

        HorizontalLayout wrapper = new HorizontalLayout(bubbleContainer);
        wrapper.setWidthFull();
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        return wrapper;
    }


    private HorizontalLayout createAiMessage(String text) {
        HorizontalLayout messageLayout = new HorizontalLayout();
        messageLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        Icon newAiIcon = FontAwesome.Solid.ROBOT.create();
        newAiIcon.setSize("20px");
        newAiIcon.getStyle().set("margin-left", "0.5em");
        Div bubble = createBubble(text, false);
        messageLayout.add(newAiIcon, bubble);
        return messageLayout;
    }

    private Div createBubble(String text, boolean isUser) {
        Div bubble = new Div(new Span(text));
        bubble.getStyle()
                .set("padding", "0.7em 1em")
                .set("border-radius", "16px")
                .set("margin-top", "0.5em")
                .set("margin-bottom", "0.5em")
                .set("max-width", "70%");
        if (isUser) {
            bubble.getStyle()
                    .set("background", "var(--lumo-primary-color)")
                    .set("color", "var(--lumo-primary-contrast-color)")
                    .set("margin-right", "0.5em");
        } else {
            bubble.getStyle()
                    .set("background", "var(--lumo-contrast-10pct)")
                    .set("color", "var(--lumo-body-text-color)")
                    .set("margin-left", "0.5em");
        }
        return bubble;
    }

    private HorizontalLayout createUserMessage(String messageText) {
        HorizontalLayout userMsg = new HorizontalLayout();
        userMsg.setAlignItems(FlexComponent.Alignment.BASELINE);
        userMsg.setWidthFull();
        userMsg.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        Div userBubble = createBubble(messageText, true);
        Icon newUserIcon = FontAwesome.Solid.USER.create();
        newUserIcon.setSize("20px");
        newUserIcon.getStyle().set("margin-left", "0.5em");
        userMsg.add(userBubble, newUserIcon);
        return userMsg;
    }

    private void updateChatLayout(VerticalLayout chatLayout) {
        chatLayout.removeAll();
        chatLayout.add(aiPresentationMsg, aiMsgStep0);
        if (step == 0) {
            chatLayout.add(inputTypeLayout);
            return;
        }
        chatLayout.add(createUserMessage(scenarioTypeSelect.getValue()));
        chatLayout.add(aiMsgStepTarget);
        if (step == 1) {
            chatLayout.add(inputTargetLayout);
            return;
        }
        chatLayout.add(createUserMessage(scenarioTargetField.getValue()));
        chatLayout.add(aiMsgStep1);
        if (step == 2) {
            chatLayout.add(inputDescLayout);
            return;
        }
        chatLayout.add(createUserMessage(shortDescription.getValue()));
        chatLayout.add(aiMsgStep2);
        if (step == 3) {
            chatLayout.add(inputDiffLayout);
            return;
        }
        chatLayout.add(createUserMessage(difficultySelect.getValue()));
        if (step == 4) {
            chatLayout.add(aiFinalMsg);
            nextButton.setVisible(true);
        }
    }
}