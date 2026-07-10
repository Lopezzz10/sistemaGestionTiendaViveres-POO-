package edu.uce.programacion2.tienda.interfaces;

import edu.uce.programacion2.tienda.negocio.Producto;

/**
 * Interfaz funcional propia (no de {@code java.util.function}) que representa
 * una regla de descuento aplicable a un {@link Producto}.
 *
 * Permite inyectar comportamiento personalizado en la fachada sin modificar
 * la lógica de {@link Producto#calcularDescuento()}. Por ejemplo:
 *
 * <pre>
 *     // Regla ya existente en el producto:
 *     ReglaDescuento reglaEstandar = Producto::calcularDescuento;
 *
 *     // Regla ad-hoc definida con lambda (promoción del 5% adicional):
 *     ReglaDescuento promoAdicional = p -> p.getPrecioUnitario() * 0.05;
 *
 *     double totalPromo = fachada.calcularTotalDescuentos(promoAdicional);
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
@FunctionalInterface
public interface ReglaDescuento {

    /**
     * Calcula el valor del descuento (en dinero) que la regla aplica
     * a un producto determinado.
     *
     * @param producto producto sobre el cual se evalúa la regla
     * @return el monto del descuento
     */
    double aplicar(Producto producto);
}
