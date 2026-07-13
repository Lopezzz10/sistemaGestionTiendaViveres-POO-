package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Compra;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Agrupa todos los parámetros OPCIONALES de una búsqueda de compras.
 *
 * Análogo a {@link CriteriosProducto} y {@link CriteriosVenta} pero para compras:
 * el llamador solo llena los campos que le interesan (setters encadenados,
 * estilo builder) y deja el resto sin tocar. {@link #aPredicate()} traduce
 * esos campos a funciones de {@link FiltrosCompra} y las combina con
 * {@link FiltrosCompra#combinar}.
 *
 * Ejemplo de uso:
 * <pre>{@code
 * CriteriosCompra criterios = new CriteriosCompra()
 *         .proveedor("Proveedor 1")
 *         .fechaDesde(inicioDeMes)
 *         .fechaHasta(hoy)
 *         .totalMin(100.0)
 *         .totalMax(500.0)
 *         .producto("Arroz")
 *         .categoria("LAC")
 *         .estado("RECIBIDA");
 *
 * ArrayList<Compra> resultado = fachada.buscarCompras(criterios);
 * }</pre>
 *
 * Agregar un nuevo criterio en el futuro solo requiere: un campo aquí,
 * un setter, y un filtro en {@link FiltrosCompra} — sin tocar el resto
 * del sistema.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CriteriosCompra {

    private String  proveedor;
    private Date    fechaDesde;
    private Date    fechaHasta;
    private Double  totalMin;
    private Double  totalMax;
    private String  producto;
    private String  categoria;
    private String  estado;

    /**
     * Filtra por nombre o RUC del proveedor (coincidencia parcial).
     */
    public CriteriosCompra proveedor(String proveedor) {
        this.proveedor = proveedor;
        return this;
    }

    /**
     * Filtra por fecha de la compra (desde).
     */
    public CriteriosCompra fechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
        return this;
    }

    /**
     * Filtra por fecha de la compra (hasta).
     */
    public CriteriosCompra fechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
        return this;
    }

    /**
     * Filtra por total mínimo de la compra.
     */
    public CriteriosCompra totalMin(Double totalMin) {
        this.totalMin = totalMin;
        return this;
    }

    /**
     * Filtra por total máximo de la compra.
     */
    public CriteriosCompra totalMax(Double totalMax) {
        this.totalMax = totalMax;
        return this;
    }

    /**
     * Filtra por nombre o código del producto incluido en la compra
     * (coincidencia parcial).
     */
    public CriteriosCompra producto(String producto) {
        this.producto = producto;
        return this;
    }

    /**
     * Filtra por clave de categoría de algún producto incluido en la compra.
     */
    public CriteriosCompra categoria(String categoria) {
        this.categoria = categoria;
        return this;
    }

    /**
     * Filtra por estado de la compra: PENDIENTE, RECIBIDA o ANULADA.
     */
    public CriteriosCompra estado(String estado) {
        this.estado = estado;
        return this;
    }

    /**
     * Retorna true si no se estableció ningún criterio (búsqueda "vacía").
     */
    public boolean estaVacio() {
        return (proveedor == null || proveedor.trim().isEmpty())
                && fechaDesde == null
                && fechaHasta == null
                && totalMin == null
                && totalMax == null
                && (producto == null || producto.trim().isEmpty())
                && (categoria == null || categoria.trim().isEmpty())
                && (estado == null || estado.trim().isEmpty());
    }

    /**
     * Traduce estos criterios a un único {@link Predicate}&lt;{@link Compra}&gt;,
     * combinando (con AND) solo los filtros correspondientes a los campos
     * que sí fueron establecidos.
     */
    public Predicate<Compra> aPredicate() {
        return FiltrosCompra.combinar(
                FiltrosCompra.porProveedor(proveedor),
                FiltrosCompra.porRangoFechas(fechaDesde, fechaHasta),
                FiltrosCompra.totalMinimo(totalMin),
                FiltrosCompra.totalMaximo(totalMax),
                FiltrosCompra.porProducto(producto),
                FiltrosCompra.porCategoria(categoria),
                FiltrosCompra.porEstado(estado)
        );
    }

    // ── Getters (opcionales, para depuración) ──────────────────────────────

    public String getProveedor() { return proveedor; }
    public Date getFechaDesde() { return fechaDesde; }
    public Date getFechaHasta() { return fechaHasta; }
    public Double getTotalMin() { return totalMin; }
    public Double getTotalMax() { return totalMax; }
    public String getProducto() { return producto; }
    public String getCategoria() { return categoria; }
    public String getEstado() { return estado; }

    @Override
    public String toString() {
        return "CriteriosCompra{" +
                "proveedor='" + proveedor + '\'' +
                ", fechaDesde=" + fechaDesde +
                ", fechaHasta=" + fechaHasta +
                ", totalMin=" + totalMin +
                ", totalMax=" + totalMax +
                ", producto='" + producto + '\'' +
                ", categoria='" + categoria + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}