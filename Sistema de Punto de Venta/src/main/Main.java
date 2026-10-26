
package main;

import datos.RepositorioProductoMySQL;
import datos.RepositorioPedidoMySQL;
import gui.PanelCatalogo;
import gui.PanelVentas;
import util.ConfiguracionBD;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // Validar las credenciales de MySQL
        ConfiguracionBD.validarCredenciales();

        SwingUtilities.invokeLater(() -> {

            // Repositorio de productos conectado a MySQL
            RepositorioProductoMySQL inventarioCompartido =
                    new RepositorioProductoMySQL();

            // Repositorio encargado de registrar los pedidos
            RepositorioPedidoMySQL repoPedidos =
                    new RepositorioPedidoMySQL();

            // Ventana principal
            JFrame frame = new JFrame(
                    "Sistema POS Soda/Pulpería - Marco, Angel & Jose"
            );

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(850, 550);
            frame.setLocationRelativeTo(null);

            // Pestañas del sistema
            JTabbedPane tabs = new JTabbedPane();

            tabs.addTab(
                    "🛒 Toma de Pedidos y Facturación",
                    new PanelVentas(inventarioCompartido, repoPedidos)
            );

            tabs.addTab(
                    "📦 Mantenimiento de Catálogo (CRUD)",
                    new PanelCatalogo(inventarioCompartido)
            );

            frame.add(tabs);
            frame.setVisible(true);
        });
    }
}
