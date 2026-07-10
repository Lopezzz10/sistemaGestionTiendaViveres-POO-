package edu.uce.programacion2.tienda.excepciones;

/**
 * Se lanza cuando no se encuentra una categoría en el sistema.
 * Mantiene consistencia con ProductoNoEncontradaException.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CategoriaNoEncontradaException extends Exception {

    public CategoriaNoEncontradaException() { super(); }
    public CategoriaNoEncontradaException(String msj) { super(msj); }
    public CategoriaNoEncontradaException(String msj, Throwable causa) { super(msj, causa); }
}
