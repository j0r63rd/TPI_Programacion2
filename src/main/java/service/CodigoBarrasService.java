package service;

import dao.CodigoBarrasDaoImpl;
import dao.ProductoDaoImpl;
import entities.CodigoBarras;
import entities.Producto;
import entities.TipoCodigo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service para manejar la lógica de negocio de Código de Barras.
 *
 * Responsabilidades (Integrante 2):
 *  - CRUD básico sobre CodigoBarras
 *  - Validaciones de datos
 *  - Regla 1→1 (un código por producto)
 *  - Búsquedas (por valor, por producto, por tipo)
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
        codigoDao.crear(cb); // el DAO setea el ID generado
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
        // baja lógica
        codigoDao.eliminar(idCodigo);
    }

    public CodigoBarras getById(long id) throws SQLException {
        return codigoDao.leer(id);
    }

    public List<CodigoBarras> getAll() throws SQLException {
        return codigoDao.leerTodos();
    }

    // ================== BÚSQUEDAS ==================

    /**
     * Busca un código de barras por su valor exacto.
     */
    public CodigoBarras buscarPorValor(String valor) throws SQLException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new SQLException("El valor del código no puede ser vacío.");
        }

        for (CodigoBarras cb : codigoDao.leerTodos()) {
            if (valor.equals(cb.getValor())) {
                return cb;
            }
        }
        return null; // no encontrado
    }

    /**
     * Devuelve todos los códigos asociados a un producto.
     * En nuestra regla 1→1 debería devolver 0 o 1 elemento.
     */
    public List<CodigoBarras> buscarPorProductoId(Long productoId) throws SQLException {
        if (productoId == null) {
            throw new SQLException("El id de producto no puede ser nulo.");
        }

        List<CodigoBarras> resultado = new ArrayList<>();
        for (CodigoBarras cb : codigoDao.leerTodos()) {
            if (Objects.equals(cb.getProductoId(), productoId)) {
                resultado.add(cb);
            }
        }
        return resultado;
    }

    /**
     * Busca todos los códigos de un tipo dado (EAN13, etc.).
     */
    public List<CodigoBarras> buscarPorTipo(TipoCodigo tipo) throws SQLException {
        if (tipo == null) {
            throw new SQLException("El tipo de código no puede ser nulo.");
        }

        List<CodigoBarras> resultado = new ArrayList<>();
        for (CodigoBarras cb : codigoDao.leerTodos()) {
            if (tipo.equals(cb.getTipo())) {
                resultado.add(cb);
            }
        }
        return resultado;
    }

    // ================== VALIDACIONES ==================

    /**
     * Valida los datos del código de barras y aplica la regla 1→1.
     * @param esNuevo true si es inserción, false si es actualización
     */
    private void validarCodigoBarras(CodigoBarras cb, boolean esNuevo) throws SQLException {
        if (cb == null) {
            throw new SQLException("El código de barras no puede ser nulo.");
        }

        // Producto asociado
        if (cb.getProductoId() == null) {
            throw new SQLException("Debe asociarse el código a un producto (productoId no puede ser nulo).");
        }

        // Verificar que el producto exista
        Producto p = productoDao.leer(cb.getProductoId());
        if (p == null) {
            throw new SQLException("El producto con ID " + cb.getProductoId() + " no existe.");
        }

        // Tipo obligatorio
        if (cb.getTipo() == null) {
            throw new SQLException("El tipo de código es obligatorio.");
        }

        // Valor obligatorio
        if (cb.getValor() == null || cb.getValor().trim().isEmpty()) {
            throw new SQLException("El valor del código de barras es obligatorio.");
        }

        // Ejemplo de validación para EAN13: exactamente 13 dígitos
        if (cb.getTipo() == TipoCodigo.EAN13) {
            String v = cb.getValor();
            if (v.length() != 13 || !esNumerico(v)) {
                throw new SQLException("Para tipo EAN13 el valor debe tener exactamente 13 dígitos numéricos.");
            }
        }

        // Fecha de asignación: si es nula, se puede completar con hoy
        if (cb.getFechaAsignacion() == null) {
            cb.setFechaAsignacion(LocalDate.now());
        }

        // Regla 1→1: un producto solo puede tener un código de barras
        // (en inserción o actualización, no debe haber otro código distinto asociado al mismo producto)
        List<CodigoBarras> existentes = buscarPorProductoId(cb.getProductoId());
        for (CodigoBarras otro : existentes) {
            // si es nuevo, cualquier registro existente viola la regla
            // si es actualización, solo viola si el ID es distinto
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
