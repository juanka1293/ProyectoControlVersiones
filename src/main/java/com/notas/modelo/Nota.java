package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa una calificación de un estudiante en una materia
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nota {
    /** ID del estudiante */
    private String estudianteId;
    
    /** ID de la materia */
    private String materiaId;
    
    /** Valor de la calificación (entre 0.0 y 5.0) */
    private double calificacion;
}
