package com.senati.biblioteca.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

/** Aplicación JAX-RS publicada en /api (declarada también en web.xml). */
@ApplicationPath("/api")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(LibroRestService.class, PrestamoRestService.class, JacksonConfig.class);
    }
}
