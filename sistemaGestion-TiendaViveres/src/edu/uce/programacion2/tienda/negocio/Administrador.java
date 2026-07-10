package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Permiso;

/**
 * Administrador de la tienda. Extiende Usuario.
 * Tiene un turno propio y su permiso es siempre "ADMINISTRADOR".
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Administrador extends Usuario {

    private String turno;

    public Administrador() {
        super();
        this.turno = "MAÑANA";
    }

    public Administrador(int id, String nombre, String email,
                         String contrasena, String turno) {
        super(id, nombre, email, contrasena);
        this.turno = turno;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Métodos abstractos heredados de Usuario
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public boolean autenticar() {
        return !getEmail().isEmpty() && getContrasena().length() >= 8;
    }

    @Override
    public String getPermiso() {
        if (getRol() != null) {
            return getRol().getNombreCargo();
        }
        return "ADMINISTRADOR";
    }

    @Override
    protected boolean hasPermisoDefault(Permiso permiso) {
        // Administrador tiene TODOS los permisos por defecto
        return true;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Métodos de negocio
    // ──────────────────────────────────────────────────────────────────────

    public void gestionarReportes() {
        System.out.println("Administrador " + getNombre() + " gestionando reportes...");
    }

    public void gestionarInventario() {
        System.out.println("Administrador " + getNombre() + " gestionando inventario...");
    }

    // ──────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ──────────────────────────────────────────────────────────────────────

    public String getTurno()         { return turno; }
    public void   setTurno(String v) { this.turno = v; }

    @Override
    public String toString() {
        return "Administrador{id=" + getIdUsuario() +
                ", nombre='" + getNombre() +
                "', turno='" + turno +
                (getRol() != null ? ", rol='" + getRol().getNombreCargo() + "'" : "") +
                "}";
    }
}