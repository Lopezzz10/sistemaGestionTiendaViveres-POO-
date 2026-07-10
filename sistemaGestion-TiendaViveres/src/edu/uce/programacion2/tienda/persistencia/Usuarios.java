package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Usuario;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import edu.uce.programacion2.tienda.objetosServicio.GeneradorId;
import java.util.ArrayList;

/**
 * Capa de persistencia para {@link Usuario}.
 * Almacena en memoria todos los usuarios del sistema (Administradores y Cajeros).
 * La unicidad se valida por email.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Usuarios {

    private ArrayList<Usuario> usuarios;

    public Usuarios() {
        this.usuarios = new ArrayList<>();
    }

    public void agregar(Usuario u) throws PersistenciaException {
        if (u == null || u.getEmail().isEmpty())
            throw new PersistenciaException("Usuario inválido.");
        for (Usuario existente : usuarios)
            if (existente.getEmail().equalsIgnoreCase(u.getEmail()))
                throw new PersistenciaException("Usuario ya existe con email: " + u.getEmail());
        u.setIdUsuario(siguienteId());
        usuarios.add(u);
    }

    /** Calcula el siguiente id auto-incremental (máximo id existente + 1). */
    public int siguienteId() {
        return GeneradorId.siguienteId(usuarios, Usuario::getIdUsuario);
    }

    public Usuario buscar(int idUsuario) throws PersistenciaException {
        for (Usuario u : usuarios)
            if (u.getIdUsuario() == idUsuario) return u;
        throw new PersistenciaException("Usuario no encontrado: id=" + idUsuario);
    }

    public Usuario buscarPorEmail(String email) throws PersistenciaException {
        for (Usuario u : usuarios)
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        throw new PersistenciaException("Usuario no encontrado con email: " + email);
    }

    public void actualizar(Usuario u) throws PersistenciaException {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getIdUsuario() == u.getIdUsuario()) {
                usuarios.set(i, u);
                return;
            }
        }
        throw new PersistenciaException("Usuario no encontrado para actualizar.");
    }

    /**
     * Inactiva (borrado lógico) un usuario: ya no se remueve de la lista,
     * solo se marca como inactivo (por ejemplo, para revocar su acceso sin
     * perder el historial de sus ventas/acciones).
     */
    public void inactivar(int idUsuario) throws PersistenciaException {
        Usuario u = buscar(idUsuario);
        u.setActivo(false);
    }

    /** Retorna solo los usuarios activos (equivalente a WHERE activo = 1). */
    public ArrayList<Usuario> listarActivos() {
        ArrayList<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios)
            if (u.isActivo()) resultado.add(u);
        return resultado;
    }

    /** Filtra usuarios por permiso: "ADMIN" o "CAJERO". */
    public ArrayList<Usuario> listarPorPermiso(String permiso) {
        ArrayList<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios)
            if (u.getPermiso().equalsIgnoreCase(permiso)) resultado.add(u);
        return resultado;
    }

    public ArrayList<Usuario> listar() { return usuarios; }

    public int conteo() { return usuarios.size(); }
}