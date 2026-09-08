package gui;
import datos.Inventario;
import modelo.*;

import javax.swing.*;
import java.awt.*;

public class PanelVentas extends JPanel {
    private Inventario inventario;
    private Pedido pedidoActual;
    private JComboBox<Producto> comboProductos;
    private JTextArea areaDetalle;

    public PanelVentas(Inventario inventario) {
        this.inventario = inventario;
        setLayout(new BorderLayout(10, 10));

        // PARTE SUPERIOR: Configuración de Orden
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelTop.setBorder(BorderFactory.createTitledBorder("Configuración de Pedido"));

        JTextField txtCliente = new JTextField(10);
        JTextField txtDatoExtra = new JTextField(10); // Mesa o Dirección
        JLabel lblExtra = new JLabel("N° Mesa:");

        JRadioButton rbLocal = new JRadioButton("Comer en Local", true);
        JRadioButton rbExpress = new JRadioButton("Express");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbLocal); bg.add(rbExpress);

        rbLocal.addActionListener(e -> lblExtra.setText("N° Mesa:"));
        rbExpress.addActionListener(e -> lblExtra.setText("Dirección:"));

        JButton btnIniciar = new JButton("Iniciar Orden");
        btnIniciar.addActionListener(e -> {
            String cliente = txtCliente.getText().trim().isEmpty() ? "Cliente Genérico" : txtCliente.getText().trim();
            if (rbLocal.isSelected()) {
                int mesa = 1;
                try { mesa = Integer.parseInt(txtDatoExtra.getText().trim()); } catch (Exception ex) {}
                pedidoActual = new PedidoLocal(cliente, mesa);
                areaDetalle.setText("--- NUEVA ORDEN (LOCAL - MESA " + mesa + ") ---
                        Cliente: " + cliente + "

                ");
            } else {
                String dir = txtDatoExtra.getText().trim().isEmpty() ? "Dirección desconocida" : txtDatoExtra.getText().trim();
                pedidoActual = new PedidoExpress(cliente, dir);
                areaDetalle.setText("--- NUEVA ORDEN (EXPRESS +₡1,500 Envíos) ---
                        Cliente: " + cliente + "
                Dirección: " + dir + "

                ");
            }
        });

        panelTop.add(new JLabel("Cliente:")); panelTop.add(txtCliente);
        panelTop.add(rbLocal); panelTop.add(rbExpress);
        panelTop.add(lblExtra); panelTop.add(txtDatoExtra);
        panelTop.add(btnIniciar);

        // CENTRO: Selección de Productos y Factura Temporal
        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        JPanel panelProd = new JPanel(new FlowLayout(FlowLayout.LEFT));

        comboProductos = new JComboBox<>();
        JButton btnRefrescar = new JButton("🔄 Cargar Catálogo");
        btnRefrescar.addActionListener(e -> {
            comboProductos.removeAllItems();
            for (Producto p : inventario.obtenerTodos()) comboProductos.addItem(p);
        });

        JButton btnAgregar = new JButton("➕ Añadir a Orden");
        btnAgregar.addActionListener(e -> {
            if (pedidoActual == null) {
                JOptionPane.showMessageDialog(this, "Primero debe hacer clic en 'Iniciar Orden'.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Producto p = (Producto) comboProductos.getSelectedItem();
            if (p != null) {
                if (p.getCantidadInventario() <= 0) {
                    JOptionPane.showMessageDialog(this, "¡Sin Stock suficiente de este producto!", "Error Inventario", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                pedidoActual.agregarProducto(p);
                areaDetalle.append(" > " + p.getNombre() + " ........ ₡" + p.getPrecio() + "
                        ");
            }
        });

        panelProd.add(btnRefrescar); panelProd.add(comboProductos); panelProd.add(btnAgregar);
        areaDetalle = new JTextArea(12, 40);
        areaDetalle.setEditable(false);

        panelCentro.add(panelProd, BorderLayout.NORTH);
        panelCentro.add(new JScrollPane(areaDetalle), BorderLayout.CENTER);

        // SUR: Facturación y Cobro
        JButton btnFacturar = new JButton("💳 Facturar y Procesar Pago");
        btnFacturar.setFont(new Font("Arial", Font.BOLD, 13));
        btnFacturar.addActionListener(e -> {
            if (pedidoActual == null || pedidoActual.getCarrito().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El carrito de ventas está vacío.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double subtotal = pedidoActual.calcularSubtotal();
            double iva = pedidoActual.calcularImpuesto();
            double total = pedidoActual.calcularTotal();

            String resumen = String.format("--- FACTURA FINAL ---
                    Subtotal: ₡%.2f
            IVA (13%%): ₡%.2f
            Total Final: ₡%.2f", subtotal, iva, total);

            String[] opciones = {"Efectivo", "Tarjeta", "SINPE Movil"};
            int pagoMetodo = JOptionPane.showOptionDialog(this, resumen + "

                    Seleccione método de pago:", "Cobro POS",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

            if (pagoMetodo == 0) { // Efectivo
                String montoStr = JOptionPane.showInputDialog(this, "Monto entregado por el cliente:");
                if (Validador.esNumeroDouble(montoStr)) {
                    double pago = Double.parseDouble(montoStr);
                    if (pago >= total) {
                        JOptionPane.showMessageDialog(this, String.format("Pago aceptado. Vuelto: ₡%.2f", (pago - total)));
                    } else {
                        JOptionPane.showMessageDialog(this, "Monto insuficiente.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Transacción procesada con éxito.");
            }

            // Descontar inventario
            for (Producto p : pedidoActual.getCarrito()) {
                p.setCantidadInventario(p.getCantidadInventario() - 1);
            }

            pedidoActual = null;
            areaDetalle.setText("");
        });

        add(panelTop, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(btnFacturar, BorderLayout.SOUTH);
    }
}
//adios