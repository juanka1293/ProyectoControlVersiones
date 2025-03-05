package com.notas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import java.util.Locale;

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
