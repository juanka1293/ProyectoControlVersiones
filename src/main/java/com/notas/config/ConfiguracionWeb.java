package com.notas.config;

// Importaciones con explicación detallada
import org.springframework.context.annotation.Bean; // Permite definir beans de Spring que serán administrados por el contenedor
import org.springframework.context.annotation.Configuration; // Marca esta clase como una clase de configuración de Spring
import org.springframework.web.reactive.config.EnableWebFlux; // Habilita la configuración de WebFlux en la aplicación
import org.springframework.web.reactive.config.ViewResolverRegistry; // Registro de resolvedores de vistas
import org.springframework.web.reactive.config.WebFluxConfigurer; // Interface para personalizar la configuración de WebFlux
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine; // Interface del motor de plantillas Thymeleaf para WebFlux
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine; // Implementación del motor de plantillas para WebFlux
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver; // Resuelve la ubicación de las plantillas
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver; // Resolvedor de vistas reactivo para Thymeleaf
import org.thymeleaf.templatemode.TemplateMode; // Define el modo de las plantillas (HTML, XML, TEXT, etc.)
import org.thymeleaf.templateresolver.ITemplateResolver; // Interface para resolver plantillas

@Configuration
@EnableWebFlux
public class ConfiguracionWeb implements WebFluxConfigurer {

    @Bean
    public ITemplateResolver thymeleafTemplateResolver() {
        final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        return resolver;
    }

    @Bean
    public ISpringWebFluxTemplateEngine thymeleafTemplateEngine() {
        SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine();
        templateEngine.setTemplateResolver(thymeleafTemplateResolver());
        return templateEngine;
    }

    @Bean
    public ThymeleafReactiveViewResolver thymeleafReactiveViewResolver() {
        ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver();
        viewResolver.setTemplateEngine(thymeleafTemplateEngine());
        viewResolver.setResponseMaxChunkSizeBytes(8192);
        return viewResolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(thymeleafReactiveViewResolver());
    }
}
