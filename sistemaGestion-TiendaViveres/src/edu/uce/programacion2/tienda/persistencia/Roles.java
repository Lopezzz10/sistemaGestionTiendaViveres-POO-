package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Rol;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.util.ArrayList;

/**
 * Capa de persistencia para Rol.
 * Gestiona un ArrayList<Rol> con operaciones CRUD.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Roles {

    private ArrayList<Rol> roles;

    public Roles() {
        this.roles = new ArrayList<>();
    }

    public void agregar(Rol r) throws PersistenciaException {
        if (r == null || r.getNombreCargo() == null || r.getNombreCargo().trim().isEmpty()) {
            throw new PersistenciaException("Rol inválido.");
        }
        for (Rol existente : roles) {
            if (existente.getNombreCargo().equalsIgnoreCase(r.getNombreCargo())) {
                throw new PersistenciaException("Ya existe un rol con el cargo: " + r.getNombreCargo());
            }
        }
        r.setIdRol(siguienteId());
        roles.add(r);
    }

    /** Calcula el siguiente id auto-incremental (máximo id existente + 1). */
    public int siguienteId() {
        return GeneradorId.siguienteId(roles, Rol::getIdRol);
    }

    public Rol buscar(int idRol) throws PersistenciaException {
        for (Rol r : roles) {
            if (r.getIdRol() == idRol) return r;
        }
        throw new PersistenciaException("Rol no encontrado. Id: " + idRol);
    }

    public Rol buscarPorNombre(String nombreCargo) throws PersistenciaException {
        for (Rol r : roles) {
            if (r.getNombreCargo().equalsIgnoreCase(nombreCargo)) return r;
        }
        throw new PersistenciaException("Rol no encontrado: " + nombreCargo);
    }

    public void actualizar(Rol r) throws PersistenciaException {
        for (int i = 0; i < roles.size(); i++) {
            if (roles.get(i).getIdRol() == r.getIdRol()) {
                roles.set(i, r);
                return;
            }
        }
        throw new PersistenciaException("Rol no encontrado para actualizar.");
    }

    /** Inactiva (borrado lógico) un rol: se conserva el registro para historial. */
    public void inactivar(int idRol) throws PersistenciaException {
        Rol r = buscar(idRol);
        r.setActivo(false);
    }

    /** Retorna solo los roles activos. */
    public ArrayList<Rol> listarActivos() {
        ArrayList<Rol> resultado = new ArrayList<>();
        for (Rol r : roles)
            if (r.isActivo()) resultado.add(r);
        return resultado;
    }

    public ArrayList<Rol> listar() { return roles; }

    public int conteo() { return roles.size(); }
}
