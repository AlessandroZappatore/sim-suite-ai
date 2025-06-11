package it.uniupo.simnova.views.media;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import it.uniupo.simnova.views.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vista per la visualizzazione a schermo intero di file multimediali.
 * Supporta immagini (JPG, PNG, GIF), PDF, video (MP4, WebM, MOV) e audio (MP3, WAV, OGG).
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Route(value = "media", layout = MainLayout.class)
@PageTitle("Visualizzatore File")
public class MediaFullscreenView extends VerticalLayout implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(MediaFullscreenView.class);
    /**
     * Nome del file da visualizzare, incluso il percorso relativo.
     * Viene impostato tramite il parametro dell'URL.
     */
    private String filename;

    /**
     * Costruttore che configura il layout di base per la visualizzazione a schermo intero.
     */
    public MediaFullscreenView() {
        setSizeFull(); // Imposta le dimensioni della vista a piena altezza e larghezza.
        setPadding(false); // Rimuove il padding.
        setSpacing(false); // Rimuove lo spazio tra i componenti.
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER); // Centra orizzontalmente i componenti.
    }

    /**
     * Imposta il parametro ricevuto dall'URL (il nome del file) e inizializza la vista.
     *
     * @param event    L'evento di navigazione.
     * @param filename Il nome del file da visualizzare, inclusi i caratteri jolly per il percorso.
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String filename) {
        this.filename = filename;
        createView();
    }

    /**
     * Crea il contenuto della vista, rimuovendo quello precedente e aggiungendo il nuovo componente multimediale.
     */
    private void createView() {
        removeAll(); // Rimuove tutti i componenti preesistenti.
        Component mediaContent = createMediaContent(); // Crea il componente specifico per il tipo di media.
        add(mediaContent); // Aggiunge il componente multimediale alla vista.
        expand(mediaContent); // Espande il componente multimediale per occupare lo spazio disponibile.
    }

    /**
     * Crea il componente Vaadin appropriato in base all'estensione del file multimediale.
     *
     * @return Un componente Vaadin che visualizza il file multimediale,
     * o un messaggio di errore se il formato non è supportato o l'estensione non può essere determinata.
     */
    private Component createMediaContent() {
        String fileExtension;

        // Estrae l'estensione del file
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < filename.length() - 1) {
            fileExtension = filename.substring(lastDotIndex + 1).toLowerCase();
        } else {
            logger.warn("Impossibile determinare l'estensione del file: {}", filename);
            return new Div(new Text("Impossibile determinare il tipo di file: " + filename));
        }

        String mediaPath = "/" + filename; // Percorso del file relativo alla radice dell'applicazione web.
        System.out.println("Tentativo di accesso a: " + mediaPath); // Log per debug.

        // Crea il componente in base all'estensione del file
        switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp":
                Image image = new Image(mediaPath, "Immagine");
                image.setWidth("100%");
                image.setHeight("100%");
                image.getStyle()
                        .set("object-fit", "contain") // Contiene l'immagine all'interno del contenitore, senza ritagliarla.
                        .set("max-height", "100vh"); // Limita l'altezza massima all'altezza della viewport.
                return image;

            case "pdf":
                IFrame pdfFrame = new IFrame(mediaPath);
                pdfFrame.setSizeFull(); // Occupa l'intera area disponibile.
                pdfFrame.getStyle().set("border", "none"); // Rimuove il bordo del frame.
                return pdfFrame;

            case "mp4", "webm", "mov":
                NativeVideo video = new NativeVideo();
                video.setSrc(mediaPath);
                video.setControls(true); // Mostra i controlli di riproduzione.
                video.setSizeFull();
                video.getElement().setAttribute("autoplay", true); // Avvia la riproduzione automatica.
                return video;

            case "mp3", "wav", "ogg":
                NativeAudio audio = new NativeAudio();
                audio.setSrc(mediaPath);
                audio.setControls(true); // Mostra i controlli di riproduzione.
                audio.setWidth("100%");
                audio.getStyle()
                        .set("max-width", "800px") // Larghezza massima per il player audio.
                        .set("margin", "auto") // Centra orizzontalmente.
                        .set("position", "absolute") // Posizionamento assoluto per centrare.
                        .set("top", "50%")
                        .set("left", "50%")
                        .set("transform", "translate(-50%, -50%)"); // Centra sia orizzontalmente che verticalmente.
                return audio;

            default:
                logger.warn("Formato file non supportato: {}", filename);
                return new Div(new Text("Formato file non supportato: " + filename));
        }
    }

    /**
     * Componente Vaadin personalizzato per la riproduzione di video HTML5.
     * Estende {@link Component} per incapsulare un elemento HTML `video`.
     */
    private static class NativeVideo extends Component {

        /**
         * Costruttore che crea un elemento video HTML.
         */
        public NativeVideo() {
            super(new Element("video"));
        }

        /**
         * Imposta il percorso (URL) del file video.
         *
         * @param src Percorso del file video.
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Abilita o disabilita i controlli di riproduzione per l'utente.
         *
         * @param controls {@code true} per mostrare i controlli, {@code false} altrimenti.
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta le dimensioni del componente video per occupare il 100% della larghezza e altezza del suo contenitore.
         */
        public void setSizeFull() {
            getElement().setAttribute("width", "100%");
            getElement().setAttribute("height", "100%");
        }
    }

    /**
     * Componente Vaadin personalizzato per la riproduzione di audio HTML5.
     * Estende {@link Component} per incapsulare un elemento HTML `audio`.
     */
    private static class NativeAudio extends Component {

        /**
         * Costruttore che crea un elemento audio HTML.
         */
        public NativeAudio() {
            super(new Element("audio"));
        }

        /**
         * Imposta il percorso (URL) del file audio.
         *
         * @param src Percorso del file audio.
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Abilita o disabilita i controlli di riproduzione per l'utente.
         *
         * @param controls {@code true} per mostrare i controlli, {@code false} altrimenti.
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta la larghezza del componente audio.
         *
         * @param width La larghezza da impostare (es. "100%", "300px").
         */
        public void setWidth(String width) {
            getElement().setAttribute("width", width);
        }
    }
}