package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.DetalleVenta;
import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.negocio.Venta;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

/**
 * Persistencia de {@link Venta} en archivo binario (.dat).
 * Cada venta se guarda en formato binario usando DataOutputStream/DataInputStream.
 *
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de ventas)
 *   - Por cada venta:
 *     - int (idVenta)
 *     - long (fecha en milisegundos)
 *     - String (metodoPago)
 *     - String (estado: ACTIVA o ANULADA)
 *     - int (cantidad de detalles)
 *     - Por cada detalle:
 *       - String (codigoProducto)
 *       - int (cantidad)
 *       - double (precioUnitario)
 *       - double (descuento)
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class VentasArchivo {

    private final Path archivo;

    public VentasArchivo() {
        this.archivo = Paths.get("ventas.dat");
    }

    /**
     * Guarda todas las ventas en el archivo binario.
     * Sobreescribe el contenido anterior.
     *
     * @param ventas lista de ventas a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Venta> ventas) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            // Primero guardamos cuántas ventas hay
            salida.writeInt(ventas.size());

            // Luego guardamos cada venta
            for (Venta v : ventas) {
                salida.writeInt(v.getIdVenta());
                salida.writeLong(v.getFecha().getTime());
                salida.writeUTF(v.getMetodoPago());
                salida.writeUTF(v.getEstado().name());

                // Guardamos los detalles
                ArrayList<DetalleVenta> detalles = v.getDetalles();
                salida.writeInt(detalles.size());

                for (DetalleVenta d : detalles) {
                    salida.writeUTF(d.getProducto().getCodigo());
                    salida.writeInt(d.getCantidad());
                    salida.writeDouble(d.getPrecioUnitario());
                    salida.writeDouble(d.getDescuento());
                }
            }
        }
    }

    /**
     * Carga las ventas desde el archivo binario y las retorna en un {@code ArrayList}.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @param catalogo lista completa de productos para reconstruir los detalles
     * @return lista de ventas leídas del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Venta> cargar(ArrayList<Producto> catalogo) throws Exception {
        ArrayList<Venta> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            // Leemos cuántas ventas hay
            int cantidad = entrada.readInt();  // ← Variable 'cantidad' (total de ventas)
            int numLinea = 0;

            // Leemos cada venta
            for (int i = 0; i < cantidad; i++) {
                numLinea++;

                int id = entrada.readInt();
                long fechaMillis = entrada.readLong();
                Date fecha = new Date(fechaMillis);
                String metodo = entrada.readUTF();
                Venta.Estado estado = Venta.Estado.valueOf(entrada.readUTF());

                Venta v = new Venta(id, metodo);
                v.setFecha(fecha);
                v.setEstado(estado);

                // Leemos los detalles
                int cantDetalles = entrada.readInt();

                for (int j = 0; j < cantDetalles; j++) {
                    String codigo = entrada.readUTF();
                    int cantidadProducto = entrada.readInt();  // ← Cambiado a 'cantidadProducto'
                    double precioUnitario = entrada.readDouble();
                    double descuento = entrada.readDouble();

                    // Buscar el producto en el catálogo
                    Producto producto = buscarEnCatalogo(catalogo, codigo);

                    if (producto != null) {
                        DetalleVenta detalle = new DetalleVenta(0, producto, cantidadProducto, descuento);
                        v.agregarDetalle(detalle);
                    } else {
                        System.err.printf("[VentasArchivo] Venta %d omitida: " +
                                        "producto con código '%s' no encontrado en el catálogo.%n",
                                numLinea, codigo);
                    }
                }

                lista.add(v);
            }

        } catch (EOFException e) {
            // Si el archivo está corrupto o incompleto, devolvemos lo que se pudo leer
            System.err.println("[VentasArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán las ventas leídas parcialmente.");
        }
        return lista;
    }

    /**
     * Busca un producto en el catálogo por su código.
     *
     * @param catalogo lista de productos a consultar
     * @param codigo   código a buscar
     * @return el {@link Producto} encontrado, o {@code null} si no existe
     */
    private Producto buscarEnCatalogo(ArrayList<Producto> catalogo, String codigo) {
        for (Producto p : catalogo) {
            if (p.getCodigo().equals(codigo)) {
                return p;
            }
        }
        return null;
    }
}