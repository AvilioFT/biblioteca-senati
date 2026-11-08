package com.senati.biblioteca.rest;

import com.senati.biblioteca.dto.LibroDTO;
import com.senati.biblioteca.model.Libro;
import com.senati.biblioteca.service.LibroService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio JAX-RS de consulta de libros y disponibilidad.
 * Base: /api/libros
 */
@Path("/libros")
@Produces(MediaType.APPLICATION_JSON)
public class LibroRestService {

    private LibroService service() {
        return CDI.current().select(LibroService.class).get();
    }

    /** GET /api/libros  -> catálogo completo. */
    @GET
    public List<LibroDTO> listar() {
        return toDto(service().listar());
    }

    /** GET /api/libros/buscar?q=texto  -> búsqueda por título, autor o ISBN. */
    @GET
    @Path("/buscar")
    public List<LibroDTO> buscar(@QueryParam("q") String texto) {
        return toDto(service().buscar(texto));
    }

    /** GET /api/libros/disponibles  -> solo libros con stock. */
    @GET
    @Path("/disponibles")
    public List<LibroDTO> disponibles() {
        return toDto(service().listarDisponibles());
    }

    /** GET /api/libros/{id}  -> detalle de un libro. */
    @GET
    @Path("/{id}")
    public Response obtener(@PathParam("id") Long id) {
        Libro libro = service().buscarPorId(id);
        if (libro == null) return Response.status(Response.Status.NOT_FOUND)
                .entity(new Error("Libro no encontrado")).build();
        return Response.ok(toDto(libro)).build();
    }

    /** GET /api/libros/{id}/disponibilidad  -> stock disponible para préstamo. */
    @GET
    @Path("/{id}/disponibilidad")
    public Response disponibilidad(@PathParam("id") Long id) {
        Libro libro = service().buscarPorId(id);
        if (libro == null) return Response.status(Response.Status.NOT_FOUND)
                .entity(new Error("Libro no encontrado")).build();
        int stock = libro.getCantidadEjemplares() == null ? 0 : libro.getCantidadEjemplares();
        return Response.ok(new Disponibilidad(libro.getId(), libro.getTitulo(), libro.getIsbn(),
                stock, stock > 0)).build();
    }

    private List<LibroDTO> toDto(List<Libro> libros) {
        List<LibroDTO> out = new ArrayList<>(libros.size());
        for (Libro l : libros) out.add(toDto(l));
        return out;
    }

    private LibroDTO toDto(Libro l) {
        LibroDTO d = new LibroDTO();
        d.setId(l.getId());
        d.setIsbn(l.getIsbn());
        d.setTitulo(l.getTitulo());
        d.setAutor(l.getAutor());
        int stock = l.getCantidadEjemplares() == null ? 0 : l.getCantidadEjemplares();
        d.setCantidadEjemplaresTotal(stock);
        d.setCantidadEjemplaresDisponibles(stock);
        d.setPrestado(stock <= 0);
        return d;
    }

    /** Respuesta de disponibilidad. */
    public record Disponibilidad(Long id, String titulo, String isbn,
                                 int ejemplaresDisponibles, boolean disponible) {}

    /** Error simple para la API. */
    public record Error(String mensaje) {}
}
