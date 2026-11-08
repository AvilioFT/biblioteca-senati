-- ============================================================
--  Biblioteca SENATI · esquema de base de datos (MariaDB/MySQL)
--  Ejecutar con: mysql -u root < db/schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS db_biblioteca
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE db_biblioteca;

-- ------------------------------------------------------------
-- Libros: un libro tiene muchos préstamos (@OneToMany)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS libros (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    isbn                VARCHAR(20)  NOT NULL,
    titulo              VARCHAR(150) NOT NULL,
    autor               VARCHAR(100) NOT NULL,
    cantidad_ejemplares INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_libros_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Usuarios: un usuario tiene muchos préstamos (@OneToMany)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    codigo_utu VARCHAR(20)  NOT NULL,
    nombre     VARCHAR(100) NOT NULL,
    correo     VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_codigo (codigo_utu),
    UNIQUE KEY uk_usuarios_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Préstamos: muchos préstamos pertenecen a un usuario y a un libro
-- (@ManyToOne). estado = ACTIVO | DEVUELTO
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prestamos (
    id                        BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id                BIGINT      NOT NULL,
    libro_id                  BIGINT      NOT NULL,
    fecha_prestamo            DATE        NOT NULL,
    fecha_devolucion_estimada DATE        NOT NULL,
    fecha_devolucion_real     DATE        NULL,
    estado                    VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id),
    KEY ix_prestamos_usuario (usuario_id),
    KEY ix_prestamos_libro (libro_id),
    KEY ix_prestamos_estado (estado),
    CONSTRAINT fk_prestamos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_prestamos_libro   FOREIGN KEY (libro_id)   REFERENCES libros (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
