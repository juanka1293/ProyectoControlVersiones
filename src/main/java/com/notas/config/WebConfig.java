package com.notas.config;

// Importaciones con explicación detallada
import org.springframework.context.annotation.Bean; // Permite definir beans de Spring que serán administrados por el contenedor
import org.springframework.context.annotation.Configuration; // Marca esta clase como una clase de configuración de Spring
import org.springframework.web.reactive.config.WebFluxConfigurer; // Interface para personalizar la configuración de WebFlux
import org.springframework.context.support.ResourceBundleMessageSource; // Carga mensajes desde archivos de propiedades para i18n
import org.springframework.web.server.i18n.LocaleContextResolver; // Resuelve el idioma (locale) para cada petición
import org.springframework.web.server.ServerWebExchange; // Contiene información sobre la petición HTTP actual
import org.springframework.context.i18n.LocaleContext; // Mantiene información sobre el idioma actual
import org.springframework.context.i18n.SimpleLocaleContext; // Implementación simple del contexto de idioma
import java.util.Locale; // Representa un idioma y región específicos

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Bean
    public LocaleContextResolver localeContextResolver() {
        return new LocaleContextResolver() {
            @Override
            public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
                String language = exchange.getRequest().getQueryParams().getFirst("lang");
                Locale targetLocale = language != null ? new Locale(language) : new Locale("es");
                return new SimpleLocaleContext(targetLocale);
            }

            @Override
            public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
                // No necesitamos implementar esto para nuestro caso
            }
        };
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        source.setDefaultLocale(new Locale("es"));
        return source;
    }
}
