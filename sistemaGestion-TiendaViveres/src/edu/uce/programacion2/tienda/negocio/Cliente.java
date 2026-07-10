package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.objetosServicio.Permiso;

/**
 * Cliente de la tienda. Tercera subclase de Usuario.
 * Puede realizar compras y acumular puntos de fidelidad.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Cliente extends Usuario {

    private String direccion;
    private String telefono;
    private String cedula;      // ← NUEVO
    private int    puntosFidelidad;

    public Cliente() {
        // No es necesario llamar a super() explícitamente
        // El compilador lo hace automáticamente
        this.direccion       = "";
        this.telefono        = "";
        this.cedula          = "";
        this.puntosFidelidad = 0;
    }

    public Cliente(int id, String nombre, String email,
                   String contrasena, String direccion, String telefono) {
        super(id, nombre, email, contrasena);  // ← Llama al constructor de Usuario
        this.direccion       = direccion;
        this.telefono        = telefono;
        this.cedula          = "";
        this.puntosFidelidad = 0;
    }

    @Override
    public boolean autenticar() {
        return !getEmail().isEmpty() && !getContrasena().isEmpty();
    }

    @Override
    public String getPermiso() {
        if (getRol() != null) {
            return getRol().getNombreCargo();
        }
        return "CLIENTE";
    }

    @Override
    protected boolean hasPermisoDefault(Permiso permiso) {
        // Cliente tiene permisos muy limitados por defecto
        switch (permiso) {
            case VER_REPORTES:
                return true;
            default:
                return false;
        }
    }

    /**
     * Acumula 1 punto por cada dólar entero gastado en una compra.
     */
    public void acumularPuntos(double totalCompra) {
        int nuevos = (int) totalCompra;
        this.puntosFidelidad += nuevos;
        System.out.println("Cliente " + getNombre() + " acumuló " + nuevos
                + " puntos. Total: " + puntosFidelidad);
    }

    public void consultarPuntos() {
        System.out.println("Cliente " + getNombre()
                + " tiene " + puntosFidelidad + " puntos de fidelidad.");
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public String getDireccion()          { return direccion; }
    public void   setDireccion(String v)   { this.direccion = v; }
    public String getTelefono()           { return telefono; }
    public void   setTelefono(String v)    { this.telefono = v; }
    public String getCedula()             { return cedula; }
    public void   setCedula(String v)      { this.cedula = v; }
    public int    getPuntosFidelidad()    { return puntosFidelidad; }
    public void   setPuntosFidelidad(int v){ this.puntosFidelidad = v; }

    @Override
    public String toString() {
        return "Cliente{id=" + getIdUsuario() + ", nombre='" + getNombre()
                + "', email='" + getEmail()
                + "', cedula='" + cedula
                + "', telefono='" + telefono
                + "', puntos=" + puntosFidelidad + "}";
    }
}