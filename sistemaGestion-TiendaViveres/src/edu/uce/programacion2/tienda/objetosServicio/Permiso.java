package edu.uce.programacion2.tienda.objetosServicio;

/**
 * Catálogo de permisos disponibles en el sistema. Cada {@linkRol} (cargo)
 * creado por el Administrador tiene asociado un subconjunto de estos
 * permisos, seleccionado mediante checkboxes en {@code DlgRol}.
 *
 * Agregar un nuevo permiso al sistema es tan simple como añadir una
 * constante mas aqui; automaticamente aparecera como checkbox disponible
 * en el dialogo de gestion de roles.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public enum Permiso {

    GESTIONAR_PRODUCTOS   ("Gestionar Productos"),
    GESTIONAR_CATEGORIAS  ("Gestionar Categorias"),
    GESTIONAR_INVENTARIO  ("Gestionar Inventario"),
    GESTIONAR_PROVEEDORES ("Gestionar Proveedores"),
    GESTIONAR_COMPRAS     ("Gestionar Compras"),
    GESTIONAR_VENTAS      ("Gestionar Ventas"),
    GESTIONAR_FACTURAS    ("Gestionar Facturas"),
    GESTIONAR_USUARIOS    ("Gestionar Usuarios"),
    GESTIONAR_ROLES       ("Gestionar Roles"),
    VER_REPORTES          ("Ver Reportes y Consultas"),
    CONFIGURAR_SISTEMA    ("Configurar Sistema (IVA, etc.)");

    private final String descripcion;

    Permiso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
