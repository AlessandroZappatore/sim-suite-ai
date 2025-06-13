package it.uniupo.simnova.domain.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Classe che rappresenta un <strong>tempo</strong> specifico all'interno di uno scenario di simulazione avanzato.
 * Contiene i parametri vitali del paziente e altre informazioni rilevanti che definiscono lo stato
 * della simulazione in quel preciso istante o fase.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Getter
@ToString
public class Tempo {
    /**
     * Identificativo univoco del tempo, assegnato dal database.
     */
    private final int idTempo;
    /**
     * <strong>Identificativo</strong> dello scenario avanzato associato.
     */
    private final int advancedScenario;
    /**
     * Frequenza respiratoria del paziente (atti/min).
     */
    private final Integer RR;
    /**
     * Saturazione di ossigeno del paziente (%).
     */
    private final Integer SpO2;
    /**
     * Frazione di ossigeno inspirato dal paziente (%).
     */
    private final Integer FiO2;
    /**
     * Litri di ossigeno somministrati al paziente (L/min).
     */
    private final Double LitriO2;
    /**
     * Pressione parziale di CO2 espirata del paziente (mmHg).
     */
    private final Integer EtCO2;
    /**
     * ID del tempo a cui andare se l'azione viene eseguita (tempo "se SI").
     */
    private final int TSi;
    /**
     * ID del tempo a cui andare se l'azione non viene eseguita (tempo "se NO").
     */
    private final int TNo;
    /**
     * Altri dettagli o note rilevanti per questo tempo.
     */
    private final String altriDettagli;
    /**
     * Durata del timer associato a questo tempo, in secondi.
     */
    private final long timerTempo;
    /**
     * Ruolo del genitore associato a questo tempo, se lo scenario è pediatrico.
     * Può essere {@code null} se non applicabile o non definito.
     */
    private final String ruoloGenitore;

    private String PA;
    private Integer FC;
    @Setter
    private double T;
    @Setter
    private String Azione;
    @Setter
    private List<ParametroAggiuntivo> parametriAggiuntivi;


    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>Tempo</code></strong> in uno scenario avanzato.
     * Include la validazione di alcuni parametri per garantire la coerenza dei dati.
     * <p>
     * {@code @Builder} is used here to allow for more flexible and readable object construction,
     * especially given the large number of parameters.
     *
     * @param idTempo          <strong>Identificativo univoco</strong> del tempo.
     * @param advancedScenario <strong>Identificativo</strong> dello scenario avanzato associato.
     * @param PA               Pressione arteriosa del paziente nel formato "sistolica/diastolica" (es. "120/80").
     * @param FC               Frequenza cardiaca del paziente. Deve essere un valore non negativo.
     * @param RR               Frequenza respiratoria del paziente. Deve essere un valore non negativo.
     * @param t                Temperatura del paziente.
     * @param spO2             Saturazione di ossigeno del paziente. Deve essere compresa tra 0 e 100.
     * @param fiO2             Frazione di ossigeno inspirato dal paziente. Deve essere compresa tra 0 e 100.
     * @param litriO2          Litri di ossigeno somministrati al paziente. Deve essere un valore non negativo.
     * @param etCO2            Pressione parziale di CO2 espirata del paziente. Deve essere un valore non negativo.
     * @param azione           Azione o evento associato a questo tempo.
     * @param TSi              ID del tempo successivo se l'azione viene eseguita (tempo "se SI").
     * @param TNo              ID del tempo successivo se l'azione NON viene eseguita (tempo "se NO").
     * @param altriDettagli    Altri dettagli rilevanti per questo tempo.
     * @param timerTempo       Durata del timer associato a questo tempo, in secondi. Deve essere un valore non negativo.
     * @param ruoloGenitore    Ruolo del genitore associato a questo tempo, se lo scenario è pediatrico.
     * @throws IllegalArgumentException se i valori di PA, FC, RR, SpO2, FiO2, LitriO2, EtCO2 o timerTempo non rispettano i criteri di validazione.
     */
    @Builder // Generates a builder for this constructor
    public Tempo(int idTempo, int advancedScenario, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer fiO2, Double litriO2, Integer etCO2, String azione, int TSi, int TNo, String altriDettagli, long timerTempo, String ruoloGenitore) {
        this.idTempo = idTempo;
        this.advancedScenario = advancedScenario;

        // RR (final)
        if (RR != null && RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa.");
        }
        this.RR = RR;

        // SpO2 (final)
        if (spO2 != null && (spO2 < 0 || spO2 > 100)) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100.");
        }
        this.SpO2 = spO2;

        // FiO2 (final)
        if (fiO2 != null && (fiO2 < 0 || fiO2 > 100)) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100.");
        }
        this.FiO2 = fiO2;

        // LitriO2 (final)
        if (litriO2 != null && litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo.");
        }
        this.LitriO2 = litriO2;

        // EtCO2 (final)
        if (etCO2 != null && etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa.");
        }
        this.EtCO2 = etCO2;

        // timerTempo (final)
        if (timerTempo < 0) {
            throw new IllegalArgumentException("Il timer non può essere negativo.");
        }
        this.timerTempo = timerTempo;

        // Other final fields
        this.TSi = TSi;
        this.TNo = TNo;
        this.altriDettagli = altriDettagli;
        this.ruoloGenitore = ruoloGenitore;

        setPA(PA);
        setFC(FC);
        setT(t);
        setAzione(azione);
    }


    /**
     * Imposta la <strong>pressione arteriosa</strong> del paziente.
     *
     * @param PA La nuova pressione arteriosa nel formato "sistolica/diastolica".
     * @throws IllegalArgumentException se il formato PA non è valido.
     */
    public void setPA(String PA) {
        if (PA != null) {
            String trimmedPA = PA.trim();
            if (!trimmedPA.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80').");
            }
            this.PA = trimmedPA;
        } else {
            this.PA = null;
        }
    }

    /**
     * Imposta la <strong>frequenza cardiaca</strong> del paziente.
     *
     * @param FC La nuova frequenza cardiaca.
     * @throws IllegalArgumentException se FC è negativo.
     */
    public void setFC(Integer FC) {
        if (FC != null && FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa.");
        }
        this.FC = FC;
    }
}