package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Dinero;

/**
 * Representa un ítem (línea) dentro de una {@link Factura}.
 *
 * <p>Cada {@code DetalleFactura} captura la información fiscal de un producto
 * en el momento de la emisión: precio unitario sin IVA, cantidad, descuento
 * aplicado, monto de IVA y subtotal final.  De este modo la factura conserva
 * una instantánea inmutable del precio aunque el catálogo cambie después.</p>
 *
 * <p>Relación con otras clases:</p>
 * <ul>
 *   <li>Se origina a partir de un {@link DetalleVenta} (mediante el constructor
 *       de conversión {@link #DetalleFactura(int, DetalleVenta)}).</li>
 *   <li>Referencia al {@link Producto} vendido (solo lectura).</li>
 *   <li>Pertenece a una {@link Factura} que mantiene una
 *       {@code ArrayList<DetalleFactura>}.</li>
 * </ul>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetalleFactura {

    // ──────────────────────────────────────────────────────────────────────
    // Atributos
    // ──────────────────────────────────────────────────────────────────────

    private int      idDetalle;
    private Producto producto;
    private int      cantidad;
    private double   precioUnitarioSinIva;  // precio base sin impuesto
    private double   descuento;
    private double   montoIva;              // IVA calculado para esta línea
    private double   subtotal;              // (precioUnitarioSinIva * cantidad) - descuento + montoIva

    // ──────────────────────────────────────────────────────────────────────
    // Constructores
    // ──────────────────────────────────────────────────────────────────────

    /** Constructor vacío. */
    public DetalleFactura() {
        this.idDetalle            = 0;
        this.producto             = null;
        this.cantidad             = 0;
        this.precioUnitarioSinIva = 0.0;
        this.descuento            = 0.0;
        this.montoIva             = 0.0;
        this.subtotal             = 0.0;
    }

    /**
     * Constructor completo con cálculo automático de IVA y subtotal.
     *
     * @param idDetalle            Identificador de la línea.
     * @param producto             Producto facturado.
     * @param cantidad             Unidades vendidas.
     * @param precioUnitarioSinIva Precio unitario sin IVA.
     * @param descuento            Descuento total aplicado a la línea.
     */
    public DetalleFactura(int idDetalle, Producto producto,
                          int cantidad, double precioUnitarioSinIva,
                          double descuento) {
        this.idDetalle            = idDetalle;
        this.producto             = producto;
        this.cantidad             = cantidad;
        this.precioUnitarioSinIva = precioUnitarioSinIva;
        this.descuento            = descuento;
        calcularMontos();
    }

    /**
     * Constructor de conversión: crea un {@code DetalleFactura} a partir de
     * un {@link DetalleVenta} existente.  El precio con IVA del detalle de
     * venta se descompone para obtener el precio base sin impuesto.
     *
     * @param idDetalle    Identificador de la línea en la factura.
     * @param detalleVenta Ítem de la venta original.
     */
    public DetalleFactura(int idDetalle, DetalleVenta detalleVenta) {
        this.idDetalle = idDetalle;
        this.producto  = detalleVenta.getProducto();
        this.cantidad  = detalleVenta.getCantidad();
        this.descuento = detalleVenta.getDescuento();
        // El precio unitario del DetalleVenta ya incluye IVA; descomponemos
        // usando la tarifa que corresponda a este producto en particular
        // (fija por categoría, ej. 0%, o la tasa general vigente).
        double tasa = (producto != null) ? producto.getTarifaIvaAplicable() : Dinero.getIva();
        this.precioUnitarioSinIva = new Dinero(detalleVenta.getPrecioUnitario())
                .descomponerSinIva(tasa).getValor();
        calcularMontos();
    }

    // ──────────────────────────────────────────────────────────────────────
    // Lógica de negocio
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Calcula {@code montoIva} y {@code subtotal} a partir de los demás campos.
     * La tarifa de IVA usada es la del producto ({@link Producto#getTarifaIvaAplicable()}):
     * fija si su categoría tiene tarifa especial predeterminada (ej. 0%), o la
     * tasa general vigente en caso contrario.
     * <pre>
     *   baseImponible = precioUnitarioSinIva * cantidad - descuento
     *   montoIva      = baseImponible * tasaDelProducto
     *   subtotal      = baseImponible + montoIva
     * </pre>
     */
    public void calcularMontos() {
        Dinero base = new Dinero((precioUnitarioSinIva * cantidad) - descuento);
        double tasa = (producto != null) ? producto.getTarifaIvaAplicable() : Dinero.getIva();
        Dinero iva  = base.calcularIva(tasa);
        this.montoIva = iva.getValor();
        this.subtotal = base.mas(iva).getValor();
    }

    /**
     * Calcula solo la base imponible (sin IVA) de la línea.
     *
     * @return {@code precioUnitarioSinIva * cantidad - descuento}
     */
    public double calcularBaseImponible() {
        return new Dinero((precioUnitarioSinIva * cantidad) - descuento).getValor();
    }

    /** Verifica que el precio unitario sea mayor a cero. */
    public boolean verificarPrecio() {
        return precioUnitarioSinIva > 0;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ──────────────────────────────────────────────────────────────────────

    public int      getIdDetalle()                        { return idDetalle; }
    public void     setIdDetalle(int v)                   { this.idDetalle = v; }
    public Producto getProducto()                         { return producto; }
    public void     setProducto(Producto v)               { this.producto = v; }
    public int      getCantidad()                         { return cantidad; }
    public void     setCantidad(int v)                    { this.cantidad = v;  calcularMontos(); }
    public double   getPrecioUnitarioSinIva()             { return precioUnitarioSinIva; }
    public void     setPrecioUnitarioSinIva(double v)     { this.precioUnitarioSinIva = v; calcularMontos(); }
    public double   getDescuento()                        { return descuento; }
    public void     setDescuento(double v)                { this.descuento = v; calcularMontos(); }
    public double   getMontoIva()                         { return montoIva; }
    public double   getSubtotal()                         { return subtotal; }

    // ──────────────────────────────────────────────────────────────────────
    // toString
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "DetalleFactura{id=" + idDetalle
                + ", producto='" + (producto != null ? producto.getNombre() : "N/A")
                + "', cantidad=" + cantidad
                + ", precioSinIva=" + precioUnitarioSinIva
                + ", descuento=" + descuento
                + ", iva=" + montoIva
                + ", subtotal=" + subtotal + "}";
    }
}