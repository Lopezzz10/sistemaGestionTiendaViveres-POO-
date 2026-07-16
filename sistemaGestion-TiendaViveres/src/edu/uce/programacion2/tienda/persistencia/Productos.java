package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Categoria;
import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la persistencia de los productos en archivo binario
 * de acceso aleatorio. Reemplaza a las antiguas Productos.java (lista en
 * memoria) y ProductosArchivo.java (serializacion de la lista completa).
 *
 * La categoria del producto se guarda como clave foranea (cveCategoria,
 * vacia si no tiene) y se resuelve al leer consultando el Categorias
 * inyectado en el constructor. Esto corrige, de paso, un detalle de la
 * version anterior: ProductosArchivo reconstruia una Categoria "a medias"
 * (solo cve, nombre y tipoProducto), perdiendo el IVA especial y el
 * estado activo/inactivo de la categoria real. Con la clave foranea se
 * recupera siempre la Categoria completa y actualizada.
 *
 * El metodo buscarPor(Predicate) se mantiene: la fachada sigue filtrando
 * con programacion funcional, solo que ahora los datos crudos vienen de
 * obtenerTodos() (lectura del archivo) en vez de una lista siempre en
 * memoria.
 *
 * <pre>
 * estructura del registro (294 bytes):
 * codigo                     20 chars    40 bytes
 * nombre                     50 chars   100 bytes
 * cveCategoria (FK)          15 chars    30 bytes  (vacio = sin categoria)
 * precioUnitario             double       8 bytes
 * tipo                       char         2 bytes  ('P' = Perecible, 'N' = No Perecible)
 * fechaVencimiento           long         8 bytes  (millis desde 1970; 0 = no aplica)
 * temperaturaAlmacenamiento  double       8 bytes  (0.0 si no aplica)
 * pesoKg                     double       8 bytes  (0.0 si no aplica)
 * marca                      30 chars    60 bytes  (aplica a todos los productos)
 * estado                     15 chars    30 bytes  ("Disponible", "Inactivo", etc.)
 * total:                                294 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Productos extends AccesoAleatorio {

    private static final int TAM_CODIGO  = 20;
    private static final int TAM_NOMBRE  = 50;
    private static final int TAM_CVE_CAT = 15;
    private static final int TAM_MARCA   = 30;
    private static final int TAM_ESTADO  = 15;
    private static final int TAM_REGISTRO =
            (TAM_CODIGO * 2) + (TAM_NOMBRE * 2) + (TAM_CVE_CAT * 2)
                    + 8 + 2 + 8 + 8 + 8 + (TAM_MARCA * 2) + (TAM_ESTADO * 2); // 294

    private static final char TIPO_PERECIBLE    = 'P';
    private static final char TIPO_NO_PERECIBLE = 'N';

    // DAO de categorias usado para resolver la categoria completa de cada
    // producto al leerlo. Puede ser null si no interesa resolverla.
    private Categorias categoriasDAO;

    // Constructor que establece el nombre del archivo, el tamano de cada
    // registro y el DAO de categorias para resolver la clave foranea.
    public Productos(String nomArchivo, Categorias categoriasDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.categoriasDAO = categoriasDAO;
    }

    public Productos(String nomArchivo) {
        this(nomArchivo, null);
    }

    // Constructor de conveniencia: usa "productos.dat" en la raiz del proyecto.
    public Productos() {
        this("productos.dat", null);
    }

    /** Permite asignar (o reemplazar) el DAO de categorias despues de construir el objeto. */
    public void setCategoriasDAO(Categorias categoriasDAO) {
        this.categoriasDAO = categoriasDAO;
    }

    // Lee un producto del archivo en la posicion actual.
    private Producto leeProducto() throws IOException {
        String codigo       = leeString(TAM_CODIGO);
        String nombre       = leeString(TAM_NOMBRE);
        String cveCategoria = leeString(TAM_CVE_CAT);
        double precio       = archivo.readDouble();
        char   tipo         = archivo.readChar();
        long   fechaMillis  = archivo.readLong();
        double temperatura  = archivo.readDouble();
        double pesoKg       = archivo.readDouble();
        String marca        = leeString(TAM_MARCA);
        String estado       = leeString(TAM_ESTADO);

        Categoria categoria = resolverCategoria(cveCategoria);

        Producto p;
        if (tipo == TIPO_PERECIBLE) {
            Date fecha = (fechaMillis > 0) ? new Date(fechaMillis) : null;
            p = new Producto(codigo, nombre, categoria, precio, fecha, temperatura);
            p.setMarca(marca);
        } else {
            p = new Producto(codigo, nombre, categoria, precio, pesoKg, marca);
        }
        p.setEstado(estado);
        return p;
    }

    // Consulta el DAO de categorias por la clave guardada. Si no hay DAO,
    // o la clave esta vacia, o la categoria ya no existe, retorna null
    // (el producto queda sin categoria, igual que antes con categoria == null).
    private Categoria resolverCategoria(String cveCategoria) {
        if (cveCategoria == null || cveCategoria.trim().isEmpty() || categoriasDAO == null) {
            return null;
        }
        try {
            return categoriasDAO.buscar(cveCategoria.trim());
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    // Escribe un producto en el archivo en la posicion actual.
    private void escribeProducto(Producto p) throws IOException {
        escribeString(p.getCodigo(), TAM_CODIGO);
        escribeString(p.getNombre(), TAM_NOMBRE);
        escribeString(p.getCategoria() != null ? p.getCategoria().getCveCategoria() : "", TAM_CVE_CAT);
        archivo.writeDouble(p.getPrecioUnitario());

        boolean esPerecible = "Perecible".equals(p.getTipo());
        archivo.writeChar(esPerecible ? TIPO_PERECIBLE : TIPO_NO_PERECIBLE);

        archivo.writeLong(p.getFechaVencimiento() != null ? p.getFechaVencimiento().getTime() : 0L);
        archivo.writeDouble(p.getTemperaturaAlmacenamiento());
        archivo.writeDouble(p.getPesoKg());
        escribeString(p.getMarca() != null ? p.getMarca() : "", TAM_MARCA);
        escribeString(p.getEstado(), TAM_ESTADO);
    }

    // Agrega un producto nuevo al final del archivo.
    public void agregar(Producto p) throws PersistenciaException {
        if (p == null || p.getCodigo() == null || p.getCodigo().trim().isEmpty()) {
            throw new PersistenciaException("Producto invalido.");
        }
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Producto leido = leeProducto();
                    if (leido.getCodigo().equalsIgnoreCase(p.getCodigo())) {
                        throw new PersistenciaException("Producto ya existe: " + p.getCodigo());
                    }
                }
            } catch (EOFException eof) {
                // llegamos al final, no hay duplicado
            }
            archivo.seek(archivo.length());
            escribeProducto(p);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de productos.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el producto.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    // Busca un producto por su codigo. Lanza excepcion si no existe (igual que la version anterior).
    public Producto buscar(String codigo) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Producto p = leeProducto();
                    if (p.getCodigo().equalsIgnoreCase(codigo)) return p;
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Producto no encontrado: " + codigo);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Producto no encontrado: " + codigo);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el producto.");
        }
    }

    // Actualiza un producto existente buscandolo por codigo.
    public void actualizar(Producto p) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Producto leido = leeProducto();
                    if (leido.getCodigo().equalsIgnoreCase(p.getCodigo())) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeProducto(p);
                        return;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Producto no encontrado para actualizar.");
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de productos no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el producto.");
        }
    }

    /**
     * Inactiva (borrado logico) un producto: no se remueve del archivo,
     * solo se marca su estado como {@link Producto#ESTADO_INACTIVO}.
     */
    public void inactivar(String codigo) throws PersistenciaException {
        Producto p = buscar(codigo);
        p.setEstado(Producto.ESTADO_INACTIVO);
        actualizar(p);
    }

    /** Retorna solo los productos activos (equivalente a WHERE activo = 1). */
    public ArrayList<Producto> listarActivos() throws PersistenciaException {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : obtenerTodos()) {
            if (p.isActivo()) resultado.add(p);
        }
        return resultado;
    }

    // Devuelve todos los productos registrados (activos e inactivos).
    public ArrayList<Producto> obtenerTodos() throws PersistenciaException {
        ArrayList<Producto> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeProducto());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los productos.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }

    /**
     * Consulta generica con programacion funcional: retorna todos los
     * productos que cumplan la condicion recibida como {@link Predicate}.
     * Se mantiene igual que en la version anterior; solo cambia de donde
     * vienen los datos crudos (ahora del archivo via obtenerTodos()).
     *
     * @param criterio funcion que decide si un producto pertenece al resultado
     * @return productos que cumplen el criterio (lista nueva, no afecta el archivo)
     */
    public ArrayList<Producto> buscarPor(Predicate<Producto> criterio) throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(criterio)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}