package dao;

import config.DatabaseConnection;
import entities.CodigoBarras;
import entities.TipoCodigo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class CodigoBarrasDaoImpl implements GenericDao<CodigoBarras> {
    private static final String INSERT_SQL = "INSERT INTO codigo_barras (producto_id, tipo, valor, fecha_asignacion, observaciones, eliminado) VALUES (?, ?, ?, ?, ?, false)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM codigo_barras WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM codigo_barras WHERE eliminado = false";
    private static final String UPDATE_SQL = "UPDATE codigo_barras SET tipo=?, valor=?, fecha_asignacion=?, observaciones=? WHERE id=?";
    private static final String DELETE_SQL = "UPDATE codigo_barras SET eliminado=true WHERE id=?";

    // Métodos originales (compatibilidad)
    @Override
    public void crear(CodigoBarras cb) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            crear(cb, conn);
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
    }

    @Override
    public CodigoBarras leer(long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return leer(id, conn);
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
    }

    @Override
    public List<CodigoBarras> leerTodos() throws SQLException {
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
    public void actualizar(CodigoBarras cb) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            actualizar(cb, conn);
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
    }

    @Override
    public void eliminar(long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            eliminar(id, conn);
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
    }

    // ✅ Sobrecargas con Connection externa
    public void crear(CodigoBarras cb, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            if (cb.getProductoId() != null) {
                ps.setLong(1, cb.getProductoId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, cb.getTipo().name());
            ps.setString(3, cb.getValor());
            if (cb.getFechaAsignacion() != null) {
                ps.setDate(4, Date.valueOf(cb.getFechaAsignacion()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, cb.getObservaciones());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cb.setId(rs.getLong(1));
                }
            }
        }
    }

    public CodigoBarras leer(long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public void actualizar(CodigoBarras cb, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, cb.getTipo().name());
            ps.setString(2, cb.getValor());
            if (cb.getFechaAsignacion() != null) {
                ps.setDate(3, Date.valueOf(cb.getFechaAsignacion()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, cb.getObservaciones());
            ps.setLong(5, cb.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private CodigoBarras mapResultSet(ResultSet rs) throws SQLException {
        CodigoBarras cb = new CodigoBarras();
        cb.setId(rs.getLong("id"));
        cb.setEliminado(rs.getBoolean("eliminado"));
        cb.setTipo(TipoCodigo.valueOf(rs.getString("tipo")));
        cb.setValor(rs.getString("valor"));
        Date sqlDate = rs.getDate("fecha_asignacion");
        cb.setFechaAsignacion((sqlDate != null) ? sqlDate.toLocalDate() : null);
        cb.setObservaciones(rs.getString("observaciones"));
        cb.setProductoId(rs.getLong("producto_id"));
        return cb;
    }
}