package com.notas.modelo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un usuario del sistema (estudiante o docente)
 */
@Data
@NoArgsConstructor
public class Usuario {
    /** Número de documento del usuario */
    private String documento;
    
    /** Contraseña encriptada */
    private String password;
    
    /** Nombre completo del usuario */
    private String nombre;
    
    /** Rol del usuario (ESTUDIANTE o DOCENTE) */
    private Rol rol;
    
    /** Grado del usuario (solo para estudiantes) */
    private String grado;

    public Usuario(String documento, String password, String nombre, Rol rol) {
        this(documento, password, nombre, rol, null);
    }

    public Usuario(String documento, String password, String nombre, Rol rol, String grado) {
        this.documento = documento;
        this.password = password;
        this.nombre = nombre;
        this.rol = rol;
        this.grado = grado;
    }
}
