package it.uniupo.simnova.views.creation.scenario;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.MainLayout;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.common.utils.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static it.uniupo.simnova.views.constant.TargetConst.*;

/**
 * Vista per la selezione del target e dei gruppi di apprendimento per uno scenario di simulazione.
 * <p>
 * Questa vista permette di specificare a quale categoria di professionisti sanitari
 * o studenti è destinato lo scenario. Offre opzioni per diverse specializzazioni e anni di corso,
 * con campi condizionali che appaiono in base alla selezione.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Target")
@Route(value = "target", layout = MainLayout.class)
public class TargetView extends Composite<VerticalLayout> implements HasUrlParameter<String> {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(TargetView.class);

    /**
     * Il servizio per la gestione delle operazioni sugli scenari.
     */
    private final ScenarioService scenarioService;

    /**
     * Gruppo di radio button per la selezione della categoria principale del target.
     */
    private final RadioButtonGroup<String> targetRadioGroup = new RadioButtonGroup<>();

    /**
     * Layout verticale per le opzioni condizionali relative ai "Medici Assistenti".
     */
    private final VerticalLayout mediciAssistentiOptionsLayout = createConditionalOptionsLayout();

    /**
     * Layout verticale per le opzioni condizionali relative ai "Medici Specialisti".
     */
    private final VerticalLayout mediciSpecialistiOptionsLayout = createConditionalOptionsLayout();

    /**
     * Layout verticale per le opzioni condizionali relative agli "Studenti di Medicina".
     */
    private final VerticalLayout studentiMedicinaOptionsLayout = createConditionalOptionsLayout();

    /**
     * Layout verticale per le opzioni condizionali relative agli "Infermieri".
     */
    private final VerticalLayout studentiInfermieristicaOptionsLayout = createConditionalOptionsLayout();

    /**
     * Layout verticale per le opzioni condizionali relative agli "Infermieri Specializzati".
     */
    private final VerticalLayout infSpecOptionsLayout = createConditionalOptionsLayout();

    /**
     * Layout verticale per le opzioni condizionali relative agli "Studenti di Odontoiatria".
     */
    private final VerticalLayout studentiOdontoiatriaOptionsLayout = createConditionalOptionsLayout();

    /**
     * Layout verticale per le opzioni condizionali relative alla categoria "Altro".
     */
    private final VerticalLayout altroOptionsLayout = createConditionalOptionsLayout();

    /**
     * Gruppo di radio button per la selezione dell'anno di corso dei "Medici Assistenti".
     */
    private final RadioButtonGroup<Integer> mediciAssistentiYearRadio = new RadioButtonGroup<>();

    /**
     * Checkbox per la specializzazione "Anestesia" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiAnestesiaChk = new Checkbox(SPEC_ANESTESIA);

    /**
     * Checkbox per la specializzazione "Emergenza" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiEmergenzaChk = new Checkbox(SPEC_EMERGENZA);

    /**
     * Checkbox per la specializzazione "Cure Intense" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiCureIntenseChk = new Checkbox(SPEC_CURE_INTENSE);

    /**
     * Checkbox per la specializzazione "Chirurgia" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiChirurgiaChk = new Checkbox(SPEC_CHIRURGIA);

    /**
     * Checkbox per la specializzazione "Ostetricia" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiOstetriciaChk = new Checkbox(SPEC_OSTETRICIA);

    /**
     * Checkbox per la specializzazione "Pediatria" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiPediatriaChk = new Checkbox(SPEC_PEDIATRIA);

    /**
     * Checkbox per la specializzazione "Interna" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiInternaChk = new Checkbox(SPEC_INTERNA);

    /**
     * Checkbox per la specializzazione "Cardiologia" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiCardiologiaChk = new Checkbox(SPEC_CARDIOLOGIA);

    /**
     * Checkbox per la specializzazione "Disastri" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiDisastriChk = new Checkbox(SPEC_DISASTRI);

    /**
     * Checkbox per la specializzazione "Altro" dei "Medici Specialisti".
     */
    private final Checkbox mediciSpecialistiAltroChk = new Checkbox(ALTRO);

    /**
     * Campo di testo per specificare la specializzazione "Altro" dei "Medici Specialisti".
     */
    private final TextField mediciSpecialistiAltroField = new TextField();

    /**
     * Gruppo di radio button per la selezione dell'anno di corso degli "Studenti di Medicina".
     */
    private final RadioButtonGroup<Integer> studentiMedicinaYearRadio = new RadioButtonGroup<>();

    /**
     * Gruppo di radio button per la selezione dell'anno di corso degli "Studenti di Infermieristica".
     */
    private final RadioButtonGroup<Integer> studentiInfermieristicaYearRadio = new RadioButtonGroup<>();

    /**
     * Checkbox per la specializzazione "Anestesia" degli "Infermieri Specializzati".
     */
    private final Checkbox infSpecAnestesiaChk = new Checkbox(SPEC_ANESTESIA);

    /**
     * Checkbox per la specializzazione "Cure Intense" degli "Infermieri Specializzati".
     */
    private final Checkbox infSpecCureIntenseChk = new Checkbox(SPEC_CURE_INTENSE);

    /**
     * Checkbox per la specializzazione "Cure Urgenti" degli "Infermieri Specializzati".
     */
    private final Checkbox infSpecCureUrgentiChk = new Checkbox(SPEC_INF_CURE_URGENTI);

    /**
     * Gruppo di radio button per la selezione dell'anno di corso degli "Studenti di Odontoiatria".
     */
    private final RadioButtonGroup<Integer> studentiOdontoiatriaYearRadio = new RadioButtonGroup<>();

    /**
     * Campo di testo per specificare la categoria "Altro".
     */
    private final TextField altroField = new TextField();

    /**
     * Bottone per procedere alla schermata successiva del flusso di creazione/modifica scenario.
     */
    private final Button nextButton = StyleApp.getNextButton();

    /**
     * L'ID dello scenario corrente, passato come parametro URL.
     */
    private Integer scenarioId;

    /**
     * La modalità della vista ("create" per creazione, "edit" per modifica).
     */
    private String mode;

    /**
     * Costruisce una nuova istanza di <code>TargetView</code>.
     * Inizializza l'interfaccia utente, inclusi l'header, il corpo centrale con le opzioni di selezione target
     * e il footer con i bottoni di navigazione.
     *
     * @param scenarioService    Il servizio per la gestione degli scenari.
     * @param fileStorageService Il servizio per la gestione dei file, utilizzato per l'intestazione dell'applicazione.
     */
    public TargetView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configura il layout principale della vista.
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // Configura l'header dell'applicazione e il bottone "Indietro".
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.setTooltipText("Torna alla schermata di creazione dello scenario");
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // Sezione dell'intestazione visuale per la vista, con titolo, sottotitolo e icona.
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "TARGET E GRUPPI DI APPRENDIMENTO",
                "Seleziona la categoria di discenti per cui è progettato lo scenario di simulazione. Per alcune categorie, saranno richieste informazioni aggiuntive.",
                FontAwesome.Solid.BULLSEYE.create(), // Icona target.
                "var(--lumo-primary-color)"
        );

        // Configura il layout per il contenuto centrale e aggiunge la sezione dell'header.
        VerticalLayout contentLayout = StyleApp.getContentLayout();
        contentLayout.add(headerSection);

        // Configura il contenuto principale della vista, inclusi i radio group e i layout condizionali.
        configureContent(contentLayout);

        // Configura il footer con il bottone "Avanti".
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Aggiunge tutte le sezioni al layout principale.
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        // Listener per il bottone "Indietro": naviga alla vista "startCreation" con l'ID scenario.
        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("startCreation/" + scenarioId)));

        // Listener per il bottone "Avanti": tenta di salvare il target e navigare.
        nextButton.addClickListener(e -> saveTargetAndNavigate(e.getSource().getUI()));
        // Aggiunge listener per gestire la visibilità e la validazione dei campi condizionali.
        addListeners();
    }

    /**
     * Configura il layout centrale della vista, aggiungendo il gruppo di radio button
     * per il target e i layout condizionali per le opzioni specifiche.
     *
     * @param contentLayout Il {@link VerticalLayout} che ospita il contenuto principale della vista.
     */
    private void configureContent(VerticalLayout contentLayout) {
        setupTargetRadioGroup(); // Configura il radio group principale.

        // Configura e aggiunge i layout per le opzioni condizionali.
        setupMediciAssistentiOptions();
        setupMediciSpecialistiOptions();
        setupStudentiMedicinaOptions();
        setupStudentiInfermieristicaOptions();
        setupInfSpecOptions();
        setupStudentiOdontoiatriaOptions();
        setupAltroOptions();

        // Aggiunge tutti i componenti al layout del contenuto.
        contentLayout.add(
                targetRadioGroup,
                mediciAssistentiOptionsLayout,
                mediciSpecialistiOptionsLayout,
                studentiMedicinaOptionsLayout,
                studentiInfermieristicaOptionsLayout,
                infSpecOptionsLayout,
                studentiOdontoiatriaOptionsLayout,
                altroOptionsLayout
        );

        // Aggiunge il layout del contenuto al layout principale del componente.
        getContent().add(contentLayout);
    }

    /**
     * Crea un layout verticale pre-configurato per ospitare opzioni condizionali.
     * Il layout è inizialmente nascosto e ha stili predefiniti per bordi e padding.
     *
     * @return Il {@link VerticalLayout} configurato.
     */
    private VerticalLayout createConditionalOptionsLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-s)");
        layout.setVisible(false); // Inizialmente nascosto.
        return layout;
    }

    /**
     * Nasconde tutti i layout condizionali, resettando la visibilità.
     * Utilizzato quando una nuova opzione del target principale viene selezionata.
     */
    private void hideAllConditionalLayouts() {
        mediciAssistentiOptionsLayout.setVisible(false);
        mediciSpecialistiOptionsLayout.setVisible(false);
        studentiMedicinaOptionsLayout.setVisible(false);
        studentiInfermieristicaOptionsLayout.setVisible(false);
        infSpecOptionsLayout.setVisible(false);
        studentiOdontoiatriaOptionsLayout.setVisible(false);
        altroOptionsLayout.setVisible(false);
    }

    /**
     * Configura il {@link RadioButtonGroup} principale per la selezione del destinatario dello scenario.
     * Imposta le voci disponibili e le proprietà di visualizzazione e validazione.
     */
    private void setupTargetRadioGroup() {
        targetRadioGroup.setLabel("Seleziona la categoria principale del target:");
        targetRadioGroup.setItems(
                MEDICI_ASSISTENTI, MEDICI_SPECIALISTI, STUDENTI_MEDICINA,
                INFERMIERI, STUDENTI_INFERMIERISTICA, INFERMIERI_SPECIALIZZATI,
                ODONTOIATRI, STUDENTI_ODONTOIATRIA, SOCCORRITORI,
                ASSISTENTI_DI_CURA, OPERATORE_SOCIO_SANITARIO, ALTRO
        );
        targetRadioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        targetRadioGroup.setWidthFull();
        targetRadioGroup.setRequired(true); // Campo obbligatorio.
        targetRadioGroup.setRequiredIndicatorVisible(true);
    }

    /**
     * Configura le opzioni specifiche per la categoria "Medici Assistenti",
     * inclusa la selezione dell'anno di corso.
     */
    private void setupMediciAssistentiOptions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        Paragraph label = createBoldParagraph("Seleziona l'anno di corso:");
        HorizontalLayout radioLayout = new HorizontalLayout(mediciAssistentiYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);

        mediciAssistentiYearRadio.setItems(1, 2, 3, 4, 5, 6); // Anni di specializzazione.
        mediciAssistentiYearRadio.setRequired(true); // Obbligatorio quando visibile.
        mediciAssistentiYearRadio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        layout.add(label, radioLayout);
        mediciAssistentiOptionsLayout.add(layout);
    }

    /**
     * Configura le opzioni specifiche per la categoria "Medici Specialisti",
     * inclusa la selezione di specializzazioni multiple tramite checkbox e un campo "Altro".
     */
    private void setupMediciSpecialistiOptions() {
        Paragraph label = createBoldParagraph("Seleziona una o più specializzazioni:");

        HorizontalLayout row1 = new HorizontalLayout(mediciSpecialistiAnestesiaChk, mediciSpecialistiEmergenzaChk, mediciSpecialistiCureIntenseChk);
        HorizontalLayout row2 = new HorizontalLayout(mediciSpecialistiChirurgiaChk, mediciSpecialistiOstetriciaChk, mediciSpecialistiPediatriaChk);
        HorizontalLayout row3 = new HorizontalLayout(mediciSpecialistiInternaChk, mediciSpecialistiCardiologiaChk, mediciSpecialistiDisastriChk);

        HorizontalLayout row4 = new HorizontalLayout();
        row4.setAlignItems(FlexComponent.Alignment.BASELINE); // Allinea verticalmente i componenti.
        mediciSpecialistiAltroField.setPlaceholder("Specifica altra specializzazione");
        mediciSpecialistiAltroField.setWidth("200px");
        mediciSpecialistiAltroField.setEnabled(false); // Inizialmente disabilitato.
        row4.add(mediciSpecialistiAltroChk, mediciSpecialistiAltroField);

        mediciSpecialistiOptionsLayout.add(label, row1, row2, row3, row4);

        // Listener per abilitare/disabilitare il campo "Altro" in base al checkbox.
        mediciSpecialistiAltroChk.addValueChangeListener(event -> {
            boolean isChecked = event.getValue();
            mediciSpecialistiAltroField.setEnabled(isChecked);
            mediciSpecialistiAltroField.setRequired(isChecked); // Campo obbligatorio se il checkbox è selezionato.
            if (!isChecked) {
                mediciSpecialistiAltroField.clear(); // Pulisce il campo se deselezionato.
            }
        });
    }

    /**
     * Configura le opzioni specifiche per la categoria "Studenti di Medicina",
     * inclusa la selezione dell'anno accademico.
     */
    private void setupStudentiMedicinaOptions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        Paragraph label = createBoldParagraph("Seleziona l'anno accademico:");
        HorizontalLayout radioLayout = new HorizontalLayout(studentiMedicinaYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);
        studentiMedicinaYearRadio.setItems(1, 2, 3, 4, 5, 6); // Anni di corso di medicina.
        studentiMedicinaYearRadio.setRequired(true); // Obbligatorio quando visibile.
        studentiMedicinaYearRadio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        layout.add(label, radioLayout);
        studentiMedicinaOptionsLayout.add(layout);
    }

    /**
     * Configura le opzioni specifiche per la categoria "Studenti di Infermieristica",
     * inclusa la selezione dell'anno accademico.
     */
    private void setupStudentiInfermieristicaOptions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        Paragraph label = createBoldParagraph("Seleziona l'anno accademico:");
        HorizontalLayout radioLayout = new HorizontalLayout(studentiInfermieristicaYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);
        studentiInfermieristicaYearRadio.setItems(1, 2, 3); // Anni di corso di infermieristica.
        studentiInfermieristicaYearRadio.setRequired(true); // Obbligatorio quando visibile.
        studentiInfermieristicaYearRadio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        layout.add(label, radioLayout);
        studentiInfermieristicaOptionsLayout.add(layout);
    }

    /**
     * Configura le opzioni specifiche per la categoria "Infermieri Specializzati",
     * inclusa la selezione di specializzazioni multiple tramite checkbox.
     */
    private void setupInfSpecOptions() {
        Paragraph label = createBoldParagraph("Seleziona una o più specializzazioni:");
        HorizontalLayout checkLayout = new HorizontalLayout(
                infSpecAnestesiaChk, infSpecCureIntenseChk, infSpecCureUrgentiChk
        );
        checkLayout.setSpacing(true);
        infSpecOptionsLayout.add(label, checkLayout);
    }

    /**
     * Configura le opzioni specifiche per la categoria "Studenti di Odontoiatria",
     * inclusa la selezione dell'anno accademico.
     */
    private void setupStudentiOdontoiatriaOptions() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        Paragraph label = createBoldParagraph("Seleziona l'anno accademico:");
        HorizontalLayout radioLayout = new HorizontalLayout(studentiOdontoiatriaYearRadio);
        radioLayout.setPadding(false);
        radioLayout.setSpacing(true);
        studentiOdontoiatriaYearRadio.setItems(1, 2, 3, 4, 5); // Anni di corso di odontoiatria.
        studentiOdontoiatriaYearRadio.setRequired(true); // Obbligatorio quando visibile.
        studentiOdontoiatriaYearRadio.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        layout.add(label, radioLayout);
        studentiOdontoiatriaOptionsLayout.add(layout);
    }

    /**
     * Configura le opzioni specifiche per la categoria "Altro",
     * che include un campo di testo libero per specificare il destinatario.
     */
    private void setupAltroOptions() {
        Paragraph label = createBoldParagraph("Specifica la categoria del destinatario:");
        altroField.setWidthFull();
        altroField.setRequired(true); // Campo obbligatorio quando visibile.
        altroOptionsLayout.add(label, altroField);
    }

    /**
     * Crea e restituisce un {@link Paragraph} con il testo fornito in grassetto.
     *
     * @param text Il testo (<code>String</code>) da visualizzare.
     * @return Un nuovo oggetto {@link Paragraph} con il testo in grassetto.
     */
    private Paragraph createBoldParagraph(String text) {
        Paragraph p = new Paragraph(text);
        p.getStyle().set("font-weight", "bold");
        return p;
    }

    /**
     * Aggiunge i listener necessari agli elementi della vista, in particolare al {@link RadioButtonGroup}
     * del target principale per gestire la visibilità e lo stato di obbligatorietà dei campi condizionali.
     */
    private void addListeners() {
        targetRadioGroup.addValueChangeListener(event -> {
            String selectedItem = event.getValue();
            updateConditionalLayoutsVisibility(selectedItem); // Aggiorna la visibilità dei layout.
            updateFieldsRequiredStatus(selectedItem);         // Aggiorna lo stato dei campi obbligatori.
        });
    }

    /**
     * Aggiorna la visibilità dei layout condizionali in base alla categoria di target selezionata.
     * Tutte le sezioni condizionali vengono prima nascoste, e poi viene mostrata solo quella pertinente.
     *
     * @param selectedTarget La categoria di target (<code>String</code>) selezionata nel radio group principale.
     */
    private void updateConditionalLayoutsVisibility(String selectedTarget) {
        hideAllConditionalLayouts(); // Nasconde tutti i layout condizionali.
        if (selectedTarget == null) {
            return;
        }

        // Mostra il layout appropriato in base alla selezione.
        switch (selectedTarget) {
            case MEDICI_ASSISTENTI:
                mediciAssistentiOptionsLayout.setVisible(true);
                break;
            case MEDICI_SPECIALISTI:
                mediciSpecialistiOptionsLayout.setVisible(true);
                break;
            case STUDENTI_MEDICINA:
                studentiMedicinaOptionsLayout.setVisible(true);
                break;
            case STUDENTI_INFERMIERISTICA:
                studentiInfermieristicaOptionsLayout.setVisible(true);
                break;
            case INFERMIERI_SPECIALIZZATI:
                infSpecOptionsLayout.setVisible(true);
                break;
            case STUDENTI_ODONTOIATRIA:
                studentiOdontoiatriaOptionsLayout.setVisible(true);
                break;
            case ALTRO:
                altroOptionsLayout.setVisible(true);
                break;
        }
        logger.debug("Visibilità dei layout condizionali aggiornata per il target: '{}'.", selectedTarget);
    }

    /**
     * Aggiorna lo stato di obbligatorietà (<code>required</code>) dei campi di input condizionali
     * in base alla categoria di target selezionata.
     *
     * @param selectedTarget La categoria di target (<code>String</code>) selezionata nel radio group principale.
     */
    private void updateFieldsRequiredStatus(String selectedTarget) {
        // Resetta lo stato di obbligatorietà per tutti i campi condizionali.
        mediciAssistentiYearRadio.setRequired(false);
        studentiMedicinaYearRadio.setRequired(false);
        studentiInfermieristicaYearRadio.setRequired(false);
        studentiOdontoiatriaYearRadio.setRequired(false);
        altroField.setRequired(false);
        mediciSpecialistiAltroField.setRequired(false);

        if (selectedTarget == null) {
            // Se nessun target è selezionato, tutti i campi condizionali non sono obbligatori.
            updateAllRequiredIndicators();
            return;
        }

        // Imposta lo stato di obbligatorietà solo per i campi pertinenti alla selezione.
        switch (selectedTarget) {
            case MEDICI_ASSISTENTI:
                mediciAssistentiYearRadio.setRequired(true);
                break;
            case STUDENTI_MEDICINA:
                studentiMedicinaYearRadio.setRequired(true);
                break;
            case STUDENTI_INFERMIERISTICA:
                studentiInfermieristicaYearRadio.setRequired(true);
                break;
            case STUDENTI_ODONTOIATRIA:
                studentiOdontoiatriaYearRadio.setRequired(true);
                break;
            case ALTRO:
                altroField.setRequired(true);
                break;
            case MEDICI_SPECIALISTI:
                // Se MEDICI_SPECIALISTI è selezionato e "Altro" è spuntato, il campo "Altro" è obbligatorio.
                mediciSpecialistiAltroField.setRequired(mediciSpecialistiAltroChk.getValue());
                break;
            case INFERMIERI_SPECIALIZZATI:
                // Per infermieri specializzati, i checkbox di specializzazione non rendono i campi di testo obbligatori.
                break;
            default:
                // Per tutte le altre categorie, nessun campo condizionale aggiuntivo è richiesto.
                break;
        }
        updateAllRequiredIndicators(); // Aggiorna la visibilità dell'indicatore di obbligatorietà.
        logger.debug("Stato di obbligatorietà dei campi aggiornato per il target: '{}'.", selectedTarget);
    }

    /**
     * Aggiorna la visibilità dell'indicatore di campo obbligatorio per tutti i campi {@link RadioButtonGroup}
     * e {@link TextField} condizionali. Questo metodo è chiamato dopo aver modificato il flag {@code required}.
     */
    private void updateAllRequiredIndicators() {
        mediciAssistentiYearRadio.setRequiredIndicatorVisible(mediciAssistentiYearRadio.isRequired());
        studentiMedicinaYearRadio.setRequiredIndicatorVisible(studentiMedicinaYearRadio.isRequired());
        studentiInfermieristicaYearRadio.setRequiredIndicatorVisible(studentiInfermieristicaYearRadio.isRequired());
        studentiOdontoiatriaYearRadio.setRequiredIndicatorVisible(studentiOdontoiatriaYearRadio.isRequired());
        altroField.setRequiredIndicatorVisible(altroField.isRequired());
        mediciSpecialistiAltroField.setRequiredIndicatorVisible(mediciSpecialistiAltroField.isRequired());
    }

    /**
     * Salva il target selezionato e i suoi dettagli nel database, quindi naviga alla schermata successiva.
     * Esegue la validazione dei campi prima del salvataggio.
     *
     * @param ui L'istanza di {@link UI} per la navigazione, se disponibile.
     */
    private void saveTargetAndNavigate(Optional<UI> ui) {
        String selectedTarget = targetRadioGroup.getValue();

        if (selectedTarget == null || selectedTarget.trim().isEmpty()) {
            Notification.show("Selezionare un tipo di destinatario.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            targetRadioGroup.setInvalid(true);
            return;
        }
        targetRadioGroup.setInvalid(false);

        if (MEDICI_ASSISTENTI.equals(selectedTarget) && mediciAssistentiYearRadio.isEmpty()) {
            ValidationError.showValidationError(mediciAssistentiYearRadio, "Selezionare l'anno per i Medici Assistenti.");
            return;
        }
        if (STUDENTI_MEDICINA.equals(selectedTarget) && studentiMedicinaYearRadio.isEmpty()) {
            ValidationError.showValidationError(studentiMedicinaYearRadio, "Selezionare l'anno per gli Studenti di Medicina.");
            return;
        }
        if (STUDENTI_INFERMIERISTICA.equals(selectedTarget) && studentiInfermieristicaYearRadio.isEmpty()) {
            ValidationError.showValidationError(studentiInfermieristicaYearRadio, "Selezionare l'anno per gli Studenti di Infermieristica.");
            return;
        }
        if (STUDENTI_ODONTOIATRIA.equals(selectedTarget) && studentiOdontoiatriaYearRadio.isEmpty()) {
            ValidationError.showValidationError(studentiOdontoiatriaYearRadio, "Selezionare l'anno per gli Studenti di Odontoiatria.");
            return;
        }
        if (ALTRO.equals(selectedTarget) && altroField.isEmpty()) {
            ValidationError.showValidationError(altroField, "Specificare il tipo di destinatario 'Altro'.");
            return;
        }
        if (MEDICI_SPECIALISTI.equals(selectedTarget) && mediciSpecialistiAltroChk.getValue() && mediciSpecialistiAltroField.isEmpty()) {
            ValidationError.showValidationError(mediciSpecialistiAltroField, "Specificare la specializzazione 'Altro'.");
            return;
        }

        String targetString = buildTargetStringManually(selectedTarget);
        logger.info("Stringa target costruita per scenario {}: {}", scenarioId, targetString);

        try {
            boolean success = scenarioService.updateScenarioTarget(scenarioId, targetString);

            if (success) {
                logger.info("Target aggiornato con successo per scenario {}", scenarioId);
                boolean isEditMode = "edit".equals(mode);

                Notification.show("Target salvato con successo", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                if (!isEditMode) {

                    ui.ifPresent(theUI -> theUI.navigate("descrizione/" + scenarioId));
                }
            } else {
                logger.error("Salvataggio target fallito per scenario {} tramite updateScenarioTarget.", scenarioId);
                Notification.show("Errore durante il salvataggio dei destinatari.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            logger.error("Eccezione durante il salvataggio del target per scenario {}: {}", scenarioId, e.getMessage(), e);
            Notification.show("Errore imprevisto durante il salvataggio.", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }


    /**
     * Costruisce la stringa completa che rappresenta il target dello scenario,
     * combinando la categoria principale con le opzioni dettagliate selezionate.
     *
     * @param selectedTarget La categoria principale del target (e.g., "Medici Assistenti").
     * @return Una {@link String} che rappresenta il target completo, formattata per essere salvata nel database.
     */
    private String buildTargetStringManually(String selectedTarget) {
        StringBuilder sb = new StringBuilder(selectedTarget);

        switch (selectedTarget) {
            case MEDICI_ASSISTENTI:
                if (mediciAssistentiYearRadio.getValue() != null) {
                    sb.append(" (").append(mediciAssistentiYearRadio.getValue()).append(" anno)");
                }
                break;
            case MEDICI_SPECIALISTI:
                List<String> selectedSpecs = getSelectedSpecializations(
                        mediciSpecialistiAnestesiaChk, mediciSpecialistiEmergenzaChk, mediciSpecialistiCureIntenseChk,
                        mediciSpecialistiChirurgiaChk, mediciSpecialistiOstetriciaChk, mediciSpecialistiPediatriaChk,
                        mediciSpecialistiInternaChk, mediciSpecialistiCardiologiaChk, mediciSpecialistiDisastriChk
                );

                if (mediciSpecialistiAltroChk.getValue() && !mediciSpecialistiAltroField.isEmpty()) {
                    selectedSpecs.add(ALTRO + ": " + mediciSpecialistiAltroField.getValue().trim());
                } else if (mediciSpecialistiAltroChk.getValue()) {
                    selectedSpecs.add(ALTRO);
                }

                if (!selectedSpecs.isEmpty()) {
                    sb.append(" (").append(String.join(", ", selectedSpecs)).append(")");
                }
                break;
            case STUDENTI_MEDICINA:
                if (studentiMedicinaYearRadio.getValue() != null) {
                    sb.append(" (").append(studentiMedicinaYearRadio.getValue()).append(" anno)");
                }
                break;
            case STUDENTI_INFERMIERISTICA:
                if (studentiInfermieristicaYearRadio.getValue() != null) {
                    sb.append(" (").append(studentiInfermieristicaYearRadio.getValue()).append(" anno)");
                }
                break;
            case INFERMIERI_SPECIALIZZATI:
                List<String> selectedInfSpecs = getSelectedSpecializations(
                        infSpecAnestesiaChk, infSpecCureIntenseChk, infSpecCureUrgentiChk
                );
                if (!selectedInfSpecs.isEmpty()) {
                    sb.append(" (").append(String.join(", ", selectedInfSpecs)).append(")");
                }
                break;
            case STUDENTI_ODONTOIATRIA:
                if (studentiOdontoiatriaYearRadio.getValue() != null) {
                    sb.append(" (").append(studentiOdontoiatriaYearRadio.getValue()).append(" anno)");
                }
                break;
            case ALTRO:
                if (!altroField.isEmpty()) {
                    sb.append(": ").append(altroField.getValue().trim());
                }
                break;
        }
        return sb.toString();
    }

    /**
     * Metodo helper per ottenere la lista delle specializzazioni selezionate da un array di checkbox.
     *
     * @param checkboxes Un array di {@link Checkbox} da cui estrarre i valori selezionati.
     * @return Una {@link List} di {@link String} contenente i testi dei checkbox selezionati.
     */
    private List<String> getSelectedSpecializations(Checkbox... checkboxes) {
        List<String> selectedSpecs = new ArrayList<>();
        for (Checkbox chk : checkboxes) {
            if (chk.getValue()) {
                selectedSpecs.add(chk.getLabel());
            }
        }
        return selectedSpecs;
    }

    /**
     * Implementazione del metodo {@link HasUrlParameter#setParameter(BeforeEvent, Object)}.
     * Questo metodo viene chiamato da Vaadin quando la vista viene navigata con un parametro URL.
     * Gestisce l'estrazione dell'ID dello scenario e della modalità (creazione o modifica) dall'URL.
     *
     * @param event     L'evento di navigazione.
     * @param parameter Il parametro URL, che può contenere l'ID dello scenario e opzionalmente la modalità "edit" (es. "123" o "123/edit").
     * @throws NotFoundException Se il parametro è nullo, vuoto, non un numero valido, non positivo, o se lo scenario non esiste.
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro URL mancante per la vista Target. ID scenario richiesto.");
                throw new NumberFormatException("Scenario ID è richiesto");
            }

            // Divide il parametro per ottenere l'ID e la modalità.
            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];

            this.scenarioId = Integer.parseInt(scenarioIdStr);
            // Verifica che l'ID sia valido e che lo scenario esista.
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Scenario ID non valido o non esistente: {}. Re indirizzamento a pagina di errore.", scenarioId);
                throw new NumberFormatException("Scenario ID non valido o non esistente.");
            }

            // Determina la modalità: "edit" se il secondo segmento è "edit", altrimenti "create".
            mode = parts.length > 1 && "edit".equalsIgnoreCase(parts[1]) ? "edit" : "create";

            logger.info("Vista Target caricata per lo scenario ID: {}, in modalità: {}.", this.scenarioId, mode);

            VerticalLayout mainLayout = getContent();

            // Nasconde l'header (AppHeader) in modalità "edit" per un layout più compatto,
            // presumendo che l'AppHeader sia contenuto in un HorizontalLayout nel mainLayout.
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst() // Trova il primo HorizontalLayout (presumibilmente l'header).
                    .ifPresent(header -> header.setVisible(!"edit".equals(mode)));

            // Nasconde la CreditsComponent nel footer in modalità "edit".
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second) // Trova l'ultimo HorizontalLayout (presumibilmente il footer).
                    .ifPresent(footer -> {
                        HorizontalLayout footerLayout = (HorizontalLayout) footer;
                        footerLayout.getChildren()
                                .filter(component -> component instanceof CreditsComponent)
                                .forEach(credits -> credits.setVisible(!"edit".equals(mode)));
                    });

            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT attiva: caricamento dati esistenti per scenario {}.", this.scenarioId);
                nextButton.setText("Salva Modifiche"); // Cambia testo del bottone.
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS); // Aggiunge stile di successo.
                nextButton.setIcon(new Icon(VaadinIcon.CHECK)); // Cambia icona.
            } else {
                logger.info("Modalità CREATE attiva per scenario {}.", this.scenarioId);
                // In modalità creazione, i campi sono già vuoti per default.
            }
            loadExistingTargets(); // Carica i dati esistenti dello scenario.
        } catch (NumberFormatException e) {
            logger.error("Errore di parsing o validazione dello Scenario ID '{}': {}", parameter, e.getMessage(), e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante. Assicurati che l'URL sia corretto.");
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'impostazione dei parametri per la vista Target: {}", e.getMessage(), e);
            event.rerouteToError(NotFoundException.class, "Si è verificato un errore durante il caricamento della pagina. Riprova.");
        }
    }

    /**
     * Carica i dati del target esistenti per lo scenario corrente dal database
     * e popola i componenti dell'interfaccia utente di conseguenza.
     */
    private void loadExistingTargets() {
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        // Se lo scenario o il suo target sono nulli/vuoti, resetta tutti i campi.
        if (scenario == null || scenario.getTarget() == null || scenario.getTarget().trim().isEmpty()) {
            logger.debug("Nessun target esistente o scenario nullo per ID: {}. Resettando tutti i campi.", scenarioId);
            resetAllFields();
            return;
        }

        String targetString = scenario.getTarget().trim();
        logger.debug("Caricamento target esistente per lo scenario {}: '{}'.", scenarioId, targetString);

        resetAllFields(); // Resetta tutti i campi prima di popolare, per evitare dati obsoleti.

        String mainTarget;
        // Estrae la parte principale del target (prima della parentesi o dei due punti).
        if (targetString.contains("(")) {
            mainTarget = targetString.substring(0, targetString.indexOf("(")).trim();
        } else if (targetString.contains(":")) {
            mainTarget = targetString.substring(0, targetString.indexOf(":")).trim();
        } else {
            mainTarget = targetString;
        }

        targetRadioGroup.setValue(mainTarget); // Imposta la selezione principale del target.

        try {
            // Estrae i dettagli tra parentesi, se presenti.
            boolean hasParentheses = targetString.contains("(") && targetString.contains(")");
            String detailsInParentheses = "";
            if (hasParentheses) {
                detailsInParentheses = targetString.substring(
                        targetString.indexOf("(") + 1,
                        targetString.lastIndexOf(")")
                );
            }

            // Popola i campi condizionali in base al target principale e ai dettagli.
            switch (mainTarget) {
                case MEDICI_ASSISTENTI:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.replace(" anno", "").trim();
                            mediciAssistentiYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Formato anno non valido per Medici Assistenti: '{}'.", targetString);
                        }
                    }
                    break;
                case MEDICI_SPECIALISTI:
                    if (hasParentheses) {
                        // Popola i checkbox per le specializzazioni.
                        if (detailsInParentheses.contains(SPEC_ANESTESIA)) mediciSpecialistiAnestesiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_EMERGENZA)) mediciSpecialistiEmergenzaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CURE_INTENSE))
                            mediciSpecialistiCureIntenseChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CHIRURGIA)) mediciSpecialistiChirurgiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_OSTETRICIA))
                            mediciSpecialistiOstetriciaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_PEDIATRIA)) mediciSpecialistiPediatriaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_INTERNA)) mediciSpecialistiInternaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CARDIOLOGIA))
                            mediciSpecialistiCardiologiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_DISASTRI)) mediciSpecialistiDisastriChk.setValue(true);

                        // Gestisce il campo "Altro".
                        if (detailsInParentheses.contains(ALTRO)) {
                            mediciSpecialistiAltroChk.setValue(true);
                            String marker = ALTRO + ":";
                            int startIdx = detailsInParentheses.indexOf(marker);
                            if (startIdx != -1) {
                                // Estrae il testo dopo "Altro:".
                                int endIdx = detailsInParentheses.indexOf(",", startIdx);
                                String altroText = (endIdx == -1)
                                        ? detailsInParentheses.substring(startIdx + marker.length()).trim()
                                        : detailsInParentheses.substring(startIdx + marker.length(), endIdx).trim();
                                mediciSpecialistiAltroField.setValue(altroText);
                                mediciSpecialistiAltroField.setEnabled(true);
                            }
                        }
                    }
                    break;
                case STUDENTI_MEDICINA:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.replace(" anno", "").trim();
                            studentiMedicinaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Formato anno non valido per Studenti di Medicina: '{}'.", targetString);
                        }
                    }
                    break;
                case STUDENTI_INFERMIERISTICA:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.replace(" anno", "").trim();
                            studentiInfermieristicaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Formato anno non valido per Studenti di Infermieristica: '{}'.", targetString);
                        }
                    }
                    break;
                case INFERMIERI_SPECIALIZZATI:
                    if (hasParentheses) {
                        // Popola i checkbox per le specializzazioni infermieristiche.
                        if (detailsInParentheses.contains(SPEC_ANESTESIA)) infSpecAnestesiaChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_CURE_INTENSE)) infSpecCureIntenseChk.setValue(true);
                        if (detailsInParentheses.contains(SPEC_INF_CURE_URGENTI)) infSpecCureUrgentiChk.setValue(true);
                    }
                    break;
                case STUDENTI_ODONTOIATRIA:
                    if (hasParentheses) {
                        try {
                            String yearStr = detailsInParentheses.replace(" anno", "").trim();
                            studentiOdontoiatriaYearRadio.setValue(Integer.parseInt(yearStr));
                        } catch (NumberFormatException e) {
                            logger.warn("Formato anno non valido per Studenti di Odontoiatria: '{}'.", targetString);
                        }
                    }
                    break;
                case ALTRO:
                    if (targetString.contains(":")) {
                        // Estrae il testo dopo "Altro:".
                        altroField.setValue(targetString.substring(targetString.indexOf(":") + 1).trim());
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Errore durante il parsing dei dettagli della stringa target '{}': {}. Resettando i campi.",
                    targetString, e.getMessage(), e);
            resetAllFields(); // Resetta i campi in caso di errore di parsing.
            targetRadioGroup.clear(); // Pulisce anche la selezione del target principale.
            return;
        }

        // Aggiorna la visibilità dei layout e lo stato di obbligatorietà dei campi.
        updateConditionalLayoutsVisibility(mainTarget);
        updateFieldsRequiredStatus(mainTarget);
        logger.info("Target esistente caricato con successo per lo scenario ID {}.", scenarioId);
    }

    /**
     * Resetta tutti i campi di input della vista ai loro valori iniziali (vuoti o non selezionati).
     * Questo è utile prima di caricare nuovi dati o quando l'utente annulla una selezione.
     */
    private void resetAllFields() {
        // Resetta i RadioButtonGroup per gli anni.
        mediciAssistentiYearRadio.clear();
        studentiMedicinaYearRadio.clear();
        studentiInfermieristicaYearRadio.clear();
        studentiOdontoiatriaYearRadio.clear();
        // Resetta il campo di testo "Altro".
        altroField.clear();

        // Resetta tutti i checkbox delle specializzazioni.
        Stream.of(mediciSpecialistiAnestesiaChk, mediciSpecialistiEmergenzaChk, mediciSpecialistiCureIntenseChk,
                mediciSpecialistiChirurgiaChk, mediciSpecialistiOstetriciaChk, mediciSpecialistiPediatriaChk,
                mediciSpecialistiInternaChk, mediciSpecialistiCardiologiaChk, mediciSpecialistiDisastriChk,
                mediciSpecialistiAltroChk).forEach(chk -> chk.setValue(false));
        mediciSpecialistiAltroField.clear();
        mediciSpecialistiAltroField.setEnabled(false); // Disabilita il campo "Altro" per medici specialisti.

        Stream.of(infSpecAnestesiaChk, infSpecCureIntenseChk, infSpecCureUrgentiChk)
                .forEach(chk -> chk.setValue(false));

        hideAllConditionalLayouts(); // Nasconde tutti i layout condizionali.
        updateFieldsRequiredStatus(null); // Resetta lo stato di obbligatorietà (nessun target selezionato).

        // Resetta lo stato di invalidità per tutti i campi.
        targetRadioGroup.setInvalid(false);
        mediciAssistentiYearRadio.setInvalid(false);
        studentiMedicinaYearRadio.setInvalid(false);
        studentiInfermieristicaYearRadio.setInvalid(false);
        studentiOdontoiatriaYearRadio.setInvalid(false);
        altroField.setInvalid(false);
        mediciSpecialistiAltroField.setInvalid(false);
        logger.debug("Tutti i campi della vista Target sono stati resettati.");
    }
}