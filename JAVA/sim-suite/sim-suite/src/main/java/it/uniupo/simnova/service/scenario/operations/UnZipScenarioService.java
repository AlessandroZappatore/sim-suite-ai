package it.uniupo.simnova.service.scenario.operations;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Servizio per la decompressione di un file ZIP contenente uno scenario.
 * Si occupa di estrarre il file <code>scenario.json</code> e tutti i file
 * multimediali presenti nella cartella designata (attualmente <code>esami/</code>).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class UnZipScenarioService {

    /**
     * Il nome standard del file JSON che contiene tutti i dati dello scenario all'interno dell'archivio ZIP.
     */
    public static final String SCENARIO_JSON_FILENAME = "scenario.json";

    /**
     * Il prefisso della cartella all'interno dell'archivio ZIP dove si prevede siano archiviati i file multimediali.
     * Attualmente impostato su "esami/".
     */
    public static final String MEDIA_FOLDER_PREFIX = "esami/";

    /**
     * Costruttore di default per il servizio di decompressione.
     * Non richiede parametri e non esegue operazioni di inizializzazione.
     */
    private UnZipScenarioService() {
        // Costruttore di default, non esegue operazioni di inizializzazione.
    }

    /**
     * Decomprime un {@link InputStream} che rappresenta un file ZIP di uno scenario.
     * Estrae il file <code>scenario.json</code> e tutti i file contenuti nella cartella {@value #MEDIA_FOLDER_PREFIX}.
     *
     * @param zipInputStream L'<code>InputStream</code> del file ZIP da decomprimere. Non deve essere <code>null</code>.
     * @return Un oggetto {@link UnzippedScenarioData} che incapsula il contenuto del file <code>scenario.json</code>
     * come array di byte e una mappa dei nomi dei file multimediali con i loro rispettivi contenuti come array di byte.
     * @throws IOException              Se si verifica un errore di I/O durante la lettura dello ZIP.
     * @throws IllegalArgumentException Se il file <code>scenario.json</code> non viene trovato all'interno dell'archivio ZIP,
     *                                  o se il <code>zipInputStream</code> è <code>null</code>.
     */
    public UnzippedScenarioData unzipScenario(InputStream zipInputStream) throws IOException {
        if (zipInputStream == null) {
            throw new IllegalArgumentException("L'InputStream del file ZIP non può essere nullo.");
        }

        byte[] scenarioJson = null;
        Map<String, byte[]> mediaFiles = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry zipEntry;
            // Scorre tutte le entry all'interno dello ZIP.
            while ((zipEntry = zis.getNextEntry()) != null) {
                // Ignora le directory, elaborando solo i file.
                if (!zipEntry.isDirectory()) {
                    // Controlla se l'entry corrente è il file scenario.json.
                    if (SCENARIO_JSON_FILENAME.equalsIgnoreCase(zipEntry.getName())) {
                        scenarioJson = readEntryData(zis);
                    }
                    // Controlla se l'entry corrente è un file multimediale nella cartella designata.
                    else if (zipEntry.getName().toLowerCase().startsWith(MEDIA_FOLDER_PREFIX)) {
                        // Estrae il nome del file multimediale rimuovendo il prefisso della cartella.
                        String mediaFileName = zipEntry.getName().substring(MEDIA_FOLDER_PREFIX.length());
                        if (!mediaFileName.isEmpty()) { // Assicura che non sia un nome vuoto.
                            mediaFiles.put(mediaFileName, readEntryData(zis));
                        }
                    }
                }
                zis.closeEntry(); // Chiude l'entry corrente prima di passare alla successiva.
            }
        }

        // Verifica che il file scenario.json sia stato trovato.
        if (scenarioJson == null) {
            throw new IllegalArgumentException("File '" + SCENARIO_JSON_FILENAME + "' non trovato nell'archivio ZIP. È essenziale per l'importazione dello scenario.");
        }

        return new UnzippedScenarioData(scenarioJson, mediaFiles);
    }

    /**
     * Legge i dati di una singola entry da un {@link ZipInputStream} e li restituisce come array di byte.
     * Questo metodo è un'utilità interna per la decompressione.
     *
     * @param zis Il {@link ZipInputStream} da cui leggere i dati dell'entry corrente.
     * @return Un array di byte contenente il contenuto dell'entry ZIP.
     * @throws IOException Se si verifica un errore durante la lettura dei dati.
     */
    private byte[] readEntryData(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // Buffer per la lettura dei dati.
        int len;
        // Legge i dati dall'InputStream della entry finché ce ne sono.
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len); // Scrive i dati nel ByteArrayOutputStream.
        }
        return baos.toByteArray(); // Restituisce i dati come array di byte.
    }

    /**
     * Record immutabile che incapsula i dati estratti da un file ZIP di uno scenario.
     * Contiene il contenuto del file <code>scenario.json</code> e una mappa di file multimediali.
     *
     * @param scenarioJson L'array di byte del contenuto del file <code>scenario.json</code>.
     * @param mediaFiles   Una {@link Map} dove la chiave è il nome del file multimediale (<code>String</code>)
     *                     e il valore è il contenuto del file come array di byte (<code>byte[]</code>).
     */
    public record UnzippedScenarioData(byte[] scenarioJson, Map<String, byte[]> mediaFiles) {
    }
}