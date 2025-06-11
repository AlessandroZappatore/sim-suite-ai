package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Servizio per la gestione dei presidi associati agli scenari.
 * Questo servizio permette di recuperare, salvare e gestire le associazioni tra scenari e presidi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class PresidiService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni sui presidi.
     */
    private static final Logger logger = LoggerFactory.getLogger(PresidiService.class);

    /**
     * Costruttore privato per evitare l'istanza diretta della classe.
     */
    private PresidiService() {
        // Costruttore privato per evitare l'istanza diretta della classe.
        // Utilizzare i metodi statici per accedere alle funzionalità del servizio.
    }

    /**
     * Recupera una lista di tutti i nomi dei presidi disponibili nel database.
     *
     * @return Una {@link List} di {@link String} contenente i nomi di tutti i presidi.
     * Restituisce una lista vuota in caso di errore o se non sono presenti presidi.
     */
    public static List<String> getAllPresidi() {
        final String sql = "SELECT nome FROM Presidi";
        List<String> presidi = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                presidi.add(rs.getString("nome"));
            }

            if (!presidi.isEmpty()) {
                logger.info("Recuperati {} presidi dal database.", presidi.size());
            } else {
                logger.warn("Nessun presidio trovato nel database.");
            }

        } catch (Exception e) {
            logger.error("Errore durante il recupero di tutti i presidi: {}", e.getMessage(), e);
        }
        return presidi;
    }

    /**
     * Recupera i nomi dei presidi specifici associati a un dato scenario.
     * La query esegue un JOIN tra le tabelle <code>Presidi</code> e <code>PresidioScenario</code>
     * per filtrare i presidi in base all'<code>id_scenario</code>.
     *
     * @param scenarioId L'ID dello scenario per il quale si desiderano recuperare i presidi.
     * @return Una {@link List} di {@link String} contenente i nomi dei presidi associati allo scenario.
     * Restituisce una lista vuota in caso di errore o se non ci sono presidi associati.
     */
    public static List<String> getPresidiByScenarioId(Integer scenarioId) {
        final String sql = "SELECT p.nome FROM Presidi p " +
                "JOIN PresidioScenario ps ON p.id_presidio = ps.id_presidio " +
                "WHERE ps.id_scenario = ?";
        List<String> presidi = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                presidi.add(rs.getString("nome"));
            }
            logger.info("Recuperati {} presidi per lo scenario con ID {}.", presidi.size(), scenarioId);

        } catch (Exception e) {
            logger.error("Errore durante il recupero dei presidi per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return presidi;
    }

    /**
     * Recupera l'ID di un presidio dal database in base al suo nome.
     *
     * @param presidio Il nome del presidio di cui si vuole ottenere l'ID.
     * @return L'ID (<code>Integer</code>) del presidio se trovato; <code>null</code> altrimenti o in caso di errore.
     */
    public Integer getPresidiId(String presidio) {
        final String sql = "SELECT id_presidio FROM Presidi WHERE nome = ?";
        Integer id = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, presidio);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id_presidio");
                logger.debug("Trovato presidio '{}' con ID: {}", presidio, id);
            } else {
                logger.warn("Nessun presidio trovato con il nome '{}'.", presidio);
            }
        } catch (Exception e) {
            logger.error("Errore durante il recupero dell'ID del presidio '{}': {}", presidio, e.getMessage(), e);
        }
        return id;
    }

    /**
     * Salva o aggiorna le associazioni dei presidi per uno scenario specifico.
     * Questa operazione prima elimina tutte le associazioni esistenti per lo scenario,
     * quindi inserisce le nuove associazioni basate sul set di nomi di presidi fornito.
     *
     * @param scenarioId L'ID dello scenario a cui i presidi devono essere associati.
     * @param value      Un {@link Set} di {@link String} contenente i nomi dei presidi da associare.
     *                   Solo i presidi i cui nomi corrispondono a presidi esistenti nel database verranno associati.
     * @return <code>true</code> se l'operazione di salvataggio/aggiornamento è riuscita per tutti i presidi; <code>false</code> altrimenti.
     */
    public boolean savePresidi(Integer scenarioId, Set<String> value) {
        boolean success = true;

        try (Connection conn = DBConnect.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Inizia la transazione.

            // 1. Elimina tutte le associazioni esistenti per lo scenario.
            final String deleteSQL = "DELETE FROM PresidioScenario WHERE id_scenario = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                deleteStmt.setInt(1, scenarioId);
                int deletedRows = deleteStmt.executeUpdate();
                logger.info("Rimosse {} associazioni presidio-scenario esistenti per lo scenario con ID {}.", deletedRows, scenarioId);
            }

            // 2. Inserisce le nuove associazioni basate sul set di nomi dei presidi.
            final String insertSQL = "INSERT INTO PresidioScenario (id_presidio, id_scenario) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                for (String presidio : value) {
                    Integer presidioId = getPresidiId(presidio); // Recupera l'ID del presidio.
                    if (presidioId != null) {
                        insertStmt.setInt(1, presidioId);
                        insertStmt.setInt(2, scenarioId);
                        insertStmt.addBatch(); // Aggiunge l'operazione al batch.
                    } else {
                        logger.warn("Presidio con nome '{}' non trovato nel database. Non sarà associato allo scenario {}.", presidio, scenarioId);
                    }
                }

                // Esegue tutte le operazioni di inserimento in batch.
                int[] result = insertStmt.executeBatch();
                for (int r : result) {
                    if (r == Statement.EXECUTE_FAILED) {
                        success = false; // Se anche una sola operazione fallisce, l'intero processo non è un successo.
                        break;
                    }
                }
                logger.info("Inserite {} nuove associazioni presidio-scenario per lo scenario con ID {}.", result.length, scenarioId);
            }

            if (success) {
                conn.commit(); // Conferma la transazione se tutto è andato a buon fine.
                logger.info("Presidi per lo scenario con ID {} salvati con successo.", scenarioId);
            } else {
                conn.rollback(); // Esegue il rollback in caso di fallimento.
                logger.warn("Rollback della transazione per il salvataggio dei presidi dello scenario con ID {}.", scenarioId);
            }
        } catch (Exception e) {
            logger.error("Errore durante il salvataggio dei presidi per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            success = false;
        }
        return success;
    }
}