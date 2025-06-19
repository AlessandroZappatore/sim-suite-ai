package it.uniupo.simnova.domain.common;

/**
 * Rappresenta un materiale con un identificativo univoco, un nome e una descrizione.
 *
 * @param idMateriale l'identificativo univoco del materiale
 * @param nome il nome del materiale
 * @param descrizione una descrizione del materiale
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