package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Dinero;

/**
 * Representa un ítem dentro de una compra a proveedor.
 * Análogo a DetalleVenta, pero orientado a la adquisición de stock.
 * Usa calcularPrecioFinal() de Producto (polimorfismo) como precio base.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetalleCompra {

    private int      idDetalle;
    private Producto producto;
    private int      cantidad;
    private double   precioCompra;
    private double   descuento;

    public DetalleCompra() {
        this.idDetalle   = 0;
        this.producto    = null;
        this.cantidad    = 0;
        this.precioCompra = 0.0;
        this.descuento   = 0.0;
    }

    public DetalleCompra(int idDetalle, Producto producto,
                         int cantidad, double precioCompra, double descuento) {
        this.idDetalle    = idDetalle;
        this.producto     = producto;
        this.cantidad     = cantidad;
        this.precioCompra = precioCompra;
        this.descuento    = descuento;
    }

    /** Calcula el subtotal: (precioCompra * cantidad) - descuento. */
    public double calcularSubtotal() {
        return new Dinero((precioCompra * cantidad) - descuento).getValor();
    }

    /** Devuelve el margen estimado respecto al precio de venta del producto. */
    public double calcularMargen() {
        if (producto == null || precioCompra <= 0) return 0;
        return new Dinero(producto.calcularPrecioFinal() - precioCompra).getValor();
    }

    public boolean verificarPrecio() { return precioCompra > 0; }

    public int      getIdDetalle()             { return idDetalle; }
    public Producto getProducto()              { return producto; }
    public void     setProducto(Producto v)     { this.producto = v; }
    public int      getCantidad()              { return cantidad; }
    public void     setCantidad(int v)          { this.cantidad = v; }
    public double   getPrecioCompra()          { return precioCompra; }
    public void     setPrecioCompra(double v)   { this.precioCompra = v; }
    public double   getDescuento()             { return descuento; }
    public void     setDescuento(double v)      { this.descuento = v; }

    @Override
    public String toString() {
        return "DetalleCompra{id=" + idDetalle
               + ", producto='" + (producto != null ? producto.getNombre() : "N/A")
               + "', cantidad=" + cantidad
               + ", precioCompra=" + precioCompra
               + ", descuento=" + descuento
               + ", subtotal=" + calcularSubtotal()
               + ", margen=" + calcularMargen() + "}";
    }
}
