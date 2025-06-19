package it.uniupo.simnova.service.ai_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configurazione CORS per consentire richieste da origini diverse.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    /**
     * Configura le regole CORS per le richieste API.
     *
     * @param registry il registro delle configurazioni CORS
     */
    @Override
    public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
