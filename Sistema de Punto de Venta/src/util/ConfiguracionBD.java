package util;

/**
 * Gestiona las credenciales de la base de datos
 * mediante variables de entorno.
 */
public class ConfiguracionBD {

    /**
     * Obtiene una variable de entorno.
     * Si no existe o está vacía, devuelve el valor predeterminado.
     */
    public static String obtenerVariable(String nombre, String valorPredeterminado) {
        String valor = System.getenv(nombre);

        return (valor == null || valor.isBlank())
                ? valorPredeterminado
                : valor;
    }

    /**
     * Comprueba que las credenciales de MySQL estén configuradas.
     */
    public static void validarCredenciales() {
        String url = obtenerVariable("DB_SODA_URL", "jdbc:mysql://localhost:3306/soda_pos?serverTimezone=UTC");
        String usuario = obtenerVariable("DB_SODA_USER", null);
        String clave = obtenerVariable("DB_SODA_PASSWORD", null);

        // Verificar que los valores no sean vacíos
        if (url == null || url.isBlank() ||
            usuario == null || usuario.isBlank() ||
            clave == null || clave.isBlank()) {
            throw new IllegalStateException(
                    "Credenciales de MySQL incompletas. " +
                            "Configure DB_SODA_URL, DB_SODA_USER y DB_SODA_PASSWORD (no use valores por defecto en producción).");
        }

        // Intentar una conexión rápida para confirmar que MySQL está disponible
        try (java.sql.Connection conn = util.GestorConexion.obtenerConexion(url, usuario, clave)) {
            // Conexión exitosa
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException(
                    "No se pudo conectar a MySQL: " + e.getMessage());
        }
    }
}