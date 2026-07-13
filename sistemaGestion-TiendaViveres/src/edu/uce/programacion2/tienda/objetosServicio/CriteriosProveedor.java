package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.negocio.Proveedor;

import java.util.function.Predicate;

/**
 * Agrupa todos los parámetros OPCIONALES de una búsqueda de proveedores.
 *
 * Análogo a {@link CriteriosProducto}, {@link CriteriosCompra}, {@link CriteriosVenta}
 * y {@link CriteriosFactura}: el llamador solo llena los campos que le
 * interesan (setters encadenados, estilo builder) y deja el resto sin
 * tocar. {@link #aPredicate()} traduce esos campos a funciones de
 * {@link FiltrosProveedor} y las combina con {@link FiltrosProveedor#combinar}.
 *
 * Ejemplo de uso:
 * <pre>{@code
 * CriteriosProveedor criterios = new CriteriosProveedor()
 *         .nombre("Distribuidora")
 *         .ruc("1790")
 *         .soloActivos(true);
 *
 * ArrayList<Proveedor> resultado = fachada.buscarProveedores(criterios);
 * }</pre>
 *
 * Agregar un nuevo criterio en el futuro solo requiere: un campo aquí,
 * un setter, y un filtro en {@link FiltrosProveedor} — sin tocar el resto
 * del sistema.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CriteriosProveedor {

    private String  nombre;
    private String  ruc;
    private String  telefono;
    private String  email;
    private String  direccion;
    private boolean soloActivos;

    /** Filtra por nombre del proveedor (coincidencia parcial). */
    public CriteriosProveedor nombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    /** Filtra por RUC del proveedor (coincidencia parcial). */
    public CriteriosProveedor ruc(String ruc) {
        this.ruc = ruc;
        return this;
    }

    /** Filtra por teléfono del proveedor (coincidencia parcial). */
    public CriteriosProveedor telefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    /** Filtra por email del proveedor (coincidencia parcial). */
    public CriteriosProveedor email(String email) {
        this.email = email;
        return this;
    }

    /** Filtra por dirección del proveedor (coincidencia parcial). */
    public CriteriosProveedor direccion(String direccion) {
        this.direccion = direccion;
        return this;
    }

    /** Filtra solo proveedores activos (no inactivados/borrado lógico). */
    public CriteriosProveedor soloActivos(boolean soloActivos) {
        this.soloActivos = soloActivos;
        return this;
    }

    /** Retorna true si no se estableció ningún criterio (búsqueda "vacía"). */
    public boolean estaVacio() {
        return (nombre == null || nombre.trim().isEmpty())
                && (ruc == null || ruc.trim().isEmpty())
                && (telefono == null || telefono.trim().isEmpty())
                && (email == null || email.trim().isEmpty())
                && (direccion == null || direccion.trim().isEmpty())
                && !soloActivos;
    }

    /**
     * Traduce estos criterios a un único {@link Predicate}&lt;{@link Proveedor}&gt;,
     * combinando (con AND) solo los filtros correspondientes a los campos
     * que sí fueron establecidos.
     */
    public Predicate<Proveedor> aPredicate() {
        return FiltrosProveedor.combinar(
                FiltrosProveedor.porNombre(nombre),
                FiltrosProveedor.porRuc(ruc),
                FiltrosProveedor.porTelefono(telefono),
                FiltrosProveedor.porEmail(email),
                FiltrosProveedor.porDireccion(direccion),
                soloActivos ? FiltrosProveedor.soloActivos() : FiltrosProveedor.sinFiltro()
        );
    }

    // ── Getters (opcionales, para depuración) ──────────────────────────────

    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getDireccion() { return direccion; }
    public boolean isSoloActivos() { return soloActivos; }

    @Override
    public String toString() {
        return "CriteriosProveedor{" +
                "nombre='" + nombre + '\'' +
                ", ruc='" + ruc + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", direccion='" + direccion + '\'' +
                ", soloActivos=" + soloActivos +
                '}';
    }
}