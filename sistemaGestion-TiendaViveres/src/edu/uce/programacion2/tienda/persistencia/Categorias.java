package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Categoria;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import java.io.*;
import java.util.ArrayList;

/**
 * Clase que gestiona la persistencia de las categorias en archivo binario
 * de acceso aleatorio. Reemplaza a las antiguas Categorias.java (lista en
 * memoria) y CategoriasArchivo.java (serializacion de la lista completa).
 * Cada registro tiene tamano fijo, lo que permite ubicar y actualizar una
 * categoria puntual con seek() sin reescribir el archivo completo.
 *
 * <pre>
 * estructura del registro (122 bytes):
 * cveCategoria       15 chars    30 bytes
 * nombre             40 chars    80 bytes
 * tipoProducto       char         2 bytes  ('P' = Perecible, 'N' = No Perecible)
 * ivaEspecial        boolean      1 byte
 * tarifaIvaEspecial  double       8 bytes
 * activo             boolean      1 byte   (borrado logico)
 * total:                        122 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Categorias extends AccesoAleatorio {

    private static final int TAM_CVE     = 15;
    private static final int TAM_NOMBRE  = 40;
    private static final int TAM_REGISTRO =
            (TAM_CVE * 2) + (TAM_NOMBRE * 2) + 2 + 1 + 8 + 1; // 122

    // Constructor que establece el nombre del archivo y el tamano de cada registro.
    public Categorias(String nomArchivo) {
        super(nomArchivo, TAM_REGISTRO);
    }

    // Constructor de conveniencia: usa "categorias.dat" en la raiz del proyecto.
    public Categorias() {
        this("categorias.dat");
    }

    // Lee una categoria del archivo en la posicion actual.
    private Categoria leeCategoria() throws IOException {
        String cve               = leeString(TAM_CVE);
        String nombre            = leeString(TAM_NOMBRE);
        char   tipoProducto      = archivo.readChar();
        boolean ivaEspecial      = archivo.readBoolean();
        double tarifaIvaEspecial = archivo.readDouble();
        boolean activo           = archivo.readBoolean();

        Categoria c = new Categoria(cve, nombre, tipoProducto, ivaEspecial, tarifaIvaEspecial);
        c.setActivo(activo);
        return c;
    }

    // Escribe una categoria en el archivo en la posicion actual.
    private void escribeCategoria(Categoria c) throws IOException {
        escribeString(c.getCveCategoria(), TAM_CVE);
        escribeString(c.getNombre(), TAM_NOMBRE);
        archivo.writeChar(c.getTipoProducto());
        archivo.writeBoolean(c.isIvaEspecial());
        archivo.writeDouble(c.getTarifaIvaEspecial());
        archivo.writeBoolean(c.isActivo());
    }

    // Agrega una categoria nueva al final del archivo.
    public void agregar(Categoria c) throws PersistenciaException {
        if (c == null || c.getCveCategoria().trim().isEmpty()) {
            throw new PersistenciaException("Categoria invalida.");
        }
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Categoria leida = leeCategoria();
                    if (leida.getCveCategoria().equalsIgnoreCase(c.getCveCategoria())) {
                        throw new PersistenciaException(
                                "Categoria ya existe: " + c.getCveCategoria());
                    }
                }
            } catch (EOFException eof) {
                // llegamos al final, no hay duplicado
            }
            archivo.seek(archivo.length());
            escribeCategoria(c);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de categorias.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar la categoria.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    // Busca una categoria por su clave. Devuelve null si no existe.
    public Categoria buscar(String cveCategoria) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Categoria c = leeCategoria();
                    if (c.getCveCategoria().equalsIgnoreCase(cveCategoria)) return c;
                }
            } catch (EOFException eof) {
                return null;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return null;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar la categoria.");
        }
    }

    // Actualiza una categoria existente buscandola por clave.
    public void actualizar(Categoria c) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Categoria leida = leeCategoria();
                    if (leida.getCveCategoria().equalsIgnoreCase(c.getCveCategoria())) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeCategoria(c);
                        return;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Categoria no encontrada para actualizar.");
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de categorias no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar la categoria.");
        }
    }

    /**
     * Inactiva (borrado logico) una categoria: no se remueve del archivo,
     * solo se marca como inactiva para preservar el historial y no romper
     * productos que ya la referencian.
     */
    public void inactivar(String cveCategoria) throws PersistenciaException {
        Categoria c = buscar(cveCategoria);
        if (c == null) {
            throw new PersistenciaException("Categoria no encontrada: " + cveCategoria);
        }
        c.setActivo(false);
        actualizar(c);
    }

    // Devuelve todas las categorias registradas (activas e inactivas).
    public ArrayList<Categoria> obtenerTodas() throws PersistenciaException {
        ArrayList<Categoria> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeCategoria());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener las categorias.");
        }
    }

    /** Retorna solo las categorias activas (equivalente a WHERE activo = 1). */
    public ArrayList<Categoria> listarActivas() throws PersistenciaException {
        ArrayList<Categoria> resultado = new ArrayList<>();
        for (Categoria c : obtenerTodas()) {
            if (c.isActivo()) resultado.add(c);
        }
        return resultado;
    }

    public int conteo() {
        return (int) numRegistros();
    }
}