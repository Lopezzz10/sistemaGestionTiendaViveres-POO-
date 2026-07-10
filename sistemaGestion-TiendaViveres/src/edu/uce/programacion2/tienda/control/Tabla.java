package edu.uce.programacion2.tienda.control;

/**
 * Representa los datos de una tabla de consulta: un título descriptivo,
 * los nombres de las columnas y la matriz de filas a mostrar.
 *
 * @author Ana
 */
public class Tabla {

    private String     titulo;
    private String[]   columnas;
    private Object[][] filas;

    public Tabla() {
    }

    public Tabla(String titulo, String[] columnas, Object[][] filas) {
        this.titulo   = titulo;
        this.columnas = columnas;
        this.filas    = filas;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String[] getColumnas() {
        return columnas;
    }

    public void setColumnas(String[] columnas) {
        this.columnas = columnas;
    }

    public Object[][] getFilas() {
        return filas;
    }

    public void setFilas(Object[][] filas) {
        this.filas = filas;
    }
}
