package it.uniupo.simnova.service.ai_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per la richiesta di generazione dei materiali necessari.
 *
 * @param description   descrizione dello scenario
 * @param scenarioType  tipologia di paziente
 * @param target        target dello scenario
 * @param objectiveExam esame obiettivo
 * @author Alessandro Zappatore
 * @version 1.0
 */
public record MatGenerationRequest(
        @JsonProperty("descrizione_scenario") String description,
        @JsonProperty("tipologia_paziente") String scenarioType,
        @JsonProperty("target") String target,
        @JsonProperty("esame_obiettivo") String objectiveExam
) {
}
