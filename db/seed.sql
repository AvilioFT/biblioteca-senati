-- ============================================================
--  Biblioteca SENATI · datos de demostración
--  Ejecutar una sola vez: mysql -u root < db/seed.sql
--  (no borra datos; el bloque de préstamos se aplica solo la
--   primera vez gracias a seed_control)
-- ============================================================

USE db_biblioteca;

CREATE TABLE IF NOT EXISTS seed_control (
    id         INT       NOT NULL,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO seed_control (id) VALUES (1);
SET @seed = ROW_COUNT();

-- ------------------------------------------------------------
-- Catálogo (ISBN-13 con dígito verificador válido)
-- ------------------------------------------------------------
INSERT IGNORE INTO libros (isbn, titulo, autor, cantidad_ejemplares) VALUES
    ('9780132350884', 'Clean Code', 'Robert C. Martin', 2),
    ('9780134685991', 'Effective Java', 'Joshua Bloch', 1),
    ('9788420412146', 'Don Quijote de la Mancha', 'Miguel de Cervantes', 3),
    ('9786071509338', 'Cien años de soledad', 'Gabriel García Márquez', 3),
    ('9780262033848', 'Introducción a los algoritmos', 'Thomas H. Cormen', 2),
    ('9780201633610', 'Patrones de diseño', 'Erich Gamma et al.', 0),
    ('9780133970777', 'Sistemas de bases de datos', 'Ramez Elmasri', 2),
    ('9780132126953', 'Redes de computadoras', 'Andrew S. Tanenbaum', 2),
    ('9780133943030', 'Ingeniería de software', 'Ian Sommerville', 2),
    ('9780134743356', 'Java: cómo programar', 'Paul Deitel', 3);

-- ------------------------------------------------------------
-- Usuarios
-- ------------------------------------------------------------
INSERT IGNORE INTO usuarios (codigo_utu, nombre, correo) VALUES
    ('U2026001', 'Carlos Mendoza', 'carlos.mendoza@senati.pe'),
    ('U2026002', 'Ana Torres', 'ana.torres@senati.pe'),
    ('U2026003', 'Luis Rojas', 'luis.rojas@senati.pe');

-- ------------------------------------------------------------
-- Préstamos de demostración
-- @seed = 1 solo cuando seed_control se insertó por primera vez
-- ------------------------------------------------------------
INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_estimada, fecha_devolucion_real, estado)
SELECT u.id, l.id, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 6 DAY), NULL, 'ACTIVO'
FROM usuarios u, libros l
WHERE u.codigo_utu = 'U2026001' AND l.isbn = '9780132350884' AND @seed = 1;

INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_estimada, fecha_devolucion_real, estado)
SELECT u.id, l.id, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY), NULL, 'ACTIVO'
FROM usuarios u, libros l
WHERE u.codigo_utu = 'U2026002' AND l.isbn = '9788420412146' AND @seed = 1;

INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_estimada, fecha_devolucion_real, estado)
SELECT u.id, l.id, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), NULL, 'ACTIVO'
FROM usuarios u, libros l
WHERE u.codigo_utu = 'U2026001' AND l.isbn = '9780134685991' AND @seed = 1;

INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_estimada, fecha_devolucion_real, estado)
SELECT u.id, l.id, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), NULL, 'ACTIVO'
FROM usuarios u, libros l
WHERE u.codigo_utu = 'U2026002' AND l.isbn = '9780201633610' AND @seed = 1;

INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_estimada, fecha_devolucion_real, estado)
SELECT u.id, l.id, DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 26 DAY), DATE_SUB(CURDATE(), INTERVAL 27 DAY), 'DEVUELTO'
FROM usuarios u, libros l
WHERE u.codigo_utu = 'U2026002' AND l.isbn = '9780132350884' AND @seed = 1;

INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_estimada, fecha_devolucion_real, estado)
SELECT u.id, l.id, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 'DEVUELTO'
FROM usuarios u, libros l
WHERE u.codigo_utu = 'U2026001' AND l.isbn = '9780132126953' AND @seed = 1;

INSERT INTO prestamos (usuario_id, libro_id, fecha_prestamo, fecha_devolucion_estimada, fecha_devolucion_real, estado)
SELECT u.id, l.id, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 11 DAY), DATE_SUB(CURDATE(), INTERVAL 12 DAY), 'DEVUELTO'
FROM usuarios u, libros l
WHERE u.codigo_utu = 'U2026003' AND l.isbn = '9780134743356' AND @seed = 1;

-- Nota: cantidad_ejemplares representa el stock disponible para nuevos
-- préstamos. Los préstamos activos anteriores ya lo reflejan.
