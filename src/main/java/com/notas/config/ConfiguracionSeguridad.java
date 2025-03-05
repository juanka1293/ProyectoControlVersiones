package com.notas.config;

// Importaciones con explicación detallada de cada una
import com.notas.servicio.ServicioNotas; // Servicio principal que maneja la lógica de negocio de las notas
import org.slf4j.Logger; // Interface para crear logs (registros) en la aplicación
import org.slf4j.LoggerFactory; // Fábrica que crea instancias del logger
import org.springframework.beans.factory.annotation.Autowired; // Permite la inyección automática de dependencias
import org.springframework.context.annotation.Bean; // Marca un método como proveedor de beans para Spring
import org.springframework.context.annotation.Configuration; // Indica que esta clase es de configuración de Spring
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity; // Habilita la seguridad para WebFlux
import org.springframework.security.config.web.server.ServerHttpSecurity; // Configuración de seguridad para servidor HTTP
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Implementación de encriptación de contraseñas
import org.springframework.security.crypto.password.PasswordEncoder; // Interface para codificar contraseñas
import org.springframework.security.web.server.SecurityWebFilterChain; // Cadena de filtros de seguridad
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler; // Maneja redirecciones después del login exitoso
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler; // Maneja errores de autenticación
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler; // Maneja redirecciones después del logout
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository; // Almacena el contexto de seguridad en la sesión web
import org.springframework.security.core.userdetails.ReactiveUserDetailsService; // Servicio reactivo para cargar datos del usuario
import org.springframework.security.core.userdetails.User; // Clase que representa un usuario en Spring Security
import reactor.core.publisher.Mono; // Contenedor reactivo para 0 o 1 elemento
import java.net.URI; // Clase para manejar URIs (direcciones web)

/**
 * Configuración de seguridad de la aplicación
 * Esta clase maneja toda la configuración de seguridad del sistema, incluyendo:
 * - Autenticación de usuarios
 * - Control de acceso a rutas
 * - Encriptación de contraseñas
 * - Manejo de sesiones
 */
@Configuration // Indica que esta clase es de configuración de Spring
@EnableWebFluxSecurity // Habilita la seguridad para aplicaciones WebFlux
public class ConfiguracionSeguridad {
    
    // Logger para registrar eventos y errores
    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionSeguridad.class);
    
    // Inyección del servicio de notas que contiene la lógica de negocio
    @Autowired
    private ServicioNotas servicioNotas;

    /**
     * Configura el filtro de seguridad principal
     * Este método define:
     * - Qué rutas son públicas y cuáles requieren autenticación
     * - Configuración del formulario de login
     * - Configuración del logout
     * - Manejo de sesiones
     */
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable() // Desactiva la protección CSRF para simplificar las peticiones
            .authorizeExchange(exchanges -> exchanges
                // Rutas públicas que no requieren autenticación
                .pathMatchers("/css/**", "/js/**", "/images/**", "/login", "/error", "/favicon.ico").permitAll()
                // Todas las demás rutas requieren autenticación
                .anyExchange().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login") // Página personalizada de login
                .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/")) // Redirección después del login exitoso
                .authenticationFailureHandler(failureHandler()) // Manejo de errores de login
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL para cerrar sesión
                .logoutSuccessHandler(new RedirectServerLogoutSuccessHandler()) // Redirección después del logout
            )
            .securityContextRepository(new WebSessionServerSecurityContextRepository()) // Almacenamiento del contexto de seguridad
            .build();
    }

    /**
     * Configura el manejador de errores de autenticación
     * Se ejecuta cuando hay un error en el login (credenciales incorrectas)
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
     * Configura el servicio de usuarios reactivo
     * Este servicio se encarga de:
     * - Buscar usuarios en la base de datos
     * - Convertir los usuarios de la base de datos al formato que Spring Security necesita
     * - Manejar los roles y permisos de los usuarios
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
                        .password(usuario.getPassword()) // La contraseña ya viene encriptada
                        .roles(usuario.getRol().toString()) // Asigna los roles del usuario
                        .build();
                })
                .doOnError(error -> logger.error("Error al buscar usuario: {}", error.getMessage()));
        };
    }

    /**
     * Configura el codificador de contraseñas
     * Utiliza BCrypt, un algoritmo de hash seguro para contraseñas
     * BCrypt automáticamente:
     * - Genera un salt único para cada contraseña
     * - Realiza múltiples iteraciones del hash
     * - Produce un hash seguro y diferente cada vez
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
