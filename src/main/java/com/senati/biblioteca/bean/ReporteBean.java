package com.senati.biblioteca.bean;

import com.senati.biblioteca.service.PrestamoService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Datos de apoyo para la pantalla de reportes y el panel (vista previa en pantalla). */
@Named("reporteBean")
@RequestScoped
public class ReporteBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject private PrestamoService prestamoService;

    /** Fila de la vista previa de libros más prestados. */
    public static class TopLibro implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String titulo;
        private final long total;
        public TopLibro(String titulo, long total) { this.titulo = titulo; this.total = total; }
        public String getTitulo() { return titulo; }
        public long getTotal() { return total; }
    }

    /** Fila de la vista previa de morosidad. */
    public static class Mora implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String codigo;
        private final String estudiante;
        private final String libro;
        private final String vence;
        private final long dias;
        public Mora(String codigo, String estudiante, String libro, String vence, long dias) {
            this.codigo = codigo; this.estudiante = estudiante; this.libro = libro;
            this.vence = vence; this.dias = dias;
        }
        public String getCodigo() { return codigo; }
        public String getEstudiante() { return estudiante; }
        public String getLibro() { return libro; }
        public String getVence() { return vence; }
        public long getDias() { return dias; }
    }

    public List<TopLibro> getTopLibros() {
        List<TopLibro> out = new ArrayList<>();
        for (Object[] f : prestamoService.topLibros(10)) {
            out.add(new TopLibro(String.valueOf(f[0]), ((Number) f[1]).longValue()));
        }
        return out;
    }

    public List<Mora> getMorosos() {
        List<Mora> out = new ArrayList<>();
        for (Object[] f : prestamoService.morosidad(20)) {
            LocalDate vence = (LocalDate) f[5];
            long dias = Math.max(java.time.temporal.ChronoUnit.DAYS.between(vence, LocalDate.now()), 0);
            out.add(new Mora(String.valueOf(f[1]), String.valueOf(f[0]), String.valueOf(f[3]),
                    vence.format(FMT), dias));
        }
        return out;
    }

    /** Primeros seis morosos, para el resumen del panel. */
    public List<Mora> getMorososTop() {
        List<Mora> todos = getMorosos();
        return todos.size() > 6 ? todos.subList(0, 6) : todos;
    }

    public long getTotalPrestamosActivos() { return prestamoService.contarActivos(); }
    public long getTotalMora() { return prestamoService.contarMora(); }
    public String getFechaHoy() { return LocalDate.now().format(FMT); }
}
