package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.interfaces.IGestionable;
import edu.uce.programacion2.tienda.interfaces.IReportable;
import edu.uce.programacion2.tienda.objetosServicio.Dinero;
import java.util.ArrayList;
import java.util.Date;

/**
 * Representa una Factura emitida al completar una Venta.
 *
 * <p>Una Factura agrupa:</p>
 * <ul>
 *   <li>La {@link Venta} que la origina (con todos sus {@link DetalleVenta}).</li>
 *   <li>El {@link Cliente} al que se emite.</li>
 *   <li>El {@link Cajero} que la procesó.</li>
 *   <li>Campos fiscales: número de factura, subtotal, IVA y total.</li>
 * </ul>
 *
 * <p>Implementa {@link IGestionable} para operaciones CRUD y
 * {@link IReportable} para su presentación en reportes.</p>
 *
 * <p>El cálculo de montos (redondeo y descomposición de IVA) se delega
 * en el objeto de servicio {@link Dinero}.</p>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Factura implements IGestionable, IReportable {

    private static int contadorFacturas = 0;

    public enum EstadoFactura { EMITIDA, ANULADA, PAGADA }

    // ──────────────────────────────────────────────────────────────────────
    // Atributos
    // ──────────────────────────────────────────────────────────────────────

    private int                       idFactura;
    private String                    numeroFactura;    // Ej.: "001-001-000000001"
    private Date                      fechaEmision;
    private Venta                     venta;
    private Cliente                   cliente;
    private Cajero                    cajero;
    private double                    subtotal;         // Sin IVA
    private double                    montoIva;
    private double                    total;            // subtotal + montoIva
    private EstadoFactura             estado;
    private ArrayList<DetalleFactura> detalles;         // Líneas fiscales de la factura

    // ──────────────────────────────────────────────────────────────────────
    // Constructores
    // ──────────────────────────────────────────────────────────────────────

    public Factura() {
        this.idFactura     = 0;
        this.numeroFactura = "";
        this.fechaEmision  = new Date();
        this.venta         = null;
        this.cliente       = null;
        this.cajero        = null;
        this.subtotal      = 0.0;
        this.montoIva      = 0.0;
        this.total         = 0.0;
        this.estado        = EstadoFactura.EMITIDA;
        this.detalles      = new ArrayList<>();
        // Constructor vacío no incrementa el contador: solo se usa internamente
    }

    /**
     * Constructor principal. Calcula automáticamente subtotal, IVA y total
     * a partir de la venta recibida.
     *
     * @param idFactura     Identificador único.
     * @param numeroFactura Número de serie (p. ej. "001-001-000000001").
     * @param venta         Venta que origina esta factura.
     * @param cliente       Cliente al que se emite.
     * @param cajero        Cajero que procesó la venta.
     */
    public Factura(int idFactura, String numeroFactura,
                   Venta venta, Cliente cliente, Cajero cajero) {
        this.idFactura     = idFactura;
        this.numeroFactura = numeroFactura;
        this.fechaEmision  = new Date();
        this.venta         = venta;
        this.cliente       = cliente;
        this.cajero        = cajero;
        this.estado        = EstadoFactura.EMITIDA;
        this.detalles      = new ArrayList<>();
        calcularMontos();
        contadorFacturas++;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Lógica de negocio
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Calcula subtotal, IVA y total a partir de la Venta asociada.
     * A diferencia de una tasa única global, cada línea puede tener su
     * propia tarifa de IVA (fija por categoría, ej. 0% en canasta básica,
     * o la tasa general vigente para el resto). Por eso el cálculo se hace
     * línea por línea y luego se suman los resultados:
     * <pre>
     *   por cada línea: montoIva_línea = baseImponible_línea * tasa_del_producto
     *   subtotal = Σ baseImponible_línea
     *   montoIva = Σ montoIva_línea
     *   total    = subtotal + montoIva
     * </pre>
     */
    public void calcularMontos() {
        if (venta == null) return;
        generarDetallesDesdeVenta();
        recalcularTotalesDesdeDetalles();
    }

    /** Suma base imponible e IVA de cada {@link DetalleFactura} para obtener los totales de la factura. */
    private void recalcularTotalesDesdeDetalles() {
        Dinero base = new Dinero(0);
        Dinero iva  = new Dinero(0);
        for (DetalleFactura d : detalles) {
            base = base.mas(new Dinero(d.calcularBaseImponible()));
            iva  = iva.mas(new Dinero(d.getMontoIva()));
        }
        this.subtotal = base.getValor();
        this.montoIva = iva.getValor();
        this.total    = base.mas(iva).getValor();
    }

    /**
     * Convierte cada {@link DetalleVenta} de la venta asociada en un
     * {@link DetalleFactura} y los añade a la lista interna.
     * Se llama automáticamente desde el constructor principal.
     */
    public void generarDetallesDesdeVenta() {
        if (venta == null) return;
        detalles.clear();
        int id = 1;
        for (DetalleVenta dv : venta.getDetalles()) {
            detalles.add(new DetalleFactura(id++, dv));
        }
    }

    /**
     * Agrega manualmente un {@link DetalleFactura} a la factura y
     * recalcula los totales.
     *
     * @param detalle Línea fiscal a añadir.
     */
    public void agregarDetalle(DetalleFactura detalle) {
        detalles.add(detalle);
        recalcularTotalesDesdeDetalles();
    }

    /**
     * Anula la factura y, como efecto de negocio esperado,
     * también anula la venta asociada para mantener consistencia.
     * Quien llame a este método debe tener en cuenta que la venta
     * quedará en estado ANULADA.
     */
    public void anular() {
        this.estado = EstadoFactura.ANULADA;
        if (venta != null) venta.anular();
        System.out.println("Factura " + numeroFactura + " ANULADA.");
    }

    /** Marca la factura como pagada. */
    public void marcarPagada() {
        this.estado = EstadoFactura.PAGADA;
        System.out.println("Factura " + numeroFactura + " marcada como PAGADA.");
    }

    // ──────────────────────────────────────────────────────────────────────
    // IGestionable
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public void crear() {
        System.out.println("Factura #" + numeroFactura + " emitida. Total: $" + total);
    }

    @Override
    public Object consultar() { return this; }

    @Override
    public void actualizar() {
        calcularMontos();
        System.out.println("Factura #" + numeroFactura + " actualizada. Total: $" + total);
    }

    @Override
    public void inactivar() {
        anular();
    }

    // ──────────────────────────────────────────────────────────────────────
    // IReportable
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public void generar() {
        System.out.println("Generando reporte de Factura #" + numeroFactura);
    }

    /** Imprime la factura completa con todos sus detalles. */
    @Override
    public void mostrar() {
        String linea = "─".repeat(52);
        System.out.println("\n" + linea);
        System.out.println("          TIENDA VIVERES — FACTURA");
        System.out.println(linea);
        System.out.printf("  Nro. Factura : %-30s%n", numeroFactura);
        System.out.printf("  Fecha        : %-30s%n", fechaEmision);
        System.out.printf("  Estado       : %-30s%n", estado);
        System.out.println(linea);
        System.out.printf("  Cliente      : %-30s%n",
                cliente != null ? cliente.getNombre() : "Consumidor Final");
        System.out.printf("  Cajero       : %-30s%n",
                cajero  != null ? cajero.getNombre()  : "N/A");
        System.out.println(linea);

        if (!detalles.isEmpty()) {
            System.out.printf("  %-18s %6s %10s %6s %6s %10s%n",
                    "PRODUCTO", "CANT.", "P.S/IVA", "TASA", "IVA", "SUBTOTAL");
            System.out.println("  " + "·".repeat(50));
            for (DetalleFactura d : detalles) {
                String nombre = d.getProducto() != null
                        ? d.getProducto().getNombre() : "N/A";
                double tasa = d.getProducto() != null
                        ? d.getProducto().getTarifaIvaAplicable() : Dinero.getIva();
                System.out.printf("  %-18s %6d %10.2f %5.0f%% %6.2f %10.2f%n",
                        nombre, d.getCantidad(),
                        d.getPrecioUnitarioSinIva(), tasa * 100, d.getMontoIva(), d.getSubtotal());
            }
        } else if (venta != null) {
            System.out.printf("  %-20s %6s %10s %10s%n",
                    "PRODUCTO", "CANT.", "P.UNIT", "SUBTOTAL");
            System.out.println("  " + "·".repeat(50));
            for (DetalleVenta d : venta.getDetalles()) {
                String nombre = d.getProducto() != null
                        ? d.getProducto().getNombre() : "N/A";
                System.out.printf("  %-20s %6d %10.2f %10.2f%n",
                        nombre, d.getCantidad(),
                        d.getPrecioUnitario(), d.calcularSubtotal());
            }
        }

        System.out.println("  " + "·".repeat(50));
        System.out.printf("  %-38s %10.2f%n", "SUBTOTAL (sin IVA):", subtotal);
        // Nota: el IVA puede ser mixto (productos con tarifa fija por categoría,
        // ej. 0% en canasta básica, junto a productos con la tasa general vigente),
        // por eso ya no se etiqueta con un único porcentaje.
        System.out.printf("  %-38s %10.2f%n", "IVA:", montoIva);
        System.out.printf("  %-38s %10.2f%n", "TOTAL:", total);
        System.out.printf("  %-38s %10s%n",   "MÉTODO DE PAGO:",
                venta != null ? venta.getMetodoPago() : "N/A");
        System.out.println(linea + "\n");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ──────────────────────────────────────────────────────────────────────

    public static int  getTotalFacturas() { return contadorFacturas; }
    public static void resetContador()    { contadorFacturas = 0; }

    public int                       getIdFactura()              { return idFactura; }
    public void                      setIdFactura(int v)          { this.idFactura = v; }
    public String                    getNumeroFactura()           { return numeroFactura; }
    public void                      setNumeroFactura(String v)   { this.numeroFactura = v; }
    public Date                      getFechaEmision()            { return fechaEmision; }
    public void                      setFechaEmision(Date v)      { this.fechaEmision = v; }  // ← NUEVO
    public double                    getSubtotal()                { return subtotal; }
    public void                      setSubtotal(double v)        { this.subtotal = v; }      // ← NUEVO
    public double                    getMontoIva()                { return montoIva; }
    public void                      setMontoIva(double v)        { this.montoIva = v; }      // ← NUEVO
    public double                    getTotal()                   { return total; }
    public void                      setTotal(double v)           { this.total = v; }         // ← NUEVO
    public Venta                     getVenta()                   { return venta; }
    public void                      setVenta(Venta v)             { this.venta = v; calcularMontos(); }
    public Cliente                   getCliente()                 { return cliente; }
    public void                      setCliente(Cliente v)         { this.cliente = v; }
    public Cajero                    getCajero()                  { return cajero; }
    public void                      setCajero(Cajero v)           { this.cajero = v; }
    public EstadoFactura             getEstado()                  { return estado; }
    public void                      setEstado(EstadoFactura v)    { this.estado = v; }
    public ArrayList<DetalleFactura> getDetalles()                { return detalles; }

    @Override
    public String toString() {
        return "Factura{nro='" + numeroFactura +
                "', fecha=" + fechaEmision +
                ", cliente=" + (cliente != null ? cliente.getNombre() : "N/A") +
                ", subtotal=$" + subtotal +
                ", iva=$" + montoIva +
                ", total=$" + total +
                ", estado=" + estado + "}";
    }
}