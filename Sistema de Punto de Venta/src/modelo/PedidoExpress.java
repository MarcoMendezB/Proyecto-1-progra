package modelo;
public class PedidoExpress extends Pedido {
    private String direccion;
    private final double COSTO_ENVIO = 1500.0;
    public PedidoExpress(String cliente, String direccion) {
        super(cliente);
        this.direccion = direccion;
    }
    @Override
    public double calcularTotal() { return calcularSubtotal() + calcularImpuesto() + COSTO_ENVIO; }
}