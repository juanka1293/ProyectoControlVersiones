package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa un usuario del sistema (estudiante o docente)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    /** Número de documento del usuario */
    private String documento;
    
    /** Contraseña encriptada */
    private String password;
    
    /** Nombre completo del usuario */
    private String nombre;
    
    /** Rol del usuario (ESTUDIANTE o DOCENTE) */
    private Rol rol;
}
