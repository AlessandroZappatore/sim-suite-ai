package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static it.uniupo.simnova.service.export.PdfExportService.checkForNewPage;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.MARGIN;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.renderHtmlWithFormatting;

/**
 * Questa classe di utilità si occupa della generazione della sezione "Sceneggiatura"
 * all'interno di un documento PDF. Esporta il testo della sceneggiatura associata a uno scenario,
 * se presente e richiesto, applicando la formattazione HTML.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioSceneggiatura {
    /**
     * Logger per registrare le operazioni e gli errori durante la creazione della sezione "Sceneggiatura".
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioSceneggiatura.class);

    /**
     * Costruttore privato per evitare l'istanza della classe, dato che è una classe di utilità.
     * Non deve essere istanziata, ma solo utilizzata attraverso il metodo statico {@link #createSceneggiaturaSection(Scenario, boolean, PatientSimulatedScenarioService)}.
     */
    private ScenarioSceneggiatura() {
        // Costruttore privato per evitare l'istanza della classe, dato che è una classe di utilità.
    }

    /**
     * Crea la sezione "Sceneggiatura" nel documento PDF.
     * La sezione viene aggiunta solo se il flag {@code scen} è impostato a <code>true</code>
     * e se la sceneggiatura per lo scenario specificato è presente e non vuota.
     *
     * @param scenario                        L'oggetto {@link Scenario} di riferimento.
     * @param scen                            Un flag che indica se la sezione "Sceneggiatura" deve essere inclusa nel PDF.
     * @param patientSimulatedScenarioService Il servizio {@link PatientSimulatedScenarioService}
     *                                        per recuperare il testo della sceneggiatura.
     * @throws IOException Se si verifica un errore durante la scrittura nel documento PDF.
     */
    public static void createSceneggiaturaSection(Scenario scenario, boolean scen, PatientSimulatedScenarioService patientSimulatedScenarioService) throws IOException {
        // Recupera il testo della sceneggiatura per lo scenario dato.
        String sceneggiatura = patientSimulatedScenarioService.getSceneggiatura(scenario.getId());

        // Se la sceneggiatura è nulla, vuota, o la sezione non è richiesta, il metodo termina.
        if (sceneggiatura == null || sceneggiatura.isEmpty() || !scen) {
            return;
        }

        // Controlla se è necessario iniziare una nuova pagina prima di disegnare la sezione.
        // Lo spazio stimato è per il titolo della sezione e un margine iniziale.
        checkForNewPage(LEADING * 5);

        // Disegna il titolo principale della sezione "Sceneggiatura".
        drawSection("Sceneggiatura", ""); // Il secondo parametro è vuoto perché il contenuto segue.

        // Inserisce il testo della sceneggiatura nel PDF, gestendo eventuali tag HTML.
        // Il testo viene indentato con un margine sinistro maggiore.
        renderHtmlWithFormatting(sceneggiatura, MARGIN + 20);

        // Aggiunge uno spazio verticale dopo la sezione per separarla dal contenuto successivo.
        PdfExportService.currentYPosition -= LEADING;

        logger.info("Sezione Sceneggiatura creata con successo per lo scenario {}.", scenario.getId());
    }
}