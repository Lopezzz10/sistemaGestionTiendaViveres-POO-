package edu.uce.programacion2.tienda.excepciones;

/**
 * Se lanza cuando no se encuentra un producto en el catálogo.
 * Disparada realmente desde FachadaTienda.buscarPerecible/buscarNoPerecible().
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class ProductoNoEncontradoException extends Exception {

    public ProductoNoEncontradoException() { super(); }
    public ProductoNoEncontradoException(String msj) { super(msj); }
    public ProductoNoEncontradoException(String msj, Throwable causa) { super(msj, causa); }
}
