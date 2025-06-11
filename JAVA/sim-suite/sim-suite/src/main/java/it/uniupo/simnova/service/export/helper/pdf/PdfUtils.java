package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.service.export.LabExamPdfExportService;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe di utilità per operazioni comuni di disegno su documenti PDF con PDFBox.
 * Compatibile con la versione 2.x della libreria.
 */
public final class PdfUtils {

    private PdfUtils() {
        // Costruttore privato per prevenire l'istanza di classi di utilità
    }

    /**
     * Disegna una singola linea di testo senza andare a capo.
     *
     * @param font     Il font da utilizzare.
     * @param fontSize La dimensione del font.
     * @param x        La coordinata X di partenza.
     * @param y        La coordinata Y di partenza.
     * @param text     Il testo da disegnare.
     * @throws IOException Se si verifica un errore durante la scrittura.
     */
    public static void drawText(PDFont font, float fontSize, float x, float y, String text) throws IOException {
        PDPageContentStream stream = LabExamPdfExportService.currentContentStream;
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    /**
     * Disegna un testo su più linee, andando a capo se supera la larghezza massima specificata.
     * (Versione compatibile con PDFBox 2.x)
     *
     * @param font      Il font da utilizzare per il calcolo e il disegno.
     * @param fontSize  La dimensione del font.
     * @param text      Il testo da disegnare.
     * @param x         La coordinata X di partenza.
     * @param y         La coordinata Y di partenza.
     * @param maxWidth  La larghezza massima consentita per il blocco di testo.
     * @param draw      Se true, disegna il testo. Se false, calcola solo l'altezza e non disegna nulla.
     * @return L'altezza totale del blocco di testo.
     * @throws IOException Se si verifica un errore.
     */
    public static float drawWrappedText(PDFont font, float fontSize, String text, float x, float y, float maxWidth, boolean draw) throws IOException {
        float leading = fontSize * 1.2f; // Spaziatura tra le linee (interlinea)

        List<String> lines = new ArrayList<>();
        if (text == null) {
            text = "";
        }
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Calcola la larghezza della linea se aggiungessimo la prossima parola
            String potentialLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
            float width = font.getStringWidth(potentialLine) / 1000 * fontSize;

            if (width > maxWidth) {
                // La linea è piena. Aggiungi la linea corrente e inizia una nuova linea con la parola corrente.
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Caso estremo: una singola parola è più lunga della larghezza massima
                    lines.add(word);
                    currentLine = new StringBuilder(); // Inizia una linea vuota
                }
            } else {
                // La parola entra nella linea corrente
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        lines.add(currentLine.toString()); // Aggiunge l'ultima linea

        if (draw) {
            PDPageContentStream stream = LabExamPdfExportService.currentContentStream;
            stream.setFont(font, fontSize); // Imposta il font prima di disegnare
            stream.beginText();
            stream.setLeading(leading);
            stream.newLineAtOffset(x, y);
            for (String line : lines) {
                stream.showText(line);
                stream.newLine();
            }
            stream.endText();
        }

        return lines.size() * leading; // Ritorna l'altezza totale del blocco di testo
    }
}