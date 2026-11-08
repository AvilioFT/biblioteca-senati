package com.senati.biblioteca.service;

import com.senati.biblioteca.dto.MensajeDTO;
import com.senati.biblioteca.dto.PrestamoDTO;
import com.senati.biblioteca.model.Libro;
import com.senati.biblioteca.model.Prestamo;
import com.senati.biblioteca.model.Usuario;
import com.senati.biblioteca.service.repositories.PrestamoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Reglas de negocio del ciclo de vida de un préstamo:
 * validación LDAP, límite de 3 préstamos activos, stock disponible,
 * devolución y cálculo de mora.
 */
@ApplicationScoped
public class PrestamoService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private LdapService ldapService;
    @Inject private UsuarioService usuarioService;
    @Inject private PrestamoRepository repo;
    @Inject private EntityManagerFactoryProvider emfProvider;

    /**
     * Registra un préstamo aplicando todas las reglas de negocio.
     * @return mensaje con el resultado para la UI.
     */
    public MensajeDTO registrarPrestamo(String codigoUtu, Long libroId) {
        try {
            if (codigoUtu == null || codigoUtu.isBlank())
                throw new BusinessException("Debe indicar el código UTU del estudiante.");
            if (libroId == null)
                throw new BusinessException("Debe seleccionar un libro.");

            // 1) Validación centralizada contra LDAP
            LdapService.LdapUser ldapUser = ldapService.buscarUsuario(codigoUtu.trim())
                    .orElseThrow(() -> new BusinessException("El estudiante no existe en el directorio LDAP."));
            if (!ldapUser.activo())
                throw new BusinessException("El estudiante no está activo en el sistema institucional (LDAP).");

            // 2) Usuario local (se registra automáticamente si viene de LDAP)
            Usuario usuario = usuarioService.buscarPorCodigo(codigoUtu.trim())
                    .orElseGet(() -> usuarioService.buscarOCrearDesdeLdap(
                            ldapUser.codigo(), ldapUser.nombre(), ldapUser.correo()));

            EntityManager em = emfProvider.getEmf().createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                // 3) Bloqueo pesimista para evitar sobreventa del stock
                Libro libro = em.find(Libro.class, libroId, LockModeType.PESSIMISTIC_WRITE);
                if (libro == null) throw new BusinessException("El libro no existe.");

                // 4) Límite de 3 libros sin devolver por usuario
                Long activos = repo.countActivos(em, usuario.getId());
                if (activos != null && activos >= Constants.MAX_PRESTAMOS_ACTIVOS)
                    throw new BusinessException("El usuario ya tiene " + Constants.MAX_PRESTAMOS_ACTIVOS
                            + " préstamos activos sin devolver.");

                // 5) Stock disponible
                if (libro.getCantidadEjemplares() == null || libro.getCantidadEjemplares() <= 0)
                    throw new BusinessException("No hay ejemplares disponibles del libro seleccionado.");

                libro.setCantidadEjemplares(libro.getCantidadEjemplares() - 1);
                em.merge(libro);

                Prestamo prestamo = new Prestamo();
                prestamo.setUsuario(usuario);
                prestamo.setLibro(libro);
                prestamo.setFechaPrestamo(LocalDate.now());
                prestamo.setFechaDevolucionEstimada(LocalDate.now().plusDays(Constants.DIAS_PRESTAMO));
                prestamo.setEstado(Prestamo.ESTADO_ACTIVO);
                em.persist(prestamo);

                tx.commit();
                return new MensajeDTO("Préstamo registrado con éxito. ID: " + prestamo.getId(), false, true);
            } catch (BusinessException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw new BusinessException("No se pudo registrar el préstamo: " + e.getMessage());
            } finally {
                em.close();
            }
        } catch (BusinessException e) {
            return new MensajeDTO(e.getMessage(), true, false);
        }
    }

    /** Registra la devolución de un préstamo y repone el ejemplar. */
    public MensajeDTO devolver(Long prestamoId) {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Prestamo p = em.find(Prestamo.class, prestamoId, LockModeType.PESSIMISTIC_WRITE);
            if (p == null) throw new BusinessException("El préstamo no existe.");
            if (!Prestamo.ESTADO_ACTIVO.equals(p.getEstado()))
                throw new BusinessException("El préstamo ya fue devuelto.");

            Libro libro = p.getLibro();
            if (libro != null) {
                libro.setCantidadEjemplares((libro.getCantidadEjemplares() == null ? 0
                        : libro.getCantidadEjemplares()) + 1);
                em.merge(libro);
            }
            p.setEstado(Prestamo.ESTADO_DEVUELTO);
            p.setFechaDevolucionReal(LocalDate.now());
            em.merge(p);
            tx.commit();
            return new MensajeDTO("Devolución registrada correctamente.", false, true);
        } catch (BusinessException e) {
            if (tx.isActive()) tx.rollback();
            return new MensajeDTO(e.getMessage(), true, false);
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            return new MensajeDTO("No se pudo registrar la devolución: " + e.getMessage(), true, false);
        } finally {
            em.close();
        }
    }

    public List<PrestamoDTO> listarTodos() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try { return repo.toDto(em, repo.findAll(em)); } finally { em.close(); }
    }

    public List<PrestamoDTO> listarPorUsuario(String codigoUtu) {
        return usuarioService.buscarPorCodigo(codigoUtu)
                .map(u -> {
                    EntityManager em = emfProvider.getEmf().createEntityManager();
                    try { return repo.toDto(em, repo.findByUsuarioId(em, u.getId())); }
                    finally { em.close(); }
                })
                .orElseGet(List::of);
    }

    public long contarActivos() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM Prestamo p WHERE p.estado = 'ACTIVO'", Long.class)
                    .getSingleResult();
        } finally { em.close(); }
    }

    public long contarMora() {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(p) FROM Prestamo p WHERE p.estado = 'ACTIVO' AND p.fechaDevolucionEstimada < :hoy",
                    Long.class).setParameter("hoy", LocalDate.now()).getSingleResult();
        } finally { em.close(); }
    }

    public long contarActivosPorCodigo(String codigoUtu) {
        return usuarioService.buscarPorCodigo(codigoUtu)
                .map(u -> {
                    EntityManager em = emfProvider.getEmf().createEntityManager();
                    try { return repo.countActivos(em, u.getId()); }
                    finally { em.close(); }
                })
                .orElse(0L);
    }

    public List<Object[]> topLibros(int limite) {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try { return repo.getTopLibros30Dias(em, limite); } finally { em.close(); }
    }

    public List<Object[]> morosidad(int limite) {
        EntityManager em = emfProvider.getEmf().createEntityManager();
        try { return repo.getUsuariosConMora(em, limite); } finally { em.close(); }
    }
}
