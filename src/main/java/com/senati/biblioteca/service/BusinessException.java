package com.senati.biblioteca.service;

/** Excepción de negocio con mensaje amigable para la UI. */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
