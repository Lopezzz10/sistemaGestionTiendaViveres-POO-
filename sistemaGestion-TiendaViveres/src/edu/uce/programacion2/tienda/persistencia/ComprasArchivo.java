package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Compra;
import edu.uce.programacion2.tienda.negocio.DetalleCompra;
import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.negocio.Proveedor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

/**
 * Persistencia de {@link Compra} en archivo binario (.dat).
 * Cada compra se guarda en formato binario usando DataOutputStream/DataInputStream.
 *
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de compras)
 *   - Por cada compra:
 *     - int (idCompra)
 *     - long (fecha en milisegundos)
 *     - String (metodoPago)
 *     - String (estado: COMPLETADA o ANULADA)
 *     - int (idProveedor, -1 si es null)
 *     - int (cantidad de detalles)
 *     - Por cada detalle:
 *       - String (codigoProducto)
 *       - int (cantidad)
 *       - double (precioCompra)
 *       - double (descuento)
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class ComprasArchivo {

    private final Path archivo;

    public ComprasArchivo() {
        this.archivo = Paths.get("compras.dat");
    }

    /**
     * Guarda todas las compras en el archivo binario.
     * Sobreescribe el contenido anterior.
     *
     * @param compras lista de compras a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Compra> compras) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            // Primero guardamos cuántas compras hay
            salida.writeInt(compras.size());

            // Luego guardamos cada compra
            for (Compra c : compras) {
                salida.writeInt(c.getIdCompra());
                salida.writeLong(c.getFecha().getTime());
                salida.writeUTF(c.getMetodoPago());
                salida.writeUTF(c.getEstado().name());

                // Guardamos el ID del proveedor (-1 si es null)
                int idProv = (c.getProveedor() != null) ? c.getProveedor().getIdProveedor() : -1;
                salida.writeInt(idProv);

                // Guardamos los detalles
                ArrayList<DetalleCompra> detalles = c.getDetalles();
                salida.writeInt(detalles.size());

                for (DetalleCompra d : detalles) {
                    salida.writeUTF(d.getProducto().getCodigo());
                    salida.writeInt(d.getCantidad());
                    salida.writeDouble(d.getPrecioCompra());
                    salida.writeDouble(d.getDescuento());
                }
            }
        }
    }

    /**
     * Carga las compras desde el archivo binario y las retorna en un {@code ArrayList}.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @param catalogo lista completa de productos para reconstruir los detalles
     * @param proveedores lista completa de proveedores para reconstruir la compra
     * @return lista de compras leídas del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Compra> cargar(ArrayList<Producto> catalogo,
                                    ArrayList<Proveedor> proveedores) throws Exception {
        ArrayList<Compra> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            // Leemos cuántas compras hay
            int cantidad = entrada.readInt();
            int numLinea = 0;

            // Leemos cada compra
            for (int i = 0; i < cantidad; i++) {
                numLinea++;

                int id = entrada.readInt();
                long fechaMillis = entrada.readLong();
                Date fecha = new Date(fechaMillis);
                String metodo = entrada.readUTF();
                Compra.Estado estado = Compra.Estado.valueOf(entrada.readUTF());

                // Leemos el ID del proveedor y lo buscamos
                int idProv = entrada.readInt();
                Proveedor proveedor = buscarProveedor(proveedores, idProv);

                Compra c = new Compra(id, metodo, proveedor, null);
                c.setFecha(fecha);
                c.setEstado(estado);

                // Leemos los detalles
                int cantDetalles = entrada.readInt();

                for (int j = 0; j < cantDetalles; j++) {
                    String codigo = entrada.readUTF();
                    int cantidadProducto = entrada.readInt();
                    double precioCompra = entrada.readDouble();
                    double descuento = entrada.readDouble();

                    // Buscar el producto en el catálogo
                    Producto producto = buscarProducto(catalogo, codigo);

                    if (producto != null) {
                        DetalleCompra detalle = new DetalleCompra(0, producto, cantidadProducto, precioCompra, descuento);
                        c.agregarDetalle(detalle);
                    } else {
                        System.err.printf("[ComprasArchivo] Compra %d: " +
                                        "producto con código '%s' no encontrado en el catálogo.%n",
                                numLinea, codigo);
                    }
                }

                lista.add(c);
            }

        } catch (EOFException e) {
            // Si el archivo está corrupto o incompleto, devolvemos lo que se pudo leer
            System.err.println("[ComprasArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán las compras leídas parcialmente.");
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
    private Producto buscarProducto(ArrayList<Producto> catalogo, String codigo) {
        for (Producto p : catalogo) {
            if (p.getCodigo().equals(codigo)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Busca un proveedor en la lista por su ID.
     *
     * @param proveedores lista de proveedores a consultar
     * @param idProveedor ID del proveedor a buscar
     * @return el {@link Proveedor} encontrado, o {@code null} si no existe
     */
    private Proveedor buscarProveedor(ArrayList<Proveedor> proveedores, int idProveedor) {
        for (Proveedor p : proveedores) {
            if (p.getIdProveedor() == idProveedor) {
                return p;
            }
        }
        return null;
    }
}