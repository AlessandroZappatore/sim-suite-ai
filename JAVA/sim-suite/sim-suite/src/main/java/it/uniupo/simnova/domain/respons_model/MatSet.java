package it.uniupo.simnova.domain.respons_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MatSet {
    @JsonProperty("nome")
    private String nome;

    @JsonProperty("descrizione_scenario")
    private String descrizione_scenario;
}
