package edu.uce.programacion2.tienda.objetosServicio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Objeto de servicio que representa una fecha simple (día, mes, año).
 * Permite trabajar con fechas en la capa de negocio sin depender
 * directamente de java.util.Date, y ofrece conversión hacia/desde Date
 * para mantener compatibilidad con el resto del sistema
 * (Compra, Venta, Factura, ProductoPerecible, Inventario, etc.).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Fecha {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private int dia;
    private int mes;
    private int anio;

    /** Constructor vacío: toma la fecha actual del sistema. */
    public Fecha() {
        Fecha hoy = Fecha.desdeDate(new Date());
        this.dia  = hoy.dia;
        this.mes  = hoy.mes;
        this.anio = hoy.anio;
    }

    public Fecha(int dia, int mes, int anio) {
        if (!esValida(dia, mes, anio)) {
            throw new IllegalArgumentException("Fecha inválida: " + dia + "/" + mes + "/" + anio);
        }
        this.dia  = dia;
        this.mes  = mes;
        this.anio = anio;
    }

    // ── Conversión con java.util.Date ───────────────────────────────────

    /** Crea una Fecha a partir de un java.util.Date existente. */
    public static Fecha desdeDate(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        return new Fecha(cal.get(Calendar.DAY_OF_MONTH),
                          cal.get(Calendar.MONTH) + 1,
                          cal.get(Calendar.YEAR));
    }

    /** Convierte esta Fecha a un java.util.Date (hora 00:00:00). */
    public Date aDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(anio, mes - 1, dia, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /** Crea una Fecha a partir de un texto en formato dd/MM/aaaa. */
    public static Fecha desdeTexto(String texto) throws ParseException {
        return desdeDate(SDF.parse(texto));
    }

    // ── Validación ───────────────────────────────────────────────────────

    public static boolean esValida(int dia, int mes, int anio) {
        if (mes < 1 || mes > 12 || dia < 1) return false;
        int[] diasPorMes = {31, esBisiesto(anio) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        return dia <= diasPorMes[mes - 1];
    }

    public static boolean esBisiesto(int anio) {
        return (anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0);
    }

    // ── Comparación ──────────────────────────────────────────────────────

    public boolean esMayorQue(Fecha otra) { return this.aDate().after(otra.aDate()); }
    public boolean esMenorQue(Fecha otra) { return this.aDate().before(otra.aDate()); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Fecha)) return false;
        Fecha f = (Fecha) obj;
        return dia == f.dia && mes == f.mes && anio == f.anio;
    }

    @Override
    public int hashCode() { return dia * 10000 + mes * 100 + anio; }

    // ── Getters / Setters ───────────────────────────────────────────────

    public int  getDia()        { return dia; }
    public void setDia(int v)   { this.dia = v; }
    public int  getMes()        { return mes; }
    public void setMes(int v)   { this.mes = v; }
    public int  getAnio()       { return anio; }
    public void setAnio(int v)  { this.anio = v; }

    @Override
    public String toString() {
        return String.format("%02d/%02d/%04d", dia, mes, anio);
    }
}
