package it.uniupo.simnova.service.export;

import it.uniupo.simnova.service.scenario.helper.MediaHelper;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Servizio per l'esportazione di uno scenario in un file ZIP.
 * Il file ZIP può contenere la rappresentazione JSON o PDF dello scenario,
 * insieme a tutti gli allegati multimediali associati.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class ZipExportService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ZipExportService.class);

    /**
     * Servizio per la gestione dello storage dei file, utilizzato per accedere ai file multimediali.
     */
    private final FileStorageService fileStorageService;

    /**
     * Servizio per l'esportazione degli scenari in formato PDF.
     */
    private final PdfExportService pdfExportService;

    /**
     * Servizio per l'esportazione degli scenari in formato JSON.
     */
    private final JSONExportService jsonExportService;

    /**
     * Costruisce una nuova istanza di <code>ZipExportService</code>.
     * Inietta le dipendenze dei servizi necessari per l'esportazione.
     *
     * @param fileStorageService Il servizio per la gestione dei file.
     * @param pdfExportService   Il servizio per l'esportazione in PDF.
     * @param jsonExportService  Il servizio per l'esportazione in JSON.
     */
    @Autowired
    public ZipExportService(FileStorageService fileStorageService, PdfExportService pdfExportService, JSONExportService jsonExportService) {
        this.fileStorageService = fileStorageService;
        this.pdfExportService = pdfExportService;
        this.jsonExportService = jsonExportService;
    }

    /**
     * Esporta uno scenario in un file ZIP.
     * Il file ZIP include la rappresentazione JSON dello scenario (<code>scenario.json</code>)
     * e tutti i file multimediali associati, organizzati nella sotto cartella <code>esami/</code>.
     *
     * @param scenarioId L'ID dello scenario da esportare.
     * @return Un array di byte che rappresenta il file ZIP generato.
     * @throws IOException se si verifica un errore durante la scrittura del file ZIP o l'accesso ai file.
     */
    public byte[] exportScenarioToZip(Integer scenarioId) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            // Aggiunge il file JSON dello scenario allo ZIP.
            byte[] jsonBytes = jsonExportService.exportScenarioToJSON(scenarioId);
            ZipEntry jsonEntry = new ZipEntry("scenario.json");
            zipOut.putNextEntry(jsonEntry);
            zipOut.write(jsonBytes);
            zipOut.closeEntry();

            // Recupera la lista dei nomi dei file multimediali associati allo scenario.
            List<String> mediaFiles = MediaHelper.getMediaFilesForScenario(scenarioId);
            if (!mediaFiles.isEmpty()) {
                // Crea una directory virtuale "esami/" all'interno dello ZIP.
                zipOut.putNextEntry(new ZipEntry("esami/"));
                zipOut.closeEntry();

                // Aggiunge ogni file multimediale allo ZIP nella sotto cartella "esami/".
                for (String filename : mediaFiles) {
                    try {
                        Path imagePath = Paths.get(fileStorageService.getMediaDirectory().toString(), filename);

                        if (Files.exists(imagePath)) {
                            byte[] imageBytes = Files.readAllBytes(imagePath);
                            ZipEntry imageEntry = new ZipEntry("esami/" + filename);
                            zipOut.putNextEntry(imageEntry);
                            zipOut.write(imageBytes);
                            zipOut.closeEntry();
                        } else {
                            logger.warn("Il file multimediale non esiste e non può essere aggiunto allo ZIP: {}", imagePath);
                        }
                    } catch (IOException e) {
                        logger.error("Errore durante l'aggiunta del file multimediale '{}' allo ZIP: {}", filename, e.getMessage());
                        // Continua con gli altri file anche in caso di errore su uno specifico.
                    }
                }
            }

            // Finalizza e svuota lo stream ZIP.
            zipOut.finish();
            zipOut.flush();

            return baos.toByteArray();
        }
    }

    /**
     * Esporta uno scenario in un file ZIP, includendo la rappresentazione PDF dello scenario (<code>scenario.pdf</code>)
     * e tutti i file multimediali associati, organizzati nella sotto cartella <code>esami/</code>.
     *
     * @param scenarioId L'ID dello scenario da esportare.
     * @param desc       Flag per includere la descrizione dello scenario nel PDF.
     * @param brief      Flag per includere il brief dello scenario nel PDF.
     * @param infoGen    Flag per includere le informazioni generali nel PDF.
     * @param patto      Flag per includere la sezione "Patto" nel PDF.
     * @param azioni     Flag per includere le azioni chiave nel PDF.
     * @param obiettivi  Flag per includere gli obiettivi nel PDF.
     * @param moula      Flag per includere la sezione "Moula" nel PDF.
     * @param liqui      Flag per includere la sezione "Liquidi" nel PDF.
     * @param matNec     Flag per includere i materiali necessari nel PDF.
     * @param param      Flag per includere i parametri del paziente nel PDF.
     * @param acces      Flag per includere gli accessi nel PDF.
     * @param fisic      Flag per includere l'esame fisico nel PDF.
     * @param esam       Flag per includere gli esami e referti nel PDF.
     * @param time       Flag per includere la timeline nel PDF (per scenari avanzati/simulati).
     * @param scen       Flag per includere la sceneggiatura nel PDF (solo per scenari simulati).
     * @return Un array di byte che rappresenta il file ZIP generato.
     * @throws IOException se si verifica un errore durante la scrittura del file ZIP o l'accesso ai file.
     */
    public byte[] exportScenarioPdfToZip(Integer scenarioId,
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
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            // Genera il PDF dello scenario e lo aggiunge allo ZIP.
            byte[] pdfBytes = pdfExportService.exportScenarioToPdf(scenarioId, desc, brief, infoGen, patto, azioni, obiettivi, moula, liqui, matNec, param, acces, fisic, esam, time, scen);
            ZipEntry pdfEntry = new ZipEntry("scenario.pdf");
            zipOut.putNextEntry(pdfEntry);
            zipOut.write(pdfBytes);
            zipOut.closeEntry();

            // Recupera la lista dei nomi dei file multimediali associati allo scenario.
            List<String> mediaFiles = MediaHelper.getMediaFilesForScenario(scenarioId);
            if (!mediaFiles.isEmpty()) {
                // Crea una directory virtuale "esami/" all'interno dello ZIP.
                zipOut.putNextEntry(new ZipEntry("esami/"));
                zipOut.closeEntry();

                // Aggiunge ogni file multimediale allo ZIP nella sotto cartella "esami/".
                for (String filename : mediaFiles) {
                    try {
                        Path imagePath = Paths.get(fileStorageService.getMediaDirectory().toString(), filename);

                        if (Files.exists(imagePath)) {
                            byte[] imageBytes = Files.readAllBytes(imagePath);
                            ZipEntry imageEntry = new ZipEntry("esami/" + filename);
                            zipOut.putNextEntry(imageEntry);
                            zipOut.write(imageBytes);
                            zipOut.closeEntry();
                        } else {
                            logger.warn("Il file multimediale non esiste e non può essere aggiunto allo ZIP: {}", imagePath);
                        }
                    } catch (IOException e) {
                        logger.error("Errore durante l'aggiunta del file multimediale '{}' allo ZIP: {}", filename, e.getMessage());
                        // Continua con gli altri file anche in caso di errore su uno specifico.
                    }
                }
            }

            // Finalizza e svuota lo stream ZIP.
            zipOut.finish();
            zipOut.flush();

            return baos.toByteArray();
        }
    }
}