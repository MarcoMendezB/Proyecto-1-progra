
package util;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GeneradorFactura {

    public static void imprimirTicket(String contenidoFactura, String nombreCliente) {

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        String nombreSeguro = nombreCliente.replaceAll(
                "[^a-zA-Z0-9_-]", "_"
        );

        String nombreArchivo = "Factura_" + nombreSeguro + "_" + timestamp + ".txt";

        try (PrintWriter pw = new PrintWriter(new FileWriter(nombreArchivo))) {

            pw.println("====== SODA POS ======");
            pw.println("Fecha: " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            ));

            pw.println(contenidoFactura);
            pw.println("======================");

        } catch (Exception e) {
            System.err.println(
                    "Error al generar el archivo de la factura: " + e.getMessage()
            );
        }
    }
}
