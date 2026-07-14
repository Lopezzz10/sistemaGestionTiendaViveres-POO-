package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import edu.uce.programacion2.tienda.objetosServicio.Permiso;
import java.io.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

/**
 * Clase que gestiona la persistencia de los roles en archivo binario
 * de acceso aleatorio. Reemplaza a las antiguas Roles.java (lista en
 * memoria) y RolesArchivo.java (serializacion de la lista completa).
 * Cada registro guarda el idRol, el nombre del cargo y, para cada
 * permiso posible del catalogo {@link Permiso}, un entero (1 = tiene
 * el permiso, 0 = no lo tiene), igual que se hizo con el archivo de
 * cargos del proyecto de referencia (canchas).
 *
 * <pre>
 * estructura del registro (variable segun cantidad de permisos):
 * idRol           int          4 bytes
 * nombreCargo     30 chars    60 bytes
 * permisos        N ints    N*4 bytes  (N = Permiso.values().length)
 * activo          boolean      1 byte
 * total actual:               109 bytes  (con N = 11)
 * </pre>
 *
 * NOTA DE MIGRACION: si se agrega un nuevo valor al enum {@link Permiso},
 * N cambia y el tamano de registro cambia con el. Un roles.dat generado
 * con una version anterior del enum queda incompatible y debe migrarse
 * o regenerarse (ver Seeder / script de migracion).
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Roles extends AccesoAleatorio {

    private static final int TAM_NOMBRE    = 30;
    private static final Permiso[] TODOS_PERMISOS = Permiso.values();
    private static final int NUM_PERMISOS   = TODOS_PERMISOS.length;
    private static final int TAM_REGISTRO   = 4 + (TAM_NOMBRE * 2) + (NUM_PERMISOS * 4) + 1;

    // Constructor que establece el nombre del archivo y el tamano de cada registro.
    public Roles(String nomArchivo) {
        super(nomArchivo, TAM_REGISTRO);
    }

    // Constructor de conveniencia: usa "roles.dat" en la raiz del proyecto.
    public Roles() {
        this("roles.dat");
    }

    // Lee un rol del archivo en la posicion actual.
    private Rol leeRol() throws IOException {
        int idRol          = archivo.readInt();
        String nombreCargo = leeString(TAM_NOMBRE);

        Set<Permiso> permisos = EnumSet.noneOf(Permiso.class);
        for (int i = 0; i < NUM_PERMISOS; i++) {
            int tiene = archivo.readInt();
            if (tiene == 1) permisos.add(TODOS_PERMISOS[i]);
        }

        boolean activo = archivo.readBoolean();

        Rol r = new Rol(idRol, nombreCargo, permisos);
        r.setActivo(activo);
        return r;
    }

    // Escribe un rol en el archivo en la posicion actual.
    private void escribeRol(Rol r) throws IOException {
        archivo.writeInt(r.getIdRol());
        escribeString(r.getNombreCargo(), TAM_NOMBRE);

        for (Permiso p : TODOS_PERMISOS) {
            archivo.writeInt(r.tienePermiso(p) ? 1 : 0);
        }

        archivo.writeBoolean(r.isActivo());
    }

    // Agrega un rol nuevo al final del archivo, asignandole el siguiente id.
    public void agregar(Rol r) throws PersistenciaException {
        if (r == null || r.getNombreCargo() == null || r.getNombreCargo().trim().isEmpty()) {
            throw new PersistenciaException("Rol invalido.");
        }
        // Ver comentario equivalente en Usuarios.agregar(): siguienteId() debe
        // calcularse antes de abrir el archivo para escritura, porque abre y
        // cierra su propia RandomAccessFile sobre el campo "archivo" heredado.
        int nuevoId = siguienteId();
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Rol leido = leeRol();
                    if (leido.getNombreCargo().equalsIgnoreCase(r.getNombreCargo())) {
                        throw new PersistenciaException(
                                "Ya existe un rol con el cargo: " + r.getNombreCargo());
                    }
                }
            } catch (EOFException eof) {
                // llegamos al final, no hay duplicado
            }
            r.setIdRol(nuevoId);
            archivo.seek(archivo.length());
            escribeRol(r);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de roles.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el rol.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Calcula el siguiente id auto-incremental (maximo id existente + 1). */
    public int siguienteId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodos(), Rol::getIdRol);
    }

    // Busca un rol por su id. Lanza excepcion si no existe (igual que la version anterior).
    public Rol buscar(int idRol) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Rol r = leeRol();
                    if (r.getIdRol() == idRol) return r;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Rol no encontrado. Id: " + idRol);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Rol no encontrado. Id: " + idRol);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el rol.");
        }
    }

    // Busca un rol por su nombre de cargo. Lanza excepcion si no existe.
    public Rol buscarPorNombre(String nombreCargo) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Rol r = leeRol();
                    if (r.getNombreCargo().equalsIgnoreCase(nombreCargo)) return r;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Rol no encontrado: " + nombreCargo);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Rol no encontrado: " + nombreCargo);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el rol.");
        }
    }

    // Actualiza un rol existente buscandolo por id.
    public void actualizar(Rol r) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Rol leido = leeRol();
                    if (leido.getIdRol() == r.getIdRol()) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeRol(r);
                        return;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Rol no encontrado para actualizar.");
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de roles no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el rol.");
        }
    }

    /** Inactiva (borrado logico) un rol: se conserva el registro para historial. */
    public void inactivar(int idRol) throws PersistenciaException {
        Rol r = buscar(idRol);
        r.setActivo(false);
        actualizar(r);
    }

    /** Retorna solo los roles activos. */
    public ArrayList<Rol> listarActivos() throws PersistenciaException {
        ArrayList<Rol> resultado = new ArrayList<>();
        for (Rol r : obtenerTodos()) {
            if (r.isActivo()) resultado.add(r);
        }
        return resultado;
    }

    // Devuelve todos los roles registrados (activos e inactivos).
    public ArrayList<Rol> obtenerTodos() throws PersistenciaException {
        ArrayList<Rol> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeRol());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los roles.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }
}