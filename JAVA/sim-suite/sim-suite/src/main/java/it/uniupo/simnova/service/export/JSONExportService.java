package it.uniupo.simnova.service.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Servizio per l'esportazione di scenari in formato JSON.
 * Utilizza la libreria <code>Gson</code> per la serializzazione di oggetti complessi
 * e aggrega dati provenienti da vari servizi di dominio.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class JSONExportService implements Serializable {

    /**
     * L'istanza di <code>Gson</code> utilizzata per la serializzazione JSON.
     * Configurato per una stampa leggibile e per includere i valori <code>null</code>.
     */
    private static Gson gson = null;

    /**
     * Servizio per la gestione delle operazioni sugli oggetti <code>Scenario</code>.
     */
    private final ScenarioService scenarioService;

    /**
     * Servizio per la gestione di esami e referti associati agli scenari.
     */
    private final EsameRefertoService esameRefertoService;

    /**
     * Servizio per la gestione dei dati del paziente al tempo zero (T0).
     */
    private final PazienteT0Service pazienteT0Service;

    /**
     * Servizio specifico per la gestione degli scenari di tipo "avanzato".
     */
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Servizio specifico per la gestione degli scenari simulati con paziente.
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;

    /**
     * Servizio per la gestione dei dati relativi all'esame fisico.
     */
    private final EsameFisicoService esameFisicoService;

    /**
     * Servizio per la gestione dei materiali necessari all'interno dello scenario.
     */
    private final MaterialeService materialeService;

    /**
     * Servizio per la gestione delle azioni chiave definite nello scenario.
     */
    private final AzioneChiaveService azioneChiaveService;

    /**
     * Costruisce una nuova istanza di <code>JSONExportService</code>.
     * Inietta le dipendenze dei servizi necessari e inizializza l'oggetto <code>Gson</code>.
     *
     * @param scenarioService                 Il servizio per le operazioni sugli scenari.
     * @param esameRefertoService             Il servizio per gli esami e referti.
     * @param pazienteT0Service               Il servizio per i dati del paziente T0.
     * @param advancedScenarioService         Il servizio per gli scenari avanzati.
     * @param patientSimulatedScenarioService Il servizio per gli scenari simulati con paziente.
     * @param esameFisicoService              Il servizio per l'esame fisico.
     * @param materialeService                Il servizio per i materiali necessari.
     * @param azioneChiaveService             Il servizio per le azioni chiave.
     */
    public JSONExportService(ScenarioService scenarioService, EsameRefertoService esameRefertoService,
                             PazienteT0Service pazienteT0Service, AdvancedScenarioService advancedScenarioService,
                             PatientSimulatedScenarioService patientSimulatedScenarioService, EsameFisicoService esameFisicoService,
                             MaterialeService materialeService, AzioneChiaveService azioneChiaveService) {
        this.scenarioService = scenarioService;
        this.esameRefertoService = esameRefertoService;
        this.pazienteT0Service = pazienteT0Service;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        // Inizializza l'istanza di Gson con formattazione leggibile e inclusione dei null.
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        this.esameFisicoService = esameFisicoService;
        this.materialeService = materialeService;
        this.azioneChiaveService = azioneChiaveService;
    }

    /**
     * Esporta tutti i dati correlati a un {@link Scenario} specifico in un formato JSON.
     * Il metodo raccoglie le informazioni principali dello scenario, il suo tipo, e tutti i dati
     * associati tramite i vari servizi di dominio (esami, paziente T0, materiali, esame fisico,
     * azioni chiave, presidi e dati specifici per il tipo di scenario).
     *
     * @param scenarioId L'identificativo unico dello scenario da esportare.
     * @return Un array di byte contenente la rappresentazione JSON dello scenario e dei suoi dati correlati.
     * Il JSON è codificato utilizzando <code>UTF-8</code>.
     * @see Scenario
     */
    public byte[] exportScenarioToJSON(Integer scenarioId) {
        // Recupera l'oggetto Scenario principale e il suo tipo.
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        String scenarioType = scenarioService.getScenarioType(scenarioId);

        // Mappa per aggregare tutti i dati da esportare.
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("scenario", scenario);
        exportData.put("tipo", scenarioType);

        // Recupera e aggiunge i dati degli esami e referti.
        var esamiReferti = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        exportData.put("esamiReferti", esamiReferti);

        // Recupera e aggiunge i dati del paziente al tempo T0.
        var pazienteT0 = pazienteT0Service.getPazienteT0ById(scenarioId);
        exportData.put("pazienteT0", pazienteT0);

        // Recupera e aggiunge i dati dei materiali necessari.
        var materialeNecessario = materialeService.getMaterialiByScenarioId(scenarioId);
        exportData.put("materialeNecessario", materialeNecessario);

        // Recupera e aggiunge i dati dell'esame fisico.
        var esameFisico = esameFisicoService.getEsameFisicoById(scenarioId);
        exportData.put("esameFisico", esameFisico);

        // Recupera e aggiunge i nomi delle azioni chiave.
        var azioniChiave = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);
        exportData.put("azioniChiave", azioniChiave);

        // Recupera e aggiunge i dati dei presidi.
        var presidi = PresidiService.getPresidiByScenarioId(scenarioId);
        exportData.put("presidi", presidi);

        // Aggiunge i dati specifici per "Advanced Scenario" o "Patient Simulated Scenario".
        if (scenarioType.equals("Advanced Scenario") || scenarioType.equals("Patient Simulated Scenario")) {
            var tempi = advancedScenarioService.getTempiByScenarioId(scenarioId);
            exportData.put("tempi", tempi);
        }

        // Aggiunge i dati specifici solo per "Patient Simulated Scenario".
        if (scenarioType.equals("Patient Simulated Scenario")) {
            var sceneggiatura = patientSimulatedScenarioService.getSceneggiatura(scenarioId);
            exportData.put("sceneggiatura", sceneggiatura);
        }

        // Converte la mappa di dati in una stringa JSON.
        String json = gson.toJson(exportData);

        // Restituisce il JSON come array di byte codificato in UTF-8.
        return json.getBytes(StandardCharsets.UTF_8);
    }
}