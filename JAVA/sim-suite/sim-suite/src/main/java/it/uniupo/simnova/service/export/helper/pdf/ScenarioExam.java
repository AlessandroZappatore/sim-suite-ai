package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.PdfExportService.FONTREGULAR;
import static it.uniupo.simnova.service.export.PdfExportService.checkForNewPage;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.BODY_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.MARGIN;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.SMALL_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.ReplaceSubscript.replaceSubscriptCharacters;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSubsection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawWrappedText;

/**
 * Questa classe si occupa della creazione della sezione "Esami e Referti"
 * all'interno di un documento PDF. Visualizza le informazioni dettagliate
 * di ogni esame, inclusi il tipo, il referto testuale e gli eventuali allegati media.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioExam {
    /**
     * Logger per registrare le operazioni e gli errori durante la creazione della sezione.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioExam.class);

    /**
     * Costruttore privato per evitare l'istanza della classe.
     * Questa classe contiene solo metodi statici e non necessita di un'istanza.
     */
    public ScenarioExam() {
        // Costruttore privato per evitare l'istanza della classe.
    }

    /**
     * Crea la sezione "Esami e Referti" nel documento PDF.
     * La sezione viene aggiunta solo se richiesto (parametro {@code esam} è <code>true</code>)
     * e se sono presenti esami per lo scenario specificato.
     *
     * @param scenarioId          L'ID dello scenario per cui recuperare gli esami.
     * @param esam                Un flag che indica se la sezione "Esami e Referti" deve essere inclusa nel PDF.
     * @param esameRefertoService Il servizio {@link EsameRefertoService} per recuperare la lista degli esami e referti.
     * @throws IOException Se si verifica un errore durante la scrittura nel documento PDF.
     */
    public static void createExamsSection(Integer scenarioId, boolean esam, EsameRefertoService esameRefertoService) throws IOException {
        List<EsameReferto> esami = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        // Se non ci sono esami o la sezione non deve essere stampata, termina.
        if (esami == null || esami.isEmpty() || !esam) {
            return;
        }

        // Controlla se è necessario iniziare una nuova pagina per questa sezione.
        // Viene allocato spazio per il titolo della sezione e un margine iniziale.
        checkForNewPage(LEADING * 5);

        // Disegna il titolo principale della sezione "Esami e Referti".
        drawSection("Esami e Referti", ""); // Il secondo parametro è vuoto perché i dettagli saranno sotto-sezioni.


        // Itera su ogni esame per disegnarne i dettagli.
        for (EsameReferto esame : esami) {
            String examType = getExamType(esame); // Recupera il tipo di esame, gestendo caratteri speciali.

            // Controlla se è necessario iniziare una nuova pagina per il prossimo esame.
            // Si alloca spazio per il titolo della sotto-sezione e un paio di righe.
            checkForNewPage(LEADING * 3);

            // Aggiunge la tipologia dell'esame come sotto-sezione (es. "ECG").
            drawSubsection(examType);

            // Aggiunge il referto testuale, se presente.
            if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                // Il testo viene disegnato con un margine sinistro maggiore per indentazione.
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, "Referto: " + esame.getRefertoTestuale());
            }

            // Aggiunge il nome del media allegato, se presente.
            if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                // Il nome del file media viene disegnato con un font più piccolo e indentato.
                drawWrappedText(FONTREGULAR, SMALL_FONT_SIZE, MARGIN + 20, "Allegato: " + esame.getMedia());
            }

            // Sposta la posizione corrente per il prossimo esame, aggiungendo uno spazio vuoto tra gli esami.
            PdfExportService.currentYPosition -= LEADING;
        }

        logger.info("Sezione Esami e Referti creata con successo.");
    }

    /**
     * Restituisce il tipo di esame, applicando una sostituzione dei caratteri
     * in apice e in pedice per garantire la compatibilità con il font del PDF.
     *
     * @param esame L'oggetto {@link EsameReferto} da cui estrarre il tipo di esame.
     * @return Il tipo di esame con i caratteri speciali sostituiti.
     */
    private static String getExamType(EsameReferto esame) {
        String examType = esame.getTipo();
        // Utilizza il metodo di utilità per sostituire i caratteri non supportati.
        return replaceSubscriptCharacters(examType);
    }
}