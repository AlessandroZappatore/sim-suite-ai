package it.uniupo.simnova.domain.common;

/**
 * Record che rappresenta un <strong>materiale</strong> generico utilizzato nel sistema.
 * Ogni materiale ha un identificativo univoco, un nome e una descrizione.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public record Materiale(
        int idMateriale,
        String nome,
        String descrizione
) {
    /**
     * Restituisce l'<strong>identificativo univoco</strong> del materiale come Integer.
     * Questo è un metodo accessore personalizzato che sovrascrive il comportamento predefinito
     * del record (che altrimenti restituirebbe un 'int' per 'idMateriale()').
     *
     * @return L'ID del materiale come Integer.
     */
    public Integer getId() {
        return idMateriale; // Autoboxing will convert int to Integer
    }
}