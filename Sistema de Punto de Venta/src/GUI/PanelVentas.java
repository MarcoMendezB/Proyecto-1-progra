package gui;
import datos.Inventario;
import modelo.*;
import javax.swing.*;
import java.awt.*;

public class PanelVentas extends JPanel {
    private Inventario inventario;
    private Pedido pedidoActual;
    private JComboBox<Producto> comboProductos;
    private JTextArea areaFactura;
    private JComboBox<String> comboPago;

    public PanelVentas(Inventario inventario) {
        this.inventario = inventario;
        setLayout(new BorderLayout());

        // --- TOP: Configuración del Pedido ---
        JPanel panelTop = new JPanel(new GridLayout(2, 1));

        JPanel panelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtCliente = new JTextField(10);
        JRadioButton rbLocal = new JRadioButton("Local", true);
        JRadioButton rbExpress = new JRadioButton("Express");
        ButtonGroup bg = new ButtonGroup(); bg.add(rbLocal); bg.add(rbExpress);

        panelCliente.add(new JLabel("Cliente:")); panelCliente.add(txtCliente);
        panelCliente.add(rbLocal); panelCliente.add(rbExpress);

        JPanel panelDetallePedido = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtMesa = new JTextField(5);
        JTextField txtDireccion = new JTextField(15);
        txtDireccion.setEnabled(false); // Desactivado por defecto (es Local)

        // Cambiar campos dependiendo si es Local o Express
        rbLocal.addActionListener(e -> { txtMesa.setEnabled(true); txtDireccion.setEnabled(false); });
        rbExpress.addActionListener(e -> { txtMesa.setEnabled(false); txtDireccion.setEnabled(true); });

        panelDetallePedido.add(new JLabel("Mesa #:")); panelDetallePedido.add(txtMesa);
        panelDetallePedido.add(new JLabel("Dirección:")); panelDetallePedido.add(txtDireccion);

        JButton btnIniciar = new JButton("Iniciar Orden");
        btnIniciar.addActionListener(e -> {
            String cli = txtCliente.getText().isEmpty() ? "Genérico" : txtCliente.getText();
            if (rbLocal.isSelected()) {
                int mesa = txtMesa.getText().isEmpty() ? 0 : Integer.parseInt(txtMesa.getText());
                pedidoActual = new PedidoLocal(cli, mesa);
                areaFactura.setText("Orden Local iniciada - Cliente: " + cli + " - Mesa: " + mesa + "\n");
            } else {
                pedidoActual = new PedidoExpress(cli, txtDireccion.getText());
                areaFactura.setText("Orden Express iniciada - Cliente: " + cli + "\nDir: " + txtDireccion.getText() + "\n");
            }
        });
        panelDetallePedido.add(btnIniciar);

        panelTop.add(panelCliente); panelTop.add(panelDetallePedido);

        // --- CENTRO: Productos ---
        JPanel panelCentro = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel();
        comboProductos = new JComboBox<>();

        JButton btnActualizar = new JButton("Cargar Catálogo");
        btnActualizar.addActionListener(e -> {
            comboProductos.removeAllItems();
            for (Producto p : inventario.obtenerTodos()) comboProductos.addItem(p);
        });

        JButton btnAdd = new JButton("Añadir a Orden");
        btnAdd.addActionListener(e -> {
            if (pedidoActual == null) { JOptionPane.showMessageDialog(this, "Inicie la orden primero"); return; }
            Producto p = (Producto) comboProductos.getSelectedItem();
            if (p != null) {
                // VALIDACIÓN DE INVENTARIO
                long cantidadEnCarrito = pedidoActual.getCarrito().stream().filter(prod -> prod.getCodigo().equals(p.getCodigo())).count();
                if (cantidadEnCarrito >= p.getCantidadInventario()) {
                    JOptionPane.showMessageDialog(this, "No hay suficiente inventario de " + p.getNombre());
                    return;
                }
                pedidoActual.agregarProducto(p);
                areaFactura.append("> " + p.getNombre() + " - ₡" + p.getPrecio() + "\n");
            }
        });

        panelBotones.add(btnActualizar); panelBotones.add(comboProductos); panelBotones.add(btnAdd);
        areaFactura = new JTextArea(10, 30);
        panelCentro.add(panelBotones, BorderLayout.NORTH);
        panelCentro.add(new JScrollPane(areaFactura), BorderLayout.CENTER);

        // --- SUR: Facturación y Pago ---
        JPanel panelSur = new JPanel();
        comboPago = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "SINPE"});
        JButton btnPagar = new JButton("Facturar y Cobrar");

        btnPagar.addActionListener(e -> {
            if (pedidoActual == null || pedidoActual.getCarrito().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El carrito está vacío"); return;
            }

            double total = pedidoActual.calcularTotal();
            String metodo = comboPago.getSelectedItem().toString();
            String mensajeVuelto = "";

            // Lógica de Efectivo y Vuelto
            if (metodo.equals("Efectivo")) {
                String input = JOptionPane.showInputDialog(this, "Total a pagar: ₡" + total + "\nIngrese monto recibido:");
                if (input == null || input.isEmpty()) return; // Canceló
                try {
                    double recibido = Double.parseDouble(input);
                    if (recibido < total) {
                        JOptionPane.showMessageDialog(this, "Monto insuficiente.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    double vuelto = recibido - total;
                    mensajeVuelto = "\nRecibido: ₡" + recibido + "\nVuelto: ₡" + vuelto;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Ingrese un monto válido.");
                    return;
                }
            }

            // REDUCIR INVENTARIO POR CADA VENTA
            for (Producto p : pedidoActual.getCarrito()) {
                p.setCantidadInventario(p.getCantidadInventario() - 1);
            }

            // Imprimir Factura Final
            String facturaFinal = "--- FACTURA (" + metodo + ") ---\n" +
                    "Subtotal: ₡" + pedidoActual.calcularSubtotal() + "\n" +
                    "IVA (13%): ₡" + pedidoActual.calcularImpuesto() + "\n" +
                    "Total a Pagar: ₡" + total + mensajeVuelto +
                    "\n\n¡Gracias por su compra!";

            JOptionPane.showMessageDialog(this, facturaFinal, "Factura", JOptionPane.INFORMATION_MESSAGE);
            pedidoActual = null; // Limpiar orden
            areaFactura.setText("");
        });

        panelSur.add(new JLabel("Método de Pago:"));
        panelSur.add(comboPago);
        panelSur.add(btnPagar);

        add(panelTop, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);
    }

    // Nuevo método público para recargar el ComboBox automáticamente
    public void actualizarComboProductos() {
        comboProductos.removeAllItems();
        for (Producto p : inventario.obtenerTodos()) {
            comboProductos.addItem(p);
        }
    }
}