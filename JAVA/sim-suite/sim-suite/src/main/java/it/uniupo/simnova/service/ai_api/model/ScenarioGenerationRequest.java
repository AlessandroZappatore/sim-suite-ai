package it.uniupo.simnova.service.ai_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO per la richiesta di generazione di uno scenario.
 *
 * @param description  descrizione dello scenario
 * @param scenarioType tipologia di scenario
 * @param target       target dello scenario
 * @param difficulty   difficoltà dello scenario
 * @author Alessandro Zappatore
 * @version 1.0
 */
public record ScenarioGenerationRequest(
        @JsonProperty("description") String description,
        @JsonProperty("scenario_type") String scenarioType,
        @JsonProperty("target") String target,
        @JsonProperty("difficulty") String difficulty
) {
    /**
     * Costruttore per la richiesta di generazione di uno scenario con difficoltà predefinita "Medio".
     *
     * @param description  descrizione dello scenario
     * @param scenarioType tipologia di scenario
     * @param target       target dello scenario
     */
    public ScenarioGenerationRequest(String description, String scenarioType, String target) {
        this(description, scenarioType, target, "Facile");
    }
}