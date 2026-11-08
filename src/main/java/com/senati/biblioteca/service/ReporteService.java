package com.senati.biblioteca.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Genera los reportes en PDF (OpenPDF) usando JPQL de la capa de servicio. */
@ApplicationScoped
public class ReporteService implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color VERDE = new Color(0x0B, 0x6E, 0x4F);
    private static final Color GRIS = new Color(0xF2, 0xF4, 0xF7);

    @Inject private PrestamoService prestamoService;

    public byte[] reporteLibrosMasPrestados() {
        List<Object[]> filas = prestamoService.topLibros(20);

        Document doc = new Document(PageSize.A4, 36, 36, 54, 42);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            encabezado(doc, "Reporte: libros más prestados",
                    "Últimos 30 días • generado el " + LocalDate.now().format(FMT));

            PdfPTable tabla = new PdfPTable(new float[]{0.8f, 4.2f, 1.2f});
            tabla.setWidthPercentage(100);
            celdaCabecera(tabla, "N°");
            celdaCabecera(tabla, "Título del libro");
            celdaCabecera(tabla, "Préstamos");

            int i = 1;
            for (Object[] fila : filas) {
                tabla.addCell(celda(String.valueOf(i++)));
                tabla.addCell(celda(String.valueOf(fila[0])));
                tabla.addCell(celda(String.valueOf(fila[1])));
            }
            if (filas.isEmpty()) {
                PdfPCell vacio = new PdfPCell(new Phrase("No hay préstamos registrados en el período.",
                        FontFactory.getFont(FontFactory.HELVETICA, BaseFont.WINANSI, 10)));
                vacio.setColspan(3);
                vacio.setPadding(12);
                tabla.addCell(vacio);
            }
            doc.add(tabla);
            pie(doc);
        } catch (Exception e) {
            throw new BusinessException("No se pudo generar el PDF: " + e.getMessage());
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    public byte[] reporteMorosidad() {
        List<Object[]> filas = prestamoService.morosidad(100);

        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 48, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            encabezado(doc, "Reporte: usuarios con mora",
                    "Préstamos vencidos al " + LocalDate.now().format(FMT));

            PdfPTable tabla = new PdfPTable(new float[]{1.1f, 2.4f, 2.8f, 1.3f, 0.9f});
            tabla.setWidthPercentage(100);
            celdaCabecera(tabla, "Código UTU");
            celdaCabecera(tabla, "Estudiante");
            celdaCabecera(tabla, "Libro");
            celdaCabecera(tabla, "Vence");
            celdaCabecera(tabla, "Días mora");

            for (Object[] fila : filas) {
                tabla.addCell(celda(String.valueOf(fila[1])));
                tabla.addCell(celda(String.valueOf(fila[0])));
                tabla.addCell(celda(String.valueOf(fila[3])));
                LocalDate vence = (LocalDate) fila[5];
                tabla.addCell(celda(vence.format(FMT)));
                long dias = java.time.temporal.ChronoUnit.DAYS.between(vence, LocalDate.now());
                tabla.addCell(celda(String.valueOf(Math.max(dias, 0))));
            }
            if (filas.isEmpty()) {
                PdfPCell vacio = new PdfPCell(new Phrase("No hay préstamos vencidos registrados.",
                        FontFactory.getFont(FontFactory.HELVETICA, BaseFont.WINANSI, 10)));
                vacio.setColspan(5);
                vacio.setPadding(12);
                tabla.addCell(vacio);
            }
            doc.add(tabla);
            pie(doc);
        } catch (Exception e) {
            throw new BusinessException("No se pudo generar el PDF: " + e.getMessage());
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private void encabezado(Document doc, String titulo, String subtitulo) throws DocumentException {
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, BaseFont.WINANSI, 18, Font.BOLD, VERDE);
        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.WINANSI, 10, Font.NORMAL, Color.DARK_GRAY);
        Paragraph p = new Paragraph("Biblioteca Universitaria SENATI", fontTitulo);
        p.setSpacingAfter(2);
        doc.add(p);
        doc.add(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD,
                BaseFont.WINANSI, 13)));
        doc.add(new Paragraph(subtitulo, fontSub));
        doc.add(new Paragraph(" "));
    }

    private void pie(Document doc) throws DocumentException {
        Paragraph p = new Paragraph("Sistema de Gestión de Biblioteca • JSF + JPA/Hibernate + CDI",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, BaseFont.WINANSI, 8, Font.ITALIC, Color.GRAY));
        p.setSpacingBefore(16);
        doc.add(p);
    }

    private void celdaCabecera(PdfPTable tabla, String texto) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, BaseFont.WINANSI, 9, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texto, f));
        cell.setBackgroundColor(VERDE);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);
    }

    private PdfPCell celda(String texto) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.WINANSI, 9);
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "" : texto, f));
        cell.setPadding(5);
        return cell;
    }
}
