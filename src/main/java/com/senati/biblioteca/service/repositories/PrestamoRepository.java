package com.senati.biblioteca.service.repositories;

import com.senati.biblioteca.dto.PrestamoDTO;
import com.senati.biblioteca.model.Prestamo;
import com.senati.biblioteca.model.Usuario;
import com.senati.biblioteca.model.Libro;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Repositorio CRUD + consultas JPQL de {@link Prestamo}. */
public class PrestamoRepository {

    public Prestamo findById(EntityManager em, Long id) {
        return em.find(Prestamo.class, id);
    }

    /** Número de préstamos activos de un usuario. */
    public Long countActivos(EntityManager em, Long userId) {
        String q = "SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.id = :uid AND p.estado = 'ACTIVO'";
        try {
            return (Long) em.createQuery(q, Long.class).setParameter("uid", userId).getSingleResult();
        } catch (NoResultException e) { return 0L; }
    }

    /** Préstamos de un usuario, ordenados por fecha. */
    public List<Prestamo> findByUsuarioId(EntityManager em, Long userId) {
        return em.createQuery("SELECT p FROM Prestamo p WHERE p.usuario.id = :uid ORDER BY p.fechaPrestamo DESC",
                Prestamo.class).setParameter("uid", userId).getResultList();
    }

    /** Todas los préstamos. */
    public List<Prestamo> findAll(EntityManager em) {
        return em.createQuery("SELECT p FROM Prestamo p ORDER BY p.fechaPrestamo DESC", Prestamo.class)
                .getResultList();
    }

    /** Actualiza el stock de un libro. */
    public void updateStock(EntityManager em, Libro libro) {
        em.merge(libro);
    }

    /** DTO con valoración de préstamos vencidos. */
    public List<PrestamoDTO> toDto(EntityManager em, List<Prestamo> list) {
        List<PrestamoDTO> out = new ArrayList<>(list.size());
        for (Prestamo p : list) out.add(toDto(em, p));
        return out;
    }

    private PrestamoDTO toDto(EntityManager em, Prestamo p) {
        PrestamoDTO d = new PrestamoDTO();
        d.setId(p.getId());
        Usuario u = p.getUsuario();
        if (u != null) {
            com.senati.biblioteca.dto.UsuarioDTO ud = new com.senati.biblioteca.dto.UsuarioDTO();
            ud.setId(u.getId()); ud.setCodigoUtu(u.getCodigoUtu());
            ud.setNombre(u.getNombre()); ud.setCorreo(u.getCorreo());
            d.setUsuario(ud);
        }
        Libro l = p.getLibro();
        if (l != null) {
            com.senati.biblioteca.dto.LibroDTO ld = new com.senati.biblioteca.dto.LibroDTO();
            ld.setId(l.getId()); ld.setIsbn(l.getIsbn()); ld.setTitulo(l.getTitulo());
            ld.setAutor(l.getAutor());
            int disp = l.getCantidadEjemplares() != null ? l.getCantidadEjemplares() : 0;
            ld.setCantidadEjemplaresTotal(disp);
            ld.setCantidadEjemplaresDisponibles(disp);
            ld.setPrestado(disp <= 0);
            d.setLibro(ld);
        }
        d.setFechaPrestamo(p.getFechaPrestamo());
        d.setFechaDevolucionEstimada(p.getFechaDevolucionEstimada());
        d.setFechaDevolucionReal(p.getFechaDevolucionReal());
        d.setEstado(p.getEstado());
        d.setCategoria(p.getEstado());
        d.setVencido(p.isVencido());
        return d;
    }

    /** Consulta JPQL: libros más solicitados en los últimos 30 días. */
    public List<Object[]> getTopLibros30Dias(EntityManager em, int limit) {
        String jpql = """
                SELECT l.titulo, COUNT(p)
                FROM Prestamo p JOIN p.libro l
                WHERE p.fechaPrestamo >= :desde
                GROUP BY l.titulo
                ORDER BY COUNT(p) DESC
                """;
        return em.createQuery(jpql, Object[].class)
                .setParameter("desde", LocalDate.now().minusMonths(1))
                .setMaxResults(limit)
                .getResultList();
    }

    /** Consulta JPQL: préstamos vencidos con estudiante y libro. */
    public List<Object[]> getUsuariosConMora(EntityManager em, int limit) {
        String jpql = """
                SELECT u.nombre, u.codigoUtu, u.correo, l.titulo, p.id, p.fechaDevolucionEstimada
                FROM Prestamo p JOIN p.usuario u JOIN p.libro l
                WHERE p.estado = 'ACTIVO' AND p.fechaDevolucionEstimada < :hoy
                ORDER BY p.fechaDevolucionEstimada ASC
                """;
        return em.createQuery(jpql, Object[].class)
                .setParameter("hoy", LocalDate.now())
                .setMaxResults(limit)
                .getResultList();
    }
}
