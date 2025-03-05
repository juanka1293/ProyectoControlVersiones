package com.notas.config;

import com.notas.servicio.ServicioNotas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;
import java.net.URI;

/**
 * Configuración de seguridad de la aplicación
 * Maneja la autenticación, autorización y encriptación de contraseñas
 */
@Configuration
@EnableWebFluxSecurity
public class ConfiguracionSeguridad {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionSeguridad.class);
    
    @Autowired
    private ServicioNotas servicioNotas;

    /**
     * Configura el filtro de seguridad principal
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/css/**", "/js/**", "/images/**", "/login", "/error", "/favicon.ico").permitAll()
                .anyExchange().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"))
                .authenticationFailureHandler(failureHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(new RedirectServerLogoutSuccessHandler())
            )
            .securityContextRepository(new WebSessionServerSecurityContextRepository())
            .build();
    }

    /**
     * Configura el manejador de errores de autenticación
     */
    @Bean
    public ServerAuthenticationFailureHandler failureHandler() {
        return (webFilterExchange, exception) -> {
            logger.error("Error de autenticación: {}", exception.getMessage());
            webFilterExchange.getExchange().getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
            webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/login?error"));
            return Mono.empty();
        };
    }

    /**
     * Configura el servicio de usuarios reactivos
     */
    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> {
            logger.debug("Buscando usuario para autenticación: {}", username);
            return servicioNotas.buscarUsuario(username)
                .map(usuario -> {
                    logger.debug("Usuario encontrado: {}, rol: {}", usuario.getDocumento(), usuario.getRol());
                    return User.builder()
                        .username(usuario.getDocumento())
                        .password(usuario.getPassword()) // La contraseña ya viene encriptada del servicio
                        .roles(usuario.getRol().toString())
                        .build();
                })
                .doOnError(error -> logger.error("Error al buscar usuario: {}", error.getMessage()));
        };
    }

    /**
     * Configura el codificador de contraseñas BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
