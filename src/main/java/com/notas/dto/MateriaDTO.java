package com.notas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * DTO para transferir información de materias a la vista
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MateriaDTO {
    private String id;
    private String codigo;
    private String nombre;
    private String grado;
    private List<String> estudiantes;
}
