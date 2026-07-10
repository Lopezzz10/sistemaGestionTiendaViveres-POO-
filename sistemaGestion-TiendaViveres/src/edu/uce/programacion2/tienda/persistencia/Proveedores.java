package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Proveedor;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.util.ArrayList;

/**
 * Capa de persistencia para {@link Proveedor}.
 * Almacena en memoria todos los proveedores registrados.
 * La unicidad se valida por RUC (identificador fiscal).
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Proveedores {

    private ArrayList<Proveedor> proveedores;

    public Proveedores() {
        this.proveedores = new ArrayList<>();
    }

    public void agregar(Proveedor p) throws PersistenciaException {
        if (p == null || p.getRuc().isEmpty())
            throw new PersistenciaException("Proveedor inválido.");
        for (Proveedor existente : proveedores)
            if (existente.getRuc().equalsIgnoreCase(p.getRuc()))
                throw new PersistenciaException("Proveedor ya existe con RUC: " + p.getRuc());
        p.setIdProveedor(siguienteId());
        proveedores.add(p);
    }

    /** Calcula el siguiente id auto-incremental (máximo id existente + 1). */
    public int siguienteId() {
        return GeneradorId.siguienteId(proveedores, Proveedor::getIdProveedor);
    }

    public Proveedor buscar(int idProveedor) throws PersistenciaException {
        for (Proveedor p : proveedores)
            if (p.getIdProveedor() == idProveedor) return p;
        throw new PersistenciaException("Proveedor no encontrado: id=" + idProveedor);
    }

    public Proveedor buscarPorRuc(String ruc) throws PersistenciaException {
        for (Proveedor p : proveedores)
            if (p.getRuc().equalsIgnoreCase(ruc)) return p;
        throw new PersistenciaException("Proveedor no encontrado con RUC: " + ruc);
    }

    public void actualizar(Proveedor p) throws PersistenciaException {
        for (int i = 0; i < proveedores.size(); i++) {
            if (proveedores.get(i).getIdProveedor() == p.getIdProveedor()) {
                proveedores.set(i, p);
                return;
            }
        }
        throw new PersistenciaException("Proveedor no encontrado para actualizar.");
    }

    /**
     * Inactiva (borrado lógico) un proveedor: ya no se remueve de la lista,
     * solo se marca como inactivo para conservar el historial de compras
     * asociadas a él.
     */
    public void inactivar(int idProveedor) throws PersistenciaException {
        Proveedor p = buscar(idProveedor);
        p.setActivo(false);
    }

    /** Retorna solo los proveedores activos (equivalente a WHERE activo = 1). */
    public ArrayList<Proveedor> listarActivos() {
        ArrayList<Proveedor> resultado = new ArrayList<>();
        for (Proveedor p : proveedores)
            if (p.isActivo()) resultado.add(p);
        return resultado;
    }

    public ArrayList<Proveedor> listar() { return proveedores; }

    public int conteo() { return proveedores.size(); }
}