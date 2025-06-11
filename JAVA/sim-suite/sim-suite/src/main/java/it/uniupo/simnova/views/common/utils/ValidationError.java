package it.uniupo.simnova.views.common.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Classe di utilità per la ***gestione centralizzata degli errori di validazione** nei componenti Vaadin.
 * Fornisce metodi per mostrare notifiche di errore, impostare lo stato di invalidità sui campi
 * e portare il focus sul campo che ha generato l'errore, migliorando l'esperienza utente.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ValidationError {
    /**
     * Costruttore privato per prevenire l'istanza della classe.
     * Questa classe è pensata per essere utilizzata solo tramite metodi statici.
     */
    private ValidationError() {
        // Costruttore privato per prevenire l'istanza della classe.
        // Questa classe è pensata per essere utilizzata solo tramite metodi statici.
    }

    /**
     * Mostra una ***notifica di errore** visibile all'utente.
     * Se il campo fornito implementa {@link HasValidation}, imposta il suo stato come invalido
     * e mostra il messaggio di errore direttamente sul campo. Se il campo è {@link Focusable},
     * imposta il focus su di esso per guidare l'utente alla correzione.
     *
     * @param field   Il componente Vaadin che ha fallito la validazione. Deve essere un'istanza di {@link Component}.
     * @param message Il messaggio di errore dettagliato da visualizzare all'utente.
     */
    public static void showValidationError(Component field, String message) {
        // Mostra una notifica temporanea al centro dello schermo con variante di errore.
        Notification.show(message, 3000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);

        // Se il campo supporta la validazione (es. TextField, ComboBox), imposta lo stato di errore.
        if (field instanceof HasValidation) {
            ((HasValidation) field).setInvalid(true);
            ((HasValidation) field).setErrorMessage(message);
        }

        // Se il campo può ricevere il focus (es. TextField), porta il focus su di esso.
        if (field instanceof Focusable) {
            ((Focusable<?>) field).focus();
        }
    }

    /**
     * Un helper method che ***mostra un errore di validazione e restituisce {@code false}**.
     * Questo è particolarmente utile nelle catene di condizioni di validazione {@code if}
     * o {@code return}, permettendo un codice più conciso.
     *
     * @param field   Il componente Vaadin che ha fallito la validazione.
     * @param message Il messaggio di errore da mostrare.
     * @return Sempre {@code false}, indicando che la validazione non è andata a buon fine.
     */
    public static boolean showErrorAndReturnFalse(Component field, String message) {
        showValidationError(field, message); // Richiama il metodo principale per mostrare l'errore.
        return false; // Restituisce false per la logica di controllo.
    }
}