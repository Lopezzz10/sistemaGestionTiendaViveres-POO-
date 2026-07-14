package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.DetalleVenta;
import edu.uce.programacion2.tienda.negocio.Venta;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

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
        // Ver comentario equivalente en Usuarios.agregar(): siguienteId() debe
        // calcularse antes de abrir el archivo para escritura, porque abre y
        // cierra su propia RandomAccessFile sobre el campo "archivo" heredado.
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
        ArrayList<DetalleVenta> resultado = new ArrayList<>();
        int siguienteId = detallesVentaDAO.siguienteIdDetalle();
        for (DetalleVenta d : detalles) {
            if (d.getIdDetalle() > 0) {
                resultado.add(d);
            } else {
                resultado.add(new DetalleVenta(siguienteId++, d.getProducto(), d.getCantidad(), d.getDescuento()));
            }
        }
        return resultado;
    }

    /** Calcula el siguiente id auto-incremental (maximo id existente + 1), solo sobre encabezados. */
    public int siguienteId() throws PersistenciaException {
        ArrayList<Venta> encabezados = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) encabezados.add(leeEncabezado());
            } catch (EOFException eof) {
                // fin del archivo
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            // archivo aun no existe: no hay encabezados
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al calcular el siguiente id de venta.");
        }
        return GeneradorId.siguienteId(encabezados, Venta::getIdVenta);
    }

    // Busca una venta por su id, con sus detalles ya adjuntos.
    public Venta buscar(int idVenta) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Venta v = leeEncabezado();
                    if (v.getIdVenta() == idVenta) return conDetalles(v);
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Venta no encontrada: id=" + idVenta);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Venta no encontrada: id=" + idVenta);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar la venta.");
        }
    }

    // Actualiza el encabezado de una venta existente y reemplaza sus detalles.
    public void actualizar(Venta v) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Venta leida = leeEncabezado();
                    if (leida.getIdVenta() == v.getIdVenta()) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeEncabezado(v);
                        break;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Venta no encontrada para actualizar.");
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

    /** Retorna solo las ventas con estado ACTIVA, con sus detalles adjuntos. */
    public ArrayList<Venta> listarActivas() throws PersistenciaException {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta v : obtenerTodos()) {
            if (v.getEstado() == Venta.Estado.ACTIVA) resultado.add(v);
        }
        return resultado;
    }

    /** Retorna solo las ventas con estado ANULADA, con sus detalles adjuntos. */
    public ArrayList<Venta> listarAnuladas() throws PersistenciaException {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta v : obtenerTodos()) {
            if (v.getEstado() == Venta.Estado.ANULADA) resultado.add(v);
        }
        return resultado;
    }

    // Devuelve todas las ventas registradas, cada una con sus detalles adjuntos.
    public ArrayList<Venta> obtenerTodos() throws PersistenciaException {
        ArrayList<Venta> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    lista.add(leeEncabezado());
                }
            } catch (EOFException eof) {
                // fin del archivo
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return lista;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al obtener las ventas.");
        }

        // Adjuntamos los detalles fuera del bloque anterior para no anidar
        // dos RandomAccessFile abiertos al mismo tiempo (este y el de
        // DetallesVenta, que abre su propio archivo internamente).
        ArrayList<Venta> conTodosLosDetalles = new ArrayList<>();
        for (Venta v : lista) {
            conTodosLosDetalles.add(conDetalles(v));
        }
        return conTodosLosDetalles;
    }

    public int conteo() {
        return (int) numRegistros();
    }
}