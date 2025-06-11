package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione degli esami e dei referti associati ai pazienti all'interno degli scenari.
 * Fornisce funzionalità per salvare, recuperare ed eliminare i referti degli esami,
 * inclusa la gestione dei file multimediali a essi collegati.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class EsameRefertoService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni su esami e referti.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsameRefertoService.class);

    /**
     * Il servizio per la gestione dello storage dei file, utilizzato per eliminare i file multimediali associati ai referti.
     */
    private final FileStorageService fileStorageService;

    /**
     * Costruisce una nuova istanza di <code>EsameRefertoService</code>.
     * Inietta il servizio {@link FileStorageService} necessario per le operazioni sui file.
     *
     * @param fileStorageService Il servizio per la gestione dei file.
     */
    public EsameRefertoService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Salva una lista di oggetti {@link EsameReferto} per uno scenario specifico.
     * Prima di inserire i nuovi referti, questo metodo tenta di eliminare tutti i referti esistenti
     * associati allo stesso <code>scenarioId</code> per prevenire duplicati o dati obsoleti.
     * L'operazione di salvataggio avviene in batch per migliorare le prestazioni.
     *
     * @param scenarioId L'ID dello scenario a cui i referti degli esami devono essere associati.
     * @param esamiData  Una {@link List} di oggetti {@link EsameReferto} da salvare nel database.
     * @return <code>true</code> se il salvataggio è avvenuto con successo per tutti i referti; <code>false</code> altrimenti.
     */
    public boolean saveEsamiReferti(int scenarioId, List<EsameReferto> esamiData) {
        // Tenta di eliminare tutti i referti esistenti per lo scenario. Se fallisce, interrompe l'operazione.
        if (!deleteEsamiReferti(scenarioId)) {
            logger.warn("Impossibile eliminare i referti esistenti per lo scenario con ID {}. Salvataggio annullato.", scenarioId);
            return false;
        }

        // Query SQL per l'inserimento dei referti.
        final String sql = "INSERT INTO EsameReferto (id_esame, id_scenario, tipo, media, referto_testuale) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Prepara tutti gli statement di inserimento in batch.
            for (EsameReferto esame : esamiData) {
                stmt.setInt(1, esame.getIdEsame());
                stmt.setInt(2, scenarioId);
                stmt.setString(3, esame.getTipo());
                stmt.setString(4, esame.getMedia());
                stmt.setString(5, esame.getRefertoTestuale());
                stmt.addBatch(); // Aggiunge l'operazione al batch.
            }

            // Esegue tutte le operazioni in batch.
            int[] results = stmt.executeBatch();
            // Verifica che tutte le righe siano state inserite correttamente.
            for (int result : results) {
                if (result <= 0) {
                    logger.warn("Fallimento parziale nel salvataggio dei referti per lo scenario con ID {}. Alcuni referti potrebbero non essere stati salvati.", scenarioId);
                    return false;
                }
            }
            logger.info("Referti salvati con successo per lo scenario con ID {}. Totale referti salvati: {}.", scenarioId, results.length);
            return true;
        } catch (SQLException e) {
            logger.error("Errore SQL durante il salvataggio dei referti per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Recupera tutti gli oggetti {@link EsameReferto} associati a uno scenario specifico.
     * I referti vengono ordinati per <code>id_esame</code>.
     *
     * @param scenarioId L'ID dello scenario per cui recuperare gli esami e i referti.
     * @return Una {@link List} di oggetti {@link EsameReferto} associati allo scenario.
     * Restituisce una lista vuota se non vengono trovati referti o in caso di errore.
     */
    public List<EsameReferto> getEsamiRefertiByScenarioId(int scenarioId) {
        final String sql = "SELECT * FROM EsameReferto WHERE id_scenario = ? ORDER BY id_esame";
        List<EsameReferto> esami = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            // Itera sui risultati e costruisce gli oggetti EsameReferto.
            while (rs.next()) {
                EsameReferto esame = new EsameReferto(
                        rs.getInt("id_esame"),
                        rs.getInt("id_scenario"),
                        rs.getString("tipo"),
                        rs.getString("media"),
                        rs.getString("referto_testuale")
                );
                esami.add(esame);
            }
            logger.info("Recuperati {} esami referti per lo scenario con ID {}.", esami.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero degli esami referti per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return esami;
    }

    /**
     * Elimina tutti i referti degli esami associati a uno scenario specifico dal database.
     * Questa operazione è di supporto per il salvataggio di nuovi set di referti.
     *
     * @param scenarioId L'ID dello scenario di cui eliminare tutti i referti.
     * @return <code>true</code> se l'eliminazione è avvenuta con successo (anche se non c'erano referti da eliminare); <code>false</code> altrimenti.
     */
    private boolean deleteEsamiReferti(int scenarioId) {
        final String sql = "DELETE FROM EsameReferto WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            int rowsAffected = stmt.executeUpdate(); // Numero di righe eliminate.
            // Se rowsAffected >= 0, significa che la query è stata eseguita senza errori.
            if (rowsAffected >= 0) {
                logger.info("Eliminati {} referti esami per lo scenario con ID {}.", rowsAffected, scenarioId);
            } else {
                // Questo caso è raro, ma indica un problema nell'esecuzione della query.
                logger.warn("Nessun referto esami eliminato per lo scenario con ID {}. Potrebbe esserci stato un problema con la query.", scenarioId);
            }
            return true;
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione dei referti esami per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Elimina un singolo referto di esame specifico dal database e il file multimediale a esso associato.
     *
     * @param idEsameReferto L'ID del referto dell'esame da eliminare.
     * @param scenarioId     L'ID dello scenario a cui appartiene il referto.
     * @return <code>true</code> se l'eliminazione del referto e del suo file media associato è avvenuta con successo; <code>false</code> altrimenti.
     */
    public boolean deleteEsameReferto(int idEsameReferto, int scenarioId) {
        // Recupera il nome del file multimediale associato al referto prima di eliminarlo dal DB.
        String mediaFilename = getMediaFilenameByEsameId(idEsameReferto, scenarioId);

        // Se esiste un nome di file multimediale, tenta di eliminare il file.
        if (mediaFilename != null && !mediaFilename.isEmpty()) {
            fileStorageService.deleteFile(mediaFilename);
            logger.info("File media '{}' dell'esame con ID {} eliminato con successo.", mediaFilename, idEsameReferto);
        }

        // Query SQL per eliminare il referto dal database.
        final String sql = "DELETE FROM EsameReferto WHERE id_esame = ? AND id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEsameReferto);
            stmt.setInt(2, scenarioId);
            int rowsAffected = stmt.executeUpdate(); // Esegue l'eliminazione.
            if (rowsAffected > 0) {
                logger.info("Referto esame con ID {} eliminato con successo per lo scenario con ID {}.", idEsameReferto, scenarioId);
                return true;
            } else {
                logger.warn("Nessun referto esame trovato con ID {} per lo scenario con ID {}. Nessuna eliminazione effettuata.", idEsameReferto, scenarioId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione del referto esame con ID {} per lo scenario con ID {}: {}", idEsameReferto, scenarioId, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Recupera il nome del file multimediale associato a un esame specifico in un dato scenario.
     *
     * @param idEsame    L'ID dell'esame di cui si vuole recuperare il nome del file multimediale.
     * @param scenarioId L'ID dello scenario a cui l'esame appartiene.
     * @return Il nome del file multimediale (<code>String</code>) se trovato; <code>null</code> altrimenti.
     */
    private String getMediaFilenameByEsameId(int idEsame, int scenarioId) {
        final String sql = "SELECT media FROM EsameReferto WHERE id_esame = ? AND id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEsame);
            stmt.setInt(2, scenarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("media");
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero del nome del file multimediale per l'esame con ID {} nello scenario {}: {}",
                    idEsame, scenarioId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Aggiorna il nome del file multimediale associato a un esame specifico in un dato scenario.
     *
     * @param idEsame          L'ID dell'esame di cui aggiornare il file multimediale.
     * @param scenarioId       L'ID dello scenario a cui l'esame appartiene.
     * @param newMediaFileName Il nuovo nome del file multimediale da associare all'esame.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateMedia(int idEsame, Integer scenarioId, String newMediaFileName) {
        final String sql = "UPDATE EsameReferto SET media = ? WHERE id_esame = ? AND id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newMediaFileName);
            stmt.setInt(2, idEsame);
            stmt.setInt(3, scenarioId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Media aggiornato con successo per l'esame con ID {} nello scenario con ID {}. Nuovo file: '{}'.", idEsame, scenarioId, newMediaFileName);
                return true;
            } else {
                logger.warn("Nessun media aggiornato per l'esame con ID {} nello scenario con ID {}. Potrebbe non esistere.", idEsame, scenarioId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del media per l'esame con ID {} nello scenario con ID {}: {}", idEsame, scenarioId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Aggiorna il referto testuale di un esame specifico in un dato scenario.
     *
     * @param idEsame      L'ID dell'esame di cui aggiornare il referto testuale.
     * @param scenarioId   L'ID dello scenario a cui l'esame appartiene.
     * @param nuovoReferto Il nuovo testo del referto da salvare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateRefertoTestuale(int idEsame, Integer scenarioId, String nuovoReferto) {
        final String sql = "UPDATE EsameReferto SET referto_testuale = ? WHERE id_esame = ? AND id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoReferto);
            stmt.setInt(2, idEsame);
            stmt.setInt(3, scenarioId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Referto testuale aggiornato con successo per l'esame con ID {} nello scenario con ID {}.", idEsame, scenarioId);
                return true;
            } else {
                logger.warn("Nessun referto testuale aggiornato per l'esame con ID {} nello scenario con ID {}. Potrebbe non esistere.", idEsame, scenarioId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del referto testuale per l'esame con ID {} nello scenario con ID {}: {}", idEsame, scenarioId, e.getMessage(), e);
            return false;
        }
    }

    // Aggiungi questo metodo dentro la classe EsameRefertoService

    /**
     * Aggiunge un singolo referto a uno scenario senza eliminare quelli esistenti.
     * L'ID dell'esame viene calcolato automaticamente per evitare conflitti.
     *
     * @param esame L'oggetto EsameReferto da aggiungere. I campi idEsame e idScenario verranno impostati dal metodo.
     * @param scenarioId L'ID dello scenario a cui aggiungere il referto.
     * @return true se l'inserimento è andato a buon fine, false altrimenti.
     */
    public boolean addEsameReferto(EsameReferto esame, int scenarioId) {
        final String sql = "INSERT INTO EsameReferto (id_esame, id_scenario, tipo, media, referto_testuale) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection()) {
            // Calcola il prossimo ID disponibile per questo scenario per evitare conflitti di chiave primaria
            int nextId = getNextEsameId(scenarioId, conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, nextId);
                stmt.setInt(2, scenarioId);
                stmt.setString(3, esame.getTipo());
                stmt.setString(4, esame.getMedia());
                stmt.setString(5, esame.getRefertoTestuale());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    logger.info("Nuovo referto (Tipo: '{}') aggiunto con successo con ID {} per lo scenario ID {}.", esame.getTipo(), nextId, scenarioId);
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiunta di un nuovo referto per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Metodo helper privato per trovare il prossimo ID disponibile per un esame in un dato scenario.
     * @param scenarioId L'ID dello scenario.
     * @param conn La connessione al database.
     * @return il prossimo ID intero disponibile.
     * @throws SQLException se si verifica un errore durante la query.
     */
    private int getNextEsameId(int scenarioId, Connection conn) throws SQLException {
        final String sql = "SELECT MAX(id_esame) FROM EsameReferto WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) + 1; // Ritorna il massimo ID trovato + 1
            } else {
                return 1; // Se non ci sono esami, inizia da 1
            }
        }
    }
}