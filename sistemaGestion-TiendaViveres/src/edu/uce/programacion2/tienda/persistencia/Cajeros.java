package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Cajero;
import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la persistencia EXCLUSIVA de los Cajeros en su propio
 * archivo binario de acceso aleatorio ("cajeros.dat").
 *
 * Ver el comentario de clase de {@link Administradores}: es la misma idea,
 * aplicada a Cajero. La caja asignada ya no se guarda como texto generico
 * ("campoEspecifico"), sino como un int real.
 *
 * <pre>
 * estructura del registro (213 bytes):
 * idUsuario      int          4 bytes
 * nombre         40 chars    80 bytes
 * email          40 chars    80 bytes
 * contrasena     20 chars    40 bytes
 * cajaAsignada   int          4 bytes
 * activo         boolean      1 byte
 * idRol          int          4 bytes  (-1 = sin rol asignado)
 * total:                     213 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Cajeros extends AccesoAleatorio {

    private static final int TAM_NOMBRE     = 40;
    private static final int TAM_EMAIL      = 40;
    private static final int TAM_CONTRASENA = 20;
    private static final int TAM_REGISTRO =
            4 + (TAM_NOMBRE * 2) + (TAM_EMAIL * 2)
                    + (TAM_CONTRASENA * 2) + 4 + 1 + 4; // 213

    private Roles rolesDAO;

    public Cajeros(String nomArchivo, Roles rolesDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.rolesDAO = rolesDAO;
    }

    public Cajeros(String nomArchivo) {
        this(nomArchivo, null);
    }

    public Cajeros() {
        this("cajeros.dat", null);
    }

    public void setRolesDAO(Roles rolesDAO) {
        this.rolesDAO = rolesDAO;
    }

    private Cajero leeCajero() throws IOException {
        int idUsuario      = archivo.readInt();
        String nombre      = leeString(TAM_NOMBRE);
        String email       = leeString(TAM_EMAIL);
        String contrasena  = leeString(TAM_CONTRASENA);
        int cajaAsignada   = archivo.readInt();
        boolean activo     = archivo.readBoolean();
        int idRol          = archivo.readInt();

        Cajero c = new Cajero(idUsuario, nombre, email, contrasena, cajaAsignada);
        c.setActivo(activo);

        if (idRol != -1 && rolesDAO != null) {
            try {
                c.setRol(rolesDAO.buscar(idRol));
            } catch (PersistenciaException pe) {
                c.setRol(null);
            }
        }
        return c;
    }

    private void escribeCajero(Cajero c) throws IOException {
        archivo.writeInt(c.getIdUsuario());
        escribeString(c.getNombre(), TAM_NOMBRE);
        escribeString(c.getEmail(), TAM_EMAIL);
        escribeString(c.getContrasena(), TAM_CONTRASENA);
        archivo.writeInt(c.getCajaAsignada());
        archivo.writeBoolean(c.isActivo());

        Rol rol = c.getRol();
        archivo.writeInt(rol != null ? rol.getIdRol() : -1);
    }

    /**
     * Agrega un cajero nuevo con un id ya asignado por el coordinador
     * (que lo calcula sobre los tres archivos de
     * usuarios a la vez, para que nunca se repita entre
     * Administrador/Cajero/Cliente).
     */
    public void agregar(Cajero c, int idAsignado) throws PersistenciaException {
        if (c == null || c.getEmail() == null || c.getEmail().trim().isEmpty()) {
            throw new PersistenciaException("Cajero invalido.");
        }
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            c.setIdUsuario(idAsignado);
            archivo.seek(archivo.length());
            escribeCajero(c);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de cajeros.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el cajero.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }
    public int maxId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodos(), Cajero::getIdUsuario) - 1;
    }

    public Cajero buscar(int idUsuario) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                Cajero c = buscarConStream(this::leeCajero, x -> x.getIdUsuario() == idUsuario);
                if (c == null) throw new PersistenciaException("Cajero no encontrado: id=" + idUsuario);
                return c;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Cajero no encontrado: id=" + idUsuario);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el cajero.");
        }
    }

    public Cajero buscarPorEmail(String email) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                Cajero c = buscarConStream(this::leeCajero, x -> x.getEmail().equalsIgnoreCase(email));
                if (c == null) throw new PersistenciaException("Cajero no encontrado con email: " + email);
                return c;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Cajero no encontrado con email: " + email);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el cajero.");
        }
    }

    public void actualizar(Cajero c) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                long indice = indiceConStream(this::leeCajero, x -> x.getIdUsuario() == c.getIdUsuario());
                if (indice == -1) {
                    throw new PersistenciaException("Cajero no encontrado para actualizar.");
                }
                archivo.seek(indice * tamRegistro);
                escribeCajero(c);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de cajeros no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el cajero.");
        }
    }

    public void inactivar(int idUsuario) throws PersistenciaException {
        Cajero c = buscar(idUsuario);
        c.setActivo(false);
        actualizar(c);
    }

    public ArrayList<Cajero> listarActivos() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(Cajero::isActivo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Cajero> obtenerTodos() throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                return leerTodosConStream(this::leeCajero);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return new ArrayList<>();
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los cajeros.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }
}