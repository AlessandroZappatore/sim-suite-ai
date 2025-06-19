package it.uniupo.simnova.service.export;

import it.uniupo.simnova.domain.respons_model.LabExamSet;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static it.uniupo.simnova.service.export.helper.pdf.LabExamPdfHelper.createLabExamSection;
import static it.uniupo.simnova.service.export.helper.pdf.LoadFont.loadFont;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;

/**
 * Servizio per la generazione e l'esportazione di PDF contenenti i risultati degli esami di laboratorio.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class LabExamPdfExportService {
    /**
     * Logger per la registrazione delle operazioni e degli errori durante la generazione del PDF.
     */
    private static final Logger logger = LoggerFactory.getLogger(LabExamPdfExportService.class);
    /**
     * Posizione corrente in Y per il testo nel PDF.
     */
    public static float currentYPosition;
    /**
     * Flusso di contenuto corrente per la scrittura nel PDF.
     */
    public static PDPageContentStream currentContentStream;
    /**
     * Font utilizzato per il testo in grassetto nel PDF.
     */
    public static PDFont FONTBOLD;
    /**
     * Font utilizzato per il testo normale nel PDF.
     */
    public static PDFont FONTREGULAR;
    /**
     * Documento PDF corrente in cui vengono scritti i dati.
     */
    private static PDDocument document;
    /**
     * Servizio per la gestione dello storage dei file.
     */
    private final FileStorageService fileStorageService;

    /**
     * Costruttore del servizio per l'esportazione dei PDF degli esami di laboratorio.
     *
     * @param fileStorageService Servizio per la gestione dello storage dei file.
     */
    public LabExamPdfExportService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Inizializza una nuova pagina nel documento PDF corrente.
     *
     * @throws IOException Se si verifica un errore durante la creazione della pagina o del flusso di contenuto.
     */
    public static void initNewPage() throws IOException {
        if (currentContentStream != null) {
            currentContentStream.close();
        }
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        currentContentStream = new PDPageContentStream(document, currentPage);
        currentYPosition = PDRectangle.A4.getHeight() - MARGIN;
    }

    /**
     * Controlla se c'è spazio sufficiente nella pagina corrente per aggiungere nuovo contenuto.
     * Se lo spazio è insufficiente, inizializza una nuova pagina.
     *
     * @param neededSpace Lo spazio necessario per il nuovo contenuto in punti.
     * @throws IOException Se si verifica un errore durante l'inizializzazione della nuova pagina.
     */
    public static void checkForNewPage(float neededSpace) throws IOException {
        if (currentYPosition - neededSpace < MARGIN) {
            initNewPage();
        }
    }

    /**
     * Genera un PDF per un set di esami di laboratorio e lo salva nella cartella di upload.
     *
     * @param labExamSet I dati degli esami da stampare.
     * @param scenario   Lo scenario di riferimento.
     * @return Il nome del file PDF salvato.
     * @throws IOException Se si verifica un errore durante la generazione o il salvataggio.
     */
    public String generateAndSaveLabExamPdf(LabExamSet labExamSet, Scenario scenario) throws IOException {
        document = null;
        currentContentStream = null;

        try {
            document = new PDDocument();
            FONTREGULAR = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            FONTBOLD = loadFont(document, "/fonts/LiberationSans-Bold.ttf");

            initNewPage();

            // Correzione: Passa l'oggetto 'document' come richiesto dalla firma del metodo
            createLabExamSection(labExamSet, scenario);

            if (currentContentStream != null) {
                currentContentStream.close();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            // Crea un nome file univoco e salvalo
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String filename = String.format("esami_laboratorio_scenario_%d_%s.pdf", scenario.getId(), timestamp);

            // Correzione: Converte il byte array in un InputStream
            InputStream inputStream = new ByteArrayInputStream(pdfBytes);
            fileStorageService.storeFile(inputStream, filename);

            logger.info("PDF degli esami di laboratorio salvato come: {}", filename);
            return filename;

        } catch (Exception e) {
            logger.error("Errore critico durante la generazione del PDF degli esami per lo scenario ID {}: {}", scenario.getId(), e.getMessage(), e);
            throw new IOException("Generazione PDF fallita: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
}