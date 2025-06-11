package it.uniupo.simnova.service.export.helper.pdf;

/**
 * Questa classe di supporto è utilizzata per <strong>rimuovere o sostituire caratteri speciali</strong>
 * (come quelli in apice e in pedice) dal testo destinato alla generazione di PDF.
 * Senza questa gestione, il font utilizzato in Apache PDFBox potrebbe non essere in grado
 * di renderizzare correttamente questi caratteri, sollevando eccezioni che impedirebbero
 * la creazione del documento PDF.
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
public class ReplaceSubscript {

    /**
     * Costruttore privato per impedire l'istanza della classe.
     */
    private ReplaceSubscript() {
        // Costruttore privato per impedire l'istanza della classe
        // poiché contiene solo metodi statici.
    }

    /**
     * Sostituisce i caratteri Unicode comunemente usati per gli apici e i pedici
     * con i loro equivalenti numerici o simboli normali. Questo assicura che il testo
     * sia compatibile con i font standard utilizzati nella generazione PDF.
     *
     * @param text Il testo da elaborare. Se il testo è <code>null</code>, il metodo restituisce <code>null</code>.
     * @return Il testo con i caratteri in apice e in pedice sostituiti da caratteri normali.
     */
    public static String replaceSubscriptCharacters(String text) {
        if (text == null) {
            return null;
        }

        // Sostituzioni per caratteri in pedice (numeri e simboli)
        return text.replace('₁', '1')
                .replace('₂', '2')
                .replace('₃', '3')
                .replace('₄', '4')
                .replace('₅', '5')
                .replace('₆', '6')
                .replace('₇', '7')
                .replace('₈', '8')
                .replace('₉', '9')
                .replace('₀', '0')
                // Sostituzioni per caratteri in apice (numeri e simboli)
                .replace('⁰', '0')
                .replace('¹', '1')
                .replace('²', '2')
                .replace('³', '3')
                .replace('⁴', '4')
                .replace('⁵', '5')
                .replace('⁶', '6')
                .replace('⁷', '7')
                .replace('⁸', '8')
                .replace('⁹', '9')
                .replace('⁻', '-')
                .replace('⁺', '+');
    }
}