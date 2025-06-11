package it.uniupo.simnova.views.ui.helper.support;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static it.uniupo.simnova.views.constant.ExamConst.ALLINSTREXAMS;
import static it.uniupo.simnova.views.constant.ExamConst.ALLLABSEXAMS;

/**
 * Rappresenta una singola riga di un form per l'inserimento di esami e referti.
 * Gestisce la selezione di esami predefiniti o personalizzati,
 * l'upload di file multimediali e l'inserimento di referti testuali.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class FormRow {
    /**
     * Logger per il tracciamento degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(FormRow.class);

    /**
     * Numero identificativo della riga del form.
     * Utilizzato per distinguere tra diverse righe di esami/referti.
     */
    public final int rowNumber;
    /**
     * Paragrafo che funge da titolo per questa riga del form.
     */
    public final Paragraph rowTitle;
    /**
     * Layout del form che contiene i campi di input per questa riga.
     * Utilizza {@link FormLayout} per una disposizione responsiva e ordinata.
     */
    public final FormLayout rowLayout;

    /**
     * Pulsante per selezionare un esame da un elenco predefinito o per inserire un esame personalizzato.
     */
    public final Button selectExamButton = new Button("Seleziona", new Icon(VaadinIcon.SEARCH));
    /**
     * Lista di esami di laboratorio e strumentali predefiniti.
     * Questi vengono mostrati in un dialog per la selezione dell'esame.
     */
    public final Dialog examDialog = new Dialog();
    /**
     * Campo di testo per mostrare l'esame selezionato da un elenco predefinito.
     * Diventa cliccabile per aprire il dialog di selezione esame.
     */
    public final TextField selectedExamField = new TextField("Tipo Esame");
    /**
     * Campo di testo per inserire manualmente il nome di un esame personalizzato.
     * Visibile solo se l'utente sceglie di inserire un esame manualmente.
     */
    public final TextField customExamField = new TextField("Esame Personalizzato");
    /**
     * Gruppo di radio button per selezionare il tipo di esame da inserire.
     * Permette di scegliere tra "Seleziona da elenco" o "Inserisci manualmente".
     */
    public final RadioButtonGroup<String> examTypeGroup = new RadioButtonGroup<>();

    /**
     * Componente di upload per caricare nuovi file multimediali associati all'esame.
     * Utilizza {@link MemoryBuffer} per gestire i file caricati in memoria.
     */
    public final Upload upload;
    /**
     * Campo di testo per inserire un referto testuale associato all'esame.
     * Permette di aggiungere commenti o descrizioni dell'esame.
     */
    public final TextField reportField;
    /**
     * Gruppo di radio button per selezionare la sorgente del media associato all'esame.
     * Permette di scegliere tra "Carica nuovo file" o "Seleziona da esistenti".
     */
    public final RadioButtonGroup<String> mediaSourceGroup = new RadioButtonGroup<>();
    /**
     * Pulsante per selezionare un file multimediale esistente da un elenco.
     * Apre un dialog che mostra i file disponibili.
     */
    public final Button selectMediaButton = new Button("Seleziona da esistenti", new Icon(VaadinIcon.FOLDER_OPEN));
    /**
     * Dialog per la selezione di file multimediali esistenti.
     * Mostra un elenco di file già caricati e permette di selezionarne uno.
     */
    public final Dialog mediaDialog = new Dialog();
    /**
     * Campo di testo per mostrare il nome del file multimediale selezionato da un elenco esistente.
     * Diventa cliccabile per aprire il dialog di selezione media.
     */
    public final TextField selectedMediaField = new TextField("Media Selezionato");
    /**
     * Servizio per la gestione dei file multimediali.
     * Utilizzato per leggere, scrivere e ottenere i file disponibili.
     */
    private final FileStorageService fileStorageService;
    /**
     * Nome del media esistente selezionato dall'utente.
     * Viene impostato quando l'utente sceglie un file esistente dal dialog.
     */
    public String selectedExistingMedia = null;

    /**
     * Costruttore per una riga del form di esami/referti.
     *
     * @param rowNumber          Il numero identificativo della riga.
     * @param fileStorageService Il servizio per la gestione dei file.
     */
    public FormRow(int rowNumber, FileStorageService fileStorageService) {
        this.rowNumber = rowNumber;
        this.fileStorageService = fileStorageService;

        this.rowTitle = new Paragraph("Esame/Referto #" + rowNumber);
        rowTitle.addClassName(LumoUtility.FontWeight.BOLD);
        rowTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

        // Configurazione del gruppo di radio button per il tipo di esame (predefinito/personalizzato)
        examTypeGroup.setLabel("Tipo di inserimento");
        examTypeGroup.setItems("Seleziona da elenco", "Inserisci manualmente");
        examTypeGroup.setValue("Seleziona da elenco"); // Valore predefinito
        examTypeGroup.addValueChangeListener(e -> updateExamFieldVisibility()); // Listener per aggiornare visibilità
        examTypeGroup.getStyle()
                .set("margin-top", "0")
                .set("margin-bottom", "var(--lumo-space-s)");

        // Campo per l'esame selezionato da elenco
        selectedExamField.setReadOnly(true);
        selectedExamField.setWidthFull();
        selectedExamField.setPrefixComponent(new Icon(VaadinIcon.FILE_TEXT));
        // Al click, apre il dialog di selezione esame se l'opzione "Seleziona da elenco" è attiva
        selectedExamField.getElement().addEventListener("click", e -> {
            if ("Seleziona da elenco".equals(examTypeGroup.getValue())) {
                selectExamButton.click();
            }
        });

        // Campo per l'esame personalizzato (visibile solo se selezionato "Inserisci manualmente")
        customExamField.setWidthFull();
        customExamField.setVisible(false);
        customExamField.setPlaceholder("Inserisci il nome dell'esame");
        customExamField.setPrefixComponent(new Icon(VaadinIcon.EDIT));

        // Pulsante per aprire il dialog di selezione esame
        selectExamButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectExamButton.addClassName(LumoUtility.Margin.Bottom.NONE);
        selectExamButton.setWidth("auto");

        // Layout per il campo esame selezionato e il pulsante di selezione
        HorizontalLayout selectionLayout = new HorizontalLayout(selectedExamField, selectExamButton);
        selectionLayout.setWidthFull();
        selectionLayout.setFlexGrow(1, selectedExamField);
        selectionLayout.setAlignItems(FlexComponent.Alignment.END);
        selectionLayout.setSpacing(true);

        // Dialog per la selezione del tipo di esame
        examDialog.setHeaderTitle("Seleziona Tipo Esame");
        examDialog.setWidth("600px");
        examDialog.setHeight("70vh");
        examDialog.setDraggable(true);
        examDialog.setResizable(true);

        // Configurazione del gruppo di radio button per la sorgente del media (upload/esistente)
        mediaSourceGroup.setLabel("Sorgente del media");
        mediaSourceGroup.setItems("Carica nuovo file", "Seleziona da esistenti");
        mediaSourceGroup.setValue("Carica nuovo file"); // Valore predefinito
        mediaSourceGroup.addValueChangeListener(e -> updateMediaFieldVisibility()); // Listener per aggiornare visibilità

        // Campo per mostrare il media selezionato (da esistenti)
        selectedMediaField.setReadOnly(true);
        selectedMediaField.setWidthFull();
        selectedMediaField.setPrefixComponent(new Icon(VaadinIcon.FILE));
        selectedMediaField.setVisible(false);

        // Pulsante per selezionare media esistente
        selectMediaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectMediaButton.setWidth("auto");
        selectMediaButton.setVisible(false);

        // Layout per il campo media selezionato e il pulsante di selezione media esistente
        HorizontalLayout mediaSelectionLayout = new HorizontalLayout(selectedMediaField, selectMediaButton);
        mediaSelectionLayout.setWidthFull();
        mediaSelectionLayout.setFlexGrow(1, selectedMediaField);
        mediaSelectionLayout.setAlignItems(FlexComponent.Alignment.END);
        mediaSelectionLayout.setSpacing(true);

        // Dialog per la selezione di media esistenti
        mediaDialog.setHeaderTitle("Seleziona Media");
        mediaDialog.setWidth("600px");
        mediaDialog.setDraggable(true);
        mediaDialog.setResizable(true);

        configureMediaDialog(); // Configura il contenuto del dialog media

        selectMediaButton.addClickListener(e -> mediaDialog.open()); // Apre il dialog media al click

        // Componenti del dialog di selezione esame
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca esame...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.addClassName(LumoUtility.Margin.Bottom.SMALL);

        Tabs categoryTabs = new Tabs();
        Tab labTab = new Tab("Laboratorio");
        Tab instrTab = new Tab("Strumentali");
        categoryTabs.add(labTab, instrTab);
        categoryTabs.setWidthFull();
        categoryTabs.getStyle()
                .set("margin-bottom", "0")
                .set("box-shadow", "0 -1px 0 0 var(--lumo-contrast-10pct) inset");

        VerticalLayout labContent = createLabExamContent(ALLLABSEXAMS);
        VerticalLayout instrContent = createInstrumentalExamContent(ALLINSTREXAMS);

        Div pages = new Div(labContent, instrContent);
        pages.setWidthFull();
        pages.getStyle().set("overflow-y", "auto");
        pages.getStyle().set("max-height", "calc(70vh - 150px)");

        // Listener per il campo di ricerca degli esami
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue().toLowerCase();
            VerticalLayout filteredLabContent = createLabExamContent(
                    ALLLABSEXAMS.stream()
                            .filter(exam -> exam.toLowerCase().contains(searchTerm))
                            .collect(Collectors.toList())
            );
            VerticalLayout filteredInstrContent = createInstrumentalExamContent(
                    ALLINSTREXAMS.stream()
                            .filter(exam -> exam.toLowerCase().contains(searchTerm))
                            .collect(Collectors.toList())
            );

            pages.removeAll();
            if (categoryTabs.getSelectedTab() == labTab) {
                pages.add(filteredLabContent);
            } else {
                pages.add(filteredInstrContent);
            }
        });

        // Listener per il cambio di tab (Laboratorio/Strumentali)
        categoryTabs.addSelectedChangeListener(event -> {
            pages.removeAll();
            if (event.getSelectedTab() == labTab) {
                pages.add(createLabExamContent(ALLLABSEXAMS));
            } else {
                pages.add(createInstrumentalExamContent(ALLINSTREXAMS));
            }
        });

        // Pulsante di chiusura del dialog esami
        Button closeButton = new Button("Chiudi", e -> examDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        examDialog.getFooter().add(closeButton);

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.add(searchField, categoryTabs, pages);
        examDialog.add(dialogContent);

        selectExamButton.addClickListener(e -> {
            if ("Seleziona da elenco".equals(examTypeGroup.getValue())) {
                examDialog.open();
            }
        });

        // Campo di upload per nuovi file media
        MemoryBuffer buffer = new MemoryBuffer();
        this.upload = new Upload(buffer);
        upload.setDropAllowed(true);
        upload.setWidthFull();
        upload.setAcceptedFileTypes(".pdf", ".jpg", "jpeg", ".png", ".gif", ".mp4", ".mp3", ".webp");
        upload.setMaxFiles(1);
        upload.setUploadButton(new Button("Carica File", new Icon(VaadinIcon.UPLOAD)));
        upload.setDropLabel(new Div(new Text("Trascina file qui o clicca per selezionare")));

        // Campo di testo per il referto
        this.reportField = new TextField("Referto Testuale");
        reportField.setWidthFull();
        reportField.setPrefixComponent(new Icon(VaadinIcon.COMMENT));
        reportField.setPlaceholder("Inserisci il referto dell'esame...");

        // Layout principale della riga del form
        this.rowLayout = new FormLayout();
        rowLayout.setWidthFull();
        // Aggiunge i componenti al FormLayout con span di colonne
        rowLayout.add(examTypeGroup, 2);
        rowLayout.add(selectionLayout, 2);
        rowLayout.add(customExamField, 2);
        rowLayout.add(mediaSourceGroup, 2);
        rowLayout.add(upload, 2);
        rowLayout.add(mediaSelectionLayout, 2);
        rowLayout.add(reportField, 2);
        // Configurazione responsive del layout
        rowLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2),
                new FormLayout.ResponsiveStep("900px", 3)
        );

        // Imposta margine inferiore per tutti i componenti del layout della riga
        rowLayout.getChildren().forEach(component ->
                component.getElement().getStyle().set("margin-bottom", "var(--lumo-space-s)"));

        updateMediaFieldVisibility(); // Aggiorna la visibilità iniziale dei campi media
    }

    /**
     * Aggiorna la visibilità dei campi di selezione esame (predefinito o personalizzato)
     * in base al valore del RadioButtonGroup {@code examTypeGroup}.
     */
    public void updateExamFieldVisibility() {
        boolean isCustom = "Inserisci manualmente".equals(examTypeGroup.getValue());
        selectedExamField.setVisible(!isCustom);
        selectExamButton.setVisible(!isCustom);
        customExamField.setVisible(isCustom);

        if (isCustom) {
            selectedExamField.clear(); // Pulisce il campo se si passa all'inserimento manuale
        } else {
            customExamField.clear(); // Pulisce il campo se si passa alla selezione da elenco
        }
    }

    /**
     * Aggiorna la visibilità dei campi relativi alla sorgente del media (upload o selezione da esistenti)
     * in base al valore del RadioButtonGroup {@code mediaSourceGroup}.
     */
    public void updateMediaFieldVisibility() {
        boolean isNewUpload = "Carica nuovo file".equals(mediaSourceGroup.getValue());
        upload.setVisible(isNewUpload);
        selectedMediaField.setVisible(!isNewUpload);
        selectMediaButton.setVisible(!isNewUpload);

        if (isNewUpload) {
            selectedMediaField.clear(); // Pulisce il campo del media selezionato se si carica un nuovo file
            selectedExistingMedia = null;
        } else {
            // Resetta il campo di upload se si passa alla selezione da esistenti
            upload.getElement().executeJs("this.files = []");
        }
    }

    /**
     * Configura il dialog per la selezione di file multimediali esistenti.
     * Include un campo di ricerca e una griglia per visualizzare le anteprime dei media.
     */
    public void configureMediaDialog() {
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca media...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.addClassName(LumoUtility.Margin.Bottom.SMALL);

        Div mediaContent = new Div();
        mediaContent.setWidthFull();
        mediaContent.getStyle()
                .set("overflow-y", "auto")
                .set("padding", "var(--lumo-space-m)")
                .set("max-height", "400px"); // Altezza massima del contenitore dei media

        Button closeButton = new Button("Chiudi", e -> mediaDialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        mediaDialog.getFooter().add(closeButton);

        // Carica i media disponibili all'apertura del dialog
        loadAvailableMedia(mediaContent);

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setPadding(false);
        dialogContent.setSpacing(false);
        dialogContent.add(searchField, mediaContent);
        dialogContent.setSizeFull();

        mediaDialog.setWidth("700px");
        mediaDialog.setHeight("600px");
        mediaDialog.add(dialogContent);

        // Listener per la ricerca dinamica dei media nel dialog
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue().toLowerCase();
            loadAvailableMedia(mediaContent, searchTerm);
        });
    }

    /**
     * Carica e visualizza i media disponibili nel contenitore specificato.
     * Non filtra i risultati.
     *
     * @param container Il Div in cui caricare i componenti dei media.
     */
    public void loadAvailableMedia(Div container) {
        loadAvailableMedia(container, null);
    }

    /**
     * Carica e visualizza i media disponibili nel contenitore, applicando un filtro di ricerca.
     *
     * @param container  Il Div in cui caricare i componenti dei media.
     * @param searchTerm Il termine di ricerca per filtrare i nomi dei media (può essere null per nessun filtro).
     */
    public void loadAvailableMedia(Div container, String searchTerm) {
        container.removeAll(); // Pulisce il contenuto precedente

        List<String> availableMedia = getAvailableMedia(); // Ottiene tutti i media disponibili

        // Applica il filtro di ricerca se un termine è fornito
        if (searchTerm != null && !searchTerm.isEmpty()) {
            availableMedia = availableMedia.stream()
                    .filter(media -> media.toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
        }

        if (availableMedia.isEmpty()) {
            Paragraph noResults = new Paragraph("Nessun media trovato");
            noResults.addClassName(LumoUtility.TextColor.SECONDARY);
            container.add(noResults);
            return;
        }

        // Griglia per visualizzare gli elementi multimediali
        Div mediaGrid = new Div();
        mediaGrid.setWidthFull();
        mediaGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(150px, 1fr))") // Griglia responsiva
                .set("grid-gap", "var(--lumo-space-m)")
                .set("padding", "var(--lumo-space-s)");

        for (String media : availableMedia) {
            VerticalLayout mediaItem = new VerticalLayout();
            mediaItem.setPadding(false);
            mediaItem.setSpacing(false);
            mediaItem.setWidth("100%");
            mediaItem.getStyle()
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("cursor", "pointer")
                    .set("transition", "all 0.2s ease-in-out")
                    .set("overflow", "hidden");

            // Anteprima del media (immagine o icona generica)
            Component mediaPreview;
            String mediaLower = media.toLowerCase();

            if (mediaLower.endsWith(".jpg") || mediaLower.endsWith(".jpeg") ||
                    mediaLower.endsWith(".png") || mediaLower.endsWith(".gif") ||
                    mediaLower.endsWith(".webp")) {
                Image image = getImage(media); // Ottiene l'immagine come StreamResource
                image.getStyle()
                        .set("object-fit", "contain")
                        .set("background-color", "var(--lumo-contrast-5pct)");
                mediaPreview = image;
            } else {
                Icon mediaIcon = getMediaIcon(media); // Ottiene l'icona in base al tipo di file
                mediaIcon.setSize("48px");
                mediaIcon.getStyle().set("margin", "var(--lumo-space-m) auto");

                Div iconContainer = new Div(mediaIcon);
                iconContainer.setWidth("100%");
                iconContainer.setHeight("100px");
                iconContainer.getStyle()
                        .set("display", "flex")
                        .set("align-items", "center")
                        .set("justify-content", "center")
                        .set("background-color", "var(--lumo-contrast-5pct)");
                mediaPreview = iconContainer;
            }

            // Nome del media
            Paragraph mediaName = new Paragraph(media);
            mediaName.getStyle()
                    .set("margin", "0")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("white-space", "nowrap")
                    .set("overflow", "hidden")
                    .set("text-overflow", "ellipsis")
                    .set("text-align", "center")
                    .set("background-color", "var(--lumo-base-color)")
                    .set("width", "100%");
            mediaName.getElement().setAttribute("title", media); // Tooltip con il nome completo

            mediaItem.add(mediaPreview, mediaName);

            // Listener per la selezione del media
            mediaItem.addClickListener(e -> {
                selectedExistingMedia = media;
                selectedMediaField.setValue(media);
                mediaDialog.close();
            });

            // Effetti visivi al passaggio del mouse
            mediaItem.getElement().addEventListener("mouseover", e ->
                    mediaItem.getStyle().set("box-shadow", "0 0 5px var(--lumo-primary-color-50pct)"));
            mediaItem.getElement().addEventListener("mouseout", e ->
                    mediaItem.getStyle().set("box-shadow", "none"));

            mediaGrid.add(mediaItem);
        }

        container.add(mediaGrid);
    }

    /**
     * Crea una {@link StreamResource} per l'immagine specificata e la incapsula in un componente {@link Image}.
     *
     * @param media Il nome del file media (es. "image.jpg").
     * @return Un componente {@link Image} configurato per visualizzare il media.
     */
    public Image getImage(String media) {
        StreamResource resource = new StreamResource(media, () -> {
            try {
                return fileStorageService.readFile(media); // Legge il file dal servizio di storage
            } catch (Exception e) {
                logger.error("Errore nel caricamento dell'anteprima per {}", media, e);
                return InputStream.nullInputStream(); // Restituisce un InputStream vuoto in caso di errore
            }
        });

        Image image = new Image(resource, "Anteprima");
        image.setWidth("100%");
        image.setHeight("100px"); // Altezza fissa per l'anteprima
        return image;
    }

    /**
     * Restituisce un'icona {@link VaadinIcon} appropriata in base all'estensione del nome del file.
     *
     * @param filename Il nome del file per cui determinare l'icona.
     * @return L'icona corrispondente al tipo di file.
     */
    public Icon getMediaIcon(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                filename.endsWith(".png") || filename.endsWith(".gif") ||
                filename.endsWith(".webp")) {
            return new Icon(VaadinIcon.PICTURE);
        } else if (filename.endsWith(".pdf")) {
            return new Icon(VaadinIcon.FILE);
        } else if (filename.endsWith(".mp4") || filename.endsWith(".webm") ||
                filename.endsWith(".mov")) {
            return new Icon(VaadinIcon.FILM);
        } else if (filename.endsWith(".mp3") || filename.endsWith(".wav") ||
                filename.endsWith(".ogg")) {
            return new Icon(VaadinIcon.HEADPHONES);
        } else {
            return new Icon(VaadinIcon.FILE); // Icona generica per tipi non riconosciuti
        }
    }

    /**
     * Ottiene la lista dei nomi di tutti i file multimediali disponibili dal servizio di storage.
     *
     * @return Una lista di stringhe contenente i nomi dei file.
     */
    public List<String> getAvailableMedia() {
        return fileStorageService.getAllFiles();
    }

    /**
     * Crea un layout verticale contenente pulsanti per la selezione degli esami di laboratorio.
     *
     * @param exams La lista di stringhe degli esami di laboratorio.
     * @return Un {@link VerticalLayout} con i pulsanti degli esami.
     */
    public VerticalLayout createLabExamContent(List<String> exams) {
        return createExamContent(exams);
    }

    /**
     * Crea un layout verticale contenente pulsanti per la selezione degli esami strumentali.
     *
     * @param exams La lista di stringhe degli esami strumentali.
     * @return Un {@link VerticalLayout} con i pulsanti degli esami.
     */
    public VerticalLayout createInstrumentalExamContent(List<String> exams) {
        return createExamContent(exams);
    }

    /**
     * Metodo generico per creare un layout verticale con pulsanti di selezione esame.
     *
     * @param exams La lista di stringhe degli esami da visualizzare.
     * @return Un {@link VerticalLayout} configurato.
     */
    public VerticalLayout createExamContent(List<String> exams) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setWidthFull();

        if (exams.isEmpty()) {
            Paragraph noResults = new Paragraph("Nessun risultato trovato");
            noResults.addClassName(LumoUtility.TextColor.SECONDARY);
            noResults.getStyle().set("padding", "var(--lumo-space-m)");
            layout.add(noResults);
        } else {
            for (String exam : exams) {
                Button examButton = createExamButton(exam);
                layout.add(examButton);
            }
        }
        return layout;
    }

    /**
     * Crea un pulsante configurato per la selezione di un esame specifico.
     * Il click sul pulsante imposta il valore nel campo {@code selectedExamField}
     * e chiude il dialog dell'esame.
     *
     * @param examName Il nome dell'esame da associare al pulsante.
     * @return Un {@link Button} preconfigurato.
     */
    public Button createExamButton(String examName) {
        Button button = new Button(examName);
        button.setWidthFull();
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        button.getStyle()
                .set("text-align", "left")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("justify-content", "flex-start")
                .set("border-radius", "var(--lumo-border-radius-m)");

        button.addClickListener(e -> {
            selectedExamField.setValue(examName);
            examDialog.close();
        });
        return button;
    }

    /**
     * Restituisce il valore dell'esame selezionato,
     * prendendolo dal campo personalizzato se l'opzione "Inserisci manualmente" è attiva,
     * altrimenti dal campo di selezione predefinito.
     *
     * @return Il nome dell'esame selezionato.
     */
    public String getSelectedExam() {
        return "Inserisci manualmente".equals(examTypeGroup.getValue())
                ? customExamField.getValue()
                : selectedExamField.getValue();
    }

    /**
     * Restituisce il nome del file multimediale selezionato dalla lista dei media esistenti.
     *
     * @return Il nome del file media selezionato, o {@code null} se non è stato selezionato nulla
     * o se la sorgente è impostata su "Carica nuovo file".
     */
    public String getSelectedMedia() {
        return selectedExistingMedia;
    }

    /**
     * Restituisce il numero di questa riga del form.
     *
     * @return Il numero della riga.
     */
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * Restituisce il componente {@link Paragraph} che funge da titolo per questa riga.
     *
     * @return Il titolo della riga.
     */
    public Paragraph getRowTitle() {
        return rowTitle;
    }

    /**
     * Restituisce il componente {@link FormLayout} che contiene i campi di input di questa riga.
     *
     * @return Il layout della riga.
     */
    public FormLayout getRowLayout() {
        return rowLayout;
    }

    /**
     * Restituisce il componente {@link Upload} per caricare nuovi file.
     *
     * @return Il componente di upload.
     */
    public Upload getUpload() {
        return upload;
    }

    /**
     * Restituisce il componente {@link TextField} per l'inserimento del referto testuale.
     *
     * @return Il campo di testo per il referto.
     */
    public TextField getReportField() {
        return reportField;
    }
}