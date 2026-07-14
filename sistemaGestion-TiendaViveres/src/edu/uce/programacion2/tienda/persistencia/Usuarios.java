package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Administrador;
import edu.uce.programacion2.tienda.negocio.Cajero;
import edu.uce.programacion2.tienda.negocio.Cliente;
import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.negocio.Usuario;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.io.*;
import java.util.ArrayList;

/**
 * Clase que gestiona la persistencia de los usuarios (Administradores y
 * Cajeros) en archivo binario de acceso aleatorio. Reemplaza a las antiguas
 * Usuarios.java (lista en memoria) y UsuariosArchivo.java (serializacion de
 * la lista completa + resolucion de roles en dos pasadas via cargarConRoles).
 *
 * Usuario es una jerarquia (Administrador / Cajero), por lo que el registro
 * guarda un discriminador de tipo y un "campo especifico" generico que
 * significa cosas distintas segun el tipo (turno para Administrador,
 * cajaAsignada para Cajero) -- mismo enfoque que ya tenia UsuariosArchivo,
 * solo que ahora con ancho fijo.
 *
 * El rol asignado (puede no tener) se guarda como clave foranea (idRol,
 * -1 si no tiene) y se resuelve en el momento de la lectura consultando
 * al Roles inyectado en el constructor -- ya no hace falta el segundo
 * pasado de archivo que hacia cargarConRoles().
 *
 * <pre>
 * estructura del registro (325 bytes):
 * tipo               char         2 bytes  ('A' = Administrador, 'C' = Cajero, 'L' = Cliente)
 * idUsuario          int          4 bytes
 * nombre             40 chars    80 bytes
 * email              40 chars    80 bytes
 * contrasena         20 chars    40 bytes
 * campoEspecifico    10 chars    20 bytes  (turno, cajaAsignada o cedula como texto)
 * activo             boolean      1 byte
 * idRol              int          4 bytes  (-1 = sin rol asignado)
 * direccion          30 chars    60 bytes  (solo Cliente, vacio en los demas)
 * telefono           15 chars    30 bytes  (solo Cliente, vacio en los demas)
 * puntosFidelidad    int          4 bytes  (solo Cliente, 0 en los demas)
 * total:                         325 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Usuarios extends AccesoAleatorio {

    private static final int TAM_NOMBRE          = 40;
    private static final int TAM_EMAIL           = 40;
    private static final int TAM_CONTRASENA      = 20;
    private static final int TAM_CAMPO_ESPECIFICO= 10;
    private static final int TAM_DIRECCION       = 30;
    private static final int TAM_TELEFONO        = 15;
    private static final int TAM_REGISTRO =
            2 + 4 + (TAM_NOMBRE * 2) + (TAM_EMAIL * 2)
                    + (TAM_CONTRASENA * 2) + (TAM_CAMPO_ESPECIFICO * 2) + 1 + 4
                    + (TAM_DIRECCION * 2) + (TAM_TELEFONO * 2) + 4; // 345

    private static final char TIPO_ADMINISTRADOR = 'A';
    private static final char TIPO_CAJERO        = 'C';
    private static final char TIPO_CLIENTE       = 'L';

    // DAO de roles usado para resolver el rol asignado a cada usuario al leerlo.
    // Puede ser null si no interesa resolver el rol (ej. en migraciones simples).
    private Roles rolesDAO;

    // Constructor que establece el nombre del archivo, el tamano de cada
    // registro y el DAO de roles para resolver la clave foranea.
    public Usuarios(String nomArchivo, Roles rolesDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.rolesDAO = rolesDAO;
    }

    public Usuarios(String nomArchivo) {
        this(nomArchivo, null);
    }

    // Constructor de conveniencia: usa "usuarios.dat" en la raiz del proyecto.
    public Usuarios() {
        this("usuarios.dat", null);
    }

    /** Permite asignar (o reemplazar) el DAO de roles despues de construir el objeto. */
    public void setRolesDAO(Roles rolesDAO) {
        this.rolesDAO = rolesDAO;
    }

    // Lee un usuario del archivo en la posicion actual, reconstruyendo
    // la subclase concreta (Administrador o Cajero) segun el tipo.
    private Usuario leeUsuario() throws IOException {
        char   tipo             = archivo.readChar();
        int    idUsuario        = archivo.readInt();
        String nombre           = leeString(TAM_NOMBRE);
        String email            = leeString(TAM_EMAIL);
        String contrasena       = leeString(TAM_CONTRASENA);
        String campoEspecifico  = leeString(TAM_CAMPO_ESPECIFICO);
        boolean activo          = archivo.readBoolean();
        int    idRol            = archivo.readInt();
        String direccion        = leeString(TAM_DIRECCION);
        String telefono         = leeString(TAM_TELEFONO);
        int    puntosFidelidad  = archivo.readInt();

        Usuario u;
        if (tipo == TIPO_ADMINISTRADOR) {
            u = new Administrador(idUsuario, nombre, email, contrasena, campoEspecifico.trim());
        } else if (tipo == TIPO_CLIENTE) {
            Cliente c = new Cliente(idUsuario, nombre, email, contrasena,
                    direccion.trim(), telefono.trim());
            c.setCedula(campoEspecifico.trim());
            c.setPuntosFidelidad(puntosFidelidad);
            u = c;
        } else {
            int cajaAsignada;
            try {
                cajaAsignada = Integer.parseInt(campoEspecifico.trim());
            } catch (NumberFormatException e) {
                cajaAsignada = 1; // valor por defecto si el dato quedo corrupto
            }
            u = new Cajero(idUsuario, nombre, email, contrasena, cajaAsignada);
        }
        u.setActivo(activo);

        if (idRol != -1 && rolesDAO != null) {
            try {
                u.setRol(rolesDAO.buscar(idRol));
            } catch (PersistenciaException pe) {
                // el rol pudo haber sido eliminado; el usuario queda sin rol dinamico
                u.setRol(null);
            }
        }
        return u;
    }

    // Escribe un usuario en el archivo en la posicion actual.
    private void escribeUsuario(Usuario u) throws IOException {
        char   tipo;
        String campoEspecifico;
        String direccion       = "";
        String telefono        = "";
        int    puntosFidelidad = 0;

        if (u instanceof Administrador) {
            tipo = TIPO_ADMINISTRADOR;
            campoEspecifico = ((Administrador) u).getTurno();
        } else if (u instanceof Cliente) {
            Cliente c = (Cliente) u;
            tipo = TIPO_CLIENTE;
            campoEspecifico = c.getCedula();
            direccion       = c.getDireccion();
            telefono        = c.getTelefono();
            puntosFidelidad = c.getPuntosFidelidad();
        } else if (u instanceof Cajero) {
            tipo = TIPO_CAJERO;
            campoEspecifico = String.valueOf(((Cajero) u).getCajaAsignada());
        } else {
            throw new IOException("Tipo de usuario desconocido: " + u.getClass().getSimpleName());
        }

        archivo.writeChar(tipo);
        archivo.writeInt(u.getIdUsuario());
        escribeString(u.getNombre(), TAM_NOMBRE);
        escribeString(u.getEmail(), TAM_EMAIL);
        escribeString(u.getContrasena(), TAM_CONTRASENA);
        escribeString(campoEspecifico, TAM_CAMPO_ESPECIFICO);
        archivo.writeBoolean(u.isActivo());

        Rol rol = u.getRol();
        archivo.writeInt(rol != null ? rol.getIdRol() : -1);

        escribeString(direccion, TAM_DIRECCION);
        escribeString(telefono, TAM_TELEFONO);
        archivo.writeInt(puntosFidelidad);
    }

    // Agrega un usuario nuevo al final del archivo, asignandole el siguiente id.
    public void agregar(Usuario u) throws PersistenciaException {
        if (u == null || u.getEmail() == null || u.getEmail().trim().isEmpty()) {
            throw new PersistenciaException("Usuario invalido.");
        }
        // Se calcula ANTES de abrir el archivo para escritura: siguienteId()
        // (via obtenerTodos()) abre y cierra su propia RandomAccessFile sobre
        // el mismo campo "archivo" heredado de AccesoAleatorio. Si se llamara
        // aqui abajo, mientras el archivo de escritura ya esta abierto, esa
        // llamada anidada lo dejaria cerrado y el archivo.seek() posterior
        // fallaria con IOException, abortando el registro silenciosamente.
        int nuevoId = siguienteId();
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Usuario leido = leeUsuario();
                    if (leido.getEmail().equalsIgnoreCase(u.getEmail())) {
                        throw new PersistenciaException(
                                "Usuario ya existe con email: " + u.getEmail());
                    }
                }
            } catch (EOFException eof) {
                // llegamos al final, no hay duplicado
            }
            u.setIdUsuario(nuevoId);
            archivo.seek(archivo.length());
            escribeUsuario(u);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de usuarios.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el usuario.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Calcula el siguiente id auto-incremental (maximo id existente + 1). */
    public int siguienteId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodos(), Usuario::getIdUsuario);
    }

    // Busca un usuario por su id. Lanza excepcion si no existe (igual que la version anterior).
    public Usuario buscar(int idUsuario) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Usuario u = leeUsuario();
                    if (u.getIdUsuario() == idUsuario) return u;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Usuario no encontrado: id=" + idUsuario);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Usuario no encontrado: id=" + idUsuario);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el usuario.");
        }
    }

    // Busca un usuario por su email. Lanza excepcion si no existe.
    public Usuario buscarPorEmail(String email) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Usuario u = leeUsuario();
                    if (u.getEmail().equalsIgnoreCase(email)) return u;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Usuario no encontrado con email: " + email);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Usuario no encontrado con email: " + email);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el usuario.");
        }
    }

    // Actualiza un usuario existente buscandolo por id.
    public void actualizar(Usuario u) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Usuario leido = leeUsuario();
                    if (leido.getIdUsuario() == u.getIdUsuario()) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeUsuario(u);
                        return;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Usuario no encontrado para actualizar.");
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de usuarios no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el usuario.");
        }
    }

    /**
     * Inactiva (borrado logico) un usuario: no se remueve del archivo,
     * solo se marca como inactivo (por ejemplo, para revocar su acceso
     * sin perder el historial de sus ventas/acciones).
     */
    public void inactivar(int idUsuario) throws PersistenciaException {
        Usuario u = buscar(idUsuario);
        u.setActivo(false);
        actualizar(u);
    }

    /** Retorna solo los usuarios activos (equivalente a WHERE activo = 1). */
    public ArrayList<Usuario> listarActivos() throws PersistenciaException {
        ArrayList<Usuario> resultado = new ArrayList<>();
        for (Usuario u : obtenerTodos()) {
            if (u.isActivo()) resultado.add(u);
        }
        return resultado;
    }

    /** Filtra usuarios por permiso: "ADMINISTRADOR", "CAJERO" o el nombre de un rol dinamico. */
    public ArrayList<Usuario> listarPorPermiso(String permiso) throws PersistenciaException {
        ArrayList<Usuario> resultado = new ArrayList<>();
        for (Usuario u : obtenerTodos()) {
            if (u.getPermiso().equalsIgnoreCase(permiso)) resultado.add(u);
        }
        return resultado;
    }

    // Devuelve todos los usuarios registrados (activos e inactivos).
    public ArrayList<Usuario> obtenerTodos() throws PersistenciaException {
        ArrayList<Usuario> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeUsuario());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los usuarios.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }
}