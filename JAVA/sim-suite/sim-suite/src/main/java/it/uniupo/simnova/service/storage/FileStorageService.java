package it.uniupo.simnova.service.storage;

import it.uniupo.simnova.service.scenario.helper.MediaHelper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione della memorizzazione dei file multimediali.
 * Fornisce metodi per salvare, eliminare e leggere file dalla directory di archiviazione.
 * I file vengono sanitizzati per garantire nomi validi e sicuri.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class FileStorageService {
    /**
     * Logger per registrare le operazioni della classe.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    /**
     * Percorso della directory di archiviazione dei file multimediali.
     * Viene configurato tramite la proprietà "storage.media-dir" in application.properties.
     */
    private final Path rootLocation;

    /**
     * Costruttore che inizializza il servizio con il percorso della directory
     * specificato in application.properties.
     *
     * @param mediaDir Il percorso della directory di archiviazione, iniettato da Spring.
     */
    public FileStorageService(@Value("${storage.media-dir}") String mediaDir) {

        this.rootLocation = Paths.get(mediaDir).toAbsolutePath().normalize();
        logger.info("Percorso di archiviazione configurato: {}", this.rootLocation);
    }

    /**
     * Genera un nome file sicuro aggiungendo l'ID dello scenario e rimuovendo caratteri non validi.
     *
     * @param filename Nome originale del file.
     * @return Nome del file sanitizzato.
     */
    private static String getSanitizedFilename(String filename) {
        if (filename == null || filename.isBlank()) return "file";

        String extension = "";
        String baseName = filename;
        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex >= 0) {
            extension = filename.substring(lastDotIndex);
            baseName = filename.substring(0, lastDotIndex);
        }

        String sanitizedBaseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_");

        sanitizedBaseName = sanitizedBaseName.replaceAll("_+", "_");

        sanitizedBaseName = sanitizedBaseName.replaceAll("^_|_$", "");


        String sanitizedExtension = extension;
        if (!extension.isEmpty()) {
            sanitizedExtension = extension.replaceAll("[^a-zA-Z0-9.]", "");
        }


        if (sanitizedBaseName.isEmpty()) {
            sanitizedBaseName = "file";
        }

        return sanitizedBaseName + sanitizedExtension;
    }

    /**
     * Metodo eseguito dopo l'inizializzazione del bean per creare la directory
     * di archiviazione se non esiste.
     */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            logger.info("Directory di archiviazione creata (o già esistente): {}", rootLocation);
        } catch (IOException e) {
            logger.error("Impossibile creare la directory di archiviazione {}", rootLocation, e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Salva un file nella directory di archiviazione.
     * Il nome del file viene sanitizzato e viene aggiunto l'ID dello scenario.
     *
     * @param file     InputStream del file da salvare.
     * @param filename Nome originale del file.
     * @return Il nome del file sanitizzato e salvato, o null in caso di input non valido.
     * @throws RuntimeException Se si verifica un errore durante il salvataggio.
     */
    public String storeFile(InputStream file, String filename) {
        try {
            if (file == null || filename == null || filename.isBlank()) {
                logger.warn("Input non valido per storeFile: file, nome file o idScenario mancanti.");
                return null;
            }
            String sanitizedFilename = getSanitizedFilename(filename);

            Path destinationFile = this.rootLocation.resolve(sanitizedFilename).normalize();


            if (!destinationFile.getParent().equals(this.rootLocation)) {
                logger.error("Tentativo di memorizzare il file fuori dalla directory consentita: {}", destinationFile);
                throw new RuntimeException("Cannot store file outside current directory");
            }


            Files.copy(file, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File memorizzato con successo: {}", sanitizedFilename);
            return sanitizedFilename;
        } catch (IOException e) {
            logger.error("Errore durante la memorizzazione del file {} (sanitized: {})", filename, getSanitizedFilename(filename), e);

            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    /**
     * Elimina una lista di file dalla directory di archiviazione.
     *
     * @param filenames Lista dei nomi dei file da eliminare.
     */
    public void deleteFiles(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            logger.warn("Lista di file da eliminare vuota o nulla.");
            return;
        }
        for (String filename : filenames) {

            deleteFile(filename);
        }
    }

    /**
     * Elimina un singolo file dalla directory di archiviazione.
     * Verifica prima che il file non sia utilizzato da altri scenari.
     *
     * @param filename Nome del file da eliminare.
     */
    public void deleteFile(String filename) {
        if (filename == null || filename.isBlank()) {
            logger.warn("Nome file non valido per l'eliminazione.");
            return;
        }

        if (MediaHelper.isFileInUse(filename)) {
            logger.info("File {} non eliminato perché è utilizzato in altri scenari", filename);
            return;
        }

        try {
            Path filePath = this.rootLocation.resolve(filename).normalize();


            if (!filePath.getParent().equals(this.rootLocation)) {
                logger.error("Tentativo di eliminare il file fuori dalla directory consentita: {}", filePath);
                return;
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.info("File eliminato con successo: {}", filename);
            } else {
                logger.warn("File non trovato per l'eliminazione: {}", filename);
            }
        } catch (IOException e) {

            logger.error("Errore durante l'eliminazione del file {}", filename, e);
        }
    }

    /**
     * Restituisce il percorso assoluto e normalizzato della directory di archiviazione.
     *
     * @return Oggetto Path che rappresenta la directory di archiviazione.
     */
    public Path getMediaDirectory() {
        return rootLocation;
    }

    /**
     * Controlla se un file esiste nella directory di archiviazione.
     *
     * @param centerLogoFilename Nome del file da controllare.
     * @return true se il file esiste, false altrimenti.
     */
    public boolean fileExists(String centerLogoFilename) {
        Path filePath = rootLocation.resolve(centerLogoFilename);
        return Files.exists(filePath);
    }

    /**
     * Memorizza un file nella directory di archiviazione.
     * Il nome del file deve essere fornito come parametro.
     *
     * @param inputStream        InputStream del file da memorizzare.
     * @param centerLogoFilename Nome del file da memorizzare.
     */
    public void store(InputStream inputStream, String centerLogoFilename) {
        try {
            Path destinationFile = rootLocation.resolve(centerLogoFilename).normalize();

            if (!destinationFile.getParent().equals(rootLocation)) {
                logger.error("Tentativo di memorizzare il file fuori dalla directory consentita: {}", destinationFile);
                throw new RuntimeException("Cannot store file outside current directory");
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File memorizzato con successo: {}", centerLogoFilename);
        } catch (IOException e) {
            logger.error("Errore durante la memorizzazione del file {}", centerLogoFilename, e);
            throw new RuntimeException("Failed to store file " + centerLogoFilename, e);
        }
    }

    /**
     * Legge un file dalla directory di archiviazione e restituisce un InputStream.
     * Il file deve essere presente nella directory di archiviazione.
     *
     * @param centerLogoFilename Nome del file da leggere.
     * @return InputStream del file letto.
     * @throws IOException Se si verifica un errore durante la lettura del file, inclusa la non esistenza del file.
     */
    public InputStream readFile(String centerLogoFilename) throws IOException {
        Path filePath = this.rootLocation.resolve(centerLogoFilename).normalize();

        // Controllo di sicurezza per assicurarsi che il file sia direttamente nella directory root
        // Mantenendo la logica di controllo del percorso originale
        if (!filePath.getParent().equals(this.rootLocation)) {
            logger.error("Tentativo di leggere il file '{}' fuori dalla directory consentita. Percorso risolto: {}", centerLogoFilename, filePath);
            // Modificato da RuntimeException a IOException per essere gestito correttamente da LogoLoader
            throw new IOException("Accesso al file non consentito (file non direttamente nella directory root): " + centerLogoFilename);
        }

        try {
            return Files.newInputStream(filePath);
        } catch (NoSuchFileException e) {
            // Logga un avviso specifico per file non trovato, poiché potrebbe essere uno scenario atteso per file opzionali.
            // LogoLoader gestirà questo e tenterà un fallback.
            logger.warn("File non trovato durante il tentativo di lettura: '{}' (percorso completo: '{}')", centerLogoFilename, filePath);
            throw e; // Rilancia NoSuchFileException (che è una FileNotFoundException)
        } catch (IOException e) {
            // Per altri errori I/O imprevisti (es. permessi, errori disco)
            logger.error("Errore I/O imprevisto durante la lettura del file '{}': {}", centerLogoFilename, e.getMessage(), e);
            throw e; // Rilancia l'IOException originale
        }
    }

    /**
     * Recupera tutti i file presenti nella directory di archiviazione, escludendo il file "center_logo.png".
     *
     * @return Una lista di nomi di file presenti nella directory di archiviazione.
     */
    public ArrayList<String> getAllFiles() {
        ArrayList<String> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootLocation)) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    String filename = path.getFileName().toString();

                    if (!filename.equals("center_logo.png")) {
                        files.add(filename);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Errore durante la lettura dei file nella directory {}", rootLocation, e);
        }
        return files;
    }
}

