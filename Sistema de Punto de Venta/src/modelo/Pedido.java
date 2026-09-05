package modelo;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

public abstract class Pedido implements Facturable{
    protected String cliente;
    protected List<Producto> carrito;

    public Pedido(String cliente) {
        this.cliente = cliente;
        this.carrito = new ArrayList<>();
    }
    public void agregarProducto(Producto p){ carrito.add(p);}
    public List<Producto> getCarrito() {return carrito;}

    @Override
    public double calcularSubtotal() {
        return carrito.stream().mapToDouble(Producto::getPrecio).sum();
    }
    @Override
    public double calcularImpuesto(){return calcularSubtotal() * 0.13;}
}
