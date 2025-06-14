package it.uniupo.simnova.domain.respons_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReportSet {
    private int id;
    private int scenarioId;

    @JsonProperty("tipologia_esame")
    private String tipologiaEsame;

    @JsonProperty("referto")
    private String descrizioneEsame;
}
