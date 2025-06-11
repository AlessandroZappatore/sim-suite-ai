package it.uniupo.simnova.service.export;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.helper.pdf.LogoLoader;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static it.uniupo.simnova.service.export.helper.pdf.LoadFont.loadFont;
import static it.uniupo.simnova.service.export.helper.pdf.LogoLoader.loadCenterLogo;
import static it.uniupo.simnova.service.export.helper.pdf.LogoLoader.loadLogo;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioDescription.createScenarioDescription;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioExam.createExamsSection;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioHeader.createScenarioHeader;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioPatient.createPatientSection;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioSceneggiatura.createSceneggiaturaSection;
import static it.uniupo.simnova.service.export.helper.pdf.ScenarioTimeline.createTimelineSection;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;

/**
 * Servizio per l'esportazione di scenari in formato PDF.
 * Questo servizio utilizza la libreria <strong>Apache PDFBox</strong> per generare documenti PDF
 * contenenti informazioni dettagliate sugli scenari, aggregando dati da vari servizi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class PdfExportService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);

    /**
     * La posizione corrente sull'asse Y per il disegno del contenuto nella pagina PDF.
     * Viene aggiornata dinamicamente durante la generazione del documento.
     */
    public static float currentYPosition;

    /**
     * Lo stream di contenuto corrente utilizzato per disegnare elementi sulla pagina PDF.
     */
    public static PDPageContentStream currentContentStream;

    /**
     * Il font PDF utilizzato per il testo in grassetto.
     */
    public static PDFont FONTBOLD;

    /**
     * Il font PDF utilizzato per il testo in grassetto e corsivo.
     */
    public static PDFont FONTBOLDITALIC;

    /**
     * Il font PDF utilizzato per il testo normale.
     */
    public static PDFont FONTREGULAR;

    /**
     * Il font PDF utilizzato per il testo in corsivo.
     */
    public static PDFont FONTITALIC;

    /**
     * Il numero della pagina corrente del documento PDF.
     * Inizializzato a 1 e incrementato a ogni nuova pagina.
     */
    private static int pageNumber = 1;

    /**
     * Il documento PDF su cui si sta lavorando.
     */
    private static PDDocument document;

    /**
     * L'oggetto immagine del logo principale da inserire nel PDF.
     */
    private static PDImageXObject logo;

    /**
     * L'oggetto immagine del logo centrale da inserire nel PDF.
     */
    private static PDImageXObject centerLogo;

    /**
     * Servizio per la gestione dello storage dei file, utilizzato per caricare loghi o immagini.
     */
    private final FileStorageService fileStorageService;

    /**
     * Servizio per la gestione dei materiali associati agli scenari.
     */
    private final MaterialeService materialeService;

    /**
     * Servizio per la gestione degli scenari di tipo "Paziente Simulato Scenario".
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;

    /**
     * Servizio per la gestione delle azioni chiave definite negli scenari.
     */
    private final AzioneChiaveService azioneChiaveService;

    /**
     * Servizio per la gestione dei dati del paziente al tempo zero (T0).
     */
    private final PazienteT0Service pazienteT0Service;

    /**
     * Servizio per la gestione degli esami e referti associati agli scenari.
     */
    private final EsameRefertoService esameRefertoService;

    /**
     * Servizio per la gestione dei dati relativi all'esame fisico.
     */
    private final EsameFisicoService esameFisicoService;

    /**
     * Servizio per la gestione delle operazioni sugli oggetti {@link Scenario}.
     */
    private final ScenarioService scenarioService;

    /**
     * Servizio per la gestione degli scenari di tipo "Advanced Scenario".
     */
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Costruisce una nuova istanza di <code>PdfExportService</code>.
     * Inietta tutte le dipendenze dei servizi necessari per la generazione del PDF.
     *
     * @param scenarioService                 Il servizio per le operazioni sugli scenari.
     * @param fileStorageService              Il servizio per lo storage dei file.
     * @param materialeService                Il servizio per i materiali.
     * @param patientSimulatedScenarioService Il servizio per gli scenari simulati con paziente.
     * @param azioneChiaveService             Il servizio per le azioni chiave.
     * @param pazienteT0Service               Il servizio per i dati del paziente T0.
     * @param esameRefertoService             Il servizio per gli esami e referti.
     * @param esameFisicoService              Il servizio per l'esame fisico.
     * @param advancedScenarioService         Il servizio per gli scenari avanzati.
     */
    public PdfExportService(ScenarioService scenarioService,
                            FileStorageService fileStorageService,
                            MaterialeService materialeService,
                            PatientSimulatedScenarioService patientSimulatedScenarioService,
                            AzioneChiaveService azioneChiaveService,
                            PazienteT0Service pazienteT0Service,
                            EsameRefertoService esameRefertoService,
                            EsameFisicoService esameFisicoService, AdvancedScenarioService advancedScenarioService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.materialeService = materialeService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.azioneChiaveService = azioneChiaveService;
        this.pazienteT0Service = pazienteT0Service;
        this.esameRefertoService = esameRefertoService;
        this.esameFisicoService = esameFisicoService;
        this.advancedScenarioService = advancedScenarioService;
    }

    /**
     * Inizializza una nuova pagina all'interno del documento PDF.
     * Questa operazione include la chiusura dello stream di contenuto precedente (se esistente),
     * l'aggiunta di una nuova pagina al documento, l'apertura di un nuovo stream di contenuto
     * e l'inserimento dei loghi (solo sulla prima pagina) e l'impostazione della posizione
     * iniziale per il disegno del contenuto.
     *
     * @throws IOException se si verifica un errore durante la creazione della pagina o la gestione dello stream.
     */
    public static void initNewPage() throws IOException {
        // Chiude lo stream di contenuto precedente se è aperto, per evitare sovrapposizioni.
        if (currentContentStream != null) {
            try {
                currentContentStream.close();
            } catch (Exception e) {
                logger.warn("Errore durante la chiusura dello stream del contenuto corrente: {}", e.getMessage());
            }
        }

        // Crea e aggiunge una nuova pagina al documento PDF.
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);

        // Apre un nuovo stream di contenuto per la pagina appena creata.
        currentContentStream = new PDPageContentStream(document, currentPage);

        // Gestione specifica per la prima pagina (aggiunta loghi).
        if (pageNumber == 1) {
            float simLogoWidth = 40;
            float simLogoHeight = 40;

            float centerLogoMaxWidth = 120;
            float centerLogoMaxHeight = 80;

            float centerLogoWidth;
            float centerLogoHeight = 0;

            // Calcola la posizione Y per il logo principale.
            float simLogoY = PDRectangle.A4.getHeight() - MARGIN - simLogoHeight;

            // Disegna il logo principale se disponibile.
            if (logo != null) {
                currentContentStream.drawImage(logo, MARGIN, simLogoY, simLogoWidth, simLogoHeight);
            }

            // Disegna il logo centrale se disponibile, scalando per adattarsi.
            if (centerLogo != null) {
                float scale = Math.min(
                        centerLogoMaxWidth / LogoLoader.centerLogoWidth,
                        centerLogoMaxHeight / LogoLoader.centerLogoHeight
                );

                centerLogoWidth = LogoLoader.centerLogoWidth * scale;
                centerLogoHeight = LogoLoader.centerLogoHeight * scale;

                float centerLogoX = (PDRectangle.A4.getWidth() - centerLogoWidth) / 2;
                float centerLogoY = PDRectangle.A4.getHeight() - MARGIN - centerLogoHeight;

                currentContentStream.drawImage(centerLogo, centerLogoX, centerLogoY, centerLogoWidth, centerLogoHeight);
            }

            // Imposta la posizione Y iniziale per il contenuto, sotto i loghi.
            float lowestY = simLogoY;
            if (centerLogo != null) {
                lowestY = Math.min(lowestY, PDRectangle.A4.getHeight() - MARGIN - centerLogoHeight);
            }
            currentYPosition = lowestY - LEADING;
        } else {
            // Per le pagine successive alla prima, la posizione Y inizia dall'alto, sotto il margine.
            currentYPosition = PDRectangle.A4.getHeight() - MARGIN;
        }
        // Incrementa il numero di pagina.
        pageNumber++;
    }

    /**
     * Controlla se è necessario creare una nuova pagina nel documento PDF.
     * Se lo spazio rimanente sulla pagina corrente è inferiore allo spazio richiesto dal prossimo contenuto,
     * viene invocato {@link #initNewPage()} per passare a una nuova pagina.
     *
     * @param neededSpace Lo spazio in punti PDF necessario per il prossimo blocco di contenuto.
     * @throws IOException se si verifica un errore durante la creazione della nuova pagina.
     */
    public static void checkForNewPage(float neededSpace) throws IOException {
        // Se la posizione Y corrente meno lo spazio necessario è inferiore al margine inferiore, crea una nuova pagina.
        if (currentYPosition - neededSpace < MARGIN) {
            initNewPage();
        }
    }

    /**
     * Esporta uno scenario e i suoi dati correlati in un documento PDF.
     * Il metodo consente di includere o escludere sezioni specifiche del contenuto
     * tramite i flag booleani forniti.
     *
     * @param scenarioId L'ID dello scenario da esportare.
     * @param desc       Flag per includere la descrizione dello scenario.
     * @param brief      Flag per includere il brief dello scenario.
     * @param infoGen    Flag per includere le informazioni generali.
     * @param patto      Flag per includere la sezione "Patto".
     * @param azioni     Flag per includere le azioni chiave.
     * @param obiettivi  Flag per includere gli obiettivi.
     * @param moula      Flag per includere la sezione "Moula".
     * @param liqui      Flag per includere la sezione "Liquidi".
     * @param matNec     Flag per includere i materiali necessari.
     * @param param      Flag per includere i parametri del paziente.
     * @param acces      Flag per includere gli accessi.
     * @param fisic      Flag per includere l'esame fisico.
     * @param esam       Flag per includere gli esami e referti.
     * @param time       Flag per includere la timeline (per scenari avanzati/simulati).
     * @param scen       Flag per includere la sceneggiatura (solo per scenari simulati).
     * @return Un array di byte contenente il documento PDF generato.
     * @throws IOException se si verifica un errore durante la generazione del PDF, ad esempio problemi di I/O o caricamento font/immagini.
     */
    public byte[] exportScenarioToPdf(int scenarioId,
                                      boolean desc,
                                      boolean brief,
                                      boolean infoGen,
                                      boolean patto,
                                      boolean azioni,
                                      boolean obiettivi,
                                      boolean moula,
                                      boolean liqui,
                                      boolean matNec,
                                      boolean param,
                                      boolean acces,
                                      boolean fisic,
                                      boolean esam,
                                      boolean time,
                                      boolean scen) throws IOException {

        // Inizializza le variabili statiche per una nuova esportazione.
        document = null;
        currentContentStream = null;
        pageNumber = 1;

        try {
            document = new PDDocument();

            // Carica tutti i font necessari per il documento PDF.
            FONTREGULAR = loadFont(document, "/fonts/LiberationSans-Regular.ttf");
            FONTBOLD = loadFont(document, "/fonts/LiberationSans-Bold.ttf");
            FONTITALIC = loadFont(document, "/fonts/LiberationSans-Italic.ttf");
            FONTBOLDITALIC = loadFont(document, "/fonts/LiberationSans-BoldItalic.ttf");

            // Carica le immagini dei loghi.
            logo = loadLogo(document);
            centerLogo = loadCenterLogo(document, fileStorageService);

            // Inizializza la prima pagina del documento.
            initNewPage();

            // Recupera l'oggetto Scenario principale.
            Scenario scenario = scenarioService.getScenarioById(scenarioId);
            logger.info("Recuperato scenario con titolo: {}", scenario.getTitolo());

            // Crea la sezione dell'intestazione dello scenario.
            createScenarioHeader(scenario);

            // Crea la sezione della descrizione dello scenario e i suoi sotto componenti, basandosi sui flag.
            createScenarioDescription(scenario, desc, brief, infoGen, patto, azioni, obiettivi, moula, liqui, matNec, scenarioService, materialeService, azioneChiaveService);

            // Crea la sezione relativa al paziente, basandosi sui flag.
            createPatientSection(scenarioId, param, acces, fisic, pazienteT0Service, esameFisicoService);

            // Crea la sezione degli esami e referti, basandosi sul flag.
            createExamsSection(scenarioId, esam, esameRefertoService);

            // Recupera il tipo di scenario per gestire sezioni condizionali.
            String scenarioType = scenarioService.getScenarioType(scenarioId);

            // Aggiunge la sezione timeline se lo scenario è "Advanced Scenario" o "Patient Simulated Scenario" e il flag 'time' è true.
            if (scenarioType != null && (scenarioType.equals("Advanced Scenario") ||
                    scenarioType.equals("Patient Simulated Scenario")) && time) {
                createTimelineSection(scenario, advancedScenarioService, scenarioService);
                logger.info("Sezione Timeline creata per lo scenario {}", scenario.getTitolo());
            }

            // Aggiunge la sezione sceneggiatura solo se lo scenario è "Patient Simulated Scenario" e il flag 'scen' è true.
            if (scenarioType != null && scenarioType.equals("Patient Simulated Scenario") && scen) {
                createSceneggiaturaSection(scenario, true, patientSimulatedScenarioService);
                logger.info("Sezione Sceneggiatura creata per lo scenario {}", scenario.getTitolo());
            }

            // Chiude l'ultimo stream di contenuto attivo prima di salvare il documento.
            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                    currentContentStream = null; // Imposta a null per sicurezza.
                } catch (Exception e) {
                    logger.warn("Errore durante la chiusura dello stream di contenuto finale: {}", e.getMessage());
                }
            }

            // Salva il documento PDF in un ByteArrayOutputStream e lo restituisce come array di byte.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            logger.info("PDF salvato con successo per lo scenario {}", scenario.getTitolo());
            return outputStream.toByteArray();

        } catch (Exception e) {
            // Gestione degli errori durante la generazione del PDF.
            logger.error("Errore critico durante la generazione del PDF per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            throw new IOException("Generazione PDF fallita: " + e.getMessage(), e);
        } finally {
            // Blocco finally per assicurarsi che tutti gli stream e il documento vengano chiusi.
            if (currentContentStream != null) {
                try {
                    currentContentStream.close();
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura dello stream nel blocco finally: {}", e.getMessage());
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    logger.warn("Errore nella chiusura del documento nel blocco finally: {}", e.getMessage());
                }
            }
        }
    }
}