package it.uniupo.simnova.domain.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.NonNull; // For non-null validation in constructor/setters if desired
import lombok.RequiredArgsConstructor; // If you want a constructor for final fields

/**
 * Classe che rappresenta un <strong>accesso venoso o arterioso</strong> nel sistema.
 * Definisce le proprietà chiave di un accesso, come tipologia, posizione, lato e misura.
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class Accesso {
    /**
     * Identificativo univoco dell'accesso, assegnato dal database.
     */
    private final int idAccesso; // idAccesso is final, so no setter will be generated for it

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
     * Imposta il lato dell'accesso.
     *
     * @param lato Lato dell'accesso, che deve essere "DX" o "SX".
     */
    public void setLato(@NonNull String lato) {
        if (!"DX".equals(lato) && !"SX".equals(lato)) {
            throw new IllegalArgumentException("Il lato deve essere 'DX' o 'SX'.");
        }
        this.lato = lato;
    }
}