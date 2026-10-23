package util;

public class Validador {


    public static boolean esNumeroDouble(String str) {
        if (str == null || str.trim().isEmpty()) {
            System.out.println("Error: el campo está vacío.");
            return false;
        }
        try {
            double val = Double.parseDouble(str.trim());
            if (val < 0) {
                System.out.println("Error: el número no puede ser negativo.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Error: '" + str + "' no es un número válido. (" + e.getMessage() + ")");
            return false;
        }
    }

    public static boolean esNumeroInt(String str) {
        if (str == null || str.trim().isEmpty()) {
            System.out.println("Error: el campo está vacío.");
            return false;
        }
        try {
            int val = Integer.parseInt(str.trim());
            if (val < 0) {
                System.out.println("Error: el número no puede ser negativo.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Error: '" + str + "' no es un número válido. (" + e.getMessage() + ")");
            return false;
        }
    }

    /**
     * Valida que el texto no contenga caracteres especiales de SQL como comillas simples,
     * punto y coma o guiones dobles que puedan ser usados en un ataque (aunque MySQL
     * ya esté protegido por CallableStatement).
     * Permite letras (con tildes y ñ/ü), números, espacios, puntos y guiones simples.
     */
    public static boolean esTextoSeguro(String str) {
        if (str == null || str.trim().isEmpty()) {
            System.out.println("Error: el campo de texto está vacío.");
            return false;
        }
        // Bloquea ', ", ;, /*, etc. Se bloquea también "--" (comentario SQL)
        if (!str.matches("^[a-zA-Z0-9 áéíóúüÁÉÍÓÚÜñÑ.-]+$") || str.contains("--")) {
            System.out.println("Error: '" + str + "' contiene caracteres no permitidos.");
            return false;
        }
        return true;
    }
}