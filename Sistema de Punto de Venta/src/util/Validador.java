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
}
