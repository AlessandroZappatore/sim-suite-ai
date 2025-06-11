package it.uniupo.simnova.views.ui.helper;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.views.common.utils.FieldGenerator;
import it.uniupo.simnova.views.common.utils.StyleApp;

import java.util.List;
import java.util.Optional;

/**
 * Componente UI per la gestione di un singolo accesso (venoso o arterioso).
 * Permette di selezionare il tipo, la posizione, il lato e la misura dell'accesso.
 * Include opzionalmente un pulsante per la rimozione del componente.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class AccessoComponent extends HorizontalLayout {
    /**
     * Campo di selezione per il tipo di accesso.
     */
    private final Select<String> tipoSelect;
    /**
     * Campo di testo per la posizione dell'accesso.
     */
    private final TextField posizioneField;
    /**
     * Campo di selezione per il lato dell'accesso (DX/SX).
     */
    private final Select<String> latoSelect;
    /**
     * Campo di selezione per la misura dell'accesso (Gauge).
     */
    private final Select<Integer> misuraSelect;
    /**
     * Oggetto Accesso associato a questo componente.
     * Contiene i dati dell'accesso selezionato.
     */
    private final Accesso accesso;

    /**
     * Costruttore per creare un nuovo componente AccessoComponent.
     *
     * @param tipo      Il tipo generico dell'accesso (es. "Venoso", "Arterioso").
     * @param hasDelete Indica se il componente deve includere un pulsante di rimozione.
     */
    public AccessoComponent(String tipo, boolean hasDelete) {
        setAlignItems(Alignment.BASELINE);
        setSpacing(true);
        getStyle().set("flex-wrap", "wrap"); // Permette al layout di andare a capo su schermi piccoli

        this.accesso = new Accesso(0, "", "", "", 0); // Inizializza un nuovo oggetto Accesso

        // Campo di selezione per il tipo di accesso
        tipoSelect = FieldGenerator.createSelect(
                "Tipo accesso " + tipo,
                List.of("Periferico", "Centrale", "CVC a breve termine", "CVC tunnellizzato",
                        "PICC", "Midline", "Intraosseo", "PORT", "Dialysis catheter", "Altro"),
                null,
                true
        );
        // Personalizza le opzioni per gli accessi arteriosi
        if (tipo.equals("Arterioso")) {
            tipoSelect.setItems(
                    "Radiale", "Femorale", "Omerale", "Brachiale", "Ascellare", "Pedidia", "Altro"
            );
        }
        tipoSelect.setWidth(null); // Larghezza automatica
        tipoSelect.getStyle()
                .set("flex-basis", "220px") // Larghezza base per il layout responsivo
                .set("min-width", "180px"); // Larghezza minima
        tipoSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                accesso.setTipologia(e.getValue());
            }
        });

        // Campo di testo per la posizione dell'accesso
        posizioneField = FieldGenerator.createTextField(
                "Posizione",
                "(es. avambraccio, collo, torace)",
                true
        );
        posizioneField.setWidth(null);
        posizioneField.getStyle()
                .set("flex-basis", "200px")
                .set("min-width", "180px");
        posizioneField.addValueChangeListener(e -> accesso.setPosizione(e.getValue()));

        // Campo di selezione per il lato (DX/SX)
        latoSelect = FieldGenerator.createSelect(
                "Lato",
                List.of("DX", "SX"),
                null,
                true
        );
        latoSelect.setWidth(null);
        latoSelect.getStyle()
                .set("flex-basis", "100px")
                .set("min-width", "90px");
        latoSelect.addValueChangeListener(e -> accesso.setLato(e.getValue()));

        // Campo di selezione per la misura (Gauge)
        misuraSelect = FieldGenerator.createSelect(
                "Misura (Gauge)",
                List.of(14, 16, 18, 20, 22, 24, 26),
                null,
                true
        );
        misuraSelect.setWidth(null);
        misuraSelect.getStyle()
                .set("flex-basis", "140px")
                .set("min-width", "120px");
        misuraSelect.addValueChangeListener(e -> accesso.setMisura(e.getValue()));

        // Pulsante per rimuovere il componente, se richiesto
        if (hasDelete) {
            Button removeButton = StyleApp.getButton(
                    "Rimuovi",
                    VaadinIcon.TRASH,
                    ButtonVariant.LUMO_ERROR,
                    "var(--lumo-error-color)"
            );
            removeButton.getStyle()
                    .set("flex-grow", "0")
                    .set("flex-shrink", "0")
                    .set("align-self", "flex-end"); // Allinea il pulsante alla fine della riga
            removeButton.addClickListener(e -> removeSelf()); // Listener per la rimozione
            add(tipoSelect, posizioneField, latoSelect, misuraSelect, removeButton);
        } else {
            add(tipoSelect, posizioneField, latoSelect, misuraSelect);
        }
    }

    /**
     * Costruttore che inizializza il componente con i dati di un oggetto Accesso esistente.
     * @param accesso Oggetto Accesso da cui prendere i dati
     * @param tipo Tipo generico dell'accesso ("Venoso" o "Arterioso")
     * @param hasDelete Se true mostra il pulsante elimina
     */
    public AccessoComponent(Accesso accesso, String tipo, boolean hasDelete) {
        this(tipo, hasDelete);
        if (accesso != null) {
            tipoSelect.setValue(accesso.getTipologia());
            posizioneField.setValue(accesso.getPosizione());
            latoSelect.setValue(accesso.getLato());
            misuraSelect.setValue(accesso.getMisura());
            // Aggiorna anche l'oggetto interno
            this.accesso.setTipologia(accesso.getTipologia());
            this.accesso.setPosizione(accesso.getPosizione());
            this.accesso.setLato(accesso.getLato());
            this.accesso.setMisura(accesso.getMisura());
        }
    }

    /**
     * Rimuove questo componente dalla sua vista genitore.
     */
    private void removeSelf() {
        Optional<Component> parentOpt = getParent();
        parentOpt.ifPresent(parent -> {
            if (parent instanceof VerticalLayout container) {
                container.remove(this);
            }
        });
    }

    /**
     * Restituisce l'oggetto {@link Accesso} associato a questo componente,
     * aggiornando i suoi campi con i valori correnti dei selettori e dei campi di testo.
     *
     * @return L'oggetto {@link Accesso} con i dati attuali del componente.
     */
    public Accesso getAccesso() {
        accesso.setTipologia(tipoSelect.getValue());
        accesso.setPosizione(posizioneField.getValue());
        accesso.setLato(latoSelect.getValue());
        accesso.setMisura(misuraSelect.getValue());
        return accesso;
    }
}

