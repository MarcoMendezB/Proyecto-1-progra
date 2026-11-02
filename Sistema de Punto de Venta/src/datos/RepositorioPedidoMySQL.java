package datos;
import modelo.*;
import util.ConfiguracionBD;
import java.sql.*;
import java.util.List;

public class RepositorioPedidoMySQL {

    private final String url = ConfiguracionBD.obtenerVariable("DB_SODA_URL", "jdbc:mysql://localhost:3306/soda_pos?serverTimezone=UTC");
    private final String usuario = ConfiguracionBD.obtenerVariable("DB_SODA_USER", "root");
    private final String clave = ConfiguracionBD.obtenerVariable("DB_SODA_PASSWORD", "rootroot");

    private Connection conectar() throws SQLException {
        return util.GestorConexion.obtenerConexion(url, usuario, clave);
    }

    public void registrarVenta(Pedido pedido) {
        if (pedido == null) return;
        try (Connection c = conectar()) {
            c.setAutoCommit(false);

            try {
                String sqlPedido = "INSERT INTO pedido (cliente, tipo_pedido, total) VALUES (?, ?, ?)";
                int pedidoId = -1;
                try (PreparedStatement ps = c.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    String cliente = pedido.getCliente();
                    ps.setString(1, cliente == null ? "" : cliente);
                    ps.setString(2, pedido instanceof PedidoLocal ? "LOCAL" : "EXPRESS");
                    ps.setDouble(3, pedido.calcularTotal());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs != null && rs.next()) pedidoId = rs.getInt(1);
                    }
                }

                String sqlDetalle = "INSERT INTO detalle_pedido (pedido_id, producto_codigo, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
                String sqlStock = "UPDATE producto SET stock = stock - 1 WHERE codigo = ? AND stock > 0";

                try (PreparedStatement psDetalle = c.prepareStatement(sqlDetalle);
                     PreparedStatement psStock = c.prepareStatement(sqlStock)) {

                    List<Producto> carrito = pedido.getCarrito();
                    if (carrito != null) {
                        for (Producto p : carrito) {
                            if (p == null) continue;
                            String codigo = p.getCodigo();
                            if (codigo == null || codigo.isEmpty()) continue;

                            psDetalle.setInt(1, pedidoId);
                            psDetalle.setString(2, codigo);
                            psDetalle.setInt(3, 1);
                            psDetalle.setDouble(4, p.getPrecio());
                            psDetalle.executeUpdate();

                            psStock.setString(1, codigo);
                            int filas = psStock.executeUpdate();
                            if (filas == 0) {
                                throw new SQLException("Stock insuficiente para: " + p.getNombre());
                            }
                        }
                    }
                }

                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar el pedido: " + e.getMessage(), e);
        }
    }
}