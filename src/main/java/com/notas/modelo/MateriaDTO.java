package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para transferir información de materias a las vistas
 * Incluye el ID y nombre de la materia (con el curso)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateriaDTO {
    private String id;
    private String nombre;
}
