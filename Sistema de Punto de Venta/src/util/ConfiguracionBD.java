
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
        String usuario = obtenerVariable("DB_USER", null);
        String clave = obtenerVariable("DB_PASSWORD", null);

        if (usuario == null || clave == null) {
            throw new IllegalStateException(
                    "Faltan credenciales de MySQL. " +
                            "Configure DB_USER y DB_PASSWORD " +
                            "en las variables de entorno de IntelliJ."
            );
        }
    }
}
