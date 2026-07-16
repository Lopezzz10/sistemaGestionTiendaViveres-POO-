package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Cliente;
import edu.uce.programacion2.tienda.negocio.Compra;
import edu.uce.programacion2.tienda.negocio.DetalleCompra;
import edu.uce.programacion2.tienda.negocio.Proveedor;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la persistencia de las compras (a proveedor) en
 * archivo binario de acceso aleatorio. Reemplaza a las antiguas
 * Compras.java (lista en memoria) y ComprasArchivo.java (serializacion de
 * la compra Y sus detalles juntos, en un solo bloque de tamano variable).
 *
 * Esta clase solo guarda el ENCABEZADO de la compra (tamano fijo). La
 * lista de {@link DetalleCompra} vive en su propio archivo, gestionado por
 * {@link DetallesCompra}, con idCompra como clave foranea -- el mismo
 * patron que usa Ventas/DetallesVenta. Asi el registro de Compra no
 * depende de cuantos productos se hayan adquirido.
 *
 * El proveedor y el cliente se guardan como FK (su id), y se resuelven al
 * leer usando {@link Proveedores} y {@link Clientes} respectivamente (si
 * se proveen esos DAOs); si no se proveen, o el id no existe, quedan null.
 *
 * El campo total NO se persiste: Compra ya lo calcula siempre a partir de
 * sus detalles (calcularTotal()).
 *
 * <pre>
 * estructura del registro de encabezado (80 bytes):
 * idCompra       int          4 bytes
 * fecha          long         8 bytes  (milisegundos desde 1970)
 * metodoPago     20 chars    40 bytes
 * estado         10 chars    20 bytes  (nombre del enum: PENDIENTE, RECIBIDA, ANULADA)
 * idProveedor    int          4 bytes  (FK -> Proveedor, 0 = sin proveedor)
 * idCliente      int          4 bytes  (FK -> Usuario/Cliente, 0 = sin cliente)
 * total:                     80 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Compras extends AccesoAleatorio {

    private static final int TAM_METODO_PAGO = 20;
    private static final int TAM_ESTADO      = 10;
    private static final int TAM_REGISTRO    = 4 + 8 + (TAM_METODO_PAGO * 2) + (TAM_ESTADO * 2) + 4 + 4; // 80

    private DetallesCompra detallesCompraDAO;
    private Proveedores proveedoresDAO;
    private Clientes clientesDAO;

    public Compras(String nomArchivo, DetallesCompra detallesCompraDAO,
                   Proveedores proveedoresDAO, Clientes clientesDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.detallesCompraDAO = detallesCompraDAO;
        this.proveedoresDAO = proveedoresDAO;
        this.clientesDAO = clientesDAO;
    }

    public Compras(String nomArchivo, DetallesCompra detallesCompraDAO) {
        this(nomArchivo, detallesCompraDAO, null, null);
    }

    public Compras(String nomArchivo) {
        this(nomArchivo, null, null, null);
    }

    public Compras() {
        this("compras.dat", null, null, null);
    }

    public void setDetallesCompraDAO(DetallesCompra detallesCompraDAO) {
        this.detallesCompraDAO = detallesCompraDAO;
    }

    public void setProveedoresDAO(Proveedores proveedoresDAO) {
        this.proveedoresDAO = proveedoresDAO;
    }

    public void setClientesDAO(Clientes clientesDAO) {
        this.clientesDAO = clientesDAO;
    }

    // Lee solo el encabezado de una compra (sin detalles) en la posicion actual.
    private Compra leeEncabezado() throws IOException {
        int idCompra        = archivo.readInt();
        long fechaMillis     = archivo.readLong();
        String metodoPago   = leeString(TAM_METODO_PAGO);
        String estadoTexto  = leeString(TAM_ESTADO);
        int idProveedor      = archivo.readInt();
        int idCliente        = archivo.readInt();

        Compra c = new Compra(idCompra, metodoPago, resolverProveedor(idProveedor), resolverCliente(idCliente));
        c.setFecha(new Date(fechaMillis));
        c.setEstado(Compra.Estado.valueOf(estadoTexto.trim()));
        return c;
    }

    // Consulta el DAO de proveedores por el id guardado.
    private Proveedor resolverProveedor(int idProveedor) {
        if (idProveedor <= 0 || proveedoresDAO == null) return null;
        try {
            return proveedoresDAO.buscar(idProveedor);
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    // Consulta el DAO de clientes por el id guardado.
    private Cliente resolverCliente(int idCliente) {
        if (idCliente <= 0 || clientesDAO == null) return null;
        try {
            return clientesDAO.buscar(idCliente);
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    // Escribe solo el encabezado de una compra (sin detalles) en la posicion actual.
    private void escribeEncabezado(Compra c) throws IOException {
        archivo.writeInt(c.getIdCompra());
        archivo.writeLong(c.getFecha() != null ? c.getFecha().getTime() : 0L);
        escribeString(c.getMetodoPago(), TAM_METODO_PAGO);
        escribeString(c.getEstado().name(), TAM_ESTADO);
        archivo.writeInt(c.getProveedor() != null ? c.getProveedor().getIdProveedor() : 0);
        archivo.writeInt(c.getCliente() != null ? c.getCliente().getIdUsuario() : 0);
    }

    // Adjunta a una compra ya leida (solo encabezado) sus detalles desde DetallesCompra.
    private Compra conDetalles(Compra c) throws PersistenciaException {
        if (detallesCompraDAO != null) {
            for (DetalleCompra d : detallesCompraDAO.obtenerPorIdCompra(c.getIdCompra())) {
                c.agregarDetalle(d);
            }
        }
        return c;
    }

    // Agrega una compra nueva (encabezado + detalles), asignandole el siguiente id.
    public void agregar(Compra c) throws PersistenciaException {
        if (c == null) {
            throw new PersistenciaException("Compra invalida.");
        }
        int nuevoId = siguienteId();
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            c.setIdCompra(nuevoId);
            archivo.seek(archivo.length());
            escribeEncabezado(c);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de compras.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar la compra.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }

        if (detallesCompraDAO != null) {
            detallesCompraDAO.reemplazarDetalles(c.getIdCompra(), asignarIds(c.getDetalles()));
        }
    }

    // Asigna IDs a los detalles que no tienen uno valido.
    private ArrayList<DetalleCompra> asignarIds(ArrayList<DetalleCompra> detalles)
            throws PersistenciaException {
        int[] siguienteId = { detallesCompraDAO.siguienteIdDetalle() };
        return detalles.stream()
                .map(d -> d.getIdDetalle() > 0
                        ? d
                        : new DetalleCompra(siguienteId[0]++, d.getProducto(), d.getCantidad(),
                        d.getPrecioCompra(), d.getDescuento()))
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
                        .mapToInt(Compra::getIdCompra)
                        .max()
                        .orElse(0) + 1;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return 1;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al calcular el siguiente id de compra.");
        }
    }

    /**
     * Busca una compra por su id usando Stream API, con sus detalles ya adjuntos.
     */
    public Compra buscar(int idCompra) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                Compra c = buscarConStream(this::leeEncabezado, x -> x.getIdCompra() == idCompra);
                if (c == null) {
                    throw new PersistenciaException("Compra no encontrada: id=" + idCompra);
                }
                return conDetalles(c);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Compra no encontrada: id=" + idCompra);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar la compra.");
        }
    }

    /**
     * Actualiza el encabezado de una compra existente usando Stream API
     * para encontrar el registro y reemplaza sus detalles.
     */
    public void actualizar(Compra c) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                long indice = indiceConStream(this::leeEncabezado, x -> x.getIdCompra() == c.getIdCompra());
                if (indice == -1) {
                    throw new PersistenciaException("Compra no encontrada para actualizar.");
                }
                archivo.seek(indice * tamRegistro);
                escribeEncabezado(c);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de compras no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar la compra.");
        }

        if (detallesCompraDAO != null) {
            detallesCompraDAO.reemplazarDetalles(c.getIdCompra(), asignarIds(c.getDetalles()));
        }
    }

    /**
     * Inactiva una compra reutilizando {@link Compra#anular()}.
     */
    public void inactivar(int idCompra) throws PersistenciaException {
        Compra c = buscar(idCompra);
        c.anular();
        actualizar(c);
    }

    /**
     * Retorna solo las compras con estado PENDIENTE, con sus detalles adjuntos.
     */
    public ArrayList<Compra> listarPendientes() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(c -> c.getEstado() == Compra.Estado.PENDIENTE)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Retorna solo las compras con estado RECIBIDA, con sus detalles adjuntos.
     */
    public ArrayList<Compra> listarRecibidas() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(c -> c.getEstado() == Compra.Estado.RECIBIDA)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Retorna solo las compras activas (no ANULADAS), con sus detalles adjuntos.
     */
    public ArrayList<Compra> listarActivas() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(c -> c.getEstado() != Compra.Estado.ANULADA)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Devuelve todas las compras registradas, cada una con sus detalles adjuntos.
     * Usa Stream API para leer todos los encabezados y luego agrupa los detalles
     * en una sola pasada para evitar N+1 queries.
     */
    public ArrayList<Compra> obtenerTodos() throws PersistenciaException {
        ArrayList<Compra> lista;
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
            throw new PersistenciaException("Error al obtener las compras.");
        }

        // Adjuntamos los detalles fuera del bloque anterior para no anidar
        // dos RandomAccessFile abiertos al mismo tiempo (este y el de
        // DetallesCompra, que abre su propio archivo internamente).
        //
        // En vez de llamar a conDetalles(c) por cada compra -- lo que reabria
        // y releia el archivo de detalles completo una vez por compra -- se
        // lee ese archivo UNA SOLA VEZ y se agrupa por idCompra en memoria.
        java.util.Map<Integer, ArrayList<DetalleCompra>> detallesPorCompra =
                (detallesCompraDAO != null)
                        ? detallesCompraDAO.obtenerAgrupadoPorCompra()
                        : new java.util.HashMap<>();

        return lista.stream()
                .peek(c -> {
                    ArrayList<DetalleCompra> detalles = detallesPorCompra.get(c.getIdCompra());
                    if (detalles != null) {
                        detalles.forEach(c::agregarDetalle);
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int conteo() {
        return (int) numRegistros();
    }
}