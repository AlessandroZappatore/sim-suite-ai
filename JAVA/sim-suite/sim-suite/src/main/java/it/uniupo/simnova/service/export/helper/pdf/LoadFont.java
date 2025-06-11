package it.uniupo.simnova.service.export.helper.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classe di supporto per il <strong>caricamento dei font</strong> all'interno dei documenti PDF.
 * Facilita l'integrazione di font TrueType personalizzati nei PDF generati,
 * garantendo coerenza estetica e corretta visualizzazione del testo.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class LoadFont {

    /**
     * Costruttore privato per impedire l'istanziazione di questa classe di utilità.
     * Tutti i metodi forniti sono statici e non richiedono un'istanza della classe.
     */
    private LoadFont() {
        // Costruttore privato per prevenire l'istanziazione
        throw new UnsupportedOperationException("Questa è una classe di utilità e non può essere istanziata");
    }

    /**
     * Carica un font TrueType da un file specificato, incorporandolo in un documento PDF.
     * Questo metodo cerca il file del font nel classpath dell'applicazione.
     *
     * @param document Il documento {@link PDDocument} in cui caricare il font.
     * @param fontPath Il percorso del file del font (es. "/fonts/MyFont.ttf").
     * @return Il font caricato, rappresentato come oggetto {@link PDFont}.
     * @throws IOException Se si verifica un errore durante la lettura o il caricamento del font,
     *                     o se il file del font non viene trovato.
     */
    public static PDFont loadFont(PDDocument document, String fontPath) throws IOException {
        // Tenta di ottenere l'InputStream del file del font dal classpath.
        try (InputStream fontStream = LoadFont.class.getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                // Se il font non è trovato, lancia un'eccezione.
                throw new IOException("Font file not found: " + fontPath);
            }
            // Carica il font nel documento PDF utilizzando PDType0Font.
            return PDType0Font.load(document, fontStream);
        }
    }
}