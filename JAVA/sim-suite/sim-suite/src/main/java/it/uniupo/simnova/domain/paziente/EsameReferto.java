package it.uniupo.simnova.domain.paziente;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EsameReferto {
    private int idEsame;
    private int idScenario;
    private String tipo;
    private String media;
    private String refertoTestuale;

    public EsameReferto(int idEsame, int scenarioId) {
        this.idEsame = idEsame;
        this.idScenario = scenarioId;
    }
}