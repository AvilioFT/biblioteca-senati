package com.senati.biblioteca.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA: Libro.
 * Relación: un libro tiene muchos préstamos (@OneToMany).
 */
@Entity
@Table(name = "libros")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Libro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String autor;

    /** Ejemplares disponibles para préstamo. */
    @Column(name = "cantidad_ejemplares", nullable = false)
    private Integer cantidadEjemplares;

    @OneToMany(mappedBy = "libro", fetch = FetchType.LAZY)
    private List<Prestamo> prestamos = new ArrayList<>();

    public Libro() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public Integer getCantidadEjemplares() { return cantidadEjemplares; }
    public void setCantidadEjemplares(Integer cantidadEjemplares) { this.cantidadEjemplares = cantidadEjemplares; }
    public List<Prestamo> getPrestamos() { return prestamos; }
    public void setPrestamos(List<Prestamo> prestamos) { this.prestamos = prestamos; }

    @Override
    public String toString() { return titulo + " (" + isbn + ")"; }
}
