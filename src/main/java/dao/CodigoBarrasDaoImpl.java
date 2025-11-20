package dao;

import config.DatabaseConnection;
import entities.CodigoBarras;
import entities.TipoCodigo;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Asumimos que GenericDao<T> tiene al menos: crear(T), leer(long), leerTodos(), actualizar(T), eliminar(long)
public class CodigoBarrasDaoImpl implements GenericDao<CodigoBarras> {

    // Consultas SQL optimizadas
    private static final String SELECT_BY_VALOR_SQL = "SELECT * FROM codigo_barras WHERE valor = ? AND eliminado = false";
    private static final String SELECT_BY_PRODUCTO_SQL = "SELECT * FROM codigo_barras WHERE producto_id = ? AND eliminado = false";
    private static final String SELECT_BY_TIPO_SQL = "SELECT * FROM codigo_barras WHERE tipo = ? AND eliminado = false";
    
    // Consultas SQL del CRUD
    private static final String INSERT_SQL = "INSERT INTO codigo_barras (producto_id, tipo, valor, fecha_asignacion, observaciones, eliminado) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM codigo_barras WHERE id = ? AND eliminado = false";
    private static final String SELECT_ALL_SQL = "SELECT * FROM codigo_barras WHERE eliminado = false";
    private static final String UPDATE_SQL = "UPDATE codigo_barras SET producto_id = ?, tipo = ?, valor = ?, fecha_asignacion = ?, observaciones = ?, eliminado = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE codigo_barras SET eliminado = true WHERE id = ?"; // Baja lógica

    // =======================================================
    // MÉTODOS DE BÚSQUEDA OPTIMIZADA
    // =======================================================

    public CodigoBarras buscarPorValor(String valor) throws SQLException {
        // ... (código interno igual, busca y mapea el ResultSet)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_VALOR_SQL)) {
            
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión para buscarPorValor", e);
        }
        return null;
    }

    public List<CodigoBarras> buscarPorProductoId(Long productoId) throws SQLException {
        // ... (código interno igual, busca y mapea el ResultSet)
        List<CodigoBarras> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PRODUCTO_SQL)) {
            
            ps.setLong(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión para buscarPorProductoId", e);
        }
        return lista;
    }

    public List<CodigoBarras> buscarPorTipo(TipoCodigo tipo) throws SQLException {
        // ... (código interno igual, busca y mapea el ResultSet)
        List<CodigoBarras> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_TIPO_SQL)) {
            
            ps.setString(1, tipo.name()); // Guardamos el enum como String
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión para buscarPorTipo", e);
        }
        return lista;
    }

    // =======================================================
    // MÉTODOS CRUD ESTÁNDAR (IMPLEMENTAN INTERFACE)
    // =======================================================
    
    @Override
    public void crear(CodigoBarras entidad) throws SQLException {
        crear(entidad, null); 
    }

    // Eliminamos @Override aquí, ya que la firma de la interfaz solo suele tener crear(T entidad)
    public void crear(CodigoBarras entidad, Connection conn) throws SQLException {
        boolean closeConn = (conn == null);
        try {
            if (closeConn) {
                conn = DatabaseConnection.getConnection();
            }
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, entidad.getProductoId());
                ps.setString(2, entidad.getTipo().name());
                ps.setString(3, entidad.getValor());
                ps.setDate(4, Date.valueOf(entidad.getFechaAsignacion()));
                ps.setString(5, entidad.getObservaciones());
                ps.setBoolean(6, entidad.getEliminado()); // 🚨 CORREGIDO: Usamos getEliminado()
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        entidad.setId(rs.getLong(1));
                    }
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public CodigoBarras leer(long id) throws SQLException {
        // ... (código interno igual, busca y mapea el ResultSet)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
        return null;
    }

    @Override
    public List<CodigoBarras> leerTodos() throws SQLException {
        // ... (código interno igual, busca y mapea el ResultSet)
        List<CodigoBarras> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapResultSet(rs));
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
        return lista;
    }

    @Override
    public void actualizar(CodigoBarras entidad) throws SQLException {
        actualizar(entidad, null);
    }
    
    // Eliminamos @Override aquí
    public void actualizar(CodigoBarras entidad, Connection conn) throws SQLException {
        boolean closeConn = (conn == null);
        try {
            if (closeConn) {
                conn = DatabaseConnection.getConnection();
            }
            try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
                ps.setLong(1, entidad.getProductoId());
                ps.setString(2, entidad.getTipo().name());
                ps.setString(3, entidad.getValor());
                ps.setDate(4, Date.valueOf(entidad.getFechaAsignacion()));
                ps.setString(5, entidad.getObservaciones());
                ps.setBoolean(6, entidad.getEliminado()); // 🚨 CORREGIDO: Usamos getEliminado()
                ps.setLong(7, entidad.getId());
                ps.executeUpdate();
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public void eliminar(long id) throws SQLException {
        eliminar(id, null);
    }
    
    // Eliminamos @Override aquí
    public void eliminar(long id, Connection conn) throws SQLException {
        boolean closeConn = (conn == null);
        try {
            if (closeConn) {
                conn = DatabaseConnection.getConnection();
            }
            try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        } finally {
            if (closeConn && conn != null) {
                conn.close();
            }
        }
    }

    // Método auxiliar de mapeo de resultados
    private CodigoBarras mapResultSet(ResultSet rs) throws SQLException {
        CodigoBarras cb = new CodigoBarras();
        cb.setId(rs.getLong("id"));
        cb.setProductoId(rs.getLong("producto_id"));
        cb.setTipo(TipoCodigo.valueOf(rs.getString("tipo")));
        cb.setValor(rs.getString("valor"));
        
        Date fechaSQL = rs.getDate("fecha_asignacion");
        if (fechaSQL != null) {
            cb.setFechaAsignacion(fechaSQL.toLocalDate());
        } else {
            cb.setFechaAsignacion(null);
        }
        
        cb.setObservaciones(rs.getString("observaciones"));
        cb.setEliminado(rs.getBoolean("eliminado"));
        return cb;
    }
}