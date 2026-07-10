package edu.uce.programacion2.tienda.excepciones;

/**
 * Se lanza cuando no hay suficiente stock de un producto.
 * Disparada realmente desde Inventario.retirar().
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class StockInsuficienteException extends Exception {

    public StockInsuficienteException() { super(); }
    public StockInsuficienteException(String msj) { super(msj); }
    public StockInsuficienteException(String msj, Throwable causa) { super(msj, causa); }
}
