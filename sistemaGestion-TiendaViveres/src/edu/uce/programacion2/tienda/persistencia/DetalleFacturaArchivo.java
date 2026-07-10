package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Producto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Persistencia de los detalles de factura en archivo binario (.dat).
 * Cada registro guarda tanto el {@code idFactura} al que pertenece como
 * los datos propios de la línea de detalle.
 * <p>
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de registros)
 *   - Por cada registro:
 *     - int    (idFactura)
 *     - int    (idDetalle)
 *     - String (codigoProducto)
 *     - String (nombreProducto)
 *     - int    (cantidad)
 *     - double (precioUnitarioSinIva)
 *     - double (descuento)
 *
 * (montoIva y subtotal no se guardan: se recalculan automáticamente al
 * reconstruir el objeto, igual que hace {@link edu.uce.programacion2.tienda.negocio.DetalleFactura}
 * en su constructor completo).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetalleFacturaArchivo {

    private final Path archivo;

    public DetalleFacturaArchivo() {
        this.archivo = Paths.get("detallefactura.dat");
    }

    /**
     * Guarda todos los registros de detalle de factura en el archivo binario.
     * Sobreescribe el contenido anterior.
     *
     * @param registros lista de registros (idFactura + detalle) a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<DetalleFactura.Registro> registros) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            salida.writeInt(registros.size());

            for (DetalleFactura.Registro r : registros) {
                edu.uce.programacion2.tienda.negocio.DetalleFactura d = r.getDetalle();
                Producto p = d.getProducto();

                salida.writeInt(r.getIdFactura());
                salida.writeInt(d.getIdDetalle());
                salida.writeUTF(p != null ? p.getCodigo() : "");
                salida.writeUTF(p != null ? p.getNombre() : "");
                salida.writeInt(d.getCantidad());
                salida.writeDouble(d.getPrecioUnitarioSinIva());
                salida.writeDouble(d.getDescuento());
            }
        }
    }

    /**
     * Carga los registros desde el archivo binario y los retorna en un {@code ArrayList}.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @param catalogo lista completa de productos para reconstruir cada detalle
     * @return lista de registros (idFactura + detalle) leídos del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<DetalleFactura.Registro> cargar(ArrayList<Producto> catalogo) throws Exception {
        ArrayList<DetalleFactura.Registro> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            int cantidad = entrada.readInt();

            for (int i = 0; i < cantidad; i++) {
                int idFactura = entrada.readInt();
                int idDetalle = entrada.readInt();
                String codigo = entrada.readUTF();
                String nombre = entrada.readUTF();
                int cantidadProd = entrada.readInt();
                double precioSinIva = entrada.readDouble();
                double descuento = entrada.readDouble();

                Producto producto = buscarProducto(catalogo, codigo);
                if (producto == null) {
                    producto = new Producto();
                    producto.setCodigo(codigo);
                    producto.setNombre(nombre.isEmpty() ? "Producto " + codigo : nombre);
                }

                edu.uce.programacion2.tienda.negocio.DetalleFactura detalle =
                        new edu.uce.programacion2.tienda.negocio.DetalleFactura(
                                idDetalle, producto, cantidadProd, precioSinIva, descuento);

                lista.add(new DetalleFactura.Registro(idFactura, detalle));
            }

        } catch (EOFException e) {
            System.err.println("[DetalleFacturaArchivo] Archivo incompleto. Se devuelven los detalles leídos.");
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
}