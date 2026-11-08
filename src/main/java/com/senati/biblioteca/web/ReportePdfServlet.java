package com.senati.biblioteca.web;

import com.senati.biblioteca.service.ReporteService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Descarga de los reportes PDF generados con JPQL + OpenPDF. */
public class ReportePdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        ReporteService service = CDI.current().select(ReporteService.class).get();

        byte[] pdf;
        String nombre;
        if ("/libros.pdf".equals(path)) {
            pdf = service.reporteLibrosMasPrestados();
            nombre = "reporte-libros-mas-prestados.pdf";
        } else if ("/mora.pdf".equals(path)) {
            pdf = service.reporteMorosidad();
            nombre = "reporte-morosidad.pdf";
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + nombre + "\"");
        resp.setContentLength(pdf.length);
        resp.getOutputStream().write(pdf);
        resp.getOutputStream().flush();
    }
}
