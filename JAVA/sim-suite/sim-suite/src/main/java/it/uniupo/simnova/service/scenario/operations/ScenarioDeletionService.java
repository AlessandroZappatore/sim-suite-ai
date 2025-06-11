package it.uniupo.simnova.service.scenario.operations;

import it.uniupo.simnova.service.scenario.helper.MediaHelper;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Servizio per la gestione della cancellazione completa di uno scenario.
 * Fornisce un metodo transazionale per eliminare uno scenario e tutti i dati
 * correlati da diverse tabelle del database, inclusi i file multimediali associati.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class ScenarioDeletionService {

    /**
     * Il servizio per la gestione dello storage dei file, utilizzato per eliminare i file multimediali.
     */
    private final FileStorageService fileStorageService;

    /**
     * Il servizio per la gestione degli scenari avanzati, utilizzato per delegare la cancellazione dei tempi.
     */
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni di cancellazione.
     */
    private final Logger logger = LoggerFactory.getLogger(ScenarioDeletionService.class);

    /**
     * Costruisce una nuova istanza di <code>ScenarioDeletionService</code>.
     * Inietta le dipendenze dei servizi necessari per le operazioni di cancellazione.
     *
     * @param fileStorageService      Il servizio per la gestione dei file multimediali.
     * @param advancedScenarioService Il servizio per la gestione degli scenari avanzati.
     */
    public ScenarioDeletionService(FileStorageService fileStorageService, AdvancedScenarioService advancedScenarioService) {
        this.fileStorageService = fileStorageService;
        this.advancedScenarioService = advancedScenarioService;
    }

    /**
     * Elimina uno scenario dal database, inclusi tutti i suoi dati correlati
     * e i file multimediali associati. L'operazione è eseguita all'interno di una transazione
     * per garantire l'integrità dei dati.
     *
     * @param scenarioId L'ID dello scenario da eliminare.
     * @return <code>true</code> se l'eliminazione è avvenuta con successo; <code>false</code> altrimenti.
     */
    public boolean deleteScenario(int scenarioId) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia la transazione.

            // 1. Recupera i nomi dei file multimediali associati allo scenario prima dell'eliminazione dal DB.
            List<String> mediaFiles = MediaHelper.getMediaFilesForScenario(scenarioId);
            logger.debug("Trovati {} file media da eliminare per lo scenario con ID {}", mediaFiles.size(), scenarioId);

            // 2. Elimina i dati correlati in ordine inverso di dipendenza.
            deleteAccessi(conn, scenarioId, "AccessoVenoso");
            deleteAccessi(conn, scenarioId, "AccessoArterioso");
            deleteRelatedMaterial(conn, scenarioId);
            deleteRelatedPresidi(conn, scenarioId);
            deleteRelatedAzioniChiave(conn, scenarioId);
            // Delega la cancellazione dei tempi al servizio specifico AdvancedScenarioService.
            advancedScenarioService.deleteTempi(conn, scenarioId);
            deletePatientSimulatedScenario(conn, scenarioId);
            deleteAdvancedScenario(conn, scenarioId);
            deleteEsamiReferti(conn, scenarioId);
            deleteEsameFisico(conn, scenarioId);
            deletePazienteT0(conn, scenarioId);
            deleteScenarioPrincipale(conn, scenarioId);

            // 3. Elimina gli accessi orfani, ovvero quelli non più referenziati da alcun paziente T0.
            deleteRelatedAccessi(conn);

            conn.commit(); // Conferma la transazione se tutte le operazioni DB sono riuscite.
            logger.info("Dati database per lo scenario con ID {} eliminati con successo.", scenarioId);

            // 4. Elimina i file multimediali dallo storage solo dopo il successo del DB.
            fileStorageService.deleteFiles(mediaFiles);
            logger.info("File media associati allo scenario con ID {} eliminati con successo.", scenarioId);

            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Esegue il rollback in caso di errore SQL.
                    logger.error("Rollback della transazione eseguito per lo scenario con ID {} a causa di un errore SQL: {}", scenarioId, e.getMessage());
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback della transazione per lo scenario con ID {}: {}", scenarioId, ex.getMessage(), ex);
                }
            }
            logger.error("Errore durante l'eliminazione dello scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina l'autocommit, indipendentemente dal successo.
                    conn.close(); // Chiude la connessione.
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione o il ripristino dell'autocommit per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Elimina le associazioni tra azioni chiave e lo scenario specificato, e poi rimuove le azioni chiave
     * che non sono più associate a nessun altro scenario (azioni orfane).
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione delle query.
     */
    private void deleteRelatedAzioniChiave(Connection conn, int scenarioId) throws SQLException {
        // Elimina le associazioni specifiche dello scenario.
        final String sqlDeleteAzioneScenario = "DELETE FROM AzioneScenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteAzioneScenario)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminate {} associazioni azione-scenario per lo scenario ID {}.", count, scenarioId);
        }

        // Elimina le azioni chiave che non sono più associate a nessuno scenario.
        final String sqlDeleteOrphanAzioni =
                "DELETE FROM AzioniChiave WHERE id_azione NOT IN (" +
                        "  SELECT DISTINCT id_azione FROM AzioneScenario" +
                        ")";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteOrphanAzioni)) {
            int count = stmt.executeUpdate();
            logger.debug("Eliminate {} azioni chiave orfane dopo l'eliminazione dello scenario ID {}.", count, scenarioId);
        }
    }

    /**
     * Elimina tutte le associazioni dei presidi con lo scenario specificato dalla tabella <code>PresidioScenario</code>.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     */
    private void deleteRelatedPresidi(Connection conn, int scenarioId) {
        final String sql = "DELETE FROM PresidioScenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminate {} associazioni presidio-scenario per lo scenario ID {}.", count, scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione delle associazioni dei presidi per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Elimina gli accessi (venosi o arteriosi) associati a un paziente T0 di uno scenario.
     * Questa funzione elimina solo le relazioni nella tabella specificata (es. <code>AccessoVenoso</code>).
     * L'effettiva eliminazione degli oggetti <code>Accesso</code> orfani è gestita da {@link #deleteRelatedAccessi(Connection)}.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario a cui gli accessi sono associati (corrisponde all'ID del paziente T0).
     * @param tableName  Il nome della tabella di associazione (es. "AccessoVenoso" o "AccessoArterioso").
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deleteAccessi(Connection conn, int scenarioId, String tableName) throws SQLException {
        // La soppressione di "SqlSourceToSinkFlow" è giustificata perché `tableName` è un valore controllato internamente.
        final String sql = "DELETE FROM " + tableName + " WHERE paziente_t0_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminate {} relazioni da {} per lo scenario ID {}.", count, tableName, scenarioId);
        }
    }

    /**
     * Elimina tutte le associazioni dei materiali con lo scenario specificato dalla tabella <code>MaterialeScenario</code>.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deleteRelatedMaterial(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM MaterialeScenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminate {} associazioni materiale-scenario per lo scenario ID {}.", count, scenarioId);
        }
    }

    /**
     * Elimina gli oggetti {@link it.uniupo.simnova.domain.common.Accesso} dalla tabella <code>Accesso</code> che non sono più referenziati
     * da alcuna relazione in <code>AccessoVenoso</code> o <code>AccessoArterioso</code>.
     * Questo pulisce gli accessi "orfani" dopo la rimozione delle loro associazioni con gli scenari.
     *
     * @param conn La {@link Connection} al database.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deleteRelatedAccessi(Connection conn) throws SQLException {
        final String sql = "DELETE FROM Accesso WHERE id_accesso IN (" +
                "SELECT a.id_accesso FROM Accesso a " +
                "LEFT JOIN AccessoVenoso av ON a.id_accesso = av.accesso_id " +
                "LEFT JOIN AccessoArterioso aa ON a.id_accesso = aa.accesso_id " +
                "WHERE (av.accesso_id IS NULL AND aa.accesso_id IS NULL))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int count = stmt.executeUpdate();
            logger.debug("Eliminati {} accessi orfani.", count);
        }
    }

    /**
     * Elimina il record da <code>PatientSimulatedScenario</code> associato allo scenario specificato.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deletePatientSimulatedScenario(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM PatientSimulatedScenario WHERE id_patient_simulated_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminato {} record da PatientSimulatedScenario per lo scenario ID {}.", count, scenarioId);
        }
    }

    /**
     * Elimina il record da <code>AdvancedScenario</code> associato allo scenario specificato.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deleteAdvancedScenario(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM AdvancedScenario WHERE id_advanced_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminato {} record da AdvancedScenario per lo scenario ID {}.", count, scenarioId);
        }
    }

    /**
     * Elimina tutti i referti degli esami associati allo scenario specificato dalla tabella <code>EsameReferto</code>.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deleteEsamiReferti(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM EsameReferto WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminati {} record da EsameReferto per lo scenario ID {}.", count, scenarioId);
        }
    }

    /**
     * Elimina il record dell'esame fisico associato allo scenario specificato dalla tabella <code>EsameFisico</code>.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deleteEsameFisico(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM EsameFisico WHERE id_esame_fisico = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminato {} record da EsameFisico per lo scenario ID {}.", count, scenarioId);
        }
    }

    /**
     * Elimina il record del paziente T0 associato allo scenario specificato dalla tabella <code>PazienteT0</code>.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deletePazienteT0(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM PazienteT0 WHERE id_paziente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminato {} record da PazienteT0 per lo scenario ID {}.", count, scenarioId);
        }
    }

    /**
     * Elimina il record principale dello scenario dalla tabella <code>Scenario</code>.
     * Questo dovrebbe essere l'ultima operazione di eliminazione nel flusso di cancellazione.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL durante l'esecuzione della query.
     */
    private void deleteScenarioPrincipale(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM Scenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminato {} record dalla tabella Scenario per lo scenario ID {}.", count, scenarioId);
        }
    }
}