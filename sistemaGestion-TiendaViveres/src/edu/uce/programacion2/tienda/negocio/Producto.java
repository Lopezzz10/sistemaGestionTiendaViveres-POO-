package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Dinero;

import java.util.Date;

/**
 * Clase unificada para todos los productos de la tienda.
 * Absorbe los campos y la lógica de ProductoPerecible y ProductoNoPerecible.
 *
 * El campo 'marca' es comun a todos los productos (Perecibles y No
 * Perecibles). El campo 'tipo' determina el resto del comportamiento:
 *   - "Perecible"    → tiene fechaVencimiento y temperaturaAlmacenamiento.
 *                      Aplica 20% de descuento si vence en 3 días o menos.
 *   - "No Perecible" → tiene pesoKg.
 *                      Aplica 10% de descuento si pesoKg > 5.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Producto {

    /**
     * Valor de {@link #estado} usado como borrado lógico: el producto ya no
     * se muestra en los listados normales ni se puede vender, pero el
     * registro se conserva (no se elimina físicamente del catálogo).
     */
    public static final String ESTADO_INACTIVO = "Inactivo";

    // ── Campos estáticos ─────────────────────────────────────────────────────
    private static int totalProductos = 0;

    // ── Campos comunes ───────────────────────────────────────────────────────
    private String    codigo;
    private String    nombre;
    private Categoria categoria;
    private double    precioUnitario;
    private String    estado;
    private String    tipo;           // "Perecible" o "No Perecible"

    // ── Campos exclusivos de Perecible ───────────────────────────────────────
    private Date   fechaVencimiento;
    private double temperaturaAlmacenamiento;

    // ── Campos exclusivos de No Perecible ────────────────────────────────────
    private double pesoKg;
    private String marca;

    // ── Constructores ─────────────────────────────────────────────────────────

    /** Constructor vacío — no incrementa el contador. */
    public Producto() {
        this.codigo                    = "";
        this.nombre                    = "";
        this.categoria                 = null;
        this.precioUnitario            = 0.0;
        this.estado                    = "Disponible";
        this.tipo                      = "No Perecible";
        this.fechaVencimiento          = null;
        this.temperaturaAlmacenamiento = 0.0;
        this.pesoKg                    = 0.0;
        this.marca                     = "";
    }

    /** Constructor de búsqueda (solo código, sin contar). */
    public Producto(String codigo) {
        this();
        this.codigo = codigo;
    }

    /**
     * Constructor para producto Perecible.
     * Uso: new Producto(codigo, nombre, categoria, precio, fechaVenc, temperatura)
     */
    public Producto(String codigo, String nombre, Categoria categoria,
                    double precioUnitario, Date fechaVencimiento,
                    double temperaturaAlmacenamiento) {
        this.codigo                    = codigo;
        this.nombre                    = nombre;
        this.categoria                 = categoria;
        this.precioUnitario            = precioUnitario;
        this.estado                    = "Disponible";
        this.tipo                      = "Perecible";
        this.fechaVencimiento          = fechaVencimiento;
        this.temperaturaAlmacenamiento = temperaturaAlmacenamiento;
        this.pesoKg                    = 0.0;
        this.marca                     = "";
        totalProductos++;
    }

    /**
     * Constructor para producto No Perecible.
     * Uso: new Producto(codigo, nombre, categoria, precio, pesoKg, marca)
     */
    public Producto(String codigo, String nombre, Categoria categoria,
                    double precioUnitario, double pesoKg, String marca) {
        this.codigo                    = codigo;
        this.nombre                    = nombre;
        this.categoria                 = categoria;
        this.precioUnitario            = precioUnitario;
        this.estado                    = "Disponible";
        this.tipo                      = "No Perecible";
        this.fechaVencimiento          = null;
        this.temperaturaAlmacenamiento = 0.0;
        this.pesoKg                    = pesoKg;
        this.marca                     = marca;
        totalProductos++;
    }

    // ── Métodos estáticos ─────────────────────────────────────────────────────
    public static int  getTotalProductos() { return totalProductos; }
    public static void resetContador()     { totalProductos = 0; }

    // ── Lógica de precio ──────────────────────────────────────────────────────

    /**
     * Calcula el precio final según el tipo:
     *   - Perecible:    20% de descuento si vence en 3 días o menos.
     *   - No Perecible: 10% de descuento si pesoKg > 5.
     */
    public double calcularPrecioFinal() {
        if ("Perecible".equals(tipo)) {
            if (estaProximoAVencer()) {
                return precioUnitario * 0.80;
            }
        } else {
            if (pesoKg > 5.0) {
                return precioUnitario * 0.90;
            }
        }
        return precioUnitario;
    }

    /** Valor del descuento aplicado (precio unitario menos precio final). */
    public double calcularDescuento() {
        return precioUnitario - calcularPrecioFinal();
    }

    // ── Lógica de IVA ──────────────────────────────────────────────────────

    /**
     * Retorna la tarifa de IVA aplicable a este producto, como fracción
     * (ej. 0.15 = 15%, 0.0 = 0%).
     *
     * Regla:
     *   - Si la categoría del producto tiene una tarifa fija predeterminada
     *     (p. ej. canasta básica: lácteos, carnes, granos, enlatados → 0%),
     *     se usa esa tarifa fija, sin importar la tasa general vigente.
     *   - Caso contrario, se usa la tasa general vigente ({@link Dinero#getIva()}),
     *     que el administrador puede cambiar manualmente (p. ej. 15% → 8% en
     *     feriados con tarifa reducida).
     *
     * Así el cajero nunca decide el IVA manualmente: siempre sale del
     * producto (vía su categoría) o de la tasa general del sistema.
     */
    public double getTarifaIvaAplicable() {
        if (categoria != null && categoria.isIvaEspecial()) {
            return categoria.getTarifaIvaEspecial();
        }
        return Dinero.getIva();
    }

    // ── Lógica de vencimiento (solo aplica para Perecibles) ───────────────────

    /** Retorna true si la fecha de vencimiento ya pasó. */
    public boolean estaVencido() {
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.before(new Date());
    }

    /** Retorna true si el producto vence en 3 días o menos. */
    public boolean estaProximoAVencer() {
        if (fechaVencimiento == null) return false;
        long ahora      = new Date().getTime();
        long vencimiento = fechaVencimiento.getTime();
        long tresDiasMs = 3L * 24 * 60 * 60 * 1000;
        return (vencimiento - ahora) <= tresDiasMs;
    }

    // ── Getters y Setters comunes ─────────────────────────────────────────────
    public String    getCodigo()                { return codigo; }
    public void      setCodigo(String v)        { this.codigo = v; }
    public String    getNombre()                { return nombre; }
    public void      setNombre(String v)        { this.nombre = v; }
    public Categoria getCategoria()             { return categoria; }
    public void      setCategoria(Categoria v)  { this.categoria = v; }
    public double    getPrecioUnitario()        { return precioUnitario; }
    public void      setPrecioUnitario(double v){ this.precioUnitario = v; }
    public String    getEstado()               { return estado; }
    public void      setEstado(String v)        { this.estado = v; }
    public String    getTipo()                 { return tipo; }
    public void      setTipo(String v)          { this.tipo = v; }

    /** Retorna false si el producto fue inactivado (borrado lógico). */
    public boolean isActivo() { return !ESTADO_INACTIVO.equalsIgnoreCase(estado); }

    // ── Getters y Setters de Perecible ────────────────────────────────────────
    public Date   getFechaVencimiento()                { return fechaVencimiento; }
    public void   setFechaVencimiento(Date v)          { this.fechaVencimiento = v; }
    public double getTemperaturaAlmacenamiento()       { return temperaturaAlmacenamiento; }
    public void   setTemperaturaAlmacenamiento(double v){ this.temperaturaAlmacenamiento = v; }

    // ── Getters y Setters de No Perecible ─────────────────────────────────────
    public double getPesoKg()         { return pesoKg; }
    public void   setPesoKg(double v) { this.pesoKg = v; }
    public String getMarca()          { return marca; }
    public void   setMarca(String v)  { this.marca = v; }

    // ── equals, hashCode, toString ────────────────────────────────────────────
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Producto)) return false;
        return this.codigo.equals(((Producto) obj).codigo);
    }

    @Override
    public int hashCode() { return codigo.hashCode(); }

    @Override
    public String toString() {
        if ("Perecible".equals(tipo)) {
            return "Producto{codigo='" + codigo + "', nombre='" + nombre +
                    "', tipo=" + tipo +
                    ", categoria=" + (categoria != null ? categoria.getNombre() : "N/A") +
                    ", precioUnitario=" + precioUnitario +
                    ", precioFinal=" + calcularPrecioFinal() +
                    ", marca='" + marca +
                    "', vencimiento=" + fechaVencimiento +
                    ", temp=" + temperaturaAlmacenamiento + "°C" +
                    ", estado='" + estado + "'}";
        } else {
            return "Producto{codigo='" + codigo + "', nombre='" + nombre +
                    "', tipo=" + tipo +
                    ", categoria=" + (categoria != null ? categoria.getNombre() : "N/A") +
                    ", precioUnitario=" + precioUnitario +
                    ", precioFinal=" + calcularPrecioFinal() +
                    ", marca='" + marca +
                    "', pesoKg=" + pesoKg +
                    ", estado='" + estado + "'}";
        }
    }
}