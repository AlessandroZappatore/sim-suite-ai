package it.uniupo.simnova.domain.paziente;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe che rappresenta un <strong>esame fisico completo</strong> di un paziente.
 * Contiene i risultati di un esame fisico organizzato per sezioni anatomiche,
 * con ogni sezione che può avere una descrizione testuale dei risultati.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@SuppressWarnings("NonAsciiCharacters") // Sopprime l'avviso per caratteri non-ASCII
@Getter
@ToString
public class EsameFisico {

    /**
     * <strong>Identificativo univoco</strong> dell'esame fisico, assegnato dal database.
     */
    private final int idEsameFisico;

    /**
     * Mappa che associa il nome di ogni sezione dell'esame fisico (es. "Generale", "Torace")
     * alla sua descrizione testuale dei risultati.
     */
    private final Map<String, String> sections;

    /**
     * Costruttore completo per creare un oggetto <strong><code>EsameFisico</code></strong>
     * con i risultati di tutte le sezioni predefinite.
     *
     * @param idEsameFisico <strong>Identificativo univoco</strong> dell'esame fisico.
     * @param generale      Risultati della sezione "Generale".
     * @param pupille       Risultati dell'esame "Pupille".
     * @param collo         Risultati dell'esame "Collo".
     * @param torace        Risultati dell'esame "Torace".
     * @param cuore         Risultati dell'esame "Cuore".
     * @param addome        Risultati dell'esame "Addome".
     * @param retto         Risultati dell'esame "Retto" (se eseguito).
     * @param cute          Risultati dell'esame "Cute".
     * @param estremità     Risultati dell'esame "Estremità".
     * @param neurologico   Risultati dell'esame "Neurologico".
     * @param fast          Risultati del FAST exam (Focused Assessment with Sonography for Trauma).
     */
    @Builder
    public EsameFisico(int idEsameFisico, String generale, String pupille, String collo, String torace, String cuore, String addome, String retto, String cute, String estremità, String neurologico, String fast) {
        this.idEsameFisico = idEsameFisico;
        // Use a temporary mutable map for initialization, then wrap it with Collections.unmodifiableMap
        Map<String, String> tempSections = new HashMap<>();
        tempSections.put("Generale", generale);
        tempSections.put("Pupille", pupille);
        tempSections.put("Collo", collo);
        tempSections.put("Torace", torace);
        tempSections.put("Cuore", cuore);
        tempSections.put("Addome", addome);
        tempSections.put("Retto", retto);
        tempSections.put("Cute", cute);
        tempSections.put("Estremità", estremità);
        tempSections.put("Neurologico", neurologico);
        tempSections.put("FAST", fast);
        this.sections = Collections.unmodifiableMap(tempSections);
    }
}