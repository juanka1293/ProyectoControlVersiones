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
        
        Usuario docente = new Usuario("D001", passwordEncriptado, "Juan Pérez", Rol.DOCENTE);
        Usuario estudiante1 = new Usuario("E001", passwordEncriptado, "Ana García", Rol.ESTUDIANTE);
        Usuario estudiante2 = new Usuario("E002", passwordEncriptado, "Carlos López", Rol.ESTUDIANTE);
        Usuario estudiante3 = new Usuario("E003", passwordEncriptado, "María Rodríguez", Rol.ESTUDIANTE);
        Usuario estudiante4 = new Usuario("E004", passwordEncriptado, "Pedro Martínez", Rol.ESTUDIANTE);
        
        logger.debug("Creando usuario docente: {}", docente);
        usuarios.put(docente.getDocumento(), docente);
        usuarios.put(estudiante1.getDocumento(), estudiante1);
        usuarios.put(estudiante2.getDocumento(), estudiante2);
        usuarios.put(estudiante3.getDocumento(), estudiante3);
        usuarios.put(estudiante4.getDocumento(), estudiante4);

        // Matemáticas para diferentes grados
        Materia matematicas9A = new Materia("MAT9A", "Matemáticas 9A", "9A", docente.getDocumento());
        Materia matematicas9B = new Materia("MAT9B", "Matemáticas 9B", "9B", docente.getDocumento());
        Materia matematicas10A = new Materia("MAT10A", "Matemáticas 10A", "10A", docente.getDocumento());
        Materia matematicas10B = new Materia("MAT10B", "Matemáticas 10B", "10B", docente.getDocumento());

        materias.put(matematicas9A.getId(), matematicas9A);
        materias.put(matematicas9B.getId(), matematicas9B);
        materias.put(matematicas10A.getId(), matematicas10A);
        materias.put(matematicas10B.getId(), matematicas10B);

        // Agregar notas para diferentes materias
        agregarNota(estudiante1.getDocumento(), matematicas9A.getId(), 4.5);
        agregarNota(estudiante2.getDocumento(), matematicas9A.getId(), 3.8);
        agregarNota(estudiante3.getDocumento(), matematicas9B.getId(), 4.2);
        agregarNota(estudiante4.getDocumento(), matematicas9B.getId(), 3.9);
        agregarNota(estudiante1.getDocumento(), matematicas10A.getId(), 4.0);
        agregarNota(estudiante2.getDocumento(), matematicas10A.getId(), 4.3);
    }

    private void agregarNota(String estudianteId, String materiaId, double calificacion) {
        Nota nota = new Nota(estudianteId, materiaId, calificacion);
        notas.computeIfAbsent(materiaId, k -> new ArrayList<>()).add(nota);
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
        return Flux.fromIterable(notas.values())
            .flatMap(Flux::fromIterable)
            .filter(n -> n.getEstudianteId().equals(estudianteId));
    }

    public Mono<Void> calificarEstudiante(String materiaId, String estudianteId, Double calificacion) {
        logger.debug("Calificando estudiante {} en materia {} con nota {}", estudianteId, materiaId, calificacion);
        
        List<Nota> notasMateria = notas.get(materiaId);
        if (notasMateria != null) {
            // Buscar si ya existe una nota para este estudiante
            Optional<Nota> notaExistente = notasMateria.stream()
                .filter(n -> n.getEstudianteId().equals(estudianteId))
                .findFirst();
            
            if (notaExistente.isPresent()) {
                // Actualizar la nota existente
                logger.debug("Actualizando nota existente de {} a {}", notaExistente.get().getCalificacion(), calificacion);
                notasMateria.remove(notaExistente.get());
                notasMateria.add(new Nota(estudianteId, materiaId, calificacion));
            } else {
                // Agregar nueva nota
                logger.debug("Agregando nueva nota para el estudiante");
                notasMateria.add(new Nota(estudianteId, materiaId, calificacion));
            }
        } else {
            // Si no existe la lista de notas para esta materia, crearla
            logger.debug("Creando nueva lista de notas para la materia");
            List<Nota> nuevasNotas = new ArrayList<>();
            nuevasNotas.add(new Nota(estudianteId, materiaId, calificacion));
            notas.put(materiaId, nuevasNotas);
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
            .filter(u -> u.getRol() == Rol.ESTUDIANTE)
            .filterWhen(usuario -> 
                obtenerMateriasEstudiante(usuario.getDocumento())
                    .map(Materia::getGrado)
                    .any(g -> g.equals(grado))
            );
    }

    private Flux<Materia> obtenerMateriasEstudiante(String estudianteId) {
        return Flux.fromIterable(notas.values())
            .flatMap(Flux::fromIterable)
            .filter(n -> n.getEstudianteId().equals(estudianteId))
            .map(n -> materias.get(n.getMateriaId()));
    }
}
