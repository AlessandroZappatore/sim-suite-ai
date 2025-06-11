package it.uniupo.simnova.views.ui.helper;

import it.uniupo.simnova.domain.common.ParametroAggiuntivo;

import java.util.List;

/**
 * Interfaccia che definisce un contratto per i fornitori di dati dei parametri vitali.
 * Permette a diversi oggetti (es. PazienteT0, Tempo) di esporre i loro parametri vitali
 * e aggiuntivi in un formato comune, utilizzabile dal componente monitor.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public interface VitalSignsDataProvider {

    /**
     * Restituisce il valore della Pressione Arteriosa (PA).
     *
     * @return La pressione arteriosa come stringa (es. "120/80").
     */
    String getPA();

    /**
     * Restituisce il valore della Frequenza Cardiaca (FC).
     *
     * @return La frequenza cardiaca in battiti al minuto (bpm).
     */
    Integer getFC();

    /**
     * Restituisce il valore della Temperatura Corporea (T).
     *
     * @return La temperatura in gradi Celsius (°C).
     */
    Double getT();

    /**
     * Restituisce il valore della Frequenza Respiratoria (RR).
     *
     * @return La frequenza respiratoria in atti al minuto (rpm).
     */
    Integer getRR();

    /**
     * Restituisce il valore della Saturazione dell'Ossigeno (SpO₂).
     *
     * @return La saturazione di ossigeno in percentuale (%).
     */
    Integer getSpO2();

    /**
     * Restituisce il valore della Frazione Inspiratoria di Ossigeno (FiO₂).
     *
     * @return La FiO₂ in percentuale (%).
     */
    Integer getFiO2();

    /**
     * Restituisce il valore dei Litri di Ossigeno erogati.
     *
     * @return I litri di ossigeno al minuto (L/min).
     */
    Double getLitriO2();

    /**
     * Restituisce il valore della Capnometria di fine espirazione (EtCO₂).
     *
     * @return L'EtCO₂ in mmHg.
     */
    Integer getEtCO2();

    /**
     * Restituisce un testo aggiuntivo per il monitoraggio.
     * Questo può essere usato per visualizzare note libere o dettagli extra.
     *
     * @return Un testo descrittivo aggiuntivo.
     */
    String getAdditionalMonitorText();

    /**
     * Restituisce una lista di parametri aggiuntivi/personalizzati.
     *
     * @return Una lista di oggetti {@link ParametroAggiuntivo}.
     */
    List<ParametroAggiuntivo> getAdditionalParameters();
}