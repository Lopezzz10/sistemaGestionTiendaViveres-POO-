package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Factura;
import edu.uce.programacion2.tienda.negocio.Venta;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Agrupa todos los parámetros OPCIONALES de una búsqueda de ventas.
 *
 * Análogo a {@link CriteriosProducto} pero para ventas: el llamador solo
 * llena los campos que le interesan (setters encadenados, estilo builder)
 * y deja el resto sin tocar. {@link #aPredicate()} traduce esos campos a
 * funciones de {@link FiltrosVenta} y las combina con
 * {@link FiltrosVenta#combinar}.
 *
 * Se filtra sobre {@link Factura} (no sobre {@link Venta} directamente)
 * porque el cliente de una venta solo se conoce a través de su factura.
 *
 * Ejemplo de uso:
 * <pre>{@code
 * CriteriosVenta criterios = new CriteriosVenta()
 *         .fechaDesde(inicioDeMes)
 *         .fechaHasta(hoy)
 *         .metodoPago("Efectivo")
 *         .soloConDescuento(true)
 *         .categoria("LAC");
 *
 * ArrayList<Factura> resultado = fachada.buscarFacturas(criterios);
 * }</pre>
 *
 * Agregar un nuevo criterio en el futuro solo requiere: un campo aquí,
 * un setter, y un filtro en {@link FiltrosVenta} — sin tocar el resto
 * del sistema.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CriteriosVenta {

    private Date    fechaDesde;
    private Date    fechaHasta;
    private String  cliente;
    private Integer idCliente;
    private String  metodoPago;
    private Double  totalMin;
    private Double  totalMax;
    private boolean soloConDescuento;
    private String  producto;
    private String  categoria;

    public CriteriosVenta fechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
        return this;
    }

    public CriteriosVenta fechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
        return this;
    }

    /** Filtra por nombre o email del cliente (coincidencia parcial). */
    public CriteriosVenta cliente(String cliente) {
        this.cliente = cliente;
        return this;
    }

    /** Filtra por id exacto de cliente (alternativa a {@link #cliente(String)}). */
    public CriteriosVenta idCliente(Integer idCliente) {
        this.idCliente = idCliente;
        return this;
    }

    public CriteriosVenta metodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
        return this;
    }

    public CriteriosVenta totalMin(Double totalMin) {
        this.totalMin = totalMin;
        return this;
    }

    public CriteriosVenta totalMax(Double totalMax) {
        this.totalMax = totalMax;
        return this;
    }

    public CriteriosVenta soloConDescuento(boolean soloConDescuento) {
        this.soloConDescuento = soloConDescuento;
        return this;
    }

    /** Filtra por nombre o código de producto incluido en la venta (coincidencia parcial). */
    public CriteriosVenta producto(String producto) {
        this.producto = producto;
        return this;
    }

    /** Filtra por clave de categoría de algún producto incluido en la venta. */
    public CriteriosVenta categoria(String categoria) {
        this.categoria = categoria;
        return this;
    }

    /** Retorna true si no se estableció ningún criterio (búsqueda "vacía"). */
    public boolean estaVacio() {
        return fechaDesde == null
                && fechaHasta == null
                && (cliente == null || cliente.trim().isEmpty())
                && idCliente == null
                && (metodoPago == null || metodoPago.trim().isEmpty())
                && totalMin == null
                && totalMax == null
                && !soloConDescuento
                && (producto == null || producto.trim().isEmpty())
                && (categoria == null || categoria.trim().isEmpty());
    }

    /**
     * Traduce estos criterios a un único {@link Predicate}&lt;{@link Factura}&gt;,
     * combinando (con AND) solo los filtros correspondientes a los campos
     * que sí fueron establecidos.
     */
    public Predicate<Factura> aPredicate() {
        return FiltrosVenta.combinar(
                FiltrosVenta.porRangoFechas(fechaDesde, fechaHasta),
                FiltrosVenta.porCliente(cliente),
                FiltrosVenta.porClienteId(idCliente),
                FiltrosVenta.porMetodoPago(metodoPago),
                FiltrosVenta.totalMinimo(totalMin),
                FiltrosVenta.totalMaximo(totalMax),
                soloConDescuento ? FiltrosVenta.conDescuento() : FiltrosVenta.sinFiltro(),
                FiltrosVenta.porProducto(producto),
                FiltrosVenta.porCategoria(categoria)
        );
    }
}