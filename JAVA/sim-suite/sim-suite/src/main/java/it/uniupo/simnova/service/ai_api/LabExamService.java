package it.uniupo.simnova.service.ai_api;

import it.uniupo.simnova.domain.respons_model.LabCategory;
import it.uniupo.simnova.domain.respons_model.LabExamSet;
import it.uniupo.simnova.domain.respons_model.LabTest;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.export.LabExamPdfExportService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione degli esami di laboratorio associati agli scenari.
 * Fornisce metodi per salvare e recuperare set completi di esami,
 * e orchestra la generazione di report PDF e il loro collegamento allo scenario.
 *
 * @author Tuo Nome
 * @version 1.1
 */
@Service
public class LabExamService {

    private static final Logger logger = LoggerFactory.getLogger(LabExamService.class);

    private final LabExamPdfExportService labExamPdfExportService;
    private final ScenarioService scenarioService;
    private final EsameRefertoService esameRefertoService;

    /**
     * Costruisce una nuova istanza di LabExamService.
     *
     * @param labExamPdfExportService Servizio per generare il PDF degli esami.
     * @param scenarioService         Servizio per recuperare i dati dello scenario.
     * @param esameRefertoService     Servizio per collegare il referto allo scenario.
     */
    public LabExamService(LabExamPdfExportService labExamPdfExportService, ScenarioService scenarioService, EsameRefertoService esameRefertoService) {
        this.labExamPdfExportService = labExamPdfExportService;
        this.scenarioService = scenarioService;
        this.esameRefertoService = esameRefertoService;
    }

    /**
     * Salva un set completo di esami di laboratorio, genera il relativo report PDF,
     * e collega il PDF e i referti testuali allo scenario in modo sicuro e non distruttivo.
     *
     * @param scenarioId L'ID dello scenario a cui associare gli esami.
     * @param labExamSet L'oggetto LabExamSet contenente tutte le categorie e i test da salvare.
     * @return true se tutte le operazioni (salvataggio DB, generazione PDF, collegamento) vanno a buon fine.
     */
    public boolean saveLabExamsAndGeneratePdf(int scenarioId, LabExamSet labExamSet) {
        // 1. Salva i dati degli esami nelle tabelle dedicate (`EsamiLaboratorio`, etc.)
        boolean dbSaveSuccess = saveLabExamsToDb(scenarioId, labExamSet);

        if (dbSaveSuccess) {
            try {
                // 2. Recupera l'oggetto Scenario per ottenere i dati necessari per il PDF (es. nome paziente)
                Scenario scenario = scenarioService.getScenarioById(scenarioId);
                if (scenario == null) {
                    logger.error("Impossibile generare il PDF: scenario con ID {} non trovato.", scenarioId);
                    return false;
                }

                // 3. Genera e salva il PDF, ottenendo il nome del file come riferimento
                String pdfFilename = labExamPdfExportService.generateAndSaveLabExamPdf(labExamSet, scenario);

                // 4. Concatena tutti i referti testuali in un unico blocco di testo
                String refertoTestualeCompleto = labExamSet.getCategorie().stream()
                        .flatMap(category -> category.getTest().stream())
                        .map(test -> test.getNome() + ": " + test.getReferto())
                        .collect(Collectors.joining("\n"));

                // 5. Crea un nuovo oggetto EsameReferto e lo aggiunge allo scenario
                EsameReferto nuovoReferto = new EsameReferto(-1, scenarioId);
                nuovoReferto.setTipo("Esami di Laboratorio");
                nuovoReferto.setMedia(pdfFilename);
                nuovoReferto.setRefertoTestuale(refertoTestualeCompleto);

                if (!esameRefertoService.addEsameReferto(nuovoReferto, scenarioId)) {
                    logger.error("Fallimento nel collegare il referto PDF allo scenario ID {}", scenarioId);
                    return false;
                }

                logger.info("PDF e referto testuale collegati con successo allo scenario ID {}", scenarioId);
                return true;

            } catch (IOException e) {
                logger.error("Fallimento nella generazione o salvataggio del PDF per lo scenario ID {}. I dati sul DB sono stati salvati.", scenarioId, e);
                return false;
            }
        }

        return false;
    }

    /**
     * Metodo privato che gestisce il salvataggio dei dati degli esami di laboratorio nel DB.
     * L'operazione è transazionale per garantire l'integrità dei dati.
     *
     * @param scenarioId L'ID dello scenario.
     * @param labExamSet L'oggetto con i dati degli esami.
     * @return true se il salvataggio è andato a buon fine, false altrimenti.
     */
    private boolean saveLabExamsToDb(int scenarioId, LabExamSet labExamSet) {
        final String insertExamSetSQL = "INSERT INTO EsamiLaboratorio (id_scenario) VALUES (?)";
        final String insertCategorySQL = "INSERT INTO CategoriaLaboratorio (id_esami_laboratorio, nome_categoria) VALUES (?, ?)";
        final String insertTestSQL = "INSERT INTO TestLaboratorio (id_categoria_lab, nome, valore, unita_misura, range_riferimento, referto) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia la transazione

            int examSetId;
            try (PreparedStatement stmt = conn.prepareStatement(insertExamSetSQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, scenarioId);
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        examSetId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione EsamiLaboratorio fallita, nessun ID ottenuto.");
                    }
                }
            }

            for (LabCategory category : labExamSet.getCategorie()) {
                int categoryId;
                try (PreparedStatement stmt = conn.prepareStatement(insertCategorySQL, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, examSetId);
                    stmt.setString(2, category.getNomeCategoria());
                    stmt.executeUpdate();
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            categoryId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creazione CategoriaLaboratorio fallita, nessun ID ottenuto.");
                        }
                    }
                }

                for (LabTest test : category.getTest()) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertTestSQL)) {
                        stmt.setInt(1, categoryId);
                        stmt.setString(2, test.getNome());
                        stmt.setString(3, test.getValore());
                        stmt.setString(4, test.getUnitaMisura());
                        stmt.setString(5, test.getRangeRiferimento());
                        stmt.setString(6, test.getReferto());
                        stmt.executeUpdate();
                    }
                }
            }

            conn.commit(); // Finalizza la transazione se tutto è andato a buon fine
            logger.info("Dati degli esami di laboratorio salvati correttamente su DB per lo scenario ID: {}", scenarioId);
            return true;

        } catch (SQLException e) {
            logger.error("Errore SQL durante il salvataggio degli esami per lo scenario ID {}. Eseguo il rollback.", scenarioId, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Errore critico durante il rollback della transazione.", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina il comportamento di default
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione al DB.", e);
                }
            }
        }
    }

    /**
     * Recupera il set completo di esami di laboratorio associato a uno scenario.
     *
     * @param scenarioId L'ID dello scenario di cui recuperare gli esami.
     * @return Un oggetto LabExamSet completamente popolato, o null se non trovato o in caso di errore.
     */
    public LabExamSet getLabExamsByScenarioId(int scenarioId) {
        final String sql = "SELECT " +
                "el.id_esami_laboratorio, el.id_scenario, " +
                "cl.id_categoria_lab, cl.nome_categoria, " +
                "tl.id_test_lab, tl.nome, tl.valore, tl.unita_misura, tl.range_riferimento, tl.referto " +
                "FROM EsamiLaboratorio el " +
                "JOIN CategoriaLaboratorio cl ON el.id_esami_laboratorio = cl.id_esami_laboratorio " +
                "JOIN TestLaboratorio tl ON cl.id_categoria_lab = tl.id_categoria_lab " +
                "WHERE el.id_scenario = ? " +
                "ORDER BY cl.id_categoria_lab, tl.id_test_lab";

        LabExamSet labExamSet = null;
        Map<Integer, LabCategory> categoryMap = new HashMap<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (labExamSet == null) {
                    labExamSet = new LabExamSet(rs.getInt("id_esami_laboratorio"), rs.getInt("id_scenario"));
                }

                int categoryId = rs.getInt("id_categoria_lab");
                LabExamSet finalLabExamSet = labExamSet;
                LabCategory category = categoryMap.computeIfAbsent(categoryId, id -> {
                    LabCategory newCategory;
                    try {
                        newCategory = new LabCategory(id, rs.getString("nome_categoria"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    finalLabExamSet.addCategory(newCategory);
                    return newCategory;
                });

                LabTest test = new LabTest(
                        rs.getInt("id_test_lab"),
                        rs.getString("nome"),
                        rs.getString("valore"),
                        rs.getString("unita_misura"),
                        rs.getString("range_riferimento"),
                        rs.getString("referto")
                );
                category.addTest(test);
            }

            if (labExamSet != null) {
                logger.info("Recuperati esami di laboratorio per lo scenario ID: {}", scenarioId);
            } else {
                logger.warn("Nessun esame di laboratorio trovato per lo scenario ID: {}", scenarioId);
            }

        } catch (SQLException e) {
            logger.error("Errore SQL durante il recupero degli esami per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            return null; // Ritorna null in caso di errore
        }

        return labExamSet;
    }
}