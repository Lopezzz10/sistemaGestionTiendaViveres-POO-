package edu.uce.programacion2.tienda.interfaces;

/**
 * Interfaz que define las operaciones de reporte del sistema.
 * Implementada por: Venta, Factura.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public interface IReportable {
    void generar();
    void mostrar();
}
