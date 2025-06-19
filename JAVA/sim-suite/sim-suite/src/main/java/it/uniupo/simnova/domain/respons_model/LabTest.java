package it.uniupo.simnova.domain.respons_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe che rappresenta un test di laboratorio con i relativi dettagli.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class LabTest {
    /**
     * Identificativo del test di laboratorio.
     */
    private int id;
    /**
     * Nome del test di laboratorio.
     */
    @JsonProperty("nome")
    private String nome;
    /**
     * Valore del test.
     */
    @JsonProperty("valore")
    private String valore;
    /**
     * Unità di misura del test di laboratorio.
     */
    @JsonProperty("unita_misura")
    private String unitaMisura;
    /**
     * Intervallo di riferimento del test di laboratorio.
     */
    @JsonProperty("range_riferimento")
    private String rangeRiferimento;
    /**
     * Referto del test di laboratorio.
     */
    @JsonProperty("referto")
    private String referto;
}