package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.DetalleVenta;
import edu.uce.programacion2.tienda.negocio.Venta;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la persistencia de las ventas en archivo binario de
 * acceso aleatorio. Reemplaza a las antiguas Ventas.java (lista en memoria)
 * y VentasArchivo.java (serializacion de la venta Y sus detalles juntos,
 * en un solo bloque de tamano variable).
 *
 * Esta clase solo guarda el ENCABEZADO de la venta (tamano fijo). La lista
 * de {@link DetalleVenta} vive en su propio archivo, gestionado por
 * {@link DetallesVenta}, con idVenta como clave foranea -- el mismo patron
 * que usa DetallesFactura/Facturas en el proyecto de referencia (canchas).
 * Asi el registro de Venta no depende de cuantos productos tenga la venta.
 *
 * El campo total NO se persiste: Venta ya lo calcula siempre a partir de
 * sus detalles (calcularTotal()), igual que en la version anterior.
 *
 * <pre>
 * estructura del registro de encabezado (72 bytes):
 * idVenta        int          4 bytes
 * fecha          long         8 bytes  (milisegundos desde 1970)
 * metodoPago     20 chars    40 bytes
 * estado         10 chars    20 bytes  (nombre del enum: ACTIVA, ANULADA, COMPLETADA)
 * total:                     72 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Ventas extends AccesoAleatorio {

    private static final int TAM_METODO_PAGO = 20;
    private static final int TAM_ESTADO      = 10;
    private static final int TAM_REGISTRO    = 4 + 8 + (TAM_METODO_PAGO * 2) + (TAM_ESTADO * 2); // 72

    // DAO de detalles, con idVenta como clave foranea.
    private DetallesVenta detallesVentaDAO;

    public Ventas(String nomArchivo, DetallesVenta detallesVentaDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.detallesVentaDAO = detallesVentaDAO;
    }

    public Ventas(String nomArchivo) {
        this(nomArchivo, null);
    }

    // Constructor de conveniencia: usa "ventas.dat" en la raiz del proyecto.
    public Ventas() {
        this("ventas.dat", null);
    }

    public void setDetallesVentaDAO(DetallesVenta detallesVentaDAO) {
        this.detallesVentaDAO = detallesVentaDAO;
    }

    // Lee solo el encabezado de una venta (sin detalles) en la posicion actual.
    private Venta leeEncabezado() throws IOException {
        int idVenta        = archivo.readInt();
        long fechaMillis    = archivo.readLong();
        String metodoPago  = leeString(TAM_METODO_PAGO);
        String estadoTexto = leeString(TAM_ESTADO);

        Venta v = new Venta(idVenta, metodoPago);
        v.setFecha(new Date(fechaMillis));
        v.setEstado(Venta.Estado.valueOf(estadoTexto.trim()));
        return v;
    }

    // Escribe solo el encabezado de una venta (sin detalles) en la posicion actual.
    private void escribeEncabezado(Venta v) throws IOException {
        archivo.writeInt(v.getIdVenta());
        archivo.writeLong(v.getFecha() != null ? v.getFecha().getTime() : 0L);
        escribeString(v.getMetodoPago(), TAM_METODO_PAGO);
        escribeString(v.getEstado().name(), TAM_ESTADO);
    }

    // Adjunta a una venta ya leida (solo encabezado) sus detalles desde
    // DetallesVenta. agregarDetalle() tambien recalcula el total.
    private Venta conDetalles(Venta v) throws PersistenciaException {
        if (detallesVentaDAO != null) {
            for (DetalleVenta d : detallesVentaDAO.obtenerPorIdVenta(v.getIdVenta())) {
                v.agregarDetalle(d);
            }
        }
        return v;
    }

    // Agrega una venta nueva (encabezado + detalles), asignandole el siguiente id.
    public void agregar(Venta v) throws PersistenciaException {
        if (v == null) {
            throw new PersistenciaException("Venta invalida.");
        }
        int nuevoId = siguienteId();
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            v.setIdVenta(nuevoId);
            archivo.seek(archivo.length());
            escribeEncabezado(v);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de ventas.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar la venta.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }

        if (detallesVentaDAO != null) {
            detallesVentaDAO.reemplazarDetalles(v.getIdVenta(), asignarIds(v.getDetalles()));
        }
    }

    // DetalleVenta no tiene setter de idDetalle: para los detalles que aun
    // no tienen un id valido (<= 0, los recien agregados en memoria) se les
    // construye una copia con un id nuevo. Los que ya traen un id valido
    // (ej. detalles leidos previamente del archivo) se conservan tal cual.
    private ArrayList<DetalleVenta> asignarIds(ArrayList<DetalleVenta> detalles)
            throws PersistenciaException {
        int[] siguienteId = { detallesVentaDAO.siguienteIdDetalle() };
        return detalles.stream()
                .map(d -> d.getIdDetalle() > 0
                        ? d
                        : new DetalleVenta(siguienteId[0]++, d.getProducto(), d.getCantidad(), d.getDescuento()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Calcula el siguiente id auto-incremental (maximo id existente + 1),
     * usando Stream API para leer todos los encabezados.
     */
    public int siguienteId() throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                return leerTodosConStream(this::leeEncabezado).stream()
                        .mapToInt(Venta::getIdVenta)
                        .max()
                        .orElse(0) + 1;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return 1;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al calcular el siguiente id de venta.");
        }
    }

    /**
     * Busca una venta por su id usando Stream API, con sus detalles ya adjuntos.
     */
    public Venta buscar(int idVenta) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                Venta v = buscarConStream(this::leeEncabezado, x -> x.getIdVenta() == idVenta);
                if (v == null) {
                    throw new PersistenciaException("Venta no encontrada: id=" + idVenta);
                }
                return conDetalles(v);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Venta no encontrada: id=" + idVenta);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar la venta.");
        }
    }

    /**
     * Actualiza el encabezado de una venta existente usando Stream API
     * para encontrar el registro y reemplaza sus detalles.
     */
    public void actualizar(Venta v) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                long indice = indiceConStream(this::leeEncabezado, x -> x.getIdVenta() == v.getIdVenta());
                if (indice == -1) {
                    throw new PersistenciaException("Venta no encontrada para actualizar.");
                }
                archivo.seek(indice * tamRegistro);
                escribeEncabezado(v);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de ventas no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar la venta.");
        }

        if (detallesVentaDAO != null) {
            detallesVentaDAO.reemplazarDetalles(v.getIdVenta(), asignarIds(v.getDetalles()));
        }
    }

    /**
     * Inactiva una venta reutilizando {@link Venta#anular()}, igual que la
     * version anterior (deja el estado en ANULADA; el encabezado se conserva).
     */
    public void inactivar(int idVenta) throws PersistenciaException {
        Venta v = buscar(idVenta);
        v.anular();
        actualizar(v);
    }

    /**
     * Retorna solo las ventas con estado ACTIVA, con sus detalles adjuntos.
     */
    public ArrayList<Venta> listarActivas() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(v -> v.getEstado() == Venta.Estado.ACTIVA)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Retorna solo las ventas con estado ANULADA, con sus detalles adjuntos.
     */
    public ArrayList<Venta> listarAnuladas() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(v -> v.getEstado() == Venta.Estado.ANULADA)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Devuelve todas las ventas registradas, cada una con sus detalles adjuntos.
     * Usa Stream API para leer todos los encabezados y luego agrupa los detalles
     * en una sola pasada para evitar N+1 queries.
     */
    public ArrayList<Venta> obtenerTodos() throws PersistenciaException {
        ArrayList<Venta> lista;
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                lista = leerTodosConStream(this::leeEncabezado);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return new ArrayList<>();
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener las ventas.");
        }

        // Adjuntamos los detalles fuera del bloque anterior para no anidar
        // dos RandomAccessFile abiertos al mismo tiempo (este y el de
        // DetallesVenta, que abre su propio archivo internamente).
        //
        // En vez de llamar a conDetalles(v) por cada venta -- lo que reabria
        // y releia el archivo de detalles completo una vez por venta -- se
        // lee ese archivo UNA SOLA VEZ y se agrupa por idVenta en memoria.
        java.util.Map<Integer, ArrayList<DetalleVenta>> detallesPorVenta =
                (detallesVentaDAO != null)
                        ? detallesVentaDAO.obtenerAgrupadoPorVenta()
                        : new java.util.HashMap<>();

        return lista.stream()
                .peek(v -> {
                    ArrayList<DetalleVenta> detalles = detallesPorVenta.get(v.getIdVenta());
                    if (detalles != null) {
                        detalles.forEach(v::agregarDetalle);
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int conteo() {
        return (int) numRegistros();
    }
}