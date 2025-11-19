package dao;

import config.DatabaseConnection;
import entities.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class ProductoDaoImpl implements GenericDao<Producto> {
    private static final String INSERT_SQL = "INSERT INTO producto (nombre, marca, categoria, precio, peso, eliminado) VALUES (?, ?, ?, ?, ?, false)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM producto WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM producto WHERE eliminado = false";
    private static final String UPDATE_SQL = "UPDATE producto SET nombre=?, marca=?, categoria=?, precio=?, peso=? WHERE id=?";
    private static final String DELETE_SQL = "UPDATE producto SET eliminado=true WHERE id=?";

    // Métodos originales (compatibilidad)
    @Override
    public void crear(Producto p) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            crear(p, conn);
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
    }

    @Override
    public Producto leer(long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return leer(id, conn);
        } catch (IOException e) {
            throw new SQLException("Error al obtener conexión", e);
        }
    }

    @Override
    public List<Producto> leerTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
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
    public void actualizar(Producto p) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            actualizar(p, conn);
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
    public void crear(Producto p, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getMarca());
            ps.setString(3, p.getCategoria());
            ps.setDouble(4, p.getPrecio());
            if (p.getPeso() != null) {
                ps.setDouble(5, p.getPeso());
            } else {
                ps.setNull(5, Types.DOUBLE);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                }
            }
        }
    }

    public Producto leer(long id, Connection conn) throws SQLException {
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

    public void actualizar(Producto p, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getMarca());
            ps.setString(3, p.getCategoria());
            ps.setDouble(4, p.getPrecio());
            if (p.getPeso() != null) {
                ps.setDouble(5, p.getPeso());
            } else {
                ps.setNull(5, Types.DOUBLE);
            }
            ps.setLong(6, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Producto mapResultSet(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setEliminado(rs.getBoolean("eliminado"));
        p.setNombre(rs.getString("nombre"));
        p.setMarca(rs.getString("marca"));
        p.setCategoria(rs.getString("categoria"));
        p.setPrecio(rs.getDouble("precio"));
        Double peso = rs.getDouble("peso");
        if (rs.wasNull()) {
            peso = null;
        }
        p.setPeso(peso);
        return p;
    }
}