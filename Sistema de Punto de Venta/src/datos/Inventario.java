package datos;
import modelo.Producto;
import java.util.ArrayList;
import java.util.List;

public class Inventario implements Repositorio<Producto> {
    private List<Producto> productos = new ArrayList<>();

    @Override
    public void agregar(Producto p) { productos.add(p); }

    public void actualizar(Producto p) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo().equals(p.getCodigo())) {
                productos.set(i, p);
                break;
            }
        }
    }

    public void eliminar(String codigo) {
        productos.removeIf(p -> p.getCodigo().equals(codigo));
    }

    @Override
    public List<Producto> obtenerTodos() { return productos; }
}