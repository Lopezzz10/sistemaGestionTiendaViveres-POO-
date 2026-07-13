package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Inventario;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Agrupa todos los parámetros OPCIONALES de una búsqueda de inventarios.
 *
 * Análogo a {@link CriteriosProducto}, {@link CriteriosProveedor}, {@link CriteriosCompra},
 * {@link CriteriosVenta} y {@link CriteriosFactura}: el llamador solo llena
 * los campos que le interesan (setters encadenados, estilo builder) y deja
 * el resto sin tocar. {@link #aPredicate()} traduce esos campos a funciones
 * de {@link FiltrosInventario} y las combina con {@link FiltrosInventario#combinar}.
 *
 * Ejemplo de uso:
 * <pre>{@code
 * CriteriosInventario criterios = new CriteriosInventario()
 *         .producto("Arroz")
 *         .stockMin(0)
 *         .stockMax(5)
 *         .soloConAlerta(true);
 *
 * ArrayList<Inventario> resultado = fachada.buscarInventarios(criterios);
 * }</pre>
 *
 * Agregar un nuevo criterio en el futuro solo requiere: un campo aquí,
 * un setter, y un filtro en {@link FiltrosInventario} — sin tocar el resto
 * del sistema.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CriteriosInventario {

    private String  producto;
    private String  categoria;
    private Integer stockMin;
    private Integer stockMax;
    private Date    fechaDesde;
    private Date    fechaHasta;
    private boolean soloConAlerta;
    private boolean soloActivos;

    /** Filtra por nombre o código del producto asociado (coincidencia parcial). */
    public CriteriosInventario producto(String producto) {
        this.producto = producto;
        return this;
    }

    /** Filtra por clave de categoría del producto asociado. */
    public CriteriosInventario categoria(String categoria) {
        this.categoria = categoria;
        return this;
    }

    /** Filtra por cantidad disponible mínima. */
    public CriteriosInventario stockMin(Integer stockMin) {
        this.stockMin = stockMin;
        return this;
    }

    /** Filtra por cantidad disponible máxima. */
    public CriteriosInventario stockMax(Integer stockMax) {
        this.stockMax = stockMax;
        return this;
    }

    /** Filtra por fecha de última actualización (desde). */
    public CriteriosInventario fechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
        return this;
    }

    /** Filtra por fecha de última actualización (hasta). */
    public CriteriosInventario fechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
        return this;
    }

    /** Filtra solo inventarios que requieren alerta (stock &lt;= umbral). */
    public CriteriosInventario soloConAlerta(boolean soloConAlerta) {
        this.soloConAlerta = soloConAlerta;
        return this;
    }

    /** Filtra solo inventarios activos (no inactivados/borrado lógico). */
    public CriteriosInventario soloActivos(boolean soloActivos) {
        this.soloActivos = soloActivos;
        return this;
    }

    /** Retorna true si no se estableció ningún criterio (búsqueda "vacía"). */
    public boolean estaVacio() {
        return (producto == null || producto.trim().isEmpty())
                && (categoria == null || categoria.trim().isEmpty())
                && stockMin == null
                && stockMax == null
                && fechaDesde == null
                && fechaHasta == null
                && !soloConAlerta
                && !soloActivos;
    }

    /**
     * Traduce estos criterios a un único {@link Predicate}&lt;{@link Inventario}&gt;,
     * combinando (con AND) solo los filtros correspondientes a los campos
     * que sí fueron establecidos.
     */
    public Predicate<Inventario> aPredicate() {
        return FiltrosInventario.combinar(
                FiltrosInventario.porProducto(producto),
                FiltrosInventario.porCategoria(categoria),
                FiltrosInventario.stockMinimo(stockMin),
                FiltrosInventario.stockMaximo(stockMax),
                FiltrosInventario.porRangoFechas(fechaDesde, fechaHasta),
                soloConAlerta ? FiltrosInventario.conAlerta() : FiltrosInventario.sinFiltro(),
                soloActivos ? FiltrosInventario.soloActivos() : FiltrosInventario.sinFiltro()
        );
    }

    // ── Getters (opcionales, para depuración) ──────────────────────────────

    public String getProducto() { return producto; }
    public String getCategoria() { return categoria; }
    public Integer getStockMin() { return stockMin; }
    public Integer getStockMax() { return stockMax; }
    public Date getFechaDesde() { return fechaDesde; }
    public Date getFechaHasta() { return fechaHasta; }
    public boolean isSoloConAlerta() { return soloConAlerta; }
    public boolean isSoloActivos() { return soloActivos; }

    @Override
    public String toString() {
        return "CriteriosInventario{" +
                "producto='" + producto + '\'' +
                ", categoria='" + categoria + '\'' +
                ", stockMin=" + stockMin +
                ", stockMax=" + stockMax +
                ", fechaDesde=" + fechaDesde +
                ", fechaHasta=" + fechaHasta +
                ", soloConAlerta=" + soloConAlerta +
                ", soloActivos=" + soloActivos +
                '}';
    }
}