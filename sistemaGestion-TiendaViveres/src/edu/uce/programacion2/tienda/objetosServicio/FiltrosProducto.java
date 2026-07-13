package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Producto;

import java.util.function.Predicate;

/**
 * Fábrica de {@link Predicate}&lt;{@link Producto}&gt; reutilizables.
 *
 * Cada método representa UN criterio de búsqueda como una función pura,
 * sin efectos secundarios. La idea de programación funcional es que estos
 * criterios se puedan combinar libremente (AND) para armar consultas con
 * varios parámetros a la vez, sin escribir un método nuevo por cada
 * combinación posible (como pasaría con el enfoque imperativo clásico de
 * "un método con 8 parámetros y muchos if").
 *
 * Todos los filtros son "seguros": si el parámetro recibido es null o
 * vacío, el filtro simplemente no descarta nada (equivale a no aplicar
 * ese criterio). Así el llamador puede pasar solo los parámetros que le
 * interesen y dejar el resto en null.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public final class FiltrosProducto {

    /** Clase de utilidades: no se instancia. */
    private FiltrosProducto() { }

    /** Predicado neutro: no filtra nada (siempre true). Útil como valor por defecto. */
    public static Predicate<Producto> sinFiltro() {
        return p -> true;
    }

    /** Coincide si el nombre del producto CONTIENE el texto dado (insensible a mayúsculas). */
    public static Predicate<Producto> porNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return p -> p.getNombre() != null && p.getNombre().toLowerCase().contains(buscado);
    }

    /** Coincide si el producto pertenece a la categoría indicada (por clave). */
    public static Predicate<Producto> porCategoria(String cveCategoria) {
        if (cveCategoria == null || cveCategoria.trim().isEmpty()) return sinFiltro();
        String cve = cveCategoria.trim();
        return p -> p.getCategoria() != null
                && p.getCategoria().getCveCategoria().equalsIgnoreCase(cve);
    }

    /** Coincide si el tipo es exactamente "Perecible" o "No Perecible". */
    public static Predicate<Producto> porTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) return sinFiltro();
        String t = tipo.trim();
        return p -> p.getTipo() != null && p.getTipo().equalsIgnoreCase(t);
    }

    /** Coincide si la marca es exactamente la indicada (solo aplica a No Perecibles). */
    public static Predicate<Producto> porMarca(String marca) {
        if (marca == null || marca.trim().isEmpty()) return sinFiltro();
        String m = marca.trim();
        return p -> p.getMarca() != null && p.getMarca().equalsIgnoreCase(m);
    }

    /** Coincide si el precio unitario es mayor o igual al mínimo dado. */
    public static Predicate<Producto> precioMinimo(Double min) {
        return (min == null) ? sinFiltro() : p -> p.getPrecioUnitario() >= min;
    }

    /** Coincide si el precio unitario es menor o igual al máximo dado. */
    public static Predicate<Producto> precioMaximo(Double max) {
        return (max == null) ? sinFiltro() : p -> p.getPrecioUnitario() <= max;
    }

    /** Coincide solo con productos activos (no inactivados/borrado lógico). */
    public static Predicate<Producto> soloActivos() {
        return Producto::isActivo;
    }

    /** Coincide solo con productos que tienen descuento vigente. */
    public static Predicate<Producto> conDescuento() {
        return p -> p.calcularDescuento() > 0;
    }

    /** Coincide solo con productos ya vencidos. */
    public static Predicate<Producto> vencidos() {
        return Producto::estaVencido;
    }

    /** Coincide solo con productos próximos a vencer (y aún no vencidos). */
    public static Predicate<Producto> proximosAVencer() {
        return p -> p.estaProximoAVencer() && !p.estaVencido();
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
    public static Predicate<Producto> combinar(Predicate<Producto>... filtros) {
        Predicate<Producto> resultado = sinFiltro();
        for (Predicate<Producto> f : filtros) {
            if (f != null) resultado = resultado.and(f);
        }
        return resultado;
    }
}
