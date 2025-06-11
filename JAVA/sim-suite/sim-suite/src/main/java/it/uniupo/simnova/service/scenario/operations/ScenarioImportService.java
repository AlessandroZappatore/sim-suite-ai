package it.uniupo.simnova.service.scenario.operations;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servizio per l'importazione di scenari da file JSON o ZIP.
 * Questo servizio gestisce la creazione di nuovi scenari a partire da dati strutturati,
 * sia da file JSON contenenti la sola definizione dello scenario, sia da archivi ZIP
 * che includono il JSON e i relativi file multimediali (esami, referti).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@SuppressWarnings("ALL") // Sopprime tutti gli avvisi del compilatore.
@Service
public class ScenarioImportService {

    /**
     * Il logger per questa classe, utilizzato per registrare informazioni ed errori durante il processo di importazione.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioImportService.class);

    /**
     * Servizio principale per la gestione delle operazioni di base sugli scenari (creazione, aggiornamento).
     */
    private final ScenarioService scenarioService;

    /**
     * Servizio per la gestione dei dati relativi all'esame fisico del paziente.
     */
    private final EsameFisicoService esameFisicoService;

    /**
     * Servizio per la gestione dei dati del paziente al tempo zero (T0).
     */
    private final PazienteT0Service pazienteT0Service;

    /**
     * Servizio per la gestione degli esami e referti associati agli scenari.
     */
    private final EsameRefertoService esameRefertoService;

    /**
     * Servizio per la gestione delle logiche specifiche degli scenari di tipo "Advanced Scenario".
     */
    private final AdvancedScenarioService advancedScenarioService;

    /**
     * Servizio per la gestione delle logiche specifiche degli scenari di tipo "Patient Simulated Scenario".
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;

    /**
     * Servizio per la gestione dei materiali necessari all'interno degli scenari.
     */
    private final MaterialeService materialeService;

    /**
     * Servizio per la gestione dei presidi associati agli scenari.
     */
    private final PresidiService presidiService;

    /**
     * Servizio per la gestione delle azioni chiave definite negli scenari.
     */
    private final AzioneChiaveService azioneChiaveService;

    /**
     * Servizio per la decompressione di archivi ZIP contenenti dati di scenari.
     */
    private final UnZipScenarioService unZipScenarioService;

    /**
     * Servizio per la gestione dello storage dei file, utilizzato per salvare i file multimediali.
     */
    private final FileStorageService fileStorageService;

    /**
     * Costruisce una nuova istanza di <code>ScenarioImportService</code>.
     * Inietta tutte le dipendenze dei servizi necessari per le operazioni di importazione.
     *
     * @param scenarioService                 Il servizio principale per gli scenari.
     * @param esameFisicoService              Il servizio per gli esami fisici.
     * @param pazienteT0Service               Il servizio per i dati del paziente T0.
     * @param esameRefertoService             Il servizio per gli esami e referti.
     * @param advancedScenarioService         Il servizio per gli scenari avanzati.
     * @param patientSimulatedScenarioService Il servizio per gli scenari simulati con paziente.
     * @param materialeService                Il servizio per i materiali necessari.
     * @param presidiService                  Il servizio per i presidi.
     * @param azioneChiaveService             Il servizio per le azioni chiave.
     * @param unZipScenarioService            Il servizio per la decompressione ZIP.
     * @param fileStorageService              Il servizio per lo storage dei file.
     */
    public ScenarioImportService(ScenarioService scenarioService, EsameFisicoService esameFisicoService,
                                 PazienteT0Service pazienteT0Service, EsameRefertoService esameRefertoService,
                                 AdvancedScenarioService advancedScenarioService, PatientSimulatedScenarioService patientSimulatedScenarioService,
                                 MaterialeService materialeService, PresidiService presidiService, AzioneChiaveService azioneChiaveService,
                                 UnZipScenarioService unZipScenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        this.esameFisicoService = esameFisicoService;
        this.pazienteT0Service = pazienteT0Service;
        this.esameRefertoService = esameRefertoService;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.materialeService = materialeService;
        this.presidiService = presidiService;
        this.azioneChiaveService = azioneChiaveService;
        this.unZipScenarioService = unZipScenarioService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Crea un nuovo scenario nel database a partire da un file JSON.
     * Il JSON deve contenere la struttura completa dello scenario, inclusi i dati principali
     * e tutti i componenti correlati (paziente T0, esami, materiali, ecc.).
     * Il tipo di scenario (Quick, Advanced, Patient Simulated) viene determinato dal campo "tipo" nel JSON.
     *
     * @param jsonFile L'array di byte che rappresenta il contenuto del file JSON.
     * @return <code>true</code> se lo scenario è stato creato con successo e tutti i suoi componenti sono stati salvati;
     * <code>false</code> in caso di errore di parsing JSON, dati mancanti, tipo di scenario non riconosciuto o fallimento del salvataggio.
     */
    @Transactional
    public boolean createScenarioByJSON(byte[] jsonFile) {
        try {
            // Converte l'array di byte JSON in una stringa UTF-8.
            String jsonString = new String(jsonFile, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().create(); // Crea un'istanza di Gson.

            // Parsa la stringa JSON in una mappa generica per l'estrazione dei dati.
            Map<String, Object> jsonData = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
            }.getType());

            // Estrae il tipo di scenario dal JSON.
            String scenarioType = (String) jsonData.get("tipo");
            int creationResult = -1; // Variabile per memorizzare l'ID dello scenario appena creato.

            // Estrae i dati principali dello scenario.
            Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");
            if (scenarioData == null) {
                logger.error("Dati 'scenario' mancanti nel JSON. Impossibile creare lo scenario.");
                return false;
            }

            String titolo = (String) scenarioData.get("titolo");
            String nomePaziente = (String) scenarioData.get("nome_paziente");
            String patologia = (String) scenarioData.get("patologia");
            String autori = (String) scenarioData.get("autori");
            // Conversione Double a float per il timer generale.
            double timerGeneraleDouble = (Double) scenarioData.getOrDefault("timer_generale", 0.0);
            float timerGenerale = (float) timerGeneraleDouble;
            String tipologia = (String) scenarioData.get("tipologia");

            // Basato sul tipo di scenario, invoca il metodo di creazione appropriato.
            switch (scenarioType) {
                case "Quick Scenario":
                    creationResult = createQuickScenarioFromJson(jsonData, titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);
                    break;
                case "Advanced Scenario":
                    // Crea lo scenario avanzato e, se riuscito, salva i componenti comuni e avanzati.
                    creationResult = advancedScenarioService.startAdvancedScenario(titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);
                    if (creationResult > 0) {
                        saveCommonScenarioComponents(creationResult, jsonData);
                        saveAdvancedScenarioComponents(creationResult, jsonData);
                    }
                    break;
                case "Patient Simulated Scenario":
                    // Crea lo scenario simulato dal paziente e, se riuscito, salva i componenti comuni, avanzati e specifici.
                    creationResult = patientSimulatedScenarioService.startPatientSimulatedScenario(titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);
                    if (creationResult > 0) {
                        saveCommonScenarioComponents(creationResult, jsonData);
                        saveAdvancedScenarioComponents(creationResult, jsonData);
                        savePatientSimulatedScenarioComponents(creationResult, jsonData);
                    }
                    break;
                default:
                    logger.error("Tipo di scenario non riconosciuto nel JSON: '{}'.", scenarioType);
                    return false;
            }

            // Verifica se la creazione dello scenario principale è riuscita.
            if (creationResult <= 0) {
                logger.error("Errore durante la creazione dello scenario di tipo '{}'. L'ID restituito non è valido.", scenarioType);
                return false;
            }

            logger.info("Scenario di tipo '{}' con ID {} creato e popolato con successo dal JSON.", scenarioType, creationResult);
            return true;

        } catch (JsonSyntaxException e) {
            logger.error("Errore di sintassi nel file JSON fornito: {}", e.getMessage(), e);
            return false;
        } catch (ClassCastException e) {
            logger.error("Errore di cast dei dati nel JSON. Assicurarsi che i tipi dei campi corrispondano a quelli attesi: {}", e.getMessage(), e);
            return false;
        } catch (RuntimeException e) {
            logger.error("Errore logico o di dipendenza durante la creazione dello scenario dal JSON: {}", e.getMessage(), e);
            // La RuntimeException viene lanciata dai metodi saveCommonScenarioComponents ecc.
            return false;
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la creazione dello scenario dal JSON: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Importa uno scenario da un file ZIP.
     * Il file ZIP è atteso contenere un file JSON denominato 'scenario.json' e
     * opzionalmente una cartella 'esami/' (o 'media/') con file multimediali.
     * I file multimediali estratti verranno salvati nello storage configurato.
     *
     * @param zipBytes L'array di byte che rappresenta il contenuto del file ZIP.
     * @param fileName Il nome del file ZIP originale (utilizzato principalmente per il logging).
     * @return <code>true</code> se l'importazione ha successo, inclusa la creazione dello scenario
     * e il salvataggio dei file multimediali; <code>false</code> altrimenti.
     */
    public boolean importScenarioFromZip(byte[] zipBytes, String fileName) {
        logger.info("Inizio importazione scenario da file ZIP: '{}'.", fileName);
        try (InputStream zipInputStream = new ByteArrayInputStream(zipBytes)) {
            // Decomprime il file ZIP e ottiene i dati dello scenario JSON e i file media.
            UnZipScenarioService.UnzippedScenarioData unzippedData = unZipScenarioService.unzipScenario(zipInputStream);

            byte[] scenarioJsonBytes = unzippedData.scenarioJson();
            if (scenarioJsonBytes == null || scenarioJsonBytes.length == 0) {
                logger.error("Il file 'scenario.json' è vuoto o mancante all'interno dell'archivio ZIP '{}'. Impossibile procedere con l'importazione.", fileName);
                return false;
            }

            // Tenta di creare lo scenario utilizzando i dati JSON estratti.
            boolean scenarioCreated = createScenarioByJSON(scenarioJsonBytes);

            if (scenarioCreated) {
                logger.info("Scenario creato con successo da 'scenario.json' contenuto in '{}'.", fileName);

                // Se lo scenario è stato creato, procede con il salvataggio dei file multimediali.
                if (!unzippedData.mediaFiles().isEmpty()) {
                    logger.info("Trovati {} file multimediali da importare per lo scenario.", unzippedData.mediaFiles().size());
                    for (Map.Entry<String, byte[]> mediaFile : unzippedData.mediaFiles().entrySet()) {
                        String mediaFileName = mediaFile.getKey();
                        byte[] mediaFileBytes = mediaFile.getValue();
                        try (InputStream mediaInputStream = new ByteArrayInputStream(mediaFileBytes)) {
                            fileStorageService.storeFile(mediaInputStream, mediaFileName);
                            logger.debug("File multimediale '{}' salvato con successo nello storage.", mediaFileName);
                        } catch (IOException ioE) {
                            logger.error("Errore di I/O durante il salvataggio del file multimediale '{}' dallo ZIP: {}", mediaFileName, ioE.getMessage(), ioE);
                            // Continua l'importazione degli altri file anche se uno fallisce.
                        }
                    }
                } else {
                    logger.info("Nessun file multimediale trovato nell'archivio ZIP '{}' da importare.", fileName);
                }
                return true;
            } else {
                logger.error("Errore durante la creazione dello scenario dal JSON estratto da '{}'. La creazione è fallita.", fileName);
                return false;
            }

        } catch (IOException e) {
            logger.error("Errore di I/O durante la decompressione del file ZIP '{}': {}", fileName, e.getMessage(), e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("Errore nel contenuto o nella struttura del file ZIP '{}': {}", fileName, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'importazione dello scenario da ZIP '{}': {}", fileName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Crea uno scenario di tipo "Quick Scenario" a partire dai dati JSON forniti.
     * Una volta creato lo scenario principale, invoca il salvataggio dei componenti comuni.
     *
     * @param scenarioData  La {@link Map} di oggetti che rappresenta i dati completi dello scenario dal JSON.
     * @param titolo        Il titolo dello scenario.
     * @param nomePaziente  Il nome del paziente associato.
     * @param patologia     La patologia del paziente.
     * @param autori        I nomi degli autori dello scenario.
     * @param timerGenerale Il valore del timer generale dello scenario.
     * @param tipologia     La tipologia specifica dello scenario.
     * @return L'ID (<code>int</code>) dello scenario appena creato, o <code>-1</code> in caso di errore
     * durante la creazione dello scenario principale o il salvataggio dei componenti.
     */
    private int createQuickScenarioFromJson(Map<String, Object> scenarioData, String titolo, String nomePaziente,
                                            String patologia, String autori, float timerGenerale, String tipologia) {
        // Avvia la creazione di uno scenario Quick.
        int newId = scenarioService.startQuickScenario(-1, titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);
        if (newId <= 0) {
            logger.error("Fallimento nella creazione del Quick Scenario principale. ID restituito: {}.", newId);
            return -1;
        }

        // Salva i componenti comuni dello scenario appena creato.
        try {
            saveCommonScenarioComponents(newId, scenarioData);
            logger.info("Quick Scenario con ID {} e suoi componenti comuni salvati con successo.", newId);
        } catch (RuntimeException e) {
            logger.error("Errore durante il salvataggio dei componenti comuni per il Quick Scenario con ID {}: {}", newId, e.getMessage(), e);
            // Potrebbe essere opportuno eliminare lo scenario parzialmente creato qui in caso di errore.
            return -1;
        }
        return newId;
    }

    /**
     * Salva i componenti comuni dello scenario che sono presenti in tutti i tipi di scenario
     * (Quick, Advanced, Patient Simulated).
     * Questo metodo si occupa di aggiornare le varie sezioni dello scenario nel database.
     * In caso di errore in qualsiasi salvataggio, una {@link RuntimeException} viene lanciata.
     *
     * @param scenarioId   L'ID dello scenario (già creato) a cui associare questi componenti.
     * @param scenarioData La {@link Map} di oggetti che rappresenta i dati completi dello scenario dal JSON.
     * @throws RuntimeException se si verifica un errore durante il salvataggio di qualsiasi componente,
     *                          con un messaggio specifico sull'errore.
     */
    private void saveCommonScenarioComponents(int scenarioId, Map<String, Object> scenarioData) {
        // Estrae la sezione "scenario" che contiene i dati principali.
        Map<String, Object> scenario = (Map<String, Object>) scenarioData.get("scenario");
        if (scenario == null) {
            logger.warn("Sezione 'scenario' mancante nel JSON per l'ID {}. Impossibile salvare i componenti comuni.", scenarioId);
            return;
        }

        // Salva i vari campi dello scenario principale.
        if (!scenarioService.updateScenarioTarget(scenarioId, (String) scenario.get("target"))) {
            throw new RuntimeException("Errore durante il salvataggio del target per lo scenario " + scenarioId);
        }
        if (!scenarioService.updateScenarioDescription(scenarioId, (String) scenario.get("descrizione"))) {
            throw new RuntimeException("Errore durante il salvataggio della descrizione per lo scenario " + scenarioId);
        }
        if (!scenarioService.updateScenarioBriefing(scenarioId, (String) scenario.get("briefing"))) {
            throw new RuntimeException("Errore durante il salvataggio del briefing per lo scenario " + scenarioId);
        }
        if (!scenarioService.updateScenarioPattoAula(scenarioId, (String) scenario.get("patto_aula"))) {
            throw new RuntimeException("Errore durante il salvataggio del patto aula per lo scenario " + scenarioId);
        }
        if (!scenarioService.updateScenarioObiettiviDidattici(scenarioId, (String) scenario.get("obiettivo"))) {
            throw new RuntimeException("Errore durante il salvataggio dell'obiettivo didattico per lo scenario " + scenarioId);
        }
        if (!scenarioService.updateScenarioMoulage(scenarioId, (String) scenario.get("moulage"))) {
            throw new RuntimeException("Errore durante il salvataggio del moulage per lo scenario " + scenarioId);
        }
        if (!scenarioService.updateScenarioLiquidi(scenarioId, (String) scenario.get("liquidi"))) {
            throw new RuntimeException("Errore durante il salvataggio dei liquidi per lo scenario " + scenarioId);
        }
        if (!scenarioService.updateScenarioGenitoriInfo(scenarioId, (String) scenario.get("infoGenitore"))) {
            throw new RuntimeException("Errore durante il salvataggio delle informazioni per il genitore per lo scenario " + scenarioId);
        }

        // Salva le azioni chiave.
        List<String> azioniChiaveList = (List<String>) scenarioData.get("azioniChiave");
        // Verifica se la lista è null per evitare NPE; se è null, passa una lista vuota.
        if (!azioneChiaveService.updateAzioniChiaveForScenario(scenarioId, azioniChiaveList != null ? azioniChiaveList : Collections.emptyList())) {
            throw new RuntimeException("Errore durante il salvataggio delle azioni chiave per lo scenario " + scenarioId);
        }
        logger.debug("Azioni chiave salvate per lo scenario {}.", scenarioId);

        // Salva i materiali necessari.
        List<Map<String, Object>> materialiList = (List<Map<String, Object>>) scenarioData.get("materialeNecessario");
        List<Integer> idMateriali = new ArrayList<>();
        if (materialiList != null) {
            // Estrae solo gli ID dei materiali dal formato JSON.
            idMateriali = materialiList.stream()
                    .map(m -> ((Double) m.get("idMateriale")).intValue()) // Conversione da Double (Gson) a Integer.
                    .collect(Collectors.toList());
        }
        if (!materialeService.associaMaterialiToScenario(scenarioId, idMateriali)) {
            throw new RuntimeException("Errore durante il salvataggio del materiale necessario per lo scenario " + scenarioId);
        }
        logger.debug("Materiali necessari salvati per lo scenario {}.", scenarioId);

        // Salva i presidi.
        List<String> presidiList = (List<String>) scenarioData.get("presidi");
        Set<String> presidi = presidiList != null ? new HashSet<>(presidiList) : new HashSet<>();

        // Verifica che tutti i presidi da importare esistano già nel database.
        List<String> presidiEsistenti = PresidiService.getAllPresidi(); // Metodo statico
        Set<String> presidiNonEsistenti = presidi.stream()
                .filter(p -> !presidiEsistenti.contains(p))
                .collect(Collectors.toSet());

        if (!presidiNonEsistenti.isEmpty()) {
            logger.warn("Attenzione: i seguenti presidi non sono presenti nel database e non possono essere associati allo scenario {}: {}", scenarioId, presidiNonEsistenti);
            // Non lancio un'eccezione critica qui per permettere l'importazione anche se alcuni presidi non corrispondono.
            // La gestione degli errori di dipendenza sui presidi può variare a seconda delle esigenze.
        }

        if (!presidiService.savePresidi(scenarioId, presidi)) {
            throw new RuntimeException("Errore durante il salvataggio dei presidi per lo scenario " + scenarioId);
        }
        logger.debug("Presidi salvati per lo scenario {}.", scenarioId);

        // Salva l'esame fisico.
        Map<String, Object> esameFisicoData = (Map<String, Object>) scenarioData.get("esameFisico");
        if (esameFisicoData != null) {
            // Il JSON potrebbe avere una sezione "sections" per l'esame fisico.
            Map<String, String> sections = new HashMap<>();
            Map<String, String> sectionsData = (Map<String, String>) esameFisicoData.get("sections");
            if (sectionsData != null) {
                sections.putAll(sectionsData);
            }
            if (!esameFisicoService.addEsameFisico(scenarioId, sections)) {
                logger.warn("Errore durante il salvataggio dell'esame fisico per lo scenario ID {}.", scenarioId);
                // Non lancio un'eccezione critica qui; il warning è sufficiente.
            }
            logger.debug("Esame fisico salvato per lo scenario {}.", scenarioId);
        } else {
            logger.debug("Nessun dato per l'esame fisico presente nel JSON per lo scenario {}.", scenarioId);
        }

        // Salva i dati del paziente T0.
        Map<String, Object> pazienteT0Data = (Map<String, Object>) scenarioData.get("pazienteT0");
        if (pazienteT0Data != null) {
            List<Map<String, Object>> venosiData = (List<Map<String, Object>>) pazienteT0Data.get("accessiVenosi");
            List<Map<String, Object>> arteriosiData = (List<Map<String, Object>>) pazienteT0Data.get("accessiArteriosi");

            // Converte le liste di mappe in liste di oggetti Accesso.
            List<Accesso> venosi = convertAccessoData(venosiData);
            List<Accesso> arteriosi = convertAccessoData(arteriosiData);

            // Recupera e converte tutti i parametri vitali, gestendo i valori nulli.
            String pa = (String) pazienteT0Data.get("PA");
            Integer fc = pazienteT0Data.get("FC") != null ? ((Double) pazienteT0Data.get("FC")).intValue() : 0;
            Integer rr = pazienteT0Data.get("RR") != null ? ((Double) pazienteT0Data.get("RR")).intValue() : 0;
            Double temp = (Double) pazienteT0Data.getOrDefault("T", 0.0);
            Integer spo2 = pazienteT0Data.get("SpO2") != null ? ((Double) pazienteT0Data.get("SpO2")).intValue() : 0;
            Integer fio2 = pazienteT0Data.get("FiO2") != null ? ((Double) pazienteT0Data.get("FiO2")).intValue() : 0;
            Float litrio2 = pazienteT0Data.get("LitriOssigeno") != null ? ((Double) pazienteT0Data.get("LitriOssigeno")).floatValue() : 0f;
            Integer etco2 = pazienteT0Data.get("EtCO2") != null ? ((Double) pazienteT0Data.get("EtCO2")).intValue() : 0;
            String monitor = (String) pazienteT0Data.get("Monitor");

            // Salva i dati del paziente T0 e i suoi accessi.
            if (!pazienteT0Service.savePazienteT0(scenarioId, pa, fc, rr, temp, spo2, fio2, litrio2, etco2, monitor, venosi, arteriosi)) {
                logger.warn("Errore durante il salvataggio del paziente T0 per lo scenario ID {}.", scenarioId);
                // Non lancio un'eccezione critica qui; il warning è sufficiente.
            }
            logger.debug("Paziente T0 salvato per lo scenario {}.", scenarioId);
        } else {
            logger.debug("Nessun dato per il paziente T0 presente nel JSON per lo scenario {}.", scenarioId);
        }

        // Salva gli esami e referti.
        List<Map<String, Object>> esamiRefertiData = (List<Map<String, Object>>) scenarioData.get("esamiReferti");
        if (esamiRefertiData != null) {
            List<EsameReferto> esami = esamiRefertiData.stream()
                    .map(e -> new EsameReferto(
                            // L'idEsame potrebbe essere -1 se non gestito al momento dell'export.
                            e.get("idEsame") != null ? ((Double) e.get("idEsame")).intValue() : -1,
                            scenarioId,
                            (String) e.get("tipo"),
                            (String) e.get("media"),
                            (String) e.get("refertoTestuale")
                    ))
                    .collect(Collectors.toList());

            if (!esameRefertoService.saveEsamiReferti(scenarioId, esami)) {
                logger.warn("Errore durante il salvataggio degli esami e referti per lo scenario ID {}.", scenarioId);
                // Non lancio un'eccezione critica qui; il warning è sufficiente.
            }
            logger.debug("Esami e referti salvati per lo scenario {}.", scenarioId);
        } else {
            logger.debug("Nessun dato per esami e referti presente nel JSON per lo scenario {}.", scenarioId);
        }
    }

    /**
     * Salva i componenti specifici dello scenario di tipo "Patient Simulated Scenario".
     * Attualmente, questo include la sceneggiatura.
     *
     * @param scenarioId L'ID dello scenario a cui associare i componenti.
     * @param jsonData   La {@link Map} di oggetti che rappresenta i dati completi dello scenario dal JSON.
     * @throws RuntimeException se si verifica un errore durante il salvataggio della sceneggiatura.
     */
    private void savePatientSimulatedScenarioComponents(int scenarioId, Map<String, Object> jsonData) {
        String sceneggiatura = (String) jsonData.get("sceneggiatura");
        boolean result = patientSimulatedScenarioService.updateScenarioSceneggiatura(scenarioId, sceneggiatura);
        if (!result) {
            throw new RuntimeException("Errore durante il salvataggio della sceneggiatura per lo scenario " + scenarioId);
        }
        logger.debug("Sceneggiatura salvata per lo scenario {}.", scenarioId);
    }

    /**
     * Salva i componenti specifici dello scenario di tipo "Advanced Scenario".
     * Attualmente, questo include i tempi e i parametri aggiuntivi.
     *
     * @param scenarioId L'ID dello scenario a cui associare i componenti.
     * @param jsonData   La {@link Map} di oggetti che rappresenta i dati completi dello scenario dal JSON.
     * @throws RuntimeException se si verifica un errore durante il salvataggio dei tempi.
     */
    private void saveAdvancedScenarioComponents(int scenarioId, Map<String, Object> jsonData) {
        List<Map<String, Object>> tempiData = (List<Map<String, Object>>) jsonData.get("tempi");
        if (tempiData != null) {
            List<Tempo> tempi = tempiData.stream()
                    .map(t -> {
                        // Estrae e converte i parametri aggiuntivi per ogni tempo.
                        List<Map<String, Object>> paramsData = (List<Map<String, Object>>) t.get("parametriAggiuntivi");
                        List<ParametroAggiuntivo> params = new ArrayList<>();

                        if (paramsData != null) {
                            paramsData.forEach(p -> {
                                String nome = (String) p.get("nome");
                                // Gestisce sia Double che String per il valore.
                                double valore = p.get("valore") instanceof String ?
                                        Double.parseDouble((String) p.get("valore")) :
                                        (Double) p.get("valore");
                                String unita = (String) p.get("unitaMisura");
                                params.add(new ParametroAggiuntivo(nome, valore, unita));
                            });
                        }

                        // Costruisce l'oggetto Tempo, gestendo valori nulli o tipi diversi da Double.
                        Tempo tempo = new Tempo(
                                t.get("idTempo") != null ? ((Double) t.get("idTempo")).intValue() : -1,
                                scenarioId,
                                (String) t.get("PA"),
                                t.get("FC") != null ? ((Double) t.get("FC")).intValue() : null,
                                t.get("RR") != null ? ((Double) t.get("RR")).intValue() : null,
                                (Double) t.getOrDefault("T", 0.0), // Default a 0.0 se nullo.
                                t.get("SpO2") != null ? ((Double) t.get("SpO2")).intValue() : null,
                                t.get("FiO2") != null ? ((Double) t.get("FiO2")).intValue() : null,
                                t.get("LitriO2") != null ? ((Double) t.get("LitriO2")).doubleValue() : null,
                                t.get("EtCO2") != null ? ((Double) t.get("EtCO2")).intValue() : null,
                                (String) t.get("Azione"),
                                t.get("TSi") != null ? ((Double) t.get("TSi")).intValue() : 0, // Default a 0 se nullo.
                                t.get("TNo") != null ? ((Double) t.get("TNo")).intValue() : 0, // Default a 0 se nullo.
                                (String) t.get("altriDettagli"),
                                t.get("timerTempo") != null ? ((Double) t.get("timerTempo")).longValue() : 0L, // Default a 0L se nullo.
                                (String) t.get("ruoloGenitore")
                        );
                        tempo.setParametriAggiuntivi(params);
                        return tempo;
                    })
                    .collect(Collectors.toList());

            if (!advancedScenarioService.saveTempi(scenarioId, tempi)) {
                logger.warn("Errore durante il salvataggio dei tempi per lo scenario ID {}.", scenarioId);
                // Non lancio un'eccezione critica qui; il warning è sufficiente.
            }
            logger.debug("Tempi e parametri aggiuntivi salvati per lo scenario {}.", scenarioId);
        } else {
            logger.debug("Nessun dato per i tempi presente nel JSON per lo scenario {}.", scenarioId);
        }
    }

    /**
     * Converte una lista di mappe (rappresentanti dati di accesso da JSON) in una lista di oggetti {@link Accesso}.
     * Gestisce la conversione dei tipi (es. da Double a Integer) e i valori nulli.
     *
     * @param accessiData La {@link List} di {@link Map} contenente i dati grezzi degli accessi.
     * @return Una {@link List} di oggetti {@link Accesso}. Restituisce una lista vuota se <code>accessiData</code> è <code>null</code> o vuota.
     */
    private List<Accesso> convertAccessoData(List<Map<String, Object>> accessiData) {
        if (accessiData == null) {
            return new ArrayList<>();
        }

        return accessiData.stream()
                .map(a -> new Accesso(
                        a.get("idAccesso") != null ? ((Double) a.get("idAccesso")).intValue() : -1, // ID potrebbe essere -1 se non gestito in export
                        (String) a.get("tipologia"),
                        (String) a.get("posizione"),
                        (String) a.get("lato"),
                        a.get("misura") != null ? ((Double) a.get("misura")).intValue() : null // Misura può essere null
                ))
                .collect(Collectors.toList());
    }
}