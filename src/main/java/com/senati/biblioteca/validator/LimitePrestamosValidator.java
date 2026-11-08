package com.senati.biblioteca.validator;

import com.senati.biblioteca.service.PrestamoService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Validador personalizado #1: impide préstamos si el estudiante ya tiene
 * 3 o más libros sin devolver.
 */
@FacesValidator("limitePrestamosValidator")
public class LimitePrestamosValidator implements Validator<String> {

    private static final int LIMITE = 3;

    @Override
    public void validate(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) return;
        PrestamoService service = CDI.current().select(PrestamoService.class).get();
        long activos = service.contarActivosPorCodigo(value.trim());
        if (activos >= LIMITE) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Límite de préstamos alcanzado",
                    "El estudiante ya tiene " + LIMITE
                            + " préstamos activos sin devolver. Debe devolver un libro antes de solicitar otro."));
        }
    }
}
