package it.uniupo.simnova.service.export.helper.pdf;

/**
 * Questa classe di supporto contiene le <strong>costanti di configurazione</strong>
 * usate per la generazione di documenti PDF. Definisce le dimensioni dei font,
 * i margini e l'interlinea per assicurare una formattazione coerente e leggibile
 * nei PDF prodotti dall'applicazione.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public final class PdfConstant {

    /**
     * La dimensione del font per l'<strong>intestazione principale</strong> del PDF,
     * come ad esempio il titolo dello scenario.
     */
    public static final float HEADER_FONT_SIZE = 14;
    /**
     * La spaziatura verticale tra le righe di testo (<strong>interlinea</strong>) in punti.
     * Questa costante influisce direttamente sulla leggibilità del testo.
     */
    public static final float LEADING = 14;
    /**
     * Il <strong>margine</strong> laterale e superiore/inferiore della pagina in punti.
     * Definisce lo spazio bianco attorno al contenuto principale del PDF.
     */
    public static final float MARGIN = 40;
    /**
     * La dimensione del font per il <strong>corpo del testo</strong> principale del PDF.
     */
    public static final float BODY_FONT_SIZE = 11;
    /**
     * La dimensione del font per <strong>note, didascalie o testo secondario</strong>
     * che richiede minore enfasi.
     */
    public static final float SMALL_FONT_SIZE = 9;
    /**
     * La dimensione del font per il <strong>titolo principale del documento PDF</strong>,
     * come il titolo generale del report.
     */
    public static final float TITLE_FONT_SIZE = 16;

    /**
     * Costruttore privato per prevenire l'istanza della classe.
     */
    private PdfConstant() {
        // Costruttore privato per prevenire l'istanza della classe
    }
}