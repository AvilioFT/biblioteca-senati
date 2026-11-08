package com.senati.biblioteca.dto;

import java.io.Serializable;

/** Mensajes del formulario de préstamos (JSF -> bean). */
public class MensajeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String texto;
    private boolean error;
    private boolean exito;

    public MensajeDTO() {}

    public MensajeDTO(String s, boolean e, boolean x) { this.texto = s; this.error = e; this.exito = x; }

    public String getTexto() { return texto; }
    public void setTexto(String s) { this.texto = s; }
    public boolean isError() { return error; }
    public void setError(boolean b) { this.error = b; }
    public boolean isExito() { return exito; }
    public void setExito(boolean b) { this.exito = b; }
}
