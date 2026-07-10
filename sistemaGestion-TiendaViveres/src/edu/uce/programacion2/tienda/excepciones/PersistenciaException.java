package edu.uce.programacion2.tienda.excepciones;

/**
 * Excepción lanzada por las clases de persistencia.
 * Soporta encadenamiento para conservar la causa original.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class PersistenciaException extends Exception {

    public PersistenciaException() { super(); }
    public PersistenciaException(String msj) { super(msj); }
    public PersistenciaException(String msj, Throwable causa) { super(msj, causa); }
    public PersistenciaException(Throwable causa) { super(causa); }
}
