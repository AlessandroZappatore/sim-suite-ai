package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static it.uniupo.simnova.service.export.PdfExportService.FONTBOLD;
import static it.uniupo.simnova.service.export.PdfExportService.FONTREGULAR;
import static it.uniupo.simnova.service.export.PdfExportService.checkForNewPage;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.BODY_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.MARGIN;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSubsection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawWrappedText;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.renderHtmlWithFormatting;

/**
 * Questa classe di utilità si occupa della generazione della sezione "Stato Paziente"
 * all'interno di un documento PDF. Permette di esportare i parametri vitali,
 * gli accessi venosi/arteriosi e i dettagli dell'esame fisico del paziente
 * associato a uno scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioPatient {
    /**
     * Logger per registrare le operazioni e gli errori durante la generazione del PDF.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioPatient.class);

    /**
     * Costruttore privato per evitare l'istanza della classe, dato che contiene solo metodi statici.
     */
    private ScenarioPatient() {
        // Costruttore privato per evitare l'istanza della classe.
    }

    /**
     * Crea la sezione "Stato Paziente" nel documento PDF.
     * Include i parametri vitali, gli accessi venosi/arteriosi e l'esame fisico
     * in base ai flag booleani forniti. La sezione viene aggiunta solo se almeno
     * una delle sue sottosezioni è abilitata.
     *
     * @param scenarioId         L'ID dello scenario di riferimento.
     * @param param              Un flag che indica se i parametri vitali del paziente devono essere inclusi.
     * @param acces              Un flag che indica se gli accessi venosi e arteriosi devono essere inclusi.
     * @param fisic              Un flag che indica se i dettagli dell'esame fisico devono essere inclusi.
     * @param pazienteT0Service  Il servizio {@link PazienteT0Service} per recuperare i dati del paziente.
     * @param esameFisicoService Il servizio {@link EsameFisicoService} per recuperare i dettagli dell'esame fisico.
     * @throws IOException In caso di errore durante la scrittura nel documento PDF.
     */
    public static void createPatientSection(Integer scenarioId, boolean param, boolean acces, boolean fisic, PazienteT0Service pazienteT0Service, EsameFisicoService esameFisicoService) throws IOException {
        // Se tutti i flag sono false, non c'è nulla da stampare in questa sezione.
        if (!param && !acces && !fisic) {
            return;
        }

        // Controlla se è necessario iniziare una nuova pagina per la sezione principale "Stato Paziente".
        checkForNewPage(LEADING * 3); // Spazio stimato per il titolo della sezione.

        // Disegna il titolo principale della sezione.
        drawSection("Stato Paziente", ""); // Il secondo parametro è vuoto perché i dettagli sono nelle sotto-sezioni.

        // Recupera i dati del paziente al tempo T0.
        PazienteT0 paziente = pazienteT0Service.getPazienteT0ById(scenarioId);

        // Se i dati del paziente sono disponibili, procede con le sottosezioni.
        if (paziente != null) {
            // Sottosezione: Parametri Vitali
            if (param) {
                checkForNewPage(LEADING * 3); // Spazio stimato per il titolo della sottosezione.
                drawSubsection("Parametri Vitali");

                // Stampa ogni parametro vitale su una nuova riga, con validazione dello spazio pagina.
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("PA: %s mmHg", paziente.getPA() != null ? paziente.getPA() : "-"));

                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("FC: %d bpm", paziente.getFC() != null ? paziente.getFC() : 0));

                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("RR: %d atti/min", paziente.getRR() != null ? paziente.getRR() : 0));

                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Temperatura: %.1f °C", paziente.getT()));

                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("SpO2: %d%%", paziente.getSpO2() != null ? paziente.getSpO2() : 0));

                // FiO2 (solo se il valore è maggiore di 0)
                if (paziente.getFiO2() != null && paziente.getFiO2() > 0) {
                    checkForNewPage(LEADING * 2);
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("FiO2: %d%%", paziente.getFiO2()));
                }

                // Litri O2 (solo se il valore è maggiore di 0)
                if (paziente.getLitriO2() != null && paziente.getLitriO2() > 0) {
                    checkForNewPage(LEADING * 2);
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Litri O2: %.1f L/min", paziente.getLitriO2()));
                }

                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("EtCO2: %d mmHg", paziente.getEtCO2() != null ? paziente.getEtCO2() : 0));

                // Monitor (solo se il testo è presente)
                if (paziente.getMonitor() != null && !paziente.getMonitor().isEmpty()) {
                    checkForNewPage(LEADING * 2);
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, String.format("Monitor: %s", paziente.getMonitor()));
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio extra dopo il blocco dei parametri vitali.
            }

            // Sottosezione: Accessi Venosi
            List<Accesso> accessiVenosi = paziente.getAccessiVenosi();
            if (accessiVenosi != null && !accessiVenosi.isEmpty() && acces) {
                checkForNewPage(LEADING * 3); // Spazio stimato per il titolo della sottosezione.
                drawSubsection("Accessi Venosi");

                for (Accesso accesso : accessiVenosi) {
                    checkForNewPage(LEADING * 2); // Spazio per ogni riga di accesso.
                    String accessoDesc = String.format("• %s - %s (%s) - %dG",
                            accesso.getTipologia(), accesso.getPosizione(), accesso.getLato(), accesso.getMisura());
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, accessoDesc);
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio extra dopo la sezione.
            }

            // Sottosezione: Accessi Arteriosi
            List<Accesso> accessiArteriosi = paziente.getAccessiArteriosi();
            if (accessiArteriosi != null && !accessiArteriosi.isEmpty() && acces) {
                checkForNewPage(LEADING * 3); // Spazio stimato per il titolo della sottosezione.
                drawSubsection("Accessi Arteriosi");

                for (Accesso accesso : accessiArteriosi) {
                    checkForNewPage(LEADING * 2); // Spazio per ogni riga di accesso.
                    String accessoDesc = String.format("• %s - %s (%s) - %dG",
                            accesso.getTipologia(), accesso.getPosizione(), accesso.getLato(), accesso.getMisura());
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, MARGIN + 20, accessoDesc);
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio extra dopo la sezione.
            }
        }

        // Sottosezione: Esame Fisico
        EsameFisico esame = esameFisicoService.getEsameFisicoById(scenarioId);
        // Inclusa solo se l'esame fisico esiste, ha sezioni e il flag 'fisic' è true.
        if (esame != null && esame.getSections() != null && !esame.getSections().isEmpty() && fisic) {
            // Verifica se tutte le sezioni dell'esame fisico sono vuote per evitare di stampare una sezione vuota.
            boolean allSectionsEmpty = esame.getSections().values().stream()
                    .allMatch(value -> value == null || value.trim().isEmpty());

            if (!allSectionsEmpty) {
                checkForNewPage(LEADING * 3); // Spazio stimato per il titolo della sottosezione.
                drawSubsection("Esame Fisico");

                Map<String, String> sections = esame.getSections();

                for (Map.Entry<String, String> entry : sections.entrySet()) {
                    String key = entry.getKey(); // Nome della sezione (es. "Torace")
                    String value = entry.getValue(); // Contenuto della sezione

                    // Stampa la sezione solo se sia la chiave che il valore sono validi.
                    if (key != null && !key.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                        checkForNewPage(LEADING * 4); // Spazio stimato per titolo sezione + testo.

                        // Titolo della sottosezione dell'esame (es. "Torace:").
                        drawWrappedText(FONTBOLD, BODY_FONT_SIZE, MARGIN + 20, key + ":");

                        // Contenuto della sezione, con gestione del formato HTML (es. <b>, <br>).
                        renderHtmlWithFormatting(value, MARGIN + 40); // Indentazione maggiore per il testo.

                        PdfExportService.currentYPosition -= LEADING; // Spazio tra le sezioni dell'esame fisico.
                    }
                }
                PdfExportService.currentYPosition -= LEADING; // Spazio extra dopo la sezione Esame Fisico.
            }
        }

        logger.info("Sezione 'Stato Paziente' creata con successo, con gestione granulare dei dettagli e dei salti pagina.");
    }
}