package it.uniupo.simnova.service.export.helper.pdf;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static it.uniupo.simnova.service.export.PdfExportService.FONTBOLD;
import static it.uniupo.simnova.service.export.PdfExportService.FONTBOLDITALIC;
import static it.uniupo.simnova.service.export.PdfExportService.FONTITALIC;
import static it.uniupo.simnova.service.export.PdfExportService.FONTREGULAR;
import static it.uniupo.simnova.service.export.PdfExportService.checkForNewPage;
import static it.uniupo.simnova.service.export.PdfExportService.currentContentStream;
import static it.uniupo.simnova.service.export.PdfExportService.currentYPosition;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.BODY_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.HEADER_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.MARGIN;

/**
 * Questa classe di utilità si occupa del disegno di varie sezioni e sottosezioni
 * all'interno di un documento PDF. Permette di gestire il testo formattato,
 * inclusa la resa HTML (grassetto, corsivo) e il wrapping automatico del testo
 * su più righe.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class SectionDrawer {

    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    private SectionDrawer() {
        // Costruttore privato per evitare l'istanza della classe.
    }

    /**
     * Disegna una <strong>sezione principale</strong> nel documento PDF con un titolo e un contenuto.
     * Il titolo viene stampato in grassetto. Il contenuto viene renderizzato con formattazione
     * HTML (se applicabile per la sezione) o come testo semplice con wrapping automatico.
     *
     * @param title   Il titolo della sezione.
     * @param content Il contenuto testuale della sezione.
     * @throws IOException In caso di errore durante la scrittura nel documento PDF.
     */
    public static void drawSection(String title, String content) throws IOException {
        // Imposta il font e la dimensione per il titolo della sezione.
        currentContentStream.setFont(FONTBOLD, HEADER_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN, currentYPosition);
        currentContentStream.showText(title);
        currentContentStream.endText();
        // Sposta la posizione verticale dopo il titolo, aggiungendo più spazio.
        currentYPosition -= LEADING * 1.5f;

        // Se il contenuto non è nullo o vuoto, procede a disegnarlo.
        if (content != null && !content.isEmpty()) {
            // Determina se la sezione richiede l'interpretazione di tag HTML.
            if (title.equals("Descrizione")
                    || title.equals("Briefing")
                    || title.equals("Informazioni dai genitori")
                    || title.equals("Patto d'Aula")
                    || title.equals("Obiettivi Didattici")
                    || title.equals("Moulage")
                    || title.equals("Liquidi e dosi farmaci")
            ) {
                // Renderizza il contenuto interpretando la formattazione HTML.
                renderHtmlWithFormatting(content, MARGIN + 20); // Indentazione per il contenuto.
                currentYPosition -= LEADING / 2; // Spazio aggiuntivo dopo il contenuto formattato.
            } else {
                // Renderizza il contenuto come testo semplice con wrapping.
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 10, content);
            }
            // Spazio finale dopo il contenuto della sezione.
            currentYPosition -= LEADING;
        }
    }

    /**
     * Esegue il rendering di testo HTML all'interno del PDF, applicando la formattazione
     * (grassetto e corsivo) e gestendo il wrapping automatico del testo.
     * Questo metodo analizza il contenuto HTML per identificare gli stili.
     *
     * @param htmlContent Il contenuto testuale in formato HTML da renderizzare.
     * @param xOffset     L'offset orizzontale (coordinata X) da cui iniziare a disegnare il testo.
     * @throws IOException In caso di errore durante la scrittura nel documento PDF.
     */
    public static void renderHtmlWithFormatting(String htmlContent, float xOffset) throws IOException {
        // Parsing del contenuto HTML per estrarre il testo semplice e identificare gli elementi di formattazione.
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        String plainText = doc.text(); // Testo senza tag HTML.

        // Seleziona tutti gli elementi che rappresentano il grassetto (<strong>, <b>).
        Elements boldElements = doc.select("strong, b");
        // Seleziona tutti gli elementi che rappresentano il corsivo (<em>, <i>).
        Elements italicElements = doc.select("em, i");

        // Mappe per memorizzare gli intervalli di testo che devono essere in grassetto o corsivo.
        // La chiave è l'indice di inizio nel plainText, il valore è l'indice di fine.
        Map<Integer, Integer> boldRanges = new HashMap<>();
        Map<Integer, Integer> italicRanges = new HashMap<>();

        // Calcola gli intervalli di grassetto nel testo semplice.
        for (org.jsoup.nodes.Element element : boldElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) { // Assicura che l'elemento sia stato trovato nel plainText.
                boldRanges.put(start, start + text.length());
            }
        }

        // Calcola gli intervalli di corsivo nel testo semplice.
        for (Element element : italicElements) {
            String text = element.text();
            int start = plainText.indexOf(text);
            if (start >= 0) {
                italicRanges.put(start, start + text.length());
            }
        }

        // Divide il testo semplice in parole per gestire il wrapping.
        String[] words = plainText.split(" ");
        StringBuilder currentLine = new StringBuilder(); // Costruisce la riga corrente.
        int charCount = 0; // Contatore globale dei caratteri nel plainText per tracciare la posizione.

        // Inizia un nuovo blocco di testo per la scrittura nel PDF.
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(xOffset, currentYPosition); // Imposta la posizione iniziale della riga.

        // Itera su ogni parola per costruire le righe e applicare il wrapping.
        for (String word : words) {
            String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
            // Calcola la larghezza della riga di prova.
            float width = FONTREGULAR.getStringWidth(testLine) / 1000 * BODY_FONT_SIZE;

            // Se la larghezza della riga di prova supera la larghezza massima consentita,
            // stampa la riga corrente e inizia una nuova riga.
            if (width > (PDRectangle.A4.getWidth() - 2 * MARGIN - 10)) {
                // Disegna la riga formattata prima di andare a capo.
                drawFormattedLine(currentLine.toString(), charCount - currentLine.length(), boldRanges, italicRanges);

                currentContentStream.endText(); // Termina il blocco di testo corrente.
                currentYPosition -= LEADING; // Sposta il cursore verso il basso.
                checkForNewPage(LEADING); // Controlla se è necessaria una nuova pagina.

                currentContentStream.beginText(); // Inizia un nuovo blocco di testo sulla nuova posizione.
                currentContentStream.newLineAtOffset(xOffset, currentYPosition); // Imposta la posizione per la nuova riga.
                currentLine = new StringBuilder(word); // Inizia la nuova riga con la parola corrente.
                charCount += 1; // Contabilizza lo spazio che precede la parola nella nuova riga.
            } else {
                // Aggiunge la parola alla riga corrente.
                if (!currentLine.isEmpty()) {
                    currentLine.append(" ");
                    charCount++; // Contabilizza lo spazio.
                }
                currentLine.append(word);
            }
            charCount += word.length(); // Aggiorna il contatore globale dei caratteri.
        }

        // Stampa l'ultima riga del testo se non è vuota.
        if (!currentLine.isEmpty()) {
            drawFormattedLine(currentLine.toString(), charCount - currentLine.length(), boldRanges, italicRanges);
        }
        currentContentStream.endText(); // Termina il blocco di testo finale.
    }

    /**
     * Disegna una singola riga di testo applicando la formattazione (grassetto, corsivo o entrambi)
     * in base agli intervalli specificati. Il testo viene suddiviso in parti con formattazione uniforme.
     *
     * @param line         La riga di testo da stampare.
     * @param startPos     La posizione iniziale globale (nel testo originale) del primo carattere di questa riga.
     * @param boldRanges   Una mappa degli intervalli di testo da formattare in grassetto.
     * @param italicRanges Una mappa degli intervalli di testo da formattare in corsivo.
     * @throws IOException In caso di errore durante la scrittura nel documento PDF.
     */
    private static void drawFormattedLine(String line, int startPos, Map<Integer, Integer> boldRanges,
                                          Map<Integer, Integer> italicRanges) throws IOException {

        StringBuilder currentPart = new StringBuilder(); // Costruisce la parte di testo con formattazione uniforme.

        // Itera su ogni carattere della riga per determinare la formattazione.
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            int globalPos = startPos + i; // Posizione del carattere nel testo originale non formattato.

            boolean isBold = isBold(globalPos, boldRanges);
            boolean isItalic = isItalic(globalPos, italicRanges);

            currentPart.append(c); // Aggiunge il carattere alla parte corrente.

            // Condizione per stampare la parte corrente:
            // 1. È l'ultimo carattere della riga.
            // 2. La formattazione del prossimo carattere è diversa dalla formattazione corrente.
            if (i == line.length() - 1 ||
                    isBold(globalPos + 1, boldRanges) != isBold ||
                    isItalic(globalPos + 1, italicRanges) != isItalic) {

                PDFont font = getPdFont(isBold, isItalic); // Ottiene il font corretto (Regular, Bold, Italic, BoldItalic).

                currentContentStream.setFont(font, BODY_FONT_SIZE); // Imposta il font per la parte da stampare.
                // currentContentStream.showText richiede che si stia dentro un blocco beginText/endText.
                // Il calcolo della larghezza qui non è necessario per la showText, ma serve per il debugging.
                font.getStringWidth(currentPart.toString());
                currentContentStream.showText(currentPart.toString()); // Stampa la parte di testo.

                currentPart = new StringBuilder(); // Resetta il builder per la prossima parte.
            }
        }
    }

    /**
     * Restituisce il font PDF appropriato (regular, bold, italic, bold-italic)
     * in base ai flag booleani per grassetto e corsivo.
     *
     * @param isBold   <code>true</code> se il testo deve essere in grassetto.
     * @param isItalic <code>true</code> se il testo deve essere in corsivo.
     * @return L'oggetto {@link PDFont} corrispondente alla formattazione richiesta.
     */
    private static PDFont getPdFont(boolean isBold, boolean isItalic) {
        PDFont font = FONTREGULAR; // Font predefinito.
        if (isBold && isItalic) {
            font = FONTBOLDITALIC;
        } else if (isBold) {
            font = FONTBOLD;
        } else if (isItalic) {
            font = FONTITALIC;
        }
        return font;
    }

    /**
     * Verifica se una data posizione all'interno del testo rientra in un intervallo marcato come grassetto.
     *
     * @param position   La posizione (indice del carattere) da verificare nel testo originale.
     * @param boldRanges Una mappa che definisce gli intervalli di testo da formattare in grassetto.
     * @return <code>true</code> se la posizione rientra in uno degli intervalli in grassetto, <code>false</code> altrimenti.
     */
    private static boolean isBold(int position, Map<Integer, Integer> boldRanges) {
        for (Map.Entry<Integer, Integer> range : boldRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se una data posizione all'interno del testo rientra in un intervallo marcato come corsivo.
     *
     * @param position     La posizione (indice del carattere) da verificare nel testo originale.
     * @param italicRanges Una mappa che definisce gli intervalli di testo da formattare in corsivo.
     * @return <code>true</code> se la posizione rientra in uno degli intervalli in corsivo, <code>false</code> altrimenti.
     */
    private static boolean isItalic(int position, Map<Integer, Integer> italicRanges) {
        for (Map.Entry<Integer, Integer> range : italicRanges.entrySet()) {
            if (position >= range.getKey() && position < range.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Disegna un blocco di testo nel documento PDF con <strong>wrapping automatico</strong> delle parole,
     * andando a capo quando il testo supera la larghezza massima della riga.
     * Gestisce anche i ritorni a capo espliciti (<code>\n</code>) presenti nel testo.
     *
     * @param font     Il {@link PDFont} da utilizzare per il testo.
     * @param fontSize La dimensione del font.
     * @param x        L'offset orizzontale (coordinata X) da cui iniziare a disegnare il testo.
     * @param text     Il testo da stampare.
     * @throws IOException In caso di errore durante la scrittura nel documento PDF.
     */
    public static void drawWrappedText(PDFont font, float fontSize, float x, String text) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }

        // Suddivide il testo in righe basandosi sui caratteri di nuova riga espliciti.
        String[] lines = text.split("\n");

        for (String line : lines) {
            // Controlla se è necessario andare a una nuova pagina prima di stampare la riga.
            checkForNewPage(LEADING);

            currentContentStream.setFont(font, fontSize);

            // Suddivide la riga corrente in parole per gestire il wrapping.
            String[] words = line.split(" ");
            StringBuilder currentLine = new StringBuilder(); // Costruisce la riga di testo da stampare.

            currentContentStream.beginText(); // Inizia un blocco di testo.
            currentContentStream.newLineAtOffset(x, currentYPosition); // Imposta la posizione iniziale della riga.

            // Itera su ogni parola per costruire le righe, andando a capo se necessario.
            for (String word : words) {
                String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;
                // Calcola la larghezza della riga di prova.
                float width = font.getStringWidth(testLine) / 1000 * fontSize;

                // Se la riga di prova supera la larghezza massima consentita,
                // stampa la riga corrente e inizia una nuova riga.
                if (width > (PDRectangle.A4.getWidth() - 2 * MARGIN - 10)) { // Larghezza massima della riga.
                    currentContentStream.showText(currentLine.toString()); // Stampa la parte di riga che entra.
                    currentContentStream.endText(); // Termina il blocco di testo.

                    currentYPosition -= LEADING; // Sposta il cursore verso il basso.

                    checkForNewPage(LEADING); // Controlla per una nuova pagina.

                    currentContentStream.setFont(font, fontSize); // Reimposta il font.

                    currentContentStream.beginText(); // Inizia un nuovo blocco di testo.
                    currentContentStream.newLineAtOffset(x, currentYPosition); // Sposta il cursore alla nuova posizione.
                    currentLine = new StringBuilder(word); // Inizia la nuova riga con la parola corrente.
                } else {
                    // Aggiunge la parola alla riga corrente.
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }

            // Stampa l'ultima parte della riga (o l'intera riga se non ha wrappato).
            if (!currentLine.isEmpty()) {
                currentContentStream.showText(currentLine.toString());
            }
            currentContentStream.endText(); // Termina il blocco di testo.
            currentYPosition -= LEADING; // Sposta il cursore verso il basso per la prossima riga di contenuto.
        }
    }

    /**
     * Disegna una <strong>sottosezione</strong> con un titolo nel documento PDF.
     * Il titolo viene stampato in grassetto con un font leggermente più piccolo
     * e indentato rispetto al margine principale. Viene inserita anche una riga
     * vuota dopo il titolo per una migliore separazione visiva.
     *
     * @param title Il titolo della sottosezione.
     * @throws IOException In caso di errore durante la scrittura nel documento PDF.
     */
    public static void drawSubsection(String title) throws IOException {
        // Imposta font e posizione per il titolo della sottosezione.
        currentContentStream.setFont(FONTBOLD, BODY_FONT_SIZE);
        currentContentStream.beginText();
        currentContentStream.newLineAtOffset(MARGIN + 10, currentYPosition); // Indentazione maggiore.
        currentContentStream.showText(title);
        currentContentStream.endText();
        currentYPosition -= LEADING; // Sposta il cursore dopo il titolo.

        // Inserisce una riga vuota dopo il titolo della sottosezione per la separazione.
        drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, ""); // Utilizza un'ulteriore indentazione.
    }
}