package com.notas.config;

// Importaciones con explicación detallada
import org.springframework.context.annotation.Bean; // Permite definir beans de Spring que serán administrados por el contenedor
import org.springframework.context.annotation.Configuration; // Marca esta clase como una clase de configuración de Spring
import org.springframework.context.support.ResourceBundleMessageSource; // Carga mensajes desde archivos de propiedades para i18n
import org.springframework.web.reactive.config.WebFluxConfigurer; // Interface para personalizar la configuración de WebFlux
import org.springframework.web.server.i18n.LocaleContextResolver; // Resuelve el idioma (locale) para cada petición
import org.springframework.context.i18n.LocaleContext; // Mantiene información sobre el idioma actual
import org.springframework.context.i18n.SimpleLocaleContext; // Implementación simple del contexto de idioma
import org.springframework.web.server.ServerWebExchange; // Contiene información sobre la petición HTTP actual
import org.springframework.web.server.WebFilter; // Permite interceptar y modificar peticiones web
import reactor.core.publisher.Mono; // Contenedor reactivo para manejar respuestas asíncronas
import java.util.Locale; // Representa un idioma y región específicos
import org.slf4j.Logger; // Interface para crear logs en la aplicación
import org.slf4j.LoggerFactory; // Fábrica que crea instancias del logger

/**
 * Configuración para la internacionalización (i18n) de la aplicación
 * Maneja la detección y cambio de idioma basado en parámetros URL
 */
@Configuration
public class ConfiguracionInternacionalizacion implements WebFluxConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionInternacionalizacion.class);

    /**
     * Configura el origen de los mensajes para i18n
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        source.setFallbackToSystemLocale(false);
        logger.debug("Configurando ResourceBundleMessageSource");
        return source;
    }

    /**
     * Configura el resolver de locale que determina el idioma actual
     */
    @Bean
    public LocaleContextResolver localeContextResolver() {
        return new LocaleContextResolver() {
            @Override
            public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
                String language = exchange.getRequest().getQueryParams().getFirst("lang");
                
                if (language != null && (language.equals("es") || language.equals("en"))) {
                    exchange.getSession().subscribe(session -> 
                        session.getAttributes().put("LOCALE", language)
                    );
                    logger.debug("Cambiando idioma a: {}", language);
                    return new SimpleLocaleContext(new Locale(language));
                }
                
                return exchange.getSession()
                    .map(session -> {
                        String sessionLang = (String) session.getAttributes().get("LOCALE");
                        if (sessionLang != null && (sessionLang.equals("es") || sessionLang.equals("en"))) {
                            logger.debug("Usando idioma de sesión: {}", sessionLang);
                            return new SimpleLocaleContext(new Locale(sessionLang));
                        }
                        logger.debug("Usando idioma por defecto: es");
                        return new SimpleLocaleContext(new Locale("es"));
                    })
                    .defaultIfEmpty(new SimpleLocaleContext(new Locale("es")))
                    .block();
            }

            @Override
            public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
                // No necesario implementar
            }
        };
    }

    @Bean
    public WebFilter languageFilter() {
        return (exchange, chain) -> {
            String language = exchange.getRequest().getQueryParams().getFirst("lang");
            if (language != null && (language.equals("es") || language.equals("en"))) {
                exchange.getSession().subscribe(session -> {
                    session.getAttributes().put("LOCALE", language);
                    logger.debug("Guardando idioma en sesión: {}", language);
                });
            }
            return chain.filter(exchange);
        };
    }
}
