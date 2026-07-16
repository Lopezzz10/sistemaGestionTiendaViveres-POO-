package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Cajero;
import edu.uce.programacion2.tienda.negocio.Cliente;
import edu.uce.programacion2.tienda.negocio.DetalleFactura;
import edu.uce.programacion2.tienda.negocio.Factura;
import edu.uce.programacion2.tienda.negocio.Venta;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Clase que gestiona la persistencia de las facturas en archivo binario de
 * acceso aleatorio. Reemplaza a las antiguas Facturas.java (lista en
 * memoria) y FacturasArchivo.java (serializacion de tamano variable).
 *
 * Esta clase solo guarda el ENCABEZADO de la factura (tamano fijo). Las
 * lineas fiscales ({@link DetalleFactura}) viven en su propio archivo,
 * gestionado por {@link DetallesFactura}, con idFactura como clave
 * foranea -- el mismo patron que Ventas/DetallesVenta y
 * Compras/DetallesCompra.
 *
 * La venta, el cliente y el cajero se guardan como FK (su id) y se
 * resuelven al leer usando {@link Ventas}, {@link Clientes} y
 * {@link Cajeros} respectivamente (si se proveen esos DAOs); si no se
 * proveen, o el id no existe, quedan null.
 *
 * subtotal, montoIva y total NO se persisten: Factura ya los recalcula
 * siempre a partir de sus detalles (calcularMontos()), igual que
 * Venta/Compra no persisten su total.
 *
 * <pre>
 * estructura del registro de encabezado (84 bytes):
 * idFactura       int          4 bytes
 * numeroFactura   20 chars    40 bytes  (ej. "001-001-000000001")
 * fechaEmision    long         8 bytes  (milisegundos desde 1970)
 * idVenta         int          4 bytes  (FK -> Venta, 0 = sin venta)
 * idCliente       int          4 bytes  (FK -> Usuario/Cliente, 0 = sin cliente)
 * idCajero        int          4 bytes  (FK -> Usuario/Cajero, 0 = sin cajero)
 * estado          10 chars    20 bytes  (nombre del enum: EMITIDA, ANULADA, PAGADA)
 * total:                      84 bytes
 * </pre>
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Facturas extends AccesoAleatorio {

    private static final int TAM_NUMERO_FACTURA = 20;
    private static final int TAM_ESTADO         = 10;
    private static final int TAM_REGISTRO =
            4 + (TAM_NUMERO_FACTURA * 2) + 8 + 4 + 4 + 4 + (TAM_ESTADO * 2); // 84

    private DetallesFactura detallesFacturaDAO;
    private Ventas ventasDAO;
    private Clientes clientesDAO;
    private Cajeros cajerosDAO;

    public Facturas(String nomArchivo, DetallesFactura detallesFacturaDAO,
                    Ventas ventasDAO, Clientes clientesDAO, Cajeros cajerosDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.detallesFacturaDAO = detallesFacturaDAO;
        this.ventasDAO = ventasDAO;
        this.clientesDAO = clientesDAO;
        this.cajerosDAO = cajerosDAO;
    }

    public Facturas(String nomArchivo, DetallesFactura detallesFacturaDAO) {
        this(nomArchivo, detallesFacturaDAO, null, null, null);
    }

    public Facturas(String nomArchivo) {
        this(nomArchivo, null, null, null, null);
    }

    public Facturas() {
        this("facturas.dat", null, null, null, null);
    }

    public void setDetallesFacturaDAO(DetallesFactura detallesFacturaDAO) {
        this.detallesFacturaDAO = detallesFacturaDAO;
    }

    public void setVentasDAO(Ventas ventasDAO) {
        this.ventasDAO = ventasDAO;
    }

    public void setClientesDAO(Clientes clientesDAO) {
        this.clientesDAO = clientesDAO;
    }

    public void setCajerosDAO(Cajeros cajerosDAO) {
        this.cajerosDAO = cajerosDAO;
    }

    // Lee solo el encabezado de una factura (sin lineas fiscales propias:
    // el constructor de Factura las genera de la venta, y luego las
    // reemplazamos por las persistidas en conDetalles()).
    private Factura leeEncabezado() throws IOException {
        int idFactura        = archivo.readInt();
        String numeroFactura = leeString(TAM_NUMERO_FACTURA);
        long fechaMillis      = archivo.readLong();
        int idVenta           = archivo.readInt();
        int idCliente         = archivo.readInt();
        int idCajero          = archivo.readInt();
        String estadoTexto    = leeString(TAM_ESTADO);

        Factura f = new Factura(idFactura, numeroFactura,
                resolverVenta(idVenta), resolverCliente(idCliente), resolverCajero(idCajero));
        f.setFechaEmision(new Date(fechaMillis));
        f.setEstado(Factura.EstadoFactura.valueOf(estadoTexto.trim()));
        return f;
    }

    private Venta resolverVenta(int idVenta) {
        if (idVenta <= 0 || ventasDAO == null) return null;
        try {
            return ventasDAO.buscar(idVenta);
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    private Cliente resolverCliente(int idCliente) {
        if (idCliente <= 0 || clientesDAO == null) return null;
        try {
            return clientesDAO.buscar(idCliente);
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    private Cajero resolverCajero(int idCajero) {
        if (idCajero <= 0 || cajerosDAO == null) return null;
        try {
            return cajerosDAO.buscar(idCajero);
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    // Escribe solo el encabezado de una factura en la posicion actual.
    private void escribeEncabezado(Factura f) throws IOException {
        archivo.writeInt(f.getIdFactura());
        escribeString(f.getNumeroFactura(), TAM_NUMERO_FACTURA);
        archivo.writeLong(f.getFechaEmision() != null ? f.getFechaEmision().getTime() : 0L);
        archivo.writeInt(f.getVenta() != null ? f.getVenta().getIdVenta() : 0);
        archivo.writeInt(f.getCliente() != null ? f.getCliente().getIdUsuario() : 0);
        archivo.writeInt(f.getCajero() != null ? f.getCajero().getIdUsuario() : 0);
        escribeString(f.getEstado().name(), TAM_ESTADO);
    }

    // Sustituye las lineas fiscales generadas automaticamente de la venta
    // (por el constructor de Factura) por las persistidas en DetallesFactura
    // -- son la "fotografia fiscal" real al momento de la emision.
    private Factura conDetalles(Factura f) throws PersistenciaException {
        if (detallesFacturaDAO != null) {
            ArrayList<DetalleFactura> persistidos = detallesFacturaDAO.obtenerPorIdFactura(f.getIdFactura());
            if (!persistidos.isEmpty()) {
                f.getDetalles().clear();
                for (DetalleFactura d : persistidos) f.agregarDetalle(d);
            }
        }
        return f;
    }

    // Agrega una factura nueva (encabezado + lineas fiscales), asignandole el siguiente id.
    public void agregar(Factura f) throws PersistenciaException {
        if (f == null || f.getNumeroFactura() == null || f.getNumeroFactura().isEmpty()) {
            throw new PersistenciaException("Factura invalida.");
        }
        if (existeNumero(f.getNumeroFactura())) {
            throw new PersistenciaException("Factura ya existe: " + f.getNumeroFactura());
        }
        int nuevoId = siguienteId();
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            f.setIdFactura(nuevoId);
            archivo.seek(archivo.length());
            escribeEncabezado(f);
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("No se pudo abrir el archivo de facturas.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al registrar la factura.");
        } finally {
            try { if (archivo != null) archivo.close(); } catch (IOException ioe) { }
        }

        if (detallesFacturaDAO != null) {
            detallesFacturaDAO.reemplazarDetalles(f.getIdFactura(), f.getDetalles());
        }
    }

    /**
     * Verifica si ya existe una factura con el mismo número usando Stream API.
     */
    private boolean existeNumero(String numeroFactura) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                return buscarConStream(this::leeEncabezado,
                        x -> x.getNumeroFactura().trim().equalsIgnoreCase(numeroFactura.trim())) != null;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return false;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al verificar existencia de factura.");
        }
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
                        .mapToInt(Factura::getIdFactura)
                        .max()
                        .orElse(0) + 1;
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return 1;
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al calcular el siguiente id de factura.");
        }
    }

    /**
     * Lee todos los encabezados (sin lineas fiscales persistidas adjuntas)
     * usando Stream API.
     */
    private ArrayList<Factura> obtenerTodosLosEncabezados() throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                return leerTodosConStream(this::leeEncabezado);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            return new ArrayList<>();
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al leer los encabezados de factura.");
        }
    }

    /**
     * Busca una factura por su número usando Stream API, con sus lineas fiscales ya adjuntas.
     */
    public Factura buscar(String numeroFactura) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                Factura f = buscarConStream(this::leeEncabezado,
                        x -> x.getNumeroFactura().trim().equalsIgnoreCase(numeroFactura.trim()));
                if (f == null) {
                    throw new PersistenciaException("Factura no encontrada: " + numeroFactura);
                }
                return conDetalles(f);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Factura no encontrada: " + numeroFactura);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar la factura.");
        }
    }

    /**
     * Busca una factura por su id usando Stream API, con sus lineas fiscales ya adjuntas.
     */
    public Factura buscarPorId(int idFactura) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                Factura f = buscarConStream(this::leeEncabezado, x -> x.getIdFactura() == idFactura);
                if (f == null) {
                    throw new PersistenciaException("Factura no encontrada: id=" + idFactura);
                }
                return conDetalles(f);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Factura no encontrada: id=" + idFactura);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar la factura.");
        }
    }

    /**
     * Actualiza el encabezado de una factura existente usando Stream API
     * para encontrar el registro y reemplaza sus lineas fiscales.
     */
    public void actualizar(Factura f) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                long indice = indiceConStream(this::leeEncabezado,
                        x -> x.getNumeroFactura().trim().equalsIgnoreCase(f.getNumeroFactura().trim()));
                if (indice == -1) {
                    throw new PersistenciaException("Factura no encontrada para actualizar.");
                }
                archivo.seek(indice * tamRegistro);
                escribeEncabezado(f);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Archivo de facturas no encontrado.");
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al actualizar la factura.");
        }

        if (detallesFacturaDAO != null) {
            detallesFacturaDAO.reemplazarDetalles(f.getIdFactura(), f.getDetalles());
        }
    }

    /**
     * Inactiva (borrado logico) una factura reutilizando {@link Factura#anular()}.
     */
    public void eliminar(String numeroFactura) throws PersistenciaException {
        Factura f = buscar(numeroFactura);
        f.anular();
        actualizar(f);
    }

    /**
     * Retorna solo las facturas con estado EMITIDA, con sus lineas fiscales adjuntas.
     */
    public ArrayList<Factura> listarEmitidas() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.EMITIDA)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Retorna solo las facturas con estado ANULADA, con sus lineas fiscales adjuntas.
     */
    public ArrayList<Factura> listarAnuladas() throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(f -> f.getEstado() == Factura.EstadoFactura.ANULADA)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Devuelve todas las facturas registradas, cada una con sus lineas fiscales adjuntas.
     * Usa Stream API para leer todos los encabezados y luego agrupa los detalles
     * en una sola pasada para evitar N+1 queries.
     */
    public ArrayList<Factura> obtenerTodos() throws PersistenciaException {
        ArrayList<Factura> lista;
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
            throw new PersistenciaException("Error al obtener las facturas.");
        }

        // En vez de llamar a conDetalles(f) por cada factura -- lo que reabria
        // y releia el archivo de detalles completo una vez por factura -- se
        // lee ese archivo UNA SOLA VEZ y se agrupa por idFactura en memoria.
        java.util.Map<Integer, ArrayList<DetalleFactura>> detallesPorFactura =
                (detallesFacturaDAO != null)
                        ? detallesFacturaDAO.obtenerAgrupadoPorFactura()
                        : new java.util.HashMap<>();

        return lista.stream()
                .peek(f -> {
                    ArrayList<DetalleFactura> persistidos = detallesPorFactura.get(f.getIdFactura());
                    if (persistidos != null && !persistidos.isEmpty()) {
                        f.getDetalles().clear();
                        persistidos.forEach(f::agregarDetalle);
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Busqueda generica por cualquier condicion expresada como funcion
     * (programacion funcional): recibe un {@link Predicate}&lt;{@link Factura}&gt;
     * y retorna las facturas que lo cumplen, con sus lineas fiscales adjuntas.
     */
    public ArrayList<Factura> buscarPor(Predicate<Factura> criterio) throws PersistenciaException {
        return obtenerTodos().stream()
                .filter(criterio)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int conteo() {
        return (int) numRegistros();
    }
}