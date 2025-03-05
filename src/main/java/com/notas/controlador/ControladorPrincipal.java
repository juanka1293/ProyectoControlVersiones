package com.notas.controlador;

import com.notas.modelo.*;
import com.notas.dto.MateriaDTO;
import com.notas.dto.NotaDTO;
import com.notas.servicio.ServicioNotas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Controlador principal de la aplicación
 * Maneja todas las rutas y la lógica de presentación
 */
@Controller
public class ControladorPrincipal {
    private static final Logger logger = LoggerFactory.getLogger(ControladorPrincipal.class);
    
    @Autowired
    private ServicioNotas servicioNotas;

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
        model.addAttribute("nombreUsuario", estudianteId);
        
        return servicioNotas.buscarUsuario(estudianteId)
            .flatMap(estudiante -> {
                model.addAttribute("estudiante", estudiante);
                return servicioNotas.obtenerNotasEstudiante(estudianteId)
                    .flatMap(nota -> 
                        servicioNotas.obtenerMateria(nota.getMateriaId())
                            .map(materia -> {
                                NotaDTO dto = new NotaDTO();
                                dto.setDocumento(nota.getEstudianteId());
                                dto.setNombreMateria(materia.getNombre());
                                dto.setNota(nota.getCalificacion());
                                return dto;
                            })
                    )
                    .collectList()
                    .doOnNext(notas -> model.addAttribute("notas", notas))
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
                dto.setNombre(materia.getNombre());
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
                dto.setNombre(materia.getNombre());
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
}
