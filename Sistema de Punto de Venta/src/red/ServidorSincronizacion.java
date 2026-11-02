package red;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Servidor de sincronización por Socket.
 * Escucha conexiones entrantes y difunde mensajes a todos los clientes conectados.
 * Diseñado para ejecutarse en segundo plano sin bloquear la interfaz gráfica.
 */
public class ServidorSincronizacion {

    private final int puerto;
    private final ExecutorService hiloAceptador = Executors.newSingleThreadExecutor();
    private final Set<PrintWriter> clientes = ConcurrentHashMap.newKeySet();
    private ServerSocket servidorSocket;
    private volatile boolean ejecutando = false;
    private final ListenerMensajes listener;

    /**
     * @param puerto Puerto TCP en el que escucha el servidor.
     * @param listener Callback para procesar mensajes recibidos de los clientes.
     */
    public ServidorSincronizacion(int puerto, ListenerMensajes listener) {
        this.puerto = puerto;
        this.listener = listener;
    }

    public interface ListenerMensajes {
        void onMensajeRecibido(MensajeSincronizacion mensaje);
    }

    /**
     * Inicia el servidor en un hilo daemon.
     */
    public void iniciar() {
        hiloAceptador.execute(() -> {
            try {
                servidorSocket = new ServerSocket(puerto);
                servidorSocket.setSoTimeout(1000);
                ejecutando = true;
                System.out.println("[Servidor] Escuchando en puerto " + puerto);

                while (ejecutando) {
                    try {
                        Socket cliente = servidorSocket.accept();
                        PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                        clientes.add(out);

                        // Hilo dedicado para leer mensajes de este cliente
                        new Thread(() -> {
                            try {
                                String linea;
                                while ((linea = in.readLine()) != null) {
                                    MensajeSincronizacion msg = MensajeSincronizacion.deserializar(linea);
                                    if (msg != null) {
                                        System.out.println("[Servidor] Recibido: " + msg);
                                        if (listener != null) {
                                            listener.onMensajeRecibido(msg);
                                        }
                                        // Reenviar a los demás clientes (difusión)
                                        difundir(msg, out);
                                    }
                                }
                            } catch (IOException e) {
                                // Cliente desconectado
                            } finally {
                                clientes.remove(out);
                                try {
                                    cliente.close();
                                } catch (IOException ignored) {}
                            }
                        }, "Lector-Cliente-" + cliente.getInetAddress()).start();

                    } catch (SocketTimeoutException e) {
                        // Timeout normal para revisar la bandera ejecutando
                    }
                }
            } catch (IOException e) {
                System.err.println("[Servidor] Error al iniciar: " + e.getMessage());
            }
        });
    }

    /**
     * Detiene el servidor y cierra todas las conexiones.
     */
    public void detener() {
        ejecutando = false;
        try {
            if (servidorSocket != null && !servidorSocket.isClosed()) {
                servidorSocket.close();
            }
        } catch (IOException ignored) {}
        for (PrintWriter out : clientes) {
            out.close();
        }
        clientes.clear();
        hiloAceptador.shutdownNow();
    }

    /**
     * Envía un mensaje a todos los clientes conectados excepto al remitente.
     */
    private void difundir(MensajeSincronizacion mensaje, PrintWriter remitente) {
        String texto = mensaje.serializar();
        for (PrintWriter out : clientes) {
            if (out != remitente) {
                out.println(texto);
            }
        }
    }

    /**
     * Envía un mensaje a todos los clientes conectados.
     */
    public void difundirAMinistros(MensajeSincronizacion mensaje) {
        String texto = mensaje.serializar();
        for (PrintWriter out : clientes) {
            out.println(texto);
        }
    }

    public boolean isEjecutando() {
        return ejecutando;
    }

    public int getClientesConectados() {
        return clientes.size();
    }
}