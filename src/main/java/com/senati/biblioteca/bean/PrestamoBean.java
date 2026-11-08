package com.senati.biblioteca.bean;

import com.senati.biblioteca.dto.MensajeDTO;
import com.senati.biblioteca.dto.PrestamoDTO;
import com.senati.biblioteca.model.Libro;
import com.senati.biblioteca.service.LibroService;
import com.senati.biblioteca.service.PrestamoService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/**
 * Bean del ciclo de vida de los préstamos (sesión del bibliotecario):
 * registro, devolución y listado. Conserva el mensaje de la operación
 * entre la acción y la vista.
 */
@Named("prestamoBean")
@SessionScoped
public class PrestamoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PrestamoService prestamoService;
    @Inject private LibroService libroService;

    private String codigoUtu;
    private Long libroId;
    private MensajeDTO mensaje;

    private List<PrestamoDTO> prestamos;

    public String registrar() {
        mensaje = prestamoService.registrarPrestamo(codigoUtu, libroId);
        if (mensaje.isExito()) {
            libroId = null;
            prestamos = null;
        }
        return null;
    }

    public String devolver(Long idPrestamo) {
        mensaje = prestamoService.devolver(idPrestamo);
        prestamos = null;
        return null;
    }

    public void limpiarMensaje() {
        mensaje = null;
    }

    public void refrescar() {
        prestamos = null;
    }

    public List<PrestamoDTO> getPrestamos() {
        if (prestamos == null) prestamos = prestamoService.listarTodos();
        return prestamos;
    }

    /** Se consulta en cada render para reflejar altas y devoluciones recientes. */
    public List<Libro> getLibrosDisponibles() {
        return libroService.listarDisponibles();
    }

    public long getActivos() { return prestamoService.contarActivos(); }
    public long getEnMora() { return prestamoService.contarMora(); }

    public String getCodigoUtu() { return codigoUtu; }
    public void setCodigoUtu(String codigoUtu) { this.codigoUtu = codigoUtu; }
    public Long getLibroId() { return libroId; }
    public void setLibroId(Long libroId) { this.libroId = libroId; }
    public MensajeDTO getMensaje() { return mensaje; }
    public void setMensaje(MensajeDTO mensaje) { this.mensaje = mensaje; }
}
