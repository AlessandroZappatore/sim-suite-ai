package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.PdfExportService;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static it.uniupo.simnova.service.export.PdfExportService.FONTBOLD;
import static it.uniupo.simnova.service.export.PdfExportService.FONTREGULAR;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.BODY_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.HEADER_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.MARGIN;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.TITLE_FONT_SIZE;

/**
 * Questa classe è responsabile della creazione dell'<strong>intestazione del PDF</strong>
 * per i report degli scenari. Include il titolo del documento, il titolo dello scenario
 * e le informazioni principali come autori, target, tipologia, paziente e durata.
 *
 * @author Alessandro Zappatore
 * @version 1.2
 */
public class ScenarioHeader {
    /**
     * Logger per registrare le operazioni e gli errori durante la creazione dell'intestazione.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioHeader.class);

    /**
     * Costruttore privato per evitare l'istanza della classe, poiché contiene solo metodi statici.
     */
    private ScenarioHeader() {
        // Costruttore privato per evitare l'istanza della classe, dato che contiene solo metodi statici.
    }

    /**
     * Crea l'intestazione iniziale del documento PDF per uno scenario.
     * Questa include un titolo generale, il titolo specifico dello scenario
     * e un riepilogo delle sue informazioni chiave.
     *
     * @param scenario L'oggetto {@link Scenario} dal quale prelevare i dati per l'intestazione.
     * @throws IOException Se si verifica un errore durante la scrittura nel documento PDF o se il content stream non è stato inizializzato.
     */
    public static void createScenarioHeader(Scenario scenario) throws IOException {
        // Verifica che il PDPageContentStream sia stato inizializzato.
        if (PdfExportService.currentContentStream == null) {
            logger.error("PDPageContentStream is null before creating ScenarioHeader.");
            throw new IOException("PDF content stream not initialized.");
        }

        // Aggiunge il titolo generale del PDF.
        drawCenteredWrappedText(FONTBOLD, TITLE_FONT_SIZE, "Dettaglio Scenario");
        PdfExportService.currentYPosition -= LEADING * 2; // Spazio aggiuntivo dopo il titolo.

        // Aggiunge il titolo specifico dello scenario.
        drawCenteredWrappedText(FONTBOLD, HEADER_FONT_SIZE, scenario.getTitolo());
        PdfExportService.currentYPosition -= LEADING * 2; // Spazio aggiuntivo dopo il titolo dello scenario.

        // Aggiunge le informazioni principali dello scenario (etichetta: valore).
        drawTextWithWrapping(FONTREGULAR, "Autori: ", scenario.getAutori());
        drawTextWithWrapping(FONTREGULAR, "Target: ", scenario.getTarget());
        drawTextWithWrapping(FONTREGULAR, "Tipologia: ", scenario.getTipologia());
        drawTextWithWrapping(FONTREGULAR, "Paziente: ", scenario.getNomePaziente());
        // Gestisce il caso in cui la patologia sia nulla o vuota.
        drawTextWithWrapping(FONTREGULAR, "Patologia: ", scenario.getPatologia() != null && !scenario.getPatologia().isEmpty() ? scenario.getPatologia() : "-");
        // Gestisce il caso in cui la durata sia zero o negativa.
        drawTextWithWrapping(FONTREGULAR, "Durata: ", scenario.getTimerGenerale() > 0 ? scenario.getTimerGenerale() + " minuti" : "-");

        // Aggiorna la posizione verticale corrente dopo l'header.
        PdfExportService.currentYPosition -= LEADING;

        logger.info("Header dello scenario creato con successo.");
    }

    /**
     * Scrive un testo centrato nel PDF con gestione dell'andata a capo automatica.
     * Il testo viene suddiviso su più righe se supera la larghezza massima disponibile.
     *
     * @param font     Il {@link PDFont} da utilizzare per il testo.
     * @param fontSize La dimensione del font.
     * @param text     Il testo da aggiungere.
     * @throws IOException Se si verifica un errore durante la scrittura nel documento PDF.
     */
    private static void drawCenteredWrappedText(PDFont font, float fontSize, String text) throws IOException {
        // Se il testo è nullo o vuoto, non fa nulla.
        if (text == null || text.isEmpty()) {
            return;
        }

        float pageWidth = PDRectangle.A4.getWidth();
        float maxWidth = pageWidth - 2 * MARGIN; // Larghezza massima per il testo (larghezza pagina - doppi margini).

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" "); // Divide il testo in parole.
        StringBuilder currentLine = new StringBuilder();

        // Costruisce le righe, andando a capo quando la larghezza supera il limite.
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (testWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        // Aggiunge l'ultima riga se non è vuota.
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        // Scrive ogni riga centrata.
        for (String line : lines) {
            checkForNewPage(); // Controlla se è necessario iniziare una nuova pagina.
            float lineWidth = font.getStringWidth(line) / 1000 * fontSize;
            float xPosition = (pageWidth - lineWidth) / 2; // Calcola la posizione X per centrare il testo.

            PdfExportService.currentContentStream.setFont(font, fontSize);
            PdfExportService.currentContentStream.beginText();
            PdfExportService.currentContentStream.newLineAtOffset(xPosition, PdfExportService.currentYPosition);
            PdfExportService.currentContentStream.showText(line);
            PdfExportService.currentContentStream.endText();
            PdfExportService.currentYPosition -= LEADING; // Sposta il cursore verso il basso.
        }
    }

    /**
     * Scrive una coppia etichetta-valore nel PDF, con gestione dell'andata a capo automatica per il valore.
     * L'etichetta viene scritta in grassetto, mentre il valore utilizza il font specificato
     * e viene suddiviso su più righe se troppo lungo.
     *
     * @param font  Il {@link PDFont} da utilizzare per il valore (non per l'etichetta, che è sempre {@code FONTBOLD}).
     * @param label L'etichetta da visualizzare (es. "Autori: ").
     * @param text  Il valore associato all'etichetta.
     * @throws IOException Se si verifica un errore durante la scrittura nel documento PDF.
     */
    private static void drawTextWithWrapping(PDFont font, String label, String text) throws IOException {
        float pageWidth = PDRectangle.A4.getWidth();
        float labelWidth = FONTBOLD.getStringWidth(label) / 1000 * BODY_FONT_SIZE; // Larghezza dell'etichetta in grassetto.
        float textStartX = MARGIN + labelWidth; // Posizione X di inizio del testo del valore.
        float textMaxWidth = pageWidth - textStartX - MARGIN; // Larghezza massima disponibile per il testo del valore.

        checkForNewPage(); // Controlla se è necessario iniziare una nuova pagina prima di disegnare.

        // Disegna l'etichetta in grassetto.
        PdfExportService.currentContentStream.setFont(FONTBOLD, BODY_FONT_SIZE);
        PdfExportService.currentContentStream.beginText();
        PdfExportService.currentContentStream.newLineAtOffset(MARGIN, PdfExportService.currentYPosition);
        PdfExportService.currentContentStream.showText(label);
        PdfExportService.currentContentStream.endText();

        // Se il testo del valore è nullo o vuoto, lo imposta a un trattino.
        if (text == null || text.isEmpty()) {
            text = "-";
        }

        // Suddivide il testo del valore in righe.
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            float testWidth = font.getStringWidth(testLine) / 1000 * BODY_FONT_SIZE;

            if (testWidth > textMaxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        boolean firstLine = true;
        for (String line : lines) {
            if (!firstLine) {
                checkForNewPage(); // Per le righe successive, controlla la nuova pagina prima di spostare.
            }

            PdfExportService.currentContentStream.setFont(font, BODY_FONT_SIZE);
            PdfExportService.currentContentStream.beginText();

            if (firstLine) {
                // La prima riga del valore inizia dopo l'etichetta.
                PdfExportService.currentContentStream.newLineAtOffset(textStartX, PdfExportService.currentYPosition);
                firstLine = false;
            } else {
                // Le righe successive del valore iniziano dal margine standard.
                PdfExportService.currentContentStream.newLineAtOffset(MARGIN, PdfExportService.currentYPosition);
            }
            PdfExportService.currentContentStream.showText(line);
            PdfExportService.currentContentStream.endText();

            PdfExportService.currentYPosition -= LEADING; // Sposta il cursore verso il basso.
        }
    }

    /**
     * Verifica se la posizione verticale corrente nel PDF è sufficiente per scrivere una nuova riga di testo.
     * Se lo spazio non è sufficiente, inizia una nuova pagina e riposiziona il cursore in cima.
     *
     * @throws IOException Se si verifica un errore durante la creazione della nuova pagina.
     */
    private static void checkForNewPage() throws IOException {
        // Se lo spazio rimanente è inferiore al LEADING (altezza di una riga) più il margine inferiore,
        // allora serve una nuova pagina.
        if (PdfExportService.currentYPosition - LEADING < MARGIN) {
            PdfExportService.initNewPage(); // Inizializza una nuova pagina.
            PdfExportService.currentYPosition = PDRectangle.A4.getHeight() - MARGIN; // Riposiziona il cursore.
        }
    }
}