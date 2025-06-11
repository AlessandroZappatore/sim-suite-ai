package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione delle azioni chiave associate agli scenari.
 * Fornisce funzionalità per recuperare, aggiornare ed eliminare le azioni chiave
 * interagendo direttamente con il database.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class AzioneChiaveService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni sulle azioni chiave.
     */
    private static final Logger logger = LoggerFactory.getLogger(AzioneChiaveService.class);

    /**
     * Costruttore vuoto per il servizio AzioneChiaveService.
     */
    public AzioneChiaveService() {
        // Costruttore vuoto, può essere utilizzato per iniezione di dipendenze o inizializzazione.
    }

    /**
     * Recupera i nomi di tutte le azioni chiave associate a uno scenario specifico.
     * La query recupera i nomi delle azioni chiave dalla tabella <code>AzioniChiave</code>
     * tramite un'associazione con la tabella <code>AzioneScenario</code> basata sull'<code>id_scenario</code>.
     *
     * @param scenarioId L'ID dello scenario per il quale si desiderano recuperare le azioni chiave.
     * @return Una {@link List} di {@link String} contenente i nomi delle azioni chiave.
     * Restituisce una lista vuota se non vengono trovate azioni chiave o in caso di errore.
     */
    public List<String> getNomiAzioniChiaveByScenarioId(Integer scenarioId) {
        List<String> nomiAzioni = new ArrayList<>();

        // Query SQL per selezionare i nomi delle azioni chiave associate a un dato scenario.
        final String sql = "SELECT ac.nome " +
                "FROM AzioniChiave ac " +
                "JOIN AzioneScenario a ON ac.id_azione = a.id_azione " +
                "WHERE a.id_scenario = ?";

        // Utilizza try-with-resources per assicurare la chiusura automatica di Connection e PreparedStatement.
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId); // Imposta il parametro id_scenario.
            ResultSet rs = stmt.executeQuery(); // Esegue la query.

            // Itera sui risultati e aggiunge i nomi delle azioni alla lista.
            while (rs.next()) {
                nomiAzioni.add(rs.getString("nome"));
            }

            // Logga il risultato dell'operazione.
            if (!nomiAzioni.isEmpty()) {
                logger.info("Recuperate {} azioni chiave per lo scenario con ID {}", nomiAzioni.size(), scenarioId);
            } else {
                logger.info("Nessuna azione chiave trovata per lo scenario con ID {}", scenarioId);
            }

        } catch (SQLException e) {
            // Logga l'errore in caso di fallimento della query SQL.
            logger.error("Errore durante il recupero delle azioni chiave per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            return new ArrayList<>(); // Restituisce una lista vuota in caso di errore.
        }
        return nomiAzioni;
    }

    /**
     * Aggiorna le azioni chiave associate a uno scenario specifico.
     * Questo metodo gestisce la rimozione delle associazioni esistenti e l'inserimento di nuove associazioni.
     * Se un'azione chiave non esiste già nel database, viene creata. L'operazione è transazionale.
     *
     * @param scenarioId          L'ID dello scenario per il quale aggiornare le azioni chiave.
     * @param nomiAzioniDaSalvare Una {@link List} di {@link String} contenente i nomi delle azioni chiave da associare allo scenario.
     *                            I nomi <code>null</code> o vuoti vengono ignorati.
     * @return <code>true</code> se l'aggiornamento è stato completato con successo; <code>false</code> altrimenti.
     */
    public boolean updateAzioniChiaveForScenario(Integer scenarioId, List<String> nomiAzioniDaSalvare) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia la transazione.

            List<Integer> idAzioniFinali = new ArrayList<>();
            // Itera sui nomi delle azioni da salvare, ottenendo o creando l'ID per ciascuna.
            if (nomiAzioniDaSalvare != null) {
                for (String nomeAzione : nomiAzioniDaSalvare) {
                    // Salta i nomi delle azioni vuoti o nulli.
                    if (nomeAzione == null || nomeAzione.trim().isEmpty()) {
                        continue;
                    }
                    Integer idAzione = getOrCreateAzioneChiaveId(conn, nomeAzione.trim());
                    idAzioniFinali.add(idAzione);
                }
            }

            // Elimina tutte le associazioni esistenti per lo scenario.
            final String deleteSql = "DELETE FROM AzioneScenario WHERE id_scenario = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, scenarioId);
                int deletedRows = deleteStmt.executeUpdate();
                logger.info("Rimosse {} associazioni azione-scenario esistenti per lo scenario con ID: {}", deletedRows, scenarioId);
            }

            // Inserisce le nuove associazioni azione-scenario, se presenti.
            if (!idAzioniFinali.isEmpty()) {
                final String insertSql = "INSERT INTO AzioneScenario (id_scenario, id_azione) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Integer idAzione : idAzioniFinali) {
                        insertStmt.setInt(1, scenarioId);
                        insertStmt.setInt(2, idAzione);
                        insertStmt.addBatch(); // Aggiunge al batch per un'esecuzione più efficiente.
                    }
                    int[] batchResult = insertStmt.executeBatch(); // Esegue tutte le operazioni in batch.
                    logger.info("Inserite {} nuove associazioni azione-scenario per lo scenario con ID: {}", batchResult.length, scenarioId);
                }
            } else {
                logger.info("Nessuna nuova azione chiave da associare per lo scenario con ID: {}. Tutte le associazioni precedenti sono state rimosse.", scenarioId);
            }

            conn.commit(); // Conferma la transazione.
            success = true;
            logger.info("Azioni chiave per lo scenario con ID {} aggiornate con successo.", scenarioId);

        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento delle azioni chiave per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback(); // Esegue il rollback in caso di errore.
                    logger.warn("Rollback della transazione eseguito per lo scenario con ID {}", scenarioId);
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback della transazione per lo scenario con ID {}: {}", scenarioId, ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina l'autocommit.
                    conn.close(); // Chiude la connessione.
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * Recupera l'ID di un'azione chiave esistente o ne crea una nuova se non è presente nel database.
     * Questa è una transazione parziale all'interno di una transazione più grande e non deve commettere.
     *
     * @param conn       La {@link Connection} al database già aperta e gestita transazionalmente.
     * @param nomeAzione Il nome dell'azione chiave da cercare o creare. Non deve essere <code>null</code> o vuoto.
     * @return L'ID dell'azione chiave (<code>Integer</code>).
     * @throws SQLException Se si verifica un errore SQL durante l'accesso o la modifica del database.
     */
    private Integer getOrCreateAzioneChiaveId(Connection conn, String nomeAzione) throws SQLException {
        // Query per cercare un'azione chiave esistente per nome.
        final String selectSql = "SELECT id_azione FROM AzioniChiave WHERE nome = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, nomeAzione);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_azione"); // Restituisce l'ID se trovata.
            }
        }

        // Se l'azione chiave non esiste, la inserisce e recupera l'ID generato.
        final String insertSql = "INSERT INTO AzioniChiave (nome) VALUES (?)";
        // Richiede il recupero delle chiavi generate automaticamente.
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, nomeAzione);
            int affectedRows = insertStmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        logger.info("Creata nuova AzioneChiave '{}' con ID: {}", nomeAzione, generatedKeys.getInt(1));
                        return generatedKeys.getInt(1); // Restituisce l'ID della nuova azione.
                    } else {
                        throw new SQLException("Creazione AzioneChiave fallita per '" + nomeAzione + "', nessun ID generato ottenuto.");
                    }
                }
            } else {
                throw new SQLException("Creazione AzioneChiave fallita per '" + nomeAzione + "', nessuna riga modificata.");
            }
        }
    }

    /**
     * Elimina un'azione chiave specifica e la sua associazione con un dato scenario.
     * Se l'azione chiave non è più associata a nessun altro scenario dopo questa eliminazione,
     * viene rimossa anche dalla tabella <code>AzioniChiave</code>.
     * Questa operazione è transazionale.
     *
     * @param scenarioId L'ID dello scenario dal quale rimuovere l'associazione con l'azione chiave.
     * @param nome       Il nome dell'azione chiave da eliminare l'associazione.
     *                   Non deve essere <code>null</code> o vuoto.
     */
    public void deleteAzioneChiaveByName(Integer scenarioId, String nome) {
        // Controlla i parametri in input.
        if (nome == null || nome.isEmpty() || scenarioId == null) {
            logger.warn("Il nome dell'azione chiave o l'ID scenario fornito è vuoto o nullo. Impossibile procedere con l'eliminazione.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia la transazione.

            Integer idAzione = null;
            // Cerca l'ID dell'azione chiave basandosi sul nome.
            final String selectSql = "SELECT id_azione FROM AzioniChiave WHERE nome = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, nome);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    idAzione = rs.getInt("id_azione");
                }
            }

            if (idAzione != null) {
                // Elimina l'associazione tra l'azione chiave e lo scenario specifico.
                final String deleteAssociationSql = "DELETE FROM AzioneScenario WHERE id_azione = ? AND id_scenario = ?";
                try (PreparedStatement deleteAssocStmt = conn.prepareStatement(deleteAssociationSql)) {
                    deleteAssocStmt.setInt(1, idAzione);
                    deleteAssocStmt.setInt(2, scenarioId);
                    int assocRowsDeleted = deleteAssocStmt.executeUpdate();
                    logger.info("Rimosse {} associazioni dell'azione chiave '{}' con lo scenario ID {}", assocRowsDeleted, nome, scenarioId);
                }

                // Controlla se l'azione chiave è ancora associata a qualsiasi altro scenario.
                final String checkAssociationSql = "SELECT COUNT(*) FROM AzioneScenario WHERE id_azione = ?";
                try (PreparedStatement checkAssocStmt = conn.prepareStatement(checkAssociationSql)) {
                    checkAssocStmt.setInt(1, idAzione);
                    ResultSet rs = checkAssocStmt.executeQuery();
                    // Se non ci sono più associazioni, elimina l'azione chiave dalla tabella principale.
                    if (rs.next() && rs.getInt(1) == 0) {
                        final String deleteActionSql = "DELETE FROM AzioniChiave WHERE id_azione = ?";
                        try (PreparedStatement deleteActionStmt = conn.prepareStatement(deleteActionSql)) {
                            deleteActionStmt.setInt(1, idAzione);
                            int actionRowsDeleted = deleteActionStmt.executeUpdate();
                            if (actionRowsDeleted > 0) {
                                logger.info("Azione chiave '{}' con ID {} eliminata con successo in quanto non più associata ad alcuno scenario.", nome, idAzione);
                            } else {
                                logger.warn("L'azione chiave '{}' con ID {} non è stata trovata per l'eliminazione finale, ma l'associazione è stata rimossa.", nome, idAzione);
                            }
                        }
                    } else {
                        logger.info("L'azione chiave '{}' con ID {} è ancora associata ad altri scenari, quindi non è stata eliminata dalla tabella principale.", nome, idAzione);
                    }
                }
                conn.commit(); // Conferma la transazione.
            } else {
                logger.warn("Nessuna azione chiave trovata con il nome '{}'. Nessuna operazione di eliminazione eseguita.", nome);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'eliminazione dell'azione chiave '{}' per lo scenario ID {}: {}", nome, scenarioId, e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback(); // Esegue il rollback in caso di errore.
                    logger.warn("Eseguito rollback dell'operazione di eliminazione dell'azione chiave '{}' per lo scenario ID {}", nome, scenarioId);
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback: {}", ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina l'autocommit.
                    conn.close(); // Chiude la connessione.
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione nel finally: {}", e.getMessage(), e);
                }
            }
        }
    }
}