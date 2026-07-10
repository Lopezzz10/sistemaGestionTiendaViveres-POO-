package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Categoria;
import edu.uce.programacion2.tienda.negocio.Producto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

/**
 * Persistencia unificada de {@link Producto} en archivo binario (.dat).
 * Reemplaza a ProductosPereciblesArchivo y ProductosNoPereciblesArchivo.
 *
 * Estructura del archivo:
 *   - int    (cantidad total de productos)
 *   - Por cada producto:
 *     - String (codigo)
 *     - String (nombre)
 *     - String (cveCategoria)
 *     - String (nombreCategoria)
 *     - char   (tipoCategoria)
 *     - double (precioUnitario)
 *     - String (tipo)             → "Perecible" o "No Perecible"
 *     - long   (fechaVencimiento) → milisegundos desde 1970, 0 si no aplica
 *     - double (temperaturaAlmacenamiento) → 0.0 si no aplica
 *     - double (pesoKg)           → 0.0 si no aplica
 *     - String (marca)            → "" si no aplica
 *     - String (estado)
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class ProductosArchivo {

    private final Path archivo;

    /** Apunta al archivo {@code productos.dat} en la raíz del proyecto. */
    public ProductosArchivo() {
        this.archivo = Paths.get("productos.dat");
    }

    /**
     * Guarda todos los productos en el archivo binario.
     * Sobreescribe el contenido anterior.
     *
     * @param productos lista de productos a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Producto> productos) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            salida.writeInt(productos.size());

            for (Producto p : productos) {
                salida.writeUTF(p.getCodigo());
                salida.writeUTF(p.getNombre());

                // Categoría
                if (p.getCategoria() != null) {
                    salida.writeUTF(p.getCategoria().getCveCategoria());
                    salida.writeUTF(p.getCategoria().getNombre());
                    salida.writeChar(p.getCategoria().getTipoProducto());
                } else {
                    salida.writeUTF("");
                    salida.writeUTF("");
                    salida.writeChar(' ');
                }

                salida.writeDouble(p.getPrecioUnitario());
                salida.writeUTF(p.getTipo());

                // Campos de Perecible (0 si no aplica)
                salida.writeLong(p.getFechaVencimiento() != null
                        ? p.getFechaVencimiento().getTime() : 0L);
                salida.writeDouble(p.getTemperaturaAlmacenamiento());

                // Campos de No Perecible ("" / 0.0 si no aplica)
                salida.writeDouble(p.getPesoKg());
                salida.writeUTF(p.getMarca());

                salida.writeUTF(p.getEstado());
            }
        }
    }

    /**
     * Carga los productos desde el archivo binario.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @return lista de productos leídos del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Producto> cargar() throws Exception {
        ArrayList<Producto> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            int cantidad = entrada.readInt();

            for (int i = 0; i < cantidad; i++) {
                String codigo  = entrada.readUTF();
                String nombre  = entrada.readUTF();

                // Categoría
                String cveCat  = entrada.readUTF();
                String nomCat  = entrada.readUTF();
                char   tipoCat = entrada.readChar();

                double precio  = entrada.readDouble();
                String tipo    = entrada.readUTF();

                // Campos de Perecible
                long   fechaMillis = entrada.readLong();
                double temp        = entrada.readDouble();

                // Campos de No Perecible
                double pesoKg = entrada.readDouble();
                String marca  = entrada.readUTF();

                String estado = entrada.readUTF();

                // Reconstruir categoría
                Categoria cat = new Categoria(cveCat, nomCat, tipoCat);

                // Reconstruir producto según tipo
                Producto p;
                if ("Perecible".equals(tipo)) {
                    Date fecha = (fechaMillis > 0) ? new Date(fechaMillis) : null;
                    p = new Producto(codigo, nombre, cat, precio, fecha, temp);
                } else {
                    p = new Producto(codigo, nombre, cat, precio, pesoKg, marca);
                }
                p.setEstado(estado);
                lista.add(p);
            }

        } catch (EOFException e) {
            System.err.println("[ProductosArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán los productos leídos parcialmente.");
        }

        return lista;
    }
}