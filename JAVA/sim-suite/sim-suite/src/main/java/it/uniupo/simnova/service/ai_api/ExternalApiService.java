package it.uniupo.simnova.service.ai_api;

import it.uniupo.simnova.domain.respons_model.LabExamSet;
import it.uniupo.simnova.domain.respons_model.MatSet;
import it.uniupo.simnova.domain.respons_model.ReportSet;
import it.uniupo.simnova.service.ai_api.model.LabExamGenerationRequest;
import it.uniupo.simnova.service.ai_api.model.MatGenerationRequest;
import it.uniupo.simnova.service.ai_api.model.ReportGenerationRequest;
import it.uniupo.simnova.service.ai_api.model.ScenarioGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Servizio per interagire con le API esterne per la generazione di esami, scenari, referti e materiali.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class ExternalApiService {
    /**
     * Logger per il servizio ExternalApiService.
     */
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    /**
     * RestTemplate per effettuare le chiamate HTTP alle API esterne.
     */
    private final RestTemplate restTemplate;
    /**
     * Porta su cui l'API esterna è in ascolto.
     */
    private final String PORT = "8001";

    /**
     * Costruttore del servizio ExternalApiService.
     *
     * @param restTemplate RestTemplate per effettuare le chiamate HTTP.
     */
    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Genera esami di laboratorio a partire da uno scenario specificato nella richiesta.
     *
     * @param request la richiesta di generazione degli esami di laboratorio contenente la descrizione dello scenario,
     * @return un Optional contenente il set di esami di laboratorio generati, o vuoto se la generazione fallisce
     * @throws RestClientException se si verifica un errore durante la chiamata all'API esterna
     */
    public Optional<LabExamSet> generateLabExamsFromScenario(LabExamGenerationRequest request) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LabExamGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            String labExamApiUrl = "http://localhost:" + PORT + "/exams/generate-lab-exams";
            logger.info("Invio richiesta per esami di laboratorio a {}: {}", labExamApiUrl, request);
            ResponseEntity<LabExamSet> response = restTemplate.postForEntity(labExamApiUrl, requestEntity, LabExamSet.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Risposta ricevuta con successo dall'API degli esami.");
                return Optional.of(response.getBody());
            } else {
                logger.warn("Risposta non valida dall'API degli esami: Status {}", response.getStatusCode());
                return Optional.empty();
            }
        } catch (RestClientException e) {
            logger.error("Errore durante la chiamata all'API degli esami di laboratorio. Rilancio l'eccezione.", e);
            throw e;
        }
    }

    /**
     * Genera uno scenario a partire dalla richiesta specificata.
     *
     * @param request la richiesta di generazione dello scenario contenente la descrizione, il tipo di scenario, il target e la difficoltà
     * @return un Optional contenente lo scenario generato come stringa JSON, o vuoto se la generazione fallisce
     * @throws RestClientException se si verifica un errore durante la chiamata all'API esterna
     */
    public Optional<String> generateScenario(ScenarioGenerationRequest request) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ScenarioGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            String scenarioApiUrl = "http://localhost:" + PORT + "/scenarios/generate-scenario";
            logger.info("Invio richiesta per creazione di scenario a {}: {}", scenarioApiUrl, request);
            ResponseEntity<String> response = restTemplate.postForEntity(scenarioApiUrl, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Risposta ricevuta con successo dall'API di generazione scenario.");
                return Optional.of(response.getBody());
            } else {
                logger.warn("Risposta non valida dall'API di generazione scenario: Status {}", response.getStatusCode());
                return Optional.empty();
            }
        } catch (RestClientException e) {
            logger.error("Errore durante la chiamata all'API di generazione scenario. Rilancio l'eccezione.", e);
            throw e;
        }
    }

    /**
     * Genera un referto medico a partire dalla richiesta specificata.
     *
     * @param request la richiesta di generazione del referto contenente la descrizione dello scenario, il tipo di paziente, il tipo di esame e l'esame obiettivo
     * @return un Optional contenente il set di referti generati, o vuoto se la generazione fallisce
     * @throws RestClientException se si verifica un errore durante la chiamata all'API esterna
     */
    public Optional<ReportSet> generateReport(ReportGenerationRequest request) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReportGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            String reportApiUrl = "http://localhost:" + PORT + "/reports/generate-medical-report";
            logger.info("Invio richiesta per creazione di referto a {}: {}", reportApiUrl, request);
            ResponseEntity<ReportSet> response = restTemplate.postForEntity(reportApiUrl, requestEntity, ReportSet.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Risposta ricevuta con successo dall'API di generazione referto.");
                return Optional.of(response.getBody());
            } else {
                logger.warn("Risposta non valida dall'API di generazione referto: Status {}", response.getStatusCode());
                return Optional.empty();
            }
        } catch (RestClientException e) {
            logger.error("Errore durante la chiamata all'API di generazione referto. Rilancio l'eccezione.", e);
            throw e;
        }
    }

    /**
     * Genera i materiali necessari per uno scenario specificato nella richiesta.
     *
     * @param request la richiesta di generazione dei materiali contenente la descrizione dello scenario, il tipo di paziente, il target e l'esame obiettivo
     * @return un Optional contenente una lista di MatSet generati, o vuoto se la generazione fallisce
     * @throws RestClientException se si verifica un errore durante la chiamata all'API esterna
     */
    public Optional<List<MatSet>> generateMaterial(MatGenerationRequest request) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MatGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            String materialApiUrl = "http://localhost:" + PORT + "/materials/generate-materials";
            logger.info("Invio richiesta per creazione di materiale a: {}", materialApiUrl);
            ResponseEntity<MatSet[]> response = restTemplate.postForEntity(materialApiUrl, requestEntity, MatSet[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Risposta ricevuta con successo. Materiali ricevuti: {}", response.getBody().length);
                return Optional.of(Arrays.asList(response.getBody()));
            } else {
                logger.warn("Risposta non valida dall'API: Status {}", response.getStatusCode());
                return Optional.empty();
            }
        } catch (RestClientException e) {
            logger.error("Errore durante la chiamata all'API di generazione materiale. Rilancio l'eccezione.", e);
            throw e;
        }
    }
}