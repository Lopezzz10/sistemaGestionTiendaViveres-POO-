package edu.uce.programacion2.tienda.objetosServicio;

/**
 * Objeto de servicio que representa un rango de fechas
 * [fechaInicio, fechaFin], usado en consultas como productos
 * próximos a vencer, o compras/ventas realizadas dentro de un
 * periodo determinado.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Periodo {

    private Fecha fechaInicio;
    private Fecha fechaFin;

    /** Constructor vacío: ambos extremos quedan en la fecha actual. */
    public Periodo() {
        this.fechaInicio = new Fecha();
        this.fechaFin    = new Fecha();
    }

    public Periodo(Fecha fechaInicio, Fecha fechaFin) {
        if (fechaInicio.esMayorQue(fechaFin)) {
            throw new IllegalArgumentException(
                "La fecha de inicio (" + fechaInicio +
                ") no puede ser posterior a la fecha de fin (" + fechaFin + ")");
        }
        this.fechaInicio = fechaInicio;
        this.fechaFin    = fechaFin;
    }

    /** Indica si la fecha dada cae dentro de este periodo (inclusive). */
    public boolean contiene(Fecha fecha) {
        return !fecha.esMenorQue(fechaInicio) && !fecha.esMayorQue(fechaFin);
    }

    public Fecha getFechaInicio()        { return fechaInicio; }
    public void  setFechaInicio(Fecha v) { this.fechaInicio = v; }
    public Fecha getFechaFin()           { return fechaFin; }
    public void  setFechaFin(Fecha v)    { this.fechaFin = v; }

    @Override
    public String toString() {
        return "Periodo[" + fechaInicio + " - " + fechaFin + "]";
    }
}
