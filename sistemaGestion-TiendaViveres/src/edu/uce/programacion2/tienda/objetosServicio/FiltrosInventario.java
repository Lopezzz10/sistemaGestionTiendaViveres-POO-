package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Inventario;

import java.util.Date;
import java.util.function.Predicate;

/**
 * Fábrica de {@link Predicate}&lt;{@link Inventario}&gt; reutilizables.
 *
 * Mismo espíritu que {@link FiltrosProducto}, {@link FiltrosProveedor},
 * {@link FiltrosVenta}, {@link FiltrosCompra} y {@link FiltrosFactura}:
 * cada método es un criterio puro e independiente, "seguro" ante
 * parámetros null/vacíos (en ese caso no filtra nada), pensado para
 * combinarse con {@link #combinar}.
 *
 * Todos los filtros son "seguros": si el parámetro recibido es null o
 * vacío, el filtro simplemente no descarta nada (equivale a no aplicar
 * ese criterio). Así el llamador puede pasar solo los parámetros que le
 * interesen y dejar el resto en null.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public final class FiltrosInventario {

    /** Clase de utilidades: no se instancia. */
    private FiltrosInventario() { }

    /** Predicado neutro: no filtra nada (siempre true). Útil como valor por defecto. */
    public static Predicate<Inventario> sinFiltro() {
        return inv -> true;
    }

    /**
     * Coincide si el nombre o código del producto asociado CONTIENE el
     * texto dado (insensible a mayúsculas).
     */
    public static Predicate<Inventario> porProducto(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return inv -> {
            if (inv.getProducto() == null) return false;
            String nombre = inv.getProducto().getNombre();
            String codigo = inv.getProducto().getCodigo();
            return (nombre != null && nombre.toLowerCase().contains(buscado))
                    || (codigo != null && codigo.toLowerCase().contains(buscado));
        };
    }

    /** Coincide si el producto asociado pertenece a la categoría indicada (por clave). */
    public static Predicate<Inventario> porCategoria(String cveCategoria) {
        if (cveCategoria == null || cveCategoria.trim().isEmpty()) return sinFiltro();
        String cve = cveCategoria.trim();
        return inv -> inv.getProducto() != null
                && inv.getProducto().getCategoria() != null
                && inv.getProducto().getCategoria().getCveCategoria().equalsIgnoreCase(cve);
    }

    /** Coincide si la cantidad disponible es mayor o igual al mínimo dado. */
    public static Predicate<Inventario> stockMinimo(Integer min) {
        return (min == null) ? sinFiltro() : inv -> inv.getCantidadDisponible() >= min;
    }

    /** Coincide si la cantidad disponible es menor o igual al máximo dado. */
    public static Predicate<Inventario> stockMaximo(Integer max) {
        return (max == null) ? sinFiltro() : inv -> inv.getCantidadDisponible() <= max;
    }

    /**
     * Coincide si la fecha de última actualización es &gt;= desde y
     * &lt;= hasta (ambos límites incluidos).
     */
    public static Predicate<Inventario> porRangoFechas(Date desde, Date hasta) {
        if (desde == null && hasta == null) return sinFiltro();
        return inv -> {
            if (inv.getFechaActualizacion() == null) return false;
            Date fecha = inv.getFechaActualizacion();
            if (desde != null && fecha.before(desde)) return false;
            if (hasta != null && fecha.after(hasta)) return false;
            return true;
        };
    }

    /** Coincide solo con inventarios que requieren alerta (stock &lt;= umbral). */
    public static Predicate<Inventario> conAlerta() {
        return Inventario::requiereAlerta;
    }

    /** Coincide solo con inventarios activos (no inactivados/borrado lógico). */
    public static Predicate<Inventario> soloActivos() {
        return Inventario::isActivo;
    }

    /**
     * Combina cualquier cantidad de filtros con AND lógico.
     *
     * Este es el mecanismo que permite las "consultas con varios
     * parámetros": cada filtro es independiente y esta función los une
     * en uno solo. Un filtro null se ignora (equivale a no restringir
     * por ese criterio), así el llamador puede pasar tranquilamente
     * predicados que vengan de parámetros opcionales.
     */
    @SafeVarargs
    public static Predicate<Inventario> combinar(Predicate<Inventario>... filtros) {
        Predicate<Inventario> resultado = sinFiltro();
        for (Predicate<Inventario> f : filtros) {
            if (f != null) resultado = resultado.and(f);
        }
        return resultado;
    }
}