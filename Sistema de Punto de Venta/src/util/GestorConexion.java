package util;

import java.sql.*;

/**
 * Gestor centralizado de conexiones a MySQL.
 * Permite registrar un driver cargado dinámicamente (desde lib/)
 * y obtener conexiones sin depender de DriverManager global.
 */
public class GestorConexion {

    private static Driver driverMySQL = null;

    /**
     * Registra un driver MySQL cargado dinámicamente.
     */
    public static void registrarDriver(Driver driver) {
        driverMySQL = driver;
    }

    /**
     * Obtiene una conexión a MySQL usando el driver registrado
     * (si existe) o DriverManager como fallback.
     */
    public static Connection obtenerConexion(String url, String usuario, String clave) throws SQLException {
        if (driverMySQL != null) {
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", usuario);
            props.setProperty("password", clave);
            return driverMySQL.connect(url, props);
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    /**
     * Indica si hay un driver MySQL registrado dinámicamente.
     */
    public static boolean tieneDriver() {
        return driverMySQL != null;
    }
}