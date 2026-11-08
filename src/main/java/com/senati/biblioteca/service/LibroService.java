package com.senati.biblioteca.service;

import com.senati.biblioteca.model.Libro;
import com.senati.biblioteca.service.repositories.LibroRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.Serializable;
import java.util.List;

/** CRUD y consultas de libros con transacciones RESOURCE_LOCAL. */
@ApplicationScoped
public class LibroService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private LibroRepository repo;
    @Inject private EntityManagerFactoryProvider emfProvider;

    public List<Libro> listar() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try { return repo.findAll(em); } finally { em.close(); }
    }

    /** Libros con stock disponible; usado en el combo de préstamos. */
    public List<Libro> listarDisponibles() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT l FROM Libro l WHERE l.cantidadEjemplares > 0 ORDER BY l.titulo", Libro.class)
                    .getResultList();
        } finally { em.close(); }
    }

    public List<Libro> buscar(String texto) {
        if (texto == null || texto.isBlank()) return listar();
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            String q = "%" + texto.trim().toLowerCase() + "%";
            return em.createQuery(
                    "SELECT l FROM Libro l WHERE lower(l.titulo) LIKE :q OR lower(l.autor) LIKE :q OR l.isbn LIKE :q ORDER BY l.titulo",
                    Libro.class).setParameter("q", q).getResultList();
        } finally { em.close(); }
    }

    public Libro buscarPorId(Long id) {
        if (id == null) return null;
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try { return repo.findById(em, id); } finally { em.close(); }
    }

    public long contar() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(l) FROM Libro l", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public void guardar(Libro libro) {
        if (libro == null) throw new BusinessException("Datos del libro inválidos.");
        validar(libro);
        EntityManager em = emfProvider.getEmf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (libro.getId() == null) {
                em.persist(libro);
            } else {
                em.merge(libro);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new BusinessException("No se pudo guardar el libro: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    private void validar(Libro libro) {
        if (libro.getTitulo() == null || libro.getTitulo().isBlank())
            throw new BusinessException("El título es obligatorio.");
        if (libro.getAutor() == null || libro.getAutor().isBlank())
            throw new BusinessException("El autor es obligatorio.");
        if (libro.getIsbn() == null || libro.getIsbn().isBlank())
            throw new BusinessException("El ISBN es obligatorio.");
        if (libro.getCantidadEjemplares() == null || libro.getCantidadEjemplares() < 0)
            throw new BusinessException("La cantidad de ejemplares no puede ser negativa.");

        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            Long duplicados;
            if (libro.getId() == null) {
                duplicados = em.createQuery(
                        "SELECT COUNT(l) FROM Libro l WHERE l.isbn = :isbn", Long.class)
                        .setParameter("isbn", libro.getIsbn().trim())
                        .getSingleResult();
            } else {
                duplicados = em.createQuery(
                        "SELECT COUNT(l) FROM Libro l WHERE l.isbn = :isbn AND l.id <> :id", Long.class)
                        .setParameter("isbn", libro.getIsbn().trim())
                        .setParameter("id", libro.getId())
                        .getSingleResult();
            }
            if (duplicados != null && duplicados > 0)
                throw new BusinessException("Ya existe un libro registrado con el ISBN " + libro.getIsbn());
        } finally { em.close(); }
    }

    public void eliminar(Long id) {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Libro libro = em.find(Libro.class, id);
            if (libro == null) throw new BusinessException("El libro no existe.");
            Long prestamos = em.createQuery(
                    "SELECT COUNT(p) FROM Prestamo p WHERE p.libro.id = :id", Long.class)
                    .setParameter("id", id).getSingleResult();
            if (prestamos != null && prestamos > 0)
                throw new BusinessException("No se puede eliminar: el libro tiene préstamos registrados.");
            em.remove(libro);
            tx.commit();
        } catch (BusinessException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new BusinessException("No se pudo eliminar el libro.");
        } finally { em.close(); }
    }
}
