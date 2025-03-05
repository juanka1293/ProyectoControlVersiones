package com.notas.controlador;

// Importaciones con explicación detallada
import com.notas.modelo.*; // Importa todas las clases del paquete modelo (Usuario, Nota, Materia, etc.)
import com.notas.dto.MateriaDTO; // Objeto de transferencia de datos para Materia
import com.notas.dto.NotaDTO; // Objeto de transferencia de datos para Nota
import com.notas.servicio.ServicioNotas; // Servicio que contiene la lógica de negocio
import org.springframework.beans.factory.annotation.Autowired; // Permite la inyección automática de dependencias
import org.springframework.context.MessageSource; // Permite la internacionalización de mensajes
import org.springframework.context.i18n.LocaleContextHolder; // Mantiene información sobre el idioma actual
import org.springframework.security.core.Authentication; // Contiene la información del usuario autenticado
import org.springframework.stereotype.Controller; // Marca la clase como un controlador web
import org.springframework.ui.Model; // Permite pasar datos a las vistas
import org.springframework.web.bind.annotation.*; // Anotaciones para mapear URLs a métodos
import reactor.core.publisher.Flux; // Contenedor reactivo para 0 a N elementos
import reactor.core.publisher.Mono; // Contenedor reactivo para 0 o 1 elemento
import org.slf4j.Logger; // Interface para crear logs en la aplicación
import org.slf4j.LoggerFactory; // Fábrica que crea instancias del logger
import java.util.ArrayList; // Lista dinámica de elementos
import java.util.stream.Collectors; // Utilidades para operaciones con streams

/**
 * Controlador principal de la aplicación
 * Maneja todas las rutas y la lógica de presentación
 */
@Controller
public class ControladorPrincipal {
    private static final Logger logger = LoggerFactory.getLogger(ControladorPrincipal.class);
    
    @Autowired
    private ServicioNotas servicioNotas;

    @Autowired
    private MessageSource messageSource;

    /**
     * Maneja excepciones generales en la aplicación
     */
    @ExceptionHandler(Exception.class)
    public Mono<String> handleError(Exception ex, Model model) {
        logger.error("Error en la aplicación: ", ex);
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("nombreUsuario", null);
        return Mono.just("error");
    }

    /**
     * Página de inicio de sesión
     */
    @GetMapping("/login")
    public Mono<String> login(Model model, 
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String lang) {
        logger.debug("Accediendo a página de login. Error: {}, Logout: {}, Lang: {}", error, logout, lang);
        
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return Mono.just("login");
    }

    /**
     * Página principal que redirige según el rol del usuario
     */
    @GetMapping("/")
    public Mono<String> index(Authentication auth) {
        if (auth == null) {
            logger.debug("Usuario no autenticado, redirigiendo a login");
            return Mono.just("redirect:/login");
        }
        
        String rol = auth.getAuthorities().iterator().next().getAuthority();
        logger.debug("Usuario autenticado con rol: {}", rol);
        
        if (rol.equals("ROLE_DOCENTE")) {
            return Mono.just("redirect:/docente");
        } else {
            return Mono.just("redirect:/estudiante");
        }
    }

    /**
     * Vista de estudiante que muestra sus notas
     */
    @GetMapping("/estudiante")
    public Mono<String> estudiante(Authentication auth, Model model) {
        logger.debug("Accediendo a portal estudiante");
        String estudianteId = auth.getName();
        
        return servicioNotas.buscarUsuario(estudianteId)
            .flatMap(estudiante -> {
                logger.debug("Estudiante encontrado: {}", estudiante);
                model.addAttribute("estudiante", estudiante);
                model.addAttribute("nombreUsuario", estudiante.getNombre());
                
                return servicioNotas.obtenerNotasEstudiante(estudianteId)
                    .flatMap(nota -> 
                        servicioNotas.obtenerMateria(nota.getMateriaId())
                            .map(materia -> {
                                NotaDTO dto = new NotaDTO();
                                dto.setDocumento(nota.getEstudianteId());
                                
                                // Traducir el nombre de la materia
                                String nombreMateria = messageSource.getMessage(
                                    materia.getNombre(), 
                                    null, 
                                    materia.getNombre(), 
                                    LocaleContextHolder.getLocale()
                                );
                                dto.setNombreMateria(nombreMateria + " " + materia.getGrado());
                                dto.setNota(nota.getCalificacion());
                                dto.setNombreEstudiante(estudiante.getNombre());
                                logger.debug("Creando DTO de nota: {}", dto);
                                return dto;
                            })
                    )
                    .collectList()
                    .doOnNext(notas -> {
                        logger.debug("Total de notas encontradas: {}", notas.size());
                        model.addAttribute("notas", notas);
                    })
                    .thenReturn("estudiante");
            });
    }

    /**
     * Vista de docente que muestra sus materias
     */
    @GetMapping("/docente")
    public Mono<String> docente(Authentication auth, Model model) {
        logger.debug("Accediendo a portal docente");
        String docenteId = auth.getName();
        model.addAttribute("nombreUsuario", docenteId);
        
        return servicioNotas.obtenerMateriasDocente(docenteId)
            .flatMap(materia -> {
                MateriaDTO dto = new MateriaDTO();
                dto.setId(materia.getId());
                dto.setCodigo(materia.getId());
                
                // Traducir el nombre de la materia usando messageSource
                String nombreMateria = messageSource.getMessage(
                    materia.getNombre(), 
                    null, 
                    materia.getNombre(), 
                    LocaleContextHolder.getLocale()
                );
                dto.setNombre(nombreMateria);
                dto.setGrado(materia.getGrado());
                
                return servicioNotas.obtenerNotasMateria(materia.getId())
                    .map(nota -> nota.getEstudianteId())
                    .collectList()
                    .map(estudiantes -> {
                        dto.setEstudiantes(estudiantes);
                        return dto;
                    });
            })
            .collectList()
            .doOnNext(materias -> model.addAttribute("materias", materias))
            .thenReturn("docente");
    }

    /**
     * Vista de materia que muestra las notas de los estudiantes
     */
    @GetMapping("/materia/{id}")
    public Mono<String> materia(@PathVariable String id, Model model, Authentication auth) {
        logger.debug("Accediendo a la materia {}", id);
        model.addAttribute("nombreUsuario", auth.getName());
        
        return servicioNotas.obtenerMateria(id)
            .map(materia -> {
                MateriaDTO dto = new MateriaDTO();
                dto.setId(materia.getId());
                dto.setCodigo(materia.getId());
                
                // Traducir el nombre de la materia
                String nombreMateria = messageSource.getMessage(
                    materia.getNombre(), 
                    null, 
                    materia.getNombre(), 
                    LocaleContextHolder.getLocale()
                );
                dto.setNombre(nombreMateria + " " + materia.getGrado());
                dto.setGrado(materia.getGrado());
                return dto;
            })
            .flatMap(materiaDTO -> {
                model.addAttribute("materia", materiaDTO);
                
                // Obtener estudiantes que tienen nota en esta materia
                Mono<Void> notas = servicioNotas.obtenerNotasMateria(id)
                    .flatMap(nota -> 
                        servicioNotas.buscarUsuario(nota.getEstudianteId())
                            .map(estudiante -> {
                                NotaDTO dto = new NotaDTO();
                                dto.setDocumento(nota.getEstudianteId());
                                dto.setNombreEstudiante(estudiante.getNombre());
                                dto.setNota(nota.getCalificacion());
                                return dto;
                            })
                    )
                    .collectList()
                    .doOnNext(notasDTO -> model.addAttribute("notas", notasDTO))
                    .then();

                // Obtener estudiantes del grado de la materia que no tienen nota
                Mono<Void> estudiantes = servicioNotas.obtenerEstudiantesGrado(materiaDTO.getGrado())
                    .collectList()
                    .doOnNext(est -> model.addAttribute("estudiantes", est))
                    .then();
                
                return Mono.when(estudiantes, notas)
                    .thenReturn("materia");
            });
    }

    /**
     * Califica a un estudiante en una materia
     */
    @PostMapping("/materia/{id}/calificar")
    public Mono<String> calificar(@PathVariable String id, 
                                 @RequestParam String documento, 
                                 @RequestParam Double nota,
                                 @RequestParam(defaultValue = "es") String lang) {
        logger.debug("Calificando estudiante {} en materia {} con nota {}", documento, id, nota);
        logger.debug("Parámetros recibidos - id: {}, documento: {}, nota: {}, lang: {}", id, documento, nota, lang);
        
        return servicioNotas.calificarEstudiante(id, documento, nota)
            .thenReturn("redirect:/materia/" + id + "?lang=" + lang);
    }

    /**
     * Elimina la nota de un estudiante en una materia
     */
    @PostMapping("/materia/{id}/eliminar")
    public Mono<String> eliminarNota(@PathVariable String id, 
                                    @RequestParam String documento,
                                    @RequestParam(defaultValue = "es") String lang) {
        logger.debug("Eliminando nota de estudiante {} en materia {}", documento, id);
        logger.debug("Parámetros recibidos - id: {}, documento: {}, lang: {}", id, documento, lang);
        
        return servicioNotas.eliminarNota(id, documento)
            .thenReturn("redirect:/materia/" + id + "?lang=" + lang);
    }

    @GetMapping("/registroEstudiante")
    public Mono<String> mostrarRegistroEstudiante(Model model, Authentication auth) {
        model.addAttribute("nombreUsuario", auth.getName());
        return Mono.just("registroEstudiante");
    }

    @PostMapping("/registrarEstudiante")
    public Mono<String> registrarEstudiante(@RequestParam String documento,
                                    @RequestParam String nombre,
                                    @RequestParam String password,
                                    @RequestParam String grado,
                                    @RequestParam(defaultValue = "es") String lang) {
        logger.debug("Registrando estudiante: {}", documento);
        
        return servicioNotas.registrarEstudiante(documento, nombre, password, grado)
            .thenReturn("redirect:/docente?registroExitoso=true&lang=" + lang)
            .onErrorResume(e -> {
                logger.error("Error al registrar estudiante: ", e);
                return Mono.just("redirect:/registroEstudiante?error=true&mensaje=" + e.getMessage() + "&lang=" + lang);
            });
    }
}
