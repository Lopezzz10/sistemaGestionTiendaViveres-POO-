package edu.uce.programacion2.tienda.negocio;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Fábrica de {@link Predicate}&lt;{@link Compra}&gt; reutilizables.
 *
 * Mismo espíritu que {@link FiltrosProducto} y {@link FiltrosVenta}:
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
public final class FiltrosCompra {

    /** Clase de utilidades: no se instancia. */
    private FiltrosCompra() { }

    /**
     * Predicado neutro: no filtra nada (siempre true).
     * Útil como valor por defecto.
     */
    public static Predicate<Compra> sinFiltro() {
        return c -> true;
    }

    /**
     * Coincide si el nombre o RUC del proveedor CONTIENE el texto dado
     * (insensible a mayúsculas).
     *
     * @param texto texto a buscar en nombre o RUC del proveedor
     * @return Predicate que filtra por proveedor
     */
    public static Predicate<Compra> porProveedor(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return c -> {
            if (c.getProveedor() == null) return false;
            String nombre = c.getProveedor().getNombre();
            String ruc = c.getProveedor().getRuc();
            return (nombre != null && nombre.toLowerCase().contains(buscado))
                    || (ruc != null && ruc.toLowerCase().contains(buscado));
        };
    }

    /**
     * Coincide si la fecha de la compra es &gt;= desde y &lt;= hasta
     * (ambos límites incluidos).
     *
     * @param desde fecha mínima (inclusive), null para no limitar
     * @param hasta fecha máxima (inclusive), null para no limitar
     * @return Predicate que filtra por rango de fechas
     */
    public static Predicate<Compra> porRangoFechas(Date desde, Date hasta) {
        if (desde == null && hasta == null) return sinFiltro();
        return c -> {
            if (c.getFecha() == null) return false;
            Date fecha = c.getFecha();
            if (desde != null && fecha.before(desde)) return false;
            if (hasta != null && fecha.after(hasta)) return false;
            return true;
        };
    }

    /**
     * Coincide si el total de la compra es mayor o igual al mínimo dado.
     *
     * @param min valor mínimo (inclusive), null para no limitar
     * @return Predicate que filtra por total mínimo
     */
    public static Predicate<Compra> totalMinimo(Double min) {
        if (min == null) return sinFiltro();
        return c -> c.getTotal() >= min;
    }

    /**
     * Coincide si el total de la compra es menor o igual al máximo dado.
     *
     * @param max valor máximo (inclusive), null para no limitar
     * @return Predicate que filtra por total máximo
     */
    public static Predicate<Compra> totalMaximo(Double max) {
        if (max == null) return sinFiltro();
        return c -> c.getTotal() <= max;
    }

    /**
     * Coincide si algún detalle de la compra incluye un producto cuyo nombre
     * o código CONTIENE el texto dado (insensible a mayúsculas).
     *
     * @param texto texto a buscar en nombre o código del producto
     * @return Predicate que filtra por producto incluido
     */
    public static Predicate<Compra> porProducto(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return c -> c.getDetalles().stream()
                .anyMatch(d -> {
                    if (d.getProducto() == null) return false;
                    String nombre = d.getProducto().getNombre();
                    String codigo = d.getProducto().getCodigo();
                    return (nombre != null && nombre.toLowerCase().contains(buscado))
                            || (codigo != null && codigo.toLowerCase().contains(buscado));
                });
    }

    /**
     * Coincide si algún detalle de la compra incluye un producto de la
     * categoría indicada (por clave).
     *
     * @param cveCategoria clave de la categoría a buscar
     * @return Predicate que filtra por categoría
     */
    public static Predicate<Compra> porCategoria(String cveCategoria) {
        if (cveCategoria == null || cveCategoria.trim().isEmpty()) return sinFiltro();
        String cve = cveCategoria.trim();
        return c -> c.getDetalles().stream()
                .anyMatch(d -> d.getProducto() != null
                        && d.getProducto().getCategoria() != null
                        && d.getProducto().getCategoria().getCveCategoria().equalsIgnoreCase(cve));
    }

    /**
     * Coincide si el estado de la compra es exactamente el indicado.
     *
     * @param estado "PENDIENTE", "RECIBIDA" o "ANULADA" (insensible a mayúsculas)
     * @return Predicate que filtra por estado
     */
    public static Predicate<Compra> porEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) return sinFiltro();
        String est = estado.trim().toUpperCase();
        return c -> c.getEstado() != null && c.getEstado().name().equalsIgnoreCase(est);
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
    public static Predicate<Compra> combinar(Predicate<Compra>... filtros) {
        Predicate<Compra> resultado = sinFiltro();
        for (Predicate<Compra> f : filtros) {
            if (f != null) resultado = resultado.and(f);
        }
        return resultado;
    }
}