package modelo;
import java.util.ArrayList;
import java.util.List;

public abstract class Pedido implements Facturable{
    protected String cliente;
    protected List<Producto> carrito;

    public Pedido(String cliente) {
        this.cliente = cliente;
        this.carrito = new ArrayList<>();
    }
    public void agregarProducto(Producto p){ if (p != null) carrito.add(p); }
    public List<Producto> getCarrito() { return carrito == null ? new ArrayList<>() : carrito; }
    public String getCliente() { return cliente == null ? "" : cliente; }

    @Override
    public double calcularSubtotal() {
        return getCarrito().stream()
            .filter(p -> p != null)
            .mapToDouble(Producto::getPrecio)
            .sum();
    }
    @Override
    public double calcularImpuesto(){return calcularSubtotal() * 0.13;}
}
