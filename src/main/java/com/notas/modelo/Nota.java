package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa una calificación de un estudiante en una materia
 */
@Data
@NoArgsConstructor
public class Nota {
    /** ID del estudiante */
    private String estudianteId;
    
    /** ID de la materia */
    private String materiaId;
    
    /** Nombre de la materia */
    private String nombreMateria;
    
    /** Nombre del estudiante */
    private String nombreEstudiante;
    
    /** Valor de la calificación (entre 0.0 y 5.0) */
    private double calificacion;

    public Nota(String estudianteId, String materiaId, double calificacion) {
        this.estudianteId = estudianteId;
        this.materiaId = materiaId;
        this.calificacion = calificacion;
    }
}
