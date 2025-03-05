package com.notas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
