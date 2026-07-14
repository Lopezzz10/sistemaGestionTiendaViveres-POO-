package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Cliente;
import edu.uce.programacion2.tienda.negocio.Compra;
import edu.uce.programacion2.tienda.negocio.DetalleCompra;
import edu.uce.programacion2.tienda.negocio.Proveedor;
import edu.uce.programacion2.tienda.negocio.Usuario;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

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
 * leer usando {@link Proveedores} y {@link Usuarios} respectivamente (si
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
    private Usuarios usuariosDAO;

    public Compras(String nomArchivo, DetallesCompra detallesCompraDAO,
                   Proveedores proveedoresDAO, Usuarios usuariosDAO) {
        super(nomArchivo, TAM_REGISTRO);
        this.detallesCompraDAO = detallesCompraDAO;
        this.proveedoresDAO = proveedoresDAO;
        this.usuariosDAO = usuariosDAO;
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

    public void setUsuariosDAO(Usuarios usuariosDAO) {
        this.usuariosDAO = usuariosDAO;
    }

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

    private Proveedor resolverProveedor(int idProveedor) {
        if (idProveedor <= 0 || proveedoresDAO == null) return null;
        try {
            return proveedoresDAO.buscar(idProveedor);
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    private Cliente resolverCliente(int idCliente) {
        if (idCliente <= 0 || usuariosDAO == null) return null;
        try {
            Usuario u = usuariosDAO.buscar(idCliente);
            return (u instanceof Cliente) ? (Cliente) u : null;
        } catch (PersistenciaException pe) {
            return null;
        }
    }

    private void escribeEncabezado(Compra c) throws IOException {
        archivo.writeInt(c.getIdCompra());
        archivo.writeLong(c.getFecha() != null ? c.getFecha().getTime() : 0L);
        escribeString(c.getMetodoPago(), TAM_METODO_PAGO);
        escribeString(c.getEstado().name(), TAM_ESTADO);
        archivo.writeInt(c.getProveedor() != null ? c.getProveedor().getIdProveedor() : 0);
        archivo.writeInt(c.getCliente() != null ? c.getCliente().getIdUsuario() : 0);
    }

    private Compra conDetalles(Compra c) throws PersistenciaException {
        if (detallesCompraDAO != null) {
            for (DetalleCompra d : detallesCompraDAO.obtenerPorIdCompra(c.getIdCompra())) {
                c.agregarDetalle(d);
            }
        }
        return c;
    }

    public void agregar(Compra c) throws PersistenciaException {
        if (c == null) {
            throw new PersistenciaException("Compra invalida.");
        }
        // Ver comentario equivalente en Usuarios.agregar(): siguienteId() debe
        // calcularse antes de abrir el archivo para escritura, porque abre y
        // cierra su propia RandomAccessFile sobre el campo "archivo" heredado.
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

    private ArrayList<DetalleCompra> asignarIds(ArrayList<DetalleCompra> detalles)
            throws PersistenciaException {
        ArrayList<DetalleCompra> resultado = new ArrayList<>();
        int siguienteId = detallesCompraDAO.siguienteIdDetalle();
        for (DetalleCompra d : detalles) {
            if (d.getIdDetalle() > 0) {
                resultado.add(d);
            } else {
                resultado.add(new DetalleCompra(siguienteId++, d.getProducto(), d.getCantidad(),
                        d.getPrecioCompra(), d.getDescuento()));
            }
        }
        return resultado;
    }

    public int siguienteId() throws PersistenciaException {
        ArrayList<Compra> encabezados = new ArrayList<>();
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
            throw new PersistenciaException("Error al calcular el siguiente id de compra.");
        }
        return GeneradorId.siguienteId(encabezados, Compra::getIdCompra);
    }

    public Compra buscar(int idCompra) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "r");
            try {
                while (true) {
                    Compra c = leeEncabezado();
                    if (c.getIdCompra() == idCompra) return conDetalles(c);
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Compra no encontrada: id=" + idCompra);
            } finally {
                archivo.close();
            }
        } catch (FileNotFoundException fnf) {
            throw new PersistenciaException("Compra no encontrada: id=" + idCompra);
        } catch (IOException ioe) {
            throw new PersistenciaException("Error al buscar la compra.");
        }
    }

    public void actualizar(Compra c) throws PersistenciaException {
        try {
            archivo = new RandomAccessFile(nomArchivo, "rw");
            try {
                while (true) {
                    Compra leida = leeEncabezado();
                    if (leida.getIdCompra() == c.getIdCompra()) {
                        archivo.seek(archivo.getFilePointer() - tamRegistro);
                        escribeEncabezado(c);
                        break;
                    }
                }
            } catch (EOFException eof) {
                throw new PersistenciaException("Compra no encontrada para actualizar.");
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

    public void inactivar(int idCompra) throws PersistenciaException {
        Compra c = buscar(idCompra);
        c.anular();
        actualizar(c);
    }

    public ArrayList<Compra> listarPendientes() throws PersistenciaException {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : obtenerTodos())
            if (c.getEstado() == Compra.Estado.PENDIENTE) resultado.add(c);
        return resultado;
    }

    public ArrayList<Compra> listarRecibidas() throws PersistenciaException {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : obtenerTodos())
            if (c.getEstado() == Compra.Estado.RECIBIDA) resultado.add(c);
        return resultado;
    }

    public ArrayList<Compra> listarActivas() throws PersistenciaException {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : obtenerTodos())
            if (c.getEstado() != Compra.Estado.ANULADA) resultado.add(c);
        return resultado;
    }

    public ArrayList<Compra> obtenerTodos() throws PersistenciaException {
        ArrayList<Compra> lista = new ArrayList<>();
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
            throw new PersistenciaException("Error al obtener las compras.");
        }

        ArrayList<Compra> conTodosLosDetalles = new ArrayList<>();
        for (Compra c : lista) {
            conTodosLosDetalles.add(conDetalles(c));
        }
        return conTodosLosDetalles;
    }

    public int conteo() {
        return (int) numRegistros();
    }
}