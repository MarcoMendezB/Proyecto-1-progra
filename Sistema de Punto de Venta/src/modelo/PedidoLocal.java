package modelo;

public class PedidoLocal extends Pedido {
    public PedidoLocal(String cliente ) { super(cliente); }
    @Override
    public double calcularTotal(){ return calcularSubtotal() + calcularImpuesto();}
}
