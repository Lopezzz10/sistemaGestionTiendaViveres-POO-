package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.objetosServicio.Permiso;
import edu.uce.programacion2.tienda.negocio.Rol;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

/**
 * Persistencia de {@link Rol} en archivo binario (.dat).
 * Cada rol se guarda en formato binario usando DataOutputStream/DataInputStream.
 *
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de roles)
 *   - Por cada rol:
 *     - int     (idRol)
 *     - String  (nombreCargo)
 *     - boolean (activo: false si el rol fue inactivado / borrado lógico)
 *     - int     (cantidad de permisos)
 *     - por cada permiso: String (nombre de la constante del enum Permiso)
 *
 * Carga sin error cuando el archivo aún no existe (primera ejecución).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class RolesArchivo {

    private final Path archivo;

    /** Apunta al archivo {@code roles.dat} en la raíz del proyecto. */
    public RolesArchivo() {
        this.archivo = Paths.get("roles.dat");
    }

    /**
     * Guarda todos los roles en el archivo binario.
     * Sobreescribe el contenido anterior.
     *
     * @param roles lista de roles a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Rol> roles) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            salida.writeInt(roles.size());

            for (Rol r : roles) {
                salida.writeInt(r.getIdRol());
                salida.writeUTF(r.getNombreCargo());
                salida.writeBoolean(r.isActivo());

                Set<Permiso> permisos = r.getPermisos();
                salida.writeInt(permisos.size());
                for (Permiso p : permisos) {
                    salida.writeUTF(p.name());
                }
            }
        }
    }

    /**
     * Carga los roles desde el archivo binario y los retorna en un {@code ArrayList}.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @return lista de roles leídos del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Rol> cargar() throws Exception {
        ArrayList<Rol> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            int cantidad = entrada.readInt();

            for (int i = 0; i < cantidad; i++) {
                int idRol = entrada.readInt();
                String nombreCargo = entrada.readUTF();
                boolean activo = entrada.readBoolean();

                int cantidadPermisos = entrada.readInt();
                Set<Permiso> permisos = EnumSet.noneOf(Permiso.class);
                for (int j = 0; j < cantidadPermisos; j++) {
                    String nombrePermiso = entrada.readUTF();
                    try {
                        permisos.add(Permiso.valueOf(nombrePermiso));
                    } catch (IllegalArgumentException ex) {
                        // Permiso que ya no existe en el enum (versión anterior): se ignora.
                        System.err.println("[RolesArchivo] Permiso desconocido ignorado: " + nombrePermiso);
                    }
                }

                Rol r = new Rol(idRol, nombreCargo, permisos);
                r.setActivo(activo);
                lista.add(r);
            }
        } catch (EOFException e) {
            // Si el archivo está corrupto o incompleto, devolvemos lo que se pudo leer
            System.err.println("[RolesArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán los roles leídos parcialmente.");
        }
        return lista;
    }
}
