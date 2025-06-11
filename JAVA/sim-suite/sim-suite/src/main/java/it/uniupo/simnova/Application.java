package it.uniupo.simnova;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import it.uniupo.simnova.utils.DBConnect;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Classe principale dell'applicazione Spring Boot per SIM Suite.
 * Questa classe avvia l'applicazione, inizializza la connessione al database SQLite
 * e configura le impostazioni principali della pagina web, come titolo, favicon e PWA.
 *
 * @author Alessandro Zappatore
 * @version 1.1
 */
@Push
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Theme(value = "sim.suite") // Specifica il tema Vaadin utilizzato dall'applicazione
@PWA(
        name = "Sim Suite", // Nome completo dell'applicazione per la PWA
        shortName = "SimSuite", // Nome breve per la PWA (es. icona nella schermata home)
        iconPath = "/icons/favicon.ico" // Imposta esplicitamente l'icona per la PWA
)
public class Application implements AppShellConfigurator {
    /**
     * Logger per registrare eventi ed errori durante l'esecuzione dell'applicazione.
     */
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    /**
     * Costruttore della classe Application.
     * Può essere utilizzato per ulteriori inizializzazioni se necessario.
     */
    public Application() {
        // Costruttore vuoto, può essere utilizzato per ulteriori inizializzazioni se necessario
    }

    /**
     * Metodo principale che avvia l'applicazione.
     * Esegue prima l'inizializzazione del database e poi avvia l'applicazione Spring Boot.
     *
     * @param args Argomenti passati dalla riga di comando.
     */
    public static void main(String[] args) {
        initializeDatabase(); // Inizializza la connessione al database
        SpringApplication.run(Application.class, args); // Avvia l'applicazione Spring Boot
    }

    /**
     * Inizializza il database SQLite verificando la connessione.
     * Registra nel logger il successo o il fallimento della connessione.
     */
    private static void initializeDatabase() {
        try (Connection connection = DBConnect.getInstance().getConnection()) {
            if (connection != null) {
                logger.info("✅ Connessione a SQLite avviata con successo!");
            } else {
                logger.error("⚠️ Errore: impossibile connettersi al database SQLite.");
            }
        } catch (SQLException e) {
            logger.error("❌ Errore durante l'inizializzazione del database: {}", e.getMessage(), e);
        }
    }

    /**
     * Configura le impostazioni della pagina HTML dell'applicazione.
     * Questo include il viewport, il titolo della pagina, le dimensioni del body,
     * i meta tag e le favicon per una migliore esperienza utente su diversi dispositivi.
     *
     * @param settings Le impostazioni della pagina da configurare.
     */
    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setViewport("width=device-width, initial-scale=1"); // Configura il viewport per il responsive design
        settings.setPageTitle("Sim Suite"); // Imposta il titolo visualizzato nella barra del browser
        settings.setBodySize("100vw", "100vh"); // Imposta la dimensione del body per occupare l'intera viewport
        settings.addMetaTag("author", "Alessandro Zappatore"); // Aggiunge il meta tag "author"
        settings.addFavIcon("icon", "icons/favicon.ico", "256x256"); // Aggiunge la favicon principale
        settings.addLink("shortcut icon", "icons/favicon.ico"); // Aggiunge il link per la shortcut icon (per compatibilità)
    }
}
