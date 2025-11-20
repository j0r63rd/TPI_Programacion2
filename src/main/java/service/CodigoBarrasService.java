package service;

import dao.CodigoBarrasDaoImpl;
import dao.ProductoDaoImpl;
import entities.CodigoBarras;
import entities.Producto;
import entities.TipoCodigo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList; // Necesario para el listado

/**
 * Service para manejar la lógica de negocio de Código de Barras.
 */
public class CodigoBarrasService {

    private final CodigoBarrasDaoImpl codigoDao;
    private final ProductoDaoImpl productoDao;

    public CodigoBarrasService() {
        this.codigoDao = new CodigoBarrasDaoImpl();
        this.productoDao = new ProductoDaoImpl();
    }

    // ================== CRUD BÁSICO ==================

    public CodigoBarras insertar(CodigoBarras cb) throws SQLException {
        validarCodigoBarras(cb, true);
        codigoDao.crear(cb); 
        return cb;
    }

    public CodigoBarras actualizar(CodigoBarras cb) throws SQLException {
        if (cb.getId() == null) {
            throw new SQLException("El ID del código no puede ser nulo para actualizar.");
        }
        validarCodigoBarras(cb, false);
        codigoDao.actualizar(cb);
        return cb;
    }

    public void eliminar(long idCodigo) throws SQLException {
        codigoDao.eliminar(idCodigo); // baja lógica
    }

    public CodigoBarras getById(long id) throws SQLException {
        return codigoDao.leer(id);
    }

    public List<CodigoBarras> getAll() throws SQLException {
        return codigoDao.leerTodos();
    }

    // ================== BÚSQUEDAS OPTIMIZADAS ==================

    /**
     * Busca un código de barras por su valor exacto (ahora usa cláusula WHERE).
     */
    public CodigoBarras buscarPorValor(String valor) throws SQLException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new SQLException("El valor del código no puede ser vacío.");
        }
        // DELEGACIÓN EFICIENTE AL DAO
        return codigoDao.buscarPorValor(valor); 
    }

    /**
     * Devuelve todos los códigos asociados a un producto (ahora usa cláusula WHERE).
     */
    public List<CodigoBarras> buscarPorProductoId(Long productoId) throws SQLException {
        if (productoId == null) {
            throw new SQLException("El id de producto no puede ser nulo.");
        }
        // DELEGACIÓN EFICIENTE AL DAO
        return codigoDao.buscarPorProductoId(productoId);
    }

    /**
     * Busca todos los códigos de un tipo dado (ahora usa cláusula WHERE).
     */
    public List<CodigoBarras> buscarPorTipo(TipoCodigo tipo) throws SQLException {
        if (tipo == null) {
            throw new SQLException("El tipo de código no puede ser nulo.");
        }
        // DELEGACIÓN EFICIENTE AL DAO
        return codigoDao.buscarPorTipo(tipo);
    }

    // ================== VALIDACIONES ==================

    /**
     * Valida los datos del código de barras y aplica la regla 1→1.
     */
    private void validarCodigoBarras(CodigoBarras cb, boolean esNuevo) throws SQLException {
        if (cb == null) {
            throw new SQLException("El código de barras no puede ser nulo.");
        }

        if (cb.getProductoId() == null) {
            throw new SQLException("Debe asociarse el código a un producto (productoId no puede ser nulo).");
        }

        Producto p = productoDao.leer(cb.getProductoId());
        if (p == null) {
            throw new SQLException("El producto con ID " + cb.getProductoId() + " no existe.");
        }

        if (cb.getTipo() == null) {
            throw new SQLException("El tipo de código es obligatorio.");
        }

        if (cb.getValor() == null || cb.getValor().trim().isEmpty()) {
            throw new SQLException("El valor del código de barras es obligatorio.");
        }

        if (cb.getTipo() == TipoCodigo.EAN13) {
            String v = cb.getValor();
            if (v.length() != 13 || !esNumerico(v)) {
                throw new SQLException("Para tipo EAN13 el valor debe tener exactamente 13 dígitos numéricos.");
            }
        }

        if (cb.getFechaAsignacion() == null) {
            cb.setFechaAsignacion(LocalDate.now());
        }

        // Regla 1→1: un producto solo puede tener un código de barras
        List<CodigoBarras> existentes = buscarPorProductoId(cb.getProductoId());
        for (CodigoBarras otro : existentes) {
            if (esNuevo || !otro.getId().equals(cb.getId())) {
                throw new SQLException("El producto " + cb.getProductoId()
                        + " ya tiene un código de barras asociado (ID " + otro.getId() + ").");
            }
        }
    }

    // ================== HELPERS ==================

    private boolean esNumerico(String valor) {
        for (int i = 0; i < valor.length(); i++) {
            if (!Character.isDigit(valor.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}