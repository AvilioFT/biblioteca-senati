package com.senati.biblioteca.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Configura el ObjectMapper de Jackson para la API REST.
 * Registra los módulos encontrados (JSR-310) y escribe las fechas como
 * texto ISO (2026-11-08) en lugar de arreglos numéricos.
 */
@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
