package it.uniupo.simnova.views.constant;

/**
 * Classe di supporto contenente le <strong>costanti dei colori</strong> da utilizzare per
 * stilizzare le righe e le sezioni dell'interfaccia utente, in particolare
 * per le diverse sezioni temporali.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public final class ColorsConst {

    /**
     * Array di stringhe contenente i <strong>colori predefiniti</strong> per i bordi delle righe
     * delle varie sezioni temporali. Questi colori sono variabili CSS Lumo,
     * oppure codici esadecimali, e sono utilizzati in modo ciclico per distinguere
     * visivamente le sezioni.
     */
    public static final String[] BORDER_COLORS = {
            "var(--lumo-primary-color)", // Colore primario del tema Lumo
            "var(--lumo-error-color)",   // Colore per gli errori del tema Lumo (rosso)
            "var(--lumo-success-color)", // Colore per il successo del tema Lumo (verde)
            "#FFB74D",                   // Arancione chiaro
            "#9575CD",                   // Viola medio
            "#4DD0E1",                   // Azzurro chiaro
            "#F06292"                    // Rosa
    };

    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    public ColorsConst() {
        // Costruttore vuoto per evitare istanziazioni
    }
}