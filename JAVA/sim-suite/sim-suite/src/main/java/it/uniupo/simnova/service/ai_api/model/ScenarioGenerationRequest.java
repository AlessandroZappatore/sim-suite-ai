package it.uniupo.simnova.service.ai_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per la richiesta di generazione di uno scenario all'API Python.
 */
public record ScenarioGenerationRequest(
        @JsonProperty("description") String description,
        @JsonProperty("scenario_type") String scenarioType,
        @JsonProperty("target") String target,
        @JsonProperty("difficulty") String difficulty
) {
    /**
     * Costruttore di convenienza con difficoltà predefinita "Medio"
     */
    public ScenarioGenerationRequest(String description, String scenarioType, String target) {
        this(description, scenarioType, target, "Medio");
    }
}