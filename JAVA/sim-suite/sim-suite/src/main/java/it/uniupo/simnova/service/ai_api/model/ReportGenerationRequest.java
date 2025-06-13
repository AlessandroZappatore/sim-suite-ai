package it.uniupo.simnova.service.ai_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per la richiesta di generazione di un referto all'API Python.
 */
public record ReportGenerationRequest(
        @JsonProperty("descrizione_scenario") String description,
        @JsonProperty("tipologia_paziente") String scenarioType,
        @JsonProperty("tipologia_esame") String examType
) {}