package it.uniupo.simnova.service.ai_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per la richiesta di generazione di un referto.
 *
 * @param description   descrizione dello scenario
 * @param scenarioType  tipologia di paziente
 * @param examType      tipologia di esame richiesto
 * @param objectiveExam esame obiettivo
 * @author Alessandro Zappatore
 * @version 1.0
 */
public record ReportGenerationRequest(
        @JsonProperty("descrizione_scenario") String description,
        @JsonProperty("tipologia_paziente") String scenarioType,
        @JsonProperty("tipologia_esame") String examType,
        @JsonProperty("esame_obiettivo") String objectiveExam
) {
}