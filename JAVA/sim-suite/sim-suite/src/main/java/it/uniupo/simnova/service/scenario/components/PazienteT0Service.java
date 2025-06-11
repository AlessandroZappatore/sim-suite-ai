package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione dei dati del paziente al tempo zero (T0) all'interno di uno scenario.
 * Gestisce i parametri vitali del paziente e gli accessi vascolari (venosi e arteriosi).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class PazienteT0Service {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori relativi alle operazioni sui dati del paziente T0.
     */
    private static final Logger logger = LoggerFactory.getLogger(PazienteT0Service.class);

    /**
     * Costruttore privato per evitare l'istanza diretta di questo servizio.
     */
    private PazienteT0Service() {
        // Costruttore privato per evitare l'istanza diretta di questo servizio.
        // Utilizzare il contesto Spring per ottenere un'istanza di questo servizio.
    }

    /**
     * Recupera un oggetto {@link PazienteT0} dal database, inclusi i suoi accessi venosi e arteriosi,
     * basandosi sull'ID dello scenario a cui è associato.
     *
     * @param scenarioId L'ID dello scenario per il quale recuperare i dati del paziente T0.
     * @return L'oggetto {@link PazienteT0} corrispondente all'ID dello scenario, o <code>null</code> se non trovato
     * o in caso di errore SQL.
     */
    public PazienteT0 getPazienteT0ById(Integer scenarioId) {
        final String sqlPaziente = "SELECT * FROM PazienteT0 WHERE id_paziente = ?";
        final String sqlAccessiVenosi = "SELECT a.* FROM Accesso a JOIN AccessoVenoso av ON a.id_accesso = av.accesso_id WHERE av.paziente_t0_id = ?";
        final String sqlAccessiArteriosi = "SELECT a.* FROM Accesso a JOIN AccessoArterioso aa ON a.id_accesso = aa.accesso_id WHERE aa.paziente_t0_id = ?";

        PazienteT0 pazienteT0 = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmtPaziente = conn.prepareStatement(sqlPaziente)) {

            stmtPaziente.setInt(1, scenarioId);
            ResultSet rsPaziente = stmtPaziente.executeQuery();

            if (rsPaziente.next()) {
                // Recupera gli accessi venosi e arteriosi separatamente.
                List<Accesso> accessiVenosi = getAccessi(conn, sqlAccessiVenosi, scenarioId);
                List<Accesso> accessiArteriosi = getAccessi(conn, sqlAccessiArteriosi, scenarioId);

                // Costruisce l'oggetto PazienteT0 con i dati recuperati.
                pazienteT0 = new PazienteT0(
                        rsPaziente.getInt("id_paziente"),
                        rsPaziente.getString("PA"),
                        rsPaziente.getInt("FC"),
                        rsPaziente.getInt("RR"),
                        rsPaziente.getFloat("T"),
                        rsPaziente.getInt("SpO2"),
                        rsPaziente.getInt("FiO2"),
                        rsPaziente.getDouble("LitriOssigeno"),
                        rsPaziente.getInt("EtCO2"),
                        rsPaziente.getString("Monitor"),
                        accessiVenosi,
                        accessiArteriosi
                );
                logger.info("Paziente T0 con ID {} recuperato con successo.", scenarioId);
            } else {
                logger.warn("Nessun paziente T0 trovato con ID {}.", scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero del paziente T0 con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return pazienteT0;
    }

    /**
     * Salva tutti i dati del paziente T0 per uno scenario specifico, inclusi i parametri vitali
     * e gli accessi vascolari. L'operazione è transazionale per garantire la consistenza dei dati.
     *
     * @param scenarioId    L'ID dello scenario a cui il paziente T0 è associato.
     * @param pa            La pressione arteriosa (es. "120/80").
     * @param fc            La frequenza cardiaca.
     * @param rr            La frequenza respiratoria.
     * @param temp          La temperatura corporea.
     * @param spo2          La saturazione di ossigeno.
     * @param fio2          La frazione di ossigeno inspirato (FiO2).
     * @param litrio2       I litri di ossigeno somministrati.
     * @param etco2         L'anidride carbonica esalata (EtCO2).
     * @param monitor       Il tipo di monitor del paziente (es. "Monitoraggio ECG").
     * @param venosiData    Una {@link List} di oggetti {@link Accesso} che rappresentano gli accessi venosi.
     * @param arteriosiData Una {@link List} di oggetti {@link Accesso} che rappresentano gli accessi arteriosi.
     * @return <code>true</code> se il salvataggio è riuscito; <code>false</code> altrimenti.
     * @throws IllegalArgumentException se uno dei parametri vitali non è valido o se il formato della pressione arteriosa non è corretto.
     */
    public boolean savePazienteT0(int scenarioId,
                                  String pa, int fc, int rr, double temp,
                                  int spo2, int fio2, float litrio2, int etco2, String monitor,
                                  List<Accesso> venosiData,
                                  List<Accesso> arteriosiData) {
        Connection conn = null;
        logger.debug("Tentativo di salvare Paziente T0 per scenario ID {}. PA: {}", scenarioId, pa);

        // Validazione dei parametri in input.
        if (fc < 0) {
            logger.warn("Frequenza cardiaca non valida: {}", fc);
            throw new IllegalArgumentException("Frequenza cardiaca non valida. Deve essere un valore non negativo.");
        }
        if (rr < 0) {
            logger.warn("Frequenza respiratoria non valida: {}", rr);
            throw new IllegalArgumentException("Frequenza respiratoria non valida. Deve essere un valore non negativo.");
        }
        if (spo2 < 0 || spo2 > 100) {
            logger.warn("Saturazione di ossigeno non valida: {}", spo2);
            throw new IllegalArgumentException("Saturazione di ossigeno non valida. Deve essere tra 0 e 100.");
        }
        if (fio2 < 0 || fio2 > 100) {
            logger.warn("FiO2 non valido: {}", fio2);
            throw new IllegalArgumentException("FiO2 non valido. Deve essere tra 0 e 100.");
        }
        if (litrio2 < 0) {
            logger.warn("LitriO2 non valido: {}", litrio2);
            throw new IllegalArgumentException("LitriO2 non valido. Deve essere un valore non negativo.");
        }
        if (etco2 < 0) {
            logger.warn("EtCO2 non valido: {}", etco2);
            throw new IllegalArgumentException("EtCO2 non valido. Deve essere un valore non negativo.");
        }
        if (!pa.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
            logger.warn("Formato della pressione arteriosa non valido: '{}'.", pa);
            throw new IllegalArgumentException("Formato PA non valido. Atteso 'sistolica/diastolica' (es. '120/80').");
        }

        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Disabilita l'autocommit per gestire la transazione manualmente.

            // 1. Salva i parametri vitali del paziente.
            if (!savePazienteParams(conn, scenarioId, pa, fc, rr, temp, spo2, fio2, litrio2, etco2, monitor)) {
                conn.rollback(); // Esegue il rollback in caso di errore.
                logger.warn("Rollback della transazione: impossibile salvare i parametri vitali per lo scenario con ID {}.", scenarioId);
                return false;
            }

            // 2. Salva gli accessi venosi.
            if (!venosiData.isEmpty()) {
                if (saveAccessi(conn, scenarioId, venosiData, true)) {
                    conn.rollback();
                    logger.warn("Rollback della transazione: impossibile salvare gli accessi venosi per lo scenario con ID {}.", scenarioId);
                    return false;
                }
            }
            //3. Salva gli accessi arteriosi.
            if (!arteriosiData.isEmpty()) {
                if (saveAccessi(conn, scenarioId, arteriosiData, false)) {
                    conn.rollback();
                    logger.warn("Rollback della transazione: impossibile salvare gli accessi arteriosi per lo scenario con ID {}.", scenarioId);
                    return false;
                }
            }

            conn.commit(); // Conferma la transazione se tutte le operazioni sono riuscite.
            logger.info("Paziente T0 con ID {} salvato con successo (inclusi parametri e accessi).", scenarioId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Esegue il rollback in caso di errore SQL.
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback per lo scenario con ID {}: {}", scenarioId, ex.getMessage(), ex);
                }
            }
            logger.error("Errore SQL durante il salvataggio del paziente T0 con ID {}: {}", scenarioId, e.getMessage(), e);
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
     * Recupera una lista di accessi (venosi o arteriosi) associati a un paziente T0 di uno scenario.
     * Questo metodo è un helper interno per {@link #getPazienteT0ById(Integer)}.
     *
     * @param conn       La {@link Connection} al database da utilizzare.
     * @param sql        La query SQL specifica per recuperare il tipo di accesso desiderato.
     * @param scenarioId L'ID dello scenario (che è anche l'ID del paziente T0) a cui gli accessi sono associati.
     * @return Una {@link List} di oggetti {@link Accesso}. Restituisce una lista vuota se non vengono trovati accessi
     * o in caso di errore.
     * @throws SQLException Se si verifica un errore SQL durante l'esecuzione della query.
     */
    private List<Accesso> getAccessi(Connection conn, String sql, int scenarioId) throws SQLException {
        List<Accesso> accessi = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                accessi.add(new Accesso(
                        rs.getInt("id_accesso"),
                        rs.getString("tipologia"),
                        rs.getString("posizione"),
                        rs.getString("lato"),
                        rs.getInt("misura")
                ));
            }
            logger.info("Recuperati {} accessi per lo scenario con ID {}.", accessi.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero degli accessi per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            throw e; // Rilancia l'eccezione per essere gestita dal metodo chiamante (savePazienteT0).
        }
        return accessi;
    }

    /**
     * Salva o aggiorna i parametri vitali del paziente T0 nel database.
     * Se un paziente T0 con l'<code>scenarioId</code> specificato esiste già, i suoi parametri verranno aggiornati;
     * altrimenti, un nuovo record verrà inserito.
     *
     * @param conn       La {@link Connection} al database (gestita esternamente).
     * @param scenarioId L'ID dello scenario a cui i parametri del paziente T0 sono associati.
     * @param pa         La pressione arteriosa (es. "120/80").
     * @param fc         La frequenza cardiaca.
     * @param rr         La frequenza respiratoria.
     * @param temp       La temperatura corporea.
     * @param spo2       La saturazione di ossigeno.
     * @param fio2       La frazione di ossigeno inspirato (FiO2).
     * @param litrio2    I litri di ossigeno somministrati.
     * @param etco2      L'anidride carbonica esalata (EtCO2).
     * @param monitor    Il tipo di monitor del paziente (es. "Monitoraggio ECG").
     * @return <code>true</code> se il salvataggio o l'aggiornamento è riuscito; <code>false</code> altrimenti.
     * @throws SQLException             Se si verifica un errore SQL durante l'esecuzione della query.
     * @throws IllegalArgumentException se uno dei parametri vitali non è valido o il formato PA non è corretto.
     */
    private boolean savePazienteParams(Connection conn, int scenarioId,
                                       String pa, int fc, int rr, double temp,
                                       int spo2, int fio2, float litrio2, int etco2, String monitor) throws SQLException {
        // Verifica se un record PazienteT0 con questo ID esiste già.
        boolean exists = getPazienteT0ById(scenarioId) != null;

        // Le validazioni sui parametri sono già state fatte nel metodo chiamante,
        // ma è buona pratica mantenerle anche qui se questo metodo potesse essere chiamato direttamente.
        // Per brevità, assumo che le validazioni siano già state eseguite e non le duplico in questo estratto.

        // Determina la query SQL appropriata (UPDATE o INSERT).
        final String sql = exists ?
                "UPDATE PazienteT0 SET PA=?, FC=?, RR=?, T=?, SpO2=?, FiO2=?, LitriOssigeno=?, EtCO2=?, Monitor=? WHERE id_paziente=?" :
                "INSERT INTO PazienteT0 (id_paziente, PA, FC, RR, T, SpO2, FiO2, LitriOssigeno, EtCO2, Monitor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;

            if (exists) {
                // Imposta i parametri per la query di UPDATE.
                paramIndex = getParamIndex(pa, fc, rr, temp, spo2, fio2, litrio2, etco2, stmt, paramIndex);
                stmt.setString(paramIndex++, monitor);
                stmt.setInt(paramIndex, scenarioId); // Condizione WHERE per l'UPDATE.
            } else {
                // Imposta i parametri per la query di INSERT.
                stmt.setInt(paramIndex++, scenarioId); // L'ID dello scenario è il primo parametro per l'INSERT.
                paramIndex = getParamIndex(pa, fc, rr, temp, spo2, fio2, litrio2, etco2, stmt, paramIndex);
                stmt.setString(paramIndex, monitor);
            }

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Parametri del paziente T0 {} con ID {} con successo.", exists ? "aggiornati" : "inseriti", scenarioId);
            } else {
                logger.warn("Nessun parametro del paziente T0 {} con ID {}. Nessuna riga modificata.", exists ? "aggiornato" : "inserito", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dei parametri del paziente T0 con ID {}: {}", scenarioId, e.getMessage(), e);
            throw e; // Rilancia l'eccezione per essere gestita dal metodo chiamante (savePazienteT0).
        }
    }

    /**
     * Helper per impostare i parametri comuni (vitali) in un {@link PreparedStatement}.
     * Questo metodo è usato da {@link #savePazienteParams} per evitare la duplicazione del codice.
     *
     * @param pa         La pressione arteriosa (es. "120/80").
     * @param fc         La frequenza cardiaca.
     * @param rr         La frequenza respiratoria.
     * @param temp       La temperatura corporea.
     * @param spo2       La saturazione di ossigeno.
     * @param fio2       La frazione di ossigeno inspirato (FiO2).
     * @param litrio2    I litri di ossigeno somministrati.
     * @param etco2      L'anidride carbonica esalata (EtCO2).
     * @param stmt       Il {@link PreparedStatement} su cui impostare i parametri.
     * @param paramIndex L'indice di partenza per l'impostazione dei parametri.
     * @return L'indice successivo disponibile per il prossimo parametro.
     * @throws SQLException Se si verifica un errore SQL durante l'impostazione dei parametri.
     */
    private int getParamIndex(String pa, int fc, int rr, double temp, int spo2, int fio2, float litrio2, int etco2, PreparedStatement stmt, int paramIndex) throws SQLException {
        stmt.setString(paramIndex++, pa);
        stmt.setInt(paramIndex++, fc);
        stmt.setInt(paramIndex++, rr);
        stmt.setDouble(paramIndex++, temp);
        stmt.setInt(paramIndex++, spo2);
        stmt.setInt(paramIndex++, fio2);
        stmt.setFloat(paramIndex++, litrio2);
        stmt.setInt(paramIndex++, etco2);
        return paramIndex;
    }

    /**
     * Salva una lista di accessi (venosi o arteriosi) per un paziente T0 di uno scenario.
     * Questa operazione comporta la rimozione degli accessi esistenti del tipo specificato
     * per lo scenario, seguita dall'inserimento dei nuovi accessi.
     *
     * @param conn        La {@link Connection} al database (gestita esternamente).
     * @param scenarioId  L'ID dello scenario a cui gli accessi sono associati (corrisponde all'ID del paziente T0).
     * @param accessiData Una {@link List} di oggetti {@link Accesso} da salvare.
     * @param isVenoso    <code>true</code> se gli accessi da salvare sono venosi; <code>false</code> se sono arteriosi.
     * @return <code>true</code> se si è verificato un errore durante il salvataggio; <code>false</code> altrimenti.
     * La logica di ritorno è invertita rispetto a un tipico "successo/fallimento" per facilitare il rollback nel chiamante.
     * @throws SQLException Se si verifica un errore SQL durante l'esecuzione delle query.
     */
    private boolean saveAccessi(Connection conn, int scenarioId,
                                List<Accesso> accessiData,
                                boolean isVenoso) throws SQLException {
        if (accessiData == null || accessiData.isEmpty()) {
            logger.info("Nessun accesso {} da salvare per lo scenario con ID {}. Saltato.", isVenoso ? "venoso" : "arterioso", scenarioId);
            return true; // Nessun errore, semplicemente non c'è nulla da salvare.
        }

        // 1. Elimina le relazioni esistenti (AccessoVenoso o AccessoArterioso) per questo paziente T0.
        final String deleteRelSql = isVenoso ?
                "DELETE FROM AccessoVenoso WHERE paziente_t0_id=?" :
                "DELETE FROM AccessoArterioso WHERE paziente_t0_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(deleteRelSql)) {
            stmt.setInt(1, scenarioId);
            int deletedRelations = stmt.executeUpdate();
            logger.info("Eliminate {} relazioni di accesso {} per lo scenario con ID {}.", deletedRelations, isVenoso ? "venoso" : "arterioso", scenarioId);
        }

        // 2. Inserisci i nuovi accessi nella tabella `Accesso` e le loro relazioni.
        final String insertAccessoSql = "INSERT INTO Accesso (tipologia, posizione, lato, misura) VALUES (?, ?, ?, ?)";
        final String insertRelSql = isVenoso ?
                "INSERT INTO AccessoVenoso (paziente_t0_id, accesso_id) VALUES (?, ?)" :
                "INSERT INTO AccessoArterioso (paziente_t0_id, accesso_id) VALUES (?, ?)";

        for (Accesso data : accessiData) {
            int accessoId;
            // Inserisce il dettaglio dell'accesso nella tabella `Accesso` e recupera l'ID generato.
            try (PreparedStatement stmt = conn.prepareStatement(insertAccessoSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, data.getTipologia());
                stmt.setString(2, data.getPosizione());
                stmt.setString(3, data.getLato());
                stmt.setInt(4, data.getMisura());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        accessoId = rs.getInt(1);
                    } else {
                        logger.warn("Impossibile ottenere l'ID generato per il nuovo accesso. Potenziale problema di integrità dati.");
                        return true; // Indica un errore.
                    }
                }
            }

            // Inserisce la relazione tra il paziente T0 e il nuovo accesso.
            try (PreparedStatement stmt = conn.prepareStatement(insertRelSql)) {
                stmt.setInt(1, scenarioId);
                stmt.setInt(2, accessoId);
                stmt.executeUpdate();
                logger.info("Accesso {} con ID {} inserito per lo scenario con ID {}.", isVenoso ? "venoso" : "arterioso", accessoId, scenarioId);
            }
        }
        return false; // Nessun errore durante il salvataggio degli accessi.
    }

    /**
     * Aggiorna il campo "Monitor" del paziente T0 associato a uno scenario specifico.
     *
     * @param scenarioId L'ID dello scenario (e del paziente T0) di cui aggiornare il monitor.
     * @param monitor    Il nuovo valore della stringa di monitoraggio.
     */
    public void saveMonitor(int scenarioId, String monitor) {
        final String sql = "UPDATE PazienteT0 SET Monitor=? WHERE id_paziente=?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, monitor);
            stmt.setInt(2, scenarioId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Monitor del paziente T0 per lo scenario con ID {} aggiornato a: '{}'.", scenarioId, monitor);
            } else {
                logger.warn("Nessun monitor aggiornato per lo scenario con ID {}. Il paziente T0 potrebbe non esistere.", scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del monitor per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Elimina un accesso (venoso o arterioso) specifico associato a un paziente T0 di uno scenario.
     * Vengono eliminate sia la relazione tra l'accesso e il paziente, sia l'accesso stesso dalla tabella `Accesso`.
     *
     * @param scenarioId L'ID dello scenario (e del paziente T0) a cui l'accesso è associato.
     * @param accessoId  L'ID dell'accesso da eliminare.
     * @param isVenoso   <code>true</code> se l'accesso da eliminare è venoso; <code>false</code> se è arterioso.
     */
    public void deleteAccesso(int scenarioId, int accessoId, boolean isVenoso) {
        final String deleteAccessoSql = "DELETE FROM Accesso WHERE id_accesso=?";
        final String deleteRelSql = isVenoso ?
                "DELETE FROM AccessoVenoso WHERE paziente_t0_id=? AND accesso_id=?" :
                "DELETE FROM AccessoArterioso WHERE paziente_t0_id=? AND accesso_id=?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmtRel = conn.prepareStatement(deleteRelSql);
             PreparedStatement stmtAccesso = conn.prepareStatement(deleteAccessoSql)) {

            // 1. Elimina la relazione tra l'accesso e il paziente T0.
            stmtRel.setInt(1, scenarioId);
            stmtRel.setInt(2, accessoId);
            int relRowsAffected = stmtRel.executeUpdate();
            if (relRowsAffected > 0) {
                logger.info("Relazione accesso {} con ID {} eliminata per lo scenario con ID {}.", isVenoso ? "venoso" : "arterioso", accessoId, scenarioId);
            } else {
                logger.warn("Nessuna relazione accesso {} trovata con ID {} per lo scenario con ID {}. Potrebbe essere già stata eliminata.", isVenoso ? "venoso" : "arterioso", accessoId, scenarioId);
            }

            // 2. Elimina l'accesso dalla tabella principale `Accesso`.
            stmtAccesso.setInt(1, accessoId);
            int accRowsAffected = stmtAccesso.executeUpdate();
            if (accRowsAffected > 0) {
                logger.info("Accesso con ID {} eliminato dalla tabella Accesso.", accessoId);
            } else {
                logger.warn("Nessun accesso con ID {} trovato nella tabella Accesso. Potrebbe essere già stato eliminato.", accessoId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione dell'accesso {} con ID {} per lo scenario con ID {}: {}", isVenoso ? "venoso" : "arterioso", accessoId, scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Aggiunge un nuovo accesso (venoso o arterioso) per un paziente T0 di uno scenario.
     * Questo metodo inserisce prima i dettagli dell'accesso nella tabella `Accesso`
     * e poi crea la relazione appropriata nella tabella `AccessoVenoso` o `AccessoArterioso`.
     *
     * @param scenarioId L'ID dello scenario (e del paziente T0) a cui aggiungere l'accesso.
     * @param accesso    L'oggetto {@link Accesso} contenente i dettagli del nuovo accesso.
     * @param isVenoso   <code>true</code> se l'accesso da aggiungere è venoso; <code>false</code> se è arterioso.
     */
    public void addAccesso(int scenarioId, Accesso accesso, boolean isVenoso) {
        final String insertAccessoSql = "INSERT INTO Accesso (tipologia, posizione, lato, misura) VALUES (?, ?, ?, ?)";
        final String insertRelSql = isVenoso ?
                "INSERT INTO AccessoVenoso (paziente_t0_id, accesso_id) VALUES (?, ?)" :
                "INSERT INTO AccessoArterioso (paziente_t0_id, accesso_id) VALUES (?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmtAccesso = conn.prepareStatement(insertAccessoSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtRel = conn.prepareStatement(insertRelSql)) {

            // Inserisce il nuovo accesso e recupera l'ID generato.
            stmtAccesso.setString(1, accesso.getTipologia());
            stmtAccesso.setString(2, accesso.getPosizione());
            stmtAccesso.setString(3, accesso.getLato());
            stmtAccesso.setInt(4, accesso.getMisura());
            int rowsAffected = stmtAccesso.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmtAccesso.getGeneratedKeys()) {
                    if (rs.next()) {
                        int accessoId = rs.getInt(1);
                        // Crea la relazione tra il paziente T0 e il nuovo accesso.
                        stmtRel.setInt(1, scenarioId);
                        stmtRel.setInt(2, accessoId);
                        stmtRel.executeUpdate();
                        logger.info("Accesso {} con ID {} aggiunto per lo scenario con ID {}.", isVenoso ? "venoso" : "arterioso", accessoId, scenarioId);
                    } else {
                        logger.warn("Impossibile ottenere l'ID generato per l'accesso. Il nuovo accesso potrebbe non essere stato salvato correttamente.");
                    }
                }
            } else {
                logger.warn("Nessuna riga modificata durante l'inserimento del nuovo accesso. Il salvataggio potrebbe essere fallito.");
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiunta dell'accesso {} per lo scenario con ID {}: {}", isVenoso ? "venoso" : "arterioso", scenarioId, e.getMessage(), e);
        }
    }
}