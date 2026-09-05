package modelo;

public class PedidoExpress extends Pedido{
    private final double COSTO_ENVIO = 1500.0;
    public PedidoExpress(String cliente) { super(cliente);}
    @Override
    public double calcularTotal() {return calcularSubtotal() + calcularImpuesto() + COSTO_ENVIO;}
}
