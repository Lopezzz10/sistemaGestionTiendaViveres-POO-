package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.excepciones.StockInsuficienteException;
import edu.uce.programacion2.tienda.objetosServicio.Fecha;
import java.util.ArrayList;
import java.util.Date;

/**
 * Gestiona el stock de un producto con historial de movimientos.
 * Lanza StockInsuficienteException REAL desde retirar() cuando
 * se solicita más stock del disponible.
 *
 * Mantiene:
 *   - umbralAlerta: atributo estático compartido
 *   - historialMovimientos: ArrayList<String> de cada ingreso y retiro
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Inventario {

    private static int umbralAlerta = 5;

    private int                  idInventario;
    private Producto             producto;
    private int                  cantidadDisponible;
    private Date                 fechaActualizacion;
    private ArrayList<String>    historialMovimientos;

    /** Indica si el registro de inventario está activo (borrado lógico: false = inactivo). */
    private boolean               activo;

    public Inventario() {
        this.idInventario        = 0;
        this.producto            = null;
        this.cantidadDisponible  = 0;
        this.fechaActualizacion  = new Date();
        this.historialMovimientos = new ArrayList<>();
        this.activo               = true;
    }

    public Inventario(int id, Producto producto, int cantidad) {
        this.idInventario        = id;
        this.producto            = producto;
        this.cantidadDisponible  = cantidad;
        this.fechaActualizacion  = new Date();
        this.historialMovimientos = new ArrayList<>();
        this.activo               = true;
        registrarMovimiento("INICIO", cantidad);
    }

    public static int  getUmbralAlerta()      { return umbralAlerta; }
    public static void setUmbralAlerta(int v) { umbralAlerta = v; }

    public void agregar(int cantidad) {
        cantidadDisponible += cantidad;
        fechaActualizacion  = new Date();
        registrarMovimiento("INGRESO +" + cantidad, cantidadDisponible);
        System.out.println("  Inventario: +" + cantidad + " '" +
                nombreProducto() + "'. Stock: " + cantidadDisponible);
    }

    /**
     * Retira unidades. Lanza StockInsuficienteException REAL si no hay suficiente stock.
     */
    public void retirar(int cantidad) throws StockInsuficienteException {
        if (cantidad > cantidadDisponible)
            throw new StockInsuficienteException(
                    "Stock insuficiente para '" + nombreProducto() +
                            "': solicitado=" + cantidad + ", disponible=" + cantidadDisponible);
        cantidadDisponible -= cantidad;
        fechaActualizacion  = new Date();
        registrarMovimiento("RETIRO -" + cantidad, cantidadDisponible);
        System.out.println("  Inventario: -" + cantidad + " '" +
                nombreProducto() + "'. Stock: " + cantidadDisponible);
    }

    public boolean hayStockSuficiente(int cantidad) { return cantidadDisponible >= cantidad; }
    public boolean requiereAlerta()                 { return cantidadDisponible <= umbralAlerta; }

    private void registrarMovimiento(String tipo, int stockResultante) {
        historialMovimientos.add(
                "[" + new Date() + "] " + tipo + " → stock=" + stockResultante);
    }

    public ArrayList<String> getHistorialMovimientos() { return historialMovimientos; }

    private String nombreProducto() { return producto != null ? producto.getNombre() : "?"; }

    public int      getIdInventario()           { return idInventario; }
    public void     setIdInventario(int v)       { this.idInventario = v; }
    public Producto getProducto()               { return producto; }
    public void     setProducto(Producto v)      { this.producto = v; }
    public int      getCantidadDisponible()      { return cantidadDisponible; }
    public void     setCantidadDisponible(int v) { this.cantidadDisponible = v; }
    public Date     getFechaActualizacion()      { return fechaActualizacion; }
    public boolean  isActivo()                   { return activo; }
    public void     setActivo(boolean v)         { this.activo = v; }

    /** Retorna la fecha de la última actualización como objeto de servicio {@link Fecha}. */
    public Fecha    getFechaActualizacionServicio() { return Fecha.desdeDate(fechaActualizacion); }

    @Override
    public String toString() {
        return "Inventario{id=" + idInventario +
                ", producto='" + nombreProducto() +
                "', stock=" + cantidadDisponible +
                ", movimientos=" + historialMovimientos.size() +
                ", alerta=" + requiereAlerta() +
                ", activo=" + activo + "}";
    }
}