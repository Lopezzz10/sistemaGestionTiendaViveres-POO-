package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.DetalleFactura;
import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;

import java.io.*;
import java.util.ArrayList;

/**
 * Clase que gestiona la persistencia de las lineas fiscales (detalles) de
 * una factura en su propio archivo binario de acceso aleatorio. Una
 * factura puede tener VARIAS lineas (una por producto facturado), por eso
 * cada registro guarda el idFactura al que pertenece -- el mismo patron
 * que {@link DetallesVenta} usa con idVenta y {@link DetallesCompra} con
 * idCompra (y que DetallesFactura usa con idFactura en el proyecto de
 * referencia, canchas).
 *
 * A diferencia de {@link DetallesVenta}/{@link DetallesCompra}, aqui SI se
 * guardan precioUnitarioSinIva y montoIva calculados en el momento de la
 * emision: son la "fotografia fiscal" de la factura y no deben cambiar
 * aunque despues se actualice el precio o la tarifa de IVA del producto.
 *
 * <pre>
 * estructura del registro (68 bytes):
 * idFactura (FK)           int          4 bytes  (agrupa las lineas de una misma factura)
 * idDetalle                int          4 bytes
 * codigoProducto (FK)     20 chars    40 bytes
 * cantidad                 int          4 bytes
 * precioUnitarioSinIva     double       8 bytes
 * descuento                double       8 bytes
 * total:                               68 bytes
 * </pre>
 *
 * Nota: montoIva y subtotal NO se persisten -- {@link DetalleFactura}
 * los recalcula siempre a partir de precioUnitarioSinIva, cantidad,
 * descuento y la tarifa de IVA vigente del producto (calcularMontos()),
 * igual que Venta/Compra no persisten su total.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetallesFactura extends AccesoAleatorio {

    private static final int TAM_CODIGO_PRODUCTO = 20;
    private static final int TAM_REGISTRO = 4 + 4 + (TAM_CODIGO_PRODUCTO * 2) + 4 + 8 + 8; // 68

    // DAO de productos usado para resolver el producto completo de cada
    // detalle al leerlo. Puede ser null si no interesa resolverlo.
    private Productos productosDAO;

    public DetallesFactura(String nomArchivo, Productos productosDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.productosDAO = productosDAO;
    }

    public DetallesFactura(String nomArchivo) {
        this(nomArchivo, null);
    }

    // Constructor de conveniencia: usa "detallesFactura.dat" en la raiz del proyecto.
    public DetallesFactura() {
        this("detallesFactura.dat", null);
    }

    public void setProductosDAO(Productos productosDAO) {
        this.productosDAO = productosDAO;
    }

    /** Envoltorio interno: un registro leido trae su idFactura ademas del detalle. */
    private static class Registro {
        int idFactura;
        DetalleFactura detalle;
    }

    // Lee un registro (idFactura + detalle) del archivo en la posicion actual.
    private Registro leeRegistro() throws IOException {
        int idFactura              = archivo.readInt();
        int idDetalle              = archivo.readInt();
        String codigoProducto      = leeString(TAM_CODIGO_PRODUCTO);
        int cantidad                = archivo.readInt();
        double precioUnitarioSinIva = archivo.readDouble();
        double descuento            = archivo.readDouble();

        Producto producto = resolverProducto(codigoProducto);

        // Este constructor recalcula montoIva/subtotal automaticamente
        // (calcularMontos()) usando la tarifa de IVA ACTUAL del producto.
        DetalleFactura detalle = new DetalleFactura(idDetalle, producto, cantidad, precioUnitarioSinIva, descuento);

        Registro r = new Registro();
        r.idFactura = idFactura;
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

    // Escribe un registro (idFactura + detalle) en el archivo en la posicion actual.
    private void escribeRegistro(int idFactura, DetalleFactura detalle) throws IOException {
        archivo.writeInt(idFactura);
        archivo.writeInt(detalle.getIdDetalle());
        String codigoProducto = (detalle.getProducto() != null) ? detalle.getProducto().getCodigo() : "";
        escribeString(codigoProducto, TAM_CODIGO_PRODUCTO);
        archivo.writeInt(detalle.getCantidad());
        archivo.writeDouble(detalle.getPrecioUnitarioSinIva());
        archivo.writeDouble(detalle.getDescuento());
    }

    /** Calcula el siguiente id auto-incremental para un detalle (global, no por factura). */
    public int siguienteIdDetalle() throws PersistenciaException {
        ArrayList<DetalleFactura> todos = new ArrayList<>();
        for (Registro r : obtenerTodosLosRegistros()) todos.add(r.detalle);
        return GeneradorId.siguienteId(todos, DetalleFactura::getIdDetalle);
    }

    // Agrega un detalle nuevo al final del archivo, asociado a idFactura.
    public void agregar(int idFactura, DetalleFactura detalle) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            archivo.seek(archivo.length());
            escribeRegistro(idFactura, detalle);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de detalles de factura.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el detalle de factura.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Retorna todos los detalles que pertenecen a una factura especifica. */
    public ArrayList<DetalleFactura> obtenerPorIdFactura(int idFactura) throws PersistenciaException {
        ArrayList<DetalleFactura> resultado = new ArrayList<>();
        for (Registro r : obtenerTodosLosRegistros()) {
            if (r.idFactura == idFactura) resultado.add(r.detalle);
        }
        return resultado;
    }

    // Devuelve todos los registros (idFactura + detalle) del archivo.
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
            throw new PersistenciaException("Error al obtener los detalles de factura.");
        }
    }

    /**
     * Elimina fisicamente todos los detalles de una factura (borra y empaca).
     * Se usa antes de volver a insertar los detalles actualizados de esa
     * factura (ver Facturas.actualizar()).
     */
    public void eliminarPorIdFactura(int idFactura) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                int numRegistros = (int) (archivo.length() / tamRegistro);
                for (int i = 0; i < numRegistros; i++) {
                    archivo.seek((long) i * tamRegistro);
                    int idLeido = archivo.readInt();
                    if (idLeido == idFactura) {
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
            throw new PersistenciaException("Error al eliminar los detalles de la factura.");
        }
    }

    /**
     * Reemplaza todos los detalles de una factura: elimina los existentes y
     * agrega los nuevos, asignando id a los que aun no tengan uno valido
     * (DetalleFactura si tiene setIdDetalle, a diferencia de DetalleVenta/
     * DetalleCompra, por lo que no hace falta reconstruirlos). Se usa desde
     * Facturas.agregar()/actualizar().
     */
    public void reemplazarDetalles(int idFactura, ArrayList<DetalleFactura> nuevosDetalles)
            throws PersistenciaException {
        eliminarPorIdFactura(idFactura);
        int siguienteId = siguienteIdDetalle();
        for (DetalleFactura d : nuevosDetalles) {
            if (d.getIdDetalle() <= 0) {
                d.setIdDetalle(siguienteId++);
            }
            agregar(idFactura, d);
        }
    }
}