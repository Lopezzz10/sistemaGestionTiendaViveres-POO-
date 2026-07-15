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
        // Ver comentario equivalente en Usuarios.agregar(): siguienteId() debe
        // calcularse antes de abrir el archivo para escritura, porque abre y
        // cierra su propia RandomAccessFile sobre el campo "archivo" heredado.
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

    private boolean existeNumero(String numeroFactura) throws PersistenciaException {
        for (Factura f : obtenerTodosLosEncabezados()) {
            if (f.getNumeroFactura().trim().equalsIgnoreCase(numeroFactura.trim())) return true;
        }
        return false;
    }

    /** Calcula el siguiente id auto-incremental (maximo id existente + 1), solo sobre encabezados. */
    public int siguienteId() throws PersistenciaException {
        return GeneradorId.siguienteId(obtenerTodosLosEncabezados(), Factura::getIdFactura);
    }

    // Lee todos los encabezados (sin lineas fiscales persistidas adjuntas).
    private ArrayList<Factura> obtenerTodosLosEncabezados() throws PersistenciaException {
        ArrayList<Factura> lista = new ArrayList<>();
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) lista.add(leeEncabezado());
            } catch (EOFException eof) {
                // fin del archivo
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            // archivo aun no existe
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al leer los encabezados de factura.");
        }
        return lista;
    }

    // Busca una factura por su numero, con sus lineas fiscales ya adjuntas.
    public Factura buscar(String numeroFactura) throws PersistenciaException {
        for (Factura f : obtenerTodosLosEncabezados()) {
            if (f.getNumeroFactura().trim().equalsIgnoreCase(numeroFactura.trim())) {
                return conDetalles(f);
            }
        }
        throw new PersistenciaException("Factura no encontrada: " + numeroFactura);
    }

    // Busca una factura por su id, con sus lineas fiscales ya adjuntas.
    public Factura buscarPorId(int idFactura) throws PersistenciaException {
        for (Factura f : obtenerTodosLosEncabezados()) {
            if (f.getIdFactura() == idFactura) return conDetalles(f);
        }
        throw new PersistenciaException("Factura no encontrada: id=" + idFactura);
    }

    // Actualiza el encabezado de una factura existente y reemplaza sus lineas fiscales.
    public void actualizar(Factura f) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Factura leida = leeEncabezado();
                    if (leida.getNumeroFactura().trim().equalsIgnoreCase(f.getNumeroFactura().trim())) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeEncabezado(f);
                        break;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Factura no encontrada para actualizar.");
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

    /** Inactiva (borrado logico) una factura reutilizando {@link Factura#anular()}. */
    public void eliminar(String numeroFactura) throws PersistenciaException {
        Factura f = buscar(numeroFactura);
        f.anular();
        actualizar(f);
    }

    /** Retorna solo las facturas con estado EMITIDA, con sus lineas fiscales adjuntas. */
    public ArrayList<Factura> listarEmitidas() throws PersistenciaException {
        ArrayList<Factura> resultado = new ArrayList<>();
        for (Factura f : obtenerTodos())
            if (f.getEstado() == Factura.EstadoFactura.EMITIDA) resultado.add(f);
        return resultado;
    }

    /** Retorna solo las facturas con estado ANULADA, con sus lineas fiscales adjuntas. */
    public ArrayList<Factura> listarAnuladas() throws PersistenciaException {
        ArrayList<Factura> resultado = new ArrayList<>();
        for (Factura f : obtenerTodos())
            if (f.getEstado() == Factura.EstadoFactura.ANULADA) resultado.add(f);
        return resultado;
    }

    // Devuelve todas las facturas registradas, cada una con sus lineas fiscales adjuntas.
    public ArrayList<Factura> obtenerTodos() throws PersistenciaException {
        ArrayList<Factura> lista = obtenerTodosLosEncabezados();

        // En vez de llamar a conDetalles(f) por cada factura -- lo que reabria
        // y releia el archivo de detalles completo una vez por factura -- se
        // lee ese archivo UNA SOLA VEZ y se agrupa por idFactura en memoria.
        java.util.Map<Integer, ArrayList<DetalleFactura>> detallesPorFactura =
                (detallesFacturaDAO != null)
                        ? detallesFacturaDAO.obtenerAgrupadoPorFactura()
                        : new java.util.HashMap<>();

        ArrayList<Factura> conTodosLosDetalles = new ArrayList<>();
        for (Factura f : lista) {
            ArrayList<DetalleFactura> persistidos = detallesPorFactura.get(f.getIdFactura());
            if (persistidos != null && !persistidos.isEmpty()) {
                f.getDetalles().clear();
                for (DetalleFactura d : persistidos) f.agregarDetalle(d);
            }
            conTodosLosDetalles.add(f);
        }
        return conTodosLosDetalles;
    }

    /**
     * Busqueda generica por cualquier condicion expresada como funcion
     * (programacion funcional): recibe un {@link Predicate}&lt;{@link Factura}&gt;
     * y retorna las facturas que lo cumplen, con sus lineas fiscales adjuntas.
     */
    public ArrayList<Factura> buscarPor(Predicate<Factura> criterio) throws PersistenciaException {
        ArrayList<Factura> resultado = new ArrayList<>();
        for (Factura f : obtenerTodos()) {
            if (criterio.test(f)) resultado.add(f);
        }
        return resultado;
    }

    public int conteo() {
        return (int) numRegistros();
    }
}