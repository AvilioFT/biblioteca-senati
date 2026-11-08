package com.senati.biblioteca.web;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro de autenticación/autorización para las páginas privadas.
 * - Sin sesión -> login.xhtml
 * - Estudiantes en páginas de bibliotecario -> mi-cuenta.xhtml
 */
public class AuthFilter implements Filter {

    private static final Set<String> RUTAS_BIBLIOTECARIO = Set.of(
            "/app/dashboard.xhtml",
            "/app/libros.xhtml",
            "/app/libro-form.xhtml",
            "/app/usuarios.xhtml",
            "/app/usuario-form.xhtml",
            "/app/prestamos.xhtml",
            "/app/prestamo-form.xhtml",
            "/app/reportes.xhtml"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());
        HttpSession session = req.getSession(false);
        UsuarioSesion usuario = session == null ? null
                : (UsuarioSesion) session.getAttribute(UsuarioSesion.KEY);

        if (usuario == null) {
            res.sendRedirect(ctx + "/login.xhtml");
            return;
        }

        boolean zonaBibliotecario = RUTAS_BIBLIOTECARIO.contains(path)
                || path.startsWith("/reportes/");
        if (zonaBibliotecario && !usuario.isBibliotecario()) {
            res.sendRedirect(ctx + "/app/mi-cuenta.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }
}
