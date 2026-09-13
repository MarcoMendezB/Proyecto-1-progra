package datos;
import modelo.Producto;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Inventario implements Repositorio<Producto> {
    private List<Producto> productos = new ArrayList<>();
    private final String ARCHIVO_CSV = "productos.csv";

    public Inventario() {
        cargarDatos(); // Carga el CSV automáticamente al iniciar el programa
    }

    // --- MANEJO DE ARCHIVOS CSV ---
    private void cargarDatos() {
        File archivo = new File(ARCHIVO_CSV);
        if (!archivo.exists()) return; // Si no hay archivo, inicia vacío

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (primeraLinea) { primeraLinea = false; continue; } // Saltar el encabezado

                String[] datos = linea.split(",");
                if (datos.length == 5) {
                    Producto p = new Producto(datos[0], datos[1], datos[2],
                            Double.parseDouble(datos[3]), Integer.parseInt(datos[4]));
                    productos.add(p);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al cargar CSV: " + e.getMessage());
        }
    }

    public void guardarDatos() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_CSV))) {
            pw.println("Codigo,Nombre,Categoria,Precio,Stock"); // Encabezado de columnas
            for (Producto p : productos) {
                pw.println(p.getCodigo() + "," + p.getNombre() + "," +
                        p.getCategoria() + "," + p.getPrecio() + "," +
                        p.getCantidadInventario());
            }
        } catch (Exception e) {
            System.out.println("Error al guardar CSV: " + e.getMessage());
        }
    }
    // ------------------------------

    @Override
    public void agregar(Producto p) {
        productos.add(p);
        guardarDatos(); // Guardar al añadir
    }

    public void actualizar(Producto p) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo().equals(p.getCodigo())) {
                productos.set(i, p);
                guardarDatos(); // Guardar al modificar
                break;
            }
        }
    }

    public void eliminar(String codigo) {
        productos.removeIf(p -> p.getCodigo().equals(codigo));
        guardarDatos(); // Guardar al borrar
    }

    @Override
    public List<Producto> obtenerTodos() { return productos; }
}