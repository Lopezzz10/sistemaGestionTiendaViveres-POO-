package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Categoria;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import java.util.ArrayList;

/**
 * Capa de persistencia para Categoria.
 * Gestiona un ArrayList<Categoria> con operaciones CRUD.
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Categorias {

    private ArrayList<Categoria> categorias;

    public Categorias() {
        this.categorias = new ArrayList<>();
    }

    public void agregar(Categoria c) throws PersistenciaException {
        if (c == null || c.getCveCategoria().isEmpty()) {
            throw new PersistenciaException("Categoria inválida.");
        }
        if (categorias.contains(c)) {
            throw new PersistenciaException("Categoria ya existe: " + c.getCveCategoria());
        }
        categorias.add(c);
    }

    public Categoria buscar(String cveCategoria) throws PersistenciaException {
        for (Categoria c : categorias) {
            if (c.getCveCategoria().equalsIgnoreCase(cveCategoria)) return c;
        }
        throw new PersistenciaException("Categoria no encontrada: " + cveCategoria);
    }

    public void actualizar(Categoria c) throws PersistenciaException {
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getCveCategoria().equalsIgnoreCase(c.getCveCategoria())) {
                categorias.set(i, c);
                return;
            }
        }
        throw new PersistenciaException("Categoria no encontrada para actualizar.");
    }

    /**
     * Inactiva (borrado lógico) una categoría: ya no se remueve de la lista,
     * solo se marca como inactiva para preservar el historial y no romper
     * productos que ya la referencian.
     */
    public void inactivar(String cveCategoria) throws PersistenciaException {
        Categoria c = buscar(cveCategoria);
        c.setActivo(false);
    }

    /** Retorna solo las categorías activas (equivalente a WHERE activo = 1). */
    public ArrayList<Categoria> listarActivas() {
        ArrayList<Categoria> resultado = new ArrayList<>();
        for (Categoria c : categorias)
            if (c.isActivo()) resultado.add(c);
        return resultado;
    }

    public ArrayList<Categoria> listar() { return categorias; }

    public int conteo() { return categorias.size(); }
}