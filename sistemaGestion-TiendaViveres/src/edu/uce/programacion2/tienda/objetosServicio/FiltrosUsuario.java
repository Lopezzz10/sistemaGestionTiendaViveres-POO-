package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Usuario;

import java.util.function.Predicate;

/**
 * Fábrica de {@link Predicate}&lt;{@link Usuario}&gt; reutilizables.
 *
 * Mismo espíritu que {@link FiltrosProducto} / {@link FiltrosVenta}: cada
 * método es un criterio puro e independiente, "seguro" ante parámetros
 * null/vacíos (en ese caso no filtra nada), pensado para combinarse con
 * {@link #combinar}.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public final class FiltrosUsuario {

    /** Clase de utilidades: no se instancia. */
    private FiltrosUsuario() { }

    /** Predicado neutro: no filtra nada (siempre true). Útil como valor por defecto. */
    public static Predicate<Usuario> sinFiltro() {
        return u -> true;
    }

    /**
     * Coincide si el nombre O el email del usuario CONTIENE el texto dado
     * (insensible a mayúsculas). Búsqueda parcial, pensada para un único
     * cuadro de texto de búsqueda en la interfaz.
     */
    public static Predicate<Usuario> porNombreOEmail(String texto) {
        if (texto == null || texto.trim().isEmpty()) return sinFiltro();
        String buscado = texto.trim().toLowerCase();
        return u -> (u.getNombre() != null && u.getNombre().toLowerCase().contains(buscado))
                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(buscado));
    }

    /**
     * Coincide si el permiso/rol del usuario es el indicado.
     * Acepta tanto los roles fijos ("ADMINISTRADOR", "CAJERO") como el
     * nombre de cargo de un {@link edu.uce.programacion2.tienda.negocio.Rol}
     * dinámico, ya que {@link Usuario#getPermiso()} devuelve ambos casos
     * como texto. Comparación insensible a mayúsculas.
     */
    public static Predicate<Usuario> porRol(String rol) {
        if (rol == null || rol.trim().isEmpty()) return sinFiltro();
        String r = rol.trim();
        return u -> u.getPermiso() != null && u.getPermiso().equalsIgnoreCase(r);
    }

    /** Coincide solo con usuarios activos (no inactivados/borrado lógico). */
    public static Predicate<Usuario> soloActivos() {
        return Usuario::isActivo;
    }

    /**
     * Combina cualquier cantidad de filtros con AND lógico.
     * Un filtro null se ignora (equivale a no restringir por ese criterio).
     */
    @SafeVarargs
    public static Predicate<Usuario> combinar(Predicate<Usuario>... filtros) {
        Predicate<Usuario> resultado = sinFiltro();
        for (Predicate<Usuario> f : filtros) {
            if (f != null) resultado = resultado.and(f);
        }
        return resultado;
    }
}