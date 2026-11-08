package com.senati.biblioteca.dto;

import java.io.Serializable;
import java.time.LocalDate;

/** Datos de un usuario. */
public class UsuarioDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String codigoUtu;
    private String nombre;
    private String correo;

    public UsuarioDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoUtu() { return codigoUtu; }
    public void setCodigoUtu(String s) { this.codigoUtu = s; }
    public String getNombre() { return nombre; }
    public void setNombre(String s) { this.nombre = s; }
    public String getCorreo() { return correo; }
    public void setCorreo(String s) { this.correo = s; }
}
