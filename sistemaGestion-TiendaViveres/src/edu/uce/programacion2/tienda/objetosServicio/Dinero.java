package edu.uce.programacion2.tienda.objetosServicio;

/**
 * Objeto de servicio que encapsula un valor monetario, centralizando
 * el redondeo a 2 decimales y el cálculo de IVA que hoy se repite
 * manualmente en Factura y DetalleFactura.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Dinero {

    /**
     * Porcentaje de IVA vigente, expresado como fracción (0.15 = 15%).
     * Por defecto se inicializa en 15%, pero puede modificarse en tiempo
     * de ejecución mediante setIva(double).
     */
    private static double IVA = 0.15;

    /** Retorna el porcentaje de IVA actual (como fracción, ej. 0.15). */
    public static double getIva() {
        return IVA;
    }

    /**
     * Establece el nuevo porcentaje de IVA.
     * @param nuevoIva valor como fracción (ej. 0.12 para 12%). Debe estar
     *                  entre 0 y 1.
     */
    public static void setIva(double nuevoIva) {
        if (nuevoIva < 0 || nuevoIva > 1) {
            throw new IllegalArgumentException(
                    "El IVA debe estar entre 0 y 1 (ej. 0.15 para 15%). Valor recibido: " + nuevoIva);
        }
        IVA = nuevoIva;
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