package it.uniupo.simnova.service.ai_api;

import it.uniupo.simnova.domain.lab_exam.LabExamSet;
import it.uniupo.simnova.service.ai_api.model.ScenarioGenerationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    private final RestTemplate restTemplate;

    public ExternalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Chiama l'API esterna per generare un set di esami di laboratorio basato su uno scenario.
     * @param descrizioneScenario La descrizione dello scenario.
     * @param tipologiaPaziente La tipologia del paziente.
     * @return Un Optional contenente LabExamSet se la chiamata ha successo, altrimenti un Optional vuoto.
     */
    public Optional<LabExamSet> generateLabExamsFromScenario(String descrizioneScenario, String tipologiaPaziente) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Crea il corpo della richiesta
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("descrizione_scenario", descrizioneScenario);
        requestBody.put("tipologia_paziente", tipologiaPaziente);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // URL dell'API Python
            String labExamApiUrl = "http://localhost:8000/exam/generate-lab-exams";
            logger.info("Invio richiesta a {}: {}", labExamApiUrl, requestBody);
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
            String scenarioApiUrl = "http://localhost:8000/medical/generate-scenario";
            logger.info("Invio richiesta a {}: {}", scenarioApiUrl, request);
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
}