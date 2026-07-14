package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Proveedor;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.io.*;
import java.util.ArrayList;

/**
 * Clase que gestiona la persistencia de los proveedores en archivo binario
 * de acceso aleatorio. Reemplaza a las antiguas Proveedores.java (lista en
 * memoria) y ProveedoresArchivo.java (serializacion de la lista completa).
 * La unicidad se valida por RUC (identificador fiscal), igual que antes.
 *
 * <pre>
 * estructura del registro (345 bytes):
 * idProveedor     int          4 bytes
 * nombre          40 chars    80 bytes
 * ruc             15 chars    30 bytes
 * telefono        15 chars    30 bytes
 * email           40 chars    80 bytes
 * direccion       60 chars   120 bytes
 * activo          boolean      1 byte
 * total:                     345 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Proveedores extends AccesoAleatorio {

    private static final int TAM_NOMBRE    = 40;
    private static final int TAM_RUC       = 15;
    private static final int TAM_TELEFONO  = 15;
    private static final int TAM_EMAIL     = 40;
    private static final int TAM_DIRECCION = 60;
    private static final int TAM_REGISTRO =
            4 + (TAM_NOMBRE * 2) + (TAM_RUC * 2) + (TAM_TELEFONO * 2)
                    + (TAM_EMAIL * 2) + (TAM_DIRECCION * 2) + 1; // 345

    // Constructor que establece el nombre del archivo y el tamano de cada registro.
    public Proveedores(String nomArchivo) {
        super(nomArchivo, TAM_REGISTRO);
    }

    // Constructor de conveniencia: usa "proveedores.dat" en la raiz del proyecto.
    public Proveedores() {
        this("proveedores.dat");
    }

    // Lee un proveedor del archivo en la posicion actual.
    private Proveedor leeProveedor() throws IOException {
        int idProveedor    = archivo.readInt();
        String nombre      = leeString(TAM_NOMBRE);
        String ruc         = leeString(TAM_RUC);
        String telefono    = leeString(TAM_TELEFONO);
        String email       = leeString(TAM_EMAIL);
        String direccion   = leeString(TAM_DIRECCION);
        boolean activo     = archivo.readBoolean();

        Proveedor p = new Proveedor(idProveedor, nombre, ruc, telefono, email, direccion);
        p.setActivo(activo);
        return p;
    }

    // Escribe un proveedor en el archivo en la posicion actual.
    private void escribeProveedor(Proveedor p) throws IOException {
        archivo.writeInt(p.getIdProveedor());
        escribeString(p.getNombre(), TAM_NOMBRE);
        escribeString(p.getRuc(), TAM_RUC);
        escribeString(p.getTelefono(), TAM_TELEFONO);
        escribeString(p.getEmail(), TAM_EMAIL);
        escribeString(p.getDireccion(), TAM_DIRECCION);
        archivo.writeBoolean(p.isActivo());
    }

    // Agrega un proveedor nuevo al final del archivo, asignandole el siguiente id.
    public void agregar(Proveedor p) throws PersistenciaException {
        if (p == null || p.getRuc() == null || p.getRuc().trim().isEmpty()) {
            throw new PersistenciaException("Proveedor invalido.");
        }
        // Ver comentario equivalente en Usuarios.agregar(): siguienteId() debe
        // calcularse antes de abrir el archivo para escritura, porque abre y
        // cierra su propia RandomAccessFile sobre el campo "archivo" heredado.
        int nuevoId = siguienteId();
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Proveedor leido = leeProveedor();
                    if (leido.getRuc().equalsIgnoreCase(p.getRuc())) {
                        throw new PersistenciaException(
                                "Proveedor ya existe con RUC: " + p.getRuc());
                    }
                }
            } catch (EOFException eof) {
                // llegamos al final, no hay duplicado
            }
            p.setIdProveedor(nuevoId);
            archivo.seek(archivo.length());
            escribeProveedor(p);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de proveedores.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el proveedor.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Calcula el siguiente id auto-incremental (maximo id existente + 1). */
    public int siguienteId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodos(), Proveedor::getIdProveedor);
    }

    // Busca un proveedor por su id. Lanza excepcion si no existe (igual que la version anterior).
    public Proveedor buscar(int idProveedor) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Proveedor p = leeProveedor();
                    if (p.getIdProveedor() == idProveedor) return p;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Proveedor no encontrado: id=" + idProveedor);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Proveedor no encontrado: id=" + idProveedor);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el proveedor.");
        }
    }

    // Busca un proveedor por su RUC. Lanza excepcion si no existe.
    public Proveedor buscarPorRuc(String ruc) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Proveedor p = leeProveedor();
                    if (p.getRuc().equalsIgnoreCase(ruc)) return p;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Proveedor no encontrado con RUC: " + ruc);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Proveedor no encontrado con RUC: " + ruc);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el proveedor.");
        }
    }

    // Actualiza un proveedor existente buscandolo por id.
    public void actualizar(Proveedor p) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Proveedor leido = leeProveedor();
                    if (leido.getIdProveedor() == p.getIdProveedor()) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeProveedor(p);
                        return;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Proveedor no encontrado para actualizar.");
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de proveedores no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el proveedor.");
        }
    }

    /**
     * Inactiva (borrado logico) un proveedor: no se remueve del archivo,
     * solo se marca como inactivo para conservar el historial de compras
     * asociadas a el.
     */
    public void inactivar(int idProveedor) throws PersistenciaException {
        Proveedor p = buscar(idProveedor);
        p.setActivo(false);
        actualizar(p);
    }

    /** Retorna solo los proveedores activos (equivalente a WHERE activo = 1). */
    public ArrayList<Proveedor> listarActivos() throws PersistenciaException {
        ArrayList<Proveedor> resultado = new ArrayList<>();
        for (Proveedor p : obtenerTodos()) {
            if (p.isActivo()) resultado.add(p);
        }
        return resultado;
    }

    // Devuelve todos los proveedores registrados (activos e inactivos).
    public ArrayList<Proveedor> obtenerTodos() throws PersistenciaException {
        ArrayList<Proveedor> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeProveedor());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los proveedores.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }
}