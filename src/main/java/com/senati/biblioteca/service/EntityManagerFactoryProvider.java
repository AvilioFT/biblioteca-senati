package com.senati.biblioteca.service;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.Serializable;

/**
 * Proveedor CDI de un único EntityManagerFactory (RESOURCE_LOCAL).
 * Centraliza el EMF para compartir el caché de segundo nivel (Ehcache) entre
 * beans JSF, servicios y recursos REST.
 */
@ApplicationScoped
public class EntityManagerFactoryProvider implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PU = "BibliotecaPU";

    private volatile EntityManagerFactory emf;

    @Produces
    @ApplicationScoped
    public synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory(PU);
        }
        return emf;
    }

    public EntityManagerFactory getEmf() {
        return getEntityManagerFactory();
    }

    @PreDestroy
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
