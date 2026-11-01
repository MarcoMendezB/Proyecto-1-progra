package gui;
import datos.Repositorio;
import modelo.Producto;
import red.ClienteSincronizacion;
import red.MensajeSincronizacion;
import red.ProtocoloSincronizacion;
import util.Validador;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelCatalogo extends JPanel {
    private Repositorio<Producto> repositorio;
    private ClienteSincronizacion clienteSync;
    private JTextField txtCodigo, txtNombre, txtCategoria, txtPrecio, txtCantidad;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnBuscar;

    public PanelCatalogo(Repositorio<Producto> repositorio) {
        this(repositorio, null);
    }

    public PanelCatalogo(Repositorio<Producto> repositorio, ClienteSincronizacion clienteSync) {
        this.repositorio = repositorio;
        this.clienteSync = clienteSync;
        setLayout(new BorderLayout(10, 10));

        // --- PANEL IZQUIERDO: FORMULARIO Y BOTONES ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout());

        JPanel panelForm = new JPanel(new GridLayout(5, 2, 8, 8));
        panelForm.setBorder(BorderFactory.createTitledBorder("Mantenimiento de Productos (CRUD)"));

        txtCodigo = new JTextField();
        txtNombre = new JTextField();
        txtCategoria = new JTextField();
        txtPrecio = new JTextField();
        txtCantidad = new JTextField();

        panelForm.add(new JLabel("Código:")); panelForm.add(txtCodigo);
        panelForm.add(new JLabel("Nombre:")); panelForm.add(txtNombre);
        panelForm.add(new JLabel("Categoría:")); panelForm.add(txtCategoria);
        panelForm.add(new JLabel("Precio (₡):")); panelForm.add(txtPrecio);
        panelForm.add(new JLabel("Cantidad Stock:")); panelForm.add(txtCantidad);

        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);

        panelIzquierdo.add(panelForm, BorderLayout.CENTER);
        panelIzquierdo.add(panelBotones, BorderLayout.SOUTH);

        // --- PANEL DERECHO: TABLA DE PRODUCTOS ---
        String[] columnas = {"Código", "Nombre", "Categoría", "Precio", "Stock"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        tablaProductos = new JTable(modeloTabla);

        // Listener: Al hacer clic en una fila de la tabla, los datos pasan al formulario
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila >= 0) {
                Object v0 = modeloTabla.getValueAt(fila, 0);
                Object v1 = modeloTabla.getValueAt(fila, 1);
                Object v2 = modeloTabla.getValueAt(fila, 2);
                Object v3 = modeloTabla.getValueAt(fila, 3);
                Object v4 = modeloTabla.getValueAt(fila, 4);
                txtCodigo.setText(v0 != null ? v0.toString() : "");
                txtNombre.setText(v1 != null ? v1.toString() : "");
                txtCategoria.setText(v2 != null ? v2.toString() : "");
                txtPrecio.setText(v3 != null ? v3.toString() : "");
                txtCantidad.setText(v4 != null ? v4.toString() : "");
                txtCodigo.setEditable(false); // No permitir cambiar el código al actualizar
            }
        });

        // --- BARRA DE BUSQUEDA (arriba de la tabla) ---
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        txtBuscar = new JTextField(15);
        btnBuscar = new JButton("🔍 Buscar");
        panelBusqueda.add(new JLabel("Filtrar por Nombre/Categoría: "));
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(panelBusqueda, BorderLayout.NORTH);
        panelDerecho.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        add(panelIzquierdo, BorderLayout.WEST);
        add(panelDerecho, BorderLayout.CENTER);

        // --- ACCIONES DE LOS BOTONES ---
        btnAgregar.addActionListener(e -> {
            if (validarCampos()) {
                try {
                    Producto p = crearProducto();
                    repositorio.insertar(p);
                    limpiarYActualizar();
                    JOptionPane.showMessageDialog(this, "Producto agregado exitosamente.");
                    // Notificar a la otra instancia vía Socket (punto extra)
                    if (clienteSync != null) {
                        String contenido = "codigo=" + p.getCodigo() + ";nombre=" + p.getNombre()
                                + ";categoria=" + p.getCategoria() + ";precio=" + p.getPrecio()
                                + ";stock=" + p.getCantidadInventario();
                        clienteSync.enviar(new MensajeSincronizacion(ProtocoloSincronizacion.PRODUCTO_INSERTADO, contenido));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al agregar el producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnActualizar.addActionListener(e -> {
            if (validarCampos()) {
                try {
                    Producto p = crearProducto();
                    repositorio.actualizar(p);
                    limpiarYActualizar();
                    JOptionPane.showMessageDialog(this, "Producto actualizado.");
                    // Notificar a la otra instancia vía Socket (punto extra)
                    if (clienteSync != null) {
                        String contenido = "codigo=" + p.getCodigo() + ";nombre=" + p.getNombre()
                                + ";categoria=" + p.getCategoria() + ";precio=" + p.getPrecio()
                                + ";stock=" + p.getCantidadInventario();
                        clienteSync.enviar(new MensajeSincronizacion(ProtocoloSincronizacion.PRODUCTO_ACTUALIZADO, contenido));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al actualizar el producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            if (!txtCodigo.getText().isEmpty()) {
                try {
                    String codigoEliminar = txtCodigo.getText();
                    repositorio.eliminar(codigoEliminar);
                    limpiarYActualizar();
                    JOptionPane.showMessageDialog(this, "Producto eliminado.");
                    // Notificar a la otra instancia vía Socket (punto extra)
                    if (clienteSync != null) {
                        clienteSync.enviar(new MensajeSincronizacion(ProtocoloSincronizacion.PRODUCTO_ELIMINADO, "codigo=" + codigoEliminar));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al eliminar el producto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Buscar con el boton o presionando Enter
        btnBuscar.addActionListener(e -> cargarTablaEnHilo(txtBuscar.getText().trim()));
        txtBuscar.addActionListener(e -> cargarTablaEnHilo(txtBuscar.getText().trim()));

        cargarTablaEnHilo(""); // Carga inicial sin filtro
    }

    // --- MÉTODOS DE APOYO ---

    // Carga la tabla en un hilo aparte para no congelar la pantalla.
    // Si "filtro" trae texto, solo se muestran los productos cuyo nombre o categoria lo contienen.
    public void cargarTablaEnHilo(String filtro) {
        SwingWorker<List<Producto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                // Se llama al repositorio (puede ser lento) fuera del hilo de la pantalla
                List<Producto> todos = repositorio == null ? null : repositorio.obtenerTodos();
                if (todos == null) {
                    todos = new ArrayList<>();
                }

                if (filtro == null || filtro.trim().isEmpty()) {
                    // Filtrar productos nulos por seguridad
                    return todos.stream().filter(p -> p != null).toList();
                }
                String f = filtro.toLowerCase();
                return todos.stream()
                        .filter(p -> p != null)
                        .filter(p -> {
                            String nombre = p.getNombre();
                            String categoria = p.getCategoria();
                            boolean matchNombre = nombre != null && nombre.toLowerCase().contains(f);
                            boolean matchCategoria = categoria != null && categoria.toLowerCase().contains(f);
                            return matchNombre || matchCategoria;
                        })
                        .toList();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> datos = get();
                    if (datos == null) {
                        datos = new ArrayList<>();
                    }
                    modeloTabla.setRowCount(0); // Limpia las filas actuales
                    for (Producto p : datos) {
                        if (p == null) continue;
                        modeloTabla.addRow(new Object[]{
                                p.getCodigo() == null ? "" : p.getCodigo(),
                                p.getNombre() == null ? "" : p.getNombre(),
                                p.getCategoria() == null ? "" : p.getCategoria(),
                                p.getPrecio(),
                                p.getCantidadInventario()
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PanelCatalogo.this, "Error al cargar el catálogo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Muestra un mensaje claro al usuario cuando la base de datos falla.
    private void limpiarYActualizar() {
        txtCodigo.setText(""); txtNombre.setText(""); txtCategoria.setText("");
        txtPrecio.setText(""); txtCantidad.setText("");
        txtCodigo.setEditable(true);
        txtBuscar.setText("");
        cargarTablaEnHilo("");
    }

    private boolean validarCampos() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Código y Nombre son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!Validador.esTextoSeguro(txtCodigo.getText())
                || !Validador.esTextoSeguro(txtNombre.getText())
                || !Validador.esTextoSeguro(txtCategoria.getText())) {
            JOptionPane.showMessageDialog(this,
                    "Código, nombre y categoría solo admiten letras, números, espacios, puntos y guiones.",
                    "Texto no permitido", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!Validador.esNumeroDouble(txtPrecio.getText())) {
            JOptionPane.showMessageDialog(this, "El precio debe ser numérico.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!Validador.esNumeroInt(txtCantidad.getText())) {
            JOptionPane.showMessageDialog(this, "El stock debe ser un número entero.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private Producto crearProducto() {
        return new Producto(
                txtCodigo.getText().trim(),
                txtNombre.getText().trim(),
                txtCategoria.getText().trim(),
                Double.parseDouble(txtPrecio.getText().trim()),
                Integer.parseInt(txtCantidad.getText().trim())
        );
    }

    /**
     * Procesa un mensaje recibido por Socket de la otra instancia y lo refleja
     * localmente en la tabla de productos. Se ejecuta de forma segura en el hilo
     * de la interfaz gráfica (EDT).
     */
    public void procesarMensajeRemoto(MensajeSincronizacion mensaje) {
        if (mensaje == null || mensaje.getTipo() == null) return;
        SwingUtilities.invokeLater(() -> {
            switch (mensaje.getTipo()) {
                case ProtocoloSincronizacion.PRODUCTO_INSERTADO:
                case ProtocoloSincronizacion.PRODUCTO_ACTUALIZADO:
                case ProtocoloSincronizacion.PRODUCTO_ELIMINADO:
                case ProtocoloSincronizacion.VENTA_REGISTRADA:
                    // Recargar la tabla para reflejar cambios provenientes de la otra instancia
                    cargarTablaEnHilo("");
                    break;
                default:
                    // Ignorar tipos de mensaje desconocidos
                    break;
            }
        });
    }
}