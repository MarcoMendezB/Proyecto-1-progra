package datos;
import modelo.Producto;
import java.util.ArrayList;
import java.util.List;

public class Inventario implements Repositorio<Producto> {
    private List<Producto> productos = new ArrayList<>();
    @Override
    public void agregar(Producto p) { productos.add(p); }
    @Override
    public List<Producto> obtenerTodos() {return productos; }
}
