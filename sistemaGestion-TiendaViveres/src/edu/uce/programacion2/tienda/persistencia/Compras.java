package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Compra;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.util.ArrayList;

/**
 * Capa de persistencia para {@link Compra}.
 * Almacena en memoria todas las compras realizadas a proveedores.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Compras {

    private ArrayList<Compra> compras;

    public Compras() {
        this.compras = new ArrayList<>();
    }

    public void agregar(Compra c) throws PersistenciaException {
        if (c == null)
            throw new PersistenciaException("Compra inválida.");
        c.setIdCompra(siguienteId());
        compras.add(c);
    }

    /** Calcula el siguiente id auto-incremental (máximo id existente + 1). */
    public int siguienteId() {
        return GeneradorId.siguienteId(compras, Compra::getIdCompra);
    }

    public Compra buscar(int idCompra) throws PersistenciaException {
        for (Compra c : compras)
            if (c.getIdCompra() == idCompra) return c;
        throw new PersistenciaException("Compra no encontrada: id=" + idCompra);
    }

    public void actualizar(Compra c) throws PersistenciaException {
        for (int i = 0; i < compras.size(); i++) {
            if (compras.get(i).getIdCompra() == c.getIdCompra()) {
                compras.set(i, c);
                return;
            }
        }
        throw new PersistenciaException("Compra no encontrada para actualizar.");
    }

    /**
     * Inactiva (borrado lógico) una compra: ya no se remueve de la lista,
     * se reutiliza {@link Compra#anular()} para dejarla en estado ANULADA.
     */
    public void inactivar(int idCompra) throws PersistenciaException {
        Compra c = buscar(idCompra);
        c.anular();
    }

    /** Retorna solo las compras con estado PENDIENTE. */
    public ArrayList<Compra> listarPendientes() {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : compras)
            if (c.getEstado() == Compra.Estado.PENDIENTE) resultado.add(c);
        return resultado;
    }

    /** Retorna solo las compras con estado RECIBIDA. */
    public ArrayList<Compra> listarRecibidas() {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : compras)
            if (c.getEstado() == Compra.Estado.RECIBIDA) resultado.add(c);
        return resultado;
    }

    /** Retorna solo las compras activas (NO anuladas). */
    public ArrayList<Compra> listarActivas() {
        ArrayList<Compra> resultado = new ArrayList<>();
        for (Compra c : compras)
            if (c.getEstado() != Compra.Estado.ANULADA) resultado.add(c);
        return resultado;
    }

    public ArrayList<Compra> listar() { return compras; }

    public int conteo() { return compras.size(); }
}