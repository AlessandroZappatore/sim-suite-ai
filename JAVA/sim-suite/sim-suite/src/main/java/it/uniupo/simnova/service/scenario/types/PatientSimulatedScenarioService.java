package it.uniupo.simnova.service.scenario.types;

import it.uniupo.simnova.domain.scenario.PatientSimulatedScenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Servizio per la gestione degli scenari simulati con paziente.
 * Questo servizio estende le funzionalità di {@link AdvancedScenarioService} per gestire
 * logiche e dati specifici degli scenari in cui è prevista l'interazione con un paziente simulato,
 * come la sceneggiatura.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class PatientSimulatedScenarioService {

    /**
     * Il logger per questa classe, utilizzato per registrare le operazioni e gli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(PatientSimulatedScenarioService.class);

    /**
     * Il servizio per la gestione degli scenari avanzati, poiché gli scenari simulati con paziente sono un'estensione di questi.
     */
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Il servizio di base per la gestione degli scenari, utilizzato per funzionalità generiche.
     */
    private final ScenarioService scenarioService;

    /**
     * Costruisce una nuova istanza di <code>PatientSimulatedScenarioService</code>.
     * Inietta i servizi dipendenti.
     *
     * @param advancedScenarioService Il servizio per la gestione degli scenari avanzati.
     * @param scenarioService         Il servizio per la gestione degli scenari generali.
     */
    public PatientSimulatedScenarioService(AdvancedScenarioService advancedScenarioService, ScenarioService scenarioService) {
        this.advancedScenarioService = advancedScenarioService;
        this.scenarioService = scenarioService;
    }

    /**
     * Avvia la creazione di un nuovo scenario simulato con paziente.
     * Questo metodo prima crea un record base dello scenario tramite {@link AdvancedScenarioService#startAdvancedScenario},
     * quindi aggiunge un record nella tabella <code>PatientSimulatedScenario</code> associandolo all'ID dello scenario avanzato.
     *
     * @param titolo        Il titolo dello scenario.
     * @param nomePaziente  Il nome del paziente associato allo scenario.
     * @param patologia     La patologia del paziente.
     * @param autori        Gli autori dello scenario.
     * @param timerGenerale Il timer generale preimpostato per lo scenario.
     * @param tipologia     La tipologia specifica dello scenario (dovrebbe essere "Patient Simulated Scenario").
     * @return L'ID (<code>int</code>) dello scenario simulato con paziente appena creato; <code>-1</code> in caso di errore
     * durante la creazione dello scenario avanzato o l'inserimento del record in <code>PatientSimulatedScenario</code>.
     */
    public int startPatientSimulatedScenario(String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        // Delega la creazione del record base dello scenario avanzato.
        int scenarioId = advancedScenarioService.startAdvancedScenario(titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);

        if (scenarioId > 0) {
            // Se lo scenario avanzato è stato creato con successo, aggiunge un record in PatientSimulatedScenario.
            final String sql = "INSERT INTO PatientSimulatedScenario (id_patient_simulated_scenario, id_advanced_scenario, sceneggiatura) VALUES (?,?,?)";

            try (Connection conn = DBConnect.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, scenarioId);     // L'ID dello scenario è lo stesso per la sua controparte PatientSimulatedScenario.
                stmt.setInt(2, scenarioId);     // Riferimento all'AdvancedScenario padre.
                stmt.setString(3, "");          // Inizializza la sceneggiatura a una stringa vuota.

                stmt.executeUpdate();
                logger.info("Record 'PatientSimulatedScenario' creato con successo per lo scenario ID: {}.", scenarioId);

            } catch (SQLException e) {
                logger.error("Errore SQL durante l'inserimento del record 'PatientSimulatedScenario' per lo scenario ID {}: {}. Si consiglia un controllo di consistenza dei dati.", scenarioId, e.getMessage(), e);
                // In caso di errore, si potrebbe voler eliminare lo scenario avanzato appena creato per evitare orfani.
                return -1;
            }
        } else {
            logger.error("Impossibile creare lo scenario avanzato padre. ID restituito: {}.", scenarioId);
        }
        return scenarioId;
    }

    /**
     * Recupera un oggetto {@link PatientSimulatedScenario} dal database in base all'ID fornito.
     * Questo metodo recupera sia i dati specifici di <code>PatientSimulatedScenario</code>
     * sia le informazioni generali dello scenario a cui è collegato.
     *
     * @param id L'ID dello scenario simulato con paziente da recuperare.
     * @return L'oggetto {@link PatientSimulatedScenario} corrispondente all'ID fornito; <code>null</code> se non trovato
     * o in caso di errore SQL.
     */
    public PatientSimulatedScenario getPatientSimulatedScenarioById(Integer id) {
        final String sql = "SELECT * FROM PatientSimulatedScenario pss " +
                "JOIN Scenario s ON pss.id_patient_simulated_scenario = s.id_scenario " +
                "WHERE pss.id_patient_simulated_scenario = ?";
        PatientSimulatedScenario scenario = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Costruisce l'oggetto PatientSimulatedScenario dai dati del ResultSet.
                scenario = new PatientSimulatedScenario(
                        rs.getInt("id_scenario"),
                        rs.getString("titolo"),
                        rs.getString("nome_paziente"),
                        rs.getString("patologia"),
                        rs.getString("descrizione"),
                        rs.getString("briefing"),
                        rs.getString("patto_aula"),
                        rs.getString("obiettivo"),
                        rs.getString("moulage"),
                        rs.getString("liquidi"),
                        rs.getFloat("timer_generale"),
                        rs.getString("autori"),
                        rs.getString("tipologia_paziente"), // Colonna 'tipologia_paziente' nel DB
                        rs.getString("target"),
                        rs.getString("info_genitore"),
                        rs.getInt("id_advanced_scenario"),
                        new ArrayList<>(), // I tempi non sono recuperati direttamente qui per evitare dipendenze circolari.
                        rs.getInt("id_patient_simulated_scenario"),
                        rs.getInt("id_advanced_scenario"),
                        rs.getString("sceneggiatura")
                );
                logger.info("Scenario simulato con paziente con ID {} recuperato con successo.", id);
            } else {
                logger.warn("Nessuno scenario simulato con paziente trovato con ID {}.", id);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero dello scenario simulato con paziente con ID {}: {}", id, e.getMessage(), e);
        }
        return scenario;
    }

    /**
     * Recupera la sceneggiatura associata a uno scenario simulato con paziente.
     *
     * @param scenarioId L'ID dello scenario simulato con paziente.
     * @return La stringa della sceneggiatura associata allo scenario; una stringa vuota se non trovata o in caso di errore.
     */
    public String getSceneggiatura(int scenarioId) {
        final String sql = "SELECT sceneggiatura FROM PatientSimulatedScenario WHERE id_patient_simulated_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String sceneggiatura = rs.getString("sceneggiatura");
                logger.info("Sceneggiatura recuperata per lo scenario con ID {}.", scenarioId);
                return sceneggiatura != null ? sceneggiatura : ""; // Ritorna stringa vuota se il valore è NULL nel DB.
            } else {
                logger.warn("Nessuna sceneggiatura trovata per lo scenario con ID {}.", scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero della sceneggiatura per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return "";
    }

    /**
     * Aggiorna il campo <code>sceneggiatura</code> per uno scenario simulato con paziente esistente.
     * Questo metodo verifica prima se lo scenario esiste ed è effettivamente di tipo "PatientSimulatedScenario"
     * utilizzando {@link ScenarioService#isPresentInTable(int, String)}}.
     *
     * @param scenarioId    L'ID dello scenario simulato con paziente da aggiornare.
     * @param sceneggiatura La nuova stringa della sceneggiatura da salvare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti
     * (es. scenario non trovato, non è un PatientSimulatedScenario, o errore SQL).
     */
    public boolean updateScenarioSceneggiatura(Integer scenarioId, String sceneggiatura) {
        // Verifica l'esistenza e la tipologia dello scenario.
        if (!scenarioService.isPresentInTable(scenarioId, "PatientSimulatedScenario")) {
            logger.warn("Lo scenario con ID {} non è un PatientSimulatedScenario o non esiste. Impossibile aggiornare la sceneggiatura.", scenarioId);
            return false;
        }

        final String sql = "UPDATE PatientSimulatedScenario SET sceneggiatura = ? WHERE id_patient_simulated_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sceneggiatura);
            stmt.setInt(2, scenarioId);

            int rowsUpdated = stmt.executeUpdate(); // Numero di righe modificate.
            if (rowsUpdated > 0) {
                logger.info("Sceneggiatura aggiornata con successo per lo scenario con ID {}.", scenarioId);
                return true;
            } else {
                logger.warn("Nessuna sceneggiatura aggiornata per lo scenario con ID {}. Il record potrebbe non essere stato modificato o il valore era già lo stesso.", scenarioId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento della sceneggiatura per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        }
    }
}