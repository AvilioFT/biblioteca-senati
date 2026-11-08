package com.senati.biblioteca.bean;

import com.senati.biblioteca.model.Usuario;
import com.senati.biblioteca.service.LdapService;
import com.senati.biblioteca.service.UsuarioService;
import com.senati.biblioteca.web.UsuarioSesion;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Optional;

/**
 * Autenticación de la aplicación.
 * <ul>
 *   <li>Bibliotecario: credenciales locales (admin/admin123).</li>
 *   <li>Estudiante: autenticación centralizada real contra LDAP.</li>
 * </ul>
 */
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ADMIN_USUARIO = "admin";
    private static final String ADMIN_CLAVE = "admin123";

    @Inject private LdapService ldapService;
    @Inject private UsuarioService usuarioService;

    private String usuario;
    private String clave;
    private String error;
    private UsuarioSesion sesion;

    public String login() {
        error = null;
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            error = "Ingrese usuario y contraseña.";
            return null;
        }

        // Bibliotecario (cuenta local de la aplicación)
        if (ADMIN_USUARIO.equals(usuario.trim()) && ADMIN_CLAVE.equals(clave)) {
            sesion = new UsuarioSesion(null, ADMIN_USUARIO, "Bibliotecario", UsuarioSesion.ROL_BIBLIOTECARIO);
            guardarEnSesion();
            return "/app/dashboard.xhtml?faces-redirect=true";
        }

        // Estudiante (autenticación centralizada LDAP)
        Optional<LdapService.LdapUser> autenticado = ldapService.autenticar(usuario.trim(), clave);
        if (autenticado.isPresent() && autenticado.get().activo()) {
            LdapService.LdapUser u = autenticado.get();
            Usuario estudiante = usuarioService.buscarOCrearDesdeLdap(u.codigo(), u.nombre(), u.correo());
            sesion = new UsuarioSesion(estudiante.getId(), estudiante.getCodigoUtu(),
                    estudiante.getNombre(), UsuarioSesion.ROL_ESTUDIANTE);
            guardarEnSesion();
            return "/app/mi-cuenta.xhtml?faces-redirect=true";
        }

        error = "Credenciales inválidas o estudiante inactivo en el directorio institucional.";
        return null;
    }

    private void guardarEnSesion() {
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put(UsuarioSesion.KEY, sesion);
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isLogueado() { return sesion != null; }
    public boolean isBibliotecario() { return sesion != null && sesion.isBibliotecario(); }
    public boolean isEstudiante() { return sesion != null && sesion.isEstudiante(); }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public UsuarioSesion getSesion() { return sesion; }
    public void setSesion(UsuarioSesion sesion) { this.sesion = sesion; }
}
