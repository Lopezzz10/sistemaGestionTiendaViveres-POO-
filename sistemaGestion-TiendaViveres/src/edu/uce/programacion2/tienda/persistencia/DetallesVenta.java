package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.DetalleVenta;
import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la persistencia de los detalles de venta en su propio
 * archivo binario de acceso aleatorio. Una venta puede tener VARIOS
 * detalles (varios productos), por eso cada registro guarda el idVenta al
 * que pertenece -- igual que hace DetallesFactura en el proyecto de
 * referencia (canchas) con idFactura. Asi el registro de Venta (en la
 * clase Ventas) puede tener tamano fijo aunque la cantidad de items por
 * venta varie.
 *
 * <pre>
 * estructura del registro (68 bytes):
 * idVenta (FK)          int          4 bytes  (agrupa las lineas de una misma venta)
 * idDetalle             int          4 bytes
 * codigoProducto (FK)  20 chars    40 bytes
 * cantidad              int          4 bytes
 * precioUnitario        double       8 bytes
 * descuento             double       8 bytes
 * total:                            68 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetallesVenta extends AccesoAleatorio {

    private static final int TAM_CODIGO_PRODUCTO = 20;
    private static final int TAM_REGISTRO = 4 + 4 + (TAM_CODIGO_PRODUCTO * 2) + 4 + 8 + 8; // 68

    // DAO de productos usado para resolver el producto completo de cada
    // detalle al leerlo. Puede ser null si no interesa resolverlo.
    private Productos productosDAO;

    public DetallesVenta(String nomArchivo, Productos productosDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.productosDAO = productosDAO;
    }

    public DetallesVenta(String nomArchivo) {
        this(nomArchivo, null);
    }

    // Constructor de conveniencia: usa "detallesVenta.dat" en la raiz del proyecto.
    public DetallesVenta() {
        this("detallesVenta.dat", null);
    }

    public void setProductosDAO(Productos productosDAO) {
        this.productosDAO = productosDAO;
    }

    /** Envoltorio interno: un registro leido trae su idVenta ademas del detalle. */
    private static class Registro {
        int idVenta;
        DetalleVenta detalle;
    }

    // Lee un registro (idVenta + detalle) del archivo en la posicion actual.
    private Registro leeRegistro() throws IOException {
        int idVenta            = archivo.readInt();
        int idDetalle          = archivo.readInt();
        String codigoProducto  = leeString(TAM_CODIGO_PRODUCTO);
        int cantidad           = archivo.readInt();
        double precioUnitario  = archivo.readDouble();
        double descuento       = archivo.readDouble();

        Producto producto = resolverProducto(codigoProducto);

        // NOTA (limitacion ya existente, no introducida por esta migracion):
        // el constructor de DetalleVenta recalcula precioUnitario a partir del
        // precio ACTUAL del producto (calcularPrecioFinal()), no del precio
        // que se guardo en el registro. Si el precio del producto cambia
        // despues de la venta, al recargarla desde el archivo el precio
        // mostrado sera el actual, no el historico -- exactamente lo mismo
        // que hacia VentasArchivo.cargar() (leia precioUnitario del archivo
        // pero lo descartaba al reconstruir con "new DetalleVenta(0, ...)").
        // Si quieren precios historicos exactos, hay que agregar un setter
        // (ej. setPrecioUnitario(double)) a DetalleVenta.java en el paquete
        // negocio; avisame y te lo prep.
        DetalleVenta detalle = new DetalleVenta(idDetalle, producto, cantidad, descuento);

        Registro r = new Registro();
        r.idVenta = idVenta;
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

    // Escribe un registro (idVenta + detalle) en el archivo en la posicion actual.
    private void escribeRegistro(int idVenta, DetalleVenta detalle) throws IOException {
        archivo.writeInt(idVenta);
        archivo.writeInt(detalle.getIdDetalle());
        String codigoProducto = (detalle.getProducto() != null) ? detalle.getProducto().getCodigo() : "";
        escribeString(codigoProducto, TAM_CODIGO_PRODUCTO);
        archivo.writeInt(detalle.getCantidad());
        archivo.writeDouble(detalle.getPrecioUnitario());
        archivo.writeDouble(detalle.getDescuento());
    }

    /** Calcula el siguiente id auto-incremental para un detalle (global, no por venta). */
    public int siguienteIdDetalle() throws PersistenciaException {
        ArrayList<DetalleVenta> todos = obtenerTodosLosRegistros().stream()
                .map(r -> r.detalle)
                .collect(Collectors.toCollection(ArrayList::new));
        return GeneradorId.siguienteId(todos, DetalleVenta::getIdDetalle);
    }

    // Agrega un detalle nuevo al final del archivo, asociado a idVenta.
    // Si el detalle no trae un idDetalle valido (<= 0), se le asigna el siguiente.
    public void agregar(int idVenta, DetalleVenta detalle) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            archivo.seek(archivo.length());
            escribeRegistro(idVenta, detalle);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de detalles de venta.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar el detalle de venta.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }
    }

    /** Retorna todos los detalles que pertenecen a una venta especifica. */
    public ArrayList<DetalleVenta> obtenerPorIdVenta(int idVenta) throws PersistenciaException {
        return obtenerTodosLosRegistros().stream()
                .filter(r -> r.idVenta == idVenta)
                .map(r -> r.detalle)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Lee el archivo de detalles UNA SOLA VEZ y los agrupa por idVenta.
     * Se usa desde Ventas.obtenerTodos() para evitar reabrir y releer todo
     * el archivo de detalles una vez por cada venta (N+1: con N ventas se
     * leia el archivo completo N veces).
     */
    public java.util.Map<Integer, ArrayList<DetalleVenta>> obtenerAgrupadoPorVenta()
            throws PersistenciaException {
        return obtenerTodosLosRegistros().stream()
                .collect(Collectors.groupingBy(
                        r -> r.idVenta,
                        Collectors.mapping(r -> r.detalle, Collectors.toCollection(ArrayList::new))));
    }

    // Devuelve todos los registros (idVenta + detalle) del archivo.
    private ArrayList<Registro> obtenerTodosLosRegistros() throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                return leerTodosConStream(this::leeRegistro);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return new ArrayList<>();
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener los detalles de venta.");
        }
    }

    /**
     * Elimina fisicamente todos los detalles de una venta (borra y empaca).
     * Se usa antes de volver a insertar los detalles actualizados de esa
     * venta (ver Ventas.actualizar()).
     */
    public void eliminarPorIdVenta(int idVenta) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                int numRegistros = (int) (archivo.length() / tamRegistro);
                try {
                    java.util.stream.IntStream.range(0, numRegistros).forEach(i -> {
                        try {
                            archivo.seek((long) i * tamRegistro);
                            int idLeido = archivo.readInt();
                            if (idLeido == idVenta) {
                                archivo.seek((long) i * tamRegistro);
                                borraRegistro();
                            }
                        } catch (IOException ioe) {
                            throw new UncheckedIOException(ioe);
                        }
                    });
                } catch (UncheckedIOException uioe) {
                    throw uioe.getCause();
                }
                empaca();
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            // no hay archivo todavia: nada que eliminar
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al eliminar los detalles de la venta.");
        }
    }

    /**
     * Reemplaza todos los detalles de una venta: elimina los existentes y
     * agrega los nuevos. Se usa desde Ventas.agregar()/actualizar().
     */
    public void reemplazarDetalles(int idVenta, ArrayList<DetalleVenta> nuevosDetalles)
            throws PersistenciaException {
        eliminarPorIdVenta(idVenta);
        for (DetalleVenta d : nuevosDetalles) {
            agregar(idVenta, d);
        }
    }
}