package it.uniupo.simnova.domain.paziente;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe che rappresenta un esame con il relativo referto.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
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

    /**
     * Costruttore per creare un oggetto EsameReferto con idEsame e scenarioId.
     *
     * @param idEsame l'identificativo dell'esame
     * @param scenarioId l'identificativo dello scenario associato all'esame
     */
    public EsameReferto(int idEsame, int scenarioId) {
        this.idEsame = idEsame;
        this.idScenario = scenarioId;
    }
}