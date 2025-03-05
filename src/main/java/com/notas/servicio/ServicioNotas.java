package com.notas.servicio;

import com.notas.modelo.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.*;

@Service
public class ServicioNotas {
    private static final Logger logger = LoggerFactory.getLogger(ServicioNotas.class);
    private final Map<String, Usuario> usuarios = new HashMap<>();
    private final Map<String, Materia> materias = new HashMap<>();
    private final Map<String, List<Nota>> notas = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ServicioNotas() {
        inicializarDatos();
    }

    private void inicializarDatos() {
        String passwordPlano = "pass123";
        String passwordEncriptado = passwordEncoder.encode(passwordPlano);
        logger.debug("Contraseña encriptada para usuarios de prueba: {}", passwordEncriptado);
        
        Usuario docenteMat = new Usuario("D001", passwordEncriptado, "Juan Pérez", Rol.DOCENTE);
        Usuario docenteEsp = new Usuario("D002", passwordEncriptado, "Ana Gómez", Rol.DOCENTE);
        Usuario docenteCie = new Usuario("D003", passwordEncriptado, "Carlos Ruiz", Rol.DOCENTE);
        Usuario docenteSoc = new Usuario("D004", passwordEncriptado, "María López", Rol.DOCENTE);
        Usuario docenteIng = new Usuario("D005", passwordEncriptado, "Pedro Sánchez", Rol.DOCENTE);
        Usuario estudiante1 = new Usuario("E001", passwordEncriptado, "Ana García", Rol.ESTUDIANTE, "9A");
        Usuario estudiante2 = new Usuario("E002", passwordEncriptado, "Carlos López", Rol.ESTUDIANTE, "9A");
        Usuario estudiante3 = new Usuario("E003", passwordEncriptado, "María Rodríguez", Rol.ESTUDIANTE, "9B");
        Usuario estudiante4 = new Usuario("E004", passwordEncriptado, "Pedro Martínez", Rol.ESTUDIANTE, "9B");
        Usuario estudiante5 = new Usuario("E005", passwordEncriptado, "Laura Torres", Rol.ESTUDIANTE, "10A");
        Usuario estudiante6 = new Usuario("E006", passwordEncriptado, "Diego Ramírez", Rol.ESTUDIANTE, "10A");
        Usuario estudiante7 = new Usuario("E007", passwordEncriptado, "Sofia Vargas", Rol.ESTUDIANTE, "10B");
        Usuario estudiante8 = new Usuario("E008", passwordEncriptado, "Daniel Castro", Rol.ESTUDIANTE, "10B");
        
        usuarios.put(docenteMat.getDocumento(), docenteMat);
        usuarios.put(docenteEsp.getDocumento(), docenteEsp);
        usuarios.put(docenteCie.getDocumento(), docenteCie);
        usuarios.put(docenteSoc.getDocumento(), docenteSoc);
        usuarios.put(docenteIng.getDocumento(), docenteIng);
        usuarios.put(estudiante1.getDocumento(), estudiante1);
        usuarios.put(estudiante2.getDocumento(), estudiante2);
        usuarios.put(estudiante3.getDocumento(), estudiante3);
        usuarios.put(estudiante4.getDocumento(), estudiante4);
        usuarios.put(estudiante5.getDocumento(), estudiante5);
        usuarios.put(estudiante6.getDocumento(), estudiante6);
        usuarios.put(estudiante7.getDocumento(), estudiante7);
        usuarios.put(estudiante8.getDocumento(), estudiante8);

        // Crear materias para cada grado
        crearMaterias("9A");
        crearMaterias("9B");
        crearMaterias("10A");
        crearMaterias("10B");

        // Agregar notas iniciales para cada estudiante
        inicializarNotas(estudiante1.getDocumento(), "9A");
        inicializarNotas(estudiante2.getDocumento(), "9A");
        inicializarNotas(estudiante3.getDocumento(), "9B");
        inicializarNotas(estudiante4.getDocumento(), "9B");
        inicializarNotas(estudiante5.getDocumento(), "10A");
        inicializarNotas(estudiante6.getDocumento(), "10A");
        inicializarNotas(estudiante7.getDocumento(), "10B");
        inicializarNotas(estudiante8.getDocumento(), "10B");
    }

    private void crearMaterias(String grado) {
        // Matemáticas
        Materia matematicas = new Materia("MAT" + grado, "Matemáticas " + grado, grado, "D001");
        materias.put(matematicas.getId(), matematicas);

        // Español
        Materia espanol = new Materia("ESP" + grado, "Español " + grado, grado, "D002");
        materias.put(espanol.getId(), espanol);

        // Ciencias Naturales
        Materia ciencias = new Materia("CIE" + grado, "Ciencias Naturales " + grado, grado, "D003");
        materias.put(ciencias.getId(), ciencias);

        // Sociales
        Materia sociales = new Materia("SOC" + grado, "Ciencias Sociales " + grado, grado, "D004");
        materias.put(sociales.getId(), sociales);

        // Inglés
        Materia ingles = new Materia("ING" + grado, "Inglés " + grado, grado, "D005");
        materias.put(ingles.getId(), ingles);
    }

    private void inicializarNotas(String estudianteId, String grado) {
        // Obtener todas las materias del grado
        materias.values().stream()
               .filter(m -> m.getGrado().equals(grado))
               .forEach(materia -> {
                   // Generar una nota aleatoria entre 3.0 y 5.0
                   double notaAleatoria = 3.0 + Math.random() * 2.0;
                   // Redondear a un decimal
                   notaAleatoria = Math.round(notaAleatoria * 10.0) / 10.0;
                   agregarNota(estudianteId, materia.getId(), notaAleatoria);
               });
    }

    private void agregarNota(String estudianteId, String materiaId, double calificacion) {
        Usuario estudiante = usuarios.get(estudianteId);
        Materia materia = materias.get(materiaId);
        
        if (estudiante != null && materia != null && estudiante.getGrado().equals(materia.getGrado())) {
            Nota nota = new Nota(estudianteId, materiaId, calificacion);
            notas.computeIfAbsent(materiaId, k -> new ArrayList<>()).add(nota);
        } else {
            logger.warn("No se puede agregar nota: estudiante y materia deben ser del mismo grado");
        }
    }

    public Mono<Usuario> buscarUsuario(String documento) {
        logger.debug("Buscando usuario con documento: {}", documento);
        Usuario usuario = usuarios.get(documento);
        if (usuario != null) {
            logger.debug("Usuario encontrado: {}", usuario);
            return Mono.just(usuario);
        } else {
            logger.debug("Usuario no encontrado: {}", documento);
            return Mono.empty();
        }
    }

    public Flux<Usuario> obtenerEstudiantes() {
        return Flux.fromIterable(usuarios.values())
            .filter(u -> u.getRol() == Rol.ESTUDIANTE);
    }

    public Mono<Materia> obtenerMateria(String id) {
        return Mono.justOrEmpty(materias.get(id));
    }

    public Flux<Materia> obtenerMateriasDocente(String docenteId) {
        return Flux.fromIterable(materias.values())
            .filter(m -> m.getDocenteId().equals(docenteId));
    }

    public Flux<Nota> obtenerNotasMateria(String materiaId) {
        return Flux.fromIterable(notas.getOrDefault(materiaId, new ArrayList<>()));
    }

    public Flux<Nota> obtenerNotasEstudiante(String estudianteId) {
        return Flux.fromIterable(materias.values())
            .flatMap(materia -> Flux.fromIterable(notas.getOrDefault(materia.getId(), new ArrayList<>()))
                .filter(nota -> nota.getEstudianteId().equals(estudianteId))
                .map(nota -> {
                    nota.setNombreMateria(materia.getNombre());
                    return nota;
                }));
    }

    public Mono<Void> calificarEstudiante(String materiaId, String estudianteId, Double calificacion) {
        logger.debug("Calificando estudiante {} en materia {} con nota {}", estudianteId, materiaId, calificacion);
        
        Usuario estudiante = usuarios.get(estudianteId);
        Materia materia = materias.get(materiaId);
        
        if (estudiante != null && materia != null && estudiante.getGrado().equals(materia.getGrado())) {
            List<Nota> notasMateria = notas.get(materiaId);
            if (notasMateria != null) {
                Optional<Nota> notaExistente = notasMateria.stream()
                    .filter(n -> n.getEstudianteId().equals(estudianteId))
                    .findFirst();
                
                if (notaExistente.isPresent()) {
                    logger.debug("Actualizando nota existente de {} a {}", notaExistente.get().getCalificacion(), calificacion);
                    notasMateria.remove(notaExistente.get());
                    notasMateria.add(new Nota(estudianteId, materiaId, calificacion));
                } else {
                    logger.debug("Agregando nueva nota para el estudiante");
                    notasMateria.add(new Nota(estudianteId, materiaId, calificacion));
                }
            } else {
                logger.debug("Creando nueva lista de notas para la materia");
                List<Nota> nuevasNotas = new ArrayList<>();
                nuevasNotas.add(new Nota(estudianteId, materiaId, calificacion));
                notas.put(materiaId, nuevasNotas);
            }
        } else {
            logger.warn("No se puede calificar: estudiante y materia deben ser del mismo grado");
        }
        
        return Mono.empty();
    }

    public Mono<Void> eliminarNota(String materiaId, String estudianteId) {
        List<Nota> notasMateria = notas.get(materiaId);
        if (notasMateria != null) {
            notasMateria.removeIf(n -> n.getEstudianteId().equals(estudianteId));
        }
        return Mono.empty();
    }

    public Flux<Usuario> obtenerEstudiantesGrado(String grado) {
        return Flux.fromIterable(usuarios.values())
            .filter(u -> u.getRol() == Rol.ESTUDIANTE && u.getGrado() != null && u.getGrado().equals(grado));
    }

    private Flux<Materia> obtenerMateriasEstudiante(String estudianteId) {
        return Flux.fromIterable(notas.values())
            .flatMap(Flux::fromIterable)
            .filter(n -> n.getEstudianteId().equals(estudianteId))
            .map(n -> materias.get(n.getMateriaId()));
    }
}
