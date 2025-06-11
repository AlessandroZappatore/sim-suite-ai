package it.uniupo.simnova.service.scenario;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione degli scenari.
 * Fornisce metodi per recuperare, creare e aggiornare i dati degli scenari nel database.
 * Questo servizio gestisce le operazioni CRUD di base per gli scenari, inclusa
 * la determinazione del tipo di scenario (Quick, Advanced, Patient Simulated).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@SuppressWarnings({"LoggingSimilarMessage"}) // Sopprime l'avviso di PMD per messaggi di log simili, usato con cautela.
@Service
public class ScenarioService {

    /**
     * Il logger per questa classe, utilizzato per registrare le operazioni e gli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioService.class);

    /**
     * Costruttore privato per prevenire l'istanza diretta del servizio.
     * Utilizzare il contesto Spring per ottenere un'istanza di questo servizio.
     */
    private ScenarioService() {
        // Costruttore privato per prevenire l'istanza diretta del servizio.
        // Utilizzare il contesto Spring per ottenere un'istanza di questo servizio.
    }

    /**
     * Recupera un oggetto {@link Scenario} completo dal database utilizzando il suo identificativo.
     *
     * @param id L'identificativo (<code>Integer</code>) dello scenario da recuperare.
     * @return L'oggetto {@link Scenario} corrispondente all'identificativo fornito; <code>null</code> se non trovato
     * o in caso di errore SQL.
     */
    public Scenario getScenarioById(Integer id) {
        final String sql = "SELECT * FROM Scenario WHERE id_scenario = ?";
        Scenario scenario = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id); // Imposta l'ID dello scenario come parametro.
            ResultSet rs = stmt.executeQuery(); // Esegue la query.

            if (rs.next()) {
                // Costruisce l'oggetto Scenario popolando tutti i campi dal ResultSet.
                scenario = new Scenario(
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
                        rs.getString("tipologia_paziente"),
                        rs.getString("info_genitore"),
                        rs.getString("target")
                );
                logger.info("Scenario con ID {} recuperato con successo.", id);
            } else {
                logger.warn("Nessuno scenario trovato con ID {}.", id);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero dello scenario con ID {}: {}", id, e.getMessage(), e);
        }
        return scenario;
    }

    /**
     * Recupera una lista di tutti gli scenari presenti nel database.
     * Per motivi di performance e di visualizzazione, vengono recuperati solo i campi essenziali.
     *
     * @return Una {@link List} di oggetti {@link Scenario} contenente gli scenari principali.
     * Restituisce una lista vuota in caso di errore o se non sono presenti scenari.
     */
    public List<Scenario> getAllScenarios() {
        final String sql = "SELECT id_scenario, titolo, autori, patologia, descrizione, tipologia_paziente FROM Scenario";
        List<Scenario> scenarios = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Costruisce oggetti Scenario con un sottoinsieme di campi.
                Scenario scenario = new Scenario(
                        rs.getInt("id_scenario"),
                        rs.getString("titolo"),
                        rs.getString("autori"),
                        rs.getString("patologia"),
                        rs.getString("descrizione"),
                        rs.getString("tipologia_paziente"));
                scenarios.add(scenario);
            }
            logger.info("Recuperati {} scenari dal database.", scenarios.size());
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero di tutti gli scenari: {}", e.getMessage(), e);
        }
        return scenarios;
    }

    /**
     * Crea un nuovo scenario rapido o aggiorna uno esistente nel database.
     * Se <code>scenarioId</code> è <code>null</code> o lo scenario non esiste, ne viene creato uno nuovo.
     * Altrimenti, lo scenario esistente viene aggiornato.
     *
     * @param scenarioId    L'ID (<code>Integer</code>) dello scenario da aggiornare. Se <code>null</code>, un nuovo scenario sarà creato.
     * @param titolo        Il titolo dello scenario.
     * @param nomePaziente  Il nome del paziente associato.
     * @param patologia     La patologia del paziente.
     * @param autori        I nomi degli autori dello scenario.
     * @param timerGenerale Il timer generale preimpostato per lo scenario.
     * @param tipologia     La tipologia del paziente (es. "Adulto", "Pediatrico").
     * @return L'ID (<code>int</code>) dello scenario creato o aggiornato; <code>-1</code> in caso di errore.
     */
    public int startQuickScenario(Integer scenarioId, String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        try (Connection conn = DBConnect.getInstance().getConnection()) {
            // Verifica se lo scenario esiste per determinare se fare un UPDATE o un INSERT.
            if (scenarioId != null && existScenario(scenarioId)) {
                // Aggiorna uno scenario esistente.
                final String updateSql = "UPDATE Scenario SET titolo=?, nome_paziente=?, patologia=?, autori=?, timer_generale=?, tipologia_paziente=? WHERE id_scenario=?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, titolo);
                    stmt.setString(2, nomePaziente);
                    stmt.setString(3, patologia);
                    stmt.setString(4, autori);
                    stmt.setFloat(5, timerGenerale);
                    stmt.setString(6, tipologia);
                    stmt.setInt(7, scenarioId);

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        logger.info("Scenario con ID {} aggiornato con successo.", scenarioId);
                        return scenarioId;
                    } else {
                        logger.warn("Nessun scenario aggiornato con ID {}. Potrebbe non esistere o il valore è lo stesso.", scenarioId);
                        return -1;
                    }
                }
            } else {
                // Crea un nuovo scenario.
                final String insertSql = "INSERT INTO Scenario (titolo, nome_paziente, patologia, autori, timer_generale, tipologia_paziente) VALUES (?,?,?,?,?,?)";
                // Specifica Statement.RETURN_GENERATED_KEYS per recuperare l'ID generato automaticamente.
                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, titolo);
                    stmt.setString(2, nomePaziente);
                    stmt.setString(3, patologia);
                    stmt.setString(4, autori);
                    stmt.setFloat(5, timerGenerale);
                    stmt.setString(6, tipologia);

                    int affectedRows = stmt.executeUpdate();
                    logger.debug("Tentativo di inserimento scenario: {} righe interessate.", affectedRows);

                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int generatedId = generatedKeys.getInt(1);
                                logger.info("Nuovo scenario creato con ID: {}.", generatedId);
                                return generatedId;
                            } else {
                                logger.error("Creazione scenario fallita: nessun ID generato restituito.");
                                return -1;
                            }
                        }
                    } else {
                        logger.warn("Creazione scenario fallita: nessuna riga inserita.");
                        return -1;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'inserimento/aggiornamento dello scenario: {}", e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Aggiorna il campo <code>descrizione</code> dello scenario specificato.
     *
     * @param scenarioId  L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param descrizione La nuova descrizione (<code>String</code>) da impostare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioDescription(int scenarioId, String descrizione) {
        return updateScenarioField(scenarioId, "descrizione", descrizione);
    }

    /**
     * Aggiorna il campo <code>briefing</code> dello scenario specificato.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param briefing   Il nuovo briefing (<code>String</code>) da impostare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioBriefing(int scenarioId, String briefing) {
        return updateScenarioField(scenarioId, "briefing", briefing);
    }

    /**
     * Aggiorna il campo <code>patto_aula</code> dello scenario specificato.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param patto_aula Il nuovo patto aula (<code>String</code>) da impostare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioPattoAula(int scenarioId, String patto_aula) {
        return updateScenarioField(scenarioId, "patto_aula", patto_aula);
    }

    /**
     * Aggiorna il campo <code>obiettivo</code> (obiettivi didattici) dello scenario specificato.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param obiettivo  Il nuovo obiettivo didattico (<code>String</code>) da impostare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioObiettiviDidattici(int scenarioId, String obiettivo) {
        return updateScenarioField(scenarioId, "obiettivo", obiettivo);
    }

    /**
     * Aggiorna il campo <code>moulage</code> dello scenario specificato.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param moulage    Il nuovo moulage (<code>String</code>) da impostare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioMoulage(int scenarioId, String moulage) {
        return updateScenarioField(scenarioId, "moulage", moulage);
    }

    /**
     * Aggiorna il campo <code>liquidi</code> dello scenario specificato.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param liquidi    I nuovi liquidi (<code>String</code>) da impostare.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioLiquidi(int scenarioId, String liquidi) {
        return updateScenarioField(scenarioId, "liquidi", liquidi);
    }

    /**
     * Aggiorna un campo generico dello scenario nel database.
     * Questo è un metodo helper privato utilizzato dagli altri metodi <code>updateScenarioXxx</code>.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param fieldName  Il nome della colonna nel database da aggiornare. **Deve essere validato per prevenire SQL Injection.**
     *                   In questo contesto, si assume che <code>fieldName</code> sia passato da metodi che garantiscono la sua sicurezza.
     * @param value      Il nuovo valore (<code>String</code>) da impostare per il campo.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo (almeno una riga modificata); <code>false</code> altrimenti.
     */
    private boolean updateScenarioField(int scenarioId, String fieldName, String value) {
        final String sql = "UPDATE Scenario SET " + fieldName + " = ? WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Campo '{}' dello scenario con ID {} aggiornato con successo.", fieldName, scenarioId);
            } else {
                logger.warn("Nessun campo '{}' dello scenario con ID {} aggiornato. Potrebbe non esistere o il valore è lo stesso.", fieldName, scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento del campo '{}' dello scenario con ID {}: {}", fieldName, scenarioId, e.getMessage(), e);
            return false;
        }
    }


    /**
     * Determina il tipo di scenario (Quick, Advanced, o Patient Simulated) in base alla sua presenza
     * nelle tabelle specifiche (<code>Scenario</code>, <code>AdvancedScenario</code>, <code>PatientSimulatedScenario</code>).
     *
     * @param idScenario L'ID (<code>int</code>) dello scenario di cui determinare il tipo.
     * @return Una {@link String} che rappresenta il tipo di scenario (es. "Quick Scenario", "Advanced Scenario", "Patient Simulated Scenario").
     * Restituisce "ScenarioNotFound" se l'ID non è presente in nessuna delle tabelle pertinenti.
     */
    public String getScenarioType(int idScenario) {
        // Uno scenario Quick è presente solo nella tabella Scenario, non in AdvancedScenario.
        if (isPresentInTable(idScenario, "Scenario") &&
                !isPresentInTable(idScenario, "AdvancedScenario")) {
            return "Quick Scenario";
        }

        // Se è un Advanced Scenario (e quindi anche in Scenario), verifica se è anche un Patient Simulated Scenario.
        if (isPresentInTable(idScenario, "AdvancedScenario")) {
            if (isPresentInTable(idScenario, "PatientSimulatedScenario")) {
                return "Patient Simulated Scenario";
            }
            return "Advanced Scenario";
        }

        // Se non rientra in nessuna delle categorie precedenti, lo scenario non è stato trovato o è di tipo sconosciuto.
        return "ScenarioNotFound";
    }

    /**
     * Controlla se un dato ID è presente come chiave primaria in una tabella specificata.
     * Questo è un metodo generico per verificare l'esistenza di record nelle tabelle correlate allo scenario.
     *
     * @param id        L'ID (<code>int</code>) da cercare.
     * @param tableName Il nome (<code>String</code>) della tabella in cui cercare.
     *                  I nomi delle tabelle supportati sono "Scenario", "AdvancedScenario", "PatientSimulatedScenario".
     * @return <code>true</code> se l'ID è presente nella tabella; <code>false</code> altrimenti o se il nome della tabella non è riconosciuto.
     */
    public boolean isPresentInTable(int id, String tableName) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE ";

        // Costruisce la clausola WHERE in base al nome della tabella per usare la colonna ID corretta.
        switch (tableName) {
            case "Scenario":
                sql += "id_scenario = ?";
                break;
            case "AdvancedScenario":
                sql += "id_advanced_scenario = ?";
                break;
            case "PatientSimulatedScenario":
                sql += "id_patient_simulated_scenario = ?";
                break;
            default:
                logger.warn("Tabella non riconosciuta: '{}'. Impossibile verificare la presenza dell'ID.", tableName);
                return false;
        }

        // La soppressione di "SqlSourceToSinkFlow" è giustificata poiché `tableName` è controllato da un switch.
        //noinspection SqlSourceToSinkFlow
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next(); // Se rs.next() restituisce true, significa che è stata trovata una riga.
            logger.debug("Verifica presenza dell'ID {} nella tabella '{}': {}.", id, tableName, exists);
            return exists;

        } catch (SQLException e) {
            logger.error("Errore SQL durante la verifica della presenza dell'ID {} nella tabella '{}': {}", id, tableName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Controlla se uno scenario esiste nel database in base al suo ID.
     * Questo è un metodo di convenienza che chiama {@link #isPresentInTable(int, String)} per la tabella <code>Scenario</code>.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da verificare.
     * @return <code>true</code> se lo scenario esiste; <code>false</code> altrimenti.
     */
    public boolean existScenario(int scenarioId) {
        return isPresentInTable(scenarioId, "Scenario");
    }

    /**
     * Verifica se uno scenario è di tipo "Pediatrico" basandosi sulla sua tipologia paziente.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da controllare.
     * @return <code>true</code> se la tipologia paziente dello scenario è "Pediatrico" (case-insensitive); <code>false</code> altrimenti.
     */
    public boolean isPediatric(int scenarioId) {
        final String sql = "SELECT tipologia_paziente FROM Scenario WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String tipologiaPaziente = rs.getString("tipologia_paziente");
                return "Pediatrico".equalsIgnoreCase(tipologiaPaziente);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero della tipologia paziente per lo scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Aggiorna il campo <code>info_genitore</code> dello scenario specificato.
     * Questo campo contiene informazioni destinate ai genitori, ad esempio in scenari pediatrici.
     *
     * @param scenarioId L'ID (<code>Integer</code>) dello scenario da aggiornare.
     * @param value      Il nuovo valore (<code>String</code>) delle informazioni per i genitori.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioGenitoriInfo(Integer scenarioId, String value) {
        final String sql = "UPDATE Scenario SET info_genitore = ? WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Informazioni per i genitori aggiornate con successo per lo scenario con ID {}.", scenarioId);
            } else {
                logger.warn("Nessuna informazione aggiornata per i genitori dello scenario con ID {}. Potrebbe non esistere o il valore è lo stesso.", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento delle informazioni per i genitori dello scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Aggiorna il campo <code>target</code> dello scenario specificato.
     * Questo campo indica il pubblico di riferimento o l'obiettivo generale dello scenario.
     *
     * @param scenarioId L'ID (<code>Integer</code>) dello scenario da aggiornare.
     * @param target     Il nuovo valore (<code>String</code>) per il target dello scenario.
     * @return <code>true</code> se l'aggiornamento è avvenuto con successo; <code>false</code> altrimenti.
     */
    public boolean updateScenarioTarget(Integer scenarioId, String target) {
        final String sql = "UPDATE Scenario SET target = ? WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, target);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Target aggiornato con successo per lo scenario con ID {}.", scenarioId);
            } else {
                logger.warn("Nessun target aggiornato per lo scenario con ID {}. Potrebbe non esistere o il valore è lo stesso.", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento del target dello scenario con ID {}: {}", scenarioId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Aggiorna contemporaneamente il titolo e gli autori di uno scenario.
     *
     * @param scenarioId L'ID (<code>Integer</code>) dello scenario da aggiornare.
     * @param newTitle   Il nuovo titolo (<code>String</code>) da impostare.
     * @param newAuthors I nuovi autori (<code>String</code>) da impostare.
     */
    public void updateScenarioTitleAndAuthors(Integer scenarioId, String newTitle, String newAuthors) {
        final String sql = "UPDATE Scenario SET titolo = ?, autori = ? WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newTitle);
            stmt.setString(2, newAuthors);
            stmt.setInt(3, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Titolo e autori aggiornati con successo per lo scenario con ID {}. Nuovo titolo: '{}', Nuovi autori: '{}'.", scenarioId, newTitle, newAuthors);
            } else {
                logger.warn("Nessun titolo o autore aggiornato per lo scenario con ID {}. Potrebbe non esistere o i valori sono gli stessi.", scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento del titolo e degli autori dello scenario con ID {}: {}", scenarioId, e.getMessage(), e);
        }
    }

    /**
     * Aggiorna un singolo campo dello scenario, identificato da un <code>label</code> logico,
     * con un nuovo valore. I <code>label</code> vengono mappati ai nomi delle colonne del database.
     *
     * @param id       L'ID (<code>int</code>) dello scenario da aggiornare.
     * @param label    Il <code>label</code> logico del campo da aggiornare. I valori supportati sono:
     *                 <ul>
     *                 <li>"Paziente" (mappa a <code>nome_paziente</code>)</li>
     *                 <li>"Patologia" (mappa a <code>patologia</code>)</li>
     *                 <li>"Tipologia" (mappa a <code>tipologia_paziente</code>)</li>
     *                 <li>"Durata" (mappa a <code>timer_generale</code>)</li>
     *                 </ul>
     * @param newValue Il nuovo valore (<code>String</code>) da impostare per il campo.
     *                 Per il campo "Durata", il valore sarà convertito in un tipo numerico appropriato dal database.
     * @throws IllegalArgumentException se il <code>label</code> fornito non è riconosciuto.
     */
    public void updateSingleField(int id, String label, String newValue) {
        String dbLabel;
        // Mappa il label logico al nome della colonna nel database.
        switch (label) {
            case "Paziente" -> dbLabel = "nome_paziente";
            case "Patologia" -> dbLabel = "patologia";
            case "Tipologia" -> dbLabel = "tipologia_paziente";
            case "Durata" -> dbLabel = "timer_generale";
            default -> {
                logger.warn("Label non valido per l'aggiornamento di un singolo campo: '{}'.", label);
                throw new IllegalArgumentException("Label non valido: " + label + ". Non corrisponde a nessun campo aggiornabile.");
            }
        }
        String sql = "UPDATE Scenario SET " + dbLabel + " = ? WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newValue);
            stmt.setInt(2, id);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Campo '{}' aggiornato con successo per lo scenario con ID {}. Nuovo valore: '{}'.", label, id, newValue);
            } else {
                logger.warn("Nessun campo '{}' aggiornato per lo scenario con ID {}. Il record potrebbe non esistere o il valore è lo stesso.", label, id);
            }
        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento del campo '{}' dello scenario con ID {}: {}", label, id, e.getMessage(), e);
        }
    }
}