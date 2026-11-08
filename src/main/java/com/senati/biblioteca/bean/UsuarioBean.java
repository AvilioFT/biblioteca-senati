package com.senati.biblioteca.bean;

import com.senati.biblioteca.model.Usuario;
import com.senati.biblioteca.service.BusinessException;
import com.senati.biblioteca.service.UsuarioService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/** Managed Bean de usuarios (listado y CRUD). */
@Named("usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private UsuarioService usuarioService;

    private Long id;
    private Usuario usuario = new Usuario();
    private List<Usuario> usuarios;

    @PostConstruct
    public void init() {
        String param = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");
        if (param != null && !param.isBlank()) {
            try {
                id = Long.valueOf(param);
                Usuario encontrado = usuarioService.buscarPorId(id);
                if (encontrado != null) usuario = encontrado;
            } catch (NumberFormatException ignored) {
                // parámetro inválido: formulario vacío
            }
        }
    }

    public String guardar() {
        try {
            usuarioService.guardar(usuario);
            addInfo("Usuario guardado correctamente.");
            return "/app/usuarios.xhtml?faces-redirect=true";
        } catch (BusinessException e) {
            addError(e.getMessage());
            return null;
        }
    }

    public String eliminar(Long idUsuario) {
        try {
            usuarioService.eliminar(idUsuario);
            addInfo("Usuario eliminado.");
            usuarios = null;
        } catch (BusinessException e) {
            addError(e.getMessage());
        }
        return null;
    }

    public String editar(Long idUsuario) {
        return "/app/usuario-form.xhtml?id=" + idUsuario + "&faces-redirect=true";
    }

    public String nuevo() {
        return "/app/usuario-form.xhtml?faces-redirect=true";
    }

    public List<Usuario> getUsuarios() {
        if (usuarios == null) usuarios = usuarioService.listar();
        return usuarios;
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
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
