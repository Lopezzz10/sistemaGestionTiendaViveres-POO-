package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Dinero;

/**
 * Representa un ítem dentro de una venta.
 * Usa calcularPrecioFinal() de Producto (polimorfismo) para calcular el subtotal.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetalleVenta {

    private int      idDetalle;
    private Producto producto;
    private int      cantidad;
    private double   precioUnitario;
    private double   descuento;

    public DetalleVenta() {
        this.idDetalle      = 0;
        this.producto       = null;
        this.cantidad       = 0;
        this.precioUnitario = 0.0;
        this.descuento      = 0.0;
    }

    public DetalleVenta(int idDetalle, Producto producto,
                        int cantidad, double descuento) {
        this.idDetalle      = idDetalle;
        this.producto       = producto;
        this.cantidad       = cantidad;
        this.precioUnitario = new Dinero(producto.calcularPrecioFinal()).getValor();
        this.descuento      = descuento;
    }

    /** Calcula el subtotal: (precioUnitario * cantidad) - descuento. Redondea a 2 decimales. */
    public double calcularSubtotal() {
        return new Dinero((precioUnitario * cantidad) - descuento).getValor();
    }

    public boolean verificarPrecio() { return precioUnitario > 0; }

    public int      getIdDetalle()            { return idDetalle; }
    public Producto getProducto()             { return producto; }
    public void     setProducto(Producto v)    { this.producto = v; }
    public int      getCantidad()             { return cantidad; }
    public void     setCantidad(int v)         { this.cantidad = v; }
    public double   getPrecioUnitario()        { return precioUnitario; }
    public double   getDescuento()            { return descuento; }
    public void     setDescuento(double v)     { this.descuento = v; }

    @Override
    public String toString() {
        return "DetalleVenta{id=" + idDetalle +
               ", producto='" + (producto != null ? producto.getNombre() : "N/A") +
               "', cantidad=" + cantidad +
               ", precioUnit=" + precioUnitario +
               ", descuento=" + descuento +
               ", subtotal=" + calcularSubtotal() + "}";
    }
}
