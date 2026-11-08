package com.senati.biblioteca.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entidad JPA: Préstamo.
 * Relaciones: muchos préstamos pertenecen a un usuario y a un libro (@ManyToOne).
 */
@Entity
@Table(name = "prestamos")
public class Prestamo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_DEVUELTO = "DEVUELTO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @Column(name = "fecha_devolucion_estimada", nullable = false)
    private LocalDate fechaDevolucionEstimada;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Column(nullable = false, length = 20)
    private String estado = ESTADO_ACTIVO;

    public Prestamo() {}

    @Transient
    public boolean isVencido() {
        return ESTADO_ACTIVO.equals(estado)
                && fechaDevolucionEstimada != null
                && LocalDate.now().isAfter(fechaDevolucionEstimada);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public LocalDate getFechaDevolucionEstimada() { return fechaDevolucionEstimada; }
    public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) { this.fechaDevolucionEstimada = fechaDevolucionEstimada; }
    public LocalDate getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) { this.fechaDevolucionReal = fechaDevolucionReal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
