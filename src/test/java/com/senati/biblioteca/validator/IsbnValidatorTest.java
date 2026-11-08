package com.senati.biblioteca.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del validador personalizado de ISBN.
 * Comprueba el dígito verificador de ISBN-10 e ISBN-13 sin necesitar
 * un contenedor JSF.
 */
class IsbnValidatorTest {

    @Test
    @DisplayName("Acepta ISBN-13 con dígito verificador correcto")
    void aceptaIsbn13Valido() {
        assertTrue(IsbnValidator.validarIsbn13("9780132350884"));
        assertTrue(IsbnValidator.validarIsbn13("9788420412146"));
        assertTrue(IsbnValidator.validarIsbn13("9786071509338"));
    }

    @Test
    @DisplayName("Rechaza ISBN-13 con dígito verificador incorrecto")
    void rechazaIsbn13Invalido() {
        assertFalse(IsbnValidator.validarIsbn13("9780132350885"));
        assertFalse(IsbnValidator.validarIsbn13("9788420412147"));
    }

    @Test
    @DisplayName("Rechaza ISBN-13 con caracteres no numéricos")
    void rechazaIsbn13ConLetras() {
        assertFalse(IsbnValidator.validarIsbn13("97801323508X4"));
    }

    @Test
    @DisplayName("Acepta ISBN-10 válido, incluida la X final")
    void aceptaIsbn10Valido() {
        assertTrue(IsbnValidator.validarIsbn10("0306406152"));
        assertTrue(IsbnValidator.validarIsbn10("097522980X"));
    }

    @Test
    @DisplayName("Rechaza ISBN-10 inválido")
    void rechazaIsbn10Invalido() {
        assertFalse(IsbnValidator.validarIsbn10("0306406153"));
        assertFalse(IsbnValidator.validarIsbn10("0975229801"));
    }
}
