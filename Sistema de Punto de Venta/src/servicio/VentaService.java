package servicio;

import datos.Repositorio;
import datos.RepositorioPedidoMySQL;
import modelo.Pedido;
import modelo.Producto;
import red.ClienteSincronizacion;
import red.MensajeSincronizacion;
import red.ProtocoloSincronizacion;
import util.GeneradorFactura;

public class VentaService {

    private final Repositorio<Producto> repositorioProductos;
    private final RepositorioPedidoMySQL repoPedidos;
    private final ClienteSincronizacion clienteSync;

    public VentaService(Repositorio<Producto> repositorioProductos, RepositorioPedidoMySQL repoPedidos) {
        this(repositorioProductos, repoPedidos, null);
    }

    public VentaService(Repositorio<Producto> repositorioProductos, RepositorioPedidoMySQL repoPedidos,
                        ClienteSincronizacion clienteSync) {
        this.repositorioProductos = repositorioProductos;
        this.repoPedidos = repoPedidos;
        this.clienteSync = clienteSync;
    }

    public double calcularSubtotal(Pedido pedido) {
        return pedido.calcularSubtotal();
    }

    public double calcularImpuesto(Pedido pedido) {
        return pedido.calcularImpuesto();
    }

    public double calcularTotal(Pedido pedido) {
        return pedido.calcularTotal();
    }

    public void registrarVenta(Pedido pedido, String nombreCliente) {
        if (pedido == null) return;
        // Decrementar stock de cada producto antes de registrar la venta
        for (Producto p : pedido.getCarrito()) {
            if (p == null) continue;
            String codigo = p.getCodigo();
            if (codigo != null && !codigo.isEmpty()) {
                repositorioProductos.decrementarStock(codigo);
            }
        }
        if (repoPedidos != null) {
            repoPedidos.registrarVenta(pedido);
        }

        String factura = "--- FACTURA ---\n"
                + "Cliente: " + nombreCliente + "\n"
                + "Subtotal: ₡" + calcularSubtotal(pedido) + "\n"
                + "IVA (13%): ₡" + calcularImpuesto(pedido) + "\n"
                + "Total a Pagar: ₡" + calcularTotal(pedido) + "\n"
                + "\n¡Gracias por su compra!";
        GeneradorFactura.imprimirTicket(factura, nombreCliente);

        // Notificar a la otra instancia vía Socket (punto extra)
        if (clienteSync != null) {
            String contenido = "cliente=" + nombreCliente + ";total=" + calcularTotal(pedido);
            clienteSync.enviar(new MensajeSincronizacion(ProtocoloSincronizacion.VENTA_REGISTRADA, contenido));
        }
    }

    public Repositorio<Producto> getRepositorioProductos() {
        return repositorioProductos;
    }
}