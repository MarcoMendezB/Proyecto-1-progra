package modelo;

public class Producto {
    private String codigo;
    private String nombre;
    private String categoria;
    private double precio;
    private int cantidadInventario;

    public Producto(String codigo, String nombre, String categoria, double precio, int cantidadInventario) {
        // Evitar NPE por campos null (por ejemplo, columnas NULL en MySQL)
        this.codigo = codigo == null ? "" : codigo;
        this.nombre = nombre == null ? "" : nombre;
        this.categoria = categoria == null ? "" : categoria;
        this.precio = precio;
        this.cantidadInventario = cantidadInventario;
    }

    // Getters y Setters necesarios
    public String getCodigo() { return codigo == null ? "" : codigo; }
    public String getNombre() { return nombre == null ? "" : nombre; }
    public String getCategoria() { return categoria == null ? "" : categoria; }
    public double getPrecio() { return precio; }
    public int getCantidadInventario() { return cantidadInventario; }
    public void setCantidadInventario(int cantidadInventario) { this.cantidadInventario = cantidadInventario; }

    @Override
    public String toString() {
        String n = getNombre();
        return (n.isEmpty() ? "(sin nombre)" : n) + " - ₡" + precio + " (Stock: " + cantidadInventario + ")";
    }
}
