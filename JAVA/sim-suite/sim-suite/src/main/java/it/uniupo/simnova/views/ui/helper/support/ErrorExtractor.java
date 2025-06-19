package it.uniupo.simnova.views.ui.helper.support;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe di utilità per l'estrazione del motivo dell'errore da una risposta JSON.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ErrorExtractor {
    /**
     * Logger per la registrazione degli errori e delle informazioni di debug.
     */
    private static final Logger logger = LoggerFactory.getLogger(ErrorExtractor.class);

    /**
     * Costruttore privato per prevenire l'istanza della classe.
     */
    private ErrorExtractor() {
    }

    /**
     * Estrae il motivo dell'errore da una risposta JSON.
     *
     * @param jsonBody corpo della risposta JSON da cui estrarre il motivo dell'errore
     * @param gson     istanza di Gson per il parsing del JSON
     * @return una stringa che rappresenta il motivo dell'errore, o un messaggio di errore predefinito
     */
    public static String extractErrorReasonFromJson(String jsonBody, Gson gson) {
        if (jsonBody == null || jsonBody.trim().isEmpty()) {
            return "Il server ha restituito una risposta di errore vuota.";
        }
        try {
            JsonObject root = gson.fromJson(jsonBody, JsonObject.class);
            if (root.has("detail") && root.get("detail").isJsonObject()) {
                JsonObject detailObject = root.getAsJsonObject("detail");
                if (detailObject.has("reason") && detailObject.get("reason").isJsonPrimitive()) {
                    return detailObject.get("reason").getAsString();
                }
            }
            return "Il formato della risposta di errore non è quello previsto.";
        } catch (JsonSyntaxException e) {
            logger.error("Errore di sintassi Gson durante il parsing del JSON di errore: {}", jsonBody, e);
            return "La risposta di errore del server non è un JSON valido.";
        } catch (Exception e) {
            logger.error("Errore imprevisto durante il parsing del JSON con Gson: {}", jsonBody, e);
            return "I dettagli specifici dell'errore non sono leggibili.";
        }
    }
}
