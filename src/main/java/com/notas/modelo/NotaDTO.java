package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para transferir información de notas a las vistas
 * Incluye el nombre de la materia (con curso) o estudiante y la nota
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaDTO {
    private String materia;
    private String estudiante;
    private double nota;
}
