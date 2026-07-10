package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.interfaces.IGestionable;
import edu.uce.programacion2.tienda.interfaces.IReportable;
import edu.uce.programacion2.tienda.objetosServicio.Dinero;
import edu.uce.programacion2.tienda.objetosServicio.Fecha;
import java.util.ArrayList;
import java.util.Date;

/**
 * Representa una compra realizada a un proveedor para reabastecer el inventario.
 * Implementa IGestionable e IReportable (igual que Venta).
 * Usa ArrayList<DetalleCompra> para gestionar los ítems adquiridos.
 * Se asocia a un Proveedor y a un Cliente (quien gestiona/autoriza la compra,
 * o puede ser el Administrador según el contexto de uso).
 * Mantiene un contador estático de compras creadas.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Compra implements IGestionable, IReportable {

    private static int contadorCompras = 0;

    public enum Estado { PENDIENTE, RECIBIDA, ANULADA }

    private int                      idCompra;
    private Date                     fecha;
    private double                   total;
    private String                   metodoPago;
    private Estado                   estado;
    private Proveedor                proveedor;
    private Cliente                  cliente;
    private ArrayList<DetalleCompra> detalles;

    public Compra() {
        this.idCompra   = 0;
        this.fecha      = new Date();
        this.total      = 0.0;
        this.metodoPago = "";
        this.estado     = Estado.PENDIENTE;
        this.proveedor  = null;
        this.cliente    = null;
        this.detalles   = new ArrayList<>();
        // Constructor vacío no incrementa el contador: solo se usa internamente
    }

    public Compra(int idCompra, String metodoPago,
                  Proveedor proveedor, Cliente cliente) {
        this.idCompra   = idCompra;
        this.fecha      = new Date();
        this.total      = 0.0;
        this.metodoPago = metodoPago;
        this.estado     = Estado.PENDIENTE;
        this.proveedor  = proveedor;
        this.cliente    = cliente;
        this.detalles   = new ArrayList<>();
        contadorCompras++;
    }

    public static int  getTotalCompras()  { return contadorCompras; }
    public static void resetContador()    { contadorCompras = 0; }

    public void agregarDetalle(DetalleCompra detalle) {
        detalles.add(detalle);
        total = calcularTotal();
    }

    /** Suma los subtotales de todos los detalles. Redondea a 2 decimales. */
    public double calcularTotal() {
        Dinero suma = new Dinero(0);
        for (DetalleCompra d : detalles) suma = suma.mas(new Dinero(d.calcularSubtotal()));
        return suma.getValor();
    }

    /** Marca la compra como recibida y acumula puntos al cliente si aplica. */
    public void recibirCompra() {
        this.estado = Estado.RECIBIDA;
        if (cliente != null) cliente.acumularPuntos(calcularTotal());
        System.out.println("Compra #" + idCompra + " recibida. Total: $" + calcularTotal());
    }

    public void anular() {
        this.estado = Estado.ANULADA;
        System.out.println("Compra #" + idCompra + " anulada.");
    }

    @Override public void   crear()      { System.out.println("Compra #" + idCompra + " registrada. Total: $" + calcularTotal()); }
    @Override public Object consultar()  { return this; }
    @Override public void   actualizar() { System.out.println("Compra #" + idCompra + " actualizada."); }
    @Override public void   inactivar()  { anular(); }

    @Override
    public void generar() {
        System.out.println("Generando reporte de compra #" + idCompra);
    }

    @Override
    public void mostrar() {
        System.out.println("=== Compra #" + idCompra + " ===");
        System.out.println("  Fecha     : " + fecha);
        System.out.println("  Método    : " + metodoPago);
        System.out.println("  Estado    : " + estado);
        System.out.println("  Proveedor : " + (proveedor != null ? proveedor.getNombre() : "N/A"));
        System.out.println("  Cliente   : " + (cliente != null ? cliente.getNombre() : "N/A"));
        for (DetalleCompra d : detalles) System.out.println("  " + d);
        System.out.println("  TOTAL     : $" + calcularTotal());
    }

    public int       getIdCompra()                      { return idCompra; }
    public void      setIdCompra(int v)                  { this.idCompra = v; }
    public Date      getFecha()                         { return fecha; }
    public void      setFecha(Date fecha)               { this.fecha = fecha; }

    /** Retorna la fecha de la compra como objeto de servicio {@link Fecha}. */
    public Fecha     getFechaServicio()                 { return Fecha.desdeDate(fecha); }

    public double    getTotal()                         { return total; }
    public String    getMetodoPago()                    { return metodoPago; }
    public Estado    getEstado()                        { return estado; }
    public void      setEstado(Estado v)                 { this.estado = v; }
    public Proveedor getProveedor()                     { return proveedor; }
    public void      setProveedor(Proveedor v)           { this.proveedor = v; }
    public Cliente   getCliente()                       { return cliente; }
    public void      setCliente(Cliente v)               { this.cliente = v; }
    public ArrayList<DetalleCompra> getDetalles()       { return detalles; }

    @Override
    public String toString() {
        return "Compra{id=" + idCompra + ", fecha=" + fecha
               + ", total=$" + calcularTotal()
               + ", metodoPago='" + metodoPago
               + "', estado=" + estado
               + ", proveedor='" + (proveedor != null ? proveedor.getNombre() : "N/A")
               + "', cliente='" + (cliente != null ? cliente.getNombre() : "N/A")
               + "', items=" + detalles.size() + "}";
    }
}
