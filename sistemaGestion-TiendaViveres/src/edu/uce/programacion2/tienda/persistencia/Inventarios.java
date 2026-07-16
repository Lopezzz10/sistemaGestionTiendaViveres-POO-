package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Inventario;
import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la persistencia de los inventarios en archivo binario
 * de acceso aleatorio. Reemplaza a las antiguas Inventarios.java (lista en
 * memoria) y InventariosArchivo.java (serializacion de la lista completa).
 *
 * El producto asociado se guarda como clave foranea (codigoProducto) y se
 * resuelve al leer consultando el Productos inyectado en el constructor.
 * Esto reemplaza al parametro "catalogo" que antes había que pasarle a
 * cargar() a mano (InventariosArchivo.cargar(catalogo)): ahora Inventarios
 * resuelve el producto por si mismo, igual que Usuarios resuelve su Rol.
 *
 * NOTA IMPORTANTE (limitacion ya existente, no introducida por esta migracion):
 * igual que la version anterior, este archivo NO persiste fechaActualizacion
 * ni historialMovimientos de Inventario -- InventariosArchivo.cargar() ya
 * reconstruia el objeto con new Inventario(id, producto, cantidad), lo que
 * reinicia la fecha a "ahora" y el historial a solo ["INICIO"] en cada carga.
 * Si luego quieren conservar el historial completo entre ejecuciones, la
 * clase Inventario (negocio) necesitaria un setter para historialMovimientos
 * y fechaActualizacion, y aqui se agregaria un archivo aparte de movimientos
 * (mismo patron que DetallesFactura en el proyecto de referencia). Avisame
 * si quieren que lo implemente asi.
 *
 * <pre>
 * estructura del registro (49 bytes):
 * idInventario        int          4 bytes
 * codigoProducto (FK) 20 chars    40 bytes
 * cantidadDisponible  int          4 bytes
 * activo              boolean      1 byte
 * total:                          49 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Inventarios extends AccesoAleatorio {

    private static final int TAM_CODIGO_PRODUCTO = 20;
    private static final int TAM_REGISTRO = 4 + (TAM_CODIGO_PRODUCTO * 2) + 4 + 1; // 49

    // DAO de productos usado para resolver el producto completo de cada
    // registro de inventario al leerlo. Puede ser null si no interesa resolverlo.
    private Productos productosDAO;

    // Constructor que establece el nombre del archivo, el tamano de cada
    // registro y el DAO de productos para resolver la clave foranea.
    public Inventarios(String nomArchivo, Productos productosDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.productosDAO = productosDAO;
    }

    public Inventarios(String nomArchivo) {
        this(nomArchivo, null);
    }

    // Constructor de conveniencia: usa "inventarios.dat" en la raiz del proyecto.
    public Inventarios() {
        this("inventarios.dat", null);
    }

    /** Permite asignar (o reemplazar) el DAO de productos despues de construir el objeto. */
    public void setProductosDAO(Productos productosDAO) {
        this.productosDAO = productosDAO;
    }

    // Lee un inventario del archivo en la posicion actual.
    private Inventario leeInventario() throws IOException {
        int idInventario         = archivo.readInt();
        String codigoProducto    = leeString(TAM_CODIGO_PRODUCTO);
        int cantidadDisponible   = archivo.readInt();
        boolean activo           = archivo.readBoolean();

        Producto producto = resolverProducto(codigoProducto);

        // El constructor reinicia fechaActualizacion a "ahora" y el historial
        // a ["INICIO"], igual que hacia la version anterior al reconstruir
        // el objeto desde el archivo (ver nota de la clase).
        Inventario inv = new Inventario(idInventario, producto, cantidadDisponible);
        inv.setActivo(activo);
        return inv;
    }

    // Consulta el DAO de productos por el codigo guardado. Si no hay DAO,
    // o el codigo esta vacio, o el producto ya no existe, retorna null.
    private Producto resolverProducto(String codigoProducto) {
        if (codigoProducto == null || codigoProducto.trim().isEmpty() || productosDAO == null) {
            return null;
        }
        try {
            return productosDAO.buscar(codigoProducto.trim());
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    // Escribe un inventario en el archivo en la posicion actual.
    private void escribeInventario(Inventario inv) throws IOException {
        archivo.writeInt(inv.getIdInventario());
        String codigoProducto = (inv.getProducto() != null) ? inv.getProducto().getCodigo() : "";
        escribeString(codigoProducto, TAM_CODIGO_PRODUCTO);
        archivo.writeInt(inv.getCantidadDisponible());
        archivo.writeBoolean(inv.isActivo());
    }

    // Agrega un inventario nuevo al final del archivo, asignandole el siguiente id.
    public void agregar(Inventario inv) throws PersistenciaException {
        if (inv == null || inv.getProducto() == null) {
            throw new PersistenciaException("Inventario invalido.");
        }
        String codigoProducto = inv.getProducto().getCodigo();
        if (buscarPorProducto(codigoProducto) != null) {
            throw new PersistenciaException("Ya existe inventario para: " + codigoProducto);
        }
        // Ver comentario equivalente en Usuarios.agregar(): siguienteId() debe
        // calcularse antes de abrir el archivo para escritura, porque abre y
        // cierra su propia RandomAccessFile sobre el campo "archivo" heredado.
        int nuevoId = siguienteId();
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            inv.setIdInventario(nuevoId);
            archivo.seek(archivo.length());
            escribeInventario(inv);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de inventarios.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el inventario.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Calcula el siguiente id auto-incremental (maximo id existente + 1). */
    public int siguienteId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodos(), Inventario::getIdInventario);
    }

    // Busca un inventario por su id. Lanza excepcion si no existe (igual que la version anterior).
    public Inventario buscar(int idInventario) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                Inventario inv = buscarConStream(this::leeInventario, x -> x.getIdInventario() == idInventario);
                if (inv == null) throw new PersistenciaException("Inventario no encontrado: id=" + idInventario);
                return inv;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Inventario no encontrado: id=" + idInventario);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el inventario.");
        }
    }

    /** Busca el inventario por codigo de producto. Retorna null si no existe (igual que antes). */
    public Inventario buscarPorProducto(String codigoProducto) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                return buscarConStream(this::leeInventario, x -> x.getProducto() != null
                        && x.getProducto().getCodigo().equalsIgnoreCase(codigoProducto));
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return null;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar el inventario por producto.");
        }
    }

    // Actualiza un inventario existente buscandolo por id.
    public void actualizar(Inventario inv) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                long indice = indiceConStream(this::leeInventario, x -> x.getIdInventario() == inv.getIdInventario());
                if (indice == -1) {
                    throw new PersistenciaException("Inventario no encontrado para actualizar.");
                }
                archivo.seek(indice * tamRegistro);
                escribeInventario(inv);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de inventarios no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar el inventario.");
        }
    }

    /**
     * Inactiva (borrado logico) un registro de inventario: no se remueve
     * del archivo, solo se marca como inactivo.
     */
    public void inactivar(int idInventario) throws PersistenciaException {
        Inventario inv = buscar(idInventario);
        inv.setActivo(false);
        actualizar(inv);
    }

    /** Retorna solo los inventarios activos (equivalente a WHERE activo = 1). */
    public ArrayList<Inventario> listarActivos() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(Inventario::isActivo)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** Retorna inventarios cuyo stock esta en o por debajo del umbral de alerta. */
    public ArrayList<Inventario> listarConAlerta() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(Inventario::requiereAlerta)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // Devuelve todos los inventarios registrados (activos e inactivos).
    public ArrayList<Inventario> obtenerTodos() throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                return leerTodosConStream(this::leeInventario);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return new ArrayList<>();
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los inventarios.");
        }
    }

    public int conteo() {
        return (int) numRegistros();
    }
}