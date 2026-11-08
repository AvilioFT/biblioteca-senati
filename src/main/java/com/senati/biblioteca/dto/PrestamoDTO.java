package com.senati.biblioteca.dto;

import java.io.Serializable;
import java.time.LocalDate;

/** Datos de un préstamo. */
public class PrestamoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private UsuarioDTO usuario;
    private LibroDTO libro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionEstimada;
    private LocalDate fechaDevolucionReal;
    private String estado;
    private String categoria;
    private String observacion;
    private Boolean vencido;

    public PrestamoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsuarioDTO getUsuario() { return usuario; }
    public void setUsuario(UsuarioDTO u) { this.usuario = u; }
    public LibroDTO getLibro() { return libro; }
    public void setLibro(LibroDTO l) { this.libro = l; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate d) { this.fechaPrestamo = d; }
    public LocalDate getFechaDevolucionEstimada() { return fechaDevolucionEstimada; }
    public void setFechaDevolucionEstimada(LocalDate d) { this.fechaDevolucionEstimada = d; }
    public LocalDate getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDate d) { this.fechaDevolucionReal = d; }
    public String getEstado() { return estado; }
    public void setEstado(String s) { this.estado = s; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String s) { this.categoria = s; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String s) { this.observacion = s; }
    public Boolean getVencido() { return vencido; }
    public void setVencido(Boolean b) { this.vencido = b; }
}
