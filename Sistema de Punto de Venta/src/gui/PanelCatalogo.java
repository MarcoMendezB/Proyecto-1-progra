package gui;
import datos.Inventario;
import modelo.Producto;
import util.Validador;

import javax.swing.*;
import java.awt.*;
public class PanelCatalogo extends JPanel {
    private Inventario inventario;
    private JTextField txtCodigo, txtNombre, txtCategoria, txtPrecio, txtCantidad;

    public PanelCatalogo(Inventario inventario) {
        this.inventario = inventario;
        setLayout(new BorderLayout(10, 10));

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

        JButton btnGuardar = new JButton("Guardar Producto");
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 12));
        btnGuardar.addActionListener(e -> guardarProducto());

        add(panelForm, BorderLayout.CENTER);
        add(btnGuardar, BorderLayout.SOUTH);
    }
    private void guardarProducto() {
        if (txtCodigo.getText().isEmpty() || txtNombre.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Código y Nombre son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validador.esNumeroDouble(txtPrecio.getText())) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido (ej. 1500.0).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Validador.esNumeroInt(txtCantidad.getText())) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un entero válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Producto p = new Producto(
                txtCodigo.getText().trim(),
                txtNombre.getText().trim(),
                txtCategoria.getText().trim(),
                Double.parseDouble(txtPrecio.getText().trim()),
                Integer.parseInt(txtCantidad.getText().trim())
        );

        inventario.agregar(p);
        JOptionPane.showMessageDialog(this, "¡Producto agregado exitosamente al catálogo!", "Éxito", JOptionPane.INFORMATION_MESSAGE);

        txtCodigo.setText(""); txtNombre.setText("");
        txtCategoria.setText(""); txtPrecio.setText(""); txtCantidad.setText("");
    }

}
