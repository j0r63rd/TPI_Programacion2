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
CREATE DATABASE tpi_programacion2;
USE tfi_bd;

CREATE TABLE producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    marca VARCHAR(80),
    categoria VARCHAR(80),
    precio DECIMAL(10,2) NOT NULL,
    peso DECIMAL(10,3),
    eliminado BOOLEAN DEFAULT FALSE
);

CREATE TABLE codigo_barras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT UNIQUE,
    tipo VARCHAR(20) NOT NULL,
    valor VARCHAR(20) NOT NULL UNIQUE,
    fecha_asignacion DATE,
    observaciones VARCHAR(255),
    eliminado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
);

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
