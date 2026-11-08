package com.senati.biblioteca.service;

/** Constantes usadas por beans y la capa de servicio. */
public final class Constants {

    private Constants() {}

    /** Límite máximo de préstamos activos sin devolver por usuario. */
    public static final int MAX_PRESTAMOS_ACTIVOS = 3;

    /** Días de préstamo antes del vencimiento. */
    public static final int DIAS_PRESTAMO = 14;

    /** Código UTU de prueba del compte demo. */
    public static final String CODIGO_DEMO_1 = "U2026001";
    public static final String CODIGO_DEMO_2 = "U2026002";
    public static final String CODIGO_DEMO_3 = "U2026003";
}
