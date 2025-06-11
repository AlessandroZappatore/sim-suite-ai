package it.uniupo.simnova.views.common.utils;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.vaadin.tinymce.TinyMce;

/**
 * Classe di utilità per generare un ***editor di testo TinyMCE** con configurazione predefinita.
 * Fornisce un'interfaccia WYSIWYG (What You See Is What You Get) per l'inserimento
 * e la modifica di contenuto testuale arricchito all'interno dell'applicazione Vaadin.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class TinyEditor extends HorizontalLayout {

    /**
     * Costruttore privato per evitare l'istanza diretta della classe.
     * Utilizzare il metodo statico {@link #getEditor()} per ottenere un'istanza configurata.
     */
    private TinyEditor() {
        // Costruttore privato per evitare l'istanza diretta della classe.
        // Utilizzare il metodo statico getEditor() per ottenere un'istanza configurata.
    }

    /**
     * Genera e configura un'istanza dell'editor di testo ***TinyMce** con un set di funzionalità standard.
     * La configurazione include plugin comuni per la formattazione, i link, le liste, le tabelle,
     * l'inserimento di immagini e la gestione delle pagine.
     *
     * @return Un'istanza di {@link TinyMce} configurata e pronta all'uso.
     */
    public static TinyMce getEditor() {
        TinyMce editor = new TinyMce();
        editor.setWidthFull(); // L'editor occupa tutta la larghezza disponibile.
        editor.setHeight("450px"); // Altezza fissa per l'editor.

        // Configurazione di TinyMCE: plugins, toolbar, menubar, skin, ecc.
        editor.configure(
                "plugins: 'link lists table hr pagebreak image charmap preview', " + // Plugin abilitati
                        "toolbar: 'undo redo | blocks | bold italic | alignleft aligncenter alignright | bullist numlist | link image | table hr', " + // Pulsanti della toolbar
                        "menubar: true, " + // Abilita la barra dei menu
                        "skin: 'oxide', " + // Tema visivo dell'editor
                        "content_css: 'default', " + // Foglio di stile per il contenuto
                        "statusbar: true, " + // Abilita la barra di stato inferiore
                        "resize: true" // Permette il ridimensionamento manuale dell'editor
        );

        // Stili CSS aggiuntivi per l'editor (bordo arrotondato, ombra, overflow nascosto).
        editor.getElement().getStyle()
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("box-shadow", "0 2px 10px rgba(0, 0, 0, 0.05)");

        return editor;
    }
}