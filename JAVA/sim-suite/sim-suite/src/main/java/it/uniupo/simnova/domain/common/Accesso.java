package it.uniupo.simnova.domain.common;

/**
 * Classe che rappresenta un <strong>accesso venoso o arterioso</strong> nel sistema.
 * Definisce le proprietà chiave di un accesso, come tipologia, posizione, lato e misura.
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
public class Accesso {
    /**
     * Identificativo univoco dell'accesso, assegnato dal database.
     */
    private final int idAccesso;
    /**
     * Tipologia dell'accesso (es. "CVC", "Agocannula" per venoso; "Radiale" per arterioso).
     */
    private String tipologia;
    /**
     * Posizione anatomica dell'accesso (es. "Giugulare destra", "Cubitale sinistro").
     */
    private String posizione;
    /**
     * Lato dell'accesso, che può essere "DX" (destro) o "SX" (sinistro).
     */
    private String lato;
    /**
     * Misura dell'accesso, espressa in Gauge (es. 14G, 16G).
     */
    private Integer misura;

    /**
     * Costruttore per creare un nuovo oggetto {@code Accesso}.
     *
     * @param idAccesso Identificativo univoco dell'accesso.
     * @param tipologia Tipologia dell'accesso (es. "CVC", "Agocannula", "Radiale").
     * @param posizione Posizione anatomica dell'accesso (es. "Giugulare destra", "Cubitale sinistro").
     * @param lato      Lato dell'accesso ("DX" o "SX"). Viene validato per accettare solo questi due valori.
     * @param misura    Misura dell'accesso (Gauge), come valore intero.
     * @throws IllegalArgumentException se il lato fornito non è "DX" o "SX".
     */
    public Accesso(int idAccesso, String tipologia, String posizione, String lato, Integer misura) {
        this.idAccesso = idAccesso;
        this.tipologia = tipologia;
        this.posizione = posizione;
        this.lato = lato;
        this.misura = misura;
    }

    /**
     * Restituisce l'<strong>identificativo univoco</strong> di questo accesso.
     *
     * @return L'ID dell'accesso.
     */
    public int getId() {
        return idAccesso;
    }

    /**
     * Restituisce la <strong>tipologia</strong> dell'accesso.
     *
     * @return La tipologia dell'accesso.
     */
    public String getTipologia() {
        return tipologia;
    }

    /**
     * Imposta una nuova <strong>tipologia</strong> per l'accesso.
     *
     * @param tipologia La nuova tipologia dell'accesso.
     */
    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }

    /**
     * Restituisce la <strong>posizione anatomica</strong> dell'accesso.
     *
     * @return La posizione anatomica dell'accesso.
     */
    public String getPosizione() {
        return posizione;
    }

    /**
     * Imposta una nuova <strong>posizione</strong> per l'accesso.
     *
     * @param posizione La nuova posizione anatomica dell'accesso.
     */
    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    /**
     * Restituisce il <strong>lato</strong> dell'accesso.
     *
     * @return Il lato dell'accesso ("DX" o "SX").
     */
    public String getLato() {
        return lato;
    }

    /**
     * Imposta il <strong>lato</strong> dell'accesso.
     *
     * @param lato Il nuovo lato dell'accesso ("DX" o "SX"). Viene validato per accettare solo questi due valori.
     * @throws IllegalArgumentException se il lato fornito non è "DX" o "SX".
     */
    public void setLato(String lato) {
        this.lato = lato;
    }

    /**
     * Restituisce la <strong>misura</strong> dell'accesso.
     *
     * @return La misura dell'accesso (Gauge).
     */
    public Integer getMisura() {
        return misura;
    }

    /**
     * Imposta la <strong>misura</strong> dell'accesso.
     *
     * @param misura La nuova misura dell'accesso.
     */
    public void setMisura(Integer misura) {
        this.misura = misura;
    }

    /**
     * Fornisce una rappresentazione in formato stringa dell'oggetto {@code Accesso},
     * utile per il debugging e la registrazione.
     *
     * @return Una stringa che descrive l'ID, la tipologia, la posizione, il lato e la misura dell'accesso.
     */
    @Override
    public String toString() {
        return "Accesso{" +
                "idAccesso=" + idAccesso +
                ", tipologia='" + tipologia + '\'' +
                ", posizione='" + posizione + '\'' +
                ", lato='" + lato + '\'' +
                ", misura=" + misura +
                '}';
    }
}