package it.uniupo.simnova.domain.paziente;

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
    public EsameFisico(int idEsameFisico, String generale, String pupille, String collo, String torace, String cuore, String addome, String retto, String cute, String estremità, String neurologico, String fast) {
        this.idEsameFisico = idEsameFisico;
        this.sections = new HashMap<>();
        // Inizializza la mappa con tutte le sezioni e i loro valori corrispondenti.
        sections.put("Generale", generale);
        sections.put("Pupille", pupille);
        sections.put("Collo", collo);
        sections.put("Torace", torace);
        sections.put("Cuore", cuore);
        sections.put("Addome", addome);
        sections.put("Retto", retto);
        sections.put("Cute", cute);
        sections.put("Estremità", estremità);
        sections.put("Neurologico", neurologico);
        sections.put("FAST", fast);
    }

    /**
     * Recupera la <strong>mappa completa</strong> di tutte le sezioni dell'esame fisico
     * con i rispettivi risultati testuali.
     *
     * @return Una mappa dove le chiavi sono i nomi delle sezioni e i valori sono le descrizioni dei risultati.
     */
    public Map<String, String> getSections() {
        return sections;
    }

    /**
     * Restituisce l'<strong>identificativo univoco</strong> dell'esame fisico.
     *
     * @return L'ID dell'esame fisico.
     */
    @SuppressWarnings("unused") // Sopprime l'avviso se il metodo non viene usato direttamente nel codice Java.
    public int getIdEsameFisico() {
        return idEsameFisico;
    }
}