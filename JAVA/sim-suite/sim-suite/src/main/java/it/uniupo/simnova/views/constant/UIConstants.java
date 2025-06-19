package it.uniupo.simnova.views.constant;

/**
 * Classe che contiene le costanti utilizzate nell'interfaccia utente.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public final class UIConstants {
    /**
     * Percorso della cartella delle icone utilizzate nell'interfaccia utente.
     */
    public static final String ICONS_PATH = "icons/";
    /**
     * Percorso per la gif dell'ambulanza.
     */
    public static final String AMBULANCE_GIF_PATH = ICONS_PATH + "ambulance.gif";
    /**
     * Percorso per la gif del laboratorio.
     */
    public static final String LAB_GIF_PATH = ICONS_PATH + "lab.gif";
    /**
     * Percorso per la gif del referto.
     */
    public static final String REF_GIF_PATH = ICONS_PATH + "ref.gif";
    /**
     * Percorso per la gif dei materiali necessari.
     */
    public static final String MAT_GIF_PATH = ICONS_PATH + "mat.gif";

    /**
     * Costruttore privato per evitare l'istanza della classe.
     */
    private UIConstants() {
    }

}