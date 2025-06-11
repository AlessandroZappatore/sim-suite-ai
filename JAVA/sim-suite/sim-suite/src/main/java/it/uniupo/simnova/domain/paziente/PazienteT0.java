package it.uniupo.simnova.domain.paziente;

import it.uniupo.simnova.domain.common.Accesso;

import java.util.List;

/**
 * Classe che rappresenta i <strong>parametri del paziente al tempo T0</strong> (iniziale) di uno scenario di simulazione.
 * Contiene i parametri vitali principali e le liste degli accessi venosi e arteriosi.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
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
    /**
     * Pressione arteriosa del paziente (formato "sistolica/diastolica").
     */
    private String PA;
    /**
     * Frequenza cardiaca del paziente (bpm).
     */
    private Integer FC;
    /**
     * Temperatura del paziente (°C).
     */
    private double T;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>PazienteT0</code></strong>.
     * Include la validazione di alcuni parametri per garantire la coerenza dei dati.
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
    public PazienteT0(int idPaziente, String PA, Integer FC, Integer RR, double t, Integer spO2, Integer fiO2, Double litriO2, Integer etCO2,
                      String monitor, List<Accesso> accessiVenosi, List<Accesso> accessiArteriosi) {
        this.idPaziente = idPaziente;

        // Validazione e impostazione PA
        if (PA != null) {
            String trimmedPA = PA.trim();
            if (!trimmedPA.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80').");
            }
            this.PA = trimmedPA;
        } else {
            this.PA = null;
        }

        // Validazione e impostazione FC
        if (FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa.");
        } else {
            this.FC = FC;
        }

        // Validazione e impostazione RR
        if (RR < 0) {
            throw new IllegalArgumentException("RR non può essere negativa.");
        } else {
            this.RR = RR;
        }

        this.T = t;

        // Validazione e impostazione SpO2
        if (spO2 < 0 || spO2 > 100) {
            throw new IllegalArgumentException("SpO2 deve essere compresa tra 0 e 100.");
        } else {
            SpO2 = spO2;
        }

        // Validazione e impostazione FiO2
        if (fiO2 < 0 || fiO2 > 100) {
            throw new IllegalArgumentException("FiO2 deve essere compresa tra 0 e 100.");
        } else {
            FiO2 = fiO2;
        }

        // Validazione e impostazione LitriO2
        if (litriO2 < 0) {
            throw new IllegalArgumentException("LitriO2 non può essere negativo.");
        } else {
            LitriO2 = litriO2;
        }

        // Validazione e impostazione EtCO2
        if (etCO2 < 0) {
            throw new IllegalArgumentException("EtCO2 non può essere negativa.");
        } else {
            EtCO2 = etCO2;
        }

        Monitor = monitor;
        this.accessiVenosi = accessiVenosi;
        this.accessiArteriosi = accessiArteriosi;
    }

    /**
     * Restituisce la <strong>pressione arteriosa</strong> del paziente.
     *
     * @return La pressione arteriosa del paziente.
     */
    public String getPA() {
        return PA;
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
     * Restituisce la <strong>frequenza cardiaca</strong> del paziente.
     *
     * @return La frequenza cardiaca del paziente.
     */
    public Integer getFC() {
        return FC;
    }

    /**
     * Imposta la <strong>frequenza cardiaca</strong> del paziente.
     *
     * @param FC La nuova frequenza cardiaca.
     * @throws IllegalArgumentException se FC è negativo.
     */
    public void setFC(Integer FC) {
        if (FC < 0) {
            throw new IllegalArgumentException("FC non può essere negativa.");
        } else {
            this.FC = FC;
        }
    }

    /**
     * Restituisce la <strong>frequenza respiratoria</strong> del paziente.
     *
     * @return La frequenza respiratoria del paziente.
     */
    public Integer getRR() {
        return RR;
    }

    /**
     * Restituisce la <strong>temperatura</strong> del paziente.
     *
     * @return La temperatura del paziente.
     */
    public double getT() {
        return T;
    }

    /**
     * Imposta la <strong>temperatura</strong> del paziente.
     *
     * @param t La nuova temperatura.
     */
    public void setT(double t) {
        T = t;
    }

    /**
     * Restituisce la <strong>saturazione di ossigeno</strong> del paziente.
     *
     * @return La saturazione di ossigeno del paziente.
     */
    public Integer getSpO2() {
        return SpO2;
    }

    /**
     * Restituisce la <strong>pressione parziale di CO2 espirata</strong> del paziente.
     *
     * @return La pressione parziale di CO2 espirata del paziente.
     */
    public Integer getEtCO2() {
        return EtCO2;
    }

    /**
     * Restituisce il <strong>monitoraggio</strong> del paziente (testo aggiuntivo).
     *
     * @return Il testo di monitoraggio del paziente.
     */
    public String getMonitor() {
        return Monitor;
    }

    /**
     * Restituisce la <strong>lista degli accessi venosi</strong> del paziente.
     *
     * @return La lista degli accessi venosi del paziente.
     */
    public List<Accesso> getAccessiVenosi() {
        return accessiVenosi;
    }

    /**
     * Restituisce la <strong>lista degli accessi arteriosi</strong> del paziente.
     *
     * @return La lista degli accessi arteriosi del paziente.
     */
    public List<Accesso> getAccessiArteriosi() {
        return accessiArteriosi;
    }

    /**
     * Restituisce la <strong>percentuale di ossigeno somministrato</strong> al paziente.
     *
     * @return La percentuale di ossigeno somministrato al paziente.
     */
    public Integer getFiO2() {
        return FiO2;
    }

    /**
     * Restituisce i <strong>litri di ossigeno somministrati</strong> al paziente.
     *
     * @return I litri di ossigeno somministrati al paziente.
     */
    public Double getLitriO2() {
        return LitriO2;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto <strong><code>PazienteT0</code></strong>,
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID del paziente, i parametri vitali principali,
     * il testo di monitoraggio e gli accessi venosi/arteriosi.
     */
    @Override
    public String toString() {
        return "PazienteT0{" +
                "idPaziente=" + idPaziente +
                ", PA='" + PA + '\'' +
                ", FC=" + FC +
                ", RR=" + RR +
                ", T=" + T +
                ", SpO2=" + SpO2 +
                ", FiO2=" + FiO2 +
                ", LitriO2=" + LitriO2 +
                ", EtCO2=" + EtCO2 +
                ", Monitor='" + Monitor + '\'' +
                ", accessiVenosi=" + accessiVenosi +
                ", accessiArteriosi=" + accessiArteriosi +
                '}';
    }
}