package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.respons_model.LabCategory;
import it.uniupo.simnova.domain.respons_model.LabExamSet;
import it.uniupo.simnova.domain.respons_model.LabTest;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.LabExamPdfExportService;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.*;
// Assicurati che PdfUtils sia nello stesso package e che i metodi siano statici
import static it.uniupo.simnova.service.export.helper.pdf.PdfUtils.*;

/**
 * Classe di utilità per la generazione della sezione degli esami di laboratorio in un PDF.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class LabExamPdfHelper {
    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    private LabExamPdfHelper() {
    }

    /**
     * Crea la sezione completa del referto degli esami di laboratorio nel PDF.
     *
     * @param labExamSet I dati degli esami da stampare.
     * @param scenario   I dati dello scenario per l'intestazione (es. nome paziente).
     * @throws IOException Se si verifica un errore durante la scrittura nel PDF.
     */
    public static void createLabExamSection(LabExamSet labExamSet, Scenario scenario) throws IOException {
        LabExamPdfExportService.checkForNewPage(200);

        // Titolo del referto
        drawText(LabExamPdfExportService.FONTBOLD, 18, MARGIN, LabExamPdfExportService.currentYPosition, "Referto Esami di Laboratorio");
        LabExamPdfExportService.currentYPosition -= LEADING * 2;

        // Aggiunge data e ora correnti per completezza
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataOra = sdf.format(new java.util.Date());
        drawText(LabExamPdfExportService.FONTREGULAR, 10, MARGIN, LabExamPdfExportService.currentYPosition, "Data referto: " + dataOra);
        LabExamPdfExportService.currentYPosition -= LEADING * 1.5f;

        // Dati del paziente
        drawText(LabExamPdfExportService.FONTREGULAR, 12, MARGIN, LabExamPdfExportService.currentYPosition, "Paziente: " + scenario.getNomePaziente());
        LabExamPdfExportService.currentYPosition -= LEADING * 2;

        float tableWidth = PDRectangle.A4.getWidth() - 2 * MARGIN;
        float[] columnWidths = {tableWidth * 0.35f, tableWidth * 0.15f, tableWidth * 0.20f, tableWidth * 0.30f};

        for (LabCategory category : labExamSet.getCategorie()) {
            LabExamPdfExportService.checkForNewPage(100);

            drawText(LabExamPdfExportService.FONTBOLD, 14, MARGIN, LabExamPdfExportService.currentYPosition, category.getNomeCategoria());
            LabExamPdfExportService.currentYPosition -= LEADING * 1.5f;

            drawTableHeader(columnWidths);

            for (LabTest test : category.getTest()) {
                LabExamPdfExportService.checkForNewPage(40);
                drawTableRow(test, columnWidths);
            }
            LabExamPdfExportService.currentYPosition -= LEADING;
        }
    }

    /**
     * Disegna l'intestazione della tabella per gli esami di laboratorio.
     *
     * @param columnWidths Le larghezze delle colonne della tabella.
     * @throws IOException Se si verifica un errore durante la scrittura nel PDF.
     */
    private static void drawTableHeader(float[] columnWidths) throws IOException {
        PDPageContentStream stream = LabExamPdfExportService.currentContentStream;
        float x = MARGIN;
        String[] headers = {"Esame", "Valore", "Unità di Misura", "Range di Riferimento"};
        for (int i = 0; i < headers.length; i++) {
            // Utilizza la funzione di utilità per coerenza
            drawText(LabExamPdfExportService.FONTBOLD, 10, x + 2, LabExamPdfExportService.currentYPosition, headers[i]);
            x += columnWidths[i];
        }
        LabExamPdfExportService.currentYPosition -= LEADING;
        // Disegna una linea sotto l'header
        stream.moveTo(MARGIN, LabExamPdfExportService.currentYPosition);
        stream.lineTo(MARGIN + PDRectangle.A4.getWidth() - 2 * MARGIN, LabExamPdfExportService.currentYPosition);
        stream.stroke();
        LabExamPdfExportService.currentYPosition -= 10;
    }

    /**
     * Disegna una riga della tabella per un test di laboratorio.
     *
     * @param test         Il test da disegnare.
     * @param columnWidths Le larghezze delle colonne della tabella.
     * @throws IOException Se si verifica un errore durante la scrittura nel PDF.
     */
    private static void drawTableRow(LabTest test, float[] columnWidths) throws IOException {
        // Definisce font e dimensione per questa riga
        final float fontSize = 10;
        final PDFont font = LabExamPdfExportService.FONTREGULAR;

        float x = MARGIN;
        float y = LabExamPdfExportService.currentYPosition;

        String[] rowData = {
                test.getNome(),
                test.getValore(),
                test.getUnitaMisura(),
                test.getRangeRiferimento()
        };

        float maxHeight = 0;

        // 1. Calcola l'altezza massima della riga senza disegnare il testo
        for (int i = 0; i < rowData.length; i++) {
            // Passa esplicitamente font e dimensione alla funzione di utilità
            float cellHeight = drawWrappedText(font, fontSize, rowData[i], 0, 0, columnWidths[i] - 4, false);
            if (cellHeight > maxHeight) {
                maxHeight = cellHeight;
            }
        }

        // 2. Ora disegna il testo, allineato verticalmente, sapendo l'altezza necessaria
        for (int i = 0; i < rowData.length; i++) {
            // Passa esplicitamente font e dimensione per disegnare
            drawWrappedText(font, fontSize, rowData[i], x + 2, y, columnWidths[i] - 4, true);
            x += columnWidths[i];
        }

        // Aggiorna la posizione Y in base all'altezza della riga più alta
        LabExamPdfExportService.currentYPosition -= (maxHeight + 10);
    }
}