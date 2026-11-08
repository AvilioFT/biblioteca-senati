package com.senati.biblioteca.service.repositories;

import com.senati.biblioteca.model.Libro;
import com.senati.biblioteca.dto.LibroDTO;
import jakarta.persistence.*;

import java.util.List;

/** Repositorio CRUD + consultas JPQL de {@link Libro}. */
public class LibroRepository {

    /** Busca un libro por ID (JPQL). */
    public Libro findById(EntityManager em, Long id) {
        try { return em.find(Libro.class, id); }
        catch (NoResultException e) { return null; }
    }

    /** Busca por ISBN. */
    public TypedQuery<Libro> findByIsbn(EntityManager em, String isbn) {
        return em.createQuery("SELECT l FROM Libro l WHERE l.isbn = :isbn", Libro.class)
                .setParameter("isbn", isbn);
    }

    /** Todos los libros, ordenados por título. */
    public List<Libro> findAll(EntityManager em) {
        TypedQuery<Libro> q = em.createQuery("SELECT l FROM Libro l ORDER BY l.titulo", Libro.class);
        return q.getResultList();
    }

    /** Conversión a DTO para el frontend. */
    public List<LibroDTO> findAllDto(EntityManager em) {
        List<LibroDTO> out = new java.util.ArrayList<>(10);
        for (Libro l : findAll(em)) out.add(toDto(l));
        return out;
    }

    private LibroDTO toDto(Libro l) {
        LibroDTO d = new LibroDTO();
        d.setId(l.getId()); d.setIsbn(l.getIsbn()); d.setTitulo(l.getTitulo());
        d.setAutor(l.getAutor()); d.setCantidadEjemplaresTotal(l.getCantidadEjemplares());
        d.setCantidadEjemplaresDisponibles(l.getCantidadEjemplares()); d.setPrestado(false);
        return d;
    }
}
