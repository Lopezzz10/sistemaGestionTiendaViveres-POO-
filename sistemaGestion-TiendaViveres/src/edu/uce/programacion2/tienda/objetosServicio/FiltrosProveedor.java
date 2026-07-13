package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Proveedor;

import java.util.function.Predicate;

/**
 * Fábrica de {@link Predicate}&lt;{@link Proveedor}&gt; reutilizables.
 *
 * Mismo espíritu que {@link FiltrosProducto}, {@link FiltrosVenta},
 * {@link FiltrosCompra} y {@link FiltrosFactura}: cada método es un
 * criterio puro e independiente, "seguro" ante parámetros null/vacíos
 * (en ese caso no filtra nada), pensado para combinarse con {@link #combinar}.
 *
 * Todos los filtros son "seguros": si el parámetro recibido es null o
 * vacío, el filtro simplemente no descarta nada (equivale a no aplicar
 * ese criterio). Así el llamador puede pasar solo los parámetros que le
 * interesen y dejar el resto en null.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public final class FiltrosProveedor {

    /** Clase de utilidades: no se instancia. */
    private FiltrosProveedor() { }

    /** Predicado neutro: no filtra nada (siempre true). Útil como valor por defecto. */
    public static Predicate<Proveedor> sinFiltro() {
        return p -> true;
    }

    /** Coincide si el nombre del proveedor CONTIENE el texto dado (insensible a mayúsculas). */
    public static Predicate<Proveedor> porNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return p -> p.getNombre() != null && p.getNombre().toLowerCase().contains(buscado);
    }

    /** Coincide si el RUC del proveedor CONTIENE el texto dado (insensible a mayúsculas). */
    public static Predicate<Proveedor> porRuc(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return p -> p.getRuc() != null && p.getRuc().toLowerCase().contains(buscado);
    }

    /** Coincide si el teléfono del proveedor CONTIENE el texto dado. */
    public static Predicate<Proveedor> porTelefono(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return p -> p.getTelefono() != null && p.getTelefono().toLowerCase().contains(buscado);
    }

    /** Coincide si el email del proveedor CONTIENE el texto dado (insensible a mayúsculas). */
    public static Predicate<Proveedor> porEmail(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return p -> p.getEmail() != null && p.getEmail().toLowerCase().contains(buscado);
    }

    /** Coincide si la dirección del proveedor CONTIENE el texto dado (insensible a mayúsculas). */
    public static Predicate<Proveedor> porDireccion(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return p -> p.getDireccion() != null && p.getDireccion().toLowerCase().contains(buscado);
    }

    /** Coincide solo con proveedores activos (no inactivados/borrado lógico). */
    public static Predicate<Proveedor> soloActivos() {
        return Proveedor::isActivo;
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
    public static Predicate<Proveedor> combinar(Predicate<Proveedor>... filtros) {
        Predicate<Proveedor> resultado = sinFiltro();
        for (Predicate<Proveedor> f : filtros) {
            if (f != null) resultado = resultado.and(f);
        }
        return resultado;
    }
}