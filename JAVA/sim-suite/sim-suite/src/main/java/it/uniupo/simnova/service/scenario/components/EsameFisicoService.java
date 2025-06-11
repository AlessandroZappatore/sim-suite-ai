package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Servizio per la gestione degli esami fisici associati ai pazienti in uno scenario.
 * Fornisce metodi per recuperare, aggiungere o aggiornare i dettagli di un esame fisico.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class EsameFisicoService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni sugli esami fisici.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsameFisicoService.class);

    /**
     * Costruttore della classe {@link EsameFisicoService}.
     * Viene utilizzato per l'iniezione delle dipendenze da parte di Spring.
     */
    public EsameFisicoService() {
        // Costruttore vuoto, necessario per l'iniezione di dipendenze da parte di Spring.
    }

    /**
     * Recupera un oggetto {@link EsameFisico} dal database utilizzando il suo identificativo.
     *
     * @param id L'ID dell'esame fisico da recuperare. Questo ID corrisponde tipicamente all'ID dello scenario.
     * @return L'oggetto {@link EsameFisico} corrispondente all'ID fornito, o <code>null</code> se nessun esame fisico viene trovato
     * o se si verifica un errore SQL.
     */
    public EsameFisico getEsameFisicoById(Integer id) {
        final String sql = "SELECT * FROM EsameFisico WHERE id_esame_fisico = ?";
        EsameFisico esameFisico = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Costruisce l'oggetto EsameFisico dai dati del ResultSet.
                esameFisico = new EsameFisico(
                        rs.getInt("id_esame_fisico"),
                        rs.getString("generale"),
                        rs.getString("pupille"),
                        rs.getString("collo"),
                        rs.getString("torace"),
                        rs.getString("cuore"),
                        rs.getString("addome"),
                        rs.getString("retto"),
                        rs.getString("cute"),
                        rs.getString("estremità"),
                        rs.getString("neurologico"),
                        rs.getString("FAST")
                );
                logger.info("Esame fisico con ID {} recuperato con successo.", id);
            } else {
                logger.warn("Nessun esame fisico trovato con ID {}.", id);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dell'esame fisico con ID {}: {}", id, e.getMessage(), e);
        }
        return esameFisico;
    }

    /**
     * Aggiunge un nuovo esame fisico o aggiorna uno esistente nel database.
     * L'operazione è determinata dalla presenza di un esame fisico con l'ID fornito.
     * Se un esame fisico con l'<code>scenarioId</code> esiste già, viene aggiornato; altrimenti, viene inserito.
     *
     * @param scenarioId L'ID dello scenario a cui l'esame fisico è associato. Questo ID viene usato come chiave primaria per l'esame fisico.
     * @param examData   Una {@link Map} contenente i nomi dei campi (colonne) e i rispettivi valori da salvare o aggiornare.
     *                   I nomi delle chiavi devono corrispondere ai nomi delle colonne nel database.
     *                   Se <code>examData</code> è <code>null</code>, viene trattato come una mappa vuota.
     * @return <code>true</code> se l'operazione di aggiunta o aggiornamento è riuscita; <code>false</code> altrimenti.
     */
    public boolean addEsameFisico(int scenarioId, Map<String, String> examData) {
        // Se examData è null, inizializza con una mappa vuota per evitare NullPointerException.
        if (examData == null) {
            examData = Map.of();
        }

        // Verifica se un esame fisico con l'ID dello scenario esiste già.
        boolean exists = getEsameFisicoById(scenarioId) != null;

        // Determina la query SQL da usare (INSERT o UPDATE) in base alla presenza dell'esame.
        final String sql = exists ?
                "UPDATE EsameFisico SET generale=?, pupille=?, collo=?, torace=?, cuore=?, " +
                        "addome=?, retto=?, cute=?, estremità=?, neurologico=?, FAST=? " +
                        "WHERE id_esame_fisico=?" :
                "INSERT INTO EsameFisico (id_esame_fisico, generale, pupille, collo, torace, " +
                        "cuore, addome, retto, cute, estremità, neurologico, FAST) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            // Se è un INSERT, il primo parametro è l'id_esame_fisico.
            if (!exists) {
                stmt.setInt(paramIndex++, scenarioId);
            }

            // Imposta i valori per tutte le colonne dell'esame fisico, usando valori di default vuoti se non presenti nella mappa.
            stmt.setString(paramIndex++, examData.getOrDefault("Generale", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Pupille", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Collo", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Torace", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Cuore", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Addome", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Retto", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Cute", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Estremità", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Neurologico", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("FAST", ""));

            // Se è un UPDATE, l'ultimo parametro è l'id_esame_fisico per la clausola WHERE.
            if (exists) {
                stmt.setInt(paramIndex, scenarioId);
            }

            // Esegue l'aggiornamento o l'inserimento e verifica il risultato.
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Esame fisico {} con ID {} con successo.", exists ? "aggiornato" : "inserito", scenarioId);
            } else {
                logger.warn("Nessun esame fisico {} con ID {}. Potrebbe essere un problema con i dati forniti o con la query.", exists ? "aggiornato" : "inserito", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dell'esame fisico con ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Aggiorna un singolo campo di un esame fisico esistente nel database.
     * Il campo da aggiornare è specificato dal suo nome di colonna.
     * Vengono eseguiti controlli di validità sul nome della colonna.
     *
     * @param scenarioId L'ID dello scenario a cui l'esame fisico è associato.
     * @param name       Il nome della colonna (campo dell'esame fisico) da aggiornare (es. "Generale", "Pupille").
     *                   Deve essere uno dei nomi di sezione validi.
     * @param value      Il nuovo valore da impostare per la colonna specificata.
     */
    public void updateSingleEsameFisico(int scenarioId, String name, String value) {
        // Lista dei nomi di colonna validi per l'esame fisico.
        List<String> sections = List.of("generale", "pupille", "collo", "torace", "cuore", "addome", "retto", "cute", "estremita", "neurologico", "FAST");

        // Verifica la validità del nome della colonna e la sua presenza nella lista delle sezioni consentite.
        if (name.isEmpty() || !sections.contains(name)) {
            logger.warn("Nome della colonna non valido per l'aggiornamento: '{}'. L'aggiornamento è stato ignorato.", name);
            return;
        }

        // Verifica che l'esame fisico esista prima di tentare l'aggiornamento.
        if (getEsameFisicoById(scenarioId) == null) {
            logger.warn("Nessun esame fisico trovato con ID {}. Impossibile aggiornare la colonna '{}'.", scenarioId, name);
            return;
        }

        // Costruisce la query SQL dinamicamente per l'aggiornamento della singola colonna.
        // La soppressione di "SqlSourceToSinkFlow" è necessaria perché il nome della colonna è una variabile e non un letterale,
        // ma è stata validata contro una lista fissa per prevenire SQL Injection.
        final String sql = "UPDATE EsameFisico SET " + name + "=? WHERE id_esame_fisico=?";

        // Utilizza try-with-resources per assicurare la chiusura automatica di Connection e PreparedStatement.
        //noinspection SqlSourceToSinkFlow
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);    // Imposta il nuovo valore per la colonna.
            stmt.setInt(2, scenarioId); // Imposta l'ID dello scenario per la clausola WHERE.

            // Esegue l'aggiornamento e verifica il risultato.
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Colonna '{}' dell'esame fisico con ID {} aggiornata con successo al valore: '{}'.", name, scenarioId, value);
            } else {
                logger.warn("Impossibile aggiornare la colonna '{}' dell'esame fisico con ID {}. Nessuna riga modificata.", name, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento della colonna '{}' dell'esame fisico con ID {}: {}", name, scenarioId, e.getMessage(), e);
        }
    }
}