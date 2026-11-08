package com.senati.biblioteca.bean;

import com.senati.biblioteca.dto.PrestamoDTO;
import com.senati.biblioteca.service.Constants;
import com.senati.biblioteca.service.PrestamoService;
import com.senati.biblioteca.web.UsuarioSesion;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/** Panel del estudiante: sus préstamos, vencimientos y límite disponible. */
@Named("miCuentaBean")
@RequestScoped
public class MiCuentaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private PrestamoService prestamoService;

    private UsuarioSesion sesion() {
        return (UsuarioSesion) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(UsuarioSesion.KEY);
    }

    public List<PrestamoDTO> getPrestamos() {
        UsuarioSesion s = sesion();
        return s == null ? List.of() : prestamoService.listarPorUsuario(s.getCodigo());
    }

    public long getActivos() {
        UsuarioSesion s = sesion();
        return s == null ? 0 : prestamoService.contarActivosPorCodigo(s.getCodigo());
    }

    public long getDisponibles() {
        return Math.max(Constants.MAX_PRESTAMOS_ACTIVOS - getActivos(), 0);
    }

    public String getCodigo() {
        UsuarioSesion s = sesion();
        return s == null ? "" : s.getCodigo();
    }

    public String getNombre() {
        UsuarioSesion s = sesion();
        return s == null ? "" : s.getNombre();
    }
}
