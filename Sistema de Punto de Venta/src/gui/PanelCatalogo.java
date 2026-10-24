package gui;
import datos.Inventario;
import modelo.Producto;
import util.Validador;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelCatalogo extends JPanel {
    private Inventario inventario;
    private JTextField txtCodigo, txtNombre, txtCategoria, txtPrecio, txtCantidad;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnBuscar;

    public PanelCatalogo(Inventario inventario) {
        this.inventario = inventario;
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
                txtCodigo.setText(modeloTabla.getValueAt(fila, 0).toString());
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtCategoria.setText(modeloTabla.getValueAt(fila, 2).toString());
                txtPrecio.setText(modeloTabla.getValueAt(fila, 3).toString());
                txtCantidad.setText(modeloTabla.getValueAt(fila, 4).toString());
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
                inventario.insertar(crearProducto());
                limpiarYActualizar();
                JOptionPane.showMessageDialog(this, "Producto agregado exitosamente.");
            }
        });

        btnActualizar.addActionListener(e -> {
            if (validarCampos()) {
                inventario.actualizar(crearProducto());
                limpiarYActualizar();
                JOptionPane.showMessageDialog(this, "Producto actualizado.");
            }
        });

        btnEliminar.addActionListener(e -> {
            if (!txtCodigo.getText().isEmpty()) {
                inventario.eliminar(txtCodigo.getText());
                limpiarYActualizar();
                JOptionPane.showMessageDialog(this, "Producto eliminado.");
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
                List<Producto> todos = inventario.obtenerTodos();

                if (filtro == null || filtro.isEmpty()) {
                    return todos;
                }
                String f = filtro.toLowerCase();
                return todos.stream()
                        .filter(p -> p.getNombre().toLowerCase().contains(f) ||
                                p.getCategoria().toLowerCase().contains(f))
                        .toList();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> datos = get();
                    modeloTabla.setRowCount(0); // Limpia las filas actuales
                    for (Producto p : datos) {
                        modeloTabla.addRow(new Object[]{
                                p.getCodigo(), p.getNombre(), p.getCategoria(),
                                p.getPrecio(), p.getCantidadInventario()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelCatalogo.this,
                            "Error al cargar el catálogo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

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
}