package com.senati.biblioteca.rest;

import com.senati.biblioteca.dto.PrestamoDTO;
import com.senati.biblioteca.service.PrestamoService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Servicio JAX-RS de consulta de préstamos.
 * Base: /api/prestamos
 */
@Path("/prestamos")
@Produces(MediaType.APPLICATION_JSON)
public class PrestamoRestService {

    private PrestamoService service() {
        return CDI.current().select(PrestamoService.class).get();
    }

    /** GET /api/prestamos  -> todos los préstamos. */
    @GET
    public List<PrestamoDTO> listar() {
        return service().listarTodos();
    }

    /** GET /api/prestamos/activos  -> préstamos sin devolver. */
    @GET
    @Path("/activos")
    public List<PrestamoDTO> activos() {
        return service().listarTodos().stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .toList();
    }

    /** GET /api/prestamos/mora  -> préstamos vencidos sin devolver. */
    @GET
    @Path("/mora")
    public List<PrestamoDTO> mora() {
        return service().listarTodos().stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .filter(p -> Boolean.TRUE.equals(p.getVencido()))
                .toList();
    }
}
