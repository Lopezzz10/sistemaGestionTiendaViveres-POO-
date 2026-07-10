package edu.uce.programacion2.tienda.persistencia;

import edu.uce.programacion2.tienda.negocio.Producto;
import edu.uce.programacion2.tienda.excepciones.PersistenciaException;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Capa de persistencia BASE para todos los productos.
 * Define el CRUD genérico una sola vez.
 * ProductosPerecibles y ProductosNoPerecibles extienden esta clase
 * para no repetir código (herencia en persistencia).
 * @author Jose Manuel Lopez Olives, Wilmer Alexis Guachamín Vargas
 */
public class Productos {

    private ArrayList<Producto> productos;

    public Productos() {
        this.productos = new ArrayList<>();
    }

    public void agregar(Producto p) throws PersistenciaException {
        if (p == null || p.getCodigo().isEmpty())
            throw new PersistenciaException("Producto inválido.");
        if (productos.contains(p))
            throw new PersistenciaException("Producto ya existe: " + p.getCodigo());
        productos.add(p);
    }

    public Producto buscar(String codigo) throws PersistenciaException {
        for (Producto p : productos)
            if (p.getCodigo().equalsIgnoreCase(codigo)) return p;
        throw new PersistenciaException("Producto no encontrado: " + codigo);
    }

    public void actualizar(Producto p) throws PersistenciaException {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getCodigo().equalsIgnoreCase(p.getCodigo())) {
                productos.set(i, p);
                return;
            }
        }
        throw new PersistenciaException("Producto no encontrado para actualizar.");
    }

    /**
     * Inactiva (borrado lógico) un producto: ya no se remueve de la lista,
     * solo se marca su estado como {@link Producto#ESTADO_INACTIVO}.
     */
    public void inactivar(String codigo) throws PersistenciaException {
        Producto p = buscar(codigo);
        p.setEstado(Producto.ESTADO_INACTIVO);
    }

    /** Retorna solo los productos activos (equivalente a WHERE activo = 1). */
    public ArrayList<Producto> listarActivos() {
        ArrayList<Producto> resultado = new ArrayList<>();
        for (Producto p : productos)
            if (p.isActivo()) resultado.add(p);
        return resultado;
    }

    public ArrayList<Producto> listar() { return productos; }

    public int conteo() { return productos.size(); }

    /**
     * Consulta genérica con programación funcional: retorna todos los
     * productos que cumplan la condición recibida como {@link Predicate}.
     *
     * Al recibir la condición como parámetro (en vez de que esta clase
     * decida qué campos filtrar), este único método sirve para CUALQUIER
     * combinación de criterios — desde "un solo campo" hasta consultas con
     * varios parámetros a la vez, con solo cambiar el predicado que se le
     * pasa (ver {@link edu.uce.programacion2.tienda.negocio.FiltrosProducto}
     * y {@link edu.uce.programacion2.tienda.negocio.CriteriosProducto}).
     *
     * @param criterio función que decide si un producto pertenece al resultado
     * @return productos que cumplen el criterio (lista nueva, no afecta la original)
     */
    public ArrayList<Producto> buscarPor(Predicate<Producto> criterio) {
        return productos.stream()
                .filter(criterio)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}