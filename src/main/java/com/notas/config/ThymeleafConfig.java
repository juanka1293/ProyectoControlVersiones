package com.notas.config;

// Importaciones con explicación detallada
import org.springframework.context.annotation.Bean; // Permite definir beans de Spring que serán administrados por el contenedor
import org.springframework.context.annotation.Configuration; // Marca esta clase como una clase de configuración de Spring
import org.thymeleaf.spring6.SpringTemplateEngine; // Motor de plantillas de Thymeleaf para Spring 6
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver; // Resuelve la ubicación de las plantillas HTML
import org.thymeleaf.templatemode.TemplateMode; // Define el modo de las plantillas (HTML, XML, TEXT, etc.)
import org.springframework.context.support.ResourceBundleMessageSource; // Carga mensajes para internacionalización

@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver templateResolver, ResourceBundleMessageSource messageSource) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setMessageSource(messageSource);
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }
}
