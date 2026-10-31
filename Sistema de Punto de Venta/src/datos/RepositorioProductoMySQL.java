package datos;
import modelo.Producto;
import util.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepositorioProductoMySQL implements Repositorio<Producto> {

    // Se leen las variables configuradas por Jose en el Round 1
    private final String url = ConfiguracionBD.obtenerVariable("DB_SODA_URL", "jdbc:mysql://localhost:3306/soda_pos?serverTimezone=UTC");
    private final String usuario = ConfiguracionBD.obtenerVariable("DB_SODA_USER", null);
    private final String clave = ConfiguracionBD.obtenerVariable("DB_SODA_PASSWORD", null);

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        // Try-with-resources asegura que la conexión se cierre automáticamente
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_listar_productos()}");
             ResultSet rs = cs.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getInt("stock")
                );
                lista.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar la flota: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public void insertar(Producto p) {
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_insertar_producto(?,?,?,?,?)}")) {
            cs.setString(1, p.getCodigo());
            cs.setString(2, p.getNombre());
            cs.setString(3, p.getCategoria());
            cs.setDouble(4, p.getPrecio());
            cs.setInt(5, p.getCantidadInventario());
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Producto p) {
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_actualizar_producto(?,?,?,?,?)}")) {
            cs.setString(1, p.getCodigo());
            cs.setString(2, p.getNombre());
            cs.setString(3, p.getCategoria());
            cs.setDouble(4, p.getPrecio());
            cs.setInt(5, p.getCantidadInventario());
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String codigo) {
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_eliminar_producto(?)}")) {
            cs.setString(1, codigo);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }
}
