Trabajo Final Integrador – Programación 2
Aplicación Java con relación 1→1 unidireccional, DAO, Service, MySQL y transacciones.

DESCRIPCIÓN DEL PROYECTO
Este proyecto implementa una aplicación Java que gestiona Productos y sus Códigos de Barras con una relación 1→1 unidireccional (Producto → CódigoBarras).
Incluye:
- Persistencia con JDBC (sin ORM).
- Patrón DAO y capa Service.
- Transacciones con commit y rollback.
- CRUD completo con baja lógica.
- Pruebas en consola (AppTest.java).

REQUISITOS
- Java: JDK 21 (o superior).
- MySQL: versión 8.x.
- Conector JDBC: mysql-connector-j-8.0.33.jar.

ESTRUCTURA DEL PROYECTO
src/
 ├─ config/           → Clase DatabaseConnection
 ├─ dao/              → DAOs (ProductoDaoImpl, CodigoBarrasDaoImpl)
 ├─ entities/         → Entidades (Producto, CodigoBarras, TipoCodigo)
 ├─ service/          → ProductoService (transacciones)
 ├─ main/             → AppTest (pruebas CRUD y transacciones)

CONFIGURACIÓN DE LA BASE DE DATOS
-- =========================================
-- Base de datos
-- =========================================
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET AUTOCOMMIT = 0;
START TRANSACTION;

CREATE DATABASE IF NOT EXISTS tfi_bd CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE tfi_bd;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS codigo_barras;
DROP TABLE IF EXISTS producto;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE producto (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    marca VARCHAR(80),
    categoria VARCHAR(80),
    precio DECIMAL(10,2) NOT NULL,
    peso DECIMAL(10,3),
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_precio CHECK (precio >= 0),
    CONSTRAINT chk_peso CHECK (peso IS NULL OR peso >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE codigo_barras (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    producto_id BIGINT NOT NULL,
    tipo ENUM('EAN13','EAN8','UPC') NOT NULL,
    valor VARCHAR(20) NOT NULL,
    fecha_asignacion DATE,
    observaciones VARCHAR(255),
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_cb_producto UNIQUE (producto_id),
    CONSTRAINT uq_cb_valor UNIQUE (valor),
    CONSTRAINT fk_cb_producto FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SEED INICIAL
INSERT INTO producto (nombre, marca, categoria, precio, peso) VALUES
('Leche entera 1L', 'La Serenísima', 'Lácteos', 1450.00, 1.000);
INSERT INTO codigo_barras (producto_id, tipo, valor, fecha_asignacion) VALUES
(LAST_INSERT_ID(), 'EAN13', '7791234567890', CURRENT_DATE);

-- CATÁLOGOS BASE
INSERT INTO producto (eliminado, nombre, marca, categoria, precio, peso) VALUES
(FALSE,'Lápiz HB','Faber','Útiles',150.00, 0.006),
(FALSE,'Cuaderno A4','Rivadavia','Papelería',2500.00, 0.420),
(FALSE,'Yerba 1kg','Taragüi','Alimentos',4600.00, 1.000),
(FALSE,'Aceite 900ml','Cocinero','Alimentos',3800.00, 0.900),
(FALSE,'Shampoo 400ml','Pantene','Higiene',5200.00, 0.400),
(FALSE,'Cepillo Dental','Colgate','Higiene',1900.00, 0.050),
(FALSE,'Mouse Óptico','Logi','Electrónica',8900.00, 0.090),
(FALSE,'Auriculares In-Ear','Philips','Electrónica',12900.00, 0.070),
(FALSE,'Agua 2.25L','Villavicencio','Bebidas',1800.00, 2.250),
(FALSE,'Galletitas 3x118g','Oreo','Alimentos',3100.00, 0.354);

INSERT INTO codigo_barras (producto_id, tipo, valor, fecha_asignacion, observaciones, eliminado)
SELECT p.id, CASE WHEN p.id % 3 = 0 THEN 'UPC' WHEN p.id % 3 = 1 THEN 'EAN13' ELSE 'EAN8' END,
LPAD(CONCAT(p.id, '987654321'), 13, '0'), CURDATE(), 'Carga base', FALSE
FROM producto p LEFT JOIN codigo_barras c ON c.producto_id = p.id WHERE c.id IS NULL;

-- CARGA MASIVA OPTIMIZADA
INSERT INTO producto (eliminado, nombre, marca, categoria, precio, peso)
SELECT FALSE, CONCAT('Producto ', n+1), CONCAT('Marca', ((n+1) % 200) + 1),
ELT(((n+1) % 6) + 1, 'Alimentos','Higiene','Electrónica','Bebidas','Papelería','Hogar'),
ROUND((((n+1) % 10000) + 100) * 1.0, 2), ROUND((((n+1) % 5000) * 0.001), 3)
FROM (SELECT a.n + 10*b.n + 100*c.n + 1000*d.n + 10000*e.n + 100000*f.n AS n
FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) e
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) f) seq
LIMIT 10;

INSERT INTO codigo_barras (producto_id, tipo, valor, fecha_asignacion, observaciones, eliminado)
SELECT p.id, 'EAN13', LPAD(CAST(p.id * 1000000 + MOD(p.id, 99999) AS CHAR), 13, '0'), CURDATE(), 'Asignado post-carga', FALSE
FROM producto p LEFT JOIN codigo_barras c ON c.producto_id = p.id WHERE c.id IS NULL;

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;
SET AUTOCOMMIT = 1;

Archivo db.properties:
db.url=jdbc:mysql://localhost:3306/tfi_bd
db.user=tu_usuario
db.password=tu_password

CÓMO COMPILAR Y EJECUTAR
Compilar:
javac -cp .;mysql-connector-j-8.0.33.jar com/mycompany/tpi_programacion2/**/*.java

Ejecutar:
java -cp .;mysql-connector-j-8.0.33.jar com.mycompany.tpi_programacion2.main.AppTest

FLUJO PROBADO EN AppTest.java
- CRUD básico con DAOs.
- Transacciones con ProductoService:
  - Crear Producto + Código.
  - Actualizar ambos.
  - Eliminar ambos (baja lógica).

ARQUITECTURA
- config: conexión a BD.
- entities: clases de dominio.
- dao: acceso a datos con PreparedStatement.
- service: lógica de negocio y transacciones.
- main: pruebas y menú.

VIDEO DEMOSTRACIÓN 
https://youtu.be/PsrlRXzB0O8
