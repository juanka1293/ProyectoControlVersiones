package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa un curso o grupo de estudiantes
 * Por ejemplo: "NovenoA", "DecimoB", etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {
    /** Identificador único del curso (ej: "9A", "10B") */
    private String id;
    
    /** Nombre descriptivo del curso (ej: "Noveno A", "Décimo B") */
    private String nombre;
    
    /** Grado al que pertenece el curso (ej: 9, 10, 11) */
    private int grado;
}
