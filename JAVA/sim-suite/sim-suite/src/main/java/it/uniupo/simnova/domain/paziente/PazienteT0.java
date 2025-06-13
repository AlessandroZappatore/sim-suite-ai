package it.uniupo.simnova.domain.paziente;

import it.uniupo.simnova.domain.common.Accesso;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Classe che rappresenta i <strong>parametri del paziente al tempo T0</strong> (iniziale) di uno scenario di simulazione.
 * Contiene i parametri vitali principali e le liste degli accessi venosi e arteriosi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Getter // Generates getters for all fields
@ToString // Generates a toString() method
public class PazienteT0 {
    /**
     * Identificativo univoco del paziente.
     */
    private final int idPaziente;
    /**
     * Frequenza respiratoria del paziente (atti/min).
     */
    private final Integer RR;
    /**
     * Saturazione di ossigeno del paziente (%).
     */
    private final Integer SpO2;
    /**
     * Percentuale di ossigeno somministrato al paziente (%).
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
     * Testo aggiuntivo per il monitoraggio del paziente.
     */
    private final String Monitor;
    /**
     * Lista degli accessi venosi del paziente.
     */
    private final List<Accesso> accessiVenosi;
    /**
     * Lista degli accessi arteriosi del paziente.
     */
    private final List<Accesso> accessiArteriosi;

    private String PA;
    private Integer FC;
    @Setter
    private double T;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>PazienteT0</code></strong>.
     * Include la validazione di alcuni parametri per garantire la coerenza dei dati.
     * <p>
     * {@code @Builder} is used here to allow for more flexible and readable object construction,
     * especially given the large number of parameters.
     *
     * @param idPaziente       <strong>Identificativo univoco</strong> del paziente.
     * @param PA               <strong>Pressione arteriosa</strong> del paziente nel formato "sistolica/diastolica" (es. "120/80").
     * @param FC               <strong>Frequenza cardiaca</strong> del paziente. Deve essere un valore non negativo.
     * @param RR               <strong>Frequenza respiratoria</strong> del paziente. Deve essere un valore non negativo.
     * @param t                <strong>Temperatura</strong> del paziente.
     * @param spO2             <strong>Saturazione di ossigeno</strong> del paziente. Deve essere compresa tra 0 e 100.
     * @param fiO2             <strong>Percentuale di ossigeno</strong> somministrato al paziente. Deve essere compresa tra 0 e 100.
     * @param litriO2          <strong>Litri di ossigeno</strong> somministrati al paziente. Deve essere un valore non negativo.
     * @param etCO2            <strong>Pressione parziale di CO2 espirata</strong> del paziente. Deve essere un valore non negativo.
     * @param monitor          <strong>Monitoraggio</strong> del paziente (testo aggiuntivo).
     * @param accessiVenosi    <strong>Lista degli accessi venosi</strong> del paziente.
     * @param accessiArteriosi <strong>Lista degli accessi arteriosi</strong> del paziente.
     * @throws IllegalArgumentException se i valori di PA, FC, RR, SpO2, FiO2, LitriO2 o EtCO2 non rispettano i criteri di validazione.
     */
    @Builder // Generates a builder for this constructor
    public PazienteT0(int idPaziente, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer fiO2, Double litriO2, Integer etCO2,
                      String monitor, List<Accesso> accessiVenosi, List<Accesso> accessiArteriosi) {
        this.idPaziente = idPaziente;

        if (RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa.");
        }
        this.RR = RR;

        if (spO2 < 0 || spO2 > 100) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100.");
        }
        this.SpO2 = spO2;

        if (fiO2 < 0 || fiO2 > 100) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100.");
        }
        this.FiO2 = fiO2;

        if (litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo.");
        }
        this.LitriO2 = litriO2;

        if (etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa.");
        }
        this.EtCO2 = etCO2;

        this.Monitor = monitor;
        this.accessiVenosi = accessiVenosi;
        this.accessiArteriosi = accessiArteriosi;

        setPA(PA);
        setFC(FC);
        setT(t);
    }


    /**
     * Imposta la <strong>pressione arteriosa</strong> del paziente.
     *
     * @param PA La nuova pressione arteriosa.
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
        } else {
            this.FC = FC;
        }
    }
}