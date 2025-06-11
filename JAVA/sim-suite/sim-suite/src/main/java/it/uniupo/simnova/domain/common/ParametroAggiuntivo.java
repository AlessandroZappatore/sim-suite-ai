package it.uniupo.simnova.domain.common;

/**
 * Classe che rappresenta un <strong>parametro aggiuntivo</strong> per uno scenario di simulazione.
 * Questa classe modella parametri personalizzati che possono essere aggiunti a specifici
 * tempi di uno scenario per arricchire la simulazione con dati extra (es. parametri vitali personalizzati, valori di laboratorio).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ParametroAggiuntivo {
    /**
     * Unità di misura del parametro (es. "mmHg", "%").
     */
    private final String unitaMisura;
    /**
     * Identificativo univoco del parametro, ID assegnato dal database.
     */
    private int id;
    /**
     * Identificativo del tempo a cui appartiene il parametro.
     */
    private int tempoId;
    /**
     * Identificativo dello scenario a cui appartiene il parametro.
     */
    private int scenarioId;
    /**
     * Nome del parametro (es. "Pressione venosa centrale", "Glicemia").
     */
    private String nome;
    /**
     * Valore del parametro, memorizzato come stringa (es. "12", "98.5").
     */
    private String valore;

    /**
     * Costruttore completo per creare un nuovo oggetto <strong><code>ParametroAggiuntivo</code></strong> con
     * tutti i dettagli, inclusi gli ID di tempo e scenario.
     *
     * @param parametriAggiuntiviId Identificativo univoco del parametro.
     * @param tempoId               Identificativo del tempo a cui il parametro è associato.
     * @param scenarioId            Identificativo dello scenario a cui il parametro è associato.
     * @param nome                  Il nome del parametro.
     * @param valore                Il valore del parametro, come stringa.
     * @param unitaMisura           L'unità di misura del parametro.
     */
    public ParametroAggiuntivo(int parametriAggiuntiviId, int tempoId, int scenarioId, String nome, String valore, String unitaMisura) {
        this.id = parametriAggiuntiviId;
        this.tempoId = tempoId;
        this.scenarioId = scenarioId;
        this.nome = nome;
        this.valore = valore;
        this.unitaMisura = unitaMisura;
    }

    /**
     * Costruttore semplificato per creare un nuovo oggetto <strong><code>ParametroAggiuntivo</code></strong>,
     * utile quando l'ID non è ancora noto (es. prima del salvataggio nel database).
     *
     * @param nome   Il nome del parametro.
     * @param valore Il valore del parametro, come numero. Verrà convertito in stringa.
     * @param unita  L'unità di misura del parametro.
     */
    public ParametroAggiuntivo(String nome, double valore, String unita) {
        this.nome = nome;
        this.valore = String.valueOf(valore); // Converte il valore numerico in stringa.
        this.unitaMisura = unita;
    }

    /**
     * Restituisce l'<strong>identificativo univoco</strong> del parametro.
     *
     * @return L'ID del parametro.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'<strong>identificativo univoco</strong> del parametro.
     *
     * @param id Il nuovo ID del parametro.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce l'<strong>identificativo dello scenario</strong> a cui appartiene il parametro.
     *
     * @return L'ID dello scenario.
     */
    public int getScenarioId() {
        return scenarioId;
    }

    /**
     * Imposta l'<strong>identificativo dello scenario</strong> a cui appartiene il parametro.
     *
     * @param scenarioId Il nuovo ID dello scenario.
     */
    public void setScenarioId(int scenarioId) {
        this.scenarioId = scenarioId;
    }

    /**
     * Restituisce il <strong>nome</strong> del parametro.
     *
     * @return Il nome del parametro.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Imposta il <strong>nome</strong> del parametro.
     *
     * @param nome Il nuovo nome del parametro.
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Restituisce il <strong>valore</strong> del parametro come stringa.
     *
     * @return Il valore del parametro.
     */
    public String getValore() {
        return valore;
    }

    /**
     * Imposta il <strong>valore</strong> del parametro come stringa.
     *
     * @param valore Il nuovo valore del parametro.
     */
    public void setValore(String valore) {
        this.valore = valore;
    }

    /**
     * Imposta il <strong>valore</strong> del parametro come numero.
     * Il valore numerico viene convertito in stringa per l'archiviazione.
     *
     * @param valore Il nuovo valore numerico del parametro.
     */
    public void setValue(double valore) {
        this.valore = String.valueOf(valore);
    }

    /**
     * Restituisce l'<strong>unità di misura</strong> del parametro.
     *
     * @return L'unità di misura del parametro.
     */
    public String getUnitaMisura() {
        return unitaMisura;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto {@code ParametroAggiuntivo},
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID, il tempo, lo scenario, il nome, il valore e l'unità di misura del parametro.
     */
    @Override
    public String toString() {
        return "ParametroAggiuntivo{" +
                "id=" + id +
                ", tempoId=" + tempoId +
                ", scenarioId=" + scenarioId +
                ", nome='" + nome + '\'' +
                ", valore='" + valore + '\'' +
                ", unitaMisura='" + unitaMisura + '\'' +
                '}';
    }
}