package it.uniupo.simnova.service.ai_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per la richiesta di generazione di un esame di laboratorio.
 *
 * @param description   descrizione dello scenario
 * @param scenarioType  tipologia di paziente
 * @param objectiveExam esame obiettivo
 * @param pathology     patologia associata
 * @author Alessandro Zappatore
 * @version 1.0
 */
public record LabExamGenerationRequest(
        @JsonProperty("descrizione_scenario") String description,
        @JsonProperty("tipologia_paziente") String scenarioType,
        @JsonProperty("esame_obiettivo") String objectiveExam,
        @JsonProperty("patologia") String pathology
) {
}