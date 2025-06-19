package it.uniupo.simnova.domain.respons_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe che rappresenta un set di referti associati a uno scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Getter
@Setter
@Builder
public class ReportSet {
    /**
     * Identificativo del set di referti.
     */
    private int id;
    /**
     * Identificativo dello scenario associato a questo set di referti.
     */
    private int scenarioId;
    /**
     * Tipologia dell'esame associato al referto.
     */
    @JsonProperty("tipologia_esame")
    private String tipologiaEsame;
    /**
     * Referto testuale dell'esame.
     */
    @JsonProperty("referto")
    private String descrizioneEsame;
}
