package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.service.scenario.operations.ScenarioImportService;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Classe di utility per la gestione di dialog comuni nell'interfaccia utente.
 * Fornisce metodi statici per mostrare dialog specifici, come quello per l'upload di file ZIP.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class DialogSupport {

    /**
     * Costruttore privato per prevenire l'istanza della classe.
     * Questa classe contiene solo metodi statici e non dovrebbe essere istanziata.
     */
    public DialogSupport() {
        // Costruttore vuoto per prevenire l'istanza della classe
    }

    /**
     * Mostra un dialog modale per il caricamento di un file ZIP.
     * Questo dialog è utilizzato per importare nuovi scenari nell'applicazione.
     * L'operazione di importazione avviene in background.
     *
     * @param detached              Flag atomico che indica se la UI è stata distaccata, per prevenire operazioni su componenti non più attaccati.
     * @param executorService       Servizio esecutore per eseguire l'importazione in un thread separato.
     * @param scenarioImportService Servizio per la logica di importazione dello scenario da file ZIP.
     * @param onSuccess             Runnable da eseguire in caso di successo dell'importazione.
     */
    public static void showZipUploadDialog(AtomicBoolean detached, ExecutorService executorService, ScenarioImportService scenarioImportService, Runnable onSuccess) {
        if (detached.get()) {
            return; // Non mostrare il dialog se la UI è già distaccata
        }

        Dialog uploadDialog = new Dialog();
        uploadDialog.setCloseOnEsc(true);
        uploadDialog.setCloseOnOutsideClick(true);
        uploadDialog.setWidth("550px");
        uploadDialog.setHeaderTitle("Importa Nuovo Scenario da ZIP");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        H4 title = new H4("Carica un file ZIP per importare un nuovo scenario");
        title.getStyle().set("margin-top", "0");

        Paragraph description = new Paragraph();
        description.add("Seleziona un file ZIP (.zip) da caricare. Il file ZIP deve essere nominato nel formato ");

        Span boldFileName = new Span("Execution_scenario_nome.zip");
        boldFileName.getStyle().set("font-weight", "bold");
        description.add(boldFileName);
        description.add(", deve contenere il file ");
        Span italicScenario = new Span("scenario.json");
        italicScenario.getStyle().set("font-style", "italic");
        description.add(italicScenario);
        description.add(" alla radice e la cartella ");
        Span italicEsami = new Span("esami/");
        italicEsami.getStyle().set("font-style", "italic");
        description.add(italicEsami);
        description.add(" con eventuali file multimediali associati.");
        description.addClassName(LumoUtility.FontSize.SMALL);

        // Configurazione del componente Upload
        MemoryBuffer buffer = new MemoryBuffer(); // Buffer per memorizzare il file caricato in memoria
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".zip"); // Accetta solo file ZIP
        upload.setMaxFiles(1); // Permette il caricamento di un solo file
        upload.setDropLabel(new Span("Trascina qui il file ZIP o clicca per cercare"));
        upload.setWidthFull();

        dialogLayout.add(title, description, upload);
        uploadDialog.add(dialogLayout);

        Button cancelButton = new Button("Annulla", e -> uploadDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        uploadDialog.getFooter().add(cancelButton);

        uploadDialog.open();

        // Listener per l'evento di caricamento completato con successo
        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] zipBytes = inputStream.readAllBytes();
                String fileName = event.getFileName();
                uploadDialog.close(); // Chiude il dialog di upload

                UI ui = UI.getCurrent();
                // Verifica che la UI sia ancora disponibile e non distaccata
                if (ui == null || detached.get()) {
                    return;
                }

                // Mostra una notifica di caricamento in corso
                Notification loadingNotification = new Notification("Importazione scenario in corso...", 0, Notification.Position.MIDDLE);
                loadingNotification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
                loadingNotification.open();

                // Esegue l'importazione in un thread separato per non bloccare la UI
                executorService.submit(() -> {
                    try {
                        boolean imported = scenarioImportService.importScenarioFromZip(zipBytes, fileName);
                        // Accede alla UI per aggiornare i componenti una volta completata l'operazione
                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close(); // Chiude la notifica di caricamento
                                if (imported) {
                                    Notification.show("Scenario importato con successo!", 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                    if (onSuccess != null) {
                                        onSuccess.run(); // Esegue il callback di successo
                                    }
                                } else {
                                    Notification.show("Errore durante l'importazione dello scenario.", 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        // Gestisce eccezioni durante l'importazione
                        if (!detached.get() && !ui.isClosing()) {
                            ui.access(() -> {
                                loadingNotification.close();
                                Notification.show("Errore importazione: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                            });
                        }
                    }
                });
                UI.getCurrent().getPage().reload();
            } catch (IOException ex) {
                // Gestisce errori di lettura del file
                Notification.show("Errore lettura file: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                uploadDialog.close();
            }
        });

        // Listener per l'evento di caricamento fallito
        upload.addFailedListener(event -> Notification.show("Caricamento fallito: " + event.getReason().getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));
    }
}