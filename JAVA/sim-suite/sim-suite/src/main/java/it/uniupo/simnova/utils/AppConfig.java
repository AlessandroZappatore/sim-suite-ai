package it.uniupo.simnova.utils;

import com.vaadin.open.App;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configurazione dell'applicazione per la creazione di bean comuni.
 * Questa classe definisce i bean necessari per l'applicazione, come RestTemplate e ExecutorService.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Configuration
public class AppConfig {

    /**
     * Crea un bean RestTemplate per effettuare chiamate HTTP.
     *
     * @return Un'istanza di RestTemplate configurata per l'applicazione.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Crea un bean ExecutorService per gestire l'esecuzione di task in thread separati.
     * Utilizza un thread pool con un numero variabile di thread, che si adatta al carico di lavoro.
     *
     * @return Un'istanza di ExecutorService configurata per l'applicazione.
     */
    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }
}