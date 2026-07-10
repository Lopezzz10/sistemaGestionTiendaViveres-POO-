package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Permiso;
import java.util.EnumSet;
import java.util.Set;

/**
 * Representa un rol (cargo) definido dinámicamente por el Administrador,
 * con un nombre libre y un conjunto de {@link Permiso} elegidos mediante
 * checkboxes en la interfaz.
 *
 * A diferencia de {@link Administrador}/{@link Cajero} (que son roles fijos
 * y "hardcodeados" en el sistema), {@code Rol} permite al Administrador
 * ampliar el catalogo de cargos sin necesidad de tocar el codigo fuente,
 * por ejemplo: "Supervisor de Bodega", "Contador", "Auxiliar de Ventas", etc.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Rol {

    private int           idRol;
    private String        nombreCargo;
    private Set<Permiso>  permisos;

    /** Indica si el rol está activo (borrado lógico: false = inactivo). */
    private boolean activo;

    public Rol() {
        this.idRol       = 0;
        this.nombreCargo = "";
        this.permisos    = EnumSet.noneOf(Permiso.class);
        this.activo      = true;
    }

    public Rol(int idRol, String nombreCargo, Set<Permiso> permisos) {
        this.idRol       = idRol;
        this.nombreCargo = nombreCargo;
        this.permisos    = (permisos == null || permisos.isEmpty())
                ? EnumSet.noneOf(Permiso.class)
                : EnumSet.copyOf(permisos);
        this.activo      = true;
    }

    /** Indica si el rol tiene concedido un permiso específico. */
    public boolean tienePermiso(Permiso p) {
        return permisos.contains(p);
    }

    /** Texto legible con la lista de permisos, para mostrar en tablas. */
    public String getPermisosTexto() {
        if (permisos.isEmpty()) return "(ninguno)";
        StringBuilder sb = new StringBuilder();
        for (Permiso p : permisos) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.getDescripcion());
        }
        return sb.toString();
    }

    public int    getIdRol()               { return idRol; }
    public void   setIdRol(int v)          { this.idRol = v; }
    public String getNombreCargo()         { return nombreCargo; }
    public void   setNombreCargo(String v) { this.nombreCargo = v; }

    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> v) {
        this.permisos = (v == null || v.isEmpty())
                ? EnumSet.noneOf(Permiso.class)
                : EnumSet.copyOf(v);
    }

    public boolean isActivo()          { return activo; }
    public void    setActivo(boolean v) { this.activo = v; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Rol)) return false;
        return this.nombreCargo.equalsIgnoreCase(((Rol) obj).nombreCargo);
    }

    @Override
    public int hashCode() { return nombreCargo.toLowerCase().hashCode(); }

    @Override
    public String toString() {
        return "Rol{id=" + idRol + ", cargo='" + nombreCargo +
                "', permisos=[" + getPermisosTexto() +
                "], activo=" + activo + "}";
    }
}
