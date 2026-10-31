package datos;
import java.util.List;

public interface Repositorio<T> {
    List<T> obtenerTodos();
    void insertar(T entidad);   // Antes se llamaba agregar
    void actualizar(T entidad); // Nuevo método explícito
    void eliminar(String id);   // Nuevo método explícito
}
