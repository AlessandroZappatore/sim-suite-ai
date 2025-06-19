package it.uniupo.simnova.domain.respons_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe che rappresenta un set di materiali associati a uno scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Getter
@Setter
@Builder
public class MatSet {
    /**
     * Nome del set di materiali.
     */
    @JsonProperty("nome")
    private String nome;
    /**
     * Descrizione del set di materiali.
     */
    @JsonProperty("descrizione")
    private String descrizione;
}
