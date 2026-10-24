package datos;
import modelo.*;
import util.ConfiguracionBD;
import java.sql.*;

public class RepositorioPedidoMySQL {

    private final String url = ConfiguracionBD.obtenerVariable("DB_SODA_URL", "jdbc:mysql://localhost:3306/soda_pos?serverTimezone=UTC");
    private final String usuario = ConfiguracionBD.obtenerVariable("DB_SODA_USER", null);
    private final String clave = ConfiguracionBD.obtenerVariable("DB_SODA_PASSWORD", null);

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    public void registrarVenta(Pedido pedido) {
        // Usamos transacciones: todo o nada.
        try (Connection c = conectar()) {
            c.setAutoCommit(false); // Inicia transacción

            try {
                // 1. Insertar el Pedido principal
                String sqlPedido = "INSERT INTO pedido (cliente, tipo_pedido, total) VALUES (?, ?, ?)";
                int pedidoId = -1;
                try (PreparedStatement ps = c.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, pedido.getCliente()); // Asume que agregaste getCliente() a Pedido
                    ps.setString(2, pedido instanceof PedidoLocal ? "LOCAL" : "EXPRESS");
                    ps.setDouble(3, pedido.calcularTotal());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) pedidoId = rs.getInt(1);
                    }
                }

                // 2. Insertar los Detalles y descontar stock
                String sqlDetalle = "INSERT INTO detalle_pedido (pedido_id, producto_codigo, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
                String sqlStock = "UPDATE producto SET stock = stock - 1 WHERE codigo = ? AND stock > 0";

                try (PreparedStatement psDetalle = c.prepareStatement(sqlDetalle);
                     PreparedStatement psStock = c.prepareStatement(sqlStock)) {

                    for (Producto p : pedido.getCarrito()) {
                        // Insertar detalle
                        psDetalle.setInt(1, pedidoId);
                        psDetalle.setString(2, p.getCodigo());
                        psDetalle.setInt(3, 1); // 1 unidad por iteración según la lógica original
                        psDetalle.setDouble(4, p.getPrecio());
                        psDetalle.executeUpdate();

                        // Reducir stock
                        psStock.setString(1, p.getCodigo());
                        int filas = psStock.executeUpdate();
                        if (filas == 0) {
                            throw new SQLException("Stock insuficiente para: " + p.getNombre());
                        }
                    }
                }

                c.commit(); // Confirmar la transacción si todo salió bien
            } catch (SQLException ex) {
                c.rollback(); // Deshacer todo si hubo un error
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el pedido: " + e.getMessage(), e);
        }
    }
}