package edu.uce.programacion2.tienda.interfaces;

/**
 * Interfaz que define las operaciones CRUD básicas del sistema.
 * Implementada por: Usuario, Venta.
 *
 * NOTA: la operación de borrado ya no elimina físicamente el registro.
 * "inactivar()" debe marcar el objeto como inactivo (o su equivalente de
 * estado, ej. ANULADA), preservando el registro para consultas/reportes.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public interface IGestionable {
    void   crear();
    Object consultar();
    void   actualizar();
    void   inactivar();   // antes: eliminar()
}