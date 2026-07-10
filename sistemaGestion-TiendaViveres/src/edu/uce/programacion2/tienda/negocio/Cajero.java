package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Permiso;

/**
 * Cajero de la tienda. Extiende Usuario.
 * Tiene la caja asignada como atributo propio y su permiso es siempre "CAJERO".
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Cajero extends Usuario {

    private int cajaAsignada;

    public Cajero() {
        super();
        this.cajaAsignada = 1;
    }

    public Cajero(int id, String nombre, String email,
                  String contrasena, int cajaAsignada) {
        super(id, nombre, email, contrasena);
        this.cajaAsignada = cajaAsignada;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Métodos abstractos heredados de Usuario
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public boolean autenticar() {
        return !getEmail().isEmpty() && getContrasena().length() >= 6 && cajaAsignada > 0;
    }

    @Override
    public String getPermiso() {
        if (getRol() != null) {
            return getRol().getNombreCargo();
        }
        return "CAJERO";
    }

    @Override
    protected boolean hasPermisoDefault(Permiso permiso) {
        // Cajero tiene permisos limitados por defecto
        switch (permiso) {
            case GESTIONAR_VENTAS:
            case GESTIONAR_FACTURAS:
            case VER_REPORTES:
                return true;
            default:
                return false;
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Métodos de negocio
    // ──────────────────────────────────────────────────────────────────────

    public void procesarVenta() {
        System.out.println("Cajero " + getNombre() + " procesando venta.");
    }

    public void abrirCaja() {
        System.out.println("Caja " + cajaAsignada + " abierta por " + getNombre());
    }

    public void cerrarCaja() {
        System.out.println("Caja " + cajaAsignada + " cerrada por " + getNombre());
    }

    // ──────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ──────────────────────────────────────────────────────────────────────

    public int  getCajaAsignada()      { return cajaAsignada; }
    public void setCajaAsignada(int v) { this.cajaAsignada = v; }

    @Override
    public String toString() {
        return "Cajero{id=" + getIdUsuario() +
                ", nombre='" + getNombre() +
                "', cajaAsignada=" + cajaAsignada +
                (getRol() != null ? ", rol='" + getRol().getNombreCargo() + "'" : "") +
                "}";
    }
}