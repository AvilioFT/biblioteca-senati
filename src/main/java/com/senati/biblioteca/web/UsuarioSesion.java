package com.senati.biblioteca.web;

import java.io.Serializable;

/** Usuario autenticado guardado en la sesión HTTP (para el filtro y las vistas). */
public class UsuarioSesion implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String KEY = "usuarioSesion";
    public static final String ROL_BIBLIOTECARIO = "BIBLIOTECARIO";
    public static final String ROL_ESTUDIANTE = "ESTUDIANTE";

    private Long id;
    private String codigo;
    private String nombre;
    private String rol;

    public UsuarioSesion() {}

    public UsuarioSesion(Long id, String codigo, String nombre, String rol) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.rol = rol;
    }

    public boolean isBibliotecario() { return ROL_BIBLIOTECARIO.equals(rol); }
    public boolean isEstudiante() { return ROL_ESTUDIANTE.equals(rol); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
