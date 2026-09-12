package main;
import datos.Inventario;
import gui.PanelCatalogo;
import gui.PanelVentas;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Instancia central de base de datos en memoria
            Inventario inventarioCompartido = new Inventario();

            JFrame frame = new JFrame("Sistema POS Soda/Pulpería - Marco, Angel & Jose");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(850, 550);
            frame.setLocationRelativeTo(null);

            // Pestañas principales
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("🛒 Toma de Pedidos y Facturación", new PanelVentas(inventarioCompartido));
            tabs.addTab("📦 Mantenimiento de Catálogo (CRUD)", new PanelCatalogo(inventarioCompartido));

            frame.add(tabs);
            frame.setVisible(true);
        });
    }
}
