package edu.uce.programacion2.tienda.excepciones;

/**
 * Excepción lanzada por la fachada.
 * Envuelve a PersistenciaException conservando la causa.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class FachadaException extends Exception {

    public FachadaException() { super(); }
    public FachadaException(String msj) { super(msj); }
    public FachadaException(String msj, Throwable causa) { super(msj, causa); }
    public FachadaException(Throwable causa) { super(causa); }
}
