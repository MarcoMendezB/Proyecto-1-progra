package modelo;
public class PedidoLocal extends Pedido {
    private int numeroMesa;
    public PedidoLocal(String cliente, int numeroMesa) {
        super(cliente);
        this.numeroMesa = numeroMesa;
    }
    @Override
    public double calcularTotal() { return calcularSubtotal() + calcularImpuesto(); }
}
