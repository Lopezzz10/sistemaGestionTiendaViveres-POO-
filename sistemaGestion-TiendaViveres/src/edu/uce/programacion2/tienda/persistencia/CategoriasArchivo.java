package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Categoria;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Persistencia de {@link Categoria} en archivo binario (.dat).
 * Cada categoría se guarda en formato binario usando DataOutputStream/DataInputStream.
 *
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de categorías)
 *   - Por cada categoría:
 *     - String (cveCategoria)
 *     - String (nombre)
 *     - char (tipoProducto)
 *     - boolean (ivaEspecial: true si la categoría tiene tarifa de IVA fija)
 *     - double (tarifaIvaEspecial: tarifa fija, ej. 0.0 = 0%. Ignorada si ivaEspecial=false)
 *     - boolean (activo: false si la categoría fue inactivada / borrado lógico)
 *
 * NOTA DE MIGRACIÓN: este formato cambió al agregar la tarifa de IVA por
 * categoría, y de nuevo al agregar el campo "activo" para el borrado lógico.
 * Un categorias.dat generado con una versión anterior (sin estos campos)
 * no es compatible y debe eliminarse/regenerarse.
 *
 * Carga sin error cuando el archivo aún no existe (primera ejecución).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class CategoriasArchivo {

    private final Path archivo;

    /** Apunta al archivo {@code categorias.dat} en la raíz del proyecto. */
    public CategoriasArchivo() {
        this.archivo = Paths.get("categorias.dat");
    }

    /**
     * Guarda todas las categorías en el archivo binario.
     * Sobreescribe el contenido anterior.
     *
     * @param categorias lista de categorías a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Categoria> categorias) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            // Primero guardamos cuántas categorías hay
            salida.writeInt(categorias.size());

            // Luego guardamos cada categoría campo por campo
            for (Categoria c : categorias) {
                salida.writeUTF(c.getCveCategoria());
                salida.writeUTF(c.getNombre());
                salida.writeChar(c.getTipoProducto());
                salida.writeBoolean(c.isIvaEspecial());
                salida.writeDouble(c.getTarifaIvaEspecial());
                salida.writeBoolean(c.isActivo());
            }
        }
    }

    /**
     * Carga las categorías desde el archivo binario y las retorna en un {@code ArrayList}.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @return lista de categorías leídas del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Categoria> cargar() throws Exception {
        ArrayList<Categoria> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            // Leemos cuántas categorías hay
            int cantidad = entrada.readInt();

            // Leemos cada categoría campo por campo
            for (int i = 0; i < cantidad; i++) {
                String cveCategoria = entrada.readUTF();
                String nombre = entrada.readUTF();
                char tipoProducto = entrada.readChar();
                boolean ivaEspecial = entrada.readBoolean();
                double tarifaIvaEspecial = entrada.readDouble();
                boolean activo = entrada.readBoolean();

                Categoria c = new Categoria(cveCategoria, nombre, tipoProducto,
                        ivaEspecial, tarifaIvaEspecial);
                c.setActivo(activo);
                lista.add(c);
            }
        } catch (EOFException e) {
            // Si el archivo está corrupto o incompleto, devolvemos lo que se pudo leer
            System.err.println("[CategoriasArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán las categorías leídas parcialmente.");
        }
        return lista;
    }
}