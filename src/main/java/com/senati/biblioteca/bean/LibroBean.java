package com.senati.biblioteca.bean;

import com.senati.biblioteca.model.Libro;
import com.senati.biblioteca.service.BusinessException;
import com.senati.biblioteca.service.LibroService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/** Managed Bean de libros (listado, búsqueda y CRUD). */
@Named("libroBean")
@ViewScoped
public class LibroBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private LibroService libroService;

    private Long id;
    private Libro libro = new Libro();
    private String filtro;
    private List<Libro> libros;

    @PostConstruct
    public void init() {
        String param = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");
        if (param != null && !param.isBlank()) {
            try {
                id = Long.valueOf(param);
                Libro encontrado = libroService.buscarPorId(id);
                if (encontrado != null) libro = encontrado;
            } catch (NumberFormatException ignored) {
                // parámetro inválido: se muestra formulario vacío
            }
        }
        if (libro.getCantidadEjemplares() == null) libro.setCantidadEjemplares(1);
    }

    public String guardar() {
        try {
            libroService.guardar(libro);
            addInfo("Libro guardado correctamente.");
            return "/app/libros.xhtml?faces-redirect=true";
        } catch (BusinessException e) {
            addError(e.getMessage());
            return null;
        }
    }

    public String eliminar(Long idLibro) {
        try {
            libroService.eliminar(idLibro);
            addInfo("Libro eliminado.");
            libros = null;
        } catch (BusinessException e) {
            addError(e.getMessage());
        }
        return null;
    }

    public String editar(Long idLibro) {
        return "/app/libro-form.xhtml?id=" + idLibro + "&faces-redirect=true";
    }

    public String nuevo() {
        return "/app/libro-form.xhtml?faces-redirect=true";
    }

    public List<Libro> getLibros() {
        if (libros == null) libros = libroService.buscar(filtro);
        return libros;
    }

    public void buscar() {
        libros = libroService.buscar(filtro);
    }

    public void limpiar() {
        filtro = null;
        libros = null;
    }

    private void addInfo(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void addError(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
    public String getFiltro() { return filtro; }
    public void setFiltro(String filtro) { this.filtro = filtro; }
}
