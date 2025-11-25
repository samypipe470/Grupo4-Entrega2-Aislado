-- Datos semilla completos para perfil "isolation" (H2 en memoria).

-- Limpieza básica
DELETE FROM reserva;
DELETE FROM prestamo;
DELETE FROM usuario;
DELETE FROM libro;
DELETE FROM configuracion;

-- Configuración de negocio
MERGE INTO configuracion (clave, valor) KEY (clave) VALUES
  ('max_prestamos_simultaneos','3'),
  ('periodo_prestamo_dias','14'),
  ('dias_renovacion','7'),
  ('dias_expiracion_reserva','2'),
  ('valor_multa_diaria','5000');

-- Usuarios: estudiante, biblio, admin, otro estudiante
MERGE INTO usuario (id_usuario, username, nombre, contrasena, id_tipo_usuario, id_estado_usuario, intentos_fallidos) KEY (id_usuario) VALUES
  (7, 'estudiante', 'Estudiante Prueba', 'pass', 1, 1, 0),
  (2, 'biblio', 'Biblio Prueba', 'pass', 2, 1, 0),
  (3, 'admin', 'Admin Prueba', 'pass', 3, 1, 0),
  (8, 'otro', 'Otro Estudiante', 'pass', 1, 1, 0);

-- Libros
INSERT INTO libro (id_libro, titulo, autor, cantidad_total, cantidad_disponible) VALUES
  (200, 'Libro Disponible', 'Autor Test', 5, 5),
  (201, 'Libro Disponible 2', 'Autor Test', 3, 3),
  (202, 'Libro Disponible 3', 'Autor Test', 2, 2),
  (96,  'Cien Anos de Soledad', 'G.G. Marquez', 5, 5),
  (99,  'Libro Agotado', 'Autor 99', 0, 0);

-- ==========================================
-- PRESTAMOS (Con nombres de columnas explícitos)
-- ==========================================

----Postman D

MERGE INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
KEY (id_prestamo)
VALUES (
    1500,           -- ID Préstamo nuevo
    7,              -- ID Usuario (Estudiante)
    99,             -- ID Libro
    CURRENT_TIMESTAMP(),
    DATEADD('DAY', 3, CURRENT_TIMESTAMP()), -- Vence en 3 días exactos
    NULL,
    2,              -- Estado ACTIVO (Requisito para ser notificado)
    0
);

----Pastman C

MERGE INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
KEY (id_prestamo)
VALUES (
    1400,           -- ID Préstamo nuevo
    7,              -- ID Usuario (Estudiante)
    200,            -- ID Libro
    DATEADD('DAY', -20, CURRENT_TIMESTAMP()),
    DATEADD('DAY', -5, CURRENT_TIMESTAMP()),  -- Venció hace 5 días -> Generará multa
    NULL,
    2,              -- Estado ACTIVO
    0
);

-- Postman B

-- 1. Crear (o asegurar) un usuario ADMIN (Tipo 3)
MERGE INTO usuario (id_usuario, username, nombre, contrasena, id_tipo_usuario, id_estado_usuario, intentos_fallidos)
KEY (id_usuario)
VALUES (99, 'admin_test', 'Administrador Jefe', '12345', 3, 1, 0);

-- 2. Crear un préstamo vencido asociado a este Admin
-- Usaremos el ID de préstamo 1300 para no mezclarlo con el anterior
MERGE INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
KEY (id_prestamo)
VALUES (
    1300,           -- ID Préstamo
    99,             -- ID Usuario (El Admin que acabamos de crear)
    200,            -- ID Libro
    DATEADD('DAY', -20, CURRENT_TIMESTAMP()), -- Prestado hace 20 días
    DATEADD('DAY', -5, CURRENT_TIMESTAMP()),  -- Venció hace 5 días
    NULL,           -- Aún no devuelto
    2,              -- Estado ACTIVO
    0               -- Multa inicial 0
);

---------- Postman A
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (1200, 7, 200, CURRENT_TIMESTAMP(), DATEADD('DAY',14,CURRENT_TIMESTAMP()), NULL, 1, 0);

UPDATE prestamo
SET fecha_devolucion_esperada = DATEADD('DAY', -5, CURRENT_TIMESTAMP),
    id_estado_prestamo = 2 -- Asegura que esté en estado ACTIVO
WHERE id_prestamo = 1200;


-- 700: SOLICITADO
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (700, 7, 200, CURRENT_TIMESTAMP(), DATEADD('DAY',14,CURRENT_TIMESTAMP()), NULL, 1, 0);

-- 701: ACTIVO (normal)
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (701, 7, 200, CURRENT_TIMESTAMP(), DATEADD('DAY',5,CURRENT_TIMESTAMP()), NULL, 2, 0);

-- 702: ACTIVO vencido para multa
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (702, 7, 201, DATEADD('DAY',-15,CURRENT_TIMESTAMP()), DATEADD('DAY',-5,CURRENT_TIMESTAMP()), NULL, 2, 0);

-- 703: DEVUELTO
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (703, 7, 201, DATEADD('DAY',-20,CURRENT_TIMESTAMP()), DATEADD('DAY',-10,CURRENT_TIMESTAMP()), DATEADD('DAY',-9,CURRENT_TIMESTAMP()), 3, 0);

-- 704: VENCIDO explícito
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (704, 7, 200, DATEADD('DAY',-25,CURRENT_TIMESTAMP()), DATEADD('DAY',-7,CURRENT_TIMESTAMP()), NULL, 4, 0);

-- 705: ACTIVO para renovar
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (705, 8, 202, CURRENT_TIMESTAMP(), DATEADD('DAY',10,CURRENT_TIMESTAMP()), NULL, 2, 0);

-- 706: ACTIVO sobre libro agotado
INSERT INTO prestamo (id_prestamo, id_usuario, id_libro, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, id_estado_prestamo, valor_multa)
VALUES (706, 7, 99, CURRENT_TIMESTAMP(), DATEADD('DAY',7,CURRENT_TIMESTAMP()), NULL, 2, 0);


-- ==========================================
-- RESERVAS (Con nombres de columnas explícitos - ESTO ARREGLA EL ERROR)
-- ==========================================

-- Cola FIFO sobre libro 99
INSERT INTO reserva (id_reserva, id_usuario, id_libro, fecha_reserva, id_estado_reserva)
VALUES (900, 7, 99, DATEADD('DAY',-1,CURRENT_TIMESTAMP()), 1);

INSERT INTO reserva (id_reserva, id_usuario, id_libro, fecha_reserva, id_estado_reserva)
VALUES (901, 2, 99, DATEADD('DAY',-2,CURRENT_TIMESTAMP()), 1);

-- Disponible (para expirar)
INSERT INTO reserva (id_reserva, id_usuario, id_libro, fecha_reserva, id_estado_reserva)
VALUES (902, 7, 99, DATEADD('DAY',-3,CURRENT_TIMESTAMP()), 2);

-- Cancelada
INSERT INTO reserva (id_reserva, id_usuario, id_libro, fecha_reserva, id_estado_reserva)
VALUES (903, 7, 200, DATEADD('DAY',-1,CURRENT_TIMESTAMP()), 3);

-- Completada
INSERT INTO reserva (id_reserva, id_usuario, id_libro, fecha_reserva, id_estado_reserva)
VALUES (904, 7, 201, DATEADD('DAY',-5,CURRENT_TIMESTAMP()), 4);

-- Pendiente extra
INSERT INTO reserva (id_reserva, id_usuario, id_libro, fecha_reserva, id_estado_reserva)
VALUES (905, 8, 200, CURRENT_TIMESTAMP(), 1);