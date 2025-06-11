package it.uniupo.simnova.domain.scenario;

import it.uniupo.simnova.domain.common.Tempo;

import java.util.ArrayList;

/**
 * Classe che rappresenta uno <strong>scenario simulato con paziente</strong>.
 * Estende la classe {@link AdvancedScenario} aggiungendo informazioni specifiche
 * per gli scenari che prevedono l'interazione con un paziente simulato,
 * come la sceneggiatura dettagliata.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class PatientSimulatedScenario extends AdvancedScenario {
    /**
     * <strong>Identificativo univoco</strong> dello scenario simulato con paziente.
     */
    private final int idPatientSimulatedScenario;
    /**
     * <strong>Identificativo</strong> dello scenario avanzato associato.
     */
    private final int advancedScenario;
    /**
     * <strong>Sceneggiatura</strong> dettagliata per il paziente simulato.
     */
    private String sceneggiatura;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>PatientSimulatedScenario</code></strong>.
     * Richiama il costruttore della superclass {@link AdvancedScenario} per inizializzare i campi comuni
     * e poi imposta i campi specifici di questa classe.
     *
     * @param id                         <strong>Identificativo univoco</strong> dello scenario.
     * @param titolo                     <strong>Titolo</strong> dello scenario.
     * @param nome_paziente              <strong>Nome</strong> del paziente associato allo scenario.
     * @param patologia                  <strong>Patologia</strong> del paziente.
     * @param descrizione                <strong>Descrizione</strong> dello scenario.
     * @param briefing                   <strong>Briefing</strong> dello scenario.
     * @param patto_aula                 <strong>Patto dell'aula</strong> per lo scenario.
     * @param obiettivo                  <strong>Obiettivo</strong> dello scenario.
     * @param moulage                    <strong>Moulage</strong> dello scenario.
     * @param liquidi                    <strong>Liquidi</strong> e dosi farmaci associati allo scenario.
     * @param timer_generale             <strong>Timer generale</strong> dello scenario.
     * @param autori                     <strong>Autori</strong> dello scenario.
     * @param tipologia                  <strong>Tipologia</strong> dello scenario.
     * @param target                     <strong>Target</strong> dello scenario.
     * @param infoGenitore               <strong>Informazioni per il genitore</strong> dello scenario.
     * @param id_advanced_scenario       <strong>Identificativo</strong> dello scenario avanzato associato.
     * @param tempi                      <strong>Lista dei tempi</strong> associati allo scenario.
     * @param idPatientSimulatedScenario <strong>Identificativo univoco</strong> dello scenario simulato con paziente.
     * @param advancedScenario           <strong>Identificativo</strong> dello scenario avanzato associato.
     * @param sceneggiatura              <strong>Sceneggiatura</strong> dello scenario simulato con paziente.
     */
    public PatientSimulatedScenario(int id, String titolo, String nome_paziente, String patologia, String descrizione, String briefing, String patto_aula, String obiettivo, String moulage, String liquidi, float timer_generale, String autori, String tipologia, String target, String infoGenitore, int id_advanced_scenario, ArrayList<Tempo> tempi, int idPatientSimulatedScenario, int advancedScenario, String sceneggiatura) {
        // Chiama il costruttore della superclass AdvancedScenario
        super(id, titolo, nome_paziente, patologia, descrizione, briefing, patto_aula, obiettivo, moulage, liquidi, timer_generale, id_advanced_scenario, tempi, autori, tipologia, infoGenitore, target);
        this.idPatientSimulatedScenario = idPatientSimulatedScenario;
        this.advancedScenario = advancedScenario;
        this.sceneggiatura = sceneggiatura;
    }

    /**
     * Restituisce la <strong>sceneggiatura</strong> dello scenario.
     *
     * @return La sceneggiatura dello scenario.
     */
    public String getSceneggiatura() {
        return sceneggiatura;
    }

    /**
     * Imposta la <strong>sceneggiatura</strong> dello scenario.
     *
     * @param sceneggiatura La nuova sceneggiatura.
     */
    public void setSceneggiatura(String sceneggiatura) {
        this.sceneggiatura = sceneggiatura;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto <strong><code>PatientSimulatedScenario</code></strong>.
     * Estende la rappresentazione della superclass con i campi specifici di questa classe.
     *
     * @return Una stringa che descrive i campi di questo scenario simulato con paziente.
     */
    @Override
    public String toString() {
        return super.toString() + "PatientSimulatedScenario{" +
                "idPatientSimulatedScenario=" + idPatientSimulatedScenario +
                ", advancedScenario=" + advancedScenario +
                ", sceneggiatura='" + sceneggiatura + '\'' +
                '}';
    }
}