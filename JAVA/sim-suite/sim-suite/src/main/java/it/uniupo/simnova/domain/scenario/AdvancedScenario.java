package it.uniupo.simnova.domain.scenario;

import it.uniupo.simnova.domain.common.Tempo;

import java.util.ArrayList;

/**
 * Classe che rappresenta uno <strong>scenario avanzato</strong> nel sistema.
 * Estende la classe base {@link Scenario} aggiungendo la gestione di <strong>più tempi di simulazione</strong>.
 * Ogni tempo rappresenta una fase distinta dello scenario con le proprie caratteristiche e parametri.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class AdvancedScenario extends Scenario {
    /**
     * <strong>Identificativo specifico</strong> per lo scenario avanzato.
     */
    private final int id_advanced_scenario;
    /**
     * <strong>Lista dei tempi/fasi</strong> dello scenario.
     */
    private ArrayList<Tempo> tempi;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>AdvancedScenario</code></strong>.
     * Richiama il costruttore della superclass {@link Scenario} per inizializzare i campi comuni
     * e poi imposta i campi specifici di questa classe.
     *
     * @param id                   <strong>Identificativo univoco</strong> dello scenario.
     * @param titolo               <strong>Titolo</strong> dello scenario.
     * @param nome_paziente        <strong>Nome</strong> del paziente associato allo scenario.
     * @param patologia            <strong>Patologia</strong> del paziente.
     * @param descrizione          <strong>Descrizione</strong> dello scenario.
     * @param briefing             <strong>Briefing</strong> dello scenario.
     * @param patto_aula           <strong>Patto dell'aula</strong> per lo scenario.
     * @param obiettivo            <strong>Obiettivo</strong> dello scenario.
     * @param moulage              <strong>Moulage</strong> dello scenario.
     * @param liquidi              <strong>Liquidi</strong> e dosi farmaci dello scenario.
     * @param timer_generale       <strong>Timer generale</strong> dello scenario.
     * @param id_advanced_scenario <strong>Identificativo specifico</strong> dello scenario avanzato.
     * @param tempi                <strong>Lista dei tempi/fasi</strong> dello scenario.
     * @param autori               <strong>Autori</strong> dello scenario.
     * @param tipologia            <strong>Tipologia</strong> dello scenario.
     * @param infoGenitore         <strong>Informazioni per il genitore</strong> dello scenario.
     * @param target               <strong>Target</strong> dello scenario.
     */
    public AdvancedScenario(int id, String titolo, String nome_paziente, String patologia,
                            String descrizione, String briefing, String patto_aula,
                            String obiettivo,
                            String moulage, String liquidi, float timer_generale,
                            int id_advanced_scenario, ArrayList<Tempo> tempi, String autori, String tipologia, String infoGenitore, String target) {
        // Chiama il costruttore della superclass Scenario per inizializzare i campi comuni.
        super(id, titolo, nome_paziente, patologia, descrizione, briefing,
                patto_aula, obiettivo, moulage,
                liquidi, timer_generale, autori, tipologia, infoGenitore, target);
        this.id_advanced_scenario = id_advanced_scenario;
        this.tempi = tempi;
    }

    /**
     * Restituisce la <strong>lista dei tempi/fasi</strong> dello scenario.
     *
     * @return L'{@link ArrayList} di oggetti {@link Tempo} che definiscono le fasi dello scenario.
     */
    public ArrayList<Tempo> getTempi() {
        return tempi;
    }

    /**
     * Imposta la <strong>lista dei tempi/fasi</strong> dello scenario.
     *
     * @param tempi La nuova {@link ArrayList} di oggetti {@link Tempo} da associare allo scenario.
     */
    public void setTempi(ArrayList<Tempo> tempi) {
        this.tempi = tempi;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto <strong><code>AdvancedScenario</code></strong>.
     * Estende la rappresentazione della superclass con i campi specifici di questa classe
     * (l'ID dello scenario avanzato e la lista dei tempi).
     *
     * @return Una stringa che descrive l'ID dello scenario avanzato e la lista dei suoi tempi.
     */
    @Override
    public String toString() {
        return super.toString() + "AdvancedScenario{" +
                "id_advanced_scenario=" + id_advanced_scenario +
                ", tempi=" + tempi +
                '}';
    }
}