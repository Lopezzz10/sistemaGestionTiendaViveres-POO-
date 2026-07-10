package edu.uce.programacion2.tienda.negocio;

import java.util.function.Predicate;

/**
 * Agrupa todos los parámetros OPCIONALES de una búsqueda de productos.
 *
 * Es el objeto que recibe la consulta con "varios parámetros a la vez":
 * el llamador solo llena los campos que le interesan (con los setters
 * encadenados, estilo builder) y deja el resto sin tocar. Internamente,
 * {@link #aPredicate()} traduce esos campos a funciones de
 * {@link FiltrosProducto} y las combina con {@link FiltrosProducto#combinar}.
 *
 * Ejemplo de uso:
 * <pre>{@code
 * CriteriosProducto criterios = new CriteriosProducto()
 *         .categoria("LAC")
 *         .precioMin(1.0)
 *         .precioMax(5.0)
 *         .soloActivos(true);
 *
 * ArrayList<Producto> resultado = fachada.buscarProductos(criterios);
 * }</pre>
 *
 * Agregar un nuevo criterio en el futuro (por ejemplo "por proveedor")
 * solo requiere: un campo aquí, un setter, y un filtro en
 * {@link FiltrosProducto} — sin tocar el resto del sistema.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CriteriosProducto {

    private String  nombre;
    private String  cveCategoria;
    private String  tipo;
    private String  marca;
    private Double  precioMin;
    private Double  precioMax;
    private boolean soloActivos;
    private boolean soloConDescuento;

    public CriteriosProducto nombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public CriteriosProducto categoria(String cveCategoria) {
        this.cveCategoria = cveCategoria;
        return this;
    }

    public CriteriosProducto tipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public CriteriosProducto marca(String marca) {
        this.marca = marca;
        return this;
    }

    public CriteriosProducto precioMin(Double precioMin) {
        this.precioMin = precioMin;
        return this;
    }

    public CriteriosProducto precioMax(Double precioMax) {
        this.precioMax = precioMax;
        return this;
    }

    public CriteriosProducto soloActivos(boolean soloActivos) {
        this.soloActivos = soloActivos;
        return this;
    }

    public CriteriosProducto soloConDescuento(boolean soloConDescuento) {
        this.soloConDescuento = soloConDescuento;
        return this;
    }

    /** Retorna true si no se estableció ningún criterio (búsqueda "vacía"). */
    public boolean estaVacio() {
        return (nombre == null || nombre.trim().isEmpty())
                && (cveCategoria == null || cveCategoria.trim().isEmpty())
                && (tipo == null || tipo.trim().isEmpty())
                && (marca == null || marca.trim().isEmpty())
                && precioMin == null
                && precioMax == null
                && !soloActivos
                && !soloConDescuento;
    }

    /**
     * Traduce estos criterios a un único {@link Predicate}&lt;{@link Producto}&gt;,
     * combinando (con AND) solo los filtros correspondientes a los campos
     * que sí fueron establecidos.
     */
    public Predicate<Producto> aPredicate() {
        return FiltrosProducto.combinar(
                FiltrosProducto.porNombre(nombre),
                FiltrosProducto.porCategoria(cveCategoria),
                FiltrosProducto.porTipo(tipo),
                FiltrosProducto.porMarca(marca),
                FiltrosProducto.precioMinimo(precioMin),
                FiltrosProducto.precioMaximo(precioMax),
                soloActivos ? FiltrosProducto.soloActivos() : FiltrosProducto.sinFiltro(),
                soloConDescuento ? FiltrosProducto.conDescuento() : FiltrosProducto.sinFiltro()
        );
    }
}
