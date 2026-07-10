package edu.uce.programacion2.tienda.objetosServicio;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Objeto de servicio responsable de calcular el siguiente id
 * auto-incremental para cualquier colección del sistema.
 *
 * Antes, cada pantalla (DlgProveedor, DlgUsuario, DlgInventario, etc.)
 * calculaba el "nuevo id" a mano con {@code lista.size() + 1}, lo que
 * generaba ids duplicados en cuanto un registro se inactivaba o se
 * borraba de la lista (el tamaño de la lista deja de coincidir con el
 * máximo id usado). Otras pantallas (DlgCompra, DlgVenta) directamente
 * le pedían al usuario que escriba el id a mano, lo cual es propenso a
 * error humano y a colisiones.
 *
 * Este generador centraliza la regla real de un auto-incremental:
 * "el máximo id ya usado, más uno", calculado sobre TODOS los
 * registros (activos e inactivos), por lo que un id jamás se repite
 * aunque haya borrados lógicos de por medio.
 *
 * Al ser genérico (usa {@link ToIntFunction} para leer el id de
 * cualquier tipo de objeto), un solo método sirve para Proveedor,
 * Usuario, Inventario, Compra, Venta y Rol sin duplicar la lógica.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public final class GeneradorId {

    private GeneradorId() {
        // Clase de utilidades: no se instancia.
    }

    /**
     * Calcula el siguiente id auto-incremental para una colección.
     *
     * @param lista       colección actual de entidades (puede incluir inactivas)
     * @param obtenerId   función que extrae el id entero de cada entidad
     * @param <T>         tipo de la entidad
     * @return el mayor id existente + 1, o 1 si la colección está vacía
     */
    public static <T> int siguienteId(List<T> lista, ToIntFunction<T> obtenerId) {
        return lista.stream()
                .mapToInt(obtenerId)
                .max()
                .orElse(0) + 1;
    }
}
