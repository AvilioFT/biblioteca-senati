package com.senati.biblioteca.service.repositories;

import com.senati.biblioteca.model.Usuario;
import jakarta.persistence.*;
import java.util.List;

/** Repositorio CRUD + consultas de {@link Usuario}. */
public class UsuarioRepository {

    public Usuario findById(EntityManager em, Long id) {
        try { return em.find(Usuario.class, id); }
        catch (NoResultException e) { return null; }
    }

    /** Busca por código UTU. */
    public TypedQuery<Usuario> findByCodigoUtu(EntityManager em, String codigoUtu) {
        return em.createQuery("SELECT u FROM Usuario u WHERE u.codigoUtu = :cu", Usuario.class)
                .setParameter("cu", codigoUtu);
    }

    /** Todos los usuarios. */
    public List<Usuario> findAll(EntityManager em) {
        return em.createQuery("SELECT u FROM Usuario u ORDER BY u.nombre", Usuario.class)
                .getResultList();
    }

    /** Convertido a DTO. */
    public List<com.senati.biblioteca.dto.UsuarioDTO> findAllDto(EntityManager em) {
        List<com.senati.biblioteca.dto.UsuarioDTO> out = new java.util.ArrayList<>(20);
        for (Usuario u : findAll(em)) out.add(toDto(u));
        return out;
    }

    private com.senati.biblioteca.dto.UsuarioDTO toDto(Usuario u) {
        com.senati.biblioteca.dto.UsuarioDTO d = new com.senati.biblioteca.dto.UsuarioDTO();
        d.setId(u.getId()); d.setCodigoUtu(u.getCodigoUtu());
        d.setNombre(u.getNombre()); d.setCorreo(u.getCorreo());
        return d;
    }
}
