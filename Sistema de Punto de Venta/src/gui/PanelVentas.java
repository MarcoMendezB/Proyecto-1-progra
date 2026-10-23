
package gui;

import datos.Inventario;
import modelo.*;
import util.GeneradorFactura;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelVentas extends JPanel {

    private Inventario inventario;
    private Pedido pedidoActual;
    private JComboBox<Producto> comboProductos;
    private JTextArea areaFactura;
    private JComboBox<String> comboPago;
    private String nombreClienteActual;

    public PanelVentas(Inventario inventario) {
        this.inventario = inventario;
        setLayout(new BorderLayout());

        // --- TOP: Configuración del Pedido ---
        JPanel panelTop = new JPanel(new GridLayout(2, 1));

        JPanel panelCliente = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtCliente = new JTextField(10);
        JRadioButton rbLocal = new JRadioButton("Local", true);
        JRadioButton rbExpress = new JRadioButton("Express");

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbLocal);
        bg.add(rbExpress);

        panelCliente.add(new JLabel("Cliente:"));
        panelCliente.add(txtCliente);
        panelCliente.add(rbLocal);
        panelCliente.add(rbExpress);

        JPanel panelDetallePedido = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtMesa = new JTextField(5);
        JTextField txtDireccion = new JTextField(15);

        txtDireccion.setEnabled(false);

        // Cambiar campos dependiendo del tipo de pedido
        rbLocal.addActionListener(e -> {
            txtMesa.setEnabled(true);
            txtDireccion.setEnabled(false);
        });

        rbExpress.addActionListener(e -> {
            txtMesa.setEnabled(false);
            txtDireccion.setEnabled(true);
        });

        panelDetallePedido.add(new JLabel("Mesa #:"));
        panelDetallePedido.add(txtMesa);
        panelDetallePedido.add(new JLabel("Dirección:"));
        panelDetallePedido.add(txtDireccion);

        JButton btnIniciar = new JButton("Iniciar Orden");

        btnIniciar.addActionListener(e -> {

            String cli = txtCliente.getText().trim().isEmpty()
                    ? "Genérico"
                    : txtCliente.getText().trim();

            if (rbLocal.isSelected()) {

                int mesa;

                try {
                    mesa = txtMesa.getText().trim().isEmpty()
                            ? 0
                            : Integer.parseInt(txtMesa.getText().trim());

                    if (mesa < 0) {
                        JOptionPane.showMessageDialog(
                                this, "El número de mesa no puede ser negativo."
                        );
                        return;
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            this, "Ingrese un número de mesa válido."
                    );
                    return;
                }

                pedidoActual = new PedidoLocal(cli, mesa);

                areaFactura.setText(
                        "Orden Local iniciada - Cliente: " + cli +
                                " - Mesa: " + mesa + "\n"
                );

            } else {

                pedidoActual = new PedidoExpress(
                        cli, txtDireccion.getText().trim()
                );

                areaFactura.setText(
                        "Orden Express iniciada - Cliente: " + cli +
                                "\nDir: " + txtDireccion.getText().trim() + "\n"
                );
            }

            nombreClienteActual = cli;
        });

        panelDetallePedido.add(btnIniciar);

        panelTop.add(panelCliente);
        panelTop.add(panelDetallePedido);

        // --- CENTRO: Productos ---
        JPanel panelCentro = new JPanel(new BorderLayout());
        JPanel panelBotones = new JPanel();

        comboProductos = new JComboBox<>();

        JButton btnActualizar = new JButton("Cargar Catálogo");

        // Carga el catálogo mediante SwingWorker
        btnActualizar.addActionListener(e -> {
            actualizarComboProductosEnHilo();
        });

        JButton btnAdd = new JButton("Añadir a Orden");

        btnAdd.addActionListener(e -> {

            if (pedidoActual == null) {
                JOptionPane.showMessageDialog(
                        this, "Inicie la orden primero"
                );
                return;
            }

            Producto p = (Producto) comboProductos.getSelectedItem();

            if (p != null) {

                // Validar la cantidad disponible en inventario
                long cantidadEnCarrito = pedidoActual.getCarrito()
                        .stream()
                        .filter(prod -> prod.getCodigo().equals(p.getCodigo()))
                        .count();

                if (cantidadEnCarrito >= p.getCantidadInventario()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No hay suficiente inventario de " + p.getNombre()
                    );
                    return;
                }

                pedidoActual.agregarProducto(p);

                areaFactura.append(
                        "> " + p.getNombre() +
                                " - ₡" + p.getPrecio() + "\n"
                );
            }
        });

        panelBotones.add(btnActualizar);
        panelBotones.add(comboProductos);
        panelBotones.add(btnAdd);

        areaFactura = new JTextArea(10, 30);
        areaFactura.setEditable(false);

        panelCentro.add(panelBotones, BorderLayout.NORTH);
        panelCentro.add(new JScrollPane(areaFactura), BorderLayout.CENTER);

        // --- SUR: Facturación y Pago ---
        JPanel panelSur = new JPanel();

        comboPago = new JComboBox<>(
                new String[]{"Efectivo", "Tarjeta", "SINPE"}
        );

        JButton btnPagar = new JButton("Facturar y Cobrar");

        btnPagar.addActionListener(e -> {

            if (pedidoActual == null || pedidoActual.getCarrito().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "El carrito está vacío"
                );
                return;
            }

            double total = pedidoActual.calcularTotal();
            String metodo = comboPago.getSelectedItem().toString();
            String mensajeVuelto = "";

            // Lógica de efectivo y vuelto
            if (metodo.equals("Efectivo")) {

                String input = JOptionPane.showInputDialog(
                        this,
                        "Total a pagar: ₡" + total +
                                "\nIngrese monto recibido:"
                );

                if (input == null || input.isEmpty()) {
                    return;
                }

                try {

                    double recibido = Double.parseDouble(input);

                    if (!Double.isFinite(recibido) || recibido < total) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Monto insuficiente o inválido.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    double vuelto = recibido - total;

                    mensajeVuelto =
                            "\nRecibido: ₡" + recibido +
                                    "\nVuelto: ₡" + vuelto;

                } catch (NumberFormatException ex) {

                    JOptionPane.showMessageDialog(
                            this, "Ingrese un monto válido."
                    );
                    return;
                }
            }

            // Reducir inventario por cada producto vendido
            for (Producto p : pedidoActual.getCarrito()) {
                p.setCantidadInventario(
                        p.getCantidadInventario() - 1
                );
            }

            // NOTA:
            // Cuando se integre MySQL, esta sección deberá
            // reemplazarse por el registro de venta mediante DAO.
            // El DAO también deberá devolver el consecutivo real.

            // Crear contenido de la factura
            String facturaFinal =
                    "--- FACTURA (" + metodo + ") ---\n" +
                            "Cliente: " + nombreClienteActual + "\n" +
                            "Subtotal: ₡" + pedidoActual.calcularSubtotal() + "\n" +
                            "IVA (13%): ₡" + pedidoActual.calcularImpuesto() + "\n" +
                            "Total a Pagar: ₡" + total + mensajeVuelto +
                            "\n\n¡Gracias por su compra!";

            // Mostrar factura en pantalla
            JOptionPane.showMessageDialog(
                    this,
                    facturaFinal,
                    "Factura",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // NUEVO: Generar archivo TXT de la factura
            GeneradorFactura.imprimirTicket(
                    facturaFinal,
                    nombreClienteActual
            );

            // Limpiar la orden
            pedidoActual = null;
            nombreClienteActual = null;
            areaFactura.setText("");

            // NUEVO: Recargar el catálogo en hilo secundario
            actualizarComboProductosEnHilo();
        });

        panelSur.add(new JLabel("Método de Pago:"));
        panelSur.add(comboPago);
        panelSur.add(btnPagar);

        add(panelTop, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);

        // NUEVO: Cargar productos al iniciar el panel
        actualizarComboProductosEnHilo();
    }

    /**
     * Obtiene los productos en un hilo secundario para
     * evitar bloquear la interfaz gráfica.
     */
    public void actualizarComboProductosEnHilo() {

        SwingWorker<List<Producto>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Producto> doInBackground() throws Exception {

                // Cuando se integre el DAO, esta llamada
                // deberá consultar MySQL.
                return inventario.obtenerTodos();
            }

            @Override
            protected void done() {

                try {

                    List<Producto> productos = get();

                    comboProductos.removeAllItems();

                    // Mostrar únicamente productos con stock
                    for (Producto p : productos) {

                        if (p.getCantidadInventario() > 0) {
                            comboProductos.addItem(p);
                        }
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            PanelVentas.this,
                            "Error al cargar los productos: " +
                                    ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }
}
