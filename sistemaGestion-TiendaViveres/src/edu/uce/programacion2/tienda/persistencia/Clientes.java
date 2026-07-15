package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Cliente;
import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.io.*;
import java.util.ArrayList;

/**
 * Clase que gestiona la persistencia EXCLUSIVA de los Clientes en su
 * propio archivo binario de acceso aleatorio ("clientes.dat").
 *
 * Ver el comentario de clase de {@link Administradores}: es la misma idea,
 * aplicada a Cliente. Aqui cedula, direccion, telefono y puntosFidelidad
 * ya no comparten columnas geneticas con Administrador/Cajero: cada uno
 * tiene su propio campo real.
 *
 * El id lo asigna el coordinador {@link Usuarios} (ver
 * {@link #agregar(Cliente, int)}), para que nunca se repita entre
 * Administrador/Cajero/Cliente aunque ahora vivan en archivos separados.
 *
 * <pre>
 * estructura del registro (333 bytes):
 * idUsuario         int          4 bytes
 * nombre            40 chars    80 bytes
 * email             40 chars    80 bytes
 * contrasena        20 chars    40 bytes
 * cedula            15 chars    30 bytes
 * direccion         30 chars    60 bytes
 * telefono          15 chars    30 bytes
 * puntosFidelidad   int          4 bytes
 * activo            boolean      1 byte
 * idRol             int          4 bytes  (-1 = sin rol asignado)
 * total:                        333 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Clientes extends AccesoAleatorio {

    private static final int TAM_NOMBRE     = 40;
    private static final int TAM_EMAIL      = 40;
    private static final int TAM_CONTRASENA = 20;
    private static final int TAM_CEDULA     = 15;
    private static final int TAM_DIRECCION  = 30;
    private static final int TAM_TELEFONO   = 15;
    private static final int TAM_REGISTRO =
            4 + (TAM_NOMBRE * 2) + (TAM_EMAIL * 2) + (TAM_CONTRASENA * 2)
                    + (TAM_CEDULA * 2) + (TAM_DIRECCION * 2) + (TAM_TELEFONO * 2)
                    + 4 + 1 + 4; // 333

    private Roles rolesDAO;

    public Clientes(String nomArchivo, Roles rolesDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.rolesDAO = rolesDAO;
    }

    public Clientes(String nomArchivo) {
        this(nomArchivo, null);
    }

    public Clientes() {
        this("clientes.dat", null);
    }

    public void setRolesDAO(Roles rolesDAO) {
        this.rolesDAO = rolesDAO;
    }

    private Cliente leeCliente() throws IOException {
        int idUsuario          = archivo.readInt();
        String nombre          = leeString(TAM_NOMBRE);
        String email           = leeString(TAM_EMAIL);
        String contrasena      = leeString(TAM_CONTRASENA);
        String cedula          = leeString(TAM_CEDULA);
        String direccion       = leeString(TAM_DIRECCION);
        String telefono        = leeString(TAM_TELEFONO);
        int puntosFidelidad    = archivo.readInt();
        boolean activo         = archivo.readBoolean();
        int idRol              = archivo.readInt();

        Cliente c = new Cliente(idUsuario, nombre, email, contrasena, direccion, telefono);
        c.setCedula(cedula);
        c.setPuntosFidelidad(puntosFidelidad);
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

    private void escribeCliente(Cliente c) throws IOException {
        archivo.writeInt(c.getIdUsuario());
        escribeString(c.getNombre(), TAM_NOMBRE);
        escribeString(c.getEmail(), TAM_EMAIL);
        escribeString(c.getContrasena(), TAM_CONTRASENA);
        escribeString(c.getCedula(), TAM_CEDULA);
        escribeString(c.getDireccion(), TAM_DIRECCION);
        escribeString(c.getTelefono(), TAM_TELEFONO);
        archivo.writeInt(c.getPuntosFidelidad());
        archivo.writeBoolean(c.isActivo());

        Rol rol = c.getRol();
        archivo.writeInt(rol != null ? rol.getIdRol() : -1);
    }

    /**
     * Agrega un cliente nuevo con un id ya asignado por el coordinador
     * {@link Usuarios} (que lo calcula sobre los tres archivos de
     * usuarios a la vez, para que nunca se repita entre
     * Administrador/Cajero/Cliente).
     */
    public void agregar(Cliente c, int idAsignado) throws PersistenciaException {
        if (c == null || c.getEmail() == null || c.getEmail().trim().isEmpty()) {
            throw new PersistenciaException("Cliente invalido.");
        }
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            c.setIdUsuario(idAsignado);
            archivo.seek(archivo.length());
            escribeCliente(c);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de clientes.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el cliente.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Calcula el mayor id existente en este archivo (0 si esta vacio). Uso interno de {@link Usuarios}. */
    public int maxId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodos(), Cliente::getIdUsuario) - 1;
    }

    public Cliente buscar(int idUsuario) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Cliente c = leeCliente();
                    if (c.getIdUsuario() == idUsuario) return c;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Cliente no encontrado: id=" + idUsuario);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Cliente no encontrado: id=" + idUsuario);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el cliente.");
        }
    }

    public Cliente buscarPorEmail(String email) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Cliente c = leeCliente();
                    if (c.getEmail().equalsIgnoreCase(email)) return c;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Cliente no encontrado con email: " + email);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Cliente no encontrado con email: " + email);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el cliente.");
        }
    }

    public void actualizar(Cliente c) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Cliente leido = leeCliente();
                    if (leido.getIdUsuario() == c.getIdUsuario()) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeCliente(c);
                        return;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Cliente no encontrado para actualizar.");
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de clientes no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el cliente.");
        }
    }

    public void inactivar(int idUsuario) throws PersistenciaException {
        Cliente c = buscar(idUsuario);
        c.setActivo(false);
        actualizar(c);
    }

    public ArrayList<Cliente> listarActivos() throws PersistenciaException {
        ArrayList<Cliente> resultado = new ArrayList<>();
        for (Cliente c : obtenerTodos()) {
            if (c.isActivo()) resultado.add(c);
        }
        return resultado;
    }

    public ArrayList<Cliente> obtenerTodos() throws PersistenciaException {
        ArrayList<Cliente> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeCliente());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los clientes.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }
}