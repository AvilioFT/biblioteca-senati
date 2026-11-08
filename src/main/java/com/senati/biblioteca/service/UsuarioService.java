package com.senati.biblioteca.service;

import com.senati.biblioteca.model.Usuario;
import com.senati.biblioteca.service.repositories.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/** CRUD y consultas de usuarios. */
@ApplicationScoped
public class UsuarioService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private UsuarioRepository repo;
    @Inject private EntityManagerFactoryProvider emfProvider;
    @Inject private LdapService ldapService;

    public List<Usuario> listar() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try { return repo.findAll(em); } finally { em.close(); }
    }

    public Usuario buscarPorId(Long id) {
        if (id == null) return null;
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try { return repo.findById(em, id); } finally { em.close(); }
    }

    public Optional<Usuario> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            return Optional.of(repo.findByCodigoUtu(em, codigo.trim()).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally { em.close(); }
    }

    public long contar() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
        } finally { em.close(); }
    }

    public void guardar(Usuario usuario) {
        if (usuario == null) throw new BusinessException("Datos del usuario inválidos.");
        if (usuario.getCodigoUtu() == null || usuario.getCodigoUtu().isBlank())
            throw new BusinessException("El código UTU es obligatorio.");
        if (usuario.getNombre() == null || usuario.getNombre().isBlank())
            throw new BusinessException("El nombre es obligatorio.");
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank())
            throw new BusinessException("El correo es obligatorio.");

        EntityManager em = emfProvider.getEmf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Long dup;
            if (usuario.getId() == null) {
                dup = em.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE u.codigoUtu = :c", Long.class)
                        .setParameter("c", usuario.getCodigoUtu().trim())
                        .getSingleResult();
            } else {
                dup = em.createQuery(
                        "SELECT COUNT(u) FROM Usuario u WHERE u.codigoUtu = :c AND u.id <> :id", Long.class)
                        .setParameter("c", usuario.getCodigoUtu().trim())
                        .setParameter("id", usuario.getId())
                        .getSingleResult();
            }
            if (dup != null && dup > 0) throw new BusinessException("Ya existe un usuario con ese código UTU.");
            if (usuario.getId() == null) em.persist(usuario);
            else em.merge(usuario);
            tx.commit();
        } catch (BusinessException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new BusinessException("No se pudo guardar el usuario: " + e.getMessage());
        } finally { em.close(); }
    }

    /** Busca el usuario por código o lo crea con los datos obtenidos desde LDAP. */
    public Usuario buscarOCrearDesdeLdap(String codigo, String nombre, String correo) {
        Optional<Usuario> existente = buscarPorCodigo(codigo);
        if (existente.isPresent()) return existente.get();

        Usuario u = new Usuario();
        u.setCodigoUtu(codigo);
        u.setNombre(nombre == null || nombre.isBlank() ? codigo : nombre);
        String email = (correo == null || correo.isBlank())
                ? codigo.toLowerCase() + "@senati.pe" : correo;
        u.setCorreo(email);
        guardar(u);
        return buscarPorCodigo(codigo).orElse(u);
    }

    public void eliminar(Long id) {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Usuario u = em.find(Usuario.class, id);
            if (u == null) throw new BusinessException("El usuario no existe.");
            Long prestamos = em.createQuery(
                    "SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.id = :id", Long.class)
                    .setParameter("id", id).getSingleResult();
            if (prestamos != null && prestamos > 0)
                throw new BusinessException("No se puede eliminar: el usuario tiene préstamos registrados.");
            em.remove(u);
            tx.commit();
        } catch (BusinessException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw new BusinessException("No se pudo eliminar el usuario.");
        } finally { em.close(); }
    }
}
