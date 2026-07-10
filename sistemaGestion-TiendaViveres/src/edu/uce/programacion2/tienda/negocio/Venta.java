package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.interfaces.IGestionable;
import edu.uce.programacion2.tienda.interfaces.IReportable;
import edu.uce.programacion2.tienda.objetosServicio.Dinero;
import edu.uce.programacion2.tienda.objetosServicio.Fecha;
import java.util.ArrayList;
import java.util.Date;

/**
 * Representa una venta realizada en la tienda.
 * Implementa IGestionable e IReportable (dos interfaces).
 * Mantiene un contador estático de ventas creadas.
 * Usa ArrayList<DetalleVenta> para gestionar los ítems.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Venta implements IGestionable, IReportable {

    private static int contadorVentas = 0;

    public enum Estado { ACTIVA, ANULADA, COMPLETADA }

    private int                   idVenta;
    private Date                  fecha;
    private double                total;
    private String                metodoPago;
    private Estado                estado;
    private ArrayList<DetalleVenta> detalles;

    public Venta() {
        this.idVenta    = 0;
        this.fecha      = new Date();
        this.total      = 0.0;
        this.metodoPago = "";
        this.estado     = Estado.ACTIVA;
        this.detalles   = new ArrayList<>();
        // Constructor vacío no incrementa el contador: solo se usa internamente
    }

    public Venta(int idVenta, String metodoPago) {
        this.idVenta    = idVenta;
        this.fecha      = new Date();
        this.total      = 0.0;
        this.metodoPago = metodoPago;
        this.estado     = Estado.ACTIVA;
        this.detalles   = new ArrayList<>();
        contadorVentas++;
    }

    public static int  getTotalVentas()  { return contadorVentas; }
    public static void resetContador()   { contadorVentas = 0; }

    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        total = calcularTotal();
    }

    /** Suma los subtotales de todos los detalles. Redondea a 2 decimales. */
    public double calcularTotal() {
        Dinero suma = new Dinero(0);
        for (DetalleVenta d : detalles) suma = suma.mas(new Dinero(d.calcularSubtotal()));
        return suma.getValor();
    }

    public void anular() {
        this.estado = Estado.ANULADA;
        System.out.println("Venta " + idVenta + " anulada.");
    }

    @Override public void   crear()      { System.out.println("Venta #" + idVenta + " registrada. Total: $" + calcularTotal()); }
    @Override public Object consultar()  { return this; }
    @Override public void   actualizar() { System.out.println("Venta #" + idVenta + " actualizada."); }
    @Override public void   inactivar()  { anular(); }

    @Override
    public void generar() {
        System.out.println("Generando reporte de venta #" + idVenta);
    }

    @Override
    public void mostrar() {
        System.out.println("=== Venta #" + idVenta + " ===");
        System.out.println("  Fecha   : " + fecha);
        System.out.println("  Método  : " + metodoPago);
        System.out.println("  Estado  : " + estado);
        for (DetalleVenta d : detalles) System.out.println("  " + d);
        System.out.println("  TOTAL   : $" + calcularTotal());
    }

    public int     getIdVenta()                    { return idVenta; }
    public void    setIdVenta(int v)                { this.idVenta = v; }
    public Date    getFecha()                      { return fecha; }
    public void    setFecha(Date fecha)            { this.fecha = fecha; }

    /** Retorna la fecha de la venta como objeto de servicio {@link Fecha}. */
    public Fecha   getFechaServicio()               { return Fecha.desdeDate(fecha); }

    public double  getTotal()                      { return total; }
    public String  getMetodoPago()                 { return metodoPago; }
    public Estado  getEstado()                     { return estado; }
    public void    setEstado(Estado v)              { this.estado = v; }
    public ArrayList<DetalleVenta> getDetalles()   { return detalles; }

    @Override
    public String toString() {
        return "Venta{id=" + idVenta + ", fecha=" + fecha +
               ", total=$" + calcularTotal() +
               ", metodoPago='" + metodoPago +
               "', estado=" + estado +
               ", items=" + detalles.size() + "}";
    }
}
