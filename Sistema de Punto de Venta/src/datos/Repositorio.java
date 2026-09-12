package datos;
import java.util.List;

public interface Repositorio<T> {
    void agregar(T entidad);
    List<T> obtenerTodos();
}
