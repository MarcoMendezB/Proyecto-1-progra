package red;

/**
 * Tipos de mensaje intercambiados entre instancias del POS mediante Sockets.
 * Permite sincronizar inventario y ventas entre dos o más aplicaciones.
 */
public final class ProtocoloSincronizacion {

    // Mensajes de control
    public static final String HANDSHAKE = "HANDSHAKE";
    public static final String HANDSHAKE_ACK = "HANDSHAKE_ACK";
    public static final String DESCONECTAR = "DESCONECTAR";

    // Sincronización de productos (catálogo)
    public static final String PRODUCTO_INSERTADO = "PRODUCTO_INSERTADO";
    public static final String PRODUCTO_ACTUALIZADO = "PRODUCTO_ACTUALIZADO";
    public static final String PRODUCTO_ELIMINADO = "PRODUCTO_ELIMINADO";
    public static final String STOCK_DECREMENTADO = "STOCK_DECREMENTADO";

    // Sincronización de ventas
    public static final String VENTA_REGISTRADA = "VENTA_REGISTRADA";

    private ProtocoloSincronizacion() {
        // Clase de utilidad, no se instancia
    }
}