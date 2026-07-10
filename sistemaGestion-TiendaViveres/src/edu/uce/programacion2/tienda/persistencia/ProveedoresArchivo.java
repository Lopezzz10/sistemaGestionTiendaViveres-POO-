package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Proveedor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Persistencia de {@link Proveedor} en archivo binario (.dat).
 * Cada proveedor se guarda en formato binario usando DataOutputStream/DataInputStream.
 *
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de proveedores)
 *   - Por cada proveedor:
 *     - int (idProveedor)
 *     - String (nombre)
 *     - String (ruc)
 *     - String (telefono)
 *     - String (email)
 *     - String (direccion)
 *     - boolean (activo: false si el proveedor fue inactivado / borrado lógico)
 *
 * Carga sin error cuando el archivo aún no existe (primera ejecución).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class ProveedoresArchivo {

    private final Path archivo;

    /** Apunta al archivo {@code proveedores.dat} en la raíz del proyecto. */
    public ProveedoresArchivo() {
        this.archivo = Paths.get("proveedores.dat");
    }

    /**
     * Guarda todos los proveedores en el archivo binario.
     * Sobreescribe el contenido anterior.
     *
     * @param proveedores lista de proveedores a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Proveedor> proveedores) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            // Primero guardamos cuántos proveedores hay
            salida.writeInt(proveedores.size());

            // Luego guardamos cada proveedor campo por campo
            for (Proveedor p : proveedores) {
                salida.writeInt(p.getIdProveedor());
                salida.writeUTF(p.getNombre());
                salida.writeUTF(p.getRuc());
                salida.writeUTF(p.getTelefono());
                salida.writeUTF(p.getEmail());
                salida.writeUTF(p.getDireccion());
                salida.writeBoolean(p.isActivo());
            }
        }
    }

    /**
     * Carga los proveedores desde el archivo binario y los retorna en un {@code ArrayList}.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @return lista de proveedores leídos del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Proveedor> cargar() throws Exception {
        ArrayList<Proveedor> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            // Leemos cuántos proveedores hay
            int cantidad = entrada.readInt();
            int numeroLinea = 0;

            // Leemos cada proveedor campo por campo
            for (int i = 0; i < cantidad; i++) {
                numeroLinea++;

                int idProveedor = entrada.readInt();
                String nombre = entrada.readUTF();
                String ruc = entrada.readUTF();
                String telefono = entrada.readUTF();
                String email = entrada.readUTF();
                String direccion = entrada.readUTF();
                boolean activo = entrada.readBoolean();

                Proveedor p = new Proveedor(idProveedor, nombre, ruc, telefono, email, direccion);
                p.setActivo(activo);
                lista.add(p);
            }

        } catch (EOFException e) {
            // Si el archivo está corrupto o incompleto, devolvemos lo que se pudo leer
            System.err.println("[ProveedoresArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán los proveedores leídos parcialmente.");
        }
        return lista;
    }
}