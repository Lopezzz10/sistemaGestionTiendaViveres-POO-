package edu.uce.programacion2.tienda.negocio;

/**
 * Representa una categoría de productos de la tienda.
 * Clase independiente (ya NO extiende Producto — esa herencia era un
 * resto del diseño viejo para "reusar" infraestructura de persistencia
 * y dejó de tener sentido al unificar Producto).
 * El campo tipoProducto indica 'P' (Perecible) o 'N' (No Perecible).
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Categoria {

    private String cveCategoria;
    private String nombre;
    private char   tipoProducto;

    /**
     * Indica si esta categoría tiene una tarifa de IVA fija y predeterminada
     * (ej. 0% para canasta básica: lácteos naturales, carnes frescas, granos,
     * enlatados nacionales de pescado, etc., según el art. 55 de la LRTI).
     * Si es {@code false}, los productos de esta categoría usan la tasa
     * general vigente (la que el administrador cambia manualmente, ej. por
     * feriados con tarifa reducida).
     */
    private boolean ivaEspecial;

    /**
     * Tarifa fija aplicable cuando {@link #ivaEspecial} es {@code true},
     * expresada como fracción (0.0 = 0%, 0.15 = 15%, etc.). Se ignora si
     * ivaEspecial es false.
     */
    private double tarifaIvaEspecial;

    /**
     * Indica si la categoría está activa. Al "inactivar" una categoría
     * (borrado lógico) este campo pasa a {@code false}; el registro se
     * conserva para no romper productos ya asociados a ella.
     */
    private boolean activo;

    public Categoria() {
        this.cveCategoria     = "";
        this.nombre            = "";
        this.tipoProducto      = ' ';
        this.ivaEspecial       = false;
        this.tarifaIvaEspecial = 0.0;
        this.activo            = true;
    }

    /** Constructor de búsqueda (solo clave). */
    public Categoria(String cveCategoria) {
        this.cveCategoria     = cveCategoria;
        this.nombre            = "";
        this.tipoProducto      = ' ';
        this.ivaEspecial       = false;
        this.tarifaIvaEspecial = 0.0;
        this.activo            = true;
    }

    /** Constructor clásico (sin tarifa especial de IVA: usa la tasa general vigente). */
    public Categoria(String cveCategoria, String nombre, char tipoProducto) {
        this(cveCategoria, nombre, tipoProducto, false, 0.0);
    }

    /**
     * Constructor completo, incluyendo la tarifa de IVA predeterminada de la categoría.
     *
     * @param ivaEspecial       true si la categoría tiene tarifa fija (ej. canasta básica).
     * @param tarifaIvaEspecial tarifa fija como fracción (0.0 = 0%). Solo aplica si ivaEspecial=true.
     */
    public Categoria(String cveCategoria, String nombre, char tipoProducto,
                     boolean ivaEspecial, double tarifaIvaEspecial) {
        this.cveCategoria      = cveCategoria;
        this.nombre             = nombre;
        this.tipoProducto       = tipoProducto;
        this.ivaEspecial        = ivaEspecial;
        this.tarifaIvaEspecial  = tarifaIvaEspecial;
        this.activo             = true;
    }

    public String  getCveCategoria()               { return cveCategoria; }
    public void    setCveCategoria(String v)       { this.cveCategoria = v; }
    public String  getNombre()                     { return nombre; }
    public void    setNombre(String v)             { this.nombre = v; }
    public char    getTipoProducto()               { return tipoProducto; }
    public void    setTipoProducto(char v)         { this.tipoProducto = v; }
    public boolean isIvaEspecial()                 { return ivaEspecial; }
    public void    setIvaEspecial(boolean v)       { this.ivaEspecial = v; }
    public double  getTarifaIvaEspecial()          { return tarifaIvaEspecial; }
    public void    setTarifaIvaEspecial(double v)  { this.tarifaIvaEspecial = v; }
    public boolean isActivo()                      { return activo; }
    public void    setActivo(boolean v)            { this.activo = v; }

    public String getTipo() {
        return tipoProducto == 'P' ? "Perecible" : "No Perecible";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Categoria)) return false;
        return this.cveCategoria.equalsIgnoreCase(((Categoria) obj).cveCategoria);
    }

    @Override
    public int hashCode() { return cveCategoria.toLowerCase().hashCode(); }

    @Override
    public String toString() {
        String iva = ivaEspecial
                ? (int) Math.round(tarifaIvaEspecial * 100) + "% (fija)"
                : "tasa general";
        return cveCategoria + ", " + nombre + ", " + tipoProducto + ", IVA=" + iva
                + ", activo=" + activo;
    }
}