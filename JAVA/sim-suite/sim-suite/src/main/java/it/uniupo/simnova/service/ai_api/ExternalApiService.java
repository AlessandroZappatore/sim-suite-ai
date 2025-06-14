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

@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    private final RestTemplate restTemplate;
    private final String PORT = "8001";

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<LabExamSet> generateLabExamsFromScenario(LabExamGenerationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LabExamGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            // URL dell'API Python
            String labExamApiUrl = "http://localhost:"+PORT+"/exams/generate-lab-exams";
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
            logger.error("Errore durante la chiamata all'API degli esami di laboratorio: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> generateScenario(ScenarioGenerationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ScenarioGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try{
            String scenarioApiUrl = "http://localhost:"+PORT+"/scenarios/generate-scenario";
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
            logger.error("Errore durante la chiamata all'API di generazione scenario: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ReportSet> generateReport(ReportGenerationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReportGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try{
            String reportApiUrl = "http://localhost:"+PORT+"/reports/generate-medical-report";
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
            logger.error("Errore durante la chiamata all'API di generazione referto: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<List<MatSet>> generateMaterial(MatGenerationRequest request) { // <-- 1. Cambia il tipo di ritorno a List<MatSet>
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MatGenerationRequest> requestEntity = new HttpEntity<>(request, headers);

        try {
            String materialApiUrl = "http://localhost:" + PORT + "/materials/generate-materials";
            logger.info("Invio richiesta per creazione di materiale a: {}", materialApiUrl);

            // 2. LA MODIFICA CHIAVE: Chiedi un array MatSet[] invece di un singolo MatSet.class
            ResponseEntity<MatSet[]> response = restTemplate.postForEntity(materialApiUrl, requestEntity, MatSet[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Risposta ricevuta con successo. Materiali ricevuti: {}", response.getBody().length);
                // 3. Converti l'array in una List e restituiscila
                return Optional.of(Arrays.asList(response.getBody()));
            } else {
                logger.warn("Risposta non valida dall'API: Status {}", response.getStatusCode());
                return Optional.empty();
            }
        } catch (RestClientException e) {
            logger.error("Errore durante la chiamata all'API di generazione materiale: {}", e.getMessage());
            return Optional.empty();
        }
    }
}