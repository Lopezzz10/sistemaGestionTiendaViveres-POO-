package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Inventario;
import edu.uce.programacion2.tienda.negocio.Producto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Persistencia de {@link Inventario} en archivo binario (.dat).
 * Cada inventario se guarda en formato binario usando DataOutputStream/DataInputStream.
 *
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de inventarios)
 *   - Por cada inventario:
 *     - int (idInventario)
 *     - String (codigoProducto)
 *     - int (cantidadDisponible)
 *     - boolean (activo: false si el registro fue inactivado / borrado lógico)
 *
 * Carga sin error cuando el archivo aún no existe (primera ejecución).
 *
 * IMPORTANTE: cargar() requiere el catálogo completo (perecibles + no perecibles)
 * como parámetro para poder reconstruir el objeto Producto a partir del código.
 * Si el código no aparece en el catálogo, la línea se omite con advertencia.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class InventariosArchivo {

    private final Path archivo;

    /** Apunta al archivo {@code inventarios.dat} en la raíz del proyecto. */
    public InventariosArchivo() {
        this.archivo = Paths.get("inventarios.dat");
    }

    /**
     * Guarda todos los inventarios en el archivo binario.
     * Sobreescribe el contenido anterior.
     * Solo se guarda el código del producto (no el objeto completo).
     *
     * @param inventarios lista de inventarios a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Inventario> inventarios) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            // Primero guardamos cuántos inventarios hay
            salida.writeInt(inventarios.size());

            // Luego guardamos cada inventario campo por campo
            for (Inventario inv : inventarios) {
                salida.writeInt(inv.getIdInventario());

                String codigoProducto = (inv.getProducto() != null)
                        ? inv.getProducto().getCodigo()
                        : "";
                salida.writeUTF(codigoProducto);

                salida.writeInt(inv.getCantidadDisponible());
                salida.writeBoolean(inv.isActivo());
            }
        }
    }

    /**
     * Carga los inventarios desde el archivo binario, reconstruyendo el objeto {@link Producto}
     * buscando por código en el catálogo provisto.
     *
     * Si el archivo no existe, retorna lista vacía sin error.
     * Si el código de producto no se encuentra en el catálogo, la entrada se omite
     * con advertencia en consola.
     *
     * @param catalogo lista completa de productos (perecibles + no perecibles ya cargados)
     * @return lista de inventarios reconstruidos
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Inventario> cargar(ArrayList<Producto> catalogo) throws Exception {
        ArrayList<Inventario> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            // Leemos cuántos inventarios hay
            int cantidad = entrada.readInt();
            int numeroLinea = 0;

            // Leemos cada inventario campo por campo
            for (int i = 0; i < cantidad; i++) {
                numeroLinea++;

                int idInventario = entrada.readInt();
                String codigoProducto = entrada.readUTF();
                int cantidadDisponible = entrada.readInt();
                boolean activo = entrada.readBoolean();

                // Buscar el producto en el catálogo por código
                Producto productoEncontrado = buscarEnCatalogo(catalogo, codigoProducto);

                if (productoEncontrado == null) {
                    System.err.printf("[InventariosArchivo] Entrada %d omitida: " +
                                    "código de producto '%s' no encontrado en el catálogo.%n",
                            numeroLinea, codigoProducto);
                    continue;
                }

                Inventario inv = new Inventario(idInventario, productoEncontrado, cantidadDisponible);
                inv.setActivo(activo);
                lista.add(inv);
            }

        } catch (EOFException e) {
            // Si el archivo está corrupto o incompleto, devolvemos lo que se pudo leer
            System.err.println("[InventariosArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán los inventarios leídos parcialmente.");
        }
        return lista;
    }

    /**
     * Busca un producto en el catálogo por su código.
     *
     * @param catalogo lista de productos a consultar
     * @param codigo   código a buscar (insensible a mayúsculas)
     * @return el {@link Producto} encontrado, o {@code null} si no existe
     */
    private Producto buscarEnCatalogo(ArrayList<Producto> catalogo, String codigo) {
        for (Producto p : catalogo)
            if (p.getCodigo().equalsIgnoreCase(codigo))
                return p;
        return null;
    }
}