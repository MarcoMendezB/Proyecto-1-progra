package modelo;

public class Producto {
    private String codigo;
    private String nombre;
    private String categoria;
    private double precio;
    private int cantidadInventario;

    public Producto(String codigo, String nombre, String categoria, double precio, int cantidad){
        this.codigo = codigo; this.nombre = nombre; this.categoria = categoria; this.precio = precio; this.cantidadInventario = cantidad;
    }

    public String getCodigo() {return codigo;}

    public String getNombre() {return nombre;}

    public String getCategoria() {return categoria;}

    public double getPrecio() {return precio;}

    public int getCantidadInventario() {return cantidadInventario;}

    public void setCodigo(String codigo) {this.codigo = codigo;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public void setCategoria(String categoria) {this.categoria = categoria;}

    public void setPrecio(double precio) {this.precio = precio;}

    public void setCantidadInventario(int cantidadInventario) {this.cantidadInventario = cantidadInventario;}

    @Override
    public String toString() {return nombre + " -₡" + precio;}
}
