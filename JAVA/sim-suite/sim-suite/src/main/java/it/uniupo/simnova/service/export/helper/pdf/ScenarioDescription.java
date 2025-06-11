package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.Materiale;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;

/**
 * Questa classe si occupa della creazione delle varie <strong>sezioni descrittive</strong>
 * di uno scenario all'interno di un documento PDF. Permette di includere o escludere
 * specifiche parti della descrizione dello scenario basandosi su flag booleani.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioDescription {

    /**
     * Costruttore privato per evitare l'istanza della classe.
     * Questa classe contiene solo metodi statici e non necessita di un'istanza.
     */
    private ScenarioDescription() {
        // Costruttore privato per evitare l'istanza della classe
    }

    /**
     * Crea e disegna le sezioni della descrizione di uno scenario nel documento PDF.
     * Ogni sezione viene inclusa solo se il corrispondente flag booleano è <code>true</code>
     * e il contenuto non è vuoto.
     *
     * @param scenario            L'oggetto {@link Scenario} che contiene tutte le informazioni da stampare.
     * @param desc                Un flag che indica se la descrizione generale dello scenario deve essere stampata.
     * @param brief               Un flag che indica se il briefing dello scenario deve essere stampato.
     * @param infoGen             Un flag che indica se le informazioni dai genitori (per scenari pediatrici) devono essere stampate.
     * @param patto               Un flag che indica se il patto d'aula deve essere stampato.
     * @param azioni              Un flag che indica se le azioni chiave devono essere stampate.
     * @param obiettivi           Un flag che indica se gli obiettivi didattici devono essere stampati.
     * @param moula               Un flag che indica se il moulage deve essere stampato.
     * @param liqui               Un flag che indica se i liquidi e le dosi di farmaci devono essere stampati.
     * @param matNec              Un flag che indica se i materiali necessari devono essere stampati.
     * @param scenarioService     Il servizio {@link ScenarioService} per accedere ai dettagli dello scenario.
     * @param materialeService    Il servizio {@link MaterialeService} per accedere ai materiali necessari.
     * @param azioneChiaveService Il servizio {@link AzioneChiaveService} per accedere alle azioni chiave.
     * @throws IOException Se si verifica un errore durante la scrittura nel file PDF.
     */
    public static void createScenarioDescription(Scenario scenario, boolean desc, boolean brief, boolean infoGen, boolean patto, boolean azioni, boolean obiettivi, boolean moula, boolean liqui, boolean matNec, ScenarioService scenarioService, MaterialeService materialeService, AzioneChiaveService azioneChiaveService) throws IOException {
        // Sezione: Descrizione
        if (scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty() && desc) {
            drawSection("Descrizione", scenario.getDescrizione());
        }

        // Sezione: Briefing
        if (scenario.getBriefing() != null && !scenario.getBriefing().isEmpty() && brief) {
            drawSection("Briefing", scenario.getBriefing());
        }

        // Sezione: Informazioni dai genitori (solo per scenari pediatrici)
        if (scenarioService.isPediatric(scenario.getId()) && scenario.getInfoGenitore() != null && !scenario.getInfoGenitore().isEmpty() && infoGen) {
            drawSection("Informazioni dai genitori", scenario.getInfoGenitore());
        }

        // Sezione: Patto d'aula
        if (scenario.getPattoAula() != null && !scenario.getPattoAula().isEmpty() && patto) {
            drawSection("Patto d'Aula", scenario.getPattoAula());
        }

        // Sezione: Azioni chiave
        List<String> nomiAzioniChiave = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenario.getId());
        if (nomiAzioniChiave != null && !nomiAzioniChiave.isEmpty() && azioni) {
            StringBuilder azioniFormattate = new StringBuilder();
            // Aggiunge un bullet point per ogni azione chiave
            for (String azione : nomiAzioniChiave) {
                azioniFormattate.append("• ").append(azione).append("\n");
            }
            // Rimuove l'ultimo carattere di nuova riga se la stringa non è vuota
            if (!azioniFormattate.isEmpty()) {
                azioniFormattate.setLength(azioniFormattate.length() - 1);
            }
            drawSection("Azioni Chiave", azioniFormattate.toString());
        }

        // Sezione: Obiettivi didattici
        if (scenario.getObiettivo() != null && !scenario.getObiettivo().isEmpty() && obiettivi) {
            drawSection("Obiettivi Didattici", scenario.getObiettivo());
        }

        // Sezione: Moulage
        if (scenario.getMoulage() != null && !scenario.getMoulage().isEmpty() && moula) {
            drawSection("Moulage", scenario.getMoulage());
        }

        // Sezione: Liquidi e dosi farmaci
        if (scenario.getLiquidi() != null && !scenario.getLiquidi().isEmpty() && liqui) {
            drawSection("Liquidi e dosi farmaci", scenario.getLiquidi());
        }

        // Sezione: Materiale necessario
        List<Materiale> materialiNecessari = materialeService.getMaterialiByScenarioId(scenario.getId());
        if (materialiNecessari != null && !materialiNecessari.isEmpty() && matNec) {
            StringBuilder materialiNecessariFormattati = new StringBuilder();
            // Aggiunge un bullet point per ogni materiale necessario, con nome e descrizione
            for (Materiale materiale : materialiNecessari) {
                materialiNecessariFormattati.append("• ").append(materiale.getNome()).append(": ").append(materiale.getDescrizione()).append("\n");
            }
            // Rimuove l'ultimo carattere di nuova riga se la stringa non è vuota
            if (!materialiNecessariFormattati.isEmpty()) {
                materialiNecessariFormattati.setLength(materialiNecessariFormattati.length() - 1);
            }
            drawSection("Materiale necessario", materialiNecessariFormattati.toString());
        }
    }
}