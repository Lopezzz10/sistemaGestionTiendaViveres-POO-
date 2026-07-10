package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.interfaces.IGestionable;
import edu.uce.programacion2.tienda.objetosServicio.Permiso;

/**
 * Clase abstracta base para todos los usuarios del sistema.
 * Implementa IGestionable para las operaciones CRUD.
 * Mantiene un contador estático contadorUsuarios (atributo estático).
 * Declara autenticar() y getPermiso() como abstractos, que cada
 * subclase implementa de forma distinta (polimorfismo).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public abstract class Usuario implements IGestionable {

    private static int contadorUsuarios = 0;

    private int    idUsuario;
    private String nombre;
    private String email;
    private String contrasena;

    /** Indica si el usuario está activo (borrado lógico: false = inactivo). */
    private boolean activo;

    /** Rol asignado al usuario (puede ser null si no tiene rol dinámico). */
    private Rol rol;

    public Usuario() {
        this.idUsuario  = 0;
        this.nombre     = "";
        this.email      = "";
        this.contrasena = "";
        this.activo     = true;
        this.rol        = null;
        contadorUsuarios++;
    }

    public Usuario(int idUsuario, String nombre, String email, String contrasena) {
        this.idUsuario  = idUsuario;
        this.nombre     = nombre;
        this.email      = email;
        this.contrasena = contrasena;
        this.activo     = true;
        this.rol        = null;
        contadorUsuarios++;
    }

    public static int  getTotalUsuarios() { return contadorUsuarios; }
    public static void resetContador()    { contadorUsuarios = 0; }

    /** Verifica si las credenciales son válidas. Cada subclase define su lógica. */
    public abstract boolean autenticar();

    /** Retorna el nivel de permiso: "ADMIN" o "CAJERO" o el nombre del rol. */
    public abstract String getPermiso();

    /**
     * Verifica si el usuario tiene un permiso específico.
     * Si tiene rol dinámico, consulta los permisos del rol.
     * Si no tiene rol, usa el método abstracto hasPermisoDefault() de la subclase.
     */
    public boolean tienePermiso(Permiso permiso) {
        if (rol != null) {
            return rol.tienePermiso(permiso);
        }
        return hasPermisoDefault(permiso);
    }

    /**
     * Método que cada subclase debe implementar para definir sus permisos
     * cuando NO tiene un rol dinámico asignado.
     */
    protected abstract boolean hasPermisoDefault(Permiso permiso);

    @Override public void   crear()      { System.out.println("Usuario creado: " + nombre); }
    @Override public Object consultar()  { return this; }
    @Override public void   actualizar() { System.out.println("Usuario actualizado: " + nombre); }
    @Override public void   inactivar()  { this.activo = false; System.out.println("Usuario inactivado: " + nombre); }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int    getIdUsuario()          { return idUsuario; }
    public void   setIdUsuario(int v)     { this.idUsuario = v; }
    public String getNombre()             { return nombre; }
    public void   setNombre(String v)     { this.nombre = v; }
    public String getEmail()              { return email; }
    public void   setEmail(String v)      { this.email = v; }
    public String getContrasena()         { return contrasena; }
    public void   setContrasena(String v) { this.contrasena = v; }
    public boolean isActivo()             { return activo; }
    public void   setActivo(boolean v)    { this.activo = v; }

    public Rol getRol()                   { return rol; }
    public void setRol(Rol rol)           { this.rol = rol; }

    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario + ", nombre='" + nombre +
                "', email='" + email + "', permiso='" + getPermiso() +
                "', activo=" + activo +
                (rol != null ? ", rol='" + rol.getNombreCargo() + "'" : "") +
                "}";
    }
}