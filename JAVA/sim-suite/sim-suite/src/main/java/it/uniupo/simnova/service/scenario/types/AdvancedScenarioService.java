package it.uniupo.simnova.service.scenario.types;

import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servizio per la gestione degli scenari avanzati.
 * Questo servizio estende le funzionalità di {@link ScenarioService} per gestire logiche
 * e dati specifici degli scenari avanzati, come la gestione dei tempi e dei parametri aggiuntivi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class AdvancedScenarioService {

    /**
     * Il logger per questa classe, utilizzato per registrare le operazioni e gli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(AdvancedScenarioService.class);

    /**
     * Il servizio di base per la gestione degli scenari, iniettato per riutilizzare le funzionalità comuni.
     */
    private final ScenarioService scenarioService;

    /**
     * Costruisce una nuova istanza di <code>AdvancedScenarioService</code>.
     * Inietta il servizio {@link ScenarioService} di base.
     *
     * @param scenarioService Il servizio di base per la gestione degli scenari.
     */
    public AdvancedScenarioService(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    /**
     * Avvia la creazione di un nuovo scenario di tipo "avanzato".
     * Questo metodo crea prima un record base dello scenario tramite {@link ScenarioService#startQuickScenario},
     * quindi aggiunge un record nella tabella <code>AdvancedScenario</code> associandolo all'ID dello scenario base.
     *
     * @param titolo        Il titolo dello scenario.
     * @param nomePaziente  Il nome del paziente associato allo scenario.
     * @param patologia     La patologia del paziente.
     * @param autori        Gli autori dello scenario.
     * @param timerGenerale Il timer generale preimpostato per lo scenario.
     * @param tipologia     La tipologia specifica dello scenario (dovrebbe essere "Advanced Scenario").
     * @return L'ID (<code>int</code>) dello scenario avanzato appena creato; <code>-1</code> in caso di errore
     * durante la creazione dello scenario base o l'inserimento del record in <code>AdvancedScenario</code>.
     */
    public int startAdvancedScenario(String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        // Delega la creazione del record base dello scenario al ScenarioService.
        int scenarioId = scenarioService.startQuickScenario(-1, titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);

        if (scenarioId > 0) {
            // Se lo scenario base è stato creato con successo, aggiunge un record in AdvancedScenario.
            final String sql = "INSERT INTO AdvancedScenario (id_advanced_scenario) VALUES (?)";

            try (Connection conn = DBConnect.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, scenarioId);
                stmt.executeUpdate();
                logger.info("Record 'AdvancedScenario' creato con successo per lo scenario ID: {}.", scenarioId);

            } catch (SQLException e) {
                logger.error("Errore SQL durante l'inserimento del record 'AdvancedScenario' per lo scenario ID {}: {}. Si consiglia un controllo di consistenza dei dati.", scenarioId, e.getMessage(), e);
                // In caso di errore, si potrebbe voler eliminare lo scenario base appena creato per evitare orfani.
                return -1;
            }
        } else {
            logger.error("Impossibile creare lo scenario base. ID restituito: {}.", scenarioId);
        }
        return scenarioId;
    }

    /**
     * Recupera tutti gli oggetti {@link Tempo} associati a uno scenario avanzato specifico.
     * Ogni oggetto <code>Tempo</code> recuperato include anche la sua lista di {@link ParametroAggiuntivo ParametriAggiuntivi} correlati.
     *
     * @param scenarioId L'ID dello scenario avanzato per cui recuperare i tempi.
     * @return Una {@link List} di oggetti {@link Tempo} ordinati per <code>id_tempo</code>.
     * Restituisce una lista vuota in caso di errore o se non sono presenti tempi.
     */
    public List<Tempo> getTempiByScenarioId(int scenarioId) {
        final String sql = "SELECT * FROM Tempo WHERE id_advanced_scenario = ? ORDER BY id_tempo";
        List<Tempo> tempi = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Costruisce l'oggetto Tempo dai dati del ResultSet.
                Tempo tempo = new Tempo(
                        rs.getInt("id_tempo"),
                        rs.getInt("id_advanced_scenario"),
                        rs.getString("PA"),
                        (Integer) rs.getObject("FC"), // Usa getObject per tipi che possono essere NULL nel DB.
                        (Integer) rs.getObject("RR"),
                        rs.getFloat("T"),
                        (Integer) rs.getObject("SpO2"),
                        (Integer) rs.getObject("FiO2"),
                        (Double) rs.getObject("LitriOssigeno"),
                        (Integer) rs.getObject("EtCO2"),
                        rs.getString("Azione"),
                        rs.getInt("TSi_id"),
                        rs.getInt("TNo_id"),
                        rs.getString("altri_dettagli"),
                        rs.getInt("timer_tempo"), // Assumendo che sia INT nel DB, altrimenti usare getLong.
                        rs.getString("ruoloGenitore")
                );

                // Recupera i parametri aggiuntivi per il tempo corrente.
                int tempoId = tempo.getIdTempo();
                List<ParametroAggiuntivo> parametriAggiuntivi = getParametriAggiuntiviByTempoId(tempoId, scenarioId);
                tempo.setParametriAggiuntivi(parametriAggiuntivi);

                tempi.add(tempo);
            }
            logger.info("Recuperati {} tempi per lo scenario avanzato con ID {}.", tempi.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero dei tempi per lo scenario avanzato con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return tempi;
    }

    /**
     * Salva una lista di oggetti {@link Tempo} associati a uno scenario avanzato specifico.
     * L'operazione è transazionale: prima vengono eliminati tutti i tempi e i loro parametri aggiuntivi esistenti
     * per lo scenario, quindi vengono inseriti i nuovi tempi e i loro parametri.
     *
     * @param scenarioId L'ID dello scenario avanzato in cui salvare i tempi.
     * @param tempi      La {@link List} di oggetti {@link Tempo} da salvare.
     * @return <code>true</code> se tutti i tempi e i loro parametri aggiuntivi sono stati salvati correttamente; <code>false</code> altrimenti.
     * @throws IllegalArgumentException se un parametro vitale o di transizione non è valido.
     */
    public boolean saveTempi(int scenarioId, List<Tempo> tempi) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Disabilita l'autocommit per gestire la transazione.

            // 1. Elimina i tempi e i parametri aggiuntivi esistenti per lo scenario.
            if (!deleteTempi(conn, scenarioId)) {
                conn.rollback();
                logger.warn("Rollback: impossibile eliminare i tempi e/o i parametri aggiuntivi esistenti per lo scenario ID {}.", scenarioId);
                return false;
            }

            // 2. Inserisce i nuovi tempi.
            final String sql = "INSERT INTO Tempo (id_tempo, id_advanced_scenario, PA, FC, RR, T, SpO2, FiO2, LitriOssigeno, EtCO2, Azione, TSi_id, TNo_id, altri_dettagli, timer_tempo, RuoloGenitore) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Tempo tempo : tempi) {
                    // Validazione dei campi prima dell'inserimento.
                    Integer fc = tempo.getFC();
                    Integer rr = tempo.getRR();
                    Integer spo2 = tempo.getSpO2();
                    Integer fio2 = tempo.getFiO2();
                    Double litrio2 = tempo.getLitriO2();
                    Integer etco2 = tempo.getEtCO2();
                    String pa = tempo.getPA();
                    int tsi = tempo.getTSi();
                    int tno = tempo.getTNo();

                    if (fc != null && fc < 0) {
                        logger.warn("Frequenza cardiaca non valida per tempo ID {}: {}", tempo.getIdTempo(), fc);
                        throw new IllegalArgumentException("Frequenza cardiaca non valida.");
                    }
                    if (rr != null && rr < 0) {
                        logger.warn("Frequenza respiratoria non valida per tempo ID {}: {}", tempo.getIdTempo(), rr);
                        throw new IllegalArgumentException("Frequenza respiratoria non valida.");
                    }
                    if (spo2 != null && (spo2 < 0 || spo2 > 100)) {
                        logger.warn("Saturazione di ossigeno non valida per tempo ID {}: {}", tempo.getIdTempo(), spo2);
                        throw new IllegalArgumentException("Saturazione di ossigeno non valida, deve essere tra 0 e 100.");
                    }
                    if (fio2 != null && (fio2 < 0 || fio2 > 100)) {
                        logger.warn("FiO2 non valido per tempo ID {}: {}", tempo.getIdTempo(), fio2);
                        throw new IllegalArgumentException("FiO2 non valido, deve essere tra 0 e 100.");
                    }
                    if (litrio2 != null && litrio2 < 0) {
                        logger.warn("LitriO2 non valido per tempo ID {}: {}", tempo.getIdTempo(), litrio2);
                        throw new IllegalArgumentException("LitriO2 non valido.");
                    }
                    if (etco2 != null && etco2 < 0) {
                        logger.warn("EtCO2 non valido per tempo ID {}: {}", tempo.getIdTempo(), etco2);
                        throw new IllegalArgumentException("EtCO2 non valido.");
                    }
                    // La regex verifica il formato "sistolica/diastolica".
                    if (pa != null && !pa.isEmpty() && !pa.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                        logger.warn("Formato della pressione arteriosa non valido per tempo ID {}: '{}'.", tempo.getIdTempo(), pa);
                        throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80').");
                    }
                    if (tsi < 0 || tno < 0) { // Gli ID delle transizioni non possono essere negativi.
                        logger.warn("ID TSi o TNo non valido per tempo ID {}: TSi={}, TNo={}.", tempo.getIdTempo(), tsi, tno);
                        throw new IllegalArgumentException("ID TSi o TNo non valido. Devono essere valori non negativi.");
                    }

                    // Imposta i parametri dello statement.
                    stmt.setInt(1, tempo.getIdTempo());
                    stmt.setInt(2, scenarioId);
                    stmt.setString(3, pa);
                    stmt.setObject(4, fc); // setObject gestisce i valori nulli correttamente.
                    stmt.setObject(5, rr);
                    stmt.setDouble(6, Math.round(tempo.getT() * 10) / 10.0); // Arrotonda a una cifra decimale.
                    stmt.setObject(7, spo2);
                    stmt.setObject(8, fio2);
                    stmt.setObject(9, litrio2);
                    stmt.setObject(10, etco2);
                    stmt.setString(11, tempo.getAzione());
                    stmt.setInt(12, tempo.getTSi());
                    stmt.setInt(13, tempo.getTNo());
                    stmt.setString(14, tempo.getAltriDettagli());
                    stmt.setLong(15, tempo.getTimerTempo());
                    stmt.setString(16, tempo.getRuoloGenitore());

                    stmt.addBatch(); // Aggiunge l'operazione al batch.
                }

                int[] results = stmt.executeBatch(); // Esegue tutte le operazioni in batch.
                // Verifica che tutte le righe siano state inserite correttamente.
                for (int result : results) {
                    if (result <= 0 && result != Statement.SUCCESS_NO_INFO) { // Statement.SUCCESS_NO_INFO indica successo ma senza info sul numero di righe.
                        conn.rollback();
                        logger.warn("Rollback: impossibile inserire uno o più tempi per lo scenario ID {}. Alcuni tempi potrebbero non essere stati salvati.", scenarioId);
                        return false;
                    }
                }
                logger.info("Tempi salvati con successo per lo scenario ID {}. Totale tempi salvati: {}.", scenarioId, results.length);
            }

            // 3. Salva i parametri aggiuntivi per i tempi appena inseriti.
            if (!saveParametriAggiuntivi(conn, scenarioId, tempi)) {
                conn.rollback();
                logger.warn("Rollback: impossibile salvare i parametri aggiuntivi per lo scenario ID {}. I tempi potrebbero non essere stati salvati correttamente.", scenarioId);
                return false;
            }

            conn.commit(); // Conferma la transazione se tutte le operazioni sono riuscite.
            logger.info("Tutti i dati dei tempi e dei parametri aggiuntivi per lo scenario ID {} sono stati salvati con successo.", scenarioId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Esegue il rollback in caso di errore SQL.
                    logger.error("Errore SQL durante il salvataggio dei tempi per lo scenario ID {}: {}", scenarioId, e.getMessage());
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback della transazione per lo scenario ID {}: {}", scenarioId, ex.getMessage(), ex);
                }
            }
            logger.error("Errore critico durante il salvataggio dei tempi per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("Errore di validazione dei dati durante il salvataggio dei tempi per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback dopo IllegalArgumentException per lo scenario ID {}: {}", scenarioId, ex.getMessage(), ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina l'autocommit, indipendentemente dal successo.
                    conn.close(); // Chiude la connessione.
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione o il ripristino dell'autocommit per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Recupera una lista di oggetti {@link ParametroAggiuntivo} associati a un tempo specifico
     * e a uno scenario avanzato.
     *
     * @param tempoId    L'ID del tempo per cui recuperare i parametri aggiuntivi.
     * @param scenarioId L'ID dello scenario avanzato in cui si trova il tempo.
     * @return Una {@link List} di oggetti {@link ParametroAggiuntivo}.
     * Restituisce una lista vuota in caso di errore o se non sono presenti parametri aggiuntivi.
     */
    public List<ParametroAggiuntivo> getParametriAggiuntiviByTempoId(int tempoId, int scenarioId) {
        final String sql = "SELECT * FROM ParametriAggiuntivi WHERE tempo_id = ? AND scenario_id = ?";
        List<ParametroAggiuntivo> parametri = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tempoId);
            stmt.setInt(2, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ParametroAggiuntivo param = new ParametroAggiuntivo(
                        rs.getInt("parametri_aggiuntivi_id"),
                        rs.getInt("tempo_id"),
                        rs.getInt("scenario_id"),
                        rs.getString("nome"),
                        rs.getString("valore"),
                        rs.getString("unità_misura")
                );
                parametri.add(param);
            }
            logger.debug("Recuperati {} parametri aggiuntivi per il tempo ID {} nello scenario ID {}.", parametri.size(), tempoId, scenarioId);
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero dei parametri aggiuntivi per il tempo ID {} nello scenario ID {}: {}", tempoId, scenarioId, e.getMessage(), e);
        }
        return parametri;
    }

    /**
     * Salva i {@link ParametroAggiuntivo ParametriAggiuntivi} per tutti i tempi di uno scenario.
     * Questo metodo è destinato a essere chiamato all'interno di una transazione più ampia.
     *
     * @param conn       La {@link Connection} al database (gestita esternamente).
     * @param scenarioId L'ID dello scenario a cui i parametri aggiuntivi sono associati.
     * @param tempi      La {@link List} di oggetti {@link Tempo} che contengono i parametri aggiuntivi da salvare.
     * @return <code>true</code> se i parametri aggiuntivi sono stati salvati correttamente; <code>false</code> altrimenti.
     * @throws SQLException Se si verifica un errore SQL durante l'interazione con il database.
     */
    private boolean saveParametriAggiuntivi(Connection conn, int scenarioId, List<Tempo> tempi) throws SQLException {
        // Query SQL per l'inserimento dei parametri aggiuntivi.
        final String sql = "INSERT INTO ParametriAggiuntivi (parametri_aggiuntivi_id, tempo_id, scenario_id, nome, valore, unità_misura) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Recupera l'ID massimo corrente per i parametri aggiuntivi e lo incrementa per i nuovi ID.
            int paramId = getMaxParamId(conn) + 1;
            int totalParamsAdded = 0;

            for (Tempo tempo : tempi) {
                List<ParametroAggiuntivo> parametri = tempo.getParametriAggiuntivi();
                if (parametri != null && !parametri.isEmpty()) {
                    for (ParametroAggiuntivo param : parametri) {
                        stmt.setInt(1, paramId++); // Assegna un nuovo ID unico.
                        stmt.setInt(2, tempo.getIdTempo());
                        stmt.setInt(3, scenarioId);
                        stmt.setString(4, param.getNome());
                        // Gestisce la conversione del valore a Double.
                        stmt.setDouble(5, Double.parseDouble(param.getValore()));
                        stmt.setString(6, param.getUnitaMisura());
                        stmt.addBatch(); // Aggiunge l'operazione al batch.
                        totalParamsAdded++;
                    }
                }
            }

            // Esegue il batch solo se ci sono parametri da aggiungere.
            if (totalParamsAdded > 0) {
                int[] results = stmt.executeBatch();
                boolean allSuccess = true;
                for (int result : results) {
                    if (result <= 0 && result != Statement.SUCCESS_NO_INFO) {
                        allSuccess = false;
                        logger.warn("Errore nel salvataggio di alcuni parametri aggiuntivi per lo scenario ID {}. Una o più righe non sono state inserite correttamente.", scenarioId);
                    }
                }
                if (allSuccess) {
                    logger.info("Salvati {} parametri aggiuntivi totali per lo scenario ID {}.", totalParamsAdded, scenarioId);
                }
                return allSuccess;
            } else {
                logger.info("Nessun parametro aggiuntivo da aggiungere per lo scenario ID {}.", scenarioId);
                return true; // Nessun parametro da salvare, quindi è un successo.
            }

        } catch (SQLException e) {
            logger.error("Errore SQL durante l'inserimento dei parametri aggiuntivi per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            throw e; // Rilancia l'eccezione per essere gestita dal chiamante (saveTempi).
        } catch (NumberFormatException e) {
            logger.error("Errore di formato numerico durante la conversione del valore di un parametro aggiuntivo per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            throw new SQLException("Errore di formato numerico nel valore del parametro aggiuntivo.", e); // Rilancia come SQLException.
        }
    }

    /**
     * Elimina tutti gli oggetti {@link Tempo} e i loro {@link ParametroAggiuntivo ParametriAggiuntivi}
     * associati a uno scenario avanzato specifico.
     * Questo metodo è destinato a essere chiamato all'interno di una transazione più ampia.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario avanzato da cui eliminare i tempi.
     * @return <code>true</code> se i tempi e i loro parametri aggiuntivi sono stati eliminati correttamente; <code>false</code> altrimenti.
     * @throws SQLException Se si verifica un errore SQL durante l'interazione con il database.
     */
    public boolean deleteTempi(Connection conn, int scenarioId) throws SQLException {
        // Prima elimina i parametri aggiuntivi correlati, per mantenere l'integrità referenziale.
        if (!deleteParametriAggiuntivi(conn, scenarioId)) {
            logger.warn("Impossibile eliminare i parametri aggiuntivi per lo scenario ID {}. Non è possibile procedere con l'eliminazione dei tempi.", scenarioId);
            return false;
        }

        // Quindi elimina i tempi stessi.
        final String sql = "DELETE FROM Tempo WHERE id_advanced_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int rowsAffected = stmt.executeUpdate();
            logger.info("Eliminati {} tempi per lo scenario ID {}.", rowsAffected, scenarioId);
            // Restituisce true se l'operazione è stata eseguita senza errori (anche se 0 righe eliminate).
            return rowsAffected >= 0;
        }
    }

    /**
     * Elimina tutti i {@link ParametroAggiuntivo ParametriAggiuntivi} associati a uno scenario specifico.
     * Questo metodo è destinato a essere chiamato all'interno di una transazione più ampia.
     *
     * @param conn       La {@link Connection} al database.
     * @param scenarioId L'ID dello scenario da cui eliminare i parametri aggiuntivi.
     * @return <code>true</code> se i parametri aggiuntivi sono stati eliminati correttamente; <code>false</code> altrimenti.
     * @throws SQLException Se si verifica un errore SQL durante l'interazione con il database.
     */
    private boolean deleteParametriAggiuntivi(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM ParametriAggiuntivi WHERE scenario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            int rowsAffected = stmt.executeUpdate();
            logger.debug("Eliminati {} parametri aggiuntivi per lo scenario ID {}.", rowsAffected, scenarioId);
            // Restituisce true se l'operazione è stata eseguita senza errori (anche se 0 righe eliminate).
            return rowsAffected >= 0;
        }
    }

    /**
     * Recupera l'ID massimo attualmente presente nella tabella <code>ParametriAggiuntivi</code>.
     * Utilizzato per generare nuovi ID univoci per i parametri aggiuntivi durante il salvataggio.
     *
     * @param conn La {@link Connection} al database.
     * @return L'ID massimo dei parametri aggiuntivi; <code>0</code> se la tabella è vuota o in caso di errore.
     * @throws SQLException Se si verifica un errore SQL durante l'interazione con il database.
     */
    private int getMaxParamId(Connection conn) throws SQLException {
        final String sql = "SELECT MAX(parametri_aggiuntivi_id) FROM ParametriAggiuntivi";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1); // Il primo (e unico) risultato è il MAX ID.
            }
            return 0; // Se non ci sono righe, il MAX ID è 0.
        }
    }

    /**
     * Aggiorna il campo "Azione" di un tempo specifico in uno scenario avanzato.
     *
     * @param idTempo    L'ID del tempo da aggiornare.
     * @param scenarioId L'ID dello scenario a cui il tempo appartiene.
     * @param newValue   Il nuovo valore della stringa "Azione" da impostare.
     */
    public void setAzione(int idTempo, int scenarioId, String newValue) {
        final String sql = "UPDATE Tempo SET Azione = ? WHERE id_tempo = ? AND id_advanced_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newValue);
            stmt.setInt(2, idTempo);
            stmt.setInt(3, scenarioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("Campo 'Azione' aggiornato con successo per il tempo ID {} nello scenario ID {}.", idTempo, scenarioId);
            } else {
                logger.warn("Nessun campo 'Azione' aggiornato per il tempo ID {} nello scenario ID {}. Il tempo potrebbe non esistere.", idTempo, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento del campo 'Azione' per il tempo ID {} nello scenario ID {}: {}", idTempo, scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Aggiorna il campo "RuoloGenitore" di un tempo specifico in uno scenario avanzato.
     *
     * @param idTempo    L'ID del tempo da aggiornare.
     * @param scenarioId L'ID dello scenario a cui il tempo appartiene.
     * @param newValue   Il nuovo valore della stringa "RuoloGenitore" da impostare.
     */
    public void setRuoloGenitore(int idTempo, int scenarioId, String newValue) {
        final String sql = "UPDATE Tempo SET RuoloGenitore = ? WHERE id_tempo = ? AND id_advanced_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newValue);
            stmt.setInt(2, idTempo);
            stmt.setInt(3, scenarioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("Campo 'RuoloGenitore' aggiornato con successo per il tempo ID {} nello scenario ID {}.", idTempo, scenarioId);
            } else {
                logger.warn("Nessun campo 'RuoloGenitore' aggiornato per il tempo ID {} nello scenario ID {}. Il tempo potrebbe non esistere.", idTempo, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento del campo 'RuoloGenitore' per il tempo ID {} nello scenario ID {}: {}", idTempo, scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Aggiorna gli ID delle transizioni (TSi_id e TNo_id) per un tempo specifico in uno scenario avanzato.
     * Questi ID rappresentano i collegamenti ad altri tempi per le transizioni "Sì" e "No".
     *
     * @param idTempo    L'ID del tempo da aggiornare.
     * @param scenarioId L'ID dello scenario a cui il tempo appartiene.
     * @param newTSi     Il nuovo ID del tempo di transizione "Sì".
     * @param newTNo     Il nuovo ID del tempo di transizione "No".
     */
    public void setTransitions(int idTempo, int scenarioId, int newTSi, int newTNo) {
        final String sql = "UPDATE Tempo SET TSi_id = ?, TNo_id = ? WHERE id_tempo = ? AND id_advanced_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newTSi);
            stmt.setInt(2, newTNo);
            stmt.setInt(3, idTempo);
            stmt.setInt(4, scenarioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("Transizioni (TSi: {}, TNo: {}) aggiornate con successo per il tempo ID {} nello scenario ID {}.", newTSi, newTNo, idTempo, scenarioId);
            } else {
                logger.warn("Nessuna transizione aggiornata per il tempo ID {} nello scenario ID {}. Il tempo potrebbe non esistere.", idTempo, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento delle transizioni per il tempo ID {} nello scenario ID {}: {}", idTempo, scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Aggiorna il campo "altri_dettagli" di un tempo specifico in uno scenario avanzato.
     *
     * @param idTempo    L'ID del tempo da aggiornare.
     * @param scenarioId L'ID dello scenario a cui il tempo appartiene.
     * @param newValue   Il nuovo valore della stringa "altri_dettagli" da impostare.
     */
    public void setDettagliAggiuntivi(int idTempo, int scenarioId, String newValue) {
        final String sql = "UPDATE Tempo SET altri_dettagli = ? WHERE id_tempo = ? AND id_advanced_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newValue);
            stmt.setInt(2, idTempo);
            stmt.setInt(3, scenarioId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                logger.info("Dettagli aggiuntivi aggiornati con successo per il tempo ID {} nello scenario ID {}.", idTempo, scenarioId);
            } else {
                logger.warn("Nessun dettaglio aggiuntivo aggiornato per il tempo ID {} nello scenario ID {}. Il tempo potrebbe non esistere.", idTempo, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento dei dettagli aggiuntivi per il tempo ID {} nello scenario ID {}: {}", idTempo, scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Salva o aggiorna un parametro vitale per un tempo specifico o per il paziente T0 (tempo 0).
     * Il metodo distingue tra parametri vitali standard (come PA, FC) e parametri aggiuntivi definiti dall'utente.
     * Per i parametri vitali standard, aggiorna la colonna corrispondente nella tabella <code>Tempo</code> o <code>PazienteT0</code>.
     * Per i parametri aggiuntivi, li aggiorna o inserisce nella tabella <code>ParametriAggiuntivi</code>.
     *
     * @param scenarioId L'ID dello scenario.
     * @param tempoId    L'ID del tempo a cui il parametro è associato. Usare <code>null</code> o <code>0</code> per il paziente T0 (tempo 0).
     * @param label      Il nome del parametro (es. "PA", "FC", "Temperatura", o un nome di parametro aggiuntivo).
     * @param newValue   Il nuovo valore del parametro da salvare (come stringa, la conversione sarà gestita internamente).
     */
    @SuppressWarnings("SqlSourceToSinkFlow") // Soppresso perché `colonnaReale` è validata da una mappa fissa.
    public void saveVitalSign(Integer scenarioId, Integer tempoId, String label, String newValue) {

        Map<String, String> colonneLecite = Map.of(
                "PA", "PA",
                "FC", "FC",
                "RR", "RR",
                "T", "T",
                "SpO₂", "SpO2",
                "FiO₂", "FiO2",
                "Litri O₂", "LitriOssigeno",
                "EtCO₂", "EtCO2"
        );

        String colonnaReale = colonneLecite.get(label);
        Connection conn = null;
        if (colonnaReale != null) {
            try {
                conn = DBConnect.getInstance().getConnection();
                conn.setAutoCommit(false); // Inizia la transazione per le operazioni correlate.

                if (tempoId == null || tempoId == 0) { // Se è il tempo 0 (Paziente T0).
                    // Aggiorna la tabella PazienteT0.
                    String sqlPaziente = "UPDATE PazienteT0 SET " + colonnaReale + " = ? WHERE id_paziente = ?";
                    try (PreparedStatement stmtPaziente = conn.prepareStatement(sqlPaziente)) {
                        stmtPaziente.setString(1, newValue);
                        stmtPaziente.setInt(2, scenarioId);
                        stmtPaziente.executeUpdate();
                    }

                    // Verifica se esiste un record Tempo con ID 0 per questo scenario.
                    String sqlCheckTempo = "SELECT COUNT(*) FROM Tempo WHERE id_advanced_scenario = ? AND id_tempo = 0";
                    try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheckTempo)) {
                        checkStmt.setInt(1, scenarioId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) > 0) {
                            // Se esiste, aggiorna anche la tabella Tempo per il tempo 0.
                            String sqlTempoZero = "UPDATE Tempo SET " + colonnaReale + " = ? WHERE id_advanced_scenario = ? AND id_tempo = 0";
                            try (PreparedStatement stmtTempoZero = conn.prepareStatement(sqlTempoZero)) {
                                stmtTempoZero.setString(1, newValue);
                                stmtTempoZero.setInt(2, scenarioId);
                                stmtTempoZero.executeUpdate();
                            }
                        }
                    }
                    conn.commit(); // Conferma la transazione.
                    logger.info("Parametro vitale '{}' aggiornato con successo per PazienteT0 e Tempo(0) nello scenario ID {}.", label, scenarioId);
                } else { // Se è un tempo diverso da 0.
                    String sqlTempo = "UPDATE Tempo SET " + colonnaReale + " = ? WHERE id_advanced_scenario = ? AND id_tempo = ?";
                    try (PreparedStatement stmtTempo = conn.prepareStatement(sqlTempo)) {
                        stmtTempo.setString(1, newValue);
                        stmtTempo.setInt(2, scenarioId);
                        stmtTempo.setInt(3, tempoId);

                        int rowsUpdated = stmtTempo.executeUpdate();
                        if (rowsUpdated > 0) {
                            logger.info("Parametro vitale '{}' aggiornato con successo per il tempo ID {} nello scenario ID {}.", label, tempoId, scenarioId);
                        } else {
                            logger.warn("Nessun parametro vitale '{}' aggiornato per il tempo ID {} nello scenario ID {}. Il tempo potrebbe non esistere.", label, tempoId, scenarioId);
                        }
                    }
                    conn.commit(); // Conferma la transazione.
                }
            } catch (SQLException e) {
                logger.error("Errore SQL durante l'aggiornamento del parametro vitale '{}' per scenario ID {} e tempo ID {}: {}", label, scenarioId, tempoId, e.getMessage(), e);
                if (conn != null) {
                    try {
                        conn.rollback();
                        logger.warn("Rollback della transazione eseguito per l'aggiornamento del parametro vitale.");
                    } catch (SQLException ex) {
                        logger.error("Errore durante il rollback dopo l'errore SQL per l'aggiornamento del parametro vitale: {}", ex.getMessage(), ex);
                    }
                }
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true); // Ripristina l'autocommit.
                    } catch (SQLException e) {
                        logger.error("Errore nel ripristino dell'autocommit per l'aggiornamento del parametro vitale: {}", e.getMessage(), e);
                    }
                    try {
                        conn.close(); // Chiude la connessione.
                    } catch (SQLException e) {
                        logger.error("Errore chiusura connessione nel finally per l'aggiornamento del parametro vitale: {}", e.getMessage(), e);
                    }
                }
            }
        } else { // Se la label non è un parametro vitale standard, si assume sia un parametro aggiuntivo.
            // Se tempoId è null o 0, lo imposta a 0 per la tabella ParametriAggiuntivi.
            Integer actualTempoId = (tempoId == null) ? 0 : tempoId;

            logger.info("Parametro '{}' riconosciuto come aggiuntivo. Verrà gestito per il tempo ID {} dello scenario ID {}.", label, actualTempoId, scenarioId);

            try (Connection localConn = DBConnect.getInstance().getConnection()) {
                // Controlla se il parametro aggiuntivo esiste già per questo tempo e scenario.
                String checkSql = "SELECT parametri_aggiuntivi_id FROM ParametriAggiuntivi WHERE tempo_id = ? AND scenario_id = ? AND nome = ?";
                try (PreparedStatement checkStmt = localConn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, actualTempoId);
                    checkStmt.setInt(2, scenarioId);
                    checkStmt.setString(3, label);

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            // Se il parametro esiste, lo aggiorna.
                            int paramId = rs.getInt("parametri_aggiuntivi_id");
                            String updateSql = "UPDATE ParametriAggiuntivi SET valore = ? WHERE parametri_aggiuntivi_id = ?";

                            try (PreparedStatement updateStmt = localConn.prepareStatement(updateSql)) {
                                updateStmt.setString(1, newValue);
                                updateStmt.setInt(2, paramId);

                                int rowsUpdated = updateStmt.executeUpdate();
                                if (rowsUpdated > 0) {
                                    logger.info("Parametro aggiuntivo '{}' aggiornato con successo per il tempo ID {} nello scenario ID {}.", label, actualTempoId, scenarioId);
                                } else {
                                    logger.warn("Nessun parametro aggiuntivo '{}' aggiornato per il tempo ID {} nello scenario ID {}. Potrebbe non esistere o il valore è lo stesso.", label, actualTempoId, scenarioId);
                                }
                            }
                        } else {
                            // Se il parametro non esiste, lo inserisce.
                            int maxId = getMaxParamId(localConn) + 1; // Genera un nuovo ID.
                            String insertSql = "INSERT INTO ParametriAggiuntivi (parametri_aggiuntivi_id, tempo_id, scenario_id, nome, valore, unità_misura) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";

                            try (PreparedStatement insertStmt = localConn.prepareStatement(insertSql)) {
                                insertStmt.setInt(1, maxId);
                                insertStmt.setInt(2, actualTempoId);
                                insertStmt.setInt(3, scenarioId);
                                insertStmt.setString(4, label);
                                insertStmt.setString(5, newValue);
                                insertStmt.setString(6, ""); // L'unità di misura può essere aggiunta se presente nel JSON.

                                int rowsInserted = insertStmt.executeUpdate();
                                if (rowsInserted > 0) {
                                    logger.info("Nuovo parametro aggiuntivo '{}' creato con successo per il tempo ID {} nello scenario ID {}.", label, actualTempoId, scenarioId);
                                } else {
                                    logger.warn("Impossibile creare il parametro aggiuntivo '{}' per il tempo ID {} nello scenario ID {}. Nessuna riga inserita.", label, actualTempoId, scenarioId);
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("Errore SQL durante la gestione del parametro aggiuntivo '{}' per il tempo ID {} nello scenario ID {}: {}", label, actualTempoId, scenarioId, e.getMessage(), e);
            }
        }
    }

    /**
     * Elimina un oggetto {@link Tempo} specifico da uno scenario avanzato.
     * L'eliminazione comporta anche la rimozione dei {@link ParametroAggiuntivo ParametriAggiuntivi} correlati a quel tempo.
     *
     * @param idTempo    L'ID del tempo da eliminare.
     * @param scenarioId L'ID dello scenario avanzato da cui eliminare il tempo.
     */
    public void deleteTempo(int idTempo, int scenarioId) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia la transazione.

            // Prima elimina i parametri aggiuntivi associati a questo tempo.
            deleteAdditionalParamsForTempo(conn, idTempo, scenarioId);

            // Quindi elimina il tempo stesso.
            final String sql = "DELETE FROM Tempo WHERE id_tempo = ? AND id_advanced_scenario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idTempo);
                stmt.setInt(2, scenarioId);

                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    logger.info("Tempo con ID {} eliminato con successo dallo scenario ID {}.", idTempo, scenarioId);
                } else {
                    logger.warn("Nessun tempo trovato con ID {} nello scenario ID {}. Nessuna eliminazione effettuata.", idTempo, scenarioId);
                }
            }
            conn.commit(); // Conferma la transazione.
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'eliminazione del tempo ID {} dallo scenario ID {}: {}", idTempo, scenarioId, e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback(); // Esegue il rollback in caso di errore.
                    logger.warn("Rollback della transazione eseguito per l'eliminazione del tempo ID {}.", idTempo);
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback dopo l'eliminazione del tempo: {}", ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina l'autocommit.
                    conn.close(); // Chiude la connessione.
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione o il ripristino dell'autocommit nel finally: {}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Elimina tutti i parametri aggiuntivi associati a un tempo specifico in uno scenario.
     * Questo è un metodo ausiliario per {@link #deleteTempo(int, int)}.
     *
     * @param conn       La {@link Connection} al database.
     * @param tempoId    L'ID del tempo da cui eliminare i parametri aggiuntivi.
     * @param scenarioId L'ID dello scenario.
     * @throws SQLException se si verifica un errore SQL.
     */
    private void deleteAdditionalParamsForTempo(Connection conn, int tempoId, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM ParametriAggiuntivi WHERE tempo_id = ? AND scenario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tempoId);
            stmt.setInt(2, scenarioId);
            int rowsDeleted = stmt.executeUpdate();
            logger.debug("Eliminati {} parametri aggiuntivi per il tempo ID {} nello scenario ID {}.", rowsDeleted, tempoId, scenarioId);
        }
    }

    /**
     * Elimina un {@link ParametroAggiuntivo} specifico associato a un tempo in uno scenario.
     *
     * @param scenarioId L'ID dello scenario.
     * @param tempoId    L'ID del tempo a cui il parametro aggiuntivo è associato.
     * @param nome       Il nome del parametro aggiuntivo da eliminare.
     */
    public void deleteAdditionalParam(Integer scenarioId, Integer tempoId, String nome) {
        final String sql = "DELETE FROM ParametriAggiuntivi WHERE tempo_id = ? AND scenario_id = ? AND nome = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tempoId);
            stmt.setInt(2, scenarioId);
            stmt.setString(3, nome);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                logger.info("Parametro aggiuntivo '{}' eliminato con successo per il tempo ID {} nello scenario ID {}.", nome, tempoId, scenarioId);
            } else {
                logger.warn("Nessun parametro aggiuntivo trovato con nome '{}' per il tempo ID {} nello scenario ID {}. Nessuna eliminazione effettuata.", nome, tempoId, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'eliminazione del parametro aggiuntivo '{}' per il tempo ID {} nello scenario ID {}: {}", nome, tempoId, scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Aggiunge un nuovo {@link ParametroAggiuntivo} a un tempo specifico in uno scenario.
     * Se <code>tempoId</code> è <code>null</code>, il parametro viene associato al tempo 0 (Paziente T0).
     *
     * @param scenarioId L'ID dello scenario.
     * @param tempoId    L'ID del tempo a cui aggiungere il parametro. Può essere <code>null</code> per il tempo 0.
     * @param newParam   L'oggetto {@link ParametroAggiuntivo} da aggiungere.
     */
    public void addAdditionalParam(Integer scenarioId, Integer tempoId, ParametroAggiuntivo newParam) {
        final String sql = "INSERT INTO ParametriAggiuntivi (parametri_aggiuntivi_id, tempo_id, scenario_id, nome, valore, unità_misura) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // Se tempoId è null, si assume che sia per il tempo 0 (Paziente T0).
        Integer actualTempoId = (tempoId != null) ? tempoId : 0;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int maxId = getMaxParamId(conn) + 1; // Genera un nuovo ID univoco per il parametro aggiuntivo.

            stmt.setInt(1, maxId);
            stmt.setInt(2, actualTempoId);
            stmt.setInt(3, scenarioId);
            stmt.setString(4, newParam.getNome());
            // Il valore viene convertito in String, assumendo che ParametroAggiuntivo.getValore() restituisca già una Stringa.
            stmt.setString(5, newParam.getValore());
            stmt.setString(6, newParam.getUnitaMisura());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                logger.info("Parametro aggiuntivo '{}' aggiunto con successo per il tempo ID {} nello scenario ID {}.", newParam.getNome(), actualTempoId, scenarioId);
            } else {
                logger.warn("Impossibile aggiungere il parametro aggiuntivo '{}' per il tempo ID {} nello scenario ID {}. Nessuna riga inserita.", newParam.getNome(), actualTempoId, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiunta del parametro aggiuntivo '{}' per il tempo ID {} nello scenario ID {}: {}", newParam.getNome(), actualTempoId, scenarioId, e.getMessage(), e);
        }
    }
}