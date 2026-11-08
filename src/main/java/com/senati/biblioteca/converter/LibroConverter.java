package com.senati.biblioteca.converter;

import com.senati.biblioteca.model.Libro;
import com.senati.biblioteca.service.LibroService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** Converter de entidad Libro para los combos de JSF. */
@Named("libroConverter")
@ApplicationScoped
public class LibroConverter implements Converter<Libro> {

    @Inject private LibroService libroService;

    @Override
    public Libro getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            Long id = Long.valueOf(value.trim());
            Libro libro = libroService.buscarPorId(id);
            if (libro == null) throw new ConverterException(new FacesMessage("El libro seleccionado no existe."));
            return libro;
        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage("Identificador de libro inválido."));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Libro value) {
        return value == null || value.getId() == null ? "" : value.getId().toString();
    }
}
