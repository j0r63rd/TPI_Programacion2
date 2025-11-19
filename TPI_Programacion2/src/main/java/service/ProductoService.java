package service;

import config.DatabaseConnection;
import dao.ProductoDaoImpl;
import dao.CodigoBarrasDaoImpl;
import entities.Producto;
import entities.CodigoBarras;

import java.sql.Connection;
import java.sql.SQLException;

public class ProductoService {
    private final ProductoDaoImpl productoDao;
    private final CodigoBarrasDaoImpl codigoDao;

    public ProductoService() {
        this.productoDao = new ProductoDaoImpl();
        this.codigoDao = new CodigoBarrasDaoImpl();
    }

    // Crear Producto + Código
    public void crearProductoConCodigo(Producto producto, CodigoBarras codigo) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            productoDao.crear(producto, conn);
            codigo.setProductoId(producto.getId());
            codigoDao.crear(codigo, conn);

            conn.commit();
            System.out.println("Transacción completada: Producto y Código creados.");
        } catch (Exception e) {
            rollback(conn, e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // Actualizar Producto + Código
    public void actualizarProductoConCodigo(Producto producto, CodigoBarras codigo) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            productoDao.actualizar(producto, conn);
            codigoDao.actualizar(codigo, conn);

            conn.commit();
            System.out.println("Transacción completada: Producto y Código actualizados.");
        } catch (Exception e) {
            rollback(conn, e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // Eliminar Producto + Código (baja lógica)
    public void eliminarProductoConCodigo(Long productoId, Long codigoId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            codigoDao.eliminar(codigoId, conn);
            productoDao.eliminar(productoId, conn);

            conn.commit();
            System.out.println("Transacción completada: Producto y Código eliminados.");
        } catch (Exception e) {
            rollback(conn, e);
        } finally {
            cerrarConexion(conn);
        }
    }

    // Auxiliares
    private void rollback(Connection conn, Exception e) throws SQLException {
        if (conn != null) {
            try {
                conn.rollback();
                System.err.println("Rollback realizado por error: " + e.getMessage());
            } catch (SQLException ex) {
                System.err.println("Error en rollback: " + ex.getMessage());
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
                System.err.println("Error al cerrar conexión: " + ex.getMessage());
            }
        }
    }
}