package main;
import datos.Inventario;
import datos.Repositorio;
import datos.RepositorioPedidoMySQL;
import datos.RepositorioProductoMySQL;
import gui.PanelCatalogo;
import gui.PanelVentas;
import modelo.Producto;
import red.ClienteSincronizacion;
import red.ServidorSincronizacion;
import servicio.VentaService;
import util.ConfiguracionBD;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) {
        // Cargar conector MySQL dinámicamente si no está en el classpath
        cargarConectorMySQLSiNecesario();

        // Determinar modo de persistencia: MySQL si las credenciales están disponibles, CSV en caso contrario
        final boolean usarMySQL = detectarModoMySQL();

        // --- Punto extra: Sockets (opcional) ---
        boolean modoServidor = false;
        String hostCliente = null;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--servidor")) {
                modoServidor = true;
            } else if (arg.startsWith("--cliente=")) {
                hostCliente = arg.substring("--cliente=".length());
            }
        }

        final AtomicReference<PanelCatalogo> refPanel = new AtomicReference<>(null);
        final ServidorSincronizacion servidor;
        final ClienteSincronizacion clienteSync;

        if (modoServidor) {
            servidor = new ServidorSincronizacion(5901, msg -> {
                System.out.println("[Servidor] Mensaje recibido: " + msg);
                PanelCatalogo panel = refPanel.get();
                if (panel != null) {
                    panel.procesarMensajeRemoto(msg);
                }
            });
            servidor.iniciar();
        } else {
            servidor = null;
        }

        if (hostCliente != null) {
            clienteSync = new ClienteSincronizacion(hostCliente, 5901, msg -> {
                System.out.println("[Cliente] Mensaje recibido: " + msg);
                PanelCatalogo panel = refPanel.get();
                if (panel != null) {
                    panel.procesarMensajeRemoto(msg);
                }
            });
            clienteSync.conectar();
        } else {
            clienteSync = null;
        }

        SwingUtilities.invokeLater(() -> {
            Repositorio<Producto> repositorioProductos;
            RepositorioPedidoMySQL repoPedidos = null;

            if (usarMySQL) {
                repositorioProductos = new RepositorioProductoMySQL();
                repoPedidos = new RepositorioPedidoMySQL();
            } else {
                repositorioProductos = new Inventario();
            }

            VentaService ventaService = new VentaService(repositorioProductos, repoPedidos, clienteSync);

            JFrame frame = new JFrame("Sistema POS Soda/Pulpería - Marco, Angel & Jose");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(850, 550);
            frame.setLocationRelativeTo(null);

            JTabbedPane tabs = new JTabbedPane();
            PanelCatalogo panelCatalogo = new PanelCatalogo(repositorioProductos, clienteSync);
            refPanel.set(panelCatalogo);

            tabs.addTab("🛒 Toma de Pedidos y Facturación", new PanelVentas(ventaService));
            tabs.addTab("📦 Mantenimiento de Catálogo (CRUD)", panelCatalogo);

            frame.add(tabs);
            frame.setVisible(true);
        });
    }

    private static boolean detectarModoMySQL() {
        try {
            ConfiguracionBD.validarCredenciales();
            System.out.println("Modo MySQL activado.");
            return true;
        } catch (IllegalStateException ex) {
            System.out.println("Modo CSV (sin MySQL): " + ex.getMessage());
            return false;
        }
    }

    /**
     * Intenta cargar el conector MySQL desde la carpeta lib/ si no está
     * disponible en el classpath. Esto permite ejecutar el programa desde
     * un IDE o con java -cp "out" sin necesidad de añadir el JAR manualmente.
     */
    private static void cargarConectorMySQLSiNecesario() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Conector MySQL ya disponible en el classpath.");
            return;
        } catch (ClassNotFoundException e) {
            // Driver no encontrado en el classpath; intentar cargar desde lib/
            System.out.println("Conector no encontrado en classpath, buscando en lib/...");
        }

        // Buscar el conector en múltiples ubicaciones posibles
        java.io.File jarFile = null;
        String[] rutasPosibles = {
            "lib/mysql-connector-j-9.4.0.jar",
            "mysql-connector-j-9.4.0.jar",
            "Sistema de Punto de Venta/lib/mysql-connector-j-9.4.0.jar",
            "../lib/mysql-connector-j-9.4.0.jar"
        };
        for (String ruta : rutasPosibles) {
            java.io.File f = new java.io.File(ruta);
            if (f.exists()) {
                jarFile = f;
                break;
            }
        }

        // Si no se encontró en rutas relativas, buscar en el directorio del proyecto
        if (jarFile == null) {
            String userDir = System.getProperty("user.dir");
            System.out.println("Directorio de trabajo actual: " + userDir);
            // Intentar rutas absolutas basadas en el directorio de trabajo
            String[] rutasAbsolutas = {
                userDir + "/lib/mysql-connector-j-9.4.0.jar",
                userDir + "/Sistema de Punto de Venta/lib/mysql-connector-j-9.4.0.jar"
            };
            for (String ruta : rutasAbsolutas) {
                java.io.File f = new java.io.File(ruta);
                if (f.exists()) {
                    jarFile = f;
                    break;
                }
            }
        }

        if (jarFile == null) {
            System.out.println("No se encontró el conector MySQL en ninguna ubicación.");
            System.out.println("Asegúrate de que lib/mysql-connector-j-9.4.0.jar exista.");
            return; // No hay conector disponible
        }

        try {
            java.net.URL jarUrl = jarFile.toURI().toURL();
            java.net.URLClassLoader classLoader = new java.net.URLClassLoader(
                    new java.net.URL[]{jarUrl},
                    Main.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);
            Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver", true, classLoader);
            java.sql.Driver driverInstance = (java.sql.Driver) driverClass.getDeclaredConstructor().newInstance();
            util.GestorConexion.registrarDriver(driverInstance);
            java.sql.DriverManager.registerDriver(new java.sql.Driver() {
                @Override
                public java.sql.Connection connect(String url, java.util.Properties info) throws java.sql.SQLException {
                    return driverInstance.connect(url, info);
                }
                @Override
                public boolean acceptsURL(String url) throws java.sql.SQLException {
                    return driverInstance.acceptsURL(url);
                }
                @Override
                public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, java.util.Properties info) throws java.sql.SQLException {
                    return driverInstance.getPropertyInfo(url, info);
                }
                @Override
                public int getMajorVersion() { return driverInstance.getMajorVersion(); }
                @Override
                public int getMinorVersion() { return driverInstance.getMinorVersion(); }
                @Override
                public boolean jdbcCompliant() { return driverInstance.jdbcCompliant(); }
                @Override
                public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
                    return driverInstance.getParentLogger();
                }
            });
            System.out.println("Conector MySQL cargado dinámicamente desde: " + jarFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("No se pudo cargar el conector MySQL dinámicamente: " + e.getMessage());
        }
    }
}
