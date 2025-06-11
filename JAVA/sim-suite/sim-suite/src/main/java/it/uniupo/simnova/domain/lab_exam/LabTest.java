package it.uniupo.simnova.domain.lab_exam;

// Utilizza Lombok per ridurre il codice boilerplate (costruttori, getter, setter)
// Se non usi Lombok, dovrai generarli manualmente.

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LabTest {
    private int id;
    @JsonProperty("nome")
    private String nome;

    @JsonProperty("valore")
    private String valore;

    @JsonProperty("unita_misura")
    private String unitaMisura;

    @JsonProperty("range_riferimento")
    private String rangeRiferimento;

    @JsonProperty("referto")
    private String referto;
}