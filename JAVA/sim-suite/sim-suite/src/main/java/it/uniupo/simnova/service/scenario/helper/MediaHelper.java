package it.uniupo.simnova.service.scenario.helper;

import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe di utilità per la gestione dei file media associati agli esami e referti.
 * Fornisce metodi per verificare se un file è attualmente in uso nel database
 * e per recuperare i nomi dei file media collegati a uno scenario specifico.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class MediaHelper {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni sui file media.
     */
    private static final Logger logger = LoggerFactory.getLogger(MediaHelper.class);

    /**
     * Costruttore privato per evitare l'istanza della classe, dato che contiene solo metodi statici.
     */
    private MediaHelper() {
        // Costruttore privato per evitare l'istanza della classe.
        // Questa classe contiene solo metodi statici.
    }

    /**
     * Verifica se un determinato file media è attualmente associato a un esame o referto nel database.
     * Questo è utile per determinare se un file può essere eliminato in sicurezza dallo storage.
     *
     * @param filename Il nome del file (<code>String</code>) da verificare. Non deve essere <code>null</code> o vuoto.
     * @return <code>true</code> se il file è in uso (ovvero, se esiste almeno un record in <code>EsameReferto</code>
     * che punta a questo filename); <code>false</code> altrimenti o in caso di nome file non valido.
     */
    public static boolean isFileInUse(String filename) {
        if (filename == null || filename.isBlank()) {
            logger.warn("Nome file non valido fornito per il controllo dell'utilizzo. Restituzione falso.");
            return false;
        }

        final String sql = "SELECT COUNT(*) FROM EsameReferto WHERE media = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, filename);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Se il conteggio è maggiore di 0, il file è in uso.
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante il controllo dell'utilizzo del file '{}': {}", filename, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Recupera una lista di tutti i nomi dei file media associati agli esami e referti di uno scenario specifico.
     * Vengono inclusi solo i file media il cui nome non è <code>NULL</code> nel database.
     *
     * @param scenarioId L'ID dello scenario (<code>int</code>) per il quale recuperare i file media.
     * @return Una {@link List} di {@link String} contenente i nomi dei file media.
     * Restituisce una lista vuota se non vengono trovati file o in caso di errore SQL.
     */
    public static List<String> getMediaFilesForScenario(int scenarioId) {
        final String sql = "SELECT media FROM EsameReferto WHERE id_scenario = ? AND media IS NOT NULL";
        List<String> files = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String media = rs.getString("media");
                // Aggiunge il nome del file solo se non è nullo o vuoto, per sicurezza.
                if (media != null && !media.isEmpty()) {
                    files.add(media);
                }
            }
            logger.info("Recuperati {} file media per lo scenario con ID {}.", files.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei file media per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return files;
    }
}