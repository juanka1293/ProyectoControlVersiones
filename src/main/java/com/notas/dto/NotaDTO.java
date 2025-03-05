package com.notas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para transferir información de notas a la vista
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaDTO {
    private String documento;
    private String nombreEstudiante;
    private String nombreMateria;
    private double nota;
}
