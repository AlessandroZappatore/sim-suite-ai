package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.PdfExportService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.PdfExportService.FONTBOLD;
import static it.uniupo.simnova.service.export.PdfExportService.FONTREGULAR;
import static it.uniupo.simnova.service.export.PdfExportService.checkForNewPage;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.BODY_FONT_SIZE;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.LEADING;
import static it.uniupo.simnova.service.export.helper.pdf.PdfConstant.MARGIN;
import static it.uniupo.simnova.service.export.helper.pdf.ReplaceSubscript.replaceSubscriptCharacters;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSubsection;
import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawWrappedText;

/**
 * Questa classe di utilità è responsabile della generazione della sezione <strong>"Timeline"</strong>
 * all'interno di un documento PDF. Esporta la sequenza temporale (tempi) di uno scenario avanzato,
 * con tutti i relativi parametri vitali, eventuali parametri aggiuntivi, dettagli specifici e azioni.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioTimeline {

    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    public ScenarioTimeline() {
        // Costruttore privato per evitare l'istanza della classe.
    }

    /**
     * Crea la sezione "Timeline" nel documento PDF.
     * Questa sezione include tutti i tempi definiti per lo scenario,
     * presentandoli con i loro parametri vitali, i parametri aggiuntivi,
     * i dettagli e le azioni/transizioni.
     *
     * @param scenario                L'oggetto {@link Scenario} di riferimento.
     * @param advancedScenarioService Il servizio {@link AdvancedScenarioService} per recuperare i dati avanzati dello scenario.
     * @param scenarioService         Il servizio {@link ScenarioService} per ottenere informazioni aggiuntive sullo scenario (es. se è pediatrico).
     * @throws IOException In caso di errore durante la scrittura nel documento PDF.
     */
    public static void createTimelineSection(Scenario scenario, AdvancedScenarioService advancedScenarioService, ScenarioService scenarioService) throws IOException {
        // Recupera la lista dei tempi associati allo scenario.
        List<Tempo> tempi = advancedScenarioService.getTempiByScenarioId(scenario.getId());

        // Se non ci sono tempi definiti per lo scenario, la sezione non viene creata.
        if (tempi.isEmpty()) {
            return;
        }

        // Calcola lo spazio necessario per il titolo della sezione e il titolo del primo tempo
        // per decidere se è necessario un salto pagina.
        float spazioTitoloSezione = LEADING * 3;
        float spazioTitoloPrimoTempo = LEADING * 3;
        checkForNewPage(spazioTitoloSezione + spazioTitoloPrimoTempo);

        // Disegna il titolo principale della sezione "Timeline".
        drawSection("Timeline", "");

        // Itera su ogni oggetto Tempo per stampare i suoi dettagli nel PDF.
        for (int i = 0; i < tempi.size(); i++) {
            Tempo tempo = tempi.get(i);

            // Costruisce il titolo del tempo (es. "Tempo 1 (2.0 min)").
            String title = String.format("Tempo %d (%.1f min)",
                    tempo.getIdTempo(),
                    tempo.getTimerTempo() / 60.0 // Converte i secondi in minuti.
            );
            // Verifica lo spazio per il titolo del tempo e lo disegna come sotto-sezione.
            checkForNewPage(LEADING * 3);
            drawSubsection(title);

            float paramsIndent = MARGIN + 20; // Definisce l'indentazione per i parametri.

            // Stampa i parametri vitali principali, uno per riga, con gestione del salto pagina.
            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("PA: %s mmHg", tempo.getPA() != null ? tempo.getPA() : "-"));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("FC: %d bpm", tempo.getFC() != null ? tempo.getFC() : 0));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("RR: %d atti/min", tempo.getRR() != null ? tempo.getRR() : 0));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("Temperatura: %.1f °C", tempo.getT()));

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("SpO2: %d%%", tempo.getSpO2() != null ? tempo.getSpO2() : 0));

            // FiO2 (stampato solo se il valore è presente e maggiore di 0).
            Number fio2 = tempo.getFiO2();
            if (fio2 != null && fio2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("FiO2: %.0f%%", fio2.doubleValue()));
            }

            // Litri O2 (stampato solo se il valore è presente e maggiore di 0).
            Number litriO2 = tempo.getLitriO2();
            if (litriO2 != null && litriO2.doubleValue() > 0) {
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("Litri O2: %.1f L/min", litriO2.doubleValue()));
            }

            checkForNewPage(LEADING * 2);
            drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent, String.format("EtCO2: %d mmHg", tempo.getEtCO2() != null ? tempo.getEtCO2() : 0));

            // Parametri aggiuntivi (se presenti).
            List<ParametroAggiuntivo> parametriAggiuntivo = advancedScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenario.getId());
            if (!parametriAggiuntivo.isEmpty()) {
                for (ParametroAggiuntivo parametro : parametriAggiuntivo) {
                    checkForNewPage(LEADING * 2);
                    // Applica la sostituzione dei caratteri speciali ai nomi e unità di misura dei parametri aggiuntivi.
                    String parametroNome = replaceSubscriptCharacters(parametro.getNome());
                    String parametroUnita = replaceSubscriptCharacters(parametro.getUnitaMisura());
                    drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, paramsIndent,
                            String.format("%s: %s %s", parametroNome, parametro.getValore(), parametroUnita));
                }
            }
            // Spazio verticale dopo il blocco dei parametri.
            PdfExportService.currentYPosition -= LEADING;

            float detailsLabelIndent = MARGIN + 20; // Indentazione per le etichette dei dettagli.
            float detailsTextIndent = MARGIN + 30; // Indentazione per il testo dei dettagli.

            // Stampa i dettagli aggiuntivi del tempo, se presenti.
            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Dettagli:");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, tempo.getAltriDettagli());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Stampa il ruolo del genitore, se lo scenario è pediatrico e il ruolo è definito.
            if (scenarioService.isPediatric(scenario.getId()) && tempo.getRuoloGenitore() != null && !tempo.getRuoloGenitore().isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Ruolo del genitore:");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, tempo.getRuoloGenitore());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Stampa le azioni da svolgere per passare al tempo "se SI".
            String azione = tempo.getAzione();
            if (azione != null && !azione.isEmpty()) {
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Azioni da svolgere per passare a → T" + tempo.getTSi() + ":");
                checkForNewPage(LEADING * 2);
                drawWrappedText(FONTREGULAR, BODY_FONT_SIZE, detailsTextIndent, azione);
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Stampa il riferimento al tempo "se NO", se definito.
            if (tempo.getTNo() >= 0) { // Un valore >= 0 indica una transizione definita (anche se a T0)
                checkForNewPage(LEADING * 3);
                drawWrappedText(FONTBOLD, BODY_FONT_SIZE, detailsLabelIndent, "Se non vengono svolte le azioni passare a → T" + tempo.getTNo());
                PdfExportService.currentYPosition -= LEADING / 2;
            }

            // Aggiunge uno spazio extra tra un tempo e il successivo, tranne che dopo l'ultimo tempo.
            if (i < tempi.size() - 1) {
                checkForNewPage(LEADING * 2);
                PdfExportService.currentYPosition -= LEADING;
            }
        }
    }
}