package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Administrador;
import edu.uce.programacion2.tienda.negocio.Cajero;
import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.negocio.Usuario;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Persistencia de {@link Usuario} (y sus subclases) en archivo binario (.dat).
 * Cada usuario se guarda en formato binario usando DataOutputStream/DataInputStream.
 *
 * Estructura del archivo:
 *   - Primer dato: int (cantidad total de usuarios)
 *   - Por cada usuario:
 *     - String (tipo: "ADMINISTRADOR" o "CAJERO")
 *     - int (idUsuario)
 *     - String (nombre)
 *     - String (email)
 *     - String (contrasena)
 *     - String (campoEspecifico: turno para Administrador, cajaAsignada para Cajero)
 *     - boolean (activo: false si el usuario fue inactivado / borrado lógico)
 *     - int (idRol: -1 si no tiene rol asignado)
 *
 * Carga sin error cuando el archivo aún no existe (primera ejecución).
 * Usuarios con tipo desconocido se omiten con advertencia.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class UsuariosArchivo {

    private static final String TIPO_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String TIPO_CAJERO        = "CAJERO";

    private final Path archivo;

    /** Apunta al archivo {@code usuarios.dat} en la raíz del proyecto. */
    public UsuariosArchivo() {
        this.archivo = Paths.get("usuarios.dat");
    }

    /**
     * Guarda todos los usuarios en el archivo binario.
     * Sobreescribe el contenido anterior.
     * Cada usuario se serializa con su tipo en la primera columna y
     * su campo específico (turno o cajaAsignada) en la última.
     *
     * @param usuarios lista de usuarios a persistir
     * @throws Exception si ocurre un error de E/S
     */
    public void guardar(ArrayList<Usuario> usuarios) throws Exception {
        try (DataOutputStream salida = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {

            // Primero guardamos cuántos usuarios hay
            salida.writeInt(usuarios.size());

            // Luego guardamos cada usuario campo por campo
            for (Usuario u : usuarios) {
                String tipo;
                String campoEspecifico;

                if (u instanceof Administrador) {
                    tipo = TIPO_ADMINISTRADOR;
                    campoEspecifico = ((Administrador) u).getTurno();
                } else if (u instanceof Cajero) {
                    tipo = TIPO_CAJERO;
                    campoEspecifico = String.valueOf(((Cajero) u).getCajaAsignada());
                } else {
                    // Subtipo desconocido: se omite con advertencia
                    System.err.printf("[UsuariosArchivo] Usuario id=%d omitido: " +
                                    "tipo desconocido '%s'.%n",
                            u.getIdUsuario(), u.getClass().getSimpleName());
                    continue;
                }

                salida.writeUTF(tipo);
                salida.writeInt(u.getIdUsuario());
                salida.writeUTF(u.getNombre());
                salida.writeUTF(u.getEmail());
                salida.writeUTF(u.getContrasena());
                salida.writeUTF(campoEspecifico);
                salida.writeBoolean(u.isActivo());

                // Guardar ID del rol (-1 si no tiene)
                int idRol = (u.getRol() != null) ? u.getRol().getIdRol() : -1;
                salida.writeInt(idRol);
            }
        }
    }

    /**
     * Carga los usuarios desde el archivo binario y los retorna en un {@code ArrayList<Usuario>}.
     * Reconstruye la subclase concreta ({@link Administrador} o {@link Cajero}) según
     * el campo {@code tipo} de cada entrada.
     * Si el archivo no existe, retorna lista vacía sin error.
     *
     * @return lista de usuarios (Administradores y Cajeros) leídos del archivo
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Usuario> cargar() throws Exception {
        ArrayList<Usuario> lista = new ArrayList<>();

        if (!Files.exists(archivo)) return lista;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            // Leemos cuántos usuarios hay
            int cantidad = entrada.readInt();
            int numeroLinea = 0;

            // Leemos cada usuario campo por campo
            for (int i = 0; i < cantidad; i++) {
                numeroLinea++;

                String tipo = entrada.readUTF();
                int idUsuario = entrada.readInt();
                String nombre = entrada.readUTF();
                String email = entrada.readUTF();
                String contrasena = entrada.readUTF();
                String campoEspecifico = entrada.readUTF();
                boolean activo = entrada.readBoolean();
                int idRol = entrada.readInt();  // ← NUEVO

                Usuario usuario = null;

                switch (tipo) {
                    case TIPO_ADMINISTRADOR:
                        Administrador admin = new Administrador(idUsuario, nombre, email,
                                contrasena, campoEspecifico);
                        admin.setActivo(activo);
                        usuario = admin;
                        break;

                    case TIPO_CAJERO:
                        int cajaAsignada;
                        try {
                            cajaAsignada = Integer.parseInt(campoEspecifico.trim());
                        } catch (NumberFormatException e) {
                            System.err.printf("[UsuariosArchivo] Entrada %d omitida: " +
                                            "cajaAsignada '%s' no es un número entero válido.%n",
                                    numeroLinea, campoEspecifico);
                            continue;
                        }
                        Cajero cajero = new Cajero(idUsuario, nombre, email,
                                contrasena, cajaAsignada);
                        cajero.setActivo(activo);
                        usuario = cajero;
                        break;

                    default:
                        System.err.printf("[UsuariosArchivo] Entrada %d omitida: " +
                                        "tipo de usuario desconocido '%s'.%n",
                                numeroLinea, tipo);
                        continue;
                }

                // Si el usuario tiene un rol asignado (idRol != -1), lo dejamos
                // para que sea asignado después por FachadaArchivos
                // Guardamos el idRol en un mapa temporal para asignarlo después
                if (usuario != null && idRol != -1) {
                    // Almacenamos el idRol para asignarlo después
                    // Usamos un mapa para guardar la relación usuario-idRol
                    // y luego en FachadaArchivos se asigna el rol
                    usuario.setRol(null); // Temporal, se asignará después
                    // Guardamos el idRol en un atributo temporal o en un mapa externo
                    // Por ahora, lo dejamos pendiente para que FachadaArchivos lo asigne
                }

                lista.add(usuario);
            }

        } catch (EOFException e) {
            // Si el archivo está corrupto o incompleto, devolvemos lo que se pudo leer
            System.err.println("[UsuariosArchivo] Archivo corrupto o incompleto. " +
                    "Se devolverán los usuarios leídos parcialmente.");
        }
        return lista;
    }

    /**
     * Carga los usuarios y asigna los roles correspondientes.
     * Este método debe ser llamado después de cargar los roles.
     *
     * @param roles lista de roles cargados
     * @return lista de usuarios con roles asignados
     * @throws Exception si ocurre un error de E/S
     */
    public ArrayList<Usuario> cargarConRoles(ArrayList<Rol> roles) throws Exception {
        ArrayList<Usuario> usuarios = cargar();

        if (usuarios == null || usuarios.isEmpty() || roles == null || roles.isEmpty()) {
            return usuarios;
        }

        // Crear un mapa de idRol -> Rol para búsqueda rápida
        Map<Integer, Rol> mapaRoles = new HashMap<>();
        for (Rol r : roles) {
            mapaRoles.put(r.getIdRol(), r);
        }

        // Reconstruir el archivo para obtener los idRol
        // Como no podemos leer el archivo dos veces, necesitamos un enfoque diferente
        // Vamos a leer el archivo nuevamente para obtener los idRol
        if (!Files.exists(archivo)) return usuarios;

        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {

            int cantidad = entrada.readInt();
            int idx = 0;

            for (int i = 0; i < cantidad && idx < usuarios.size(); i++) {
                String tipo = entrada.readUTF();
                int idUsuario = entrada.readInt();
                String nombre = entrada.readUTF();
                String email = entrada.readUTF();
                String contrasena = entrada.readUTF();
                String campoEspecifico = entrada.readUTF();
                boolean activo = entrada.readBoolean();
                int idRol = entrada.readInt();

                // Asignar el rol al usuario correspondiente
                if (idRol != -1 && idx < usuarios.size()) {
                    Rol rol = mapaRoles.get(idRol);
                    if (rol != null) {
                        usuarios.get(idx).setRol(rol);
                    }
                }
                idx++;
            }

        } catch (EOFException e) {
            System.err.println("[UsuariosArchivo] Error al leer roles del archivo.");
        }

        return usuarios;
    }
}