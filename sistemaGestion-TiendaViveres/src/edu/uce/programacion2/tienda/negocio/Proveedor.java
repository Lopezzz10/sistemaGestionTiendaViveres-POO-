package edu.uce.programacion2.tienda.negocio;

import edu.uce.programacion2.tienda.interfaces.IGestionable;

/**
 * Proveedor de productos para la tienda.
 * Implementa IGestionable para las operaciones CRUD.
 * Mantiene un contador estático contadorProveedores.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Proveedor implements IGestionable {

    private static int contadorProveedores = 0;

    private int    idProveedor;
    private String nombre;
    private String ruc;
    private String telefono;
    private String email;
    private String direccion;

    /** Indica si el proveedor está activo (borrado lógico: false = inactivo). */
    private boolean activo;

    public Proveedor() {
        this.idProveedor = 0;
        this.nombre      = "";
        this.ruc         = "";
        this.telefono    = "";
        this.email       = "";
        this.direccion   = "";
        this.activo      = true;
        contadorProveedores++;
    }

    public Proveedor(int idProveedor, String nombre, String ruc,
                     String telefono, String email, String direccion) {
        this.idProveedor = idProveedor;
        this.nombre      = nombre;
        this.ruc         = ruc;
        this.telefono    = telefono;
        this.email       = email;
        this.direccion   = direccion;
        this.activo      = true;
        contadorProveedores++;
    }

    public static int  getTotalProveedores() { return contadorProveedores; }
    public static void resetContador()       { contadorProveedores = 0; }

    @Override public void   crear()      { System.out.println("Proveedor creado: " + nombre + " (RUC: " + ruc + ")"); }
    @Override public Object consultar()  { return this; }
    @Override public void   actualizar() { System.out.println("Proveedor actualizado: " + nombre); }
    @Override public void   inactivar()   { this.activo = false; System.out.println("Proveedor inactivado: " + nombre); }

    public int    getIdProveedor()        { return idProveedor; }
    public void   setIdProveedor(int v)    { this.idProveedor = v; }
    public String getNombre()             { return nombre; }
    public void   setNombre(String v)      { this.nombre = v; }
    public String getRuc()                { return ruc; }
    public void   setRuc(String v)         { this.ruc = v; }
    public String getTelefono()           { return telefono; }
    public void   setTelefono(String v)    { this.telefono = v; }
    public String getEmail()              { return email; }
    public void   setEmail(String v)       { this.email = v; }
    public String getDireccion()          { return direccion; }
    public void   setDireccion(String v)   { this.direccion = v; }
    public boolean isActivo()             { return activo; }
    public void   setActivo(boolean v)     { this.activo = v; }

    @Override
    public String toString() {
        return "Proveedor{id=" + idProveedor + ", nombre='" + nombre
                + "', ruc='" + ruc + "', telefono='" + telefono
                + "', email='" + email + "', activo=" + activo + "}";
    }
}