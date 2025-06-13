package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.common.Materiale;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione dei materiali necessari all'interno degli scenari.
 * Fornisce metodi per recuperare, salvare, associare ed eliminare i materiali nel database.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class MaterialeService {

    /**
     * Il logger per questa classe, utilizzato per registrare le operazioni e gli errori relativi ai materiali.
     */
    private static final Logger logger = LoggerFactory.getLogger(MaterialeService.class);

    /**
     * Istanza singleton del servizio {@link MaterialeService}.
     * Utilizza il pattern Singleton per garantire che ci sia una sola istanza di questo servizio.
     */
    private MaterialeService() {
        // Costruttore privato per prevenire l'istanza diretta della classe.
        // Utilizzare il metodo statico getInstance() per ottenere un'istanza del servizio.
    }

    /**
     * Recupera una lista di tutti i {@link Materiale Materiali} disponibili nel database.
     *
     * @return Una {@link List} di oggetti {@link Materiale} contenente tutti i materiali.
     * Restituisce una lista vuota in caso di errore o se non sono presenti materiali.
     */
    public List<Materiale> getAllMaterials() {
        final String sql = "SELECT id_materiale, nome, descrizione FROM Materiale";
        List<Materiale> materiali = new ArrayList<>();

        // Utilizza try-with-resources per assicurare la chiusura automatica di Connection, Statement e ResultSet.
        try (Connection conn = DBConnect.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Materiale materiale = new Materiale(
                        rs.getInt("id_materiale"),
                        rs.getString("nome"),
                        rs.getString("descrizione")
                );
                materiali.add(materiale);
            }
            logger.info("Recuperati {} materiali totali dal database.", materiali.size());
        } catch (SQLException e) {
            logger.error("Errore durante il recupero di tutti i materiali: {}", e.getMessage(), e);
        }
        return materiali;
    }

    /**
     * Recupera una lista di {@link Materiale Materiali} specifici associati a un dato scenario.
     * La query esegue un JOIN tra le tabelle <code>Materiale</code> e <code>MaterialeScenario</code>
     * per filtrare i materiali in base all'<code>id_scenario</code>.
     *
     * @param scenarioId L'ID dello scenario per il quale si desiderano recuperare i materiali.
     * @return Una {@link List} di oggetti {@link Materiale} associati allo scenario.
     * Restituisce una lista vuota in caso di errore o se non ci sono materiali associati.
     */
    public List<Materiale> getMaterialiByScenarioId(int scenarioId) {
        final String sql = "SELECT m.id_materiale, m.nome, m.descrizione " +
                "FROM Materiale m " +
                "JOIN MaterialeScenario sm ON m.id_materiale = sm.id_materiale " +
                "WHERE sm.id_scenario = ?";

        List<Materiale> materiali = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Materiale materiale = new Materiale(
                            rs.getInt("id_materiale"),
                            rs.getString("nome"),
                            rs.getString("descrizione")
                    );
                    materiali.add(materiale);
                }
            }
            logger.info("Recuperati {} materiali per lo scenario con ID {}.", materiali.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei materiali per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return materiali;
    }

    /**
     * Salva un nuovo oggetto {@link Materiale} nel database.
     * Se l'operazione ha successo, l'ID generato automaticamente per il nuovo materiale
     * viene popolato nell'oggetto restituito.
     *
     * @param materiale L'oggetto {@link Materiale} da salvare. Il campo <code>id_materiale</code> non è richiesto in input.
     * @return L'oggetto {@link Materiale} salvato con il suo <code>id_materiale</code> generato;
     * <code>null</code> se il salvataggio fallisce.
     */
    public Materiale saveMateriale(Materiale materiale) {
        final String sql = "INSERT INTO Materiale (nome, descrizione) VALUES (?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, materiale.nome());
            stmt.setString(2, materiale.descrizione());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        logger.info("Materiale '{}' salvato con successo con ID: {}.", materiale.nome(), id);
                        return new Materiale(id, materiale.nome(), materiale.descrizione());
                    }
                }
            }
            logger.warn("Nessun materiale salvato. Affected rows: {}.", affectedRows);
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio del materiale '{}': {}", materiale.nome(), e.getMessage(), e);
        }
        return null;
    }

    /**
     * Associa una lista di materiali (tramite i loro ID) a uno scenario specifico.
     * Questa operazione è transazionale: prima vengono rimosse tutte le associazioni esistenti
     * per lo scenario, poi vengono inserite le nuove associazioni.
     *
     * @param scenarioId   L'ID dello scenario a cui associare i materiali.
     * @param idsMateriali Una {@link List} di <code>Integer</code> contenente gli ID dei materiali da associare.
     *                     Se la lista è vuota, tutte le associazioni esistenti verranno rimosse e nessuna nuova verrà aggiunta.
     * @return <code>true</code> se l'associazione è avvenuta con successo; <code>false</code> altrimenti.
     */
    public boolean associaMaterialiToScenario(int scenarioId, List<Integer> idsMateriali) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia la transazione.

            // Elimina tutte le associazioni esistenti tra lo scenario e i materiali.
            final String deleteSQL = "DELETE FROM MaterialeScenario WHERE id_scenario = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                deleteStmt.setInt(1, scenarioId);
                int deletedRows = deleteStmt.executeUpdate();
                logger.info("Rimosse {} associazioni materiali esistenti per lo scenario con ID {}.", deletedRows, scenarioId);
            }

            // Inserisce le nuove associazioni, se la lista non è vuota.
            if (!idsMateriali.isEmpty()) {
                final String insertSQL = "INSERT INTO MaterialeScenario (id_scenario, id_materiale) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Integer idMateriale : idsMateriali) {
                        insertStmt.setInt(1, scenarioId);
                        insertStmt.setInt(2, idMateriale);
                        insertStmt.addBatch(); // Aggiunge l'operazione al batch.
                    }
                    int[] insertedRows = insertStmt.executeBatch(); // Esegue tutte le operazioni in batch.
                    logger.info("Inserite {} nuove associazioni materiali per lo scenario con ID {}.", insertedRows.length, scenarioId);
                }
            } else {
                logger.info("Nessun materiale da associare allo scenario con ID {}. Tutte le associazioni precedenti sono state rimosse.", scenarioId);
            }

            conn.commit(); // Conferma la transazione.
            logger.info("Materiali per lo scenario con ID {} associati con successo.", scenarioId);
            return true;
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'associazione dei materiali allo scenario {}: {}", scenarioId, e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback(); // Esegue il rollback in caso di errore.
                    logger.warn("Rollback della transazione eseguito per lo scenario con ID {}.", scenarioId);
                }
            } catch (SQLException ex) {
                logger.error("Errore durante il rollback della transazione per lo scenario con ID {}: {}", scenarioId, ex.getMessage(), ex);
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina l'autocommit.
                    conn.close(); // Chiude la connessione.
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione nel finally per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Elimina un {@link Materiale} dal database.
     * Questa operazione è transazionale: prima vengono rimosse tutte le associazioni
     * del materiale con gli scenari, poi il materiale stesso viene eliminato dalla tabella <code>Materiale</code>.
     *
     * @param idMateriale L'ID del materiale da eliminare.
     * @return <code>true</code> se l'eliminazione è avvenuta con successo; <code>false</code> altrimenti.
     */
    public boolean deleteMateriale(Integer idMateriale) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia la transazione.

            // Elimina tutte le associazioni del materiale con gli scenari.
            final String deleteAssociazioniSQL = "DELETE FROM MaterialeScenario WHERE id_materiale = ?";
            try (PreparedStatement deleteAssociazioniStmt = conn.prepareStatement(deleteAssociazioniSQL)) {
                deleteAssociazioniStmt.setInt(1, idMateriale);
                int deletedAssocRows = deleteAssociazioniStmt.executeUpdate();
                logger.info("Rimosse {} associazioni per il materiale con ID {}.", deletedAssocRows, idMateriale);
            }

            // Elimina il materiale dalla tabella principale.
            final String deleteMaterialeSQL = "DELETE FROM Materiale WHERE id_materiale = ?";
            try (PreparedStatement deleteMaterialeStmt = conn.prepareStatement(deleteMaterialeSQL)) {
                deleteMaterialeStmt.setInt(1, idMateriale);
                int rowsDeleted = deleteMaterialeStmt.executeUpdate();

                conn.commit(); // Conferma la transazione.
                if (rowsDeleted > 0) {
                    logger.info("Materiale con ID {} eliminato con successo dal database.", idMateriale);
                } else {
                    logger.warn("Nessun materiale eliminato con ID {}. Potrebbe non esistere.", idMateriale);
                }
                return rowsDeleted > 0;
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'eliminazione del materiale con ID {}: {}", idMateriale, e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback(); // Esegue il rollback in caso di errore.
                    logger.warn("Rollback della transazione eseguito per il materiale con ID {}.", idMateriale);
                }
            } catch (SQLException ex) {
                logger.error("Errore durante il rollback della transazione per il materiale con ID {}: {}", idMateriale, ex.getMessage(), ex);
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina l'autocommit.
                    conn.close(); // Chiude la connessione.
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione nel finally per il materiale con ID {}: {}", idMateriale, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Genera una rappresentazione testuale di tutti i materiali associati a uno scenario specifico.
     * La stringa risultante elenca ogni materiale con il suo nome e descrizione.
     *
     * @param scenarioId L'ID dello scenario di cui si vogliono rappresentare i materiali.
     * @return Una {@link String} che contiene il nome e la descrizione di ogni materiale associato,
     * separati da un newline. Se non ci sono materiali, restituisce una stringa vuota.
     */
    public String toStringAllMaterialsByScenarioId(int scenarioId) {
        StringBuilder sb = new StringBuilder();
        List<Materiale> materiali = getMaterialiByScenarioId(scenarioId);
        if (materiali.isEmpty()) {
            logger.info("Nessun materiale trovato per generare la stringa per lo scenario con ID {}.", scenarioId);
        }
        for (Materiale materiale : materiali) {
            sb.append(materiale.nome())
                    .append(": ")
                    .append(materiale.descrizione())
                    .append("\n");
        }
        return sb.toString();
    }
}