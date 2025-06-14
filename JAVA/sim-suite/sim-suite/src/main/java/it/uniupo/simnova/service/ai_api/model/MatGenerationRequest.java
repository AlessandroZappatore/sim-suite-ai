package it.uniupo.simnova.service.ai_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatGenerationRequest(
        @JsonProperty("descrizione_scenario") String description,
        @JsonProperty("tipologia_paziente") String scenarioType,
        @JsonProperty("target") String target,
        @JsonProperty("esame_obiettivo") String objectiveExam
) {}
