package service;

import config.DatabaseConnection;
import dao.ProductoDaoImpl;
import dao.CodigoBarrasDaoImpl;
import entities.Producto;
import entities.CodigoBarras;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List; // Necesario para el método getAll()

public class ProductoService {

    private final ProductoDaoImpl productoDao;
    private final CodigoBarrasDaoImpl codigoDao;

    public ProductoService() {
        this.productoDao = new ProductoDaoImpl();
        this.codigoDao = new CodigoBarrasDaoImpl();
    }

    // ============================================================
    //    MÉTODOS DE LECTURA Y LISTADO (Añadidos para AppMenu)
    // ============================================================

    /**
     * Lee un producto por su ID (delegación directa al DAO).
     * @param id El ID del producto.
     * @return El objeto Producto.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Producto leer(long id) throws SQLException {
        return productoDao.leer(id);
    }

    /**
     * Devuelve todos los productos activos (no eliminados).
     * @return Lista de Productos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Producto> getAll() throws SQLException {
        return productoDao.leerTodos();
    }
     
    // ============================================================
    //  CREAR Producto + Código (transacción) - CÓDIGO EXISTENTE
    // ============================================================
    public void crearProductoConCodigo(Producto producto, CodigoBarras codigo) throws SQLException {
        validarProducto(producto);
        validarCodigoBasico(codigo);

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // CREAR PRODUCTO
            productoDao.crear(producto, conn);

            // Asociar FK
            codigo.setProductoId(producto.getId());

            // CREAR CÓDIGO
            codigoDao.crear(codigo, conn);

            conn.commit();
            System.out.println("✔ Transacción OK: Producto y Código creados.");

        } catch (Exception e) {
            rollback(conn, e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // ============================================================
    //  ACTUALIZAR Producto + Código (transacción) - CÓDIGO EXISTENTE
    // ============================================================
    public void actualizarProductoConCodigo(Producto producto, CodigoBarras codigo) throws SQLException {
        if (producto.getId() == null) {
            throw new SQLException("El ID del producto no puede ser nulo.");
        }
        if (codigo.getId() == null) {
            throw new SQLException("El ID del código no puede ser nulo.");
        }

        validarProducto(producto);
        validarCodigoBasico(codigo);

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            productoDao.actualizar(producto, conn);
            codigoDao.actualizar(codigo, conn);

            conn.commit();
            System.out.println("✔ Transacción OK: Producto y Código actualizados.");

        } catch (Exception e) {
            rollback(conn, e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // ============================================================
    //  BAJA lógica de Producto + Código (transacción) - CÓDIGO EXISTENTE
    // ============================================================
    public void eliminarProductoConCodigo(Long productoId, Long codigoId) throws SQLException {
        if (productoId == null) throw new SQLException("ID de producto requerido.");
        if (codigoId == null) throw new SQLException("ID de código requerido.");

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            codigoDao.eliminar(codigoId, conn);
            productoDao.eliminar(productoId, conn);

            conn.commit();
            System.out.println("✔ Transacción OK: Producto y Código eliminados.");

        } catch (Exception e) {
            rollback(conn, e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // ============================================================
    //  VALIDACIONES - CÓDIGO EXISTENTE
    // ============================================================
    private void validarProducto(Producto p) throws SQLException {
        if (p == null) throw new SQLException("El producto no puede ser nulo.");

        if (p.getNombre() == null || p.getNombre().trim().isEmpty())
            throw new SQLException("El nombre del producto es obligatorio.");

        if (p.getMarca() == null || p.getMarca().trim().isEmpty())
            throw new SQLException("La marca del producto es obligatoria.");

        if (p.getCategoria() == null || p.getCategoria().trim().isEmpty())
            throw new SQLException("La categoría del producto es obligatoria.");

        if (p.getPrecio() < 0)
            throw new SQLException("El precio no puede ser negativo.");

        if (p.getPeso() != null && p.getPeso() < 0)
            throw new SQLException("El peso no puede ser negativo.");
    }

    private void validarCodigoBasico(CodigoBarras c) throws SQLException {
        if (c == null) throw new SQLException("El código de barras no puede ser nulo.");

        if (c.getValor() == null || c.getValor().trim().isEmpty())
            throw new SQLException("El valor del código de barras no puede ser vacío.");

        if (c.getTipo() == null)
            throw new SQLException("Debe especificarse el tipo de código.");
    }

    // ============================================================
    //  AUXILIARES - CÓDIGO EXISTENTE
    // ============================================================
    private void rollback(Connection conn, Exception e) throws SQLException {
        if (conn != null) {
            try {
                conn.rollback();
                System.err.println("⚠ Rollback realizado por error: " + e.getMessage());
            } catch (SQLException ex) {
                System.err.println("⚠ Error en rollback: " + ex.getMessage());
            }
        }
        throw new SQLException("Error en la transacción", e);
    }

    private void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {
                System.err.println("⚠ Error al cerrar conexión: " + ex.getMessage());
            }
        }
    }
}