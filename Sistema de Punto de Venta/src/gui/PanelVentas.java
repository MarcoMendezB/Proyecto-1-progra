package gui;
import datos.Repositorio;
import servicio.VentaService;
import modelo.*;
import javax.swing.*;
import javax.swing.SwingWorker;
import java.awt.*;
import java.util.List;

public class PanelVentas extends JPanel {
    private VentaService ventaService;
    private Pedido pedidoActual;
    private String nombreClienteActual;
    private JComboBox<Producto> comboProductos;
    private JTextArea areaFactura;
    private JComboBox<String> comboPago;

    public PanelVentas(VentaService ventaService) {
        this.ventaService = ventaService;
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
            String cli = txtCliente.getText().isEmpty() ? "Genérico" : txtCliente.getText().trim();
            if (rbLocal.isSelected()) {
                int mesa;
                try {
                    mesa = txtMesa.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtMesa.getText().trim());
                    if (mesa < 0) {
                        JOptionPane.showMessageDialog(this, "El número de mesa no puede ser negativo.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Ingrese un número de mesa válido.");
                    return;
                }
                nombreClienteActual = cli;
                pedidoActual = new PedidoLocal(cli, mesa);
                areaFactura.setText("Orden Local iniciada - Cliente: " + cli + " - Mesa: " + mesa + "\n");
            } else {
                String direccion = txtDireccion.getText().trim();
                if (direccion.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "La dirección es obligatoria para pedidos Express.");
                    return;
                }
                nombreClienteActual = cli;
                pedidoActual = new PedidoExpress(cli, direccion);
                areaFactura.setText("Orden Express iniciada - Cliente: " + cli + "\nDir: " + direccion + "\n");
            }
        });
        panelDetallePedido.add(btnIniciar);

        panelTop.add(panelCliente); panelTop.add(panelDetallePedido);

        // --- CENTRO: Productos ---
        JPanel panelCentro = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel();
        comboProductos = new JComboBox<>();

        JButton btnActualizar = new JButton("Cargar Catálogo");
        btnActualizar.addActionListener(e -> actualizarComboProductosEnHilo());

        JButton btnAdd = new JButton("Añadir a Orden");
        btnAdd.addActionListener(e -> {
            if (pedidoActual == null) { JOptionPane.showMessageDialog(this, "Inicie la orden primero"); return; }
            Producto p = (Producto) comboProductos.getSelectedItem();
            if (p != null) {
                // VALIDACIÓN DE INVENTARIO
                long cantidadEnCarrito = pedidoActual.getCarrito().stream().filter(prod -> prod != null && prod.getCodigo().equals(p.getCodigo())).count();
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

            ventaService.registrarVenta(pedidoActual, nombreClienteActual);

            // Imprimir Factura Final
            String facturaFinal = "--- FACTURA (" + metodo + ") ---\n" +
                    "Subtotal: ₡" + ventaService.calcularSubtotal(pedidoActual) + "\n" +
                    "IVA (13%): ₡" + ventaService.calcularImpuesto(pedidoActual) + "\n" +
                    "Total a Pagar: ₡" + ventaService.calcularTotal(pedidoActual) + mensajeVuelto +
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
        actualizarComboProductosEnHilo();
    }

    public void actualizarComboProductos() {
        comboProductos.removeAllItems();
        List<Producto> productos = ventaService.getRepositorioProductos().obtenerTodos();
        if (productos == null) return;
        for (Producto p : productos) {
            if (p != null) comboProductos.addItem(p);
        }
    }

    /**
     * Obtiene los productos en un hilo secundario para
     * evitar bloquear la interfaz gráfica.
     */
    public void actualizarComboProductosEnHilo() {
        SwingWorker<List<Producto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return ventaService.getRepositorioProductos().obtenerTodos();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    comboProductos.removeAllItems();
                    if (productos == null) {
                        return;
                    }
                    // Mostrar únicamente productos con stock
                    for (Producto p : productos) {
                        if (p != null && p.getCantidadInventario() > 0) {
                            comboProductos.addItem(p);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            PanelVentas.this,
                            "Error al cargar los productos: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}