package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Usuario;

import java.util.function.Predicate;

/**
 * Agrupa todos los parámetros OPCIONALES de una búsqueda de usuarios.
 *
 * Es el objeto que recibe la consulta con "varios parámetros a la vez":
 * el llamador solo llena los campos que le interesan (con los setters
 * encadenados, estilo builder) y deja el resto sin tocar. Internamente,
 * {@link #aPredicate()} traduce esos campos a funciones de
 * {@link FiltrosUsuario} y las combina con {@link FiltrosUsuario#combinar}.
 *
 * Ejemplo de uso:
 * <pre>{@code
 * CriteriosUsuario criterios = new CriteriosUsuario()
 *         .texto("gmail.com")
 *         .rol("CAJERO")
 *         .soloActivos(true);
 *
 * ArrayList<Usuario> resultado = fachada.buscarUsuarios(criterios);
 * }</pre>
 *
 * Agregar un nuevo criterio en el futuro solo requiere: un campo aquí,
 * un setter, y un filtro en {@link FiltrosUsuario} — sin tocar el resto
 * del sistema.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CriteriosUsuario {

    private String  texto;
    private String  rol;
    private boolean soloActivos;

    /** Texto a buscar (parcial) dentro de nombre o email. */
    public CriteriosUsuario texto(String texto) {
        this.texto = texto;
        return this;
    }

    /** Rol/permiso exacto: "ADMINISTRADOR", "CAJERO" o el nombre de un cargo dinámico. */
    public CriteriosUsuario rol(String rol) {
        this.rol = rol;
        return this;
    }

    public CriteriosUsuario soloActivos(boolean soloActivos) {
        this.soloActivos = soloActivos;
        return this;
    }

    /** Retorna true si no se estableció ningún criterio (búsqueda "vacía"). */
    public boolean estaVacio() {
        return (texto == null || texto.trim().isEmpty())
                && (rol == null || rol.trim().isEmpty())
                && !soloActivos;
    }

    /**
     * Traduce estos criterios a un único {@link Predicate}&lt;{@link Usuario}&gt;,
     * combinando (con AND) solo los filtros correspondientes a los campos
     * que sí fueron establecidos.
     */
    public Predicate<Usuario> aPredicate() {
        return FiltrosUsuario.combinar(
                FiltrosUsuario.porNombreOEmail(texto),
                FiltrosUsuario.porRol(rol),
                soloActivos ? FiltrosUsuario.soloActivos() : FiltrosUsuario.sinFiltro()
        );
    }
}