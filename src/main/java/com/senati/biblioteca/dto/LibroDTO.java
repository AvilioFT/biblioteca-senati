package com.senati.biblioteca.dto;

import java.io.Serializable;
import java.time.LocalDate;

/** Datos transferidos entre JSF y la API REST. */
public class LibroDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String isbn;
    private String titulo;
    private String autor;
    private Integer cantidadEjemplaresTotal;
    private Integer cantidadEjemplaresDisponibles;
    private Boolean prestado;

    public LibroDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public Integer getCantidadEjemplaresTotal() { return cantidadEjemplaresTotal; }
    public void setCantidadEjemplaresTotal(Integer c) { this.cantidadEjemplaresTotal = c; }
    public Integer getCantidadEjemplaresDisponibles() { return cantidadEjemplaresDisponibles; }
    public void setCantidadEjemplaresDisponibles(Integer c) { this.cantidadEjemplaresDisponibles = c; }
    public Boolean getPrestado() { return prestado; }
    public void setPrestado(Boolean b) { this.prestado = b; }
}
