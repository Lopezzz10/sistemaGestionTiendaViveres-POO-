package edu.uce.programacion2.tienda.objetosServicio;

import edu.uce.programacion2.tienda.persistencia.Iva;

/**
 * Objeto de servicio que encapsula un valor monetario, centralizando
 * el redondeo a 2 decimales y el cálculo de IVA que hoy se repite
 * manualmente en Factura y DetalleFactura.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Dinero {

    /**
     * DAO usado para persistir el IVA vigente en iva.dat, de modo que el
     * valor configurado sobreviva a un reinicio de la aplicación (antes
     * de esto, IVA era solo un valor en memoria que volvía a 15% cada
     * vez que se cerraba el programa).
     */
    private static final Iva ivaDAO = new Iva();

    /**
     * Porcentaje de IVA vigente, expresado como fracción (0.15 = 15%).
     * Por defecto se inicializa en 15%, pero puede modificarse en tiempo
     * de ejecución mediante setIva(double). El valor real y actualizado
     * se carga desde iva.dat al arrancar la fachada (ver
     * FachadaArchivos/FachadaTienda), que llama a setIva(...) con lo que
     * haya persistido en disco (o el valor por defecto, si es la primera
     * ejecución).
     */
    private static double IVA = Iva.IVA_POR_DEFECTO;

    /** Retorna el porcentaje de IVA actual (como fracción, ej. 0.15). */
    public static double getIva() {
        return IVA;
    }

    /**
     * Establece el nuevo porcentaje de IVA y lo persiste inmediatamente
     * en iva.dat, para que el cambio sobreviva a un reinicio de la
     * aplicación.
     * @param nuevoIva valor como fracción (ej. 0.12 para 12%). Debe estar
     *                  entre 0 y 1.
     */
    public static void setIva(double nuevoIva) {
        if (nuevoIva < 0 || nuevoIva > 1) {
            throw new IllegalArgumentException(
                    "El IVA debe estar entre 0 y 1 (ej. 0.15 para 15%). Valor recibido: " + nuevoIva);
        }
        IVA = nuevoIva;
        try {
            ivaDAO.guardarIva(IVA);
        } catch (java.io.IOException ioe) {
            // No queremos que un error de E/S al persistir el IVA rompa
            // la operacion en curso (ej. emitir una factura): el valor
            // ya quedo actualizado en memoria para esta sesion, solo
            // avisamos que no se pudo guardar en disco.
            System.err.println("Advertencia: no se pudo guardar el IVA en iva.dat: " + ioe.getMessage());
        }
    }

    private double valor;

    public Dinero() {
        this.valor = 0.0;
    }

    public Dinero(double valor) {
        this.valor = redondear(valor);
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public Dinero mas(Dinero otro)   { return new Dinero(this.valor + otro.valor); }
    public Dinero menos(Dinero otro) { return new Dinero(this.valor - otro.valor); }

    public Dinero porPorcentaje(double porcentaje) {
        return new Dinero(this.valor * porcentaje);
    }

    /** Calcula el monto de IVA correspondiente a este valor (como base imponible),
     *  usando la tasa general vigente. */
    public Dinero calcularIva() {
        return porPorcentaje(IVA);
    }

    /**
     * Calcula el monto de IVA usando una tasa específica (fracción, ej. 0.0
     * para productos exentos, 0.15 para tasa general). Útil cuando el
     * producto pertenece a una categoría con tarifa fija predeterminada,
     * en vez de la tasa general vigente.
     */
    public Dinero calcularIva(double tasa) {
        return porPorcentaje(tasa);
    }

    /** Retorna un nuevo Dinero con el IVA ya incluido (base + IVA), usando la tasa general vigente. */
    public Dinero conIvaIncluido() {
        return this.mas(this.calcularIva());
    }

    /** Retorna un nuevo Dinero con el IVA ya incluido (base + IVA), usando una tasa específica. */
    public Dinero conIvaIncluido(double tasa) {
        return this.mas(this.calcularIva(tasa));
    }

    /** Dado un valor que YA incluye IVA, retorna la base imponible (sin IVA),
     *  usando la tasa general vigente. */
    public Dinero descomponerSinIva() {
        return new Dinero(this.valor / (1 + IVA));
    }

    /** Dado un valor que YA incluye IVA a una tasa específica, retorna la base imponible (sin IVA). */
    public Dinero descomponerSinIva(double tasa) {
        return new Dinero(this.valor / (1 + tasa));
    }

    public double getValor()         { return valor; }
    public void   setValor(double v) { this.valor = redondear(v); }

    @Override
    public String toString() {
        return String.format("$ %.2f", valor);
    }
}