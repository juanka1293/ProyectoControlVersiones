package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa una materia en el sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Materia {
    private String id;
    private String nombre;
    private String grado;
    private String docenteId;
}
