package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import java.util.ArrayList;

/**
 * Capa de persistencia (en memoria) para los detalles de factura.
 * <p>
 * A diferencia de {@link Facturas}, que guarda los detalles embebidos dentro
 * de cada {@code Factura}, esta clase los administra como registros
 * independientes, asociados a su factura mediante {@code idFactura}. Esto
 * permite futuras consultas o reportes por línea de detalle sin depender
 * de recorrer factura por factura.
 * <p>
 * NOTA: como esta clase también se llama {@code DetalleFactura} (igual que
 * la clase de negocio {@link edu.uce.programacion2.tienda.negocio.DetalleFactura}),
 * no se puede importar esa clase aquí (Java no permite importar un tipo con
 * el mismo nombre simple que la clase declarada en el archivo). Por eso se
 * usa su nombre completamente calificado en todo el código.
 *
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class DetalleFactura {

    /** Asocia un detalle de factura con la factura a la que pertenece. */
    public static class Registro {
        private final int idFactura;
        private final edu.uce.programacion2.tienda.negocio.DetalleFactura detalle;

        public Registro(int idFactura, edu.uce.programacion2.tienda.negocio.DetalleFactura detalle) {
            this.idFactura = idFactura;
            this.detalle = detalle;
        }

        public int getIdFactura() { return idFactura; }
        public edu.uce.programacion2.tienda.negocio.DetalleFactura getDetalle() { return detalle; }
    }

    private ArrayList<Registro> registros;

    public DetalleFactura() {
        this.registros = new ArrayList<>();
    }

    /**
     * Agrega un detalle asociado a una factura.
     *
     * @param idFactura id de la factura a la que pertenece
     * @param detalle   detalle a registrar
     */
    public void agregar(int idFactura, edu.uce.programacion2.tienda.negocio.DetalleFactura detalle)
            throws PersistenciaException {
        if (detalle == null)
            throw new PersistenciaException("Detalle de factura inválido.");
        registros.add(new Registro(idFactura, detalle));
    }

    /**
     * Busca un detalle específico dentro de una factura por su idDetalle.
     */
    public edu.uce.programacion2.tienda.negocio.DetalleFactura buscar(int idFactura, int idDetalle)
            throws PersistenciaException {
        for (Registro r : registros)
            if (r.getIdFactura() == idFactura && r.getDetalle().getIdDetalle() == idDetalle)
                return r.getDetalle();
        throw new PersistenciaException(
                "Detalle no encontrado: idFactura=" + idFactura + ", idDetalle=" + idDetalle);
    }

    /** Retorna todos los detalles pertenecientes a una factura. */
    public ArrayList<edu.uce.programacion2.tienda.negocio.DetalleFactura> listarPorFactura(int idFactura) {
        ArrayList<edu.uce.programacion2.tienda.negocio.DetalleFactura> resultado = new ArrayList<>();
        for (Registro r : registros)
            if (r.getIdFactura() == idFactura) resultado.add(r.getDetalle());
        return resultado;
    }

    public void actualizar(int idFactura, edu.uce.programacion2.tienda.negocio.DetalleFactura detalle)
            throws PersistenciaException {
        for (int i = 0; i < registros.size(); i++) {
            Registro r = registros.get(i);
            if (r.getIdFactura() == idFactura && r.getDetalle().getIdDetalle() == detalle.getIdDetalle()) {
                registros.set(i, new Registro(idFactura, detalle));
                return;
            }
        }
        throw new PersistenciaException("Detalle no encontrado para actualizar.");
    }

    public void eliminar(int idFactura, int idDetalle) throws PersistenciaException {
        edu.uce.programacion2.tienda.negocio.DetalleFactura d = buscar(idFactura, idDetalle);
        for (Registro r : registros) {
            if (r.getIdFactura() == idFactura && r.getDetalle() == d) {
                registros.remove(r);
                return;
            }
        }
    }

    /** Elimina todos los detalles asociados a una factura (p. ej. al anularla). */
    public void eliminarPorFactura(int idFactura) {
        registros.removeIf(r -> r.getIdFactura() == idFactura);
    }

    public ArrayList<Registro> listar() { return registros; }

    public int conteo() { return registros.size(); }
}