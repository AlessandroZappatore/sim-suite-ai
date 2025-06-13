package it.uniupo.simnova.domain.lab_exam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportSet {
    private int id;
    private int scenarioId;

    @JsonProperty("tipologia_esame")
    private String tipologiaEsame;

    @JsonProperty("referto")
    private String descrizioneEsame;

    public ReportSet(int id, int scenarioId, String tipologiaEsame, String descrizioneEsame) {
        this.id = id;
        this.scenarioId = scenarioId;
        this.tipologiaEsame = tipologiaEsame;
        this.descrizioneEsame = descrizioneEsame;
    }

}
