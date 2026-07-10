package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Venta;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.util.ArrayList;

/**
 * Capa de persistencia para {@link Venta}.
 * Almacena en memoria todas las ventas registradas en la tienda.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Ventas {

    private ArrayList<Venta> ventas;

    public Ventas() {
        this.ventas = new ArrayList<>();
    }

    public void agregar(Venta v) throws PersistenciaException {
        if (v == null)
            throw new PersistenciaException("Venta inválida.");
        v.setIdVenta(siguienteId());
        ventas.add(v);
    }

    /** Calcula el siguiente id auto-incremental (máximo id existente + 1). */
    public int siguienteId() {
        return GeneradorId.siguienteId(ventas, Venta::getIdVenta);
    }

    public Venta buscar(int idVenta) throws PersistenciaException {
        for (Venta v : ventas)
            if (v.getIdVenta() == idVenta) return v;
        throw new PersistenciaException("Venta no encontrada: id=" + idVenta);
    }

    public void actualizar(Venta v) throws PersistenciaException {
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getIdVenta() == v.getIdVenta()) {
                ventas.set(i, v);
                return;
            }
        }
        throw new PersistenciaException("Venta no encontrada para actualizar.");
    }

    /**
     * Inactiva (borrado lógico) una venta: ya no se remueve de la lista,
     * se reutiliza {@link Venta#anular()} para dejarla en estado ANULADA.
     */
    public void inactivar(int idVenta) throws PersistenciaException {
        Venta v = buscar(idVenta);
        v.anular();
    }

    /** Retorna solo las ventas con estado ACTIVA. */
    public ArrayList<Venta> listarActivas() {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas)
            if (v.getEstado() == Venta.Estado.ACTIVA) resultado.add(v);
        return resultado;
    }

    /** Retorna solo las ventas con estado ANULADA. */
    public ArrayList<Venta> listarAnuladas() {
        ArrayList<Venta> resultado = new ArrayList<>();
        for (Venta v : ventas)
            if (v.getEstado() == Venta.Estado.ANULADA) resultado.add(v);
        return resultado;
    }

    public ArrayList<Venta> listar() {
        return ventas;
    }

    public int conteo() {
        return ventas.size();
    }
}