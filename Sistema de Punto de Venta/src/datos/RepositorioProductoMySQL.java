package datos;
import modelo.Producto;
import util.ConfiguracionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepositorioProductoMySQL implements Repositorio<Producto> {

    private final String url = ConfiguracionBD.obtenerVariable("DB_SODA_URL", "jdbc:mysql://localhost:3306/soda_pos?serverTimezone=UTC");
    private final String usuario = ConfiguracionBD.obtenerVariable("DB_SODA_USER", "root");
    private final String clave = ConfiguracionBD.obtenerVariable("DB_SODA_PASSWORD", "rootroot");

    private Connection conectar() throws SQLException {
        return util.GestorConexion.obtenerConexion(url, usuario, clave);
    }

    @Override
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        try (Connection c = conectar()) {
            if (c == null) {
                throw new SQLException("No se pudo establecer la conexión con la base de datos.");
            }
            try (CallableStatement cs = c.prepareCall("{CALL sp_listar_productos()}");
                 ResultSet rs = cs.executeQuery()) {

                if (rs == null) {
                    return lista;
                }
                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    String nombre = rs.getString("nombre");
                    String categoria = rs.getString("categoria");
                    double precio = rs.getDouble("precio");
                    if (rs.wasNull()) {
                        precio = 0.0;
                    }
                    int stock = rs.getInt("stock");
                    if (rs.wasNull()) {
                        stock = 0;
                    }

                    // Evitar NPE por campos null en la BD
                    if (codigo == null) codigo = "";
                    if (nombre == null) nombre = "";
                    if (categoria == null) categoria = "";

                    Producto p = new Producto(codigo, nombre, categoria, precio, stock);
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar productos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void insertar(Producto p) {
        if (p == null) return;
        try (Connection c = conectar()) {
            if (c == null) throw new SQLException("No se pudo establecer la conexión.");
            try (CallableStatement cs = c.prepareCall("{CALL sp_insertar_producto(?,?,?,?,?)}")) {
                cs.setString(1, p.getCodigo());
                cs.setString(2, p.getNombre());
                cs.setString(3, p.getCategoria());
                cs.setDouble(4, p.getPrecio());
                cs.setInt(5, p.getCantidadInventario());
                cs.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Producto p) {
        if (p == null) return;
        try (Connection c = conectar()) {
            if (c == null) throw new SQLException("No se pudo establecer la conexión.");
            try (CallableStatement cs = c.prepareCall("{CALL sp_actualizar_producto(?,?,?,?,?)}")) {
                cs.setString(1, p.getCodigo());
                cs.setString(2, p.getNombre());
                cs.setString(3, p.getCategoria());
                cs.setDouble(4, p.getPrecio());
                cs.setInt(5, p.getCantidadInventario());
                cs.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String codigo) {
        if (codigo == null) return;
        try (Connection c = conectar()) {
            if (c == null) throw new SQLException("No se pudo establecer la conexión.");
            try (CallableStatement cs = c.prepareCall("{CALL sp_eliminar_producto(?)}")) {
                cs.setString(1, codigo);
                cs.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void decrementarStock(String codigo) {
        if (codigo == null) return;
        try (Connection c = conectar()) {
            if (c == null) throw new SQLException("No se pudo establecer la conexión.");
            try (PreparedStatement ps = c.prepareStatement("UPDATE producto SET stock = stock - 1 WHERE codigo = ? AND stock > 0")) {
                ps.setString(1, codigo);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + codigo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al decrementar stock: " + e.getMessage(), e);
        }
    }
}