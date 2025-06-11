package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.service.storage.FileStorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe di utilità per il <strong>caricamento dei loghi</strong> da includere nei documenti PDF generati.
 * Gestisce sia il logo predefinito dell'applicazione che un logo personalizzato del centro,
 * se fornito.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class LogoLoader {
    /**
     * Logger per la classe {@link LogoLoader}.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogoLoader.class);

    /**
     * Percorso del logo predefinito di SIM SUITE, che viene caricato dal classpath.
     */
    private static final String LOGO_URL = "/static/icons/LogoSimsuite.png";
    /**
     * Nome del file del logo del centro, che viene caricato dalla directory di upload.
     */
    private static final String CENTER_LOGO_FILENAME = "center_logo.png";
    /**
     * Percorso del logo di default del centro, che viene caricato dal classpath.
     * Questo logo viene utilizzato se il logo personalizzato non è disponibile o è vuoto/illeggibile.
     */
    private static final String DEFAULT_CENTER_LOGO_FILENAME = "/static/icons/default_center_logo.png";

    /**
     * Larghezza del logo del centro dopo il caricamento.
     */
    public static float centerLogoWidth;
    /**
     * Altezza del logo del centro dopo il caricamento.
     */
    public static float centerLogoHeight;

    /**
     * Costruttore privato per evitare l'istanza della classe, poiché contiene solo metodi statici.
     */
    private LogoLoader() {
        // Costruttore privato per evitare l'istanza della classe.
    }

    /**
     * Carica il <strong>logo predefinito di SIM SUITE</strong> da aggiungere nel documento PDF.
     * Il logo viene caricato dal classpath dell'applicazione.
     *
     * @param document Il documento {@link PDDocument} in cui caricare il logo.
     * @return Un oggetto {@link PDImageXObject} che rappresenta il logo di SIM SUITE,
     * o {@code null} se il file del logo non viene trovato.
     * @throws IOException Se si verifica un errore durante il caricamento del logo (es. errore di I/O).
     */
    public static PDImageXObject loadLogo(PDDocument document) throws IOException {
        try (InputStream logoStream = LogoLoader.class.getResourceAsStream(LOGO_URL)) {
            if (logoStream == null) {
                logger.warn("File del logo predefinito non trovato: {}", LOGO_URL);
                return null;
            }

            // Converte l'InputStream del logo in un array di byte.
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            // Crea l'oggetto immagine PDF da un array di byte.
            return PDImageXObject.createFromByteArray(document, buffer.toByteArray(), "SimSuiteLogo");
        }
    }

    /**
     * Carica il <strong>logo personalizzato del centro</strong> da aggiungere nel documento PDF.
     * Il logo viene letto tramite il {@link FileStorageService}, permettendo di caricare file
     * dalla directory di upload dell'applicazione.
     *
     * @param document           Il documento {@link PDDocument} in cui caricare il logo.
     * @param fileStorageService Il servizio {@link FileStorageService} per accedere al file del logo.
     * @return Un oggetto {@link PDImageXObject} che rappresenta il logo del centro.
     * Le sue dimensioni (larghezza e altezza) vengono memorizzate nelle variabili statiche
     * {@code centerLogoWidth} e {@code centerLogoHeight}.
     * @throws RuntimeException Se il caricamento del logo personalizzato e del logo di default fallisce.
     */
    public static PDImageXObject loadCenterLogo(PDDocument document, FileStorageService fileStorageService) {
        try (InputStream logoStream = fileStorageService.readFile(CENTER_LOGO_FILENAME)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            if (buffer.size() == 0) {
                logger.warn("Il file del logo del centro ({}) è vuoto o illeggibile. Tento con il logo di default.", CENTER_LOGO_FILENAME);
                try {
                    return loadDefaultCenterLogo(document);
                } catch (IOException ex) {
                    logger.error("Errore durante il caricamento del logo del centro di default ({}) dopo che il logo personalizzato era vuoto/illeggibile: {}", DEFAULT_CENTER_LOGO_FILENAME, ex.getMessage(), ex);
                    throw new RuntimeException("Impossibile caricare il logo del centro: il file personalizzato è vuoto/illeggibile e anche il fallback al default è fallito.", ex);
                }
            }

            PDImageXObject logoImage = PDImageXObject.createFromByteArray(document, buffer.toByteArray(), CENTER_LOGO_FILENAME);
            centerLogoWidth = logoImage.getWidth();
            centerLogoHeight = logoImage.getHeight();
            logger.info("Logo del centro personalizzato ({}) caricato con successo.", CENTER_LOGO_FILENAME);
            return logoImage;

        } catch (FileNotFoundException e) {
            logger.info("Logo del centro personalizzato ({}) non trovato. Tento con il logo di default.", CENTER_LOGO_FILENAME);
            try {
                return loadDefaultCenterLogo(document);
            } catch (IOException ex) {
                logger.error("Errore durante il caricamento del logo del centro di default ({}) dopo che il logo personalizzato non è stato trovato: {}", DEFAULT_CENTER_LOGO_FILENAME, ex.getMessage(), ex);
                throw new RuntimeException("Impossibile caricare il logo del centro: il file personalizzato non è stato trovato e anche il fallback al default è fallito.", ex);
            }
        } catch (IOException e) { // Altri errori I/O durante la lettura del logo personalizzato
            logger.error("Errore I/O durante il caricamento del logo del centro personalizzato ({}): {}. Tento con il logo di default.", CENTER_LOGO_FILENAME, e.getMessage(), e);
            try {
                return loadDefaultCenterLogo(document);
            } catch (IOException ex) {
                logger.error("Errore durante il caricamento del logo del centro di default ({}) dopo un errore I/O sul logo personalizzato: {}", DEFAULT_CENTER_LOGO_FILENAME, ex.getMessage(), ex);
                throw new RuntimeException("Impossibile caricare il logo del centro: errore I/O sul file personalizzato e anche il fallback al default è fallito.", ex);
            }
        }
    }

    /**
     * Carica il <strong>logo di default del centro</strong> da aggiungere nel documento PDF.
     * Il logo viene caricato dal classpath dell'applicazione.
     *
     * @param document Il documento {@link PDDocument} in cui caricare il logo.
     * @return Un oggetto {@link PDImageXObject} che rappresenta il logo di default del centro.
     * @throws IOException Se il file del logo di default non viene trovato, è vuoto/illeggibile,
     *                     o si verifica un altro errore di I/O durante il caricamento.
     */
    private static PDImageXObject loadDefaultCenterLogo(PDDocument document) throws IOException {
        try (InputStream logoStream = LogoLoader.class.getResourceAsStream(DEFAULT_CENTER_LOGO_FILENAME)) {
            if (logoStream == null) {
                logger.warn("File del logo del centro di default non trovato nel classpath: {}", DEFAULT_CENTER_LOGO_FILENAME);
                throw new FileNotFoundException("File del logo del centro di default non trovato nel classpath: " + DEFAULT_CENTER_LOGO_FILENAME);
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            if (buffer.size() == 0) {
                logger.warn("Il file del logo del centro di default ({}) è vuoto o illeggibile.", DEFAULT_CENTER_LOGO_FILENAME);
                throw new IOException("Il file del logo del centro di default è vuoto o illeggibile: " + DEFAULT_CENTER_LOGO_FILENAME);
            }

            PDImageXObject logoImage = PDImageXObject.createFromByteArray(document, buffer.toByteArray(), "DefaultCenterLogo");
            centerLogoWidth = logoImage.getWidth();
            centerLogoHeight = logoImage.getHeight();
            logger.info("Logo del centro di default ({}) caricato con successo.", DEFAULT_CENTER_LOGO_FILENAME);
            return logoImage;
        }
    }
}
