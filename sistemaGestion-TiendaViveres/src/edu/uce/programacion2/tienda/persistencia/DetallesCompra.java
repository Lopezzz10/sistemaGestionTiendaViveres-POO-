package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.DetalleCompra;
import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;

import java.io.*;
import java.util.ArrayList;

/**
 * Clase que gestiona la persistencia de los detalles de compra en su propio
 * archivo binario de acceso aleatorio. Una compra puede tener VARIOS
 * detalles (varios productos adquiridos), por eso cada registro guarda el
 * idCompra al que pertenece -- exactamente el mismo patron que
 * {@link DetallesVenta} usa con idVenta (y que DetallesFactura usa con
 * idFactura en el proyecto de referencia). Asi el registro de Compra (en
 * la clase {@link Compras}) puede tener tamano fijo aunque la cantidad de
 * items por compra varie.
 *
 * <pre>
 * estructura del registro (76 bytes):
 * idCompra (FK)          int          4 bytes  (agrupa las lineas de una misma compra)
 * idDetalle              int          4 bytes
 * codigoProducto (FK)   20 chars    40 bytes
 * cantidad               int          4 bytes
 * precioCompra           double       8 bytes
 * descuento              double       8 bytes
 * total:                             68 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetallesCompra extends AccesoAleatorio {

    private static final int TAM_CODIGO_PRODUCTO = 20;
    private static final int TAM_REGISTRO = 4 + 4 + (TAM_CODIGO_PRODUCTO * 2) + 4 + 8 + 8; // 68

    // DAO de productos usado para resolver el producto completo de cada
    // detalle al leerlo. Puede ser null si no interesa resolverlo.
    private Productos productosDAO;

    public DetallesCompra(String nomArchivo, Productos productosDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.productosDAO = productosDAO;
    }

    public DetallesCompra(String nomArchivo) {
        this(nomArchivo, null);
    }

    // Constructor de conveniencia: usa "detallesCompra.dat" en la raiz del proyecto.
    public DetallesCompra() {
        this("detallesCompra.dat", null);
    }

    public void setProductosDAO(Productos productosDAO) {
        this.productosDAO = productosDAO;
    }

    /** Envoltorio interno: un registro leido trae su idCompra ademas del detalle. */
    private static class Registro {
        int idCompra;
        DetalleCompra detalle;
    }

    // Lee un registro (idCompra + detalle) del archivo en la posicion actual.
    private Registro leeRegistro() throws IOException {
        int idCompra           = archivo.readInt();
        int idDetalle          = archivo.readInt();
        String codigoProducto  = leeString(TAM_CODIGO_PRODUCTO);
        int cantidad           = archivo.readInt();
        double precioCompra    = archivo.readDouble();
        double descuento       = archivo.readDouble();

        Producto producto = resolverProducto(codigoProducto);

        DetalleCompra detalle = new DetalleCompra(idDetalle, producto, cantidad, precioCompra, descuento);

        Registro r = new Registro();
        r.idCompra = idCompra;
        r.detalle = detalle;
        return r;
    }

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

    // Escribe un registro (idCompra + detalle) en el archivo en la posicion actual.
    private void escribeRegistro(int idCompra, DetalleCompra detalle) throws IOException {
        archivo.writeInt(idCompra);
        archivo.writeInt(detalle.getIdDetalle());
        String codigoProducto = (detalle.getProducto() != null) ? detalle.getProducto().getCodigo() : "";
        escribeString(codigoProducto, TAM_CODIGO_PRODUCTO);
        archivo.writeInt(detalle.getCantidad());
        archivo.writeDouble(detalle.getPrecioCompra());
        archivo.writeDouble(detalle.getDescuento());
    }

    /** Calcula el siguiente id auto-incremental para un detalle (global, no por compra). */
    public int siguienteIdDetalle() throws PersistenciaException {
        ArrayList<DetalleCompra> todos = new ArrayList<>();
        for (Registro r : obtenerTodosLosRegistros()) todos.add(r.detalle);
        return GeneradorId.siguienteId(todos, DetalleCompra::getIdDetalle);
    }

    // Agrega un detalle nuevo al final del archivo, asociado a idCompra.
    public void agregar(int idCompra, DetalleCompra detalle) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            archivo.seek(archivo.length());
            escribeRegistro(idCompra, detalle);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de detalles de compra.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el detalle de compra.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Retorna todos los detalles que pertenecen a una compra especifica. */
    public ArrayList<DetalleCompra> obtenerPorIdCompra(int idCompra) throws PersistenciaException {
        ArrayList<DetalleCompra> resultado = new ArrayList<>();
        for (Registro r : obtenerTodosLosRegistros()) {
            if (r.idCompra == idCompra) resultado.add(r.detalle);
        }
        return resultado;
    }

    // Devuelve todos los registros (idCompra + detalle) del archivo.
    private ArrayList<Registro> obtenerTodosLosRegistros() throws PersistenciaException {
        ArrayList<Registro> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeRegistro());
                }
            } catch (EOFException eof) {
                return lista;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los detalles de compra.");
        }
    }

    /**
     * Elimina fisicamente todos los detalles de una compra (borra y empaca).
     * Se usa antes de volver a insertar los detalles actualizados de esa
     * compra (ver Compras.actualizar()).
     */
    public void eliminarPorIdCompra(int idCompra) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                int numRegistros = (int) (archivo.length() / tamRegistro);
                for (int i = 0; i < numRegistros; i++) {
                    archivo.seek((long) i * tamRegistro);
                    int idLeido = archivo.readInt();
                    if (idLeido == idCompra) {
                        archivo.seek((long) i * tamRegistro);
                        borraRegistro();
                    }
                }
                empaca();
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            // no hay archivo todavia: nada que eliminar
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al eliminar los detalles de la compra.");
        }
    }

    /**
     * Reemplaza todos los detalles de una compra: elimina los existentes y
     * agrega los nuevos. Se usa desde Compras.agregar()/actualizar().
     */
    public void reemplazarDetalles(int idCompra, ArrayList<DetalleCompra> nuevosDetalles)
            throws PersistenciaException {
        eliminarPorIdCompra(idCompra);
        for (DetalleCompra d : nuevosDetalles) {
            agregar(idCompra, d);
        }
    }
}