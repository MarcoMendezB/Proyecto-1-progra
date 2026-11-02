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
        File archivoTmp = new File(ARCHIVO_CSV + ".tmp");
        File archivo = new File(ARCHIVO_CSV);
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoTmp))) {
            pw.println("Codigo,Nombre,Categoria,Precio,Stock"); // Encabezado de columnas
            for (Producto p : productos) {
                pw.println(p.getCodigo() + "," + p.getNombre() + "," +
                        p.getCategoria() + "," + p.getPrecio() + "," +
                        p.getCantidadInventario());
            }
        } catch (Exception e) {
            System.out.println("Error al guardar CSV: " + e.getMessage());
            return;
        }
        if (!archivoTmp.renameTo(archivo)) {
            System.out.println("Error al renombrar archivo temporal de CSV.");
        }
    }
    // ------------------------------

    @Override
    public void insertar(Producto p) {
        productos.add(p);
        guardarDatos(); // Guardar al añadir
    }

    @Override
    public void actualizar(Producto p) {
        if (p == null) return;
        String codigo = p.getCodigo();
        for (int i = 0; i < productos.size(); i++) {
            Producto actual = productos.get(i);
            if (actual != null && java.util.Objects.equals(actual.getCodigo(), codigo)) {
                productos.set(i, p);
                guardarDatos(); // Guardar al modificar
                break;
            }
        }
    }

    @Override
    public void eliminar(String codigo) {
        if (productos == null) return;
        productos.removeIf(p -> p != null && java.util.Objects.equals(p.getCodigo(), codigo));
        guardarDatos(); // Guardar al borrar
    }

    @Override
    public void decrementarStock(String codigo) {
        if (productos == null) return;
        for (Producto p : productos) {
            if (p != null && java.util.Objects.equals(p.getCodigo(), codigo)) {
                int stockActual = p.getCantidadInventario();
                if (stockActual > 0) {
                    p.setCantidadInventario(stockActual - 1);
                }
                break;
            }
        }
        guardarDatos();
    }

    public List<Producto> obtenerTodos() {
        List<Producto> copia = new ArrayList<>();
        if (productos == null) return copia;
        for (Producto p : productos) {
            if (p != null) copia.add(p);
        }
        return copia;
    }
}