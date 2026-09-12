package gui;

import datos.Inventario;
import modelo.Producto;
import util.Validador;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelCatalogo extends JPanel {
    private Inventario inventario;
    private JTextField txtCodigo, txtNombre, txtCategoria, txtPrecio, txtCantidad;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;

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

        add(panelIzquierdo, BorderLayout.WEST);
        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        // --- ACCIONES DE LOS BOTONES ---
        btnAgregar.addActionListener(e -> {
            if (validarCampos()) {
                inventario.agregar(crearProducto());
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
    }

    // --- MÉTODOS DE APOYO ---

    // Este es el método que el Main está buscando y causaba los errores
    public void actualizarTabla() {
        modeloTabla.setRowCount(0); // Limpia las filas actuales
        for (Producto p : inventario.obtenerTodos()) {
            modeloTabla.addRow(new Object[]{
                    p.getCodigo(), p.getNombre(), p.getCategoria(), p.getPrecio(), p.getCantidadInventario()
            });
        }
    }

    private void limpiarYActualizar() {
        txtCodigo.setText(""); txtNombre.setText(""); txtCategoria.setText("");
        txtPrecio.setText(""); txtCantidad.setText("");
        txtCodigo.setEditable(true);
        actualizarTabla();
    }

    private boolean validarCampos() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Código y Nombre son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
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