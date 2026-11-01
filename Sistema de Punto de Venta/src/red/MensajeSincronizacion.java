package red;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa un mensaje de sincronización enviado por Socket.
 * Formato de transmisión: TIPO|campo1=valor1;campo2=valor2|timestamp
 */
public class MensajeSincronizacion {

    private final String tipo;
    private final String contenido;
    private final String timestamp;

    public MensajeSincronizacion(String tipo, String contenido) {
        this.tipo = tipo;
        this.contenido = contenido;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getTipo() {
        return tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Serializa el mensaje a una línea de texto para enviar por Socket.
     */
    public String serializar() {
        return tipo + "|" + contenido + "|" + timestamp;
    }

    /**
     * Deserializa una línea de texto recibida por Socket a un MensajeSincronizacion.
     * Retorna null si el formato es inválido.
     */
    public static MensajeSincronizacion deserializar(String linea) {
        if (linea == null || linea.isBlank()) {
            return null;
        }
        String[] partes = linea.split("\\|", 3);
        if (partes.length < 2) {
            return null;
        }
        String tipo = partes[0];
        String contenido = partes[1];
        String timestamp = (partes.length >= 3) ? partes[2] : "";
        return new MensajeSincronizacion(tipo, contenido) {
            @Override
            public String getTimestamp() {
                return timestamp;
            }
        };
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + tipo + ": " + contenido;
    }
}