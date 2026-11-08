package com.senati.biblioteca.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Validador personalizado #2: valida el dígito verificador de un ISBN-10 o ISBN-13.
 * Se registra en faces-config mediante la anotación {@link FacesValidator}.
 */
@FacesValidator("isbnValidator")
public class IsbnValidator implements Validator<String> {

    @Override
    public void validate(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) return;
        String isbn = value.replaceAll("[\\s-]", "").toUpperCase();

        if (isbn.length() != 10 && isbn.length() != 13) {
            throw error("El ISBN debe tener 10 o 13 caracteres (sin contar guiones).");
        }
        boolean valido = isbn.length() == 10 ? validarIsbn10(isbn) : validarIsbn13(isbn);
        if (!valido) {
            throw error("El ISBN " + value + " no es válido (dígito verificador incorrecto).");
        }
    }

    /** ISBN-10: suma ponderada 10..1, el último puede ser X (=10). */
    static boolean validarIsbn10(String isbn) {
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            char c = isbn.charAt(i);
            int digito;
            if (i == 9 && c == 'X') digito = 10;
            else if (Character.isDigit(c)) digito = c - '0';
            else return false;
            suma += (10 - i) * digito;
        }
        return suma % 11 == 0;
    }

    /** ISBN-13: pesos alternos 1 y 3; la suma debe ser múltiplo de 10. */
    static boolean validarIsbn13(String isbn) {
        int suma = 0;
        for (int i = 0; i < 13; i++) {
            if (!Character.isDigit(isbn.charAt(i))) return false;
            int digito = isbn.charAt(i) - '0';
            suma += (i % 2 == 0) ? digito : digito * 3;
        }
        return suma % 10 == 0;
    }

    private ValidatorException error(String mensaje) {
        return new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "ISBN inválido", mensaje));
    }
}
