package edu.uce.programacion2.tienda.negocio;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Agrupa todos los parámetros OPCIONALES de una búsqueda de facturas.
 *
 * Análogo a {@link CriteriosProducto}, {@link CriteriosVenta} y {@link CriteriosCompra}
 * pero para facturas: el llamador solo llena los campos que le interesan
 * (setters encadenados, estilo builder) y deja el resto sin tocar.
 * {@link #aPredicate()} traduce esos campos a funciones de {@link FiltrosFactura}
 * y las combina con {@link FiltrosFactura#combinar}.
 *
 * Ejemplo de uso:
 * <pre>{@code
 * CriteriosFactura criterios = new CriteriosFactura()
 *         .fechaDesde(inicioDeMes)
 *         .fechaHasta(hoy)
 *         .totalMin(100.0)
 *         .totalMax(500.0)
 *         .cliente("Juan")
 *         .numeroFactura("001-001")
 *         .soloConIvaEspecial(true)
 *         .estado("EMITIDA");
 *
 * ArrayList<Factura> resultado = fachada.buscarFacturas(criterios);
 * }</pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CriteriosFactura {

    private Date    fechaDesde;
    private Date    fechaHasta;
    private Double  totalMin;
    private Double  totalMax;
    private String  cliente;
    private String  numeroFactura;
    private Boolean soloConIvaEspecial;
    private String  estado;

    /**
     * Filtra por fecha de emisión de la factura (desde).
     */
    public CriteriosFactura fechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
        return this;
    }

    /**
     * Filtra por fecha de emisión de la factura (hasta).
     */
    public CriteriosFactura fechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
        return this;
    }

    /**
     * Filtra por total mínimo de la factura.
     */
    public CriteriosFactura totalMin(Double totalMin) {
        this.totalMin = totalMin;
        return this;
    }

    /**
     * Filtra por total máximo de la factura.
     */
    public CriteriosFactura totalMax(Double totalMax) {
        this.totalMax = totalMax;
        return this;
    }

    /**
     * Filtra por nombre o email del cliente (coincidencia parcial).
     */
    public CriteriosFactura cliente(String cliente) {
        this.cliente = cliente;
        return this;
    }

    /**
     * Filtra por número de factura (coincidencia parcial).
     */
    public CriteriosFactura numeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
        return this;
    }

    /**
     * Filtra solo facturas que contienen productos con IVA especial.
     * @param soloConIvaEspecial true para filtrar solo facturas con IVA especial
     */
    public CriteriosFactura soloConIvaEspecial(Boolean soloConIvaEspecial) {
        this.soloConIvaEspecial = soloConIvaEspecial;
        return this;
    }

    /**
     * Filtra por estado de la factura: EMITIDA, ANULADA o PAGADA.
     */
    public CriteriosFactura estado(String estado) {
        this.estado = estado;
        return this;
    }

    /**
     * Retorna true si no se estableció ningún criterio (búsqueda "vacía").
     */
    public boolean estaVacio() {
        return fechaDesde == null
                && fechaHasta == null
                && totalMin == null
                && totalMax == null
                && (cliente == null || cliente.trim().isEmpty())
                && (numeroFactura == null || numeroFactura.trim().isEmpty())
                && soloConIvaEspecial == null
                && (estado == null || estado.trim().isEmpty());
    }

    /**
     * Traduce estos criterios a un único {@link Predicate}&lt;{@link Factura}&gt;,
     * combinando (con AND) solo los filtros correspondientes a los campos
     * que sí fueron establecidos.
     */
    public Predicate<Factura> aPredicate() {
        return FiltrosFactura.combinar(
                FiltrosFactura.porRangoFechas(fechaDesde, fechaHasta),
                FiltrosFactura.totalMinimo(totalMin),
                FiltrosFactura.totalMaximo(totalMax),
                FiltrosFactura.porCliente(cliente),
                FiltrosFactura.porNumeroFactura(numeroFactura),
                FiltrosFactura.soloConIvaEspecial(soloConIvaEspecial),
                FiltrosFactura.porEstado(estado)
        );
    }

    // ── Getters (opcionales, para depuración) ──────────────────────────────

    public Date getFechaDesde() { return fechaDesde; }
    public Date getFechaHasta() { return fechaHasta; }
    public Double getTotalMin() { return totalMin; }
    public Double getTotalMax() { return totalMax; }
    public String getCliente() { return cliente; }
    public String getNumeroFactura() { return numeroFactura; }
    public Boolean getSoloConIvaEspecial() { return soloConIvaEspecial; }
    public String getEstado() { return estado; }

    @Override
    public String toString() {
        return "CriteriosFactura{" +
                "fechaDesde=" + fechaDesde +
                ", fechaHasta=" + fechaHasta +
                ", totalMin=" + totalMin +
                ", totalMax=" + totalMax +
                ", cliente='" + cliente + '\'' +
                ", numeroFactura='" + numeroFactura + '\'' +
                ", soloConIvaEspecial=" + soloConIvaEspecial +
                ", estado='" + estado + '\'' +
                '}';
    }
}