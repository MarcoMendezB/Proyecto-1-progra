package gui;
import datos.Repositorio;
import modelo.Producto;
import util.Validador;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PanelCatalogo extends JPanel {
    private Repositorio<Producto> repositorio;
    private JTextField txtCodigo, txtNombre, txtCategoria, txtPrecio, txtCantidad;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private JButton btnBuscar;

    public PanelCatalogo(Repositorio<Producto> repositorio) {
        this.repositorio = repositorio;
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
                try {
                    repositorio.insertar(crearProducto());
                    limpiarYActualizar();
                    JOptionPane.showMessageDialog(this, "Producto agregado exitosamente.");
                } catch (Exception ex) {
                    mostrarErrorBD("agregar el producto", ex);
                }
            }
        });

        btnActualizar.addActionListener(e -> {
            if (validarCampos()) {
                try {
                    repositorio.actualizar(crearProducto());
                    limpiarYActualizar();
                    JOptionPane.showMessageDialog(this, "Producto actualizado.");
                } catch (Exception ex) {
                    mostrarErrorBD("actualizar el producto", ex);
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            if (!txtCodigo.getText().isEmpty()) {
                try {
                    repositorio.eliminar(txtCodigo.getText());
                    limpiarYActualizar();
                    JOptionPane.showMessageDialog(this, "Producto eliminado.");
                } catch (Exception ex) {
                    mostrarErrorBD("eliminar el producto", ex);
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
                List<Producto> todos = repositorio.obtenerTodos();

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
                    mostrarErrorBD("cargar el catálogo", ex);
                }
            }
        };
        worker.execute();
    }

    // Muestra un mensaje claro al usuario cuando la base de datos falla.
    // El formulario NO se limpia, para que el usuario pueda corregir el dato.
    private void mostrarErrorBD(String accion, Exception ex) {
        JOptionPane.showMessageDialog(this,
                "No se pudo " + accion + ".\n" + traducirErrorBD(ex),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }

    // Convierte un error de MySQL en un texto sencillo.
    // Busca el SQLException que viene escondido dentro de otras excepciones.
    static String traducirErrorBD(Throwable ex) {
        SQLException sql = null;
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof SQLException se) { sql = se; break; }
        }
        if (sql == null) {
            return ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado.";
        }

        String estado = sql.getSQLState() == null ? "" : sql.getSQLState();
        switch (sql.getErrorCode()) {
            case 1062: return "Ya existe un producto con ese código.";
            case 1451: return "No se puede eliminar: el producto ya tiene ventas registradas.";
            case 1045: return "Usuario o contraseña de MySQL incorrectos.";
            case 1370: case 1142: return "El usuario de la aplicación no tiene permiso para esta acción.";
        }
        if (estado.equals("45000")) {
            return sql.getMessage(); // Mensaje escrito por Marco en el procedimiento almacenado
        }
        if (estado.startsWith("08")) {
            return "No hay conexión con la base de datos. Revise que MySQL esté encendido.";
        }
        return "Error de MySQL: " + sql.getMessage();
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