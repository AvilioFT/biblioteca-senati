package com.senati.biblioteca.bean;

import com.senati.biblioteca.dto.PrestamoDTO;
import com.senati.biblioteca.service.LibroService;
import com.senati.biblioteca.service.PrestamoService;
import com.senati.biblioteca.service.UsuarioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/** Datos del panel principal. */
@Named("dashboardBean")
@RequestScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private LibroService libroService;
    @Inject private UsuarioService usuarioService;
    @Inject private PrestamoService prestamoService;

    public long getTotalLibros() { return libroService.contar(); }
    public long getTotalUsuarios() { return usuarioService.contar(); }
    public long getPrestamosActivos() { return prestamoService.contarActivos(); }
    public long getPrestamosEnMora() { return prestamoService.contarMora(); }

    public List<PrestamoDTO> getUltimosPrestamos() {
        List<PrestamoDTO> todos = prestamoService.listarTodos();
        return todos.size() > 6 ? todos.subList(0, 6) : todos;
    }
}
