package edu.uce.programacion2.tienda.negocio;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Fábrica de {@link Predicate}&lt;{@link Factura}&gt; reutilizables.
 *
 * Se filtra sobre {@link Factura} (y no directamente sobre {@link Venta})
 * porque es la Factura la que conoce al {@link Cliente}; de ahí se accede
 * a la {@link Venta} asociada (fecha, total, método de pago, detalles)
 * con {@link Factura#getVenta()}.
 *
 * Mismo espíritu que {@link FiltrosProducto}: cada método es un criterio
 * puro e independiente, "seguro" ante parámetros null/vacíos (en ese caso
 * no filtra nada), pensado para combinarse con {@link #combinar}.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public final class FiltrosVenta {

    /** Clase de utilidades: no se instancia. */
    private FiltrosVenta() { }

    /** Predicado neutro: no filtra nada (siempre true). Útil como valor por defecto. */
    public static Predicate<Factura> sinFiltro() {
        return f -> true;
    }

    /** Coincide si la fecha de la venta es &gt;= desde y &lt;= hasta (ambos límites incluidos). */
    public static Predicate<Factura> porRangoFechas(Date desde, Date hasta) {
        if (desde == null && hasta == null) return sinFiltro();
        return f -> {
            if (f.getVenta() == null || f.getVenta().getFecha() == null) return false;
            Date fecha = f.getVenta().getFecha();
            if (desde != null && fecha.before(desde)) return false;
            if (hasta != null && fecha.after(hasta)) return false;
            return true;
        };
    }

    /**
     * Coincide si el cliente de la factura tiene un nombre o email que
     * CONTIENE el texto dado (insensible a mayúsculas).
     */
    public static Predicate<Factura> porCliente(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return f -> f.getCliente() != null && (
                (f.getCliente().getNombre() != null && f.getCliente().getNombre().toLowerCase().contains(buscado))
                        || (f.getCliente().getEmail()  != null && f.getCliente().getEmail().toLowerCase().contains(buscado))
        );
    }

    /** Coincide si el cliente de la factura es exactamente el id de cliente dado. */
    public static Predicate<Factura> porClienteId(Integer idCliente) {
        if (idCliente == null) return sinFiltro();
        return f -> f.getCliente() != null && f.getCliente().getIdUsuario() == idCliente;
    }

    /** Coincide si el método de pago de la venta es exactamente el indicado. */
    public static Predicate<Factura> porMetodoPago(String metodoPago) {
        if (metodoPago == null || metodoPago.trim().isEmpty()) return sinFiltro();
        String mp = metodoPago.trim();
        return f -> f.getVenta() != null
                && f.getVenta().getMetodoPago() != null
                && f.getVenta().getMetodoPago().equalsIgnoreCase(mp);
    }

    /** Coincide si el total de la venta es mayor o igual al mínimo dado. */
    public static Predicate<Factura> totalMinimo(Double min) {
        if (min == null) return sinFiltro();
        return f -> f.getVenta() != null && f.getVenta().getTotal() >= min;
    }

    /** Coincide si el total de la venta es menor o igual al máximo dado. */
    public static Predicate<Factura> totalMaximo(Double max) {
        if (max == null) return sinFiltro();
        return f -> f.getVenta() != null && f.getVenta().getTotal() <= max;
    }

    /** Coincide solo con ventas que tienen al menos un detalle con descuento aplicado (&gt; 0). */
    public static Predicate<Factura> conDescuento() {
        return f -> f.getVenta() != null && f.getVenta().getDetalles().stream()
                .anyMatch(d -> d.getDescuento() > 0);
    }

    /**
     * Coincide si algún detalle de la venta incluye un producto cuyo nombre
     * o código CONTIENE el texto dado (insensible a mayúsculas).
     */
    public static Predicate<Factura> porProducto(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return f -> f.getVenta() != null && f.getVenta().getDetalles().stream()
                .anyMatch(d -> d.getProducto() != null && (
                        (d.getProducto().getNombre() != null && d.getProducto().getNombre().toLowerCase().contains(buscado))
                                || (d.getProducto().getCodigo() != null && d.getProducto().getCodigo().toLowerCase().contains(buscado))
                ));
    }

    /** Coincide si algún detalle de la venta incluye un producto de la categoría indicada (por clave). */
    public static Predicate<Factura> porCategoria(String cveCategoria) {
        if (cveCategoria == null || cveCategoria.trim().isEmpty()) return sinFiltro();
        String cve = cveCategoria.trim();
        return f -> f.getVenta() != null && f.getVenta().getDetalles().stream()
                .anyMatch(d -> d.getProducto() != null
                        && d.getProducto().getCategoria() != null
                        && d.getProducto().getCategoria().getCveCategoria().equalsIgnoreCase(cve));
    }

    /**
     * Combina cualquier cantidad de filtros con AND lógico.
     * Un filtro null se ignora (equivale a no restringir por ese criterio).
     */
    @SafeVarargs
    public static Predicate<Factura> combinar(Predicate<Factura>... filtros) {
        Predicate<Factura> resultado = sinFiltro();
        for (Predicate<Factura> f : filtros) {
            if (f != null) resultado = resultado.and(f);
        }
        return resultado;
    }
}