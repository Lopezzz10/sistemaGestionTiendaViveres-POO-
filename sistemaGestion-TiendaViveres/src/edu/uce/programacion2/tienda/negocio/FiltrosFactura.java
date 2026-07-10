package edu.uce.programacion2.tienda.negocio;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Fábrica de {@link Predicate}&lt;{@link Factura}&gt; reutilizables.
 *
 * Mismo espíritu que {@link FiltrosProducto}, {@link FiltrosVenta} y {@link FiltrosCompra}:
 * cada método es un criterio puro e independiente, "seguro" ante parámetros
 * null/vacíos (en ese caso no filtra nada), pensado para combinarse con
 * {@link #combinar}.
 *
 * Todos los filtros son "seguros": si el parámetro recibido es null o
 * vacío, el filtro simplemente no descarta nada (equivale a no aplicar
 * ese criterio). Así el llamador puede pasar solo los parámetros que le
 * interesen y dejar el resto en null.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public final class FiltrosFactura {

    /** Clase de utilidades: no se instancia. */
    private FiltrosFactura() { }

    /**
     * Predicado neutro: no filtra nada (siempre true).
     * Útil como valor por defecto.
     */
    public static Predicate<Factura> sinFiltro() {
        return f -> true;
    }

    /**
     * Coincide si la fecha de emisión de la factura es &gt;= desde y &lt;= hasta
     * (ambos límites incluidos).
     *
     * @param desde fecha mínima (inclusive), null para no limitar
     * @param hasta fecha máxima (inclusive), null para no limitar
     * @return Predicate que filtra por rango de fechas
     */
    public static Predicate<Factura> porRangoFechas(Date desde, Date hasta) {
        if (desde == null && hasta == null) return sinFiltro();
        return f -> {
            if (f.getFechaEmision() == null) return false;
            Date fecha = f.getFechaEmision();
            if (desde != null && fecha.before(desde)) return false;
            if (hasta != null && fecha.after(hasta)) return false;
            return true;
        };
    }

    /**
     * Coincide si el total de la factura es mayor o igual al mínimo dado.
     *
     * @param min valor mínimo (inclusive), null para no limitar
     * @return Predicate que filtra por total mínimo
     */
    public static Predicate<Factura> totalMinimo(Double min) {
        if (min == null) return sinFiltro();
        return f -> f.getTotal() >= min;
    }

    /**
     * Coincide si el total de la factura es menor o igual al máximo dado.
     *
     * @param max valor máximo (inclusive), null para no limitar
     * @return Predicate que filtra por total máximo
     */
    public static Predicate<Factura> totalMaximo(Double max) {
        if (max == null) return sinFiltro();
        return f -> f.getTotal() <= max;
    }

    /**
     * Coincide si el cliente de la factura tiene un nombre o email que
     * CONTIENE el texto dado (insensible a mayúsculas).
     *
     * @param texto texto a buscar en nombre o email del cliente
     * @return Predicate que filtra por cliente
     */
    public static Predicate<Factura> porCliente(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return f -> f.getCliente() != null && (
                (f.getCliente().getNombre() != null && f.getCliente().getNombre().toLowerCase().contains(buscado))
                        || (f.getCliente().getEmail() != null && f.getCliente().getEmail().toLowerCase().contains(buscado))
        );
    }

    /**
     * Coincide si el número de factura CONTIENE el texto dado
     * (insensible a mayúsculas).
     *
     * @param texto texto a buscar en el número de factura
     * @return Predicate que filtra por número de factura
     */
    public static Predicate<Factura> porNumeroFactura(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return f -> f.getNumeroFactura() != null
                && f.getNumeroFactura().toLowerCase().contains(buscado);
    }

    /**
     * Coincide solo con facturas que tienen al menos un detalle con IVA especial.
     * El IVA especial se determina verificando si el producto tiene una categoría
     * con {@link Categoria#isIvaEspecial()} = true.
     *
     * @param soloConIvaEspecial true para filtrar solo facturas con IVA especial,
     *                           false para filtrar solo sin IVA especial,
     *                           null para no filtrar por este criterio
     * @return Predicate que filtra por IVA especial
     */
    public static Predicate<Factura> soloConIvaEspecial(Boolean soloConIvaEspecial) {
        if (soloConIvaEspecial == null) return sinFiltro();
        return f -> f.getDetalles().stream()
                .anyMatch(d -> d.getProducto() != null
                        && d.getProducto().getCategoria() != null
                        && d.getProducto().getCategoria().isIvaEspecial()) == soloConIvaEspecial;
    }

    /**
     * Coincide si el estado de la factura es exactamente el indicado.
     *
     * @param estado "EMITIDA", "ANULADA" o "PAGADA" (insensible a mayúsculas)
     * @return Predicate que filtra por estado
     */
    public static Predicate<Factura> porEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) return sinFiltro();
        String est = estado.trim().toUpperCase();
        return f -> f.getEstado() != null && f.getEstado().name().equalsIgnoreCase(est);
    }

    /**
     * Combina cualquier cantidad de filtros con AND lógico.
     *
     * Este es el mecanismo que permite las "consultas con varios
     * parámetros": cada filtro es independiente y esta función los une
     * en uno solo. Un filtro null se ignora (equivale a no restringir
     * por ese criterio), así el llamador puede pasar tranquilamente
     * predicados que vengan de parámetros opcionales.
     *
     * @param filtros lista de predicados a combinar
     * @return un único Predicate que es la conjunción (AND) de todos
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