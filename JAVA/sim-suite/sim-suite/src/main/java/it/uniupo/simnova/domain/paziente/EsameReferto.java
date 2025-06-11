package it.uniupo.simnova.domain.paziente;

import lombok.Getter;
import lombok.Setter;

/**
 * Classe che rappresenta un <strong>esame con referto</strong> nel sistema.
 * Contiene informazioni su un esame medico, inclusi:
 * <ul>
 * <li><strong>Identificativi</strong> dell'esame e dello scenario associato</li>
 * <li><strong>Tipo di esame</strong> (es. Radiografia, Ecografia)</li>
 * <li><strong>Percorso del file multimediale</strong> (se presente)</li>
 * <li><strong>Referto testuale</strong></li>
 * </ul>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class EsameReferto {

    /**
     * <strong>Identificativo univoco</strong> dell'esame e referto, assegnato dal database.
     * -- GETTER --
     *  Restituisce l'<strong>identificativo univoco</strong> dell'esame.
     */
    @Getter
    private final int idEsame;
    /**
     * Identificativo dello scenario associato all'esame.
     */
    @Getter
    private final int id_scenario;
    /**
     * Tipologia dell'esame, ad esempio "Radiografia", "Ecografia".
     * -- GETTER --
     *  Restituisce la <strong>tipologia</strong> dell'esame.
     * -- SETTER --
     *  Imposta una nuova <strong>tipologia</strong> per l'esame.
     *
     */
    @Setter
    @Getter
    private String tipo;
    /**
     * Percorso del file multimediale associato all'esame.
     */
    @Getter
    @Setter
    private String media;
    /**
     * Contenuto testuale del referto dell'esame.
     */
    @Getter
    @Setter
    private String refertoTestuale;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>EsameReferto</code></strong>.
     *
     * @param idEsame         <strong>Identificativo univoco</strong> dell'esame.
     * @param scenario        <strong>Identificativo</strong> dello scenario associato.
     * @param tipo            <strong>Tipologia</strong> dell'esame (es. "Radiografia", "Ecografia").
     * @param media           <strong>Percorso del file multimediale</strong> associato (opzionale).
     * @param refertoTestuale <strong>Contenuto testuale</strong> del referto.
     */
    public EsameReferto(int idEsame, int scenario, String tipo, String media, String refertoTestuale) {
        this.idEsame = idEsame;
        this.id_scenario = scenario;
        this.tipo = tipo;
        this.media = media;
        this.refertoTestuale = refertoTestuale;
    }

    public EsameReferto(int idEsame, int idScenario) {
        this.idEsame = idEsame;
        id_scenario = idScenario;
    }


    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto {@code EsameReferto},
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID dell'esame, lo scenario, il tipo, il media e il referto testuale.
     */
    @Override
    public String toString() {
        return "EsameReferto{" +
                "id_esame=" + idEsame +
                ", id_scenario=" + id_scenario +
                ", tipo='" + tipo + '\'' +
                ", media='" + media + '\'' +
                ", refertoTestuale='" + refertoTestuale + '\'' +
                '}';
    }
}