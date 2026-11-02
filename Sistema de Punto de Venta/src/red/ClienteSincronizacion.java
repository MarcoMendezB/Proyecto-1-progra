package red;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Cliente de sincronización por Socket.
 * Se conecta a un servidor remoto, envía mensajes y escucha respuestas en segundo plano.
 * Diseñado para ejecutarse sin bloquear la interfaz gráfica.
 */
public class ClienteSincronizacion {

    private final String host;
    private final int puerto;
    private final ServidorSincronizacion.ListenerMensajes listener;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean conectado = false;
    private Thread hiloLectura;

    public ClienteSincronizacion(String host, int puerto, ServidorSincronizacion.ListenerMensajes listener) {
        this.host = host;
        this.puerto = puerto;
        this.listener = listener;
    }

    /**
     * Intenta conectar al servidor. Retorna true si tuvo éxito.
     */
    public boolean conectar() {
        try {
            socket = new Socket(host, puerto);
            socket.setSoTimeout(0);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            conectado = true;
            System.out.println("[Cliente] Conectado a " + host + ":" + puerto);

            hiloLectura = new Thread(() -> {
                try {
                    String linea;
                    while (conectado && (linea = in.readLine()) != null) {
                        MensajeSincronizacion msg = MensajeSincronizacion.deserializar(linea);
                        if (msg != null) {
                            System.out.println("[Cliente] Recibido: " + msg);
                            if (listener != null) {
                                listener.onMensajeRecibido(msg);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (conectado) {
                        System.err.println("[Cliente] Conexión cerrada: " + e.getMessage());
                    }
                } finally {
                    desconectar();
                }
            }, "Cliente-Lectura");
            hiloLectura.start();

            return true;
        } catch (IOException e) {
            System.err.println("[Cliente] No se pudo conectar a " + host + ":" + puerto + " -> " + e.getMessage());
            conectado = false;
            return false;
        }
    }

    /**
     * Envía un mensaje al servidor. Retorna false si no hay conexión activa.
     */
    public boolean enviar(MensajeSincronizacion mensaje) {
        if (!conectado || out == null) {
            return false;
        }
        out.println(mensaje.serializar());
        return true;
    }

    public boolean isConectado() {
        return conectado;
    }

    /**
     * Cierra la conexión con el servidor.
     */
    public void desconectar() {
        conectado = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }
}