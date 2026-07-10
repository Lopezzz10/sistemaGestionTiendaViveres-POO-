package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Inventario;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.util.ArrayList;

/**
 * Capa de persistencia para {@link Inventario}.
 * Almacena en memoria el stock de cada producto registrado.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Inventarios {

    private ArrayList<Inventario> inventarios;

    public Inventarios() {
        this.inventarios = new ArrayList<>();
    }

    public void agregar(Inventario inv) throws PersistenciaException {
        if (inv == null)
            throw new PersistenciaException("Inventario inválido.");
        if (buscarPorProducto(inv.getProducto().getCodigo()) != null)
            throw new PersistenciaException("Ya existe inventario para: "
                    + inv.getProducto().getCodigo());
        inv.setIdInventario(siguienteId());
        inventarios.add(inv);
    }

    /** Calcula el siguiente id auto-incremental (máximo id existente + 1). */
    public int siguienteId() {
        return GeneradorId.siguienteId(inventarios, Inventario::getIdInventario);
    }

    public Inventario buscar(int idInventario) throws PersistenciaException {
        for (Inventario inv : inventarios)
            if (inv.getIdInventario() == idInventario) return inv;
        throw new PersistenciaException("Inventario no encontrado: id=" + idInventario);
    }

    /** Busca el inventario por código de producto. Retorna null si no existe. */
    public Inventario buscarPorProducto(String codigoProducto) {
        for (Inventario inv : inventarios)
            if (inv.getProducto() != null &&
                    inv.getProducto().getCodigo().equalsIgnoreCase(codigoProducto))
                return inv;
        return null;
    }

    public void actualizar(Inventario inv) throws PersistenciaException {
        for (int i = 0; i < inventarios.size(); i++) {
            if (inventarios.get(i).getIdInventario() == inv.getIdInventario()) {
                inventarios.set(i, inv);
                return;
            }
        }
        throw new PersistenciaException("Inventario no encontrado para actualizar.");
    }

    /**
     * Inactiva (borrado lógico) un registro de inventario: ya no se remueve
     * de la lista, solo se marca como inactivo.
     */
    public void inactivar(int idInventario) throws PersistenciaException {
        Inventario inv = buscar(idInventario);
        inv.setActivo(false);
    }

    /** Retorna solo los inventarios activos (equivalente a WHERE activo = 1). */
    public ArrayList<Inventario> listarActivos() {
        ArrayList<Inventario> resultado = new ArrayList<>();
        for (Inventario inv : inventarios)
            if (inv.isActivo()) resultado.add(inv);
        return resultado;
    }

    /** Retorna inventarios cuyo stock está en o por debajo del umbral de alerta. */
    public ArrayList<Inventario> listarConAlerta() {
        ArrayList<Inventario> resultado = new ArrayList<>();
        for (Inventario inv : inventarios)
            if (inv.requiereAlerta()) resultado.add(inv);
        return resultado;
    }

    public ArrayList<Inventario> listar() { return inventarios; }

    public int conteo() { return inventarios.size(); }
}