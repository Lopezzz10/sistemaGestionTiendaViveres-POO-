package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Administrador;
import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.io.*;
import java.util.ArrayList;

/**
 * Clase que gestiona la persistencia EXCLUSIVA de los Administradores en su
 * propio archivo binario de acceso aleatorio ("administradores.dat").
 *
 * Antes, Administrador, Cajero y Cliente compartian un unico archivo
 * ("usuarios.dat") con un discriminador de tipo por registro. Esta clase
 * separa esa tabla: cada tipo de usuario ahora vive en su propio archivo,
 * con un registro de tamano fijo que solo contiene los campos que
 * realmente le pertenecen (ya no hace falta "campoEspecifico" generico
 * ni columnas vacias de Cliente).
 *
 * El id sigue siendo unico a nivel de TODO el sistema de usuarios (no solo
 * de este archivo): lo asigna la clase coordinadora {@link Usuarios}, que
 * calcula el maximo id entre Administradores, Cajeros y Clientes antes de
 * agregar un registro nuevo (ver {@link #agregar(Administrador, int)}).
 *
 * El rol asignado (puede no tener) se guarda como clave foranea (idRol,
 * -1 si no tiene) y se resuelve en el momento de la lectura consultando
 * al Roles inyectado en el constructor.
 *
 * <pre>
 * estructura del registro (239 bytes):
 * idUsuario   int          4 bytes
 * nombre      40 chars    80 bytes
 * email       40 chars    80 bytes
 * contrasena  20 chars    40 bytes
 * turno       15 chars    30 bytes
 * activo      boolean      1 byte
 * idRol       int          4 bytes  (-1 = sin rol asignado)
 * total:                  239 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Administradores extends AccesoAleatorio {

    private static final int TAM_NOMBRE     = 40;
    private static final int TAM_EMAIL      = 40;
    private static final int TAM_CONTRASENA = 20;
    private static final int TAM_TURNO      = 15;
    private static final int TAM_REGISTRO =
            4 + (TAM_NOMBRE * 2) + (TAM_EMAIL * 2)
                    + (TAM_CONTRASENA * 2) + (TAM_TURNO * 2) + 1 + 4; // 239

    private Roles rolesDAO;

    public Administradores(String nomArchivo, Roles rolesDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.rolesDAO = rolesDAO;
    }

    public Administradores(String nomArchivo) {
        this(nomArchivo, null);
    }

    public Administradores() {
        this("administradores.dat", null);
    }

    public void setRolesDAO(Roles rolesDAO) {
        this.rolesDAO = rolesDAO;
    }

    private Administrador leeAdministrador() throws IOException {
        int idUsuario      = archivo.readInt();
        String nombre      = leeString(TAM_NOMBRE);
        String email       = leeString(TAM_EMAIL);
        String contrasena  = leeString(TAM_CONTRASENA);
        String turno       = leeString(TAM_TURNO);
        boolean activo     = archivo.readBoolean();
        int idRol          = archivo.readInt();

        Administrador a = new Administrador(idUsuario, nombre, email, contrasena, turno);
        a.setActivo(activo);

        if (idRol != -1 && rolesDAO != null) {
            try {
                a.setRol(rolesDAO.buscar(idRol));
            } catch (PersistenciaException pe) {
                a.setRol(null);
            }
        }
        return a;
    }

    private void escribeAdministrador(Administrador a) throws IOException {
        archivo.writeInt(a.getIdUsuario());
        escribeString(a.getNombre(), TAM_NOMBRE);
        escribeString(a.getEmail(), TAM_EMAIL);
        escribeString(a.getContrasena(), TAM_CONTRASENA);
        escribeString(a.getTurno(), TAM_TURNO);
        archivo.writeBoolean(a.isActivo());

        Rol rol = a.getRol();
        archivo.writeInt(rol != null ? rol.getIdRol() : -1);
    }

    /**
     * Agrega un administrador nuevo con un id ya asignado por el
     * coordinador {@link Usuarios} (que lo calcula sobre los tres
     * archivos de usuarios a la vez, para que nunca se repita entre
     * Administrador/Cajero/Cliente).
     */
    public void agregar(Administrador a, int idAsignado) throws PersistenciaException {
        if (a == null || a.getEmail() == null || a.getEmail().trim().isEmpty()) {
            throw new PersistenciaException("Administrador invalido.");
        }
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            a.setIdUsuario(idAsignado);
            archivo.seek(archivo.length());
            escribeAdministrador(a);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de administradores.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el administrador.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Calcula el mayor id existente en este archivo (0 si esta vacio). Uso interno de {@link Usuarios}. */
    public int maxId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodos(), Administrador::getIdUsuario) - 1;
    }

    public Administrador buscar(int idUsuario) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Administrador a = leeAdministrador();
                    if (a.getIdUsuario() == idUsuario) return a;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Administrador no encontrado: id=" + idUsuario);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Administrador no encontrado: id=" + idUsuario);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el administrador.");
        }
    }

    public Administrador buscarPorEmail(String email) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Administrador a = leeAdministrador();
                    if (a.getEmail().equalsIgnoreCase(email)) return a;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Administrador no encontrado con email: " + email);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Administrador no encontrado con email: " + email);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el administrador.");
        }
    }

    public void actualizar(Administrador a) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Administrador leido = leeAdministrador();
                    if (leido.getIdUsuario() == a.getIdUsuario()) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeAdministrador(a);
                        return;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Administrador no encontrado para actualizar.");
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de administradores no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el administrador.");
        }
    }

    public void inactivar(int idUsuario) throws PersistenciaException {
        Administrador a = buscar(idUsuario);
        a.setActivo(false);
        actualizar(a);
    }

    public ArrayList<Administrador> listarActivos() throws PersistenciaException {
        ArrayList<Administrador> resultado = new ArrayList<>();
        for (Administrador a : obtenerTodos()) {
            if (a.isActivo()) resultado.add(a);
        }
        return resultado;
    }

    public ArrayList<Administrador> obtenerTodos() throws PersistenciaException {
        ArrayList<Administrador> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeAdministrador());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los administradores.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }
}